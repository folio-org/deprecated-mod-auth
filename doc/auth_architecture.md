# Auth Modules Architecture

This is an overview of the basic components of the Auth system, and the interactions thereof.

## Authorization (Auth/Z)

The Auth/Z module is the primary "filter" that sits in front of all proxied "regular" modules. It is responsible for checking that an incoming request has a valid JWT attached and assigning the permissions to the request, based on the requestor's identity.

It also exposes a service to generate tokens, that can be called by privileged modules, such as Auth/N.

## Authentication (Auth/N)

The Auth/N module provides an endpoint for a user to obtain a valid JWT. The "default" implementation is to act as a basic username/password store and allow password authentication. However, we can have multiple Auth/N modules existing, in order to offer alternatives such as SAML, OAuth, CAS, etc.

## Permissions

This module is responsible for two things: Storing the hierarchy of a given permission, and returning the list of permissions assigned to a given user of the system. It does not store module permissions, which are defined in the module descriptors and brokered by Okapi itself.

Any given permission can contain a number of other permissions as "sub-permissions." When permissions are evaluated, they are returned as a list of all permissions included. In this manner, a permission can act as a simple permission bit, or as a broader "role."

![Auth Modules Diagram](auth_modules.png "Auth Modules")

## Interactions

1. The Auth/N module, upon completing a successful "login", calls the /token endpoint of the Auth/Z module in order to request a new token to give to the user. The /token endpoint is privileged, and requires the use of a shared secret, set at module deployment.
2. It is possible that the Auth/N module may need to make changes to the Permissions of a user, based on information received during login. An example of this might be if a SAML permissions crosswalk has been defined, or if we are allow permissions to be managed using LDAP. Again, this interaction requires passing a shared secret.
3. The Auth/Z module relies on the Permissions module in order to retrieve the permissions granted to a given user when the JWT is verified.

## Notes

Because the Auth modules need privileged access to each other, they must be given a shared secret when the modules are started. They are able to use this secret as an API key to verify themselves to each other, as the normal Auth mechanism can't be relied upon until the Auth chain is bootstrapped.

The Permissions module is currently being built to use MongoDB as a backend for storing permissions, but as the Domain Models mature and stabilize, it will ideally use storage provided by FOLIO's own storage layer.



