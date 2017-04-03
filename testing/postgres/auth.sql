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
40a8dd31-5ff6-4519-a428-0d9bcca82d2f	{"hash": "C8C38072D79AD0C97E3FFA11FF0BDF4A4B9128D3", "salt": "F255821CEB07287F673FA9BC54482CF4B1104D69", "username": "joe"}
\.


SET search_path = diku_permissions_module, pg_catalog;

--
-- Data for Name: permissions; Type: TABLE DATA; Schema: diku_permissions_module; Owner: dbuser
--

COPY permissions (_id, jsonb) FROM stdin;
75a463e1-5509-48c2-beaa-c2f510d3b0ee	{"id" : "8cd22acd-e347-4382-9344-42020f65bb86", "tags": [], "permissionName": "login.addUser", "mutable" : false, "subPermissions": []}
011a44da-3f09-428c-8dc9-408a0bf12a38	{"id" : "a239a767-3c49-47c4-99e5-5485dc7ac8fd","tags": [], "permissionName": "login", "mutable" : true, "subPermissions": ["login.addUser", "login.modifyUser", "login.removeUser", "login.readUser"]}
0ad6600f-6865-4149-b60a-40c260cba2bd	{"id" : "e8fd127f-19c6-406a-91b2-fbc601edb0ec","tags": [], "permissionName": "login.modifyUser", "mutable" : false, "subPermissions": []}
fe8987fa-239c-4c06-9946-6ac728b6acf6	{"id" : "71d2aa6a-c39a-4e3c-b7ad-9548a1c8267d","tags": [], "permissionName": "login.readUser", "mutable" : false, "subPermissions": []}
856ebd96-2a68-4ed7-badb-ae1dca0a55a7	{"id" : "6c7a4410-4ec0-46f5-9fcd-754f47a9e5cd","tags": [], "permissionName": "login.removeUser", "mutable" : false, "subPermissions": []}
4c2552c5-b4ba-4d2d-92ef-482673db6094	{"id" : "83613c13-f412-4bc4-86b6-77e084e39921","tags": [], "permissionName": "perms.permissions.create", "mutable" : false, "subPermissions": []}
ef3a5fb2-db74-49e7-92b6-48aae9fc8f5a	{"id" : "5914e7fd-d1d9-455e-b520-f8435a342671","tags": [], "permissionName": "perms.permissions.delete", "mutable" : false, "subPermissions": []}
6a3548ea-e8f4-4e15-bcae-14ced0af6e76	{"id" : "4ec6aadd-f5ba-4e32-9c01-d6636261a274","tags": [], "permissionName": "perms.permissions", "mutable" : true, "subPermissions": ["perms.permissions.read", "perms.permissions.create", "perms.permissions.delete"]}
f3c7324a-9f7f-4912-a8f9-e87f5eb12c51	{"id" : "afd0c500-8c01-4140-85d5-11e1461df4d8","tags": [], "permissionName": "perms.permissions.read", "mutable" : false, "subPermissions": []}
ee91a409-0b49-4646-898d-9176965feddd	{"id" : "44865483-8b87-49b9-b76f-155e9c367ee2","tags": [], "permissionName": "retrieve.read", "mutable" : false, "subPermissions": []}
638f2852-0865-4097-829b-128ec31e644a	{"id" : "9b115c7f-5b2b-4a5f-868f-347362e9e544","tags": [], "permissionName": "perms.users.create", "subPermissions": []}
0e899da0-de84-4784-bfb6-37660e155231	{"id" : "dd61cfc2-8a9c-420c-ba74-5400398efa4a","tags": [], "permissionName": "perms.users.modify", "mutable" : false, "subPermissions": []}
40bf7a16-22b4-4434-a5bd-a0e1b43f47f7	{"id" : "5df3f0c6-315f-45ce-a690-f17198c797d9","tags": [], "permissionName": "perms.users.delete", "mutable" : false, "subPermissions": []}
b62f6a5b-6a0f-4ccf-8429-4ff099e98108	{"id" : "98280690-174f-4251-8138-acea29523e20","tags": [], "permissionName": "perms.users", "mutable" : true, "subPermissions": ["perms.users.create", "perms.users.modify", "perms.users.read", "perms.users.delete"]}
90af90d1-d4ef-4d30-8584-2972938f427e	{"id" : "95993363-ee9e-4fad-84db-6c6bba408ef8","tags": [], "permissionName": "perms.users.read", "mutable" : false, "subPermissions": []}
ab0b3c86-53ee-4f93-b017-f6e21ca76a5b	{"id" : "295a41ce-f102-4fe8-9da0-82a2e05095e9","tags": [], "permissionName": "thing.create", "mutable" : false, "subPermissions": []}
7ca7ebc2-6a93-45f9-9fb0-5a51565a1c15	{"id" : "95426638-01bd-4290-ab42-c96ab96baf88","tags": [], "permissionName": "thing.delete", "mutable" : false, "subPermissions": []}
a856ccb7-d283-4a75-9278-d2a1438a4781	{"id" : "b122ad54-3a01-4a03-af7e-14f57fb77b45","tags": [], "permissionName": "thing.edit", "mutable" : false, "subPermissions": []}
17b8fb37-ea44-40e9-bfec-6fae074e4248	{"id" : "6302eaf3-394e-498c-93bd-8a7e90171d53","tags": [], "permissionName": "thing.read", "mutable" : false, "subPermissions": []}
017cb6eb-20ac-4dc9-94a9-a8449feaed8b	{"id" : "5ef495c4-aaff-4816-b948-e0ac3985e78c","tags": [], "permissionName": "thing.see_sensitive", "mutable" : false, "subPermissions": []}
b2bf661d-209c-43a9-8851-a025238065fc	{"id" : "2a23b100-0359-4459-9021-b0052c636bfa","tags": [], "permissionName": "thing.super", "mutable" : true, "subPermissions": ["thing.read", "thing.create", "thing.edit", "thing.delete", "thing.see_sensitive"]}
5e61d34c-0bfd-48cc-b583-811575bba1a8	{"id" : "12f13449-d687-48f4-81f4-31fdc91f763f","tags": [], "permissionName": "users.read", "mutable" : false, "subPermissions": []}
1cfd92db-30ad-43e3-8d2e-e5a0e8532130	{"id" : "6d04e6bf-1cde-4370-8fb4-6f7764468c61","tags": [], "permissionName": "users.create", "mutable" : false, "subPermissions": []}
359c54d8-ddca-4c16-b7d3-877284e7ad8b	{"id" : "3b903874-cf92-4af0-96b5-bef7308b99bb","tags": [], "permissionName": "users.edit", "mutable" : false, "subPermissions": []}
f19a8860-eeb5-497a-bd6e-13c53d565c05	{"id" : "fcdc162e-fbc9-4f9d-b0a0-848406b9672f","tags": [], "permissionName": "users.delete", "mutable" : false, "subPermissions": []}
ce42fc6d-0e50-4932-a6f3-cedc24fd0f16	{"id" : "f99905ee-2ee2-4d60-9c2d-7c7f1e2e1373","tags": [], "permissionName": "usergroups.read", "mutable" : false, "subPermissions": []}
c331cfbd-4866-4343-8812-2c9255eb5ec7	{"id" : "11d9fc45-5df0-4843-a53c-c0e25320f849","tags": [], "permissionName": "usergroups.create", "mutable" : false, "subPermissions" : []}
6e73df5c-c6a8-46bc-aa91-86022deb1365	{"id" : "70df39dc-da72-4a32-ba00-dbbc597a8c8b","tags": [], "permissionName": "usergroups.edit", "mutable" : false, "subPermissions": []}
f0b5da84-a8af-4b31-b7f7-eff315dd8f4b	{"id" : "86e010e9-bb73-413b-bdf3-3b795b4b5ede","tags": [], "permissionName": "usergroups.delete", "mutable" : false, "subPermissions" : []}
\.


--
-- Data for Name: permissions_users; Type: TABLE DATA; Schema: diku_permissions_module; Owner: dbuser
--

COPY permissions_users (_id, jsonb) FROM stdin;
49d17eb7-0ac0-4e67-9e21-41b0364b9f68	{"username": "jack", "permissions": ["thing.read"]}
0a1fcb83-9806-4459-a601-ca75e9551460	{"username": "jill", "permissions": ["thing.read", "thing.see_sensitive", "thing.create", "thing.delete"]}
6d8b21ea-6a34-4d73-8ab8-8c58f8e6c148	{"username": "joe", "permissions": []}
5b5d6eb9-77e7-4589-879d-f25ae80a1b1f	{"username": "shane", "permissions": ["perms.users", "perms.permissions", "login", "users.read", "users.create", "users.edit", "users.delete", "usergroups.read", "usergroups.create", "usergroups.edit", "usergroups.delete", "users.read.basic"]}
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

