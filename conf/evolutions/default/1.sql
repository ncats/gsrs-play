# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ct_author (
  id                        bigint not null,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  affiliation               varchar(255),
  constraint pk_ct_author primary key (id))
;

create table ct_etag (
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
  constraint pk_ct_etag primary key (etag))
;

create table ct_etag_id (
  etag_etag                 varchar(16),
  id                        bigint)
;

create table ct_edit (
  id                        bigint not null,
  type                      varchar(255),
  refid                     bigint,
  timestamp                 timestamp,
  principal_id              bigint,
  path                      varchar(1024),
  old_value                 clob,
  new_value                 clob,
  constraint pk_ct_edit primary key (id))
;

create table ct_granite_funding (
  id                        bigint not null,
  grant_id                  bigint not null,
  ic                        varchar(255),
  amount                    integer,
  constraint pk_ct_granite_funding primary key (id))
;

create table ct_granite_grant (
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
  project_abstract          clob,
  constraint pk_ct_granite_grant primary key (id))
;

create table ct_granite_investigator (
  id                        bigint not null,
  name                      varchar(255),
  pi_id                     bigint,
  organization_id           bigint,
  role                      integer,
  constraint ck_ct_granite_investigator_role check (role in (0,1)),
  constraint pk_ct_granite_investigator primary key (id))
;

create table ct_journal (
  id                        bigint not null,
  issn                      varchar(10),
  volume                    integer,
  issue                     integer,
  year                      integer,
  month                     integer,
  title                     varchar(255),
  iso_abbr                  varchar(255),
  constraint pk_ct_journal primary key (id))
;

create table ct_keyword (
  id                        bigint not null,
  term                      varchar(255),
  constraint pk_ct_keyword primary key (id))
;

create table ct_link (
  id                        bigint not null,
  name                      varchar(255),
  dir                       integer,
  uri                       varchar(1024),
  source                    varchar(255),
  source_id                 bigint,
  target                    varchar(255),
  target_id                 bigint,
  constraint ck_ct_link_dir check (dir in (0,1,2,3)),
  constraint pk_ct_link primary key (id))
;

create table ct_mesh (
  id                        bigint not null,
  term                      varchar(255),
  major_topic               boolean,
  constraint pk_ct_mesh primary key (id))
;

create table ct_granite_organization (
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
  longitude                 double,
  latitude                  double,
  constraint pk_ct_granite_organization primary key (id))
;

create table ct_payload (
  id                        bigint not null,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime_type                 varchar(128),
  size                      bigint,
  constraint pk_ct_payload primary key (id))
;

create table ct_permission (
  id                        bigint not null,
  principal_id              bigint not null,
  resource                  varchar(255),
  access                    varchar(255),
  constraint pk_ct_permission primary key (id))
;

create table ct_principal (
  id                        bigint not null,
  name                      varchar(255),
  constraint uq_ct_principal_name unique (name),
  constraint pk_ct_principal primary key (id))
;

create table ct_processing_status (
  id                        bigint not null,
  status                    integer,
  message                   varchar(4000),
  payload_id                bigint,
  constraint ck_ct_processing_status_status check (status in (0,1,2)),
  constraint pk_ct_processing_status primary key (id))
;

create table ct_publication (
  id                        bigint not null,
  grant_id                  bigint not null,
  pmid                      bigint,
  pmcid                     bigint,
  title                     varchar(1024),
  pages                     varchar(255),
  doi                       varchar(255),
  abstract_text             clob,
  journal_id                bigint,
  constraint pk_ct_publication primary key (id))
;


create table ct_granite_grant_investigator (
  ct_granite_grant_id            bigint not null,
  ct_granite_investigator_id     bigint not null,
  constraint pk_ct_granite_grant_investigator primary key (ct_granite_grant_id, ct_granite_investigator_id))
;

create table ct_granite_grant_keyword (
  ct_granite_grant_id            bigint not null,
  ct_keyword_id                  bigint not null,
  constraint pk_ct_granite_grant_keyword primary key (ct_granite_grant_id, ct_keyword_id))
;

create table ct_publication_keyword (
  ct_publication_id              bigint not null,
  ct_keyword_id                  bigint not null,
  constraint pk_ct_publication_keyword primary key (ct_publication_id, ct_keyword_id))
;

create table ct_publication_mesh (
  ct_publication_id              bigint not null,
  ct_mesh_id                     bigint not null,
  constraint pk_ct_publication_mesh primary key (ct_publication_id, ct_mesh_id))
;

create table ct_publication_author (
  ct_publication_id              bigint not null,
  ct_author_id                   bigint not null,
  constraint pk_ct_publication_author primary key (ct_publication_id, ct_author_id))
;
create sequence ct_author_seq;

create sequence ct_etag_seq;

create sequence ct_edit_seq;

create sequence ct_granite_funding_seq;

create sequence ct_granite_grant_seq;

create sequence ct_granite_investigator_seq;

create sequence ct_journal_seq;

create sequence ct_keyword_seq;

create sequence ct_link_seq;

create sequence ct_mesh_seq;

create sequence ct_granite_organization_seq;

create sequence ct_payload_seq;

create sequence ct_permission_seq;

create sequence ct_principal_seq;

create sequence ct_processing_status_seq;

create sequence ct_publication_seq;

alter table ct_etag_id add constraint fk_ct_etag_id_etag_1 foreign key (etag_etag) references ct_etag (etag) on delete restrict on update restrict;
create index ix_ct_etag_id_etag_1 on ct_etag_id (etag_etag);
alter table ct_edit add constraint fk_ct_edit_principal_2 foreign key (principal_id) references ct_principal (id) on delete restrict on update restrict;
create index ix_ct_edit_principal_2 on ct_edit (principal_id);
alter table ct_granite_funding add constraint fk_ct_granite_funding_ct_grani_3 foreign key (grant_id) references ct_granite_grant (id) on delete restrict on update restrict;
create index ix_ct_granite_funding_ct_grani_3 on ct_granite_funding (grant_id);
alter table ct_granite_investigator add constraint fk_ct_granite_investigator_org_4 foreign key (organization_id) references ct_granite_organization (id) on delete restrict on update restrict;
create index ix_ct_granite_investigator_org_4 on ct_granite_investigator (organization_id);
alter table ct_permission add constraint fk_ct_permission_ct_principal_5 foreign key (principal_id) references ct_principal (id) on delete restrict on update restrict;
create index ix_ct_permission_ct_principal_5 on ct_permission (principal_id);
alter table ct_processing_status add constraint fk_ct_processing_status_payloa_6 foreign key (payload_id) references ct_payload (id) on delete restrict on update restrict;
create index ix_ct_processing_status_payloa_6 on ct_processing_status (payload_id);
alter table ct_publication add constraint fk_ct_publication_ct_granite_g_7 foreign key (grant_id) references ct_granite_grant (id) on delete restrict on update restrict;
create index ix_ct_publication_ct_granite_g_7 on ct_publication (grant_id);
alter table ct_publication add constraint fk_ct_publication_journal_8 foreign key (journal_id) references ct_journal (id) on delete restrict on update restrict;
create index ix_ct_publication_journal_8 on ct_publication (journal_id);



alter table ct_granite_grant_investigator add constraint fk_ct_granite_grant_investiga_01 foreign key (ct_granite_grant_id) references ct_granite_grant (id) on delete restrict on update restrict;

alter table ct_granite_grant_investigator add constraint fk_ct_granite_grant_investiga_02 foreign key (ct_granite_investigator_id) references ct_granite_investigator (id) on delete restrict on update restrict;

alter table ct_granite_grant_keyword add constraint fk_ct_granite_grant_keyword_c_01 foreign key (ct_granite_grant_id) references ct_granite_grant (id) on delete restrict on update restrict;

alter table ct_granite_grant_keyword add constraint fk_ct_granite_grant_keyword_c_02 foreign key (ct_keyword_id) references ct_keyword (id) on delete restrict on update restrict;

alter table ct_publication_keyword add constraint fk_ct_publication_keyword_ct__01 foreign key (ct_publication_id) references ct_publication (id) on delete restrict on update restrict;

alter table ct_publication_keyword add constraint fk_ct_publication_keyword_ct__02 foreign key (ct_keyword_id) references ct_keyword (id) on delete restrict on update restrict;

alter table ct_publication_mesh add constraint fk_ct_publication_mesh_ct_pub_01 foreign key (ct_publication_id) references ct_publication (id) on delete restrict on update restrict;

alter table ct_publication_mesh add constraint fk_ct_publication_mesh_ct_mes_02 foreign key (ct_mesh_id) references ct_mesh (id) on delete restrict on update restrict;

alter table ct_publication_author add constraint fk_ct_publication_author_ct_p_01 foreign key (ct_publication_id) references ct_publication (id) on delete restrict on update restrict;

alter table ct_publication_author add constraint fk_ct_publication_author_ct_a_02 foreign key (ct_author_id) references ct_author (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ct_author;

drop table if exists ct_etag;

drop table if exists ct_etag_id;

drop table if exists ct_edit;

drop table if exists ct_granite_funding;

drop table if exists ct_granite_grant;

drop table if exists ct_granite_grant_investigator;

drop table if exists ct_granite_grant_keyword;

drop table if exists ct_granite_investigator;

drop table if exists ct_journal;

drop table if exists ct_keyword;

drop table if exists ct_link;

drop table if exists ct_mesh;

drop table if exists ct_granite_organization;

drop table if exists ct_payload;

drop table if exists ct_permission;

drop table if exists ct_principal;

drop table if exists ct_processing_status;

drop table if exists ct_publication;

drop table if exists ct_publication_keyword;

drop table if exists ct_publication_mesh;

drop table if exists ct_publication_author;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ct_author_seq;

drop sequence if exists ct_etag_seq;

drop sequence if exists ct_edit_seq;

drop sequence if exists ct_granite_funding_seq;

drop sequence if exists ct_granite_grant_seq;

drop sequence if exists ct_granite_investigator_seq;

drop sequence if exists ct_journal_seq;

drop sequence if exists ct_keyword_seq;

drop sequence if exists ct_link_seq;

drop sequence if exists ct_mesh_seq;

drop sequence if exists ct_granite_organization_seq;

drop sequence if exists ct_payload_seq;

drop sequence if exists ct_permission_seq;

drop sequence if exists ct_principal_seq;

drop sequence if exists ct_processing_status_seq;

drop sequence if exists ct_publication_seq;

