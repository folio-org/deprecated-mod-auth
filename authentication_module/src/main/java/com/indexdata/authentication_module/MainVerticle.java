/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.authentication_module;

import com.indexdata.authentication_module.impl.DummyAuthSource;
import com.indexdata.authentication_module.impl.MongoAuthSource;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author kurt
 */
public class MainVerticle extends AbstractVerticle {
  private AuthSource authSource = null;
  private AuthUtil authUtil;
  private MongoClient mongoClient;
  private String okapiUrl;
  private String authApiKey;

  @Override
  public void start(Future<Void> future) {
    //authSource = new DummyAuthSource();
    authUtil = new AuthUtil();
    authApiKey = System.getProperty("auth.api.key", "VERY_WEAK_KEY");
    okapiUrl = System.getProperty("okapi.url", "http://localhost:9130");
    
    String mongoURL = System.getProperty("mongo.url", "mongodb://localhost:27017");
    mongoClient = MongoClient.createShared(vertx, new JsonObject().put("connection_string", mongoURL));
    authSource = new MongoAuthSource(mongoClient, authUtil);
    
    Router router = Router.router(vertx);
    router.post("/authn/login").handler(BodyHandler.create()); //Allow us to read the POST data
    router.post("/authn/login").handler(this::handleLogin);
    router.route("authn/users").handler(BodyHandler.create());
    router.post("authn/users").handler(this::handleUser);
    router.put("authn/users/:username").handler(this::handleUser);
    router.delete("authn/users/:username").handler(this::handleUser);
    
    HttpServer server = vertx.createHttpServer();
    final int port = Integer.parseInt(System.getProperty("port", "8081"));
    server.requestHandler(router::accept).listen(port, result -> {
        if(result.succeeded()) {
          future.complete();
        } else {
          future.fail(result.cause());
        }
    });  
  }
  
  private void handleLogin(RoutingContext ctx) {
    final String postContent = ctx.getBodyAsString();
    JsonObject json = null;
    try {
      json = new JsonObject(postContent);
    } catch(DecodeException dex) {
      ctx.response().setStatusCode(400);
      ctx.response().end("Unable to decode '" + postContent + "' as valid JSON");
      return;
    }
    authSource.authenticate(json).setHandler( res -> {
      AuthResult authResult = res.result();
      if(!authResult.getSuccess()) {
        ctx.response()
                .setStatusCode(403)
                .end("Invalid credentials");
        return;
      }
      //String token = Jwts.builder().setSubject(authResult.getUser()).signWith(JWTAlgorithm, JWTSigningKey).compact();
      JsonObject payload = new JsonObject()
              .put("sub", authResult.getUser());                 
      fetchToken(payload, okapiUrl + "/token", authApiKey).setHandler(result -> {
        String token = result.result();
        ctx.response()
              .putHeader("Authorization", token)
              .setStatusCode(200)
              .end(postContent); 
        return;
      });
    });
  }
  
  private void handleUser(RoutingContext ctx) {
    String requestBody = null;
    if(ctx.request().method() == HttpMethod.POST ||
            ctx.request().method() == HttpMethod.PUT) {
      requestBody = ctx.getBodyAsString();
    }
    if(ctx.request().method() == HttpMethod.POST) {
      JsonObject postData = new JsonObject(requestBody);
      JsonObject credentials = postData.getJsonObject("credentials");
      JsonObject metadata = postData.getJsonObject("metadata");
      authSource.addAuth(credentials, metadata).setHandler(res-> {
        if(!res.succeeded()) {
          ctx.response()
                  .setStatusCode(500)
                  .end("Unable to add user");
        } else {
          ctx.response()
                  .setStatusCode(201)
                  .end("Added user");
        }
      });
      
    } else if (ctx.request().method() == HttpMethod.PUT) {
      String username = ctx.request().getParam("username");
      JsonObject postData = new JsonObject(requestBody);
      JsonObject credentials = postData.getJsonObject("credentials");
      JsonObject metadata = postData.getJsonObject("metadata");
      if(!credentials.getString("username").equals(username)) {
        ctx.response()
                .setStatusCode(400)
                .end("Invalid user");
        return;
      }
      authSource.updateAuth(credentials, metadata).setHandler(res -> {
        if(!res.succeeded()) {
          ctx.response()
                  .setStatusCode(500)
                  .end("Unable to update user");
        } else {
          ctx.response()
                  .setStatusCode(200)
                  .end("Updated user");
        }
      });      
    } else if (ctx.request().method() == HttpMethod.DELETE) {
      String username = ctx.request().getParam("username");
      authSource.deleteAuth(username).setHandler(res -> {
        if(!res.succeeded()) {
          ctx.response()
                  .setStatusCode(500)
                  .end("Unable to remove user");
        } else {
          ctx.response()
                  .setStatusCode(200)
                  .end("Deleted user");
        }
      });
    } else {
      ctx.response()
              .setStatusCode(400)
              .end("Unsupported operation");
      return;
    }
  }
  /*
  Retrieve a token from our token generator...in this case, the Authorization
  module. We pass along a shared key, since this exists outside of the standard
  auth chain
  */
  private Future<String> fetchToken(JsonObject payload, String url, String key) {
    Future<String> future = Future.future();
    HttpClient client = vertx.createHttpClient();
    HttpClientRequest request = client.request(HttpMethod.POST, url);
    request.putHeader("AUTH_API_KEY", key)
            .end(payload.encode());
    request.handler(result -> {
      if(result.statusCode() != 200) {
        future.fail("Got error " + result.statusCode() + " fetching token");
        return;
      }
      String token = result.getHeader("Authorization");
      future.complete(token);
    });      
    return future;
  }
  
}