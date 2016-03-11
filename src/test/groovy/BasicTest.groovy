package com.indexdata.okapi_modules

import io.vertx.core.Vertx;
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.unit.TestContext
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.ContentType


@RunWith(VertxUnitRunner.class)
class BasicTest {
  def vertx
  def httpClient
  def authUtil

  @Before
  void setUp(TestContext context) {
    java.lang.System.setProperty("authType", "flatfile")
    //java.lang.System.setProperty("secretsFilepath", "/tmp/authSecrets.txt")
    java.lang.System.setProperty("loglevel", "debug")
    vertx = Vertx.vertx()
    vertx.deployVerticle(MainVerticle.class.getName(), context.asyncAssertSuccess())
    httpClient = new HTTPBuilder("http://localhost:8081")
    authUtil = new AuthUtil()
  }

  @After
  void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess())
  }
  
  @Test
  void doBasicTest(TestContext context) {
    assert true == true
  }

  @Test
  void getAndUseToken(TestContext context) {
    def token;
    //Get a token
    httpClient.request( Method.POST, ContentType.JSON ) { req ->
      uri.path = '/token'
      body = [
        username : 'erikthered',
        password : 'ChickenMonkeyDuck'
      ]

      response.success = { resp, json ->
        println resp.headers.'Authorization'
        token = authUtil.extractToken(resp.headers.'Authorization')
      }

      response.failure = { resp ->
        println "Failure to authenticate for token: ${resp.statusLine}"
        context.fail(resp.statusLine.toString())
      }
    }
    
    //Try to access a protected resource without our token
    httpClient.request(Method.POST, ContentType.TEXT) { req ->
      uri.path = '/'
      body = "Hello world!"

      response.success = { resp ->
        context.fail("Failed to require authorization")
      }

      response.failure = { resp ->
        assert resp.status == 400
      }
    }

    //Try to access a protected resource with our token
    httpClient.request(Method.POST, ContentType.TEXT) { req ->
      uri.path = '/'
      body = "Hello again, world!"
      headers.'Authorization' = "Bearer ${token}"

      response.success = { resp ->
        assert resp.status == 202
      }

      response.failure = { resp ->
        context.fail(resp.statusLine.toString())
      }
    }
    //Expire the token

    httpClient.request(Method.POST, ContentType.JSON) { req ->
      uri.path = '/expire'
      body = [
        token : token
      ]
      headers.'Authorization' = "Bearer ${token}"
      
      response.success = { resp ->
        assert resp.status == 200
      }
    }

    //Try to access the protected resource with our expired token
    httpClient.request(Method.POST, ContentType.TEXT) { req ->
      uri.path = '/'
      body = 'Hello again, again, world!'
      headers.'Authorization' = "Bearer ${token}"

      response.success = { resp ->
        context.fail("Failed to expire token")
      }

      response.failure = { resp ->
        assert resp.status == 400
      }
    }
  }
}
