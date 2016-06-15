package com.indexdata.okapi_modules.impl;

import com.indexdata.okapi_modules.AuthResult;
import io.vertx.core.json.JsonObject;
import com.indexdata.okapi_modules.AuthStore;
import io.vertx.core.Future;

public class DummyAuthStore implements AuthStore {
  
  public Future<AuthResult> verifyLogin(JsonObject credentials) {
    AuthResult authResult = new AuthResult(true);
    authResult.setMetadata(null);
    return Future.succeededFuture(authResult);
  }

  @Override
  public Future<Boolean> updateLogin(JsonObject credentials, JsonObject metadata) {
    return Future.succeededFuture(new Boolean(true));
  }

  @Override
  public Future<Boolean> removeLogin(JsonObject credentials) {
    return Future.succeededFuture(new Boolean(true));
  }

  @Override
  public Future<Boolean> addLogin(JsonObject credentials, JsonObject metadata) {
    return Future.succeededFuture(new Boolean(true));
  }

  @Override
  public Future<JsonObject> getMetadata(JsonObject credentials) {
    return Future.succeededFuture(null);
  }
}
