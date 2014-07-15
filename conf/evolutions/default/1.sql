# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table author (
  id                        bigint not null,
  fullname                  varchar(255),
  lastname                  varchar(255),
  firstname                 varchar(255),
  affiliation               varchar(255),
  constraint pk_author primary key (id))
;

create table etag (
  etag                      varchar(16) not null,
  uri                       varchar(4000),
  method                    varchar(10),
  hash                      varchar(40),
  total                     integer,
  count                     integer,
  skip                      integer,
  top                       integer,
  status                    integer,
  timestamp                 timestamp,
  modified                  timestamp,
  filter                    varchar(1024),
  constraint pk_etag primary key (etag))
;

create table etag_id (
  etag_etag                 varchar(16),
  id                        bigint)
;

create table edit (
  id                        bigint not null,
  type                      varchar(255),
  refid                     bigint,
  timestamp                 timestamp,
  principal_id              bigint,
  path                      varchar(1024),
  old_value                 clob,
  new_value                 clob,
  constraint pk_edit primary key (id))
;

create table funding (
  id                        bigint not null,
  grant_id                  bigint not null,
  ic                        varchar(255),
  amount                    integer,
  constraint pk_funding primary key (id))
;

create table grant (
  id                        bigint not null,
  application_id            bigint,
  activity                  varchar(255),
  administering_ic          varchar(255),
  application_type          integer,
  is_arra_funded            boolean,
  award_notice_date         timestamp,
  budget_start              timestamp,
  budget_end                timestamp,
  cfda_code                 integer,
  foa_number                varchar(255),
  full_project_num          varchar(255),
  subproject_id             bigint,
  fiscal_year               integer,
  ic_name                   varchar(255),
  ed_inst_type              varchar(255),
  nih_spending_cats         varchar(255),
  program_officer_name      varchar(255),
  project_start             timestamp,
  project_end               timestamp,
  core_project_num          varchar(255),
  project_title             varchar(255),
  public_health_relevance   clob,
  serial_number             bigint,
  study_section             varchar(255),
  study_section_name        varchar(255),
  suffix                    varchar(255),
  funding_mechanism         varchar(255),
  total_cost                integer,
  total_cost_subproject     integer,
  project_abstract          varchar(4000),
  constraint pk_grant primary key (id))
;

create table investigator (
  id                        bigint not null,
  name                      varchar(255),
  pi_id                     bigint,
  organization_id           bigint,
  role                      integer,
  constraint ck_investigator_role check (role in (0,1)),
  constraint uq_investigator_pi_id unique (pi_id),
  constraint pk_investigator primary key (id))
;

create table keyword (
  id                        bigint not null,
  grant_id                  bigint not null,
  term                      varchar(255),
  constraint pk_keyword primary key (id))
;

create table organization (
  id                        bigint not null,
  duns                      varchar(10),
  name                      varchar(255),
  department                varchar(255),
  city                      varchar(255),
  state                     varchar(20),
  zipcode                   varchar(15),
  district                  varchar(255),
  country                   varchar(255),
  fips                      varchar(3),
  constraint pk_organization primary key (id))
;

create table payload (
  id                        bigint not null,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime_type                 varchar(128),
  size                      bigint,
  constraint pk_payload primary key (id))
;

create table permission (
  id                        bigint not null,
  principal_id              bigint not null,
  resource                  varchar(255),
  access                    varchar(255),
  constraint pk_permission primary key (id))
;

create table principal (
  id                        bigint not null,
  name                      varchar(255),
  constraint uq_principal_name unique (name),
  constraint pk_principal primary key (id))
;

create table processing_status (
  id                        bigint not null,
  status                    integer,
  message                   varchar(4000),
  payload_id                bigint,
  constraint ck_processing_status_status check (status in (0,1,2)),
  constraint pk_processing_status primary key (id))
;

create table publication (
  id                        bigint not null,
  grant_id                  bigint not null,
  pmid                      bigint,
  pmcid                     bigint,
  title                     varchar(1024),
  year                      integer,
  pages                     varchar(255),
  doi                       varchar(255),
  abstract_text             varchar(4000),
  journal                   varchar(255),
  volume                    integer,
  issue                     integer,
  issn                      varchar(10),
  constraint pk_publication primary key (id))
;


create table grant_investigator (
  grant_id                       bigint not null,
  investigator_id                bigint not null,
  constraint pk_grant_investigator primary key (grant_id, investigator_id))
;

create table publication_keyword (
  publication_id                 bigint not null,
  keyword_id                     bigint not null,
  constraint pk_publication_keyword primary key (publication_id, keyword_id))
;

create table publication_author (
  publication_id                 bigint not null,
  author_id                      bigint not null,
  constraint pk_publication_author primary key (publication_id, author_id))
;
create sequence author_seq;

create sequence etag_seq;

create sequence edit_seq;

create sequence funding_seq;

create sequence grant_seq;

create sequence investigator_seq;

create sequence keyword_seq;

create sequence organization_seq;

create sequence payload_seq;

create sequence permission_seq;

create sequence principal_seq;

create sequence processing_status_seq;

create sequence publication_seq;

alter table etag_id add constraint fk_etag_id_etag_1 foreign key (etag_etag) references etag (etag) on delete restrict on update restrict;
create index ix_etag_id_etag_1 on etag_id (etag_etag);
alter table edit add constraint fk_edit_principal_2 foreign key (principal_id) references principal (id) on delete restrict on update restrict;
create index ix_edit_principal_2 on edit (principal_id);
alter table funding add constraint fk_funding_grant_3 foreign key (grant_id) references grant (id) on delete restrict on update restrict;
create index ix_funding_grant_3 on funding (grant_id);
alter table investigator add constraint fk_investigator_organization_4 foreign key (organization_id) references organization (id) on delete restrict on update restrict;
create index ix_investigator_organization_4 on investigator (organization_id);
alter table keyword add constraint fk_keyword_grant_5 foreign key (grant_id) references grant (id) on delete restrict on update restrict;
create index ix_keyword_grant_5 on keyword (grant_id);
alter table permission add constraint fk_permission_principal_6 foreign key (principal_id) references principal (id) on delete restrict on update restrict;
create index ix_permission_principal_6 on permission (principal_id);
alter table processing_status add constraint fk_processing_status_payload_7 foreign key (payload_id) references payload (id) on delete restrict on update restrict;
create index ix_processing_status_payload_7 on processing_status (payload_id);
alter table publication add constraint fk_publication_grant_8 foreign key (grant_id) references grant (id) on delete restrict on update restrict;
create index ix_publication_grant_8 on publication (grant_id);



alter table grant_investigator add constraint fk_grant_investigator_grant_01 foreign key (grant_id) references grant (id) on delete restrict on update restrict;

alter table grant_investigator add constraint fk_grant_investigator_investi_02 foreign key (investigator_id) references investigator (id) on delete restrict on update restrict;

alter table publication_keyword add constraint fk_publication_keyword_public_01 foreign key (publication_id) references publication (id) on delete restrict on update restrict;

alter table publication_keyword add constraint fk_publication_keyword_keywor_02 foreign key (keyword_id) references keyword (id) on delete restrict on update restrict;

alter table publication_author add constraint fk_publication_author_publica_01 foreign key (publication_id) references publication (id) on delete restrict on update restrict;

alter table publication_author add constraint fk_publication_author_author_02 foreign key (author_id) references author (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists author;

drop table if exists etag;

drop table if exists etag_id;

drop table if exists edit;

drop table if exists funding;

drop table if exists grant;

drop table if exists grant_investigator;

drop table if exists investigator;

drop table if exists keyword;

drop table if exists organization;

drop table if exists payload;

drop table if exists permission;

drop table if exists principal;

drop table if exists processing_status;

drop table if exists publication;

drop table if exists publication_keyword;

drop table if exists publication_author;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists author_seq;

drop sequence if exists etag_seq;

drop sequence if exists edit_seq;

drop sequence if exists funding_seq;

drop sequence if exists grant_seq;

drop sequence if exists investigator_seq;

drop sequence if exists keyword_seq;

drop sequence if exists organization_seq;

drop sequence if exists payload_seq;

drop sequence if exists permission_seq;

drop sequence if exists principal_seq;

drop sequence if exists processing_status_seq;

drop sequence if exists publication_seq;

