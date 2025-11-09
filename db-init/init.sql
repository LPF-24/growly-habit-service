--
-- PostgreSQL database dump
--

-- Dumped from database version 15.12 (Debian 15.12-1.pgdg120+1)
-- Dumped by pg_dump version 15.12 (Debian 15.12-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: habit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.habit (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description character varying(255),
    created_at date NOT NULL,
    active boolean,
    person_id bigint NOT NULL
);


--
-- Name: habit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.habit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: habit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.habit_id_seq OWNED BY public.habit.id;


--
-- Name: habit id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.habit ALTER COLUMN id SET DEFAULT nextval('public.habit_id_seq'::regclass);


--
-- Name: habit habit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.habit
    ADD CONSTRAINT habit_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

