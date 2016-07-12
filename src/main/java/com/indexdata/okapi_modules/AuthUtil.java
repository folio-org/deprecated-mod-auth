package com.indexdata.okapi_modules;

import com.sun.xml.internal.messaging.saaj.util.Base64;
import io.vertx.core.json.JsonObject;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;


public class AuthUtil {

  /*
   * Utility method to pull the Bearer token out of a header
   */
  public String extractToken(String authorizationHeader) {
    Pattern pattern = null;
    Matcher matcher = null;
    String authToken = null;
    if(authorizationHeader == null) { return null; }
    pattern = Pattern.compile("Bearer\\s+(.+)"); // Grab anything after 'Bearer' and whitespace
    matcher = pattern.matcher(authorizationHeader);
    if(matcher.find() && matcher.groupCount() > 0) {
      return matcher.group(1);
    }
    return null;
  }
  
  /*
   * Obtain a hash value based on a provided user password
   */
  public String calculateHash(String password, String salt, String algorithm, int iterations, int keyLength) {
    //public String calculateHash(String password, String salt) {
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
    
  public String getSalt() {
    SecureRandom random = new SecureRandom();
    byte bytes[] = new byte[20];
    random.nextBytes(bytes);
    return DatatypeConverter.printHexBinary(bytes);
  }
  
  public JsonObject getClaims(String jwt) {
    String encodedJson = jwt.split("\\.")[1];
    String decodedJson = Base64.base64Decode(encodedJson);
    return new JsonObject(decodedJson);    
  }

}
