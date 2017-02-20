package org.folio.auth.authtoken_module;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import com.jayway.restassured.RestAssured;
import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;


/**
 *
 * @author heikki
 */
@RunWith(VertxUnitRunner.class)
public class AuthTokenTest {
  private final Logger logger = LoggerFactory.getLogger("okapi");
  private static final String LS = System.lineSeparator();
  private static final String tenant = "Roskilde";
  private HttpClient httpClient;
  Vertx vertx;
  Async async;

  private final int port = Integer.parseInt(System.getProperty("port", "8081"));
  // TODO - Something wrong with passing the port around
  // The module defaults to 8081, so that's what we use here.

  @Before
  public void setUp(TestContext context) {
    logger.info("Setting up AuthTokenTest. Port=" + port);
    vertx = Vertx.vertx();
    JsonObject conf = new JsonObject()
      .put("port", port);

    DeploymentOptions opt = new DeploymentOptions()
      .setConfig(conf);
    vertx.deployVerticle(MainVerticle.class.getName(),
      opt, context.asyncAssertSuccess());
    httpClient = vertx.createHttpClient();
    RestAssured.port = port;
  }

  @After
  public void tearDown(TestContext context) {
    logger.info("Cleaning up after AuthTokenTest");
    async = context.async();
    vertx.close(x -> {
      async.complete();
    });

  }

  public AuthTokenTest() {
  }

  @Test
  public void test1(TestContext context) {
    async = context.async();
    logger.debug("test1 starting");

    RestAssuredClient c;
    Response r;

    // Simple request, mostly to see we can talk to the module
    given()
      .get("/foo") // any path should work
      .then()
      .statusCode(400)
      .body(containsString("Missing header: X-Okapi-Tenant"));

    // A request without X-Okapi-Url header.
    // Should return 400, returns 500 because the module crashes
    // in ModulePermissionsSource.setOkapiUrl(ModulePermissionsSource.java:39)
    given()
      .header("X-Okapi-Tenant", tenant)
      .get("/foo")
      .then()
      .statusCode(500);

    // A request that should succeed
    // Even without any credentials in the request, we get back the whole lot,
    // most notbaly a token that certifies the fact that we have a tenant, but
    // have not yet identified ourself.
    given()
      .header("X-Okapi-Tenant", tenant)
      .header("X-Okapi-Url", "http://localhost:9130")
      .get("/foo")
      .then()
      .statusCode(202)
      .header("X-Okapi-Permissions", "[]")
      .header("X-Okapi-Module-Tokens", startsWith("{\"_\":\""))
      .header("X-Okapi-Token", not(isEmptyString()))
      .header("Authorization", startsWith("Bearer "));


    String nodeListDoc = "[ {" + LS
      + "  \"nodeId\" : \"localhost\"," + LS
      + "  \"url\" : \"http://localhost:9129\"" + LS
      + "} ]";

    async.complete();
    logger.debug("test1 done");

  }


}
