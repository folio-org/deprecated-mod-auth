package com.indexdata.okapi_modules;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


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

}
