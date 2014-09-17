# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ct_core_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ct_core_acl_perm check (perm in (0,1,2,3,4,5)),
  constraint pk_ct_core_acl primary key (id))
;

create table ct_core_author (
  id                        bigint not null,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  affiliation               varchar(255),
  orcid                     varchar(20),
  email                     varchar(255),
  url                       varchar(1024),
  constraint pk_ct_core_author primary key (id))
;

create table ct_core_curation (
  id                        bigint not null,
  curator_id                bigint,
  status                    integer,
  timestamp                 timestamp,
  constraint ck_ct_core_curation_status check (status in (0,1,2)),
  constraint pk_ct_core_curation primary key (id))
;

create table ct_core_etag (
  id                        bigint not null,
  etag                      varchar(16),
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
  constraint pk_ct_core_etag primary key (id))
;

create table ct_core_etagref (
  id                        bigint not null,
  etag_id                   bigint,
  ref_id                    bigint,
  constraint pk_ct_core_etagref primary key (id))
;

create table ct_core_edit (
  id                        bigint not null,
  type                      varchar(255),
  refid                     bigint,
  timestamp                 timestamp,
  principal_id              bigint,
  path                      varchar(1024),
  comments                  clob,
  old_value                 clob,
  new_value                 clob,
  constraint pk_ct_core_edit primary key (id))
;

create table ct_ncats_funding (
  id                        bigint not null,
  grant_id                  bigint not null,
  ic                        varchar(255),
  amount                    integer,
  constraint pk_ct_ncats_funding primary key (id))
;

create table ct_ncats_grant (
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
  constraint pk_ct_ncats_grant primary key (id))
;

create table ct_core_group (
  id                        bigint not null,
  name                      varchar(255),
  constraint uq_ct_core_group_name unique (name),
  constraint pk_ct_core_group primary key (id))
;

create table ct_core_investigator (
  id                        bigint not null,
  name                      varchar(255),
  pi_id                     bigint,
  organization_id           bigint,
  role                      integer,
  constraint ck_ct_core_investigator_role check (role in (0,1)),
  constraint pk_ct_core_investigator primary key (id))
;

create table ct_core_journal (
  id                        bigint not null,
  issn                      varchar(10),
  volume                    integer,
  issue                     integer,
  year                      integer,
  month                     integer,
  title                     varchar(255),
  iso_abbr                  varchar(255),
  constraint pk_ct_core_journal primary key (id))
;

create table ct_core_keyword (
  id                        bigint not null,
  property_id               bigint,
  curation_id               bigint,
  term                      varchar(255),
  constraint pk_ct_core_keyword primary key (id))
;

create table ct_core_link (
  id                        bigint not null,
  name                      varchar(255),
  dir                       integer,
  uri                       varchar(1024),
  source                    varchar(255),
  source_id                 bigint,
  target                    varchar(255),
  target_id                 bigint,
  constraint ck_ct_core_link_dir check (dir in (0,1,2,3)),
  constraint pk_ct_core_link primary key (id))
;

create table ct_core_mesh (
  id                        bigint not null,
  property_id               bigint,
  curation_id               bigint,
  term                      varchar(255),
  major_topic               boolean,
  constraint pk_ct_core_mesh primary key (id))
;

create table ct_core_organization (
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
  constraint pk_ct_core_organization primary key (id))
;

create table ct_core_payload (
  id                        bigint not null,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime                      varchar(128),
  size                      bigint,
  created                   timestamp,
  constraint pk_ct_core_payload primary key (id))
;

create table ct_core_principal (
  id                        bigint not null,
  name                      varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  pkey                      varchar(256),
  constraint uq_ct_core_principal_pkey unique (pkey),
  constraint pk_ct_core_principal primary key (id))
;

create table ct_core_processingstatus (
  id                        bigint not null,
  status                    integer,
  message                   varchar(4000),
  payload_id                bigint,
  constraint ck_ct_core_processingstatus_status check (status in (0,1,2)),
  constraint pk_ct_core_processingstatus primary key (id))
;

create table ct_core_property (
  id                        bigint not null,
  name                      varchar(255),
  type                      varchar(255),
  resource_id               bigint,
  label                     varchar(255),
  constraint pk_ct_core_property primary key (id))
;

create table ct_core_publication (
  id                        bigint not null,
  grant_id                  bigint not null,
  pmid                      bigint,
  pmcid                     bigint,
  title                     varchar(1024),
  pages                     varchar(255),
  doi                       varchar(255),
  abstract_text             clob,
  journal_id                bigint,
  constraint pk_ct_core_publication primary key (id))
;

create table ct_core_resource (
  id                        bigint not null,
  name                      varchar(255),
  modifier                  integer,
  constraint ck_ct_core_resource_modifier check (modifier in (0,1,2)),
  constraint uq_ct_core_resource_name unique (name),
  constraint pk_ct_core_resource primary key (id))
;

create table ct_core_role (
  id                        bigint not null,
  role                      integer,
  principal_id              bigint,
  constraint ck_ct_core_role_role check (role in (0,1,2,3)),
  constraint pk_ct_core_role primary key (id))
;

create table ct_core_stitch (
  id                        bigint not null,
  name                      varchar(255),
  impl                      varchar(1024),
  description               clob,
  constraint pk_ct_core_stitch primary key (id))
;

create table ct_core_vint (
  id                        bigint not null,
  property_id               bigint,
  curation_id               bigint,
  value                     bigint,
  constraint pk_ct_core_vint primary key (id))
;

create table ct_core_vnum (
  id                        bigint not null,
  property_id               bigint,
  curation_id               bigint,
  value                     double,
  constraint pk_ct_core_vnum primary key (id))
;

create table ct_core_vstr (
  id                        bigint not null,
  property_id               bigint,
  curation_id               bigint,
  value                     varchar(1024),
  constraint pk_ct_core_vstr primary key (id))
;

create table ct_core_value (
  id                        bigint not null,
  property_id               bigint,
  curation_id               bigint,
  constraint pk_ct_core_value primary key (id))
;


create table ct_core_acl_principal (
  ct_core_acl_id                 bigint not null,
  ct_core_principal_id           bigint not null,
  constraint pk_ct_core_acl_principal primary key (ct_core_acl_id, ct_core_principal_id))
;

create table ct_core_acl_group (
  ct_core_acl_id                 bigint not null,
  ct_core_group_id               bigint not null,
  constraint pk_ct_core_acl_group primary key (ct_core_acl_id, ct_core_group_id))
;

create table ct_ncats_grant_investigator (
  ct_ncats_grant_id              bigint not null,
  ct_core_investigator_id        bigint not null,
  constraint pk_ct_ncats_grant_investigator primary key (ct_ncats_grant_id, ct_core_investigator_id))
;

create table ct_ncats_grant_keyword (
  ct_ncats_grant_id              bigint not null,
  ct_core_keyword_id             bigint not null,
  constraint pk_ct_ncats_grant_keyword primary key (ct_ncats_grant_id, ct_core_keyword_id))
;

create table ct_core_group_principal (
  ct_core_group_id               bigint not null,
  ct_core_principal_id           bigint not null,
  constraint pk_ct_core_group_principal primary key (ct_core_group_id, ct_core_principal_id))
;

create table ct_core_payload_property (
  ct_core_payload_id             bigint not null,
  ct_core_property_id            bigint not null,
  constraint pk_ct_core_payload_property primary key (ct_core_payload_id, ct_core_property_id))
;

create table ct_core_publication_keyword (
  ct_core_publication_id         bigint not null,
  ct_core_keyword_id             bigint not null,
  constraint pk_ct_core_publication_keyword primary key (ct_core_publication_id, ct_core_keyword_id))
;

create table ct_core_publication_mesh (
  ct_core_publication_id         bigint not null,
  ct_core_mesh_id                bigint not null,
  constraint pk_ct_core_publication_mesh primary key (ct_core_publication_id, ct_core_mesh_id))
;

create table ct_core_publication_author (
  ct_core_publication_id         bigint not null,
  ct_core_author_id              bigint not null,
  constraint pk_ct_core_publication_author primary key (ct_core_publication_id, ct_core_author_id))
;

create table ct_core_resource_role (
  ct_core_resource_id            bigint not null,
  ct_core_role_id                bigint not null,
  constraint pk_ct_core_resource_role primary key (ct_core_resource_id, ct_core_role_id))
;

create table ct_core_resource_acl (
  ct_core_resource_id            bigint not null,
  ct_core_acl_id                 bigint not null,
  constraint pk_ct_core_resource_acl primary key (ct_core_resource_id, ct_core_acl_id))
;

create table ct_core_stitch_property (
  ct_core_stitch_id              bigint not null,
  ct_core_property_id            bigint not null,
  constraint pk_ct_core_stitch_property primary key (ct_core_stitch_id, ct_core_property_id))
;
create sequence ct_core_acl_seq;

create sequence ct_core_author_seq;

create sequence ct_core_curation_seq;

create sequence ct_core_etag_seq;

create sequence ct_core_etagref_seq;

create sequence ct_core_edit_seq;

create sequence ct_ncats_funding_seq;

create sequence ct_ncats_grant_seq;

create sequence ct_core_group_seq;

create sequence ct_core_investigator_seq;

create sequence ct_core_journal_seq;

create sequence ct_core_keyword_seq;

create sequence ct_core_link_seq;

create sequence ct_core_mesh_seq;

create sequence ct_core_organization_seq;

create sequence ct_core_payload_seq;

create sequence ct_core_principal_seq;

create sequence ct_core_processingstatus_seq;

create sequence ct_core_property_seq;

create sequence ct_core_publication_seq;

create sequence ct_core_resource_seq;

create sequence ct_core_role_seq;

create sequence ct_core_stitch_seq;

create sequence ct_core_vint_seq;

create sequence ct_core_vnum_seq;

create sequence ct_core_vstr_seq;

create sequence ct_core_value_seq;

alter table ct_core_curation add constraint fk_ct_core_curation_curator_1 foreign key (curator_id) references ct_core_principal (id) on delete restrict on update restrict;
create index ix_ct_core_curation_curator_1 on ct_core_curation (curator_id);
alter table ct_core_etagref add constraint fk_ct_core_etagref_etag_2 foreign key (etag_id) references ct_core_etag (id) on delete restrict on update restrict;
create index ix_ct_core_etagref_etag_2 on ct_core_etagref (etag_id);
alter table ct_core_edit add constraint fk_ct_core_edit_principal_3 foreign key (principal_id) references ct_core_principal (id) on delete restrict on update restrict;
create index ix_ct_core_edit_principal_3 on ct_core_edit (principal_id);
alter table ct_ncats_funding add constraint fk_ct_ncats_funding_ct_ncats_g_4 foreign key (grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;
create index ix_ct_ncats_funding_ct_ncats_g_4 on ct_ncats_funding (grant_id);
alter table ct_core_investigator add constraint fk_ct_core_investigator_organi_5 foreign key (organization_id) references ct_core_organization (id) on delete restrict on update restrict;
create index ix_ct_core_investigator_organi_5 on ct_core_investigator (organization_id);
alter table ct_core_keyword add constraint fk_ct_core_keyword_property_6 foreign key (property_id) references ct_core_property (id) on delete restrict on update restrict;
create index ix_ct_core_keyword_property_6 on ct_core_keyword (property_id);
alter table ct_core_keyword add constraint fk_ct_core_keyword_curation_7 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_keyword_curation_7 on ct_core_keyword (curation_id);
alter table ct_core_mesh add constraint fk_ct_core_mesh_property_8 foreign key (property_id) references ct_core_property (id) on delete restrict on update restrict;
create index ix_ct_core_mesh_property_8 on ct_core_mesh (property_id);
alter table ct_core_mesh add constraint fk_ct_core_mesh_curation_9 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_mesh_curation_9 on ct_core_mesh (curation_id);
alter table ct_core_processingstatus add constraint fk_ct_core_processingstatus_p_10 foreign key (payload_id) references ct_core_payload (id) on delete restrict on update restrict;
create index ix_ct_core_processingstatus_p_10 on ct_core_processingstatus (payload_id);
alter table ct_core_property add constraint fk_ct_core_property_resource_11 foreign key (resource_id) references ct_core_resource (id) on delete restrict on update restrict;
create index ix_ct_core_property_resource_11 on ct_core_property (resource_id);
alter table ct_core_publication add constraint fk_ct_core_publication_ct_nca_12 foreign key (grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;
create index ix_ct_core_publication_ct_nca_12 on ct_core_publication (grant_id);
alter table ct_core_publication add constraint fk_ct_core_publication_journa_13 foreign key (journal_id) references ct_core_journal (id) on delete restrict on update restrict;
create index ix_ct_core_publication_journa_13 on ct_core_publication (journal_id);
alter table ct_core_role add constraint fk_ct_core_role_principal_14 foreign key (principal_id) references ct_core_principal (id) on delete restrict on update restrict;
create index ix_ct_core_role_principal_14 on ct_core_role (principal_id);
alter table ct_core_vint add constraint fk_ct_core_vint_property_15 foreign key (property_id) references ct_core_property (id) on delete restrict on update restrict;
create index ix_ct_core_vint_property_15 on ct_core_vint (property_id);
alter table ct_core_vint add constraint fk_ct_core_vint_curation_16 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_vint_curation_16 on ct_core_vint (curation_id);
alter table ct_core_vnum add constraint fk_ct_core_vnum_property_17 foreign key (property_id) references ct_core_property (id) on delete restrict on update restrict;
create index ix_ct_core_vnum_property_17 on ct_core_vnum (property_id);
alter table ct_core_vnum add constraint fk_ct_core_vnum_curation_18 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_vnum_curation_18 on ct_core_vnum (curation_id);
alter table ct_core_vstr add constraint fk_ct_core_vstr_property_19 foreign key (property_id) references ct_core_property (id) on delete restrict on update restrict;
create index ix_ct_core_vstr_property_19 on ct_core_vstr (property_id);
alter table ct_core_vstr add constraint fk_ct_core_vstr_curation_20 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_vstr_curation_20 on ct_core_vstr (curation_id);
alter table ct_core_value add constraint fk_ct_core_value_property_21 foreign key (property_id) references ct_core_property (id) on delete restrict on update restrict;
create index ix_ct_core_value_property_21 on ct_core_value (property_id);
alter table ct_core_value add constraint fk_ct_core_value_curation_22 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_value_curation_22 on ct_core_value (curation_id);



alter table ct_core_acl_principal add constraint fk_ct_core_acl_principal_ct_c_01 foreign key (ct_core_acl_id) references ct_core_acl (id) on delete restrict on update restrict;

alter table ct_core_acl_principal add constraint fk_ct_core_acl_principal_ct_c_02 foreign key (ct_core_principal_id) references ct_core_principal (id) on delete restrict on update restrict;

alter table ct_core_acl_group add constraint fk_ct_core_acl_group_ct_core__01 foreign key (ct_core_acl_id) references ct_core_acl (id) on delete restrict on update restrict;

alter table ct_core_acl_group add constraint fk_ct_core_acl_group_ct_core__02 foreign key (ct_core_group_id) references ct_core_group (id) on delete restrict on update restrict;

alter table ct_ncats_grant_investigator add constraint fk_ct_ncats_grant_investigato_01 foreign key (ct_ncats_grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;

alter table ct_ncats_grant_investigator add constraint fk_ct_ncats_grant_investigato_02 foreign key (ct_core_investigator_id) references ct_core_investigator (id) on delete restrict on update restrict;

alter table ct_ncats_grant_keyword add constraint fk_ct_ncats_grant_keyword_ct__01 foreign key (ct_ncats_grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;

alter table ct_ncats_grant_keyword add constraint fk_ct_ncats_grant_keyword_ct__02 foreign key (ct_core_keyword_id) references ct_core_keyword (id) on delete restrict on update restrict;

alter table ct_core_group_principal add constraint fk_ct_core_group_principal_ct_01 foreign key (ct_core_group_id) references ct_core_group (id) on delete restrict on update restrict;

alter table ct_core_group_principal add constraint fk_ct_core_group_principal_ct_02 foreign key (ct_core_principal_id) references ct_core_principal (id) on delete restrict on update restrict;

alter table ct_core_payload_property add constraint fk_ct_core_payload_property_c_01 foreign key (ct_core_payload_id) references ct_core_payload (id) on delete restrict on update restrict;

alter table ct_core_payload_property add constraint fk_ct_core_payload_property_c_02 foreign key (ct_core_property_id) references ct_core_property (id) on delete restrict on update restrict;

alter table ct_core_publication_keyword add constraint fk_ct_core_publication_keywor_01 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_publication_keyword add constraint fk_ct_core_publication_keywor_02 foreign key (ct_core_keyword_id) references ct_core_keyword (id) on delete restrict on update restrict;

alter table ct_core_publication_mesh add constraint fk_ct_core_publication_mesh_c_01 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_publication_mesh add constraint fk_ct_core_publication_mesh_c_02 foreign key (ct_core_mesh_id) references ct_core_mesh (id) on delete restrict on update restrict;

alter table ct_core_publication_author add constraint fk_ct_core_publication_author_01 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_publication_author add constraint fk_ct_core_publication_author_02 foreign key (ct_core_author_id) references ct_core_author (id) on delete restrict on update restrict;

alter table ct_core_resource_role add constraint fk_ct_core_resource_role_ct_c_01 foreign key (ct_core_resource_id) references ct_core_resource (id) on delete restrict on update restrict;

alter table ct_core_resource_role add constraint fk_ct_core_resource_role_ct_c_02 foreign key (ct_core_role_id) references ct_core_role (id) on delete restrict on update restrict;

alter table ct_core_resource_acl add constraint fk_ct_core_resource_acl_ct_co_01 foreign key (ct_core_resource_id) references ct_core_resource (id) on delete restrict on update restrict;

alter table ct_core_resource_acl add constraint fk_ct_core_resource_acl_ct_co_02 foreign key (ct_core_acl_id) references ct_core_acl (id) on delete restrict on update restrict;

alter table ct_core_stitch_property add constraint fk_ct_core_stitch_property_ct_01 foreign key (ct_core_stitch_id) references ct_core_stitch (id) on delete restrict on update restrict;

alter table ct_core_stitch_property add constraint fk_ct_core_stitch_property_ct_02 foreign key (ct_core_property_id) references ct_core_property (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ct_core_acl;

drop table if exists ct_core_acl_principal;

drop table if exists ct_core_acl_group;

drop table if exists ct_core_author;

drop table if exists ct_core_curation;

drop table if exists ct_core_etag;

drop table if exists ct_core_etagref;

drop table if exists ct_core_edit;

drop table if exists ct_ncats_funding;

drop table if exists ct_ncats_grant;

drop table if exists ct_ncats_grant_investigator;

drop table if exists ct_ncats_grant_keyword;

drop table if exists ct_core_group;

drop table if exists ct_core_group_principal;

drop table if exists ct_core_investigator;

drop table if exists ct_core_journal;

drop table if exists ct_core_keyword;

drop table if exists ct_core_link;

drop table if exists ct_core_mesh;

drop table if exists ct_core_organization;

drop table if exists ct_core_payload;

drop table if exists ct_core_payload_property;

drop table if exists ct_core_principal;

drop table if exists ct_core_processingstatus;

drop table if exists ct_core_property;

drop table if exists ct_core_publication;

drop table if exists ct_core_publication_keyword;

drop table if exists ct_core_publication_mesh;

drop table if exists ct_core_publication_author;

drop table if exists ct_core_resource;

drop table if exists ct_core_resource_role;

drop table if exists ct_core_resource_acl;

drop table if exists ct_core_role;

drop table if exists ct_core_stitch;

drop table if exists ct_core_stitch_property;

drop table if exists ct_core_vint;

drop table if exists ct_core_vnum;

drop table if exists ct_core_vstr;

drop table if exists ct_core_value;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ct_core_acl_seq;

drop sequence if exists ct_core_author_seq;

drop sequence if exists ct_core_curation_seq;

drop sequence if exists ct_core_etag_seq;

drop sequence if exists ct_core_etagref_seq;

drop sequence if exists ct_core_edit_seq;

drop sequence if exists ct_ncats_funding_seq;

drop sequence if exists ct_ncats_grant_seq;

drop sequence if exists ct_core_group_seq;

drop sequence if exists ct_core_investigator_seq;

drop sequence if exists ct_core_journal_seq;

drop sequence if exists ct_core_keyword_seq;

drop sequence if exists ct_core_link_seq;

drop sequence if exists ct_core_mesh_seq;

drop sequence if exists ct_core_organization_seq;

drop sequence if exists ct_core_payload_seq;

drop sequence if exists ct_core_principal_seq;

drop sequence if exists ct_core_processingstatus_seq;

drop sequence if exists ct_core_property_seq;

drop sequence if exists ct_core_publication_seq;

drop sequence if exists ct_core_resource_seq;

drop sequence if exists ct_core_role_seq;

drop sequence if exists ct_core_stitch_seq;

drop sequence if exists ct_core_vint_seq;

drop sequence if exists ct_core_vnum_seq;

drop sequence if exists ct_core_vstr_seq;

drop sequence if exists ct_core_value_seq;

