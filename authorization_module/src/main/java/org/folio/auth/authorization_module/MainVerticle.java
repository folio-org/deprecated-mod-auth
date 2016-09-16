package org.folio.auth.authorization_module;

import org.folio.auth.authorization_module.PermissionsSource;
import org.folio.auth.authorization_module.impl.DummyPermissionsSource;
import org.folio.auth.authorization_module.impl.ModulePermissionsSource;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kurt
 */
public class MainVerticle extends AbstractVerticle {
  private static final String PERMISSIONS_HEADER = "X-Okapi-Permissions";
  private static final String DESIRED_PERMISSIONS_HEADER = "X-Okapi-Permissions-Desired";
  private static final String REQUIRED_PERMISSIONS_HEADER = "X-Okapi-Permissions-Required";
  private static final String MODULE_PERMISSIONS_HEADER = "X-Okapi-Module-Permissions";
  private static final String CALLING_MODULE_HEADER = "X-Okapi-Calling-Module";
  private static final String MODULE_TOKENS_HEADER = "X-Okapi-Module-Tokens";
  private static final String OKAPI_URL_HEADER = "X-Okapi-URL";
  private static final String OKAPI_TOKEN_HEADER = "X-Okapi-Token";
  private static final String SIGN_TOKEN_PERMISSION = "auth.signtoken";
  
  private Key JWTSigningKey = MacProvider.generateKey(JWTAlgorithm);
  private static final SignatureAlgorithm JWTAlgorithm = SignatureAlgorithm.HS512;
  PermissionsSource permissionsSource;
  private String authApiKey;
  private String okapiUrl;
 
  public void start(Future<Void> future) {
    Router router = Router.router(vertx);
    HttpServer server = vertx.createHttpServer();
    authApiKey = System.getProperty("auth.api.key", "VERY_WEAK_KEY");
    
    String keySetting = System.getProperty("jwt.signing.key");
    if(keySetting != null) {
      //JWTSigningKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(keySetting), JWTAlgorithm.getJcaName());
      JWTSigningKey = new SecretKeySpec(keySetting.getBytes(), JWTAlgorithm.getJcaName());
    }
    permissionsSource = new ModulePermissionsSource(vertx);
    //permissionsSource = new DummyPermissionsSource();
    permissionsSource.setAuthApiKey(authApiKey);
    final int port = Integer.parseInt(System.getProperty("port", "8081"));
    
    //router.route("/token").handler(BodyHandler.create());
    //router.route("/token").handler(this::handleToken);
    router.route("/*").handler(BodyHandler.create());
    router.route("/*").handler(this::handleAuthorize);
    
    server.requestHandler(router::accept).listen(port, result -> {
        if(result.succeeded()) {
          future.complete();
        } else {
          future.fail(result.cause());
        }
    });  
  }
  

  private void handleToken(RoutingContext ctx) {
    
    updateOkapiUrl(ctx);
    if(ctx.request().method() == HttpMethod.POST) {
      final String postContent = ctx.getBodyAsString();
      JsonObject json = null;
      JsonObject payload = null;
      try {
        json = new JsonObject(postContent);
      } catch(DecodeException dex) {
        ctx.response().setStatusCode(400);
        ctx.response().end("Unable to decode '" + postContent + "' as valid JSON");
        return;
      }
      try {
        payload = json.getJsonObject("payload");
      } catch(Exception e) { }
      if(payload == null) {
        ctx.response().setStatusCode(400)
                .end("Valid 'payload' field is required");
        return;
      }
      if(!payload.containsKey("sub")) {
        ctx.response().setStatusCode(400)
                .end("Payload must contain a 'sub' field");
        return;
      }

      String tenant = ctx.request().headers().get("X-Okapi-Tenant");
      /*
      claims.put("tenant", tenant);
      String token = Jwts.builder()
              .signWith(JWTAlgorithm, JWTSigningKey)
              .setClaims(claims)
              .setSubject(payload.getString("sub"))
              .compact();
      */
      payload.put("tenant", tenant);
      String token = createToken(payload);
      
      ctx.response().setStatusCode(200)
              .putHeader("Authorization", "Bearer " + token)
              //.putHeader(OKAPI_TOKEN_HEADER, token)
              .end(postContent);
      return;
    } else {
      ctx.response().setStatusCode(400)
              .end("Unsupported operation: " + ctx.request().method().toString());
      return;
    }
  }  
  
  private void handleAuthorize(RoutingContext ctx) {
    if(ctx.request().path().startsWith("/perms/privileged")) {
      ctx.response()
              .setStatusCode(200)
              .end();
      return;
    }
    updateOkapiUrl(ctx);
    String requestToken = getRequestToken(ctx);
    String authHeader = ctx.request().headers().get("Authorization");
    String candidateToken = extractToken(authHeader);
    String tenant = ctx.request().headers().get("X-Okapi-Tenant");
    permissionsSource.setTenant(tenant);
    if(candidateToken == null) {
      //Generate a new "dummy" token
      DateFormat df = new SimpleDateFormat("MM/dd/yyyyTHH:mm:ss");
      Date now = Calendar.getInstance().getTime();
      JsonObject dummyPayload = new JsonObject()
              .put("sub", "UNDEFINED_USER__" + ctx.request().remoteAddress().toString() +
                      "__" + df.format(now))
              .put("tenant", tenant)
              .put("dummy", true);
      
      candidateToken = createToken(dummyPayload);
    }
    final String authToken = candidateToken;
    JwtParser parser = null;
    try {
      parser = Jwts.parser().setSigningKey(JWTSigningKey);
      parser.parseClaimsJws(authToken);
    } catch (io.jsonwebtoken.MalformedJwtException|SignatureException s) {
        //logger.debug("JWT auth did not succeed");
        ctx.response().setStatusCode(400)
                //.end("Invalid token");
                .end(); 
       //System.out.println(authToken + " is not valid");
        return;
    }
    
    System.out.println("Authz received token " + authToken);
    System.out.println("Token claims are " + getClaims(authToken).encode());
    
    /*
    Here, we're really basically saying that we are only going to allow access 
    to the /token endpoint if the request has a module-level permission defined
    for it. There really should be no other case for this endpoint to be accessed
    */
    
    JsonObject tokenClaims = getClaims(authToken);
    
    if(ctx.request().path().startsWith("/token")) {
      JsonArray extraPermissions = tokenClaims.getJsonArray("extra_permissions");
      if(extraPermissions == null || !extraPermissions.contains(SIGN_TOKEN_PERMISSION)) {
        //do nothing
      } else {
        handleToken(ctx);
        return;
      }
    }
   
    String username = tokenClaims.getString("sub");
    String jwtTenant = tokenClaims.getString("tenant");
    if(!jwtTenant.equals(tenant)) {
      System.out.println("Expected tenant: " + tenant + ", got tenant: " + jwtTenant);
      ctx.response()
              .setStatusCode(403)
              .end("Invalid token for access");
      return;
    }
    
    //Check and see if we have any module permissions defined
    final JsonArray extraPermissions = getClaims(authToken).getJsonArray("extra_permissions");
    
    
    //get user permissions
    //JsonArray permissions = 
    
    //Instead of storing tokens, let's store an array of objects that each
    
    JsonObject moduleTokens = new JsonObject();
    /* TODO get module permissions (if they exist) */
    if(ctx.request().headers().contains(MODULE_PERMISSIONS_HEADER)) {
      JsonObject modulePermissions = new JsonObject(ctx.request().headers().get(MODULE_PERMISSIONS_HEADER));
      System.out.println("Recieved module permissions are " + modulePermissions.encode());
      for(String moduleName : modulePermissions.fieldNames()) {
        JsonArray permissionList = modulePermissions.getJsonArray(moduleName);
        JsonObject tokenPayload = new JsonObject();
        tokenPayload.put("sub", username);
        tokenPayload.put("tenant", tenant);
        tokenPayload.put("module", moduleName);
        tokenPayload.put("extra_permissions", permissionList);
        String moduleToken = createToken(tokenPayload);
        moduleTokens.put(moduleName, moduleToken);
     }
    }
    
    //Add the original token back into the module tokens
    moduleTokens.put("_", authToken);
    //Populate the permissionsRequired array from the header
    JsonArray permissionsRequired = new JsonArray();
    JsonArray permissionsDesired = new JsonArray();
    
    if(ctx.request().headers().contains(REQUIRED_PERMISSIONS_HEADER)) {
      String permissionsString = ctx.request().headers().get(REQUIRED_PERMISSIONS_HEADER);
      for(String entry : permissionsString.split(",")) {
        permissionsRequired.add(entry);
      }
    }
    
    if(ctx.request().headers().contains(DESIRED_PERMISSIONS_HEADER)) {
      String permString = ctx.request().headers().get(DESIRED_PERMISSIONS_HEADER);
      for(String entry : permString.split(",")) {
        permissionsDesired.add(entry);
      }
    }
    
    PermissionsSource usePermissionsSource;
    if(tokenClaims.getBoolean("dummy") != null) {
      usePermissionsSource = new DummyPermissionsSource();
    } else {
      usePermissionsSource = permissionsSource;
    }
    
    //Retrieve the user permissions and populate the permissions header
    usePermissionsSource.getPermissionsForUser(username).setHandler((AsyncResult<JsonArray> res) -> {
      
      if(!res.succeeded()) {
        ctx.response()
                .setStatusCode(500)
                //.end("Unable to retrieve permissions for user");
                .end();
        return;
      }
      JsonArray permissions = res.result();
      System.out.println("Permissions for " + username + ": " + permissions.encode());
      if(extraPermissions != null) {
        for(Object o : extraPermissions)
        {
          permissions.add((String)o);
        }
      }
      
      //Check that for all required permissions, we have them
      for(Object o : permissionsRequired) {
        if(!permissions.contains((String)o)) {
          System.out.println("Authz: " + permissions.encode() + " does not contain " + (String)o);
          ctx.response()
                  //.putHeader("Content-Type", "text/plain")
                  .setStatusCode(403)
                  //.end("Access requires permission: " + (String)o);
                  .end();
          return;
        }
      }

      //Remove all permissions not listed in permissionsRequired or permissionsDesired
      List<Object> deleteList = new ArrayList<>();
      for(Object o : permissions) {
        if(!permissionsRequired.contains(o) && !permissionsDesired.contains(o)) {
          deleteList.add(o);
        }
      }

      for(Object o : deleteList) {
        permissions.remove(o);
      }

      //Create new JWT to pass back with request, include calling module field
      JsonObject claims = getClaims(authToken);

      if(ctx.request().headers().contains(CALLING_MODULE_HEADER)) {
        claims.put("calling_module", ctx.request().headers().get(CALLING_MODULE_HEADER));
      }

      String token = Jwts.builder()
              .signWith(JWTAlgorithm, JWTSigningKey)
              .setPayload(claims.encode())
              .compact();

      System.out.println("Returning header " + PERMISSIONS_HEADER + " with content " + permissions.encode());
      System.out.println("Returning header " + MODULE_TOKENS_HEADER + " with content " + moduleTokens.encode());
      System.out.println("Returning Authorization Bearer token with content " + claims.encode());
      //Return header containing relevant permissions
      ctx.response()
              .setChunked(true)
              .setStatusCode(202)
              .putHeader(PERMISSIONS_HEADER, permissions.encode())
              .putHeader(MODULE_TOKENS_HEADER, moduleTokens.encode())
              .putHeader("Authorization", "Bearer " + token)
              .end(ctx.getBodyAsString());
              //.end();
      return;
    });
  }
  
  private void updateOkapiUrl(RoutingContext ctx) {
    if(ctx.request().getHeader(OKAPI_URL_HEADER) != null) {
      this.okapiUrl = ctx.request().getHeader(OKAPI_URL_HEADER);
    }
    permissionsSource.setOkapiUrl(okapiUrl);
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
  
  public JsonObject getClaims(String jwt) {
    String encodedJson = jwt.split("\\.")[1];
    String decodedJson = Base64.base64Decode(encodedJson);
    return new JsonObject(decodedJson);    
  }
  
  private String createToken(JsonObject payload) {
    String token = Jwts.builder()
              .signWith(JWTAlgorithm, JWTSigningKey)
              .setPayload(payload.encode())
              .compact();
    return token;
  }
  
  private String getRequestToken(RoutingContext ctx) {
    String token = ctx.request().headers().get(OKAPI_TOKEN_HEADER);
    if(token == null) {
      return "";
    }
    return token;
  }  
  
}
