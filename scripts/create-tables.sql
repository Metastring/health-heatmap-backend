-- Tables

CREATE TABLE public.dataelements (
    id bigint NOT NULL,
    settlement character varying(255),
    gender character varying(255),
    end_time date,
    start_time date,
    value character varying(255),
    geography_id bigint,
    indicator_id bigint,
    report_id bigint,
    source_id bigint,
    upload_id bigint
);

CREATE TABLE public.geographies (
    id bigint NOT NULL,
    name character varying(255),
    ceased date,
    established date,
    type character varying(255),
    short_code character varying(255),
    wikidata_code character varying(255),
    belongs_to bigint
);

CREATE TABLE public.indicators (
    id bigint NOT NULL,
    name character varying(255),
    derivation character varying(255),
    short_code character varying(255),
    group_1 character varying(255),
    group_2 character varying(255)
);

CREATE TABLE public.indicator_groups (
    id bigint NOT NULL,
    name character varying(255)
);

CREATE TABLE public.indicator_group_hierarchy (
    id bigint NOT NULL,
    level_1 bigint,
    level_2 bigint,
    level_3 bigint,
    level_4 bigint,
    level_5 bigint,
    level_6 bigint
);

CREATE TABLE public.reports (
    id bigint NOT NULL,
    uri character varying(255)
);

CREATE TABLE public.sources (
    id bigint NOT NULL,
    name character varying(255),
    end_time date,
    start_time date,
    belongs_to bigint
);

CREATE TABLE public.source_geographies (
    id bigint NOT NULL,
    name character varying(255),
    source bigint,
    original bigint
);

CREATE TABLE public.source_indicators (
    id bigint NOT NULL,
    name character varying(255),
    source bigint,
    original bigint
);

CREATE TABLE public.uploads (
    id bigint NOT NULL,
    report_id bigint,
    source_id bigint
);

CREATE TABLE public.indicator_group_mappings (
    indicator_id bigint,
    group_id bigint
);


-- Sequences

CREATE SEQUENCE public.dataelements_sequence
   START WITH 1
   INCREMENT BY 50
   NO MINVALUE
   NO MAXVALUE
   CACHE 1;

CREATE SEQUENCE public.geographies_sequence
   START WITH 1
   INCREMENT BY 50
   NO MINVALUE
   NO MAXVALUE
   CACHE 1;

CREATE SEQUENCE public.indicators_sequence
   START WITH 1
   INCREMENT BY 50
   NO MINVALUE
   NO MAXVALUE
   CACHE 1;

CREATE SEQUENCE public.indicator_groups_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.indicator_group_hierarchy_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.reports_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.sources_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.source_geographies_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.source_indicators_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE public.uploads_sequence
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Primary Keys

ALTER TABLE ONLY public.dataelements
    ADD CONSTRAINT dataelements_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.geographies
    ADD CONSTRAINT geographies_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.indicators
    ADD CONSTRAINT indicators_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sources
    ADD CONSTRAINT sources_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.source_geographies
    ADD CONSTRAINT source_geographies_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.source_indicators
    ADD CONSTRAINT source_indicators_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.uploads
    ADD CONSTRAINT uploads_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.indicator_groups
    ADD CONSTRAINT indicator_groups_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.indicator_group_hierarchy
    ADD CONSTRAINT indicator_group_hierarchy_pkey PRIMARY KEY (id);

-- Foreign Keys

ALTER TABLE ONLY public.dataelements
    ADD CONSTRAINT data_element_geography_id_fk FOREIGN KEY (geography_id) REFERENCES public.geographies(id);

ALTER TABLE ONLY public.dataelements
    ADD CONSTRAINT data_element_indicator_id_fk FOREIGN KEY (indicator_id) REFERENCES public.indicators(id);

ALTER TABLE ONLY public.dataelements
    ADD CONSTRAINT data_element_report_id_fk FOREIGN KEY (report_id) REFERENCES public.reports(id);

ALTER TABLE ONLY public.dataelements
    ADD CONSTRAINT data_element_source_id_fk FOREIGN KEY (source_id) REFERENCES public.sources(id);

ALTER TABLE ONLY public.dataelements
    ADD CONSTRAINT data_element_upload_id_fk FOREIGN KEY (upload_id) REFERENCES public.uploads(id);

ALTER TABLE ONLY public.geographies
    ADD CONSTRAINT geography_belongs_to_fk FOREIGN KEY (belongs_to) REFERENCES public.geographies(id);

ALTER TABLE ONLY public.sources
    ADD CONSTRAINT source_belongs_to_fk FOREIGN KEY (belongs_to) REFERENCES public.sources(id);

ALTER TABLE ONLY public.source_geographies
    ADD CONSTRAINT source_geography_source_fk FOREIGN KEY (source) REFERENCES public.sources(id),
    ADD CONSTRAINT source_geography_original_fk FOREIGN KEY (original) REFERENCES public.geographies(id);

ALTER TABLE ONLY public.source_indicators
    ADD CONSTRAINT source_indicator_source_fk FOREIGN KEY (source) REFERENCES public.sources(id),
    ADD CONSTRAINT source_indicator_original_fk FOREIGN KEY (original) REFERENCES public.indicators(id);

ALTER TABLE ONLY public.uploads
    ADD CONSTRAINT upload_report_id_fk FOREIGN KEY (report_id) REFERENCES public.reports(id);

ALTER TABLE ONLY public.uploads
    ADD CONSTRAINT upload_source_id_fk FOREIGN KEY (source_id) REFERENCES public.sources(id);

ALTER TABLE ONLY public.indicator_group_hierarchy
    ADD CONSTRAINT indicator_group_level_1_fk FOREIGN KEY (level_1) REFERENCES public.indicator_groups(id),
    ADD CONSTRAINT indicator_group_level_2_fk FOREIGN KEY (level_2) REFERENCES public.indicator_groups(id),
    ADD CONSTRAINT indicator_group_level_3_fk FOREIGN KEY (level_3) REFERENCES public.indicator_groups(id),
    ADD CONSTRAINT indicator_group_level_4_fk FOREIGN KEY (level_4) REFERENCES public.indicator_groups(id),
    ADD CONSTRAINT indicator_group_level_5_fk FOREIGN KEY (level_5) REFERENCES public.indicator_groups(id),
    ADD CONSTRAINT indicator_group_level_6_fk FOREIGN KEY (level_6) REFERENCES public.indicator_groups(id);

ALTER TABLE ONLY public.indicator_group_mappings
    ADD CONSTRAINT indicator_mapping_indicator_fk FOREIGN KEY (indicator_id) REFERENCES public.indicators(id),
    ADD CONSTRAINT indicator_mapping_group_fk FOREIGN KEY (group_id) REFERENCES public.indicator_groups(id);

-- Set permissions

ALTER TABLE public.dataelements OWNER TO metastring;
ALTER TABLE public.geographies OWNER TO metastring;
ALTER TABLE public.indicators OWNER TO metastring;
ALTER TABLE public.reports OWNER TO metastring;
ALTER TABLE public.uploads OWNER TO metastring;
ALTER TABLE public.sources OWNER TO metastring;
ALTER TABLE public.source_geographies OWNER TO metastring;
ALTER TABLE public.source_indicators OWNER TO metastring;
ALTER TABLE public.indicator_groups OWNER TO metastring;
ALTER TABLE public.indicator_group_hierarchy OWNER TO metastring;


ALTER TABLE public.dataelements_sequence OWNER TO metastring;
ALTER TABLE public.geographies_sequence OWNER TO metastring;
ALTER TABLE public.indicators_sequence OWNER TO metastring;
ALTER TABLE public.indicator_groups_sequence OWNER TO metastring;
ALTER TABLE public.indicator_group_hierarchy_sequence OWNER TO metastring;
ALTER TABLE public.reports_sequence OWNER TO metastring;
ALTER TABLE public.sources_sequence OWNER TO metastring;
ALTER TABLE public.source_geographies_sequence OWNER TO metastring;
ALTER TABLE public.source_indicators_sequence OWNER TO metastring;
ALTER TABLE public.uploads_sequence OWNER TO metastring;

-- Finished creating database structure successfully