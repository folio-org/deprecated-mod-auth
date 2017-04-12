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
-- Data for Name: permissions_users; Type: TABLE DATA; Schema: diku_permissions_module; Owner: dbuser
--

COPY permissions_users (_id, jsonb) FROM stdin;
49d17eb7-0ac0-4e67-9e21-41b0364b9f68	{"username": "jack", "permissions": ["thing.read"]}
0a1fcb83-9806-4459-a601-ca75e9551460	{"username": "jill", "permissions": ["thing.read", "thing.see_sensitive", "thing.create", "thing.delete"]}
6d8b21ea-6a34-4d73-8ab8-8c58f8e6c148	{"username": "joe", "permissions": []}
5b5d6eb9-77e7-4589-879d-f25ae80a1b1f	{"username": "shane", "permissions": ["login.all", "perms.all", "users.all", "users-bl.all"]}
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

