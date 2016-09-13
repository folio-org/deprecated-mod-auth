# Testing the Auth environment in Vagrant

### Prequisites: Vagrant installed on host system

## Initialize a Ubuntu 16.04 environment in Vagrant

```
vagrant box add bento/ubuntu-16.04
vagrant init bento/ubuntu-16.04
vagrant up
```

## SSH into your Vagrant box
```
vagrant ssh
```

## Install required software to run the demo

```
sudo apt-get update
sudo apt-get git default-jdk nodejs npm maven mongodb
```

## Update nodejs to the the newer version

```
sudo npm cache clean -f
sudo npm install -g n
sudo n stable
```

## Clone the Okapi repo and build it
```
git clone https://github.com/folio-org/okapi.git
cd okapi
mvn clean install
```

## Create symlinks in the testing directory for Okapi
```
cd mod-auth/testing/auth_test/
ln -s ~/okapi/okapi-core/target okapi
```

## Clone the auth module repo and build the 3 modules
```
git clone https://github.com/folio-org/mod-auth.git
cd mod-auth
git checkout split_modules
cd authentication_module
mvn clean install
cd ../authorization_module/
mvn clean install
cd ../permissions_module/
mvn clean install
```

## Build the nodejs sample modules we'll be using

```
cd /home/vagrant/mod-auth/testing/thing_module
npm install
cd /home/vagrant/mod-auth/testing/retrieve_module
npm install
```

## Initialize MongoDB with our testing data

```
mongoimport -d test -c permissions ~/mod-auth/testing/mongo/
mongoimport -d test -c permissions ~/mod-auth/testing/mongo/permissions.json  
mongoimport -d test -c credentials ~/mod-auth/testing/mongo/credentials.json 
```

## Run the script to load the modules

```
cd mod-auth/testing/auth_test/
./runme
```
