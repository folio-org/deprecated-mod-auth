/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.userdata_module;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author kurt
 */
public interface DataStore {
  public Future<JsonObject> getUser();
  
}
