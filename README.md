# Authentication and Authorization Modules

## Authentication
The Authentication module is responsible for verifying the user's identity and issuing a valid JWT that can be used for system access.
The implementation of this module may vary (username/password, SAML, OAuth, etc.)

## Filtering
This module is responsible for filtering all proxy traffic and checking for a valid token. In addition, it is responsible for
retrieving the permissions for a given user and passing these permissions to destination modules.

## User and Permission Data
This module stores the users, as well as their associated permissions. It needs to be accessible by the filtering module, and may
need to be accessible by the Authentication module, in the event that Authentication events come with permissions/roles information.
