create table dataelements (
    id int8 not null,
    end_time timestamp,
    start_time timestamp,
    value varchar(255),
    geography_id int8,
    indicator_id int8,
    source_id int8,
    primary key (id)
);

create table geographies (
   id int8 not null,
    name varchar(255),
    ceased timestamp,
    established timestamp,
    type varchar(255),
    short_code varchar(255),
    belongs_to_id int8,
    primary key (id)
);

create table indicators (
   id int8 not null,
    name varchar(255),
    derivation varchar(255),
    short_code varchar(255),
    primary key (id)
)

create table sources (
   id int8 not null,
    name varchar(255),
    primary key (id)
)

alter table if exists dataelements
   add constraint geography_id_fk
   foreign key (geography_id)
   references geographies

alter table if exists dataelements
   add constraint indicator_id_fk
   foreign key (indicator_id)
   references indicators

alter table if exists dataelements
   add constraint source_id_fk
   foreign key (source_id)
   references sources

alter table if exists geographies
   add constraint FKaaigiqh0oy0ii5yjiy57rir7l
   foreign key (belongs_to_id)
   references geographies