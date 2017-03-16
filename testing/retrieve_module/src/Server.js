var express = require("express");
var parseArgs = require("minimist");
var fetch = require('node-fetch');

var argv = parseArgs(process.argv.slice(2));
var port = 3000;

if (argv.p) {
  port = parseInt(argv.p);
  if (isNaN(port)) {
    throw "Invalid port specifier: " + argv.p;
  }
}

var app = express();
var router = express.Router();

router.use((req, res, next) => {
  next();
});

router.all('/retrieve/:name', retrieve_thing);
app.use('/', router);
app.listen(port, () => {
  console.log("Listening on port " + port);
});

function retrieve_thing(req, res) {
  var name = req.params.name;
  res.type('application/json');
  var okapi_url = get_okapi_url(req);
  if (okapi_url == null) {
    res.status(500)
      .json({
        "error": "Unable to get Okapi URL"
      });
    return;
  }
  //In the future, discovery will likely be more sophisticated
  var thing_url = okapi_url + "things/" + name;
  console.log("Requesting things on URL: " + thing_url);
  var token = get_jwt(req);
  if (token == null) {
    res.status(500)
      .json({
        "error": "Unable to get a valid token for request"
      });
    return;
  }
  //need to make a request to the other module
  headers = new fetch.Headers();
  headers.append('X-Okapi-Token', +token);
  headers.append('X-Okapi-Tenant', get_okapi_tenant(req));
  fetch(thing_url, {
    method: 'GET',
    headers: headers
  }).then((fetch_res) => {
    if (fetch_res.ok) {
      res.status(200);
    } else {
      res.status(fetch_res.status);
    }
    return fetch_res.json().then((json) => {
      res.json(json);
    });
  });
}

function get_okapi_url(req) {
  var url = req.get('X-Okapi-Url');
  console.log("X-Okapi-Url is " + url);
  if (!url.endsWith("/")) {
    url = url + "/";
  }
  return url;
}

function get_jwt(req) {
  return req.get('X-Okapi-Token');
}

function get_okapi_tenant(req) {
  return req.get('X-Okapi-Tenant');
}
