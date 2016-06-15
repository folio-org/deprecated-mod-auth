package com.indexdata.okapi_modules;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface AuthStore {
    public Future<AuthResult> verifyLogin(JsonObject credentials);
    public Future<Boolean> updateLogin(JsonObject credentials, JsonObject metadata);
    public Future<Boolean> removeLogin(JsonObject credentials);
    public Future<Boolean> addLogin(JsonObject credentials, JsonObject metadata);
    public Future<JsonObject> getMetadata(JsonObject credentials);

}
