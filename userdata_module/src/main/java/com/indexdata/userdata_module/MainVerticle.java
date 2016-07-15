/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.userdata_module;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 *
 * @author kurt
 */
public class MainVerticle extends AbstractVerticle {
  private DataStore dataStore;
  
  public void start(Future<Void> future) {
    String mongoURL = System.getProperty("mongo.url", "mongodb://localhost:27017");
    MongoClient mongoClient = MongoClient.createShared(vertx, new JsonObject().put("connection_string", mongoURL));
    
    Router router = Router.router(vertx);
    router.route("/user").handler(BodyHandler.create());
    router.delete("/user/:username").handler(this::handleUser);
    router.put("/user/:username").handler(BodyHandler.create());
    router.put("/user/:username").handler(this::handleUser);
    router.route("/user").handler(this::handleUser);
  }
  
  public void handleUser(RoutingContext ctx) {
    JsonObject requestJson;
    if(ctx.request().method() == HttpMethod.POST || ctx.request().method() == HttpMethod.PUT) {
      String postContent = ctx.getBodyAsString();
      try {
        requestJson = new JsonObject(postContent);
      } catch (DecodeException dex) {
        ctx.response()
          .setStatusCode(400)
          .end("Unable to parse POST data as JSON");
        System.out.println("Bad JSON format for postdata: " + postContent);
        return;
      }
    }
    if(ctx.request().method() == HttpMethod.GET) {
      String username = ctx.request().getParam("username");
      dataStore.getUser().setHandler(res -> {
        if(!res.succeeded()) {
          ctx.response()
                  .setStatusCode(404)
                  .end("User not found");
          return;
        }
        JsonObject userData = res.result();
        ctx.response()
                .setStatusCode(200)
                .end(userData.encode());
        return;
      });
    }
  }
}

