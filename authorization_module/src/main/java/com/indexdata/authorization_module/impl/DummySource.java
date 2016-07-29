/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.authorization_module.impl;

import com.indexdata.authorization_module.PermissionsSource;
import io.vertx.core.json.JsonArray;

/**
 *
 * @author kurt
 */
public class DummySource implements PermissionsSource {

  @Override
  public JsonArray getPermissionsForUser(String username, String tenant) {
    return new JsonArray(); //To change body of generated methods, choose Tools | Templates.
  }
  
}
