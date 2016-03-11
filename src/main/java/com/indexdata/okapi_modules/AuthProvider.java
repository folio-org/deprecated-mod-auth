package com.indexdata.okapi_modules;

import io.vertx.core.json.JsonObject;

public interface AuthProvider {
    public AuthResult verifyLogin(JsonObject credentials);

}
