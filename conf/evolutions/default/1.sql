# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ct_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ct_acl_perm check (perm in (0,1,2,3,4,5)),
  constraint pk_ct_acl primary key (id))
;

create table ct_author (
  id                        bigint not null,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  affiliation               varchar(255),
  orcid                     varchar(255),
  email                     varchar(255),
  url                       varchar(255),
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

create table ct_etag_ref (
  id                        bigint not null,
  etag_etag                 varchar(16),
  ref_id                    bigint,
  constraint pk_ct_etag_ref primary key (id))
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

create table ct_group (
  id                        bigint not null,
  name                      varchar(255),
  constraint uq_ct_group_name unique (name),
  constraint pk_ct_group primary key (id))
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

create table ct_organization (
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
  constraint pk_ct_organization primary key (id))
;

create table ct_payload (
  id                        bigint not null,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime                      varchar(128),
  size                      bigint,
  created                   timestamp,
  constraint pk_ct_payload primary key (id))
;

create table ct_principal (
  id                        bigint not null,
  name                      varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  pkey                      varchar(256),
  constraint uq_ct_principal_pkey unique (pkey),
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

create table ct_property (
  id                        bigint not null,
  payload_id                bigint not null,
  name                      varchar(255),
  type                      varchar(255),
  constraint pk_ct_property primary key (id))
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

create table ct_resource (
  id                        bigint not null,
  name                      varchar(255),
  modifier                  integer,
  constraint ck_ct_resource_modifier check (modifier in (0,1,2)),
  constraint uq_ct_resource_name unique (name),
  constraint pk_ct_resource primary key (id))
;

create table ct_role (
  id                        bigint not null,
  role                      integer,
  principal_id              bigint,
  constraint ck_ct_role_role check (role in (0,1,2,3)),
  constraint pk_ct_role primary key (id))
;

create table ct_stitch (
  id                        bigint not null,
  name                      varchar(255),
  impl                      varchar(1024),
  description               clob,
  constraint pk_ct_stitch primary key (id))
;

create table ct_vint (
  id                        bigint not null,
  label                     varchar(255) not null,
  property_id               bigint,
  value                     bigint,
  constraint pk_ct_vint primary key (id))
;

create table ct_vnum (
  id                        bigint not null,
  label                     varchar(255) not null,
  property_id               bigint,
  value                     double,
  constraint pk_ct_vnum primary key (id))
;

create table ct_vstr (
  id                        bigint not null,
  label                     varchar(255) not null,
  property_id               bigint,
  value                     varchar(1024),
  constraint pk_ct_vstr primary key (id))
;

create table ct_value (
  id                        bigint not null,
  label                     varchar(255) not null,
  property_id               bigint,
  constraint pk_ct_value primary key (id))
;


create table ct_acl_principal (
  ct_acl_id                      bigint not null,
  ct_principal_id                bigint not null,
  constraint pk_ct_acl_principal primary key (ct_acl_id, ct_principal_id))
;

create table ct_acl_group (
  ct_acl_id                      bigint not null,
  ct_group_id                    bigint not null,
  constraint pk_ct_acl_group primary key (ct_acl_id, ct_group_id))
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

create table ct_group_principal (
  ct_group_id                    bigint not null,
  ct_principal_id                bigint not null,
  constraint pk_ct_group_principal primary key (ct_group_id, ct_principal_id))
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

create table ct_resource_role (
  ct_resource_id                 bigint not null,
  ct_role_id                     bigint not null,
  constraint pk_ct_resource_role primary key (ct_resource_id, ct_role_id))
;

create table ct_resource_acl (
  ct_resource_id                 bigint not null,
  ct_acl_id                      bigint not null,
  constraint pk_ct_resource_acl primary key (ct_resource_id, ct_acl_id))
;

create table ct_stitch_property (
  ct_stitch_id                   bigint not null,
  ct_property_id                 bigint not null,
  constraint pk_ct_stitch_property primary key (ct_stitch_id, ct_property_id))
;
create sequence ct_acl_seq;

create sequence ct_author_seq;

create sequence ct_etag_seq;

create sequence ct_etag_ref_seq;

create sequence ct_edit_seq;

create sequence ct_granite_funding_seq;

create sequence ct_granite_grant_seq;

create sequence ct_group_seq;

create sequence ct_granite_investigator_seq;

create sequence ct_journal_seq;

create sequence ct_keyword_seq;

create sequence ct_link_seq;

create sequence ct_mesh_seq;

create sequence ct_organization_seq;

create sequence ct_payload_seq;

create sequence ct_principal_seq;

create sequence ct_processing_status_seq;

create sequence ct_property_seq;

create sequence ct_publication_seq;

create sequence ct_resource_seq;

create sequence ct_role_seq;

create sequence ct_stitch_seq;

create sequence ct_vint_seq;

create sequence ct_vnum_seq;

create sequence ct_vstr_seq;

create sequence ct_value_seq;

alter table ct_etag_ref add constraint fk_ct_etag_ref_etag_1 foreign key (etag_etag) references ct_etag (etag) on delete restrict on update restrict;
create index ix_ct_etag_ref_etag_1 on ct_etag_ref (etag_etag);
alter table ct_edit add constraint fk_ct_edit_principal_2 foreign key (principal_id) references ct_principal (id) on delete restrict on update restrict;
create index ix_ct_edit_principal_2 on ct_edit (principal_id);
alter table ct_granite_funding add constraint fk_ct_granite_funding_ct_grani_3 foreign key (grant_id) references ct_granite_grant (id) on delete restrict on update restrict;
create index ix_ct_granite_funding_ct_grani_3 on ct_granite_funding (grant_id);
alter table ct_granite_investigator add constraint fk_ct_granite_investigator_org_4 foreign key (organization_id) references ct_organization (id) on delete restrict on update restrict;
create index ix_ct_granite_investigator_org_4 on ct_granite_investigator (organization_id);
alter table ct_processing_status add constraint fk_ct_processing_status_payloa_5 foreign key (payload_id) references ct_payload (id) on delete restrict on update restrict;
create index ix_ct_processing_status_payloa_5 on ct_processing_status (payload_id);
alter table ct_property add constraint fk_ct_property_ct_payload_6 foreign key (payload_id) references ct_payload (id) on delete restrict on update restrict;
create index ix_ct_property_ct_payload_6 on ct_property (payload_id);
alter table ct_publication add constraint fk_ct_publication_ct_granite_g_7 foreign key (grant_id) references ct_granite_grant (id) on delete restrict on update restrict;
create index ix_ct_publication_ct_granite_g_7 on ct_publication (grant_id);
alter table ct_publication add constraint fk_ct_publication_journal_8 foreign key (journal_id) references ct_journal (id) on delete restrict on update restrict;
create index ix_ct_publication_journal_8 on ct_publication (journal_id);
alter table ct_role add constraint fk_ct_role_principal_9 foreign key (principal_id) references ct_principal (id) on delete restrict on update restrict;
create index ix_ct_role_principal_9 on ct_role (principal_id);
alter table ct_vint add constraint fk_ct_vint_property_10 foreign key (property_id) references ct_property (id) on delete restrict on update restrict;
create index ix_ct_vint_property_10 on ct_vint (property_id);
alter table ct_vnum add constraint fk_ct_vnum_property_11 foreign key (property_id) references ct_property (id) on delete restrict on update restrict;
create index ix_ct_vnum_property_11 on ct_vnum (property_id);
alter table ct_vstr add constraint fk_ct_vstr_property_12 foreign key (property_id) references ct_property (id) on delete restrict on update restrict;
create index ix_ct_vstr_property_12 on ct_vstr (property_id);
alter table ct_value add constraint fk_ct_value_property_13 foreign key (property_id) references ct_property (id) on delete restrict on update restrict;
create index ix_ct_value_property_13 on ct_value (property_id);



alter table ct_acl_principal add constraint fk_ct_acl_principal_ct_acl_01 foreign key (ct_acl_id) references ct_acl (id) on delete restrict on update restrict;

alter table ct_acl_principal add constraint fk_ct_acl_principal_ct_princi_02 foreign key (ct_principal_id) references ct_principal (id) on delete restrict on update restrict;

alter table ct_acl_group add constraint fk_ct_acl_group_ct_acl_01 foreign key (ct_acl_id) references ct_acl (id) on delete restrict on update restrict;

alter table ct_acl_group add constraint fk_ct_acl_group_ct_group_02 foreign key (ct_group_id) references ct_group (id) on delete restrict on update restrict;

alter table ct_granite_grant_investigator add constraint fk_ct_granite_grant_investiga_01 foreign key (ct_granite_grant_id) references ct_granite_grant (id) on delete restrict on update restrict;

alter table ct_granite_grant_investigator add constraint fk_ct_granite_grant_investiga_02 foreign key (ct_granite_investigator_id) references ct_granite_investigator (id) on delete restrict on update restrict;

alter table ct_granite_grant_keyword add constraint fk_ct_granite_grant_keyword_c_01 foreign key (ct_granite_grant_id) references ct_granite_grant (id) on delete restrict on update restrict;

alter table ct_granite_grant_keyword add constraint fk_ct_granite_grant_keyword_c_02 foreign key (ct_keyword_id) references ct_keyword (id) on delete restrict on update restrict;

alter table ct_group_principal add constraint fk_ct_group_principal_ct_grou_01 foreign key (ct_group_id) references ct_group (id) on delete restrict on update restrict;

alter table ct_group_principal add constraint fk_ct_group_principal_ct_prin_02 foreign key (ct_principal_id) references ct_principal (id) on delete restrict on update restrict;

alter table ct_publication_keyword add constraint fk_ct_publication_keyword_ct__01 foreign key (ct_publication_id) references ct_publication (id) on delete restrict on update restrict;

alter table ct_publication_keyword add constraint fk_ct_publication_keyword_ct__02 foreign key (ct_keyword_id) references ct_keyword (id) on delete restrict on update restrict;

alter table ct_publication_mesh add constraint fk_ct_publication_mesh_ct_pub_01 foreign key (ct_publication_id) references ct_publication (id) on delete restrict on update restrict;

alter table ct_publication_mesh add constraint fk_ct_publication_mesh_ct_mes_02 foreign key (ct_mesh_id) references ct_mesh (id) on delete restrict on update restrict;

alter table ct_publication_author add constraint fk_ct_publication_author_ct_p_01 foreign key (ct_publication_id) references ct_publication (id) on delete restrict on update restrict;

alter table ct_publication_author add constraint fk_ct_publication_author_ct_a_02 foreign key (ct_author_id) references ct_author (id) on delete restrict on update restrict;

alter table ct_resource_role add constraint fk_ct_resource_role_ct_resour_01 foreign key (ct_resource_id) references ct_resource (id) on delete restrict on update restrict;

alter table ct_resource_role add constraint fk_ct_resource_role_ct_role_02 foreign key (ct_role_id) references ct_role (id) on delete restrict on update restrict;

alter table ct_resource_acl add constraint fk_ct_resource_acl_ct_resourc_01 foreign key (ct_resource_id) references ct_resource (id) on delete restrict on update restrict;

alter table ct_resource_acl add constraint fk_ct_resource_acl_ct_acl_02 foreign key (ct_acl_id) references ct_acl (id) on delete restrict on update restrict;

alter table ct_stitch_property add constraint fk_ct_stitch_property_ct_stit_01 foreign key (ct_stitch_id) references ct_stitch (id) on delete restrict on update restrict;

alter table ct_stitch_property add constraint fk_ct_stitch_property_ct_prop_02 foreign key (ct_property_id) references ct_property (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ct_acl;

drop table if exists ct_acl_principal;

drop table if exists ct_acl_group;

drop table if exists ct_author;

drop table if exists ct_etag;

drop table if exists ct_etag_ref;

drop table if exists ct_edit;

drop table if exists ct_granite_funding;

drop table if exists ct_granite_grant;

drop table if exists ct_granite_grant_investigator;

drop table if exists ct_granite_grant_keyword;

drop table if exists ct_group;

drop table if exists ct_group_principal;

drop table if exists ct_granite_investigator;

drop table if exists ct_journal;

drop table if exists ct_keyword;

drop table if exists ct_link;

drop table if exists ct_mesh;

drop table if exists ct_organization;

drop table if exists ct_payload;

drop table if exists ct_principal;

drop table if exists ct_processing_status;

drop table if exists ct_property;

drop table if exists ct_publication;

drop table if exists ct_publication_keyword;

drop table if exists ct_publication_mesh;

drop table if exists ct_publication_author;

drop table if exists ct_resource;

drop table if exists ct_resource_role;

drop table if exists ct_resource_acl;

drop table if exists ct_role;

drop table if exists ct_stitch;

drop table if exists ct_stitch_property;

drop table if exists ct_vint;

drop table if exists ct_vnum;

drop table if exists ct_vstr;

drop table if exists ct_value;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ct_acl_seq;

drop sequence if exists ct_author_seq;

drop sequence if exists ct_etag_seq;

drop sequence if exists ct_etag_ref_seq;

drop sequence if exists ct_edit_seq;

drop sequence if exists ct_granite_funding_seq;

drop sequence if exists ct_granite_grant_seq;

drop sequence if exists ct_group_seq;

drop sequence if exists ct_granite_investigator_seq;

drop sequence if exists ct_journal_seq;

drop sequence if exists ct_keyword_seq;

drop sequence if exists ct_link_seq;

drop sequence if exists ct_mesh_seq;

drop sequence if exists ct_organization_seq;

drop sequence if exists ct_payload_seq;

drop sequence if exists ct_principal_seq;

drop sequence if exists ct_processing_status_seq;

drop sequence if exists ct_property_seq;

drop sequence if exists ct_publication_seq;

drop sequence if exists ct_resource_seq;

drop sequence if exists ct_role_seq;

drop sequence if exists ct_stitch_seq;

drop sequence if exists ct_vint_seq;

drop sequence if exists ct_vnum_seq;

drop sequence if exists ct_vstr_seq;

drop sequence if exists ct_value_seq;

