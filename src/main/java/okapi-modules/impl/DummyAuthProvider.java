package com.indexdata.okapi_modules;

import io.vertx.core.json.JsonObject;

public class DummyAuthProvider implements AuthProvider {
  public AuthResult verifyLogin(JsonObject credentials) {
    AuthResult authResult = new AuthResult(true);
    authResult.setMetadata(null);
    return authResult;
  }
}
