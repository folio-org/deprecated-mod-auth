var fetch = require('node-fetch');
var Mocha = require('mocha');
var expect = require('chai').expect;

var okapi_url = 'http://localhost:9130';

var bacon = {
    name : 'bacon',
    purpose : 'pleasure and nourishment',
    secret_power : 'so crisp, so delicious'
}

//Get a JWT with the jill user
//
describe('Tests with Jill', () => {
    var jill_token = null;
    it('should get a valid token for Jill', () => {
        var token_request = {
            username : 'jill',
            password : 'LittleNiceSheep'
        };

        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        return fetch(okapi_url + '/authn/login',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(token_request)
            }
        ).then( response => {
            if(response.ok) {
                jill_token = response.headers.get('Authorization');
                expect(jill_token).to.be.a('string');
                console.log('Jill token is: ' + jill_token);
            } else {
                response.text().then(text => {
                    console.log("Got status " + response.status);
                    console.log("Got message " + text);
                    throw new Error("Bad response: " + text);
                });
            }
        });
    });
    
    it('should be able to create a new Thing entry', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', jill_token);
        return fetch(okapi_url + '/things',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(bacon)
            }
        ).then(response => {
            if(response.ok) {
                //Do nothing, we're good
            } else {
                return response.text().then(text=>{
                    console.log("Status: " + response.status +
                        " : " + text);
                    throw new Error(text);
                });
            }
        });
    });
    it('should be able to delete the new Thing entry', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', jill_token);
        return fetch(okapi_url + '/things/bacon',
            {
                method : 'DELETE', headers : headers,
            }
        ).then(response => {
            if(response.ok) {
                //Do nothing, we're good
            } else {
                throw new Error("Bad response: " + response.status);
            }
        });
    });
});

//Get token for the shane user
//Attempt to add a sausage item to the thing app (expect failure)
//Add the thing.super permission to shane's permissions
//Attempt to add the sausage item
//Attempt to delete the sausage item
//Remove the thing.super permission from shane
//Attempt to add the sausage item (expect failure)
//
describe('Tests with Shane', () => {
    var ham = {
        name : 'ham',
        purpose : 'fooooood',
        secret_power : 'cured to perfection'
    }


    var shane_token = null;
    it('should get a valid token for Shane', () => {
        var token_request = {
            username : 'shane',
            password : 'LittleNiceSheep'
        };

        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        return fetch(okapi_url + '/authn/login',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(token_request)
            }
        ).then( response => {
            if(response.ok) {
                shane_token = response.headers.get('Authorization');
                expect(shane_token).to.be.a('string');
                var payload_string = get_payload(shane_token);
                var payload = JSON.parse(payload_string);
                expect(payload.sub).to.equal('shane');
                console.log('Shane token is: ' + payload_string);
            } else {
                response.text().then(text => {
                    console.log("Got status " + response.status);
                    console.log("Got message " + text);
                    throw new Error("Bad response: " + text);
                });
            }
        });
    });
    
    it('should fail to create a new Thing entry', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        console.log("Making fetch call to add ham");
        return fetch(okapi_url + '/things',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(ham)
            }
        ).then(response => {
            console.log("response.ok is " + response.ok);
            expect(response.ok).to.equal(false); 
        });
    });

    it('should add the thing permissions to Shane', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        var new_perm = { permission_name : 'thing.super' };
        return fetch(okapi_url + '/perms/users/shane/permissions',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(new_perm)
            }
        ).then(response => {
            expect(response.ok).to.equal(true);
        });
    });

    it('should be able to create a new Thing entry', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        return fetch(okapi_url + '/things',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(ham)
            }
        ).then(response => {
            if(response.ok) {
                //Do nothing, we're good
            } else {
                return response.text().then(text=>{
                    console.log("Status: " + response.status +
                        " : " + text);
                    throw new Error(text);
                });
            }
        });
    });

    it('should be able to delete the new Thing entry', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        return fetch(okapi_url + '/things/ham',
            {
                method : 'DELETE', headers : headers,
            }
        ).then(response => {
            if(response.ok) {
                //Do nothing, we're good
            } else {
                throw new Error("Bad response: " + response.status);
            }
        });
    });

    it('should remove the thing permissions from Shane', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        return fetch(okapi_url + '/perms/users/shane/permissions/thing.super',
            {
                method : 'DELETE', headers : headers,
            }
        ).then(response => {
            expect(response.ok).to.equal(true);
        });
    });



});

//Get a token for shane
//Create a new permission read.all, that includes thing.read and
//retrieve.read
//Get a token for joe
//Attempt to read retrieve entries with Joe (expect fail)
//Add read.all permission to joe
//Attempt to read retrieve entries with Joe
//Attempt to read thing entries with Joe
//Remove read.all permission from Joe
//Delete thing.all permission
//
describe('should perform tests with adding and removing permissions from Joe', () => {
    var shane_token = null;
    var joe_token = null;
    it('should get a valid token for Shane', () => {
        var token_request = {
            username : 'shane',
            password : 'LittleNiceSheep'
        };

        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        return fetch(okapi_url + '/authn/login',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(token_request)
            }
        ).then( response => {
            if(response.ok) {
                shane_token = response.headers.get('Authorization');
                expect(shane_token).to.be.a('string');
                var payload_string = get_payload(shane_token);
                var payload = JSON.parse(payload_string);
                expect(payload.sub).to.equal('shane');
                //console.log('Shane token is: ' + payload_string);
            } else {
                response.text().then(text => {
                    console.log("Got status " + response.status);
                    console.log("Got message " + text);
                    throw new Error("Bad response: " + text);
                });
            }
        });
    });
    var read_all_perm = {
        permission_name : "read.all",
        sub_permissions : [ "thing.read", "retrieve.read" ]
    };
    it('should create a new read.all permission', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        return fetch(okapi_url + '/perms/permissions',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(read_all_perm)
            }
        ).then(response => {
            expect(response.ok).to.equal(true);
        });
    });

    it('should get a valid token for Joe', () => {
        var token_request = {
            username : 'joe',
            password : 'OldLazyDog'
        };

        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        return fetch(okapi_url + '/authn/login',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(token_request)
            }
        ).then( response => {
            if(response.ok) {
                joe_token = response.headers.get('Authorization');
                expect(joe_token).to.be.a('string');
                var payload_string = get_payload(joe_token);
                var payload = JSON.parse(payload_string);
                expect(payload.sub).to.equal('joe');
            } else {
                response.text().then(text => {
                    console.log("Got status " + response.status);
                    console.log("Got message " + text);
                    throw new Error("Bad response: " + text);
                });
            }
        });
    });

    it('should fail to read the things as Joe', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', joe_token);
        return fetch(okapi_url + '/things',
            {
                method : 'GET', headers : headers,
            }
        ).then(response => {
            expect(response.ok).to.equal(false); 
        });
    });

    it('should add the read.all permission to Joe', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        var new_perm = { permission_name : 'read.all' };
        return fetch(okapi_url + '/perms/users/joe/permissions',
            {
                method : 'POST', headers : headers,
                body : JSON.stringify(new_perm)
            }
        ).then(res => {
            expect(res.ok).to.equal(true);
        });
    });

    it('should read the things as Joe', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', joe_token);
        return fetch(okapi_url + '/things',
            {
                method : 'GET', headers : headers,
            }
        ).then(response => {
            expect(response.ok).to.equal(true); 
        });
    });


    it('should remove the read.all permission from Joe', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        return fetch(okapi_url + '/perms/users/joe/permissions/read.all',
            {
                method : 'DELETE', headers : headers,
            }
        ).then(response => {
            expect(response.ok).to.equal(true);
        });
    });
    it('should delete the read.all permission', () => {
        var headers = new fetch.Headers();
        headers.append('X-Okapi-Tenant', 'diku');
        headers.append('Content-Type', 'application/json');
        headers.append('Authorization', shane_token);
        return fetch(okapi_url + '/perms/permissions/read.all',
            {
                method : 'DELETE', headers : headers
            }
        ).then(response => {
            expect(response.ok).to.equal(true);
        });
    });

});

function get_payload(token) {
    var token_parts = token.split('.');
    var payload_encoded = token_parts[1];
    return Buffer.from(payload_encoded, 'base64');
}
