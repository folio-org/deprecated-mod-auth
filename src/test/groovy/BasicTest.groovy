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
import java.lang.Thread
import java.nio.file.Paths
import java.nio.file.Files


@RunWith(VertxUnitRunner.class)
class BasicTest {
  def vertx
  def httpClient
  def authUtil

  @Before
  void setUp(TestContext context) {
    /*
    TODO: Find a way to copy the authSecrets file from the resources directory
    to the OS's temporary directory and load the verticle with that file as
    opposed to using the one within the jar
    */
    //url = Thread.currentThread().getContextClassLoader().getResource("auth-prototype/authSecrets.json");
    path = this.getClass().getResource("authSecrets.json");
    origPath = Paths.get(path)
    newPath = Paths.get("/tmp/authSecrets.json") //Need to make this OS agnostic
    Files.copy(origPath, newPath)
    
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
  
  @Test
  void addUserWithNoPerms(TestContext context) {
    def privilegedToken; //Token from the user with perms
    def weakToken; //Token from the new users
    //Get the privileged token
      
    httpClient.request( Method.POST, ContentType.JSON ) { req ->
      uri.path = '/token'
      body = [
        username : 'erikthered',
        password : 'ChickenMonkeyDuck'
      ]

      response.success = { resp, json ->
        println resp.headers.'Authorization'
        privilegedToken = authUtil.extractToken(resp.headers.'Authorization')
      }

      response.failure = { resp ->
        println "Failure to authenticate for token: ${resp.statusLine}"
        context.fail(resp.statusLine.toString())
      }
    }
    
    // Create our unprivileged user
    httpClient.request( Method.POST, ContentType.JSON ) { req ->
      uri.path = '/user'
      body = [
        "credentials" : [
          "username" : "mickey",
          "password" : "mouse"
        ],
        "metadata" : [
          []
        ]
      ]
      headers.'Authorization' = "Bearer ${privilegedToken}"
      
      response.success = { resp, json ->
        println "User 'mickey' created"
      }
      
      response.failure = { resp ->
        println "Failure to create new user"
        context.fail(resp.statusLine.toString())
      }
    }
    
    //Get the weak token
    httpClient.request( Method.POST, ContentType.JSON ) { req ->
      uri.path = '/token'
      body = [
        "username" : 'mickey',
        "password" : 'mouse'
      ]

      response.success = { resp, json ->
        println resp.headers.'Authorization'
        weakToken = authUtil.extractToken(resp.headers.'Authorization')
      }

      response.failure = { resp ->
        println "Failure to authenticate for token: ${resp.statusLine}"
        context.fail(resp.statusLine.toString())
      }
    }
    
    //Try to add a user with the weak token (expected failure)
    httpClient.request( Method.POST, ContentType.JSON ) { req ->
      uri.path = '/user'
      body = [
        "credentials" : [
          "username" : "donald",
          "password" : "duck"
        ],
        "metadata" : [
          []
        ]
      ]
      headers.'Authorization' = "Bearer ${weakToken}"
      
      response.success = { resp, json ->
        println "User created with unprivileged token. Ungood"
        context.fail("User should not be created with unprivileged token")
      }
      
      response.failure = { resp ->
        //do nothing, expected
      }
    }
    
    //delete the no-perm user
    httpClient.request( Method.DELETE, ContentType.JSON ) { req ->
      uri.path = "/user"
      body = [
        "credentials" : [
          "username" : "mickey"
        ]
      ]
      headers.'Authorization' = "Bearer ${privilegedToken}"
      
      response.success = { resp, json ->
        //okay
      }
      
      response.failure = { resp ->
        println "Unable to delete user 'mickey'"
        context.fail(resp.statusLine.toString())
      }
    }
      
  }
}
