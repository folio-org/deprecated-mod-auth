#!/bin/bash

#######
echo "Starting Okapi"
java -jar -Dloglevel=DEBUG okapi/okapi-core-fat.jar dev &> /tmp/okapi_out.log &
export OKAPI_PID=$!
echo Okapi PID is $OKAPI_PID
sleep 6  #Give Okapi a few seconds to spin up

##NASTY HACK TO DEAL WITH OKAPI/RMB TENANT INIT WEIRDNESS
#sudo -u postgres bash -c "psql -c \"DROP DATABASE permissions;\""
#sudo -u postgres bash -c "psql -c \"CREATE DATABASE permissions WITH OWNER=dbuser;\""
#sudo -u postgres bash -c "psql -c \"DROP ROLE diku_permissions_module;\""
##END NASTY HACK

echo "Creating our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenants/diku.json \
    http://localhost:9130/_/proxy/tenants

### Users module
echo "Registering the Users module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/mod-users.json \
    http://localhost:9130/_/proxy/modules

echo "Deploying the Users module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/mod-users.json \
    http://localhost:9130/_/discovery/modules

echo "Adding the Users module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -H "X-Okapi-Tenant: diku" \
    -d @./tenant_associations/mod-users.json \
    http://localhost:9130/_/proxy/tenants/diku/modules


### Permissions module
echo "Registering the Permissions module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/permissions.json \
    http://localhost:9130/_/proxy/modules

echo "Deploying the Permissions module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/permissions.json \
    http://localhost:9130/_/discovery/modules

echo "Adding the Permissions module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/permissions.json \
    http://localhost:9130/_/proxy/tenants/diku/modules


### Login module
echo "Registering the login module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/login.json \
    http://localhost:9130/_/proxy/modules

echo "Deploying the login module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/login.json \
    http://localhost:9130/_/discovery/modules

echo "Adding the login module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/login.json \
    http://localhost:9130/_/proxy/tenants/diku/modules


### authtoken module
echo "Registering the authtoken module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/authtoken.json \
    http://localhost:9130/_/proxy/modules

echo "Deploying the authtoken module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/authtoken.json \
    http://localhost:9130/_/discovery/modules

echo "Adding the authtoken module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/authtoken.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

### Thing module
echo "Registering the Thing module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/thing.json \
    http://localhost:9130/_/proxy/modules

echo "Deploying the Thing module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/thing.json \
    http://localhost:9130/_/discovery/modules

echo "Adding the Thing module to our tenant"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./tenant_associations/thing.json \
    http://localhost:9130/_/proxy/tenants/diku/modules

### Retrieve module
echo "Registering the Retrieve module"
curl -w '\n' -X POST -D - \
    -H "Content-type: application/json" \
    -d @./module_descriptors/retrieve.json \
    http://localhost:9130/_/proxy/modules

echo "Deploying the Retrieve module"
curl -w '\n' -D - -s \
    -X POST \
    -H "Content-type: application/json" \
    -d @./deployment_descriptors/retrieve.json \
    http://localhost:9130/_/discovery/modules

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
    http://localhost:9131/users
done

#echo "Populating the permissions module"
#for f in ./permissions/users/*
#do
#    echo processing $f
#    curl -w '\n' -X POST -D - \
#    -H "Content-type: application/json" \
#    -H "X-Okapi-Tenant: diku" \
#    -d @$f \
#    http://localhost:9130/perms/users
#done
#
#for f in ./permissions/permissions/*
#do
#    echo processing $f
#    curl -w '\n' -X POST -D - \
#    -H "Content-type: application/json" \
#    -H "X-Okapi-Tenant: diku" \
#    -d @$f \
#    http://localhost:9130/perms/permissions
#done

echo "Okapi process id is $OKAPI_PID"
#echo "Killing Okapi"
#kill $OKAPI_PID

