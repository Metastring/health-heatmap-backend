
CREATE TABLE public.dataelements (
    id bigint NOT NULL,
    settlement character varying(255),
    end_time date,
    start_time date,
    value character varying(255),
    geography_id bigint,
    indicator_id bigint,
    report_id bigint,
    source_id bigint,
    upload_id bigint
);


ALTER TABLE public.dataelements OWNER TO metastring;


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


ALTER TABLE public.geographies OWNER TO metastring;


CREATE TABLE public.geography_names (
    id bigint NOT NULL,
    name character varying(255),
    geography_id bigint
);


ALTER TABLE public.geography_names OWNER TO metastring;


CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO metastring;

CREATE TABLE public.indicators (
    id bigint NOT NULL,
    name character varying(255),
    derivation character varying(255),
    short_code character varying(255)
);


ALTER TABLE public.indicators OWNER TO metastring;


CREATE TABLE public.reports (
    id bigint NOT NULL,
    uri character varying(255)
);


ALTER TABLE public.reports OWNER TO metastring;


CREATE TABLE public.sources (
    id bigint NOT NULL,
    name character varying(255),
    end_time date,
    start_time date,
    belongs_to bigint
);


ALTER TABLE public.sources OWNER TO metastring;


CREATE TABLE public.uploads (
    id bigint NOT NULL,
    report_id bigint,
    source_id bigint
);


ALTER TABLE public.uploads OWNER TO metastring;


ALTER TABLE ONLY public.dataelements
    ADD CONSTRAINT dataelements_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.geographies
    ADD CONSTRAINT geographies_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.geography_names
    ADD CONSTRAINT geography_names_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.indicators
    ADD CONSTRAINT indicators_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.sources
    ADD CONSTRAINT sources_pkey PRIMARY KEY (id);



ALTER TABLE ONLY public.uploads
    ADD CONSTRAINT uploads_pkey PRIMARY KEY (id);


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


ALTER TABLE ONLY public.geography_names
    ADD CONSTRAINT fks0ah9dh55iyj0y37m26bnqch3 FOREIGN KEY (geography_id) REFERENCES public.geographies(id);


ALTER TABLE ONLY public.geographies
    ADD CONSTRAINT geography_belongs_to_fk FOREIGN KEY (belongs_to) REFERENCES public.geographies(id);


ALTER TABLE ONLY public.sources
    ADD CONSTRAINT source_belongs_to_fk FOREIGN KEY (belongs_to) REFERENCES public.sources(id);


ALTER TABLE ONLY public.uploads
    ADD CONSTRAINT upload_report_id_fk FOREIGN KEY (report_id) REFERENCES public.reports(id);


ALTER TABLE ONLY public.uploads
    ADD CONSTRAINT upload_source_id_fk FOREIGN KEY (source_id) REFERENCES public.sources(id);

