/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.authorization_module.impl;

import com.indexdata.authorization_module.PermissionsSource;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

/**
 *
 * @author kurt
 */
public class DummyPermissionsSource implements PermissionsSource {

  @Override
  public Future<JsonArray> getPermissionsForUser(String username) {
    Future<JsonArray> future = Future.future();
    future.complete(new JsonArray());
    return future;
  }

  @Override
  public void setOkapiUrl(String url) {
    return;
  }

  @Override
  public void setRequestToken(String token) {
    return;
  }

  @Override
  public void setAuthApiKey(String key) {
    return; 
  }

  
}
