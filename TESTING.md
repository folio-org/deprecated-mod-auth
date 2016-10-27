# Testing the Auth environment in Vagrant

### Prerequisites: Vagrant installed on host system

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

## Add the source to Ubuntu to install mongo-org community version

```
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927
echo "deb http://repo.mongodb.org/apt/ubuntu xenial/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list
```

## Install required software to run the demo

```
sudo apt-get update
sudo apt-get install curl default-jdk git maven nodejs npm mongodb-org
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

## Clone the auth module repo and build the 3 modules
```
cd ~
git clone https://github.com/folio-org/mod-auth.git
cd mod-auth
cd login_module && mvn clean install
cd ../authtoken_module && mvn clean install
cd ../permissions_module && mvn clean install
```

## Build the nodejs sample modules we'll be using

```
cd ~/mod-auth/testing/thing_module && npm install
cd ~/mod-auth/testing/retrieve_module && npm install
```

## Create symlinks in the testing directory for Okapi
```
cd ~/mod-auth/testing/auth_test/
ln -s ~/okapi/okapi-core/target okapi
```

## Initialize MongoDB with our testing data

```
mongoimport -d test -c users ~/mod-auth/testing/mongo/users.json
mongoimport -d test -c permissions ~/mod-auth/testing/mongo/permissions.json  
mongoimport -d test -c credentials ~/mod-auth/testing/mongo/credentials.json 
```

## Run the script to load the modules

```
cd ~/mod-auth/testing/auth_test/
./run_me.sh
```

## Build and run the Mocha tests

```
cd ~/mod-auth/testing/mocha_testing/
npm install
npm start

```
