--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.5
-- Dumped by pg_dump version 9.5.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'LATIN1';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: diku; Type: SCHEMA; Schema: -; Owner: diku
--



--
-- Name: diku_login_module; Type: SCHEMA; Schema: -; Owner: diku_login_module
--

CREATE SCHEMA diku_login_module;


ALTER SCHEMA diku_login_module OWNER TO diku_login_module;

--
-- Name: diku_permissions_module; Type: SCHEMA; Schema: -; Owner: diku_permissions_module
--

CREATE SCHEMA diku_permissions_module;


ALTER SCHEMA diku_permissions_module OWNER TO diku_permissions_module;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';



SET search_path = diku_login_module, pg_catalog;

--
-- Name: auth_credentials; Type: TABLE; Schema: diku_login_module; Owner: dbuser
--

CREATE TABLE auth_credentials (
    _id uuid DEFAULT public.gen_random_uuid() NOT NULL,
    jsonb jsonb NOT NULL
);


ALTER TABLE auth_credentials OWNER TO dbuser;

SET search_path = diku_permissions_module, pg_catalog;

--
-- Name: permissions; Type: TABLE; Schema: diku_permissions_module; Owner: dbuser
--

CREATE TABLE permissions (
    _id uuid DEFAULT public.gen_random_uuid() NOT NULL,
    jsonb jsonb NOT NULL
);


ALTER TABLE permissions OWNER TO dbuser;

--
-- Name: permissions_users; Type: TABLE; Schema: diku_permissions_module; Owner: dbuser
--

CREATE TABLE permissions_users (
    _id uuid DEFAULT public.gen_random_uuid() NOT NULL,
    jsonb jsonb NOT NULL
);


ALTER TABLE permissions_users OWNER TO dbuser;

SET search_path = diku_login_module, pg_catalog;

--
-- Data for Name: auth_credentials; Type: TABLE DATA; Schema: diku_login_module; Owner: dbuser
--

COPY auth_credentials (_id, jsonb) FROM stdin;
acfee48a-b43e-4cdc-a3ba-6ab1928e718c	{"hash": "F4B3659457E73516B84FDA2DD92D69A3A76770FB", "salt": "B4FF9E7BD0AE71A04CA3978483CCB2A20111BC94", "username": "jill"}
\.


SET search_path = diku_permissions_module, pg_catalog;

--
-- Data for Name: permissions; Type: TABLE DATA; Schema: diku_permissions_module; Owner: dbuser
--

COPY permissions (_id, jsonb) FROM stdin;
75a463e1-5509-48c2-beaa-c2f510d3b0ee	{"tags": [], "permission_name": "login.addUser", "sub_permissions": []}
011a44da-3f09-428c-8dc9-408a0bf12a38	{"tags": [], "permission_name": "login", "sub_permissions": ["login.addUser", "login.modifyUser", "login.removeUser", "login.readUser"]}
0ad6600f-6865-4149-b60a-40c260cba2bd	{"tags": [], "permission_name": "login.modifyUser", "sub_permissions": []}
fe8987fa-239c-4c06-9946-6ac728b6acf6	{"tags": [], "permission_name": "login.readUser", "sub_permissions": []}
856ebd96-2a68-4ed7-badb-ae1dca0a55a7	{"tags": [], "permission_name": "login.removeUser", "sub_permissions": []}
eac7dd73-86a4-4f28-8a92-aad97f9641b8	{"tags": [], "permission_name": "perms", "sub_permissions": ["perms.users", "perms.permissions"]}
4c2552c5-b4ba-4d2d-92ef-482673db6094	{"tags": [], "permission_name": "perms.permissions.create", "sub_permissions": []}
ef3a5fb2-db74-49e7-92b6-48aae9fc8f5a	{"tags": [], "permission_name": "perms.permissions.delete", "sub_permissions": []}
6a3548ea-e8f4-4e15-bcae-14ced0af6e76	{"tags": [], "permission_name": "perms.permissions", "sub_permissions": ["perms.permissions.read", "perms.permissions.create", "perms.permissions.delete"]}
f3c7324a-9f7f-4912-a8f9-e87f5eb12c51	{"tags": [], "permission_name": "perms.permissions.read", "sub_permissions": []}
ee91a409-0b49-4646-898d-9176965feddd	{"tags": [], "permission_name": "retrieve.read", "sub_permissions": []}
638f2852-0865-4097-829b-128ec31e644a	{"tags": [], "permission_name": "perms.users.create", "sub_permissions": []}
40bf7a16-22b4-4434-a5bd-a0e1b43f47f7	{"tags": [], "permission_name": "perms.users.delete", "sub_permissions": []}
b62f6a5b-6a0f-4ccf-8429-4ff099e98108	{"tags": [], "permission_name": "perms.users", "sub_permissions": ["perms.users.create", "perms.users.read", "perms.users.delete"]}
90af90d1-d4ef-4d30-8584-2972938f427e	{"tags": [], "permission_name": "perms.users.read", "sub_permissions": []}
ab0b3c86-53ee-4f93-b017-f6e21ca76a5b	{"tags": [], "permission_name": "thing.create", "sub_permissions": []}
7ca7ebc2-6a93-45f9-9fb0-5a51565a1c15	{"tags": [], "permission_name": "thing.delete", "sub_permissions": []}
a856ccb7-d283-4a75-9278-d2a1438a4781	{"tags": [], "permission_name": "thing.edit", "sub_permissions": []}
17b8fb37-ea44-40e9-bfec-6fae074e4248	{"tags": [], "permission_name": "thing.read", "sub_permissions": []}
017cb6eb-20ac-4dc9-94a9-a8449feaed8b	{"tags": [], "permission_name": "thing.see_sensitive", "sub_permissions": []}
b2bf661d-209c-43a9-8851-a025238065fc	{"tags": [], "permission_name": "thing.super", "sub_permissions": ["thing.read", "thing.create", "thing.edit", "thing.delete", "thing.see_sensitive"]}
\.


--
-- Data for Name: permissions_users; Type: TABLE DATA; Schema: diku_permissions_module; Owner: dbuser
--

COPY permissions_users (_id, jsonb) FROM stdin;
49d17eb7-0ac0-4e67-9e21-41b0364b9f68	{"username": "jack", "permissions": ["thing.read"]}
0a1fcb83-9806-4459-a601-ca75e9551460	{"username": "jill", "permissions": ["thing.read", "thing.see_sensitive", "thing.create", "thing.delete"]}
6d8b21ea-6a34-4d73-8ab8-8c58f8e6c148	{"username": "joe", "permissions": []}
5b5d6eb9-77e7-4589-879d-f25ae80a1b1f	{"username": "shane", "permissions": ["perms", "login"]}
\.


SET search_path = diku_login_module, pg_catalog;

--
-- Name: auth_credentials_pkey; Type: CONSTRAINT; Schema: diku_login_module; Owner: dbuser
--

ALTER TABLE ONLY auth_credentials
    ADD CONSTRAINT auth_credentials_pkey PRIMARY KEY (_id);


SET search_path = diku_permissions_module, pg_catalog;

--
-- Name: permissions_pkey; Type: CONSTRAINT; Schema: diku_permissions_module; Owner: dbuser
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (_id);


--
-- Name: permissions_users_pkey; Type: CONSTRAINT; Schema: diku_permissions_module; Owner: dbuser
--

ALTER TABLE ONLY permissions_users
    ADD CONSTRAINT permissions_users_pkey PRIMARY KEY (_id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;



SET search_path = diku_login_module, pg_catalog;

--
-- Name: auth_credentials; Type: ACL; Schema: diku_login_module; Owner: dbuser
--

REVOKE ALL ON TABLE auth_credentials FROM PUBLIC;
REVOKE ALL ON TABLE auth_credentials FROM dbuser;
GRANT ALL ON TABLE auth_credentials TO dbuser;
GRANT ALL ON TABLE auth_credentials TO diku_login_module;


SET search_path = diku_permissions_module, pg_catalog;

--
-- Name: permissions; Type: ACL; Schema: diku_permissions_module; Owner: dbuser
--

REVOKE ALL ON TABLE permissions FROM PUBLIC;
REVOKE ALL ON TABLE permissions FROM dbuser;
GRANT ALL ON TABLE permissions TO dbuser;
GRANT ALL ON TABLE permissions TO diku_permissions_module;


--
-- Name: permissions_users; Type: ACL; Schema: diku_permissions_module; Owner: dbuser
--

REVOKE ALL ON TABLE permissions_users FROM PUBLIC;
REVOKE ALL ON TABLE permissions_users FROM dbuser;
GRANT ALL ON TABLE permissions_users TO dbuser;
GRANT ALL ON TABLE permissions_users TO diku_permissions_module;


--
-- PostgreSQL database dump complete
--

