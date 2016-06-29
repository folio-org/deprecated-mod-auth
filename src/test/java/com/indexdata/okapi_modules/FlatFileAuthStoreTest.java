/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.okapi_modules;

import com.indexdata.okapi_modules.impl.FlatFileAuthStore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author kurt
 */
@RunWith(VertxUnitRunner.class)
public class FlatFileAuthStoreTest {

  private FlatFileAuthStore ffAS;
  JsonObject credentials;
  JsonObject authParams;
  
  @Before
  public void setUp(TestContext context) throws IOException {
    final Async async = context.async();
    //Guess who hates declaring JSON inline in Java? This guy.
    String jsonFileContents = 
            "[{\"username\":\"erikthered\",\"hash\":\"878978635FB7D8DD653B64AF0D174A496FFBAE37\",\"salt\":\"0EB926D24332F4D9\",\"metadata\":{\"permissions\":[\"auth_add_user\",\"auth_update_user\",\"auth_delete_user\"]}}]";
    
    File tempConfigFile = File.createTempFile("ffauthtest", ".json");
    PrintWriter dump = new PrintWriter(tempConfigFile.getAbsolutePath());
    dump.write(jsonFileContents);
    dump.close();
    credentials = new JsonObject()
            .put("username", "erikthered")
            .put("password", "ChickenMonkeyDuck");
    authParams = new JsonObject()
              .put("iterations", 1000)
              .put("keyLength", 160)
              .put("algorithm", "PBKDF2WithHmacSHA1");
    ffAS = new FlatFileAuthStore(tempConfigFile.getAbsolutePath(), authParams);
    async.complete();
  }  
  
  @Test
  public void testMetadata(TestContext context) {
    final Async async = context.async();
    ffAS.getMetadata(credentials).setHandler( res -> {
     JsonObject metadata = res.result();
     context.assertTrue(metadata.containsKey("permissions"));
     async.complete();
    });
  }
  
  @Test
  public void addUser(TestContext context) {
    final Async async = context.async();
    JsonObject newCreds = new JsonObject();
    newCreds.put("username", "brak");
    newCreds.put("password", "SpacePirate");
    JsonObject newMetadata = new JsonObject();
    newMetadata.put("permissions", new JsonArray().add("auth_add_user").add("auth_update_user"));
    ffAS.addLogin(newCreds, newMetadata);
    ffAS.getMetadata(newCreds).setHandler(res -> {
      context.assertTrue(res.result().getJsonArray("permissions").contains("auth_update_user"));
      ffAS.verifyLogin(newCreds).setHandler(res2 -> {
        context.assertTrue(res2.result().getSuccess());
        async.complete();
      });
    });
  }
  
  @Test
  public void modUser(TestContext context) {
    final Async async = context.async();
    JsonObject newCreds = new JsonObject();
    newCreds.put("username", "erikthered");
    JsonObject newMetadata = new JsonObject()
            .put("permissions", new JsonArray()
                    .add("auth_update_user")
                    .add("auth_delete_user")
             );
    ffAS.updateLogin(newCreds, newMetadata).setHandler(res -> {
      context.assertTrue(res.result());
      ffAS.getMetadata(newCreds).setHandler(res2 -> {
        context.assertTrue(res2.result().getJsonArray("permissions").contains("auth_delete_user"));      
        JsonObject loginCreds = new JsonObject()
                .put("username", "erikthered")
                .put("password", "ChickenMonkeyDuck");
        ffAS.verifyLogin(loginCreds).setHandler(res3-> {
          context.assertTrue(res3.result().getSuccess());
          async.complete();
        });
      });
    });
  }
    
  
  @After
  public void tearDown() {
    
  }
}
