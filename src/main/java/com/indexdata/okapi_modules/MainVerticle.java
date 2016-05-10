package com.indexdata.okapi_modules;

import com.indexdata.okapi_modules.impl.DummyAuthStore;
import com.indexdata.okapi_modules.impl.FlatFileAuthStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import java.nio.file.Paths;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.jsonwebtoken.SignatureException;
import java.security.Key;


public class MainVerticle extends AbstractVerticle {

  /*
   * TODO: Find a better way to assign the signing secret. Perhaps in
   * a properties file or some other external configuration option.
   */
  private final String signingSecret = "all_zombies_must_dance";
  private final AuthUtil authUtil = new AuthUtil();
  private final Long expires = 600L;
  private final TokenStore tokenStore = new DummyTokenStore();
  private final Logger logger = LoggerFactory.getLogger("auth_module");
  private final String PERMISSIONS_HEADER = "X-Okapi-Permissions";
  private final String ADD_USER_PERMISSION = "auth_add_user";
  private final String UPDATE_USER_PERMISSION = "auth_update_user";
  private final String DELETE_USER_PERMISSION = "auth_delete_user";
  
  private Key key = MacProvider.generateKey();
  
  private AuthStore authStore;
  private JWTAuth jwtAuth = null;
  private boolean standalone = false;
  
  @Override
  public void start(Future<Void> fut) {
    String standaloneChoice = System.getProperty("standalone", "false");
    if(standaloneChoice.equals("true")) {
      standalone = true;
    }
    String authStoreChoice = System.getProperty("authType", "dummy");
    if(authStoreChoice.equals("flatfile")) {
      String authSecretsProp = System.getProperty("secretsFilepath", "authSecrets.json");
      String authSecretsPath;
      if(Paths.get(authSecretsProp).isAbsolute()) {
        authSecretsPath = authSecretsProp;
      } else {
        authSecretsPath = this.getClass().getClassLoader().getResource(authSecretsProp).getFile();
      }
      System.out.println("Using '" + authSecretsPath + "' for user secrets");
      authStore = new FlatFileAuthStore(authSecretsPath);
    } else {
      authStore = new DummyAuthStore();
    }
    JsonObject jwtAuthConfig = new JsonObject()
      .put("keyStore", new JsonObject()
          .put("path", "keystore.jceks") //this resolves relative to our resources/ dir
          .put("type", "jceks")
          .put("password", signingSecret)
      );

    jwtAuth = JWTAuth.create(vertx, jwtAuthConfig);
    tokenStore.addStore("expired");
    Router router = Router.router(vertx);

    router.post("/token").handler(BodyHandler.create()); //Allow us to read the POST data
    router.post("/token").handler(this::handleCreateToken);
    router.route("/*").handler(this::handleAuth);
    router.route("/user").handler(BodyHandler.create());
    router.route("/user").handler(this::handleUser);
    router.post("/expire").handler(BodyHandler.create());
    router.post("/expire").handler(this::handleExpireToken);
    router.get("/dummy").handler(this::handleDummy);
    
    HttpServer server = vertx.createHttpServer();
    final int port = Integer.parseInt(System.getProperty("port", "8081"));
    server.requestHandler(router::accept).listen(port, result -> {
        if(result.succeeded()) {
          fut.complete();
        } else {
          fut.fail(result.cause());
        }
    });  
  }

  /*
   * The handler to actually check that a JWT token is valid. If it is,
   * return a status of 202 and write the request content (if any)
   * back to the response
   *
   * It is possible that we could do some of this with vert.x's built-in
   * JWT Auth handler, but we need to make sure it can return data
   * in the method required by Okapi
   */
  private void handleAuth(RoutingContext ctx) {
    String authHeader = ctx.request().headers().get("Authorization");
    String authToken = authUtil.extractToken(authHeader);
    if(authToken == null) {
      ctx.response().setStatusCode(400);
      ctx.response().end("No valid JWT token found. Header should be in 'Authorization: Bearer' format.");
      return;
    }
    // Is the token in our listing of expired tokens? 
    if(tokenStore.hasToken(authToken, "expired")) {
        ctx.response()
          .setStatusCode(400)
          .setStatusMessage("Token is expired")
          .end();
        return;
    }
    //JsonObject authInfo = new JsonObject().put("jwt", authToken);
    try {
      Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
    } catch (SignatureException s) {
        logger.debug("JWT auth did not succeed");
        ctx.response().setStatusCode(400);
        ctx.response().setStatusMessage("Invalid token");
        ctx.response().end();
        System.out.println(authToken + " is not valid");
        return;
    }
      
    String username = authUtil.getClaims(authToken).getString("sub");
    JsonObject metadata = authStore.getMetadata(new JsonObject().put("username", username));
    JsonArray permissions = metadata.getJsonArray("permissions");
    //If we have a list of permissions stored for this username, assign them as a header
    if(metadata != null && permissions != null) {
      System.out.println("Assigning metadata header containing: " + permissions.encode());
      ctx.response().putHeader(PERMISSIONS_HEADER, permissions.encode());
    } else {
      System.out.println("No permission header assigned for request");
    }
    //Assuming that all is well, switch to chunked and return the content
    if(!this.standalone) {
      ctx.response().setChunked(true);
      ctx.response().setStatusCode(202);
      //Assign a handler that simply writes back the data
      ctx.request().handler( data -> {
        ctx.response().write(data);
      });
    //Assign an end handler that closes the request
      ctx.request().endHandler( data -> {
        ctx.response().end();
      });
    } else {
      ctx.next();
    }
      
  }

  /*
   * The handler for our POST request that actually gets a new JWT. Takes
   * JSON with the fields 'username' and 'password' and asks for verification
   * against the AuthUtil::verifyLogin() method. If verification is successful
   * we generate a token and return it in the Authorization: Bearer <token>
   * header format.
   *
   * We write the post content back to the response. This seems to be necessary
   * for proper operation with Okapi
   */
  private void handleCreateToken(RoutingContext ctx) {
    final String postContent = ctx.getBodyAsString();
    JsonObject json = null;
    try {
      json = new JsonObject(postContent);
    } catch(DecodeException dex) {
      ctx.response().setStatusCode(400);
      ctx.response().end("Unable to decode '" + postContent + "' as valid JSON");
      return;
    }
    if(!json.containsKey("username") || !json.containsKey("password")) {
      ctx.response().setStatusCode(400);
      ctx.response().end("POST must be in JSON format and contain fields for both username and password");
      return;
    }
    if(!authStore.verifyLogin(json).getSuccess()) {
        ctx.response().setStatusCode(403);
        ctx.response().setStatusMessage("Invalid credentials").end();
        return;
    }
    ctx.response().setStatusCode(200);
    String token = Jwts.builder().setSubject(json.getString("username")).signWith(SignatureAlgorithm.HS256, key).compact();
    /*
    String token = jwtAuth.generateToken(
        new JsonObject().put("sub", json.getString("username")),
        new JWTOptions().setExpiresInSeconds(expires)
    );
    */
    ctx.response().putHeader("Authorization", "Bearer " + token);
    System.out.println("Generated new token: " + token);
    ctx.response().end(postContent);
  }

  /*
   * Take a POSTed token to expire. Instead of plucking the token
   * directly from the header, we'll use one specified in the POST
   * data, in order to allow for a privileged 'admin' user with a
   * non-matching token to expire a different token 
   * */
  private void handleExpireToken(RoutingContext ctx) {
    final String postContent = ctx.getBodyAsString();
    JsonObject json = null;
    String authToken = authUtil.extractToken(ctx.request().headers().get("Authorization"));
    if(authToken == null) {
      ctx.response()
        .setStatusCode(400)
        .setStatusMessage("No valid JWT token found.")
        .end();
      return;
    }
    /*
     * TODO: Add some way to check if we have a 'superuser'
     * token to permit expiring of a non-possessed token
     */
    if(tokenStore.hasToken(authToken, "expired")) {
        ctx.response().setStatusCode(400);
        ctx.response().setStatusMessage("Token is already expired").end();
        return;
    }
    try {
      json = new JsonObject(postContent);
    } catch(DecodeException dex) {
      ctx.response().setStatusCode(400);
      ctx.response().setStatusMessage("Unable to decode payload as valid JSON").end();
      return;
    }
    if(!json.containsKey("token")) {
        ctx.response().setStatusCode(400);
        ctx.response().setStatusMessage("POST JSON must contain the field 'token'").end();
        return;
    }
    if(!authToken.equals(json.getString("token"))) {
      ctx.response()
        .setStatusCode(400)
        .setStatusMessage("Token to expire does not match actual token")
        .end();
      return;
    }
    final String expireToken = json.getString("token");
    jwtAuth.authenticate(
        new JsonObject().put("jwt", authToken),
        result -> {
          if(!result.succeeded()) {
            ctx.response()
              .setStatusCode(400)
              .setStatusMessage("Denied")
              .end();
          } else {
            ctx.response()
              .setStatusCode(200)
              .putHeader("Authorization", "Bearer " + authToken)
              .end(postContent);
            tokenStore.addToken(expireToken, "expired");
          }
        }
    );
  }
  
  /*
    Handle user creation and modification,
    if supported by the current backend
  */
  private void handleUser(RoutingContext ctx) {
    final String postContent = ctx.getBodyAsString();
    JsonArray permissions = null;
    try {
      permissions = new JsonArray(ctx.request().getHeader(PERMISSIONS_HEADER));
    } catch(Exception e) {
      //maybe log something?
    }
    JsonObject postJson = null;
    try {
      postJson = new JsonObject(postContent);
    } catch (DecodeException dex) {
      ctx.response()
        .setStatusCode(400)
        .setStatusMessage("Unable to parse POST data as JSON")
        .end();
      return;
    }

    if(ctx.request().method() == HttpMethod.POST) {
      if(permissions == null || !permissions.contains(ADD_USER_PERMISSION)) {
        ctx.response()
          .setStatusCode(401)
          .setStatusMessage("You do not have permission to add new users")
          .end();
        return;
      }
      System.out.println("Calling addLogin");
      boolean success = authStore.addLogin(postJson.getJsonObject("credentials"), postJson.getJsonObject("metadata"));
      if(success) {
        ctx.response().setStatusCode(200).end(postContent);
        return;
      } else {
        ctx.response().setStatusCode(400).setStatusMessage("Unable to add user").end();
        return;
      }
    } else if(ctx.request().method() == HttpMethod.PUT) {
      if(permissions == null || !permissions.contains(UPDATE_USER_PERMISSION)) {
        ctx.response().setStatusCode(401).setStatusMessage("You do not have permission to modify users").end();
        return;
      }
      boolean success = authStore.updateLogin(postJson.getJsonObject("credentials"), postJson.getJsonObject("metadata"));
      if(success) {
        ctx.response().setStatusCode(200).end(postContent);
        return;
      } else {
        ctx.response().setStatusCode(400).end("Unable to update user");
        return;
      }
    } else if(ctx.request().method() == HttpMethod.DELETE) {
      if(permissions == null || !permissions.contains(DELETE_USER_PERMISSION)) {
        ctx.response().setStatusCode(401).setStatusMessage("You do not have permission to delete users").end();
        return;
      }
      boolean success = authStore.removeLogin(postJson.getJsonObject("credentials"));
      if(success) {
        ctx.response().setStatusCode(200).end(postContent);
        return;
      } else {
        ctx.response().setStatusCode(400).setStatusMessage("Unable to remove user").end();
        return;
      }
    } else {
      ctx.response()
        .setStatusCode(400)
        .setStatusMessage("Operation unsupported")
        .end();
      return;
    }
  }
  
  private void handleDummy(RoutingContext ctx) {
    ctx.response()
      .setStatusCode(200)
      .end("{\"message\" : \"Dummy\"}");
    return;
  }
}
