/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.okapi_modules.impl;

import com.indexdata.okapi_modules.AuthResult;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import com.indexdata.okapi_modules.AuthStore;
import com.indexdata.okapi_modules.AuthUtil;
import io.vertx.core.Future;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author kurt
 */
public class FlatFileAuthStore implements AuthStore {

  final private String secretsFilepath;
  //final private int iterations = 1000;
  //final private int keyLength = 160;
  //final private String algorithm = "PBKDF2WithHmacSHA1";
  final private AuthUtil authUtil = new AuthUtil();
  private ReentrantLock fileLock = new ReentrantLock();
  private JsonArray users = null;
  private JsonObject authParams;

  public FlatFileAuthStore(String secretsFilepath, JsonObject authParams) {
    this.secretsFilepath = secretsFilepath;
    this.authParams = authParams;
    deserializeFile();
  }

  /*
  Read user entries from a text file. 
  Entries are serialized as JSON in a list of objects.
  Clearly do not want to use this for more than a few users.
  Use salt and provided password to generate new hash. Check for match.
  */
  private CheckResult checkPassword(String username, String password) {
    fileLock.lock();
    try {
      if(users == null) {
        deserializeFile();
      }
      for(Object ob : users) {
        JsonObject jOb = (JsonObject)ob;
        if(!jOb.containsKey("username") || !jOb.getString("username").equals(username)) {
          continue;
        }
        String storedHash = jOb.getString("hash");
        String storedSalt = jOb.getString("salt");
        if(!calculateHash(password, storedSalt).equals(storedHash)) {
          return new CheckResult(false, true, null);
        }
        return new CheckResult(true, true, jOb.getJsonObject("metadata"));
       }
      return new CheckResult(false, false, null);
    } finally {
      fileLock.unlock();
    }
  }

  private void deserializeFile() {
    try {
      String userdata = new String(Files.readAllBytes(Paths.get(secretsFilepath)));
      this.users = new JsonArray(userdata);      
    } catch(IOException e) {
      throw new RuntimeException(e);
    } 
  }
  
  private void serializeFile() {
    String userdata = this.users.encodePrettily();
    System.out.println("Writing string '" + userdata + "' to file '" + secretsFilepath + "'");
    try {
        PrintWriter printWriter = new PrintWriter(secretsFilepath);
        printWriter.write(userdata);
        printWriter.flush();
        printWriter.close();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String calculateHash(String password, String salt) {
    return authUtil.calculateHash(password, salt, authParams.getString("algorithm"), 
            authParams.getInteger("iterations"), authParams.getInteger("keyLength"));
  }

  @Override
  public Future<AuthResult> verifyLogin(JsonObject credentials) {
    /*
    TODO: Implement caching
    */
    CheckResult checkResult = checkPassword(credentials.getString("username"), credentials.getString("password"));
    AuthResult authResult = new AuthResult(checkResult.isSuccess());
    authResult.setMetadata(checkResult.getMetadata());
    return Future.succeededFuture(authResult);
  }

  @Override
  public Future<Boolean> updateLogin(JsonObject credentials, JsonObject metadata) {
    fileLock.lock();
    String username = credentials.getString("username");
    try {
      if(this.users == null) {
        deserializeFile();
      }
      for(Object ob : this.users) {
        JsonObject jOb = (JsonObject)ob;
        if(!jOb.containsKey("username") || !jOb.getString("username").equals(username)) {
          continue;
        }
        //jOb.put("username",credentials.getString("username"));
        if(credentials.containsKey("password")) {
          String newSalt = authUtil.getSalt();
          String newHash = this.calculateHash(credentials.getString("password"), newSalt);
          jOb.put("salt", newSalt);
          jOb.put("hash", newHash);
        }
        if(metadata != null) {
          jOb.put("metadata", metadata);
        }
        serializeFile();
        return Future.succeededFuture(new Boolean(true));
      }
    } finally {
      fileLock.unlock();
    }
    System.out.println("Unable to locate user " + username + " to update");
    return Future.succeededFuture(new Boolean(false));
  }

  @Override
  public Future<Boolean> removeLogin(JsonObject credentials) {
    fileLock.lock();
    try {
      if(users == null) {
        deserializeFile();
      }
      for(Object ob : users) {
        JsonObject jOb = (JsonObject)ob;
        if(!jOb.containsKey("username") || !jOb.getString("username").equals(credentials.getString("username"))) {
          continue;
        }
        users.remove(jOb);
        serializeFile();
        return Future.succeededFuture(new Boolean(true));
      }
      System.out.println("Unable to locate " + credentials.getString("username") + " in users list");
      return Future.succeededFuture(new Boolean(false));
    } finally {
      fileLock.unlock();
    }
  }

  @Override
  public Future<Boolean> addLogin(JsonObject credentials, JsonObject metadata) {
    fileLock.lock();
    try {
      if(this.users == null) {
        deserializeFile();
      }
      for(Object ob : this.users) {
        JsonObject jOb = (JsonObject)ob;
        if(jOb.containsKey("username") && jOb.getString("username").equals(credentials.getString("username"))) {
          System.out.println(credentials.getString("username") + " already exists.");
          return Future.succeededFuture(new Boolean(false)); //Name already exists
        }
      }
      String salt = authUtil.getSalt();
      String hash = this.calculateHash(credentials.getString("password"), salt);
      JsonObject newUser = new JsonObject();
      newUser.put("username", credentials.getString("username"));
      newUser.put("salt", salt);
      newUser.put("hash", hash);
      newUser.put("metadata", metadata);
      this.users.add(newUser);
      serializeFile();
      return Future.succeededFuture(new Boolean(true));
    } finally {
      fileLock.unlock();
    }
  }

  @Override
  public Future<JsonObject> getMetadata(JsonObject credentials) {
    fileLock.lock();
    String username = credentials.getString("username");
    try {
      if(users == null) {
        deserializeFile();
      }
      for(Object ob : users) {
        JsonObject jOb = (JsonObject)ob;
        if(!jOb.containsKey("username") || !jOb.getString("username").equals(username)) {
          continue;
        }
        return Future.succeededFuture(jOb.getJsonObject("metadata"));
      }
      return Future.succeededFuture(null);
    } finally {
      fileLock.unlock();
    }
  }
  

  private static class CheckResult {

    final private boolean success;
    final private boolean exists;
    JsonObject metadata;

    public CheckResult(boolean success, boolean exists, JsonObject metadata) {
      this.success = success;
      this.exists = exists;
      this.metadata = metadata;
    }

    public boolean isSuccess() {
      return success;
    }

    public boolean isExists() {
      return exists;
    }

    public JsonObject getMetadata() {
      return metadata;
    }

  }

}
