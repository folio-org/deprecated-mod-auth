/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.okapi_modules;

import com.indexdata.okapi_modules.impl.FlatFileAuthStore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author kurt
 */
public class FlatFileAuthStoreTest {

  private FlatFileAuthStore ffAS;
  JsonObject credentials;
  
  @Before
  public void setUp() throws IOException {
    //Guess who hates declaring JSON inline in Java? This guy.
    String jsonFileContents = 
            "[{\"username\":\"erikthered\",\"hash\":\"878978635FB7D8DD653B64AF0D174A496FFBAE37\",\"salt\":\"0EB926D24332F4D9\",\"metadata\":{\"permissions\":[\"auth_add_user\",\"auth_update_user\",\"auth_delete_user\"]}}]";
    
    File tempConfigFile = File.createTempFile("ffauthtest", ".json");
    PrintWriter dump = new PrintWriter(tempConfigFile.getAbsolutePath());
    dump.write(jsonFileContents);
    dump.close();
    credentials = new JsonObject();
    credentials.put("username", "erikthered");
    credentials.put("password", "ChickenMonkeyDuck");
    ffAS = new FlatFileAuthStore(tempConfigFile.getAbsolutePath());
  }  
  
  @Test
  public void testMetadata() {
    JsonObject metadata = ffAS.getMetadata(credentials);
    assert(metadata.containsKey("permissions"));
  }
  
  @Test
  public void addUser() {
    JsonObject newCreds = new JsonObject();
    newCreds.put("username", "brak");
    newCreds.put("password", "SpacePirate");
    JsonObject newMetadata = new JsonObject();
    newMetadata.put("permissions", new JsonArray().add("auth_add_user").add("auth_update_user"));
    ffAS.addLogin(newCreds, newMetadata);
    assert(ffAS.getMetadata(newCreds).getJsonArray("permissions").contains("auth_update_user"));
  }
  
  @After
  public void tearDown() {
    
  }
}
