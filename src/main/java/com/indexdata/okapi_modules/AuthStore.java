package com.indexdata.okapi_modules;

import io.vertx.core.json.JsonObject;

public interface AuthStore {
    public AuthResult verifyLogin(JsonObject credentials);
    public boolean updateLogin(JsonObject credentials, JsonObject metadata);
    public boolean removeLogin(JsonObject credentials);
    public boolean addLogin(JsonObject credentials, JsonObject metadata);

}
