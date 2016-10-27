/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.folio.auth.authentication_module.impl;

import io.vertx.core.Future;
import org.folio.auth.authentication_module.UserResult;
import org.folio.auth.authentication_module.UserSource;

/**
 *
 * @author kurt
 */
public class DummyUserSource implements UserSource {

  @Override
  public Future<UserResult> getUser(String username) {
    Future<UserResult> future = Future.future();
    future.complete(new UserResult(username));
    return future;
  }
  
}
