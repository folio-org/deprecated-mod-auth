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

import io.vertx.core.impl.VertxInternal;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Key;
import javax.crypto.Mac;
import io.vertx.ext.auth.jwt.impl.JWT;

public class MainVerticle extends AbstractVerticle {

  private final String signingSecret = "secret";
  private AuthUtil authUtil = new AuthUtil();
  private JWTAuth jwtAuth = null;
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

    //router.post("/login").handler(this::handleLogin);
    //router.post("/validate").handler(this::handleValidation);
    router.get("/").handler(this::handleIndex);
    router.post("/token").handler(BodyHandler.create()); //Allow us to read the POST data
    router.post("/token").handler(this::handleCreateToken);
    
    HttpServer server = vertx.createHttpServer();
    server.requestHandler(router::accept).listen(8081);  
  }

  private void handleIndex(RoutingContext ctx) {
    ctx.response().setStatusCode(200);
    ctx.response().putHeader("content-type", "text/plain");
    ctx.response().end("<h1>Hello!</h1>");
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
        new JWTOptions()
    );
    ctx.response().putHeader("Content-Type", "text/plain");
    ctx.response().end(token);
  }

/*
  private void handleValidate(RoutingContext ctx) {
  }
*/

}

