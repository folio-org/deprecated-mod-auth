package com.indexdata.okapi_modules;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.DecodeException;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;

public class MainVerticle extends AbstractVerticle {

  /*
   * TODO: Find a better way to assign the signing secret. Perhaps in
   * a properties file or some other external configuration option.
   */
  private final String signingSecret = "all_zombies_must_dance";
  private AuthUtil authUtil = new AuthUtil();
  private AuthProvider authProvider = new DummyAuthProvider();
  private JWTAuth jwtAuth = null;
  private Long expires = 60L;
  private TokenStore tokenStore = new DummyTokenStore();
  @Override
  public void start(Future<Void> fut) {
  
    JsonObject jwtAuthConfig = new JsonObject()
      .put("keyStore", new JsonObject()
          .put("path", "keystore.jceks") //this resolves releative to our resources/ dir
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
    }
    JsonObject authInfo = new JsonObject().put("jwt", authToken);
    jwtAuth.authenticate(authInfo, result -> {
      if(!result.succeeded()) {
        System.out.println("Did not succeed");
        ctx.response().setStatusCode(400);
        ctx.response().end("Denied");
      
      } else {
        //Assuming that all is well, switch to chunked and return the content
        ctx.response().setChunked(true);
        ctx.response().setStatusCode(202);
        //Assign a handler that simply writes back the data
        ctx.request().handler( data -> {
          System.out.println("Writing data to response");
          ctx.response().write(data);
        });
      //Assign an end handler that closes the request
        ctx.request().endHandler( data -> {
          System.out.println("Closing response");
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
    if(!authProvider.verifyLogin(json).getSuccess()) {
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
            tokenStore.addToken(authToken, "expired");
          }
        }
    );
  }
}
