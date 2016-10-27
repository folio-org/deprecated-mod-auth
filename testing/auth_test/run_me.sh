#!/bin/bash

#Start Okapi
echo "Starting Okapi"
java -jar okapi/okapi-core-fat.jar dev &> /tmp/okapi_out.log &
export OKAPI_PID=$!

#Give Okapi a few seconds to spin up
sleep 6

#Create our tenant
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenants/diku.json \
    http://localhost:9130/_/proxy/tenants

#Register the Users module with Okapi
echo "Registering the Users module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/mod-users.json \
    http://localhost:9130/_/proxy/modules

#Deploy the users module
echo "Deploying the Users module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/mod-users.json \
    http://localhost:9130/_/discovery/modules

#Associate the users module with our tenant
echo "Adding the Permissions module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/mod-users.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

#Register the Permissions module with Okapi
echo "Registering the Permissions module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/permissions.json \
    http://localhost:9130/_/proxy/modules

#Deploy the permissions module
echo "Deploying the Permissions module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/permissions.json \
    http://localhost:9130/_/discovery/modules

#Associate the Permissions module with our tenant
echo "Adding the Permissions module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/permissions.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

#Register the Authn module with Okapi
echo "Registering the Authn module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/authentication.json \
    http://localhost:9130/_/proxy/modules
#Deploy the Authn module
echo "Deploying the Authn module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/authentication.json \
    http://localhost:9130/_/discovery/modules
#Associate the Authn module with our tenant
echo "Adding the Authn module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/authentication.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

#Register the Authz module with Okapi
echo "Registering the Authz module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/authorization.json \
    http://localhost:9130/_/proxy/modules
#Deploy the Authz module
echo "Deploying the Authz module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/authorization.json \
    http://localhost:9130/_/discovery/modules
#Associate the Authz module with our tenant
echo "Adding the Authz module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/authorization.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

#Register the Thing module with Okapi
echo "Registering the Thing module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/thing.json \
    http://localhost:9130/_/proxy/modules
#Deploy the Thing module
echo "Deploying the Thing module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/thing.json \
    http://localhost:9130/_/discovery/modules
#Associate the Thing module with our tenant
echo "Adding the Thing module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/thing.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

#Register the Retrieve module with Okapi
echo "Registering the Retrieve module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/retrieve.json \
    http://localhost:9130/_/proxy/modules
#Deploy the Retrieve module
echo "Deploying the Retrieve module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/retrieve.json \
    http://localhost:9130/_/discovery/modules
#Associate the Retrieve module with our tenant
echo "Adding the Retrieve module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/retrieve.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

echo "Adding the users to mod-users"
for f in ./users/*
do
    echo processing $f
    curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -H "X-Okapi-Tenant: diku" \
    -d @$f \
    http://localhost:9130/users
done


echo "Okapi process id is $OKAPI_PID"
#echo "Killing Okapi"
#kill $OKAPI_PID

