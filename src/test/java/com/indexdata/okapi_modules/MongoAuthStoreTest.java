/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.okapi_modules;

import com.indexdata.okapi_modules.impl.MongoAuthStore;
import io.vertx.core.json.JsonObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
//import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.mongo.MongoClient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.flapdoodle.embed.mongo.config.ArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import org.junit.AfterClass;

/**
 *
 * @author kurt
 */

@RunWith(VertxUnitRunner.class)
public class MongoAuthStoreTest {
  private MongoAuthStore mAS;
  private MongoClient mongoClient;
  private JsonObject credentials;
  private JsonObject authParams;
  
  private Vertx vertx;  
  private static MongodProcess MONGO;
  private static int MONGO_PORT = 12345;
  
  @BeforeClass
  public static void initialize() throws IOException {
    MongodStarter starter = MongodStarter.getDefaultInstance();
    IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
            .build();
    MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
    MONGO = mongodExecutable.start();
  }
  
  @AfterClass
  public static void shutDown() {
    MONGO.stop();
  }
  
  @Before
  public void setUp(TestContext context) throws IOException {
    final Async async = context.async();
    JsonObject mongoConfig = new JsonObject();
    String host = "localhost";
    //mongoConfig.put("port", MONGO_PORT);
    //mongoConfig.put("host", host);
    mongoConfig.put("connection_string", "mongodb://localhost:" + MONGO_PORT);
    mongoConfig.put("db_name", "test_db");
   
    //mongoConfig.put("db_name", mongoDB.getName());
    vertx = Vertx.vertx();
    mongoClient = MongoClient.createShared(vertx, mongoConfig);
    credentials = new JsonObject()
            .put("username", "erikthered")
            .put("password", "ChickenMonkeyDuck");
    authParams = new JsonObject()
            .put("iterations", 1000)
            .put("keyLength", 160)
            .put("algorithm", "PBKDF2WithHmacSHA1");
    mAS = new MongoAuthStore(mongoClient, authParams);
    JsonObject newUser = new JsonObject()
            .put("username", "erikthered")
            .put("salt", "0EB926D24332F4D9")
            .put("hash", "878978635FB7D8DD653B64AF0D174A496FFBAE37")
            .put("metadata", new JsonObject()
                    .put("permissions", new JsonArray()
                            .add("auth_add_user")
                            .add("auth_update_user")
                            .add("auth_delete_user")
                    )
            );
    System.out.println("Adding new user");
    mongoClient.insert("users", newUser, res -> { 
      if(res.succeeded()) {
        async.complete();
      } else {
        context.fail();
        //throw new RuntimeException("Error inserting basic user");
      }
    });    
  }
  
  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }
  
  @Test
  public void testMetadata(TestContext context) {
    final Async async = context.async();
    mAS.getMetadata(credentials).setHandler(res -> {
      JsonObject metadata = res.result();
      System.out.println("Checking for permissions key");
      context.assertTrue(metadata.containsKey("permissions"));
      async.complete();
    });
  }
  
  @Test
  public void testVerifyLogin(TestContext context) {
    final Async async = context.async();
    mAS.verifyLogin(credentials).setHandler(res -> {
      AuthResult authResult = res.result();
      if(authResult.getSuccess()) {
        async.complete();
      } else {
        context.fail();
      }
    });
  }
}

