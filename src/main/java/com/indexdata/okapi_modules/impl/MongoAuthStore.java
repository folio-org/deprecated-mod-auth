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
        String calculatedHash = authUtil.calculateHash(
                credentials.getString("password"),
                storedSalt,
                authParams.getString("algorithm"),
                authParams.getInteger("iterations"),
                authParams.getInteger("keyLength")     
        );
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
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Future<Boolean> removeLogin(JsonObject credentials) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Future<Boolean> addLogin(JsonObject credentials, JsonObject metadata) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Future<JsonObject> getMetadata(JsonObject credentials) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
