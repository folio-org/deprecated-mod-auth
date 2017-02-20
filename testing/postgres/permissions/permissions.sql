--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.1
-- Dumped by pg_dump version 9.6.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: diku; Type: SCHEMA; Schema: -; Owner: diku
--

CREATE SCHEMA diku;


ALTER SCHEMA diku OWNER TO diku;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = diku, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: permissions; Type: TABLE; Schema: diku; Owner: dbuser
--

CREATE TABLE permissions (
    _id integer NOT NULL,
    jsonb jsonb NOT NULL
);


ALTER TABLE permissions OWNER TO dbuser;

--
-- Name: permissions__id_seq; Type: SEQUENCE; Schema: diku; Owner: dbuser
--

CREATE SEQUENCE permissions__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE permissions__id_seq OWNER TO dbuser;

--
-- Name: permissions__id_seq; Type: SEQUENCE OWNED BY; Schema: diku; Owner: dbuser
--

ALTER SEQUENCE permissions__id_seq OWNED BY permissions._id;


--
-- Name: permissions_users; Type: TABLE; Schema: diku; Owner: dbuser
--

CREATE TABLE permissions_users (
    _id integer NOT NULL,
    jsonb jsonb NOT NULL
);


ALTER TABLE permissions_users OWNER TO dbuser;

--
-- Name: permissions_users__id_seq; Type: SEQUENCE; Schema: diku; Owner: dbuser
--

CREATE SEQUENCE permissions_users__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE permissions_users__id_seq OWNER TO dbuser;

--
-- Name: permissions_users__id_seq; Type: SEQUENCE OWNED BY; Schema: diku; Owner: dbuser
--

ALTER SEQUENCE permissions_users__id_seq OWNED BY permissions_users._id;


--
-- Name: permissions _id; Type: DEFAULT; Schema: diku; Owner: dbuser
--

ALTER TABLE ONLY permissions ALTER COLUMN _id SET DEFAULT nextval('permissions__id_seq'::regclass);


--
-- Name: permissions_users _id; Type: DEFAULT; Schema: diku; Owner: dbuser
--

ALTER TABLE ONLY permissions_users ALTER COLUMN _id SET DEFAULT nextval('permissions_users__id_seq'::regclass);


--
-- Data for Name: permissions; Type: TABLE DATA; Schema: diku; Owner: dbuser
--

COPY permissions (_id, jsonb) FROM stdin;
8	{"tags": [], "permission_name": "thing.read", "sub_permissions": []}
9	{"tags": [], "permission_name": "thing.create", "sub_permissions": []}
10	{"tags": [], "permission_name": "thing.edit", "sub_permissions": []}
11	{"tags": [], "permission_name": "thing.delete", "sub_permissions": []}
12	{"tags": [], "permission_name": "thing.see_sensitive", "sub_permissions": []}
13	{"tags": [], "permission_name": "thing.super", "sub_permissions": ["thing.read", "thing.create", "thing.edit", "thing.delete", "thing.see_sensitive"]}
14	{"tags": [], "permission_name": "perms.users.create", "sub_permissions": []}
15	{"tags": [], "permission_name": "perms.users.read", "sub_permissions": []}
16	{"tags": [], "permission_name": "perms.users.delete", "sub_permissions": []}
17	{"tags": [], "permission_name": "perms.permissions.read", "sub_permissions": []}
18	{"tags": [], "permission_name": "perms.permissions.create", "sub_permissions": []}
19	{"tags": [], "permission_name": "perms.permissions.delete", "sub_permissions": []}
20	{"tags": [], "permission_name": "perms.permissions", "sub_permissions": ["perms.permissions.read", "perms.permissions.create", "perms.permissions.delete"]}
21	{"tags": [], "permission_name": "perms.users", "sub_permissions": ["perms.users.create", "perms.users.read", "perms.users.delete"]}
22	{"tags": [], "permission_name": "perms", "sub_permissions": ["perms.users", "perms.permissions"]}
23	{"tags": [], "permission_name": "retrieve.read", "sub_permissions": []}
\.


--
-- Name: permissions__id_seq; Type: SEQUENCE SET; Schema: diku; Owner: dbuser
--

SELECT pg_catalog.setval('permissions__id_seq', 23, true);


--
-- Data for Name: permissions_users; Type: TABLE DATA; Schema: diku; Owner: dbuser
--

COPY permissions_users (_id, jsonb) FROM stdin;
1	{"username": "jack", "permissions": ["thing.read"]}
2	{"username": "shane", "permissions": ["perms"]}
3	{"username": "jill", "permissions": ["thing.read", "thing.see_sensitive", "thing.create", "thing.delete"]}
4	{"username": "joe", "permissions": []}
\.


--
-- Name: permissions_users__id_seq; Type: SEQUENCE SET; Schema: diku; Owner: dbuser
--

SELECT pg_catalog.setval('permissions_users__id_seq', 4, true);


--
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: diku; Owner: dbuser
--

ALTER TABLE ONLY permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (_id);


--
-- Name: permissions_users permissions_users_pkey; Type: CONSTRAINT; Schema: diku; Owner: dbuser
--

ALTER TABLE ONLY permissions_users
    ADD CONSTRAINT permissions_users_pkey PRIMARY KEY (_id);


--
-- Name: permissions; Type: ACL; Schema: diku; Owner: dbuser
--

GRANT ALL ON TABLE permissions TO diku;


--
-- Name: permissions__id_seq; Type: ACL; Schema: diku; Owner: dbuser
--

GRANT ALL ON SEQUENCE permissions__id_seq TO diku;


--
-- Name: permissions_users; Type: ACL; Schema: diku; Owner: dbuser
--

GRANT ALL ON TABLE permissions_users TO diku;


--
-- Name: permissions_users__id_seq; Type: ACL; Schema: diku; Owner: dbuser
--

GRANT ALL ON SEQUENCE permissions_users__id_seq TO diku;


--
-- PostgreSQL database dump complete
--

