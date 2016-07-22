
import com.indexdata.authorization_module.PermissionsSource;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.vertx.core.AbstractVerticle;
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
import java.util.ArrayList;
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
  
  private Key JWTSigningKey = MacProvider.generateKey(JWTAlgorithm);
  private static final SignatureAlgorithm JWTAlgorithm = SignatureAlgorithm.HS512;
  PermissionsSource permissionsSource;
 
  public void start(Future<Void> future) {
    Router router = Router.router(vertx);
    HttpServer server = vertx.createHttpServer();
    String keySetting = System.getProperty("jwt.signing.key");
    if(keySetting != null) {
      JWTSigningKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(keySetting), JWTAlgorithm.getJcaName());
    }
    final int port = Integer.parseInt(System.getProperty("port", "8081"));
    
    router.route("/token").handler(BodyHandler.create());
    router.route("/token").handler(this::handleToken);
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
      Map<String, Object> claims = new HashMap<>();
      String tenant = ctx.request().headers().get("X-Okapi-Tenant");
      claims.put("tenant", tenant);
      String token = Jwts.builder()
              .signWith(JWTAlgorithm, JWTSigningKey)
              .setClaims(claims)
              .setSubject(payload.getString("sub"))
              .compact();
      ctx.response().setStatusCode(200)
              .putHeader("Authorization", "Bearer " + token)
              .end(postContent);
      return;
    } else {
      ctx.response().setStatusCode(400)
              .end("Unsupported operation: " + ctx.request().method().toString());
      return;
    }
  }  
  
  private void handleAuthorize(RoutingContext ctx) {
    String authHeader = ctx.request().headers().get("Authorization");
    String authToken = extractToken(authHeader);
    if(authToken == null) {
      ctx.response().setStatusCode(400)
              .end("No valid JWT token found. Header should be in 'Authorization: Bearer' format.");
      return;
    }
    JwtParser parser = null;
    try {
      parser = Jwts.parser().setSigningKey(JWTSigningKey);
      parser.parseClaimsJws(authToken);
    } catch (io.jsonwebtoken.MalformedJwtException|SignatureException s) {
        //logger.debug("JWT auth did not succeed");
        ctx.response().setStatusCode(400)
                .end("Invalid token");
        //System.out.println(authToken + " is not valid");
        return;
    }
    String username = getClaims(authToken).getString("sub");
    String tenant = ctx.request().headers().get("X-Okapi-Tenant");
    
    //get user permissions
    JsonArray permissions = permissionsSource.getPermissionsForUser(username, tenant);
    
    /* TODO get module permissions (if they exist) */
    
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
    
    //Check that for all required permissions, we have them
    for(Object o : permissionsRequired) {
      if(!permissions.contains((String)o)) {
        ctx.response()
                .putHeader("Content-Type", "text/plain")
                .setStatusCode(403)
                .end("Access requires permission: " + (String)o);
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
    
    //Return header containing relevant permissions
    ctx.response().setChunked(true)
            .setStatusCode(202)
            .putHeader(PERMISSIONS_HEADER, permissions.encode())
            .end(ctx.getBodyAsString());
    return;
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
  
}
