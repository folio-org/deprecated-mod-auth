package com.indexdata.okapi_modules;

import io.vertx.core.json.JsonObject;

public class AuthResult {
  private final boolean success;
  private JsonObject metadata;

  public AuthResult(boolean success) {
    this.success = success;
  }

  public AuthResult(boolean success, JsonObject metadata) {
    this.success = success;
    this.metadata = metadata;
  }

  public boolean getSuccess() { return success; }

  public JsonObject getMetadata() { return metadata; }

  public AuthResult setMetadata(JsonObject metadata) {
    this.metadata = metadata;
    return this;
  }

}
