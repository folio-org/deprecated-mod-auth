/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.okapi_modules.impl;

import com.indexdata.okapi_modules.AuthResult;
import com.indexdata.okapi_modules.AuthStore;
import com.indexdata.okapi_modules.AuthUtil;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 *
 * @author kurt
 */
public class MongoAuthStore implements AuthStore {
  
  private final MongoClient mongoClient;
  final private AuthUtil authUtil = new AuthUtil();
  private JsonObject authParams;
  
  public MongoAuthStore(MongoClient mongoClient, JsonObject authParams) {
    this.mongoClient = mongoClient;
    this.authParams = authParams;
  }
  
  private String calculateHash(String password, String salt) {
    return authUtil.calculateHash(password, salt, 
            authParams.getString("algorithm"),
            authParams.getInteger("iterations"),
            authParams.getInteger("keyLength")
    );
  }

  @Override
  public Future<AuthResult> verifyLogin(JsonObject credentials) {
    String username = credentials.getString("username");
    JsonObject query = new JsonObject().put("username", username);
    Future<AuthResult> future = Future.future();
    mongoClient.find("users", query, res -> {
      if(res.succeeded()) {
        JsonObject user = res.result().get(0);
        String storedHash = user.getString("hash");
        String storedSalt = user.getString("salt");
        String calculatedHash = calculateHash(credentials.getString("password"),
          storedSalt);
        if(calculatedHash.equals(storedHash)) {
          future.complete(new AuthResult(true, user.getJsonObject("metadata")));
        } else {
          future.complete(new AuthResult(false, user.getJsonObject("metadata")));
        }
      } else {
        //username not found
        future.complete(new AuthResult(false, null));
      }
    });
    return future;
  }

  @Override
  public Future<Boolean> updateLogin(JsonObject credentials, JsonObject metadata) {
    Future<Boolean> future = Future.future();
    String username = credentials.getString("username");
    JsonObject query = new JsonObject().put("username", username);
    JsonObject update = new JsonObject();
    String newSalt = authUtil.getSalt();
    if(credentials.containsKey("password")) {
      String password = credentials.getString("password");
      String newHash = calculateHash(password, newSalt);
      update
            .put("salt", newSalt)
            .put("hash", newHash);
    }
    if(metadata != null) {
      update.put("metadata", metadata);
    }
    mongoClient.update("users", query, update, res -> {
      if(res.succeeded()) {
        future.complete(Boolean.TRUE);
      } else {
        future.complete(Boolean.FALSE);
      }
    });
    
    return future;
  }

  @Override
  public Future<Boolean> removeLogin(JsonObject credentials) {
    Future<Boolean> future = Future.future();
    String username = credentials.getString("username");
    JsonObject query = new JsonObject().put("username", username);
    mongoClient.remove("users", query, res -> {
      if(res.succeeded()) {
        future.complete(Boolean.TRUE);
      } else {
        future.complete(Boolean.FALSE);
      }
    }); 
    return future;
  }

  @Override
  public Future<Boolean> addLogin(JsonObject credentials, JsonObject metadata) {
    Future<Boolean> future = Future.future();
    String username = credentials.getString("username");
    String password = credentials.getString("password");
    JsonObject query = new JsonObject().put("username", username);
    mongoClient.find("users", query, res -> {
      if(res.succeeded()) {
        //username already exists!
        future.complete(Boolean.FALSE);
      } else {
        String newSalt = authUtil.getSalt();
        String newHash = calculateHash(username, password);
        JsonObject insert = new JsonObject()
                .put("username", username)
                .put("password", password)
                .put("metadata", metadata);
        mongoClient.insert("users", insert, res2 -> {
          if(res2.succeeded()) {
            future.complete(Boolean.TRUE);
          } else {
            future.complete(Boolean.FALSE);
          }
        });     
      }
    });
    return future;
  }

  @Override
  public Future<JsonObject> getMetadata(JsonObject credentials) {
    Future<JsonObject> future = Future.future();
    String username = credentials.getString("username");
    JsonObject query = new JsonObject().put("username", username);
    mongoClient.find("users", query, res -> {
      if(res.succeeded()) {
        future.complete(res.result().get(0).getJsonObject("metadata"));
      } else {
        future.complete(null);
      }
    });
    
    return future;
  }
  
}
