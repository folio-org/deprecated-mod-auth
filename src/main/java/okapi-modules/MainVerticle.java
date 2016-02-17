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

  private final String signingSecret = "all_zombies_must_dance";
  private AuthUtil authUtil = new AuthUtil();
  private JWTAuth jwtAuth = null;
  private Long expires = 60L;
  @Override
  public void start(Future<Void> fut) {
  
    JsonObject jwtAuthConfig = new JsonObject()
      .put("keyStore", new JsonObject()
          .put("path", "keystore.jceks")
          .put("type", "jceks")
          .put("password", signingSecret)
      );

    jwtAuth = JWTAuth.create(vertx, jwtAuthConfig);

    Router router = Router.router(vertx);

    router.post("/token").handler(BodyHandler.create()); //Allow us to read the POST data
    router.post("/token").handler(this::handleCreateToken);
    router.get("/*").handler(this::handleAuth);
    router.get("/test").handler(this::handleTest);
    
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(router::accept).listen(8081);  
  }

  private void handleAuth(RoutingContext ctx) {
    String authToken = ctx.request().headers().get("X-Okapi-Authorization");
    JsonObject authInfo = new JsonObject().put("jwt", authToken);
    jwtAuth.authenticate(authInfo, result -> {
      if(!result.succeeded()) {
        System.out.println("Did not succeed");
        ctx.response().setStatusCode(400);
        ctx.response().end("Denied");
      
      } else {
        //Assuming that all is well, switch to chunked and return the content
        ctx.response().setChunked(true);
        ctx.response().setStatusCode(200);
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
    if(!authUtil.verifyLogin(json.getString("username"), json.getString("password"))) {
        ctx.response().setStatusCode(400);
        ctx.response().end("Invalid credentials");
    }
    ctx.response().setStatusCode(200);
    String token = jwtAuth.generateToken(
        new JsonObject().put("username", json.getString("username")),
        new JWTOptions().setExpiresInSeconds(expires)
    );
    ctx.response().putHeader("Content-Type", "text/plain");
    ctx.response().end(token);
  }

  private void handleTest(RoutingContext ctx) {
    ctx.response().setStatusCode(200);
    ctx.response().putHeader("Content-Type", "text/plain");
    ctx.response().end("You have passed the test.");
  }

/*
  private void handleValidate(RoutingContext ctx) {
  }
*/

}

