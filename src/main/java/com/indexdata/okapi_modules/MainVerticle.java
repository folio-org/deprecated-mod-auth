package com.indexdata.okapi_modules;

import com.indexdata.okapi_modules.impl.DummyAuthStore;
import com.indexdata.okapi_modules.impl.FlatFileAuthStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.DecodeException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import java.nio.file.Paths;


public class MainVerticle extends AbstractVerticle {

  /*
   * TODO: Find a better way to assign the signing secret. Perhaps in
   * a properties file or some other external configuration option.
   */
  private final String signingSecret = "all_zombies_must_dance";
  private final AuthUtil authUtil = new AuthUtil();
  private AuthStore authStore;
  private JWTAuth jwtAuth = null;
  private final Long expires = 60L;
  private final TokenStore tokenStore = new DummyTokenStore();
  private final Logger logger = LoggerFactory.getLogger("auth_module");
  @Override
  public void start(Future<Void> fut) {
  
    String authStoreChoice = System.getProperty("authType", "dummy");
    if(authStoreChoice.equals("flatfile")) {
      String authSecretsProp = System.getProperty("secretsFilepath", "authSecrets.txt");
      String authSecretsPath;
      if(Paths.get(authSecretsProp).isAbsolute()) {
        authSecretsPath = authSecretsProp;
      } else {
        authSecretsPath = this.getClass().getClassLoader().getResource(authSecretsProp).getFile();
      }
      logger.debug("Using '" + authSecretsPath + "' for user secrets");
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
    router.post("/expire").handler(BodyHandler.create());
    router.post("/expire").handler(this::handleExpireToken);
    router.route("/*").handler(this::handleAuth);
    
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
          .end("Token is expired");
        return;
    }
    JsonObject authInfo = new JsonObject().put("jwt", authToken);
    jwtAuth.authenticate(authInfo, result -> {
      if(!result.succeeded()) {
        logger.debug("JWT auth did not succeed");
        ctx.response().setStatusCode(400);
        ctx.response().end("Denied");
        return;
      } else {
        String username = authUtil.getClaims(authToken).getString("sub");
        JsonObject metadata = authStore.getMetadata(new JsonObject().put("username", username));
        //If we have a list of permissions stored for this username, assign them as a header
        if(metadata != null && metadata.getJsonArray("permissions") != null) {
          ctx.response().putHeader("X-Okapi-Permissions", metadata.getJsonArray("permissions").encode());
        }
        //Assuming that all is well, switch to chunked and return the content
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
    if(!authStore.verifyLogin(json).getSuccess()) {
        ctx.response().setStatusCode(400);
        ctx.response().end("Invalid credentials");
        return;
    }
    ctx.response().setStatusCode(200);
    String token = jwtAuth.generateToken(
        new JsonObject().put("username", json.getString("username")),
        new JWTOptions().setExpiresInSeconds(expires)
    );
    ctx.response().putHeader("Authorization", "Bearer " + token);
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
        ctx.response().end("Token is already expired");
        return;
    }
    try {
      json = new JsonObject(postContent);
    } catch(DecodeException dex) {
      ctx.response().setStatusCode(400);
      ctx.response().end("Unable to decode '" + postContent + "' as valid JSON");
      return;
    }
    if(!json.containsKey("token")) {
        ctx.response().setStatusCode(400);
        ctx.response().end("POST JSON must contain the field 'token'");
        return;
    }
    if(!authToken.equals(json.getString("token"))) {
      ctx.response()
        .setStatusCode(400)
        .end("Token to expire does not match actual token");
      return;
    }
    final String expireToken = json.getString("token");
    jwtAuth.authenticate(
        new JsonObject().put("jwt", authToken),
        result -> {
          if(!result.succeeded()) {
            ctx.response()
              .setStatusCode(400)
              .end("Denied");
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
}
