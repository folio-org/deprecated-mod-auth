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
b1e2e3cf-1a97-4484-8930-e73b736eff75	{"hash": "F4B3659457E73516B84FDA2DD92D69A3A76770FB", "salt": "B4FF9E7BD0AE71A04CA3978483CCB2A20111BC94", "username": "shane"}
\.


SET search_path = diku_permissions_module, pg_catalog;

--
-- Data for Name: permissions; Type: TABLE DATA; Schema: diku_permissions_module; Owner: dbuser
--

COPY permissions (_id, jsonb) FROM stdin;
75a463e1-5509-48c2-beaa-c2f510d3b0ee	{"id" : "8cd22acd-e347-4382-9344-42020f65bb86", "tags": [], "permissionName": "login.addUser", "subPermissions": []}
011a44da-3f09-428c-8dc9-408a0bf12a38	{"id" : "a239a767-3c49-47c4-99e5-5485dc7ac8fd","tags": [], "permissionName": "login", "subPermissions": ["login.addUser", "login.modifyUser", "login.removeUser", "login.readUser"]}
0ad6600f-6865-4149-b60a-40c260cba2bd	{"id" : "e8fd127f-19c6-406a-91b2-fbc601edb0ec","tags": [], "permissionName": "login.modifyUser", "subPermissions": []}
fe8987fa-239c-4c06-9946-6ac728b6acf6	{"id" : "71d2aa6a-c39a-4e3c-b7ad-9548a1c8267d","tags": [], "permissionName": "login.readUser", "subPermissions": []}
856ebd96-2a68-4ed7-badb-ae1dca0a55a7	{"id" : "6c7a4410-4ec0-46f5-9fcd-754f47a9e5cd","tags": [], "permissionName": "login.removeUser", "subPermissions": []}
eac7dd73-86a4-4f28-8a92-aad97f9641b8	{"id" : "250494e9-275f-4b2c-a0b1-8cf65ea0b5ad","tags": [], "permissionName": "perms", "subPermissions": ["perms.users", "perms.permissions"]}
4c2552c5-b4ba-4d2d-92ef-482673db6094	{"id" : "83613c13-f412-4bc4-86b6-77e084e39921","tags": [], "permissionName": "perms.permissions.create", "subPermissions": []}
ef3a5fb2-db74-49e7-92b6-48aae9fc8f5a	{"id" : "5914e7fd-d1d9-455e-b520-f8435a342671","tags": [], "permissionName": "perms.permissions.delete", "subPermissions": []}
6a3548ea-e8f4-4e15-bcae-14ced0af6e76	{"id" : "4ec6aadd-f5ba-4e32-9c01-d6636261a274","tags": [], "permissionName": "perms.permissions", "subPermissions": ["perms.permissions.read", "perms.permissions.create", "perms.permissions.delete"]}
f3c7324a-9f7f-4912-a8f9-e87f5eb12c51	{"id" : "afd0c500-8c01-4140-85d5-11e1461df4d8","tags": [], "permissionName": "perms.permissions.read", "subPermissions": []}
ee91a409-0b49-4646-898d-9176965feddd	{"id" : "44865483-8b87-49b9-b76f-155e9c367ee2","tags": [], "permissionName": "retrieve.read", "subPermissions": []}
638f2852-0865-4097-829b-128ec31e644a	{"id" : "9b115c7f-5b2b-4a5f-868f-347362e9e544","tags": [], "permissionName": "perms.users.create", "subPermissions": []}
40bf7a16-22b4-4434-a5bd-a0e1b43f47f7	{"id" : "5df3f0c6-315f-45ce-a690-f17198c797d9","tags": [], "permissionName": "perms.users.delete", "subPermissions": []}
b62f6a5b-6a0f-4ccf-8429-4ff099e98108	{"id" : "98280690-174f-4251-8138-acea29523e20","tags": [], "permissionName": "perms.users", "subPermissions": ["perms.users.create", "perms.users.read", "perms.users.delete"]}
90af90d1-d4ef-4d30-8584-2972938f427e	{"id" : "95993363-ee9e-4fad-84db-6c6bba408ef8","tags": [], "permissionName": "perms.users.read", "subPermissions": []}
ab0b3c86-53ee-4f93-b017-f6e21ca76a5b	{"id" : "295a41ce-f102-4fe8-9da0-82a2e05095e9","tags": [], "permissionName": "thing.create", "subPermissions": []}
7ca7ebc2-6a93-45f9-9fb0-5a51565a1c15	{"id" : "95426638-01bd-4290-ab42-c96ab96baf88","tags": [], "permissionName": "thing.delete", "subPermissions": []}
a856ccb7-d283-4a75-9278-d2a1438a4781	{"id" : "b122ad54-3a01-4a03-af7e-14f57fb77b45","tags": [], "permissionName": "thing.edit", "subPermissions": []}
17b8fb37-ea44-40e9-bfec-6fae074e4248	{"id" : "6302eaf3-394e-498c-93bd-8a7e90171d53","tags": [], "permissionName": "thing.read", "subPermissions": []}
017cb6eb-20ac-4dc9-94a9-a8449feaed8b	{"id" : "5ef495c4-aaff-4816-b948-e0ac3985e78c","tags": [], "permissionName": "thing.see_sensitive", "subPermissions": []}
b2bf661d-209c-43a9-8851-a025238065fc	{"id" : "2a23b100-0359-4459-9021-b0052c636bfa","tags": [], "permissionName": "thing.super", "subPermissions": ["thing.read", "thing.create", "thing.edit", "thing.delete", "thing.see_sensitive"]}
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

