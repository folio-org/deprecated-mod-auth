package com.indexdata.okapi_modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class AuthUtil {
  /*
   * Authentication stub method. At some point, this needs to become
   * a lot more complex, as it could potentially need to handle
   * several auth backends. Also need to consider how it might pass
   * relevent information back (e.g. SAML token) for inclusion into
   * the JWT
   */
  public boolean verifyLogin(String username, String password) {
    return true; //EVERY LOGIN IS VALID. OMG.
  }

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

}
