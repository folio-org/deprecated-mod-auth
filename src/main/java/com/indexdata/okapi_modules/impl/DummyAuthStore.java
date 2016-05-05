package com.indexdata.okapi_modules.impl;

import com.indexdata.okapi_modules.AuthResult;
import io.vertx.core.json.JsonObject;
import com.indexdata.okapi_modules.AuthStore;

public class DummyAuthStore implements AuthStore {
  
  public AuthResult verifyLogin(JsonObject credentials) {
    AuthResult authResult = new AuthResult(true);
    authResult.setMetadata(null);
    return authResult;
  }

  @Override
  public boolean updateLogin(JsonObject credentials, JsonObject metadata) {
    return true;
  }

  @Override
  public boolean removeLogin(JsonObject credentials) {
    return true;
  }

  @Override
  public boolean addLogin(JsonObject credentials, JsonObject metadata) {
    return true;
  }

  @Override
  public JsonObject getMetadata(JsonObject credentials) {
    return null;
  }
}
