/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.authorization_module.impl;

import com.indexdata.authorization_module.PermissionsSource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;

/**
 *
 * @author kurt
 */
public class ModulePermissionsSource implements PermissionsSource {

  private String okapiUrl;
  private Vertx vertx;
  
  public ModulePermissionsSource(Vertx vertx) {
    //permissionsModuleUrl = url;
    this.vertx = vertx;
  }
  
  public void setOkapiUrl(String url) {
    okapiUrl = url;
  }
  
  @Override
  public Future<JsonArray> getPermissionsForUser(String username) {
    Future<JsonArray> future = Future.future();
    HttpClient client = vertx.createHttpClient();
    String requestUrl = okapiUrl + "/permissions/user/" + username + "/permissions";
    client.get(requestUrl, res-> {
      if(res.statusCode() == 200) {
        res.bodyHandler(res2 -> {
          JsonArray permissions = new JsonArray(res2.toString());
          future.complete(permissions);
        });
      }
    });
    return future;
  }

  
}
