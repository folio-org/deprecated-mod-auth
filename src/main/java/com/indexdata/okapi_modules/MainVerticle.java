package com.indexdata.okapi_modules;

import com.indexdata.okapi_modules.impl.DummyAuthStore;
import com.indexdata.okapi_modules.impl.FlatFileAuthStore;
import com.indexdata.okapi_modules.impl.MongoAuthStore;
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
//import io.vertx.ext.auth.jwt.JWTAuth;
//import io.vertx.ext.auth.jwt.JWTOption;
import java.nio.file.Paths;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.JwtParser;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.mongo.MongoClient;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;


public class MainVerticle extends AbstractVerticle {

  /*
   * TODO: Find a better way to assign the signing secret. Perhaps in
   * a properties file or some other external configuration option.
   */
  //private final String signingSecret = "all_zombies_must_dance";
  private final AuthUtil authUtil = new AuthUtil();
  private final Long expires = 600L;
  private final TokenStore tokenStore = new DummyTokenStore();
  private final Logger logger = LoggerFactory.getLogger("auth_module");
  private static final String PERMISSIONS_HEADER = "X-Okapi-Permissions";
  private static final String ADD_USER_PERMISSION = "auth_add_user";
  private static final String UPDATE_USER_PERMISSION = "auth_update_user";
  private static final String DELETE_USER_PERMISSION = "auth_delete_user";
  private static final String DESIRED_PERMISSIONS_HEADER = "X-Okapi-Permissions-Desired";
  private static final String REQUIRED_PERMISSIONS_HEADER = "X-Okapi-Permissions-Required";
  private static final SignatureAlgorithm JWTAlgorithm = SignatureAlgorithm.HS512;

  private Key JWTSigningKey = MacProvider.generateKey(JWTAlgorithm);

  private AuthStore authStore;
  //private JWTAuth jwtAuth = null;
  private boolean standalone = false;

  @Override
  public void start(Future<Void> fut) {
    String keySetting = System.getProperty("jwtSigningKey");
    if(keySetting != null) {
      JWTSigningKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(keySetting), JWTAlgorithm.getJcaName());
    }
    String standaloneChoice = System.getProperty("standalone", "false");
    if(standaloneChoice.equals("true")) {
      standalone = true;
    }
    String authParamsFilepath = System.getProperty("authParamsFilepath", null);
    JsonObject authParams;
    if(authParamsFilepath == null) {
      authParams = new JsonObject()
              .put("iterations", 1000)
              .put("keyLength", 160)
              .put("algorithm", "PBKDF2WithHmacSHA1");
    } else {
      try {
        authParams = new JsonObject(new String(Files.readAllBytes(Paths.get(authParamsFilepath))));
      } catch(IOException ioe) {
        throw new RuntimeException(ioe);
      }
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
      authStore = new FlatFileAuthStore(authSecretsPath, authParams);
    } else if(authStoreChoice.equals("mongo")) {
      String mongoURL = System.getProperty("mongoURL", "mongodb://localhost:27017");
      MongoClient mongoClient = MongoClient.createShared(vertx, new JsonObject().put("connection_string", mongoURL));
      authStore = new MongoAuthStore(mongoClient, authParams);
    } else {
      authStore = new DummyAuthStore();
    }

    /*
    JsonObject jwtAuthConfig = new JsonObject()
      .put("keyStore", new JsonObject()
          .put("path", "keystore.jceks") //this resolves relative to our resources/ dir
          .put("type", "jceks")
          .put("password", signingSecret)
      );
    */

    //jwtAuth = JWTAuth.create(vertx, jwtAuthConfig);
    tokenStore.addStore("expired");
    Router router = Router.router(vertx);

    router.post("/token").handler(BodyHandler.create()); //Allow us to read the POST data
    router.post("/token").handler(this::handleCreateToken);
    router.route("/user").handler(BodyHandler.create());
    router.delete("/user/:username").handler(this::handleUser);
    router.put("/user/:username").handler(BodyHandler.create());
    router.put("/user/:username").handler(this::handleUser);
    router.route("/user").handler(this::handleUser);
    router.route("/*").handler(this::handleAuth);
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

  private JsonObject checkAuthRequest(HttpServerRequest request) {
    JsonObject result = new JsonObject();
    result.put("headers", new JsonObject());

    String authHeader = request.headers().get("Authorization");
    String authToken = authUtil.extractToken(authHeader);
    if(authToken == null) {
      result.put("errorCode", 400);
      result.put("message", "No valid JWT token found. Header should be in 'Authorization: Bearer' format.");
      return result;
    }
    // Is the token in our listing of expired tokens?
    if(tokenStore.hasToken(authToken, "expired")) {
      result.put("errorCode", 400);
      result.put("message", "Token is expired.");
      return result;
    }

    JwtParser parser = null;
    try {
      parser = Jwts.parser().setSigningKey(JWTSigningKey);
      parser.parseClaimsJws(authToken);
    } catch (io.jsonwebtoken.MalformedJwtException|SignatureException s) {
        logger.debug("JWT auth did not succeed");
        result.put("errorCode", 400);
        result.put("message", "Invalid token.");
        System.out.println(authToken + " is not valid");
        return result;
    }

    String username = authUtil.getClaims(authToken).getString("sub");
    JsonObject metadata = authStore.getMetadata(new JsonObject().put("username", username)).result();
    JsonArray permissions = null;
    if(metadata != null) {
      permissions = metadata.getJsonArray("permissions");
    }
    //If we have a list of permissions stored for this username, assign them as a header
    if(metadata != null && permissions != null) {
      System.out.println("Assigning metadata header containing: " + permissions.encode());
      result.getJsonObject("headers").put(PERMISSIONS_HEADER, permissions.encode());
    } else {
      System.out.println("No permission header assigned for request");
    }
    return result;
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
    System.out.println("Calling handleAuth");
    JsonObject authResult = this.checkAuthRequest(ctx.request());
    JsonArray permissionsRequired = new JsonArray();
    //JsonArray permissionsDesired;

    /*
    for(String name : ctx.request().headers().names()) {
      String value = ctx.request().headers().get(name);
      System.out.println("Request has header '" + name + "' with value '" + value + "'");
    }
    */
    if(ctx.request().headers().contains(REQUIRED_PERMISSIONS_HEADER)) {
      String permissionsString = ctx.request().headers().get(REQUIRED_PERMISSIONS_HEADER);
      for(String entry : permissionsString.split(",")) {
        permissionsRequired.add(entry);
      }
    }

    if(authResult.containsKey("errorCode")) {
      ctx.response().putHeader("Content-Type", "text/plain");
      ctx.response().setStatusCode(authResult.getInteger("errorCode"));
      ctx.response().end(authResult.getString("message"));
      return;
    }
    System.out.println("authResult is " + authResult.encode());
    System.out.println("Checking for required permissions: " + permissionsRequired.encode());
    for(Object o : permissionsRequired) {
      JsonArray providedPermissions = null;
      if(authResult.getJsonObject("headers").containsKey(PERMISSIONS_HEADER)) {
        providedPermissions = new JsonArray(authResult.getJsonObject("headers").getString(PERMISSIONS_HEADER));
      }
      if(providedPermissions == null || !providedPermissions.contains((String)o)) {
        ctx.response()
                .putHeader("Content-Type", "text/plain")
                .setStatusCode(403)
                .end("Access requires permission: " + (String)o);
        return;
      }
    }

    //Assuming that all is well, switch to chunked and return the content
    if(authResult.containsKey("headers")) {
      JsonObject headers = authResult.getJsonObject("headers");
      for(String key : headers.fieldNames()) {
        ctx.response().putHeader(key, headers.getString(key));
        System.out.println("Adding header " + key + " with value of " + headers.getString(key));
      }
    }
    //ctx.response().putHeader(PERMISSIONS_HEADER, authResult.getJsonObject("headers").getString(PERMISSIONS_HEADER));
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
    if(!authStore.verifyLogin(json).result().getSuccess()) {
        ctx.response().setStatusCode(403);
        ctx.response().end("Invalid credentials");
        return;
    }
    ctx.response().setStatusCode(200);
    String token = Jwts.builder().setSubject(json.getString("username")).signWith(JWTAlgorithm, JWTSigningKey).compact();
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
        .end("No valid JWT token found.");
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
      ctx.response().setStatusMessage("Unable to decode payload as valid JSON object").end();
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
    JwtParser parser = null;
    try {
      parser = Jwts.parser().setSigningKey(JWTSigningKey);
      parser.parseClaimsJws(authToken);
    } catch(io.jsonwebtoken.MalformedJwtException|SignatureException e) {
      ctx.response()
              .setStatusCode(403)
              .end("Denied");
      return;
    }
    ctx.response()
            .setStatusCode(200)
            .putHeader("Authorization", "Bearer " + authToken)
            .end(postContent);
    tokenStore.addToken(expireToken, "expired");
    /*
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
    */
  }

  /*
    Handle user creation and modification,
    if supported by the current backend
  */
  private void handleUser(RoutingContext ctx) {
    System.out.println("Calling handleUser");
    JsonObject authResult = this.checkAuthRequest(ctx.request());
    if(authResult.containsKey("errorCode")) {
      ctx.response()
        .setStatusCode(authResult.getInteger("errorCode"))
        .end(authResult.getString("message"));
      return;
    }
    final String postContent = ctx.getBodyAsString();
    JsonArray permissions = null;
    try {
      permissions = new JsonArray(authResult.getJsonObject("headers").getString(PERMISSIONS_HEADER));
      //permissions = new JsonArray(ctx.request().getHeader(PERMISSIONS_HEADER));
    } catch(Exception e) {
      //maybe log something?
    }
    JsonObject postJson = null;
    if(ctx.request().method() == HttpMethod.POST || ctx.request().method() == HttpMethod.PUT) {
      try {
        postJson = new JsonObject(postContent);
      } catch (DecodeException dex) {
        ctx.response()
          .setStatusCode(400)
          .end("Unable to parse POST data as JSON");
        System.out.println("Bad JSON format for postdata: " + postContent);
        return;
      }
    }

    //System.out.println("Permissions and postcontent successfully received");

    if(ctx.request().method() == HttpMethod.POST) {
      if(permissions == null || !permissions.contains(ADD_USER_PERMISSION)) {
        ctx.response()
          .setStatusCode(401)
          .setStatusMessage("You do not have permission to add new users")
          .end();
        return;
      }
      System.out.println("Calling addLogin");
      boolean success = authStore.addLogin(postJson.getJsonObject("credentials"), postJson.getJsonObject("metadata")).result();
      if(success) {
        ctx.response().setStatusCode(200).end(postContent);
        return;
      } else {
        ctx.response().setStatusCode(400).end("Unable to add user");
        return;
      }
    } else if(ctx.request().method() == HttpMethod.PUT) {
      if(permissions == null || !permissions.contains(UPDATE_USER_PERMISSION)) {
        ctx.response().setStatusCode(401).setStatusMessage("You do not have permission to modify users").end();
        return;
      }
      String username = ctx.request().getParam("username");
      if(!username.equals(postJson.getJsonObject("credentials").getString("username"))) {
        ctx.response()
                .setStatusCode(400)
                .end("Username in credentials does not match named user");
        return;
      }
      boolean success = authStore.updateLogin(postJson.getJsonObject("credentials"), postJson.getJsonObject("metadata")).result();
      if(success) {
        ctx.response().setStatusCode(200).end(postContent);
        return;
      } else {
        ctx.response().setStatusCode(400).end("Unable to update user");
        return;
      }
    } else if(ctx.request().method() == HttpMethod.DELETE) {
      System.out.println("Deletion requested");
      if(permissions == null || !permissions.contains(DELETE_USER_PERMISSION)) {
        ctx.response().setStatusCode(401).end("You do not have permission to delete users");
        return;
      }
      String username = ctx.request().getParam("username");
      System.out.println("Calling removeLogin");
      boolean success = authStore.removeLogin(new JsonObject().put("username", username)).result();
      //boolean success = authStore.removeLogin(postJson.getJsonObject("credentials"));
      if(success) {
        ctx.response().setStatusCode(200).end();
        return;
      } else {
        ctx.response().setStatusCode(400).end("Unable to remove user " + username);
        return;
      }
    } else {
      ctx.response()
        .setStatusCode(400)
        .end("Operation unsupported");
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
