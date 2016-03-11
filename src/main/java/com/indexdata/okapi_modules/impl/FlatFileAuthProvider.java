/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.indexdata.okapi_modules.impl;

import com.indexdata.okapi_modules.AuthProvider;
import com.indexdata.okapi_modules.AuthResult;
import io.vertx.core.json.JsonObject;
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


/**
 *
 * @author kurt
 */
public class FlatFileAuthProvider implements AuthProvider {

  final private String secretsFilepath;
  final private int iterations = 1000;
  final private int keyLength = 160;
  final private String algorithm = "PBKDF2WithHmacSHA1";

  public FlatFileAuthProvider(String secretsFilepath) {
    this.secretsFilepath = secretsFilepath;
  }

  @Override
  public AuthResult verifyLogin(JsonObject credentials) {
    CheckResult checkResult = checkPassword(credentials.getString("username"), credentials.getString("password"));
    AuthResult authResult = new AuthResult(checkResult.isSuccess());
    JsonObject metadata = new JsonObject();
    metadata.put("authLevel", checkResult.getAuthLevel());
    return authResult;
  }

  /*
  Read user entries from a text file. Lines are tab separated, in the following format:
  <username>\t<hash>\t<salt>\t<auth_level>
  Use salt and provided password to generate new hash. Check for match.
  */
  private CheckResult checkPassword(String username, String password){
    try {
      FileInputStream secretsInputStream = new FileInputStream(secretsFilepath);
      BufferedReader reader = new BufferedReader(new InputStreamReader(secretsInputStream));
      String readLine;
      while((readLine = reader.readLine()) != null) {
        String[] parts = readLine.trim().split("\t");
        if(parts[0].equals(username)) {
          String storedHash = parts[1];
          String storedSalt = parts[2];
          int authLevel = Integer.parseInt(parts[3]);
          if(calculateHash(password, storedSalt).equals(storedHash)) {
            return new CheckResult(true, true, authLevel);
          } else {
            return new CheckResult(false, true, authLevel); //Should this be set to zero?
          }
        } else {
          continue;
        }
      }
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
    return new CheckResult(false, false, 0);
  }

  private String calculateHash(String password, String salt) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), DatatypeConverter.parseHexBinary(salt), iterations, keyLength);
    byte[] hash;
    try {
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithm);
      hash = keyFactory.generateSecret(spec).getEncoded();
    } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
    return DatatypeConverter.printHexBinary(hash);
  }

  private static class CheckResult {

    final private boolean success;
    final private boolean exists;
    final private int authLevel;

    public CheckResult(boolean success, boolean exists, int authLevel) {
      this.success = success;
      this.exists = exists;
      this.authLevel = authLevel;
    }

    public boolean isSuccess() {
      return success;
    }

    public boolean isExists() {
      return exists;
    }

    public int getAuthLevel() {
      return authLevel;
    }

  }

}
