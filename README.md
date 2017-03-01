# mod-auth

Copyright (C) 2017 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

# Auth modules
The mod-auth project contains most of the modules related to user authentication
and authorization:
  * login: Allows a user to authenticate
  * authtoken: Manages auth token creation and verification
  * permissions: Assigns permissions to users, possibly via permission sets

## Login
The login module is responsible for verifying the user's identity and
issuing a valid JWT that can be used for system access. The implementation of
this module may vary (username/password, SAML, OAuth, etc.), and it is possible
for more than one Authentication module to exist in a running system. The
default implementation uses a simple username and password for authentication.

## Authtoken
This module is responsible for filtering all proxy traffic and checking for a
valid token. In addition, it is responsible for retrieving the permissions for
a given user and making decisions regarding access based on user permissions
and defined requirements for a given path. It provides a token creation endpoint
that privileged modules (such as Authentication) may make use of.

## Permissions
This module stores permissions and associations between permissions and users.
It also maintains a hierarchy of permissions and sub-permissions, allowing for
permissions to act as roles, rather than simple bits. It is used primarily by
the Authtoken module, though it is possible that some Authentication
implementations may have reason to make calls to the Permissions module as well.

# Additional information

The [raml-module-builder](https://github.com/folio-org/raml-module-builder) framework.

Other [modules](http://dev.folio.org/source-code/#server-side).

Other FOLIO Developer documentation is at [dev.folio.org](http://dev.folio.org/)
