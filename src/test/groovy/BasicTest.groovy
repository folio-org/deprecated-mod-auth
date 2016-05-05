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
import groovy.json.JsonOutput


@RunWith(VertxUnitRunner.class)
class BasicTest {
  def vertx
  def httpClient
  def authUtil

  @Before
  void setUp(TestContext context) {
    /*
    Create a JSON file in a temp directory that we'll use as the flatfile
    for our auth backend
    */
    def json_config = [
      [
        "username" : "erikthered",
        "hash" : "878978635FB7D8DD653B64AF0D174A496FFBAE37",
        "salt" :  "0EB926D24332F4D9",
        "metadata" : [
          "permissions" : [ "auth_add_user", "auth_update_user", "auth_delete_user" ]
        ]
     ]
    ]
    def tempFile = File.createTempFile("authmodule_test", ".json")
    tempFile.write(JsonOutput.toJson(json_config))
    java.lang.System.setProperty("authType", "flatfile")
    java.lang.System.setProperty("secretsFilepath", tempFile.getAbsolutePath())
    java.lang.System.setProperty("loglevel", "debug")
    java.lang.System.setProperty("standalone", "true")
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
        println "Successfuly rejected token-less access attempt"
      }
    }

    //Try to access a protected resource with our token
    httpClient.request(Method.POST, ContentType.TEXT) { req ->
      uri.path = '/'
      body = "Hello again, world!"
      headers.'Authorization' = "Bearer ${token}"

      response.success = { resp ->
        assert resp.status == 202
        println "Successful access using token"
      }

      response.failure = { resp ->
        println "Unable to access using token ${token}"
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
      
      response.failure = { resp, reader ->
        println "Failure to create new user"
        println "Headers:"
        resp.headers.each { h ->
          println " ${h.name} : ${h.value}"
        }
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
        println "Failure to get token for 'mickey' user: ${resp.statusLine}"
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
