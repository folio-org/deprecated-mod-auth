/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.authentication_module.impl;

import com.indexdata.authentication_module.AuthResult;
import com.indexdata.authentication_module.AuthSource;
import com.indexdata.authentication_module.AuthUtil;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 *
 * @author kurt
 */
public class MongoAuthSource implements AuthSource {
  private final MongoClient mongoClient;
  private AuthUtil authUtil;
  
  public MongoAuthSource(MongoClient mongoClient, AuthUtil authUtil) {
    this.mongoClient = mongoClient;
    this.authUtil = authUtil;
  }

  @Override
  public Future<AuthResult> authenticate(JsonObject credentials) {
    String username, password;
    username = credentials.getString("username");
    password = credentials.getString("password");
    if(username == null || password == null) {
      return Future.failedFuture("Credentials must contain a username and password");
    }
    JsonObject query = new JsonObject().put("username", username);
    Future<AuthResult> future = Future.future();
    mongoClient.find("credentials", query, res -> {
      if(res.succeeded()) {
        JsonObject user = res.result().get(0);
        String storedHash = user.getString("hash");
        String storedSalt = user.getString("salt");
        String calculatedHash = authUtil.calculateHash(password, storedSalt);
        if(calculatedHash.equals(storedHash)) {
          future.complete(new AuthResult(true, username, user.getJsonObject("metadata")));
        } else {
          future.complete(new AuthResult(false, username, user.getJsonObject("metadata")));
        }
      } else {
        //username not found
        future.complete(new AuthResult(false, username, null));
      }
    });
    return future;
  }

  @Override
  public Future<Boolean> addAuth(JsonObject credentials, JsonObject metadata) {
    Future<Boolean> future = Future.future();
    String username = credentials.getString("username");
    String password = credentials.getString("password");
    JsonObject query = new JsonObject().put("username", username);
    mongoClient.find("credentials", query, res -> {
      if(res.succeeded()) {
        //username already exists!
        future.complete(Boolean.FALSE);
      } else {
        String newSalt = authUtil.getSalt();
        String newHash = authUtil.calculateHash(username, password);
        JsonObject insert = new JsonObject()
                .put("username", username)
                .put("password", password)
                .put("metadata", metadata);
        mongoClient.insert("credentials", insert, res2 -> {
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
  public Future<Boolean> updateAuth(JsonObject credentials, JsonObject metadata) {
    Future<Boolean> future = Future.future();
    String username = credentials.getString("username");
    JsonObject query = new JsonObject().put("username", username);
    JsonObject update = new JsonObject();
    String newSalt = authUtil.getSalt();
    if(credentials.containsKey("password")) {
      String password = credentials.getString("password");
      String newHash = authUtil.calculateHash(password, newSalt);
      update
            .put("salt", newSalt)
            .put("hash", newHash);
    }
    if(metadata != null) {
      update.put("metadata", metadata);
    }
    mongoClient.update("credentials", query, update, res -> {
      if(res.succeeded()) {
        future.complete(Boolean.TRUE);
      } else {
        future.complete(Boolean.FALSE);
      }
    });
    
    return future;
  }

  @Override
  public Future<Boolean> deleteAuth(String username) {
    Future<Boolean> future = Future.future();
    JsonObject query = new JsonObject().put("username", username);
    mongoClient.remove("credentials", query, res -> {
      if(res.succeeded()) {
        future.complete(Boolean.TRUE);
      } else {
        future.complete(Boolean.FALSE);
      }
    }); 
    return future;
  }
  
}
