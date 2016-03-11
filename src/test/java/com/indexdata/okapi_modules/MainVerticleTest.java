package com.indexdata.okapi_modules;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {
  private Vertx vertx;
  private AuthUtil authUtil;

  @Before
  public void setUp(TestContext context) {
    authUtil = new AuthUtil();
    vertx = Vertx.vertx();
    System.out.println("Deploying main verticle");
    vertx.deployVerticle(MainVerticle.class.getName(),
        context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  /*
   * Pretty basic. Ask for a token and check to see that we have one in the proper format
   */
  @Test
  public void testApplication(TestContext context) {
    final Async async = context.async();
    System.out.println("Creating http client to test POST");
    vertx.createHttpClient().post(8081, "localhost", "/token", response -> {
      context.assertEquals(200, response.statusCode());
      response.endHandler( x -> {
        context.assertNotNull(authUtil.extractToken(response.headers().get("Authorization")));
        async.complete();
      });
    }).end("{\"username\":\"joe\", \"password\":\"schmoe\"}");
  }

}


