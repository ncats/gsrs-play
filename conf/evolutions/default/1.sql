# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ct_core_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ct_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ct_core_acl primary key (id))
;

create table ct_core_attribute (
  id                        bigint not null,
  name                      varchar(255),
  type                      varchar(255),
  resource_id               bigint,
  label                     varchar(255),
  constraint pk_ct_core_attribute primary key (id))
;

create table ct_core_author (
  id                        bigint not null,
  username                  varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  pkey                      varchar(256),
  selfie_id                 bigint,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  affiliation               varchar(255),
  orcid                     varchar(20),
  url                       varchar(1024),
  constraint uq_ct_core_author_pkey unique (pkey),
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

create table ct_ncats_employee (
  id                        bigint not null,
  username                  varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  pkey                      varchar(256),
  selfie_id                 bigint,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  affiliation               varchar(255),
  orcid                     varchar(20),
  url                       varchar(1024),
  ncats_employee            boolean,
  ic                        varchar(255),
  dn                        varchar(1024),
  uid                       bigint,
  biography                 clob,
  title                     varchar(255),
  is_lead                   boolean,
  role                      integer,
  constraint ck_ct_ncats_employee_role check (role in (0,1,2)),
  constraint uq_ct_ncats_employee_pkey unique (pkey),
  constraint pk_ct_ncats_employee primary key (id))
;

create table ct_core_event (
  id                        bigint not null,
  title                     varchar(1024),
  description               clob,
  url                       varchar(1024),
  start                     timestamp,
  end                       timestamp,
  is_duration               boolean,
  constraint pk_ct_core_event primary key (id))
;

create table ct_core_figure (
  id                        bigint not null,
  caption                   varchar(255),
  mime_type                 varchar(255),
  url                       varchar(1024),
  data                      blob,
  size                      integer,
  sha1                      varchar(140),
  constraint pk_ct_core_figure primary key (id))
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
  curation_id               bigint,
  term                      varchar(255),
  major_topic               boolean,
  constraint pk_ct_core_mesh primary key (id))
;

create table ct_ncats_author (
  id                        bigint not null,
  username                  varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  pkey                      varchar(256),
  selfie_id                 bigint,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  affiliation               varchar(255),
  orcid                     varchar(20),
  url                       varchar(1024),
  ncats_employee            boolean,
  ic                        varchar(255),
  dn                        varchar(1024),
  uid                       bigint,
  biography                 clob,
  title                     varchar(255),
  constraint uq_ct_ncats_author_pkey unique (pkey),
  constraint pk_ct_ncats_author primary key (id))
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
  username                  varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  pkey                      varchar(256),
  selfie_id                 bigint,
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

create table ct_ncats_project (
  id                        bigint not null,
  title                     varchar(1024),
  objective                 clob,
  scope                     clob,
  opportunities             clob,
  team                      varchar(255),
  acl_id                    bigint,
  curation_id               bigint,
  constraint pk_ct_ncats_project primary key (id))
;

create table ct_core_publication (
  id                        bigint not null,
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
  curation_id               bigint,
  value                     bigint,
  constraint pk_ct_core_vint primary key (id))
;

create table ct_core_vnum (
  id                        bigint not null,
  curation_id               bigint,
  value                     double,
  constraint pk_ct_core_vnum primary key (id))
;

create table ct_core_vstr (
  id                        bigint not null,
  curation_id               bigint,
  value                     varchar(1024),
  constraint pk_ct_core_vstr primary key (id))
;

create table ct_core_value (
  id                        bigint not null,
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

create table ct_core_event_figure (
  ct_core_event_id               bigint not null,
  ct_core_figure_id              bigint not null,
  constraint pk_ct_core_event_figure primary key (ct_core_event_id, ct_core_figure_id))
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

create table ct_ncats_grant_publication (
  ct_ncats_grant_id              bigint not null,
  ct_core_publication_id         bigint not null,
  constraint pk_ct_ncats_grant_publication primary key (ct_ncats_grant_id, ct_core_publication_id))
;

create table ct_core_group_principal (
  ct_core_group_id               bigint not null,
  ct_core_principal_id           bigint not null,
  constraint pk_ct_core_group_principal primary key (ct_core_group_id, ct_core_principal_id))
;

create table ct_core_value_attribute (
  ct_core_keyword_id             bigint not null,
  ct_core_attribute_id           bigint not null,
  constraint pk_ct_core_value_attribute primary key (ct_core_keyword_id, ct_core_attribute_id))
;

create table ct_core_payload_attribute (
  ct_core_payload_id             bigint not null,
  ct_core_attribute_id           bigint not null,
  constraint pk_ct_core_payload_attribute primary key (ct_core_payload_id, ct_core_attribute_id))
;

create table ct_ncats_project_annotation (
  ct_ncats_project_id            bigint not null,
  ct_core_value_id               bigint not null,
  constraint pk_ct_ncats_project_annotation primary key (ct_ncats_project_id, ct_core_value_id))
;

create table ct_ncats_project_member (
  ct_ncats_project_id            bigint not null,
  ct_ncats_employee_id           bigint not null,
  constraint pk_ct_ncats_project_member primary key (ct_ncats_project_id, ct_ncats_employee_id))
;

create table ct_ncats_project_collaborator (
  ct_ncats_project_id            bigint not null,
  ct_core_author_id              bigint not null,
  constraint pk_ct_ncats_project_collaborator primary key (ct_ncats_project_id, ct_core_author_id))
;

create table ct_ncats_project_figure (
  ct_ncats_project_id            bigint not null,
  ct_core_figure_id              bigint not null,
  constraint pk_ct_ncats_project_figure primary key (ct_ncats_project_id, ct_core_figure_id))
;

create table ct_ncats_project_milestone (
  ct_ncats_project_id            bigint not null,
  ct_core_event_id               bigint not null,
  constraint pk_ct_ncats_project_milestone primary key (ct_ncats_project_id, ct_core_event_id))
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

create table ct_core_publication_figure (
  ct_core_publication_id         bigint not null,
  ct_core_figure_id              bigint not null,
  constraint pk_ct_core_publication_figure primary key (ct_core_publication_id, ct_core_figure_id))
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

create table ct_core_stitch_attribute (
  ct_core_stitch_id              bigint not null,
  ct_core_attribute_id           bigint not null,
  constraint pk_ct_core_stitch_attribute primary key (ct_core_stitch_id, ct_core_attribute_id))
;
create sequence ct_core_acl_seq;

create sequence ct_core_attribute_seq;

create sequence ct_core_author_seq;

create sequence ct_core_curation_seq;

create sequence ct_core_etag_seq;

create sequence ct_core_etagref_seq;

create sequence ct_core_edit_seq;

create sequence ct_ncats_employee_seq;

create sequence ct_core_event_seq;

create sequence ct_core_figure_seq;

create sequence ct_ncats_funding_seq;

create sequence ct_ncats_grant_seq;

create sequence ct_core_group_seq;

create sequence ct_core_investigator_seq;

create sequence ct_core_journal_seq;

create sequence ct_core_keyword_seq;

create sequence ct_core_link_seq;

create sequence ct_core_mesh_seq;

create sequence ct_ncats_author_seq;

create sequence ct_core_organization_seq;

create sequence ct_core_payload_seq;

create sequence ct_core_principal_seq;

create sequence ct_core_processingstatus_seq;

create sequence ct_ncats_project_seq;

create sequence ct_core_publication_seq;

create sequence ct_core_resource_seq;

create sequence ct_core_role_seq;

create sequence ct_core_stitch_seq;

create sequence ct_core_vint_seq;

create sequence ct_core_vnum_seq;

create sequence ct_core_vstr_seq;

create sequence ct_core_value_seq;

alter table ct_core_attribute add constraint fk_ct_core_attribute_resource_1 foreign key (resource_id) references ct_core_resource (id) on delete restrict on update restrict;
create index ix_ct_core_attribute_resource_1 on ct_core_attribute (resource_id);
alter table ct_core_author add constraint fk_ct_core_author_selfie_2 foreign key (selfie_id) references ct_core_figure (id) on delete restrict on update restrict;
create index ix_ct_core_author_selfie_2 on ct_core_author (selfie_id);
alter table ct_core_curation add constraint fk_ct_core_curation_curator_3 foreign key (curator_id) references ct_core_principal (id) on delete restrict on update restrict;
create index ix_ct_core_curation_curator_3 on ct_core_curation (curator_id);
alter table ct_core_etagref add constraint fk_ct_core_etagref_etag_4 foreign key (etag_id) references ct_core_etag (id) on delete restrict on update restrict;
create index ix_ct_core_etagref_etag_4 on ct_core_etagref (etag_id);
alter table ct_core_edit add constraint fk_ct_core_edit_principal_5 foreign key (principal_id) references ct_core_principal (id) on delete restrict on update restrict;
create index ix_ct_core_edit_principal_5 on ct_core_edit (principal_id);
alter table ct_ncats_employee add constraint fk_ct_ncats_employee_selfie_6 foreign key (selfie_id) references ct_core_figure (id) on delete restrict on update restrict;
create index ix_ct_ncats_employee_selfie_6 on ct_ncats_employee (selfie_id);
alter table ct_ncats_funding add constraint fk_ct_ncats_funding_ct_ncats_g_7 foreign key (grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;
create index ix_ct_ncats_funding_ct_ncats_g_7 on ct_ncats_funding (grant_id);
alter table ct_core_investigator add constraint fk_ct_core_investigator_organi_8 foreign key (organization_id) references ct_core_organization (id) on delete restrict on update restrict;
create index ix_ct_core_investigator_organi_8 on ct_core_investigator (organization_id);
alter table ct_core_keyword add constraint fk_ct_core_keyword_curation_9 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_keyword_curation_9 on ct_core_keyword (curation_id);
alter table ct_core_mesh add constraint fk_ct_core_mesh_curation_10 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_mesh_curation_10 on ct_core_mesh (curation_id);
alter table ct_ncats_author add constraint fk_ct_ncats_author_selfie_11 foreign key (selfie_id) references ct_core_figure (id) on delete restrict on update restrict;
create index ix_ct_ncats_author_selfie_11 on ct_ncats_author (selfie_id);
alter table ct_core_principal add constraint fk_ct_core_principal_selfie_12 foreign key (selfie_id) references ct_core_figure (id) on delete restrict on update restrict;
create index ix_ct_core_principal_selfie_12 on ct_core_principal (selfie_id);
alter table ct_core_processingstatus add constraint fk_ct_core_processingstatus_p_13 foreign key (payload_id) references ct_core_payload (id) on delete restrict on update restrict;
create index ix_ct_core_processingstatus_p_13 on ct_core_processingstatus (payload_id);
alter table ct_ncats_project add constraint fk_ct_ncats_project_acl_14 foreign key (acl_id) references ct_core_acl (id) on delete restrict on update restrict;
create index ix_ct_ncats_project_acl_14 on ct_ncats_project (acl_id);
alter table ct_ncats_project add constraint fk_ct_ncats_project_curation_15 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_ncats_project_curation_15 on ct_ncats_project (curation_id);
alter table ct_core_publication add constraint fk_ct_core_publication_journa_16 foreign key (journal_id) references ct_core_journal (id) on delete restrict on update restrict;
create index ix_ct_core_publication_journa_16 on ct_core_publication (journal_id);
alter table ct_core_role add constraint fk_ct_core_role_principal_17 foreign key (principal_id) references ct_core_principal (id) on delete restrict on update restrict;
create index ix_ct_core_role_principal_17 on ct_core_role (principal_id);
alter table ct_core_vint add constraint fk_ct_core_vint_curation_18 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_vint_curation_18 on ct_core_vint (curation_id);
alter table ct_core_vnum add constraint fk_ct_core_vnum_curation_19 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_vnum_curation_19 on ct_core_vnum (curation_id);
alter table ct_core_vstr add constraint fk_ct_core_vstr_curation_20 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_vstr_curation_20 on ct_core_vstr (curation_id);
alter table ct_core_value add constraint fk_ct_core_value_curation_21 foreign key (curation_id) references ct_core_curation (id) on delete restrict on update restrict;
create index ix_ct_core_value_curation_21 on ct_core_value (curation_id);



alter table ct_core_acl_principal add constraint fk_ct_core_acl_principal_ct_c_01 foreign key (ct_core_acl_id) references ct_core_acl (id) on delete restrict on update restrict;

alter table ct_core_acl_principal add constraint fk_ct_core_acl_principal_ct_c_02 foreign key (ct_core_principal_id) references ct_core_principal (id) on delete restrict on update restrict;

alter table ct_core_acl_group add constraint fk_ct_core_acl_group_ct_core__01 foreign key (ct_core_acl_id) references ct_core_acl (id) on delete restrict on update restrict;

alter table ct_core_acl_group add constraint fk_ct_core_acl_group_ct_core__02 foreign key (ct_core_group_id) references ct_core_group (id) on delete restrict on update restrict;

alter table ct_core_event_figure add constraint fk_ct_core_event_figure_ct_co_01 foreign key (ct_core_event_id) references ct_core_event (id) on delete restrict on update restrict;

alter table ct_core_event_figure add constraint fk_ct_core_event_figure_ct_co_02 foreign key (ct_core_figure_id) references ct_core_figure (id) on delete restrict on update restrict;

alter table ct_ncats_grant_investigator add constraint fk_ct_ncats_grant_investigato_01 foreign key (ct_ncats_grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;

alter table ct_ncats_grant_investigator add constraint fk_ct_ncats_grant_investigato_02 foreign key (ct_core_investigator_id) references ct_core_investigator (id) on delete restrict on update restrict;

alter table ct_ncats_grant_keyword add constraint fk_ct_ncats_grant_keyword_ct__01 foreign key (ct_ncats_grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;

alter table ct_ncats_grant_keyword add constraint fk_ct_ncats_grant_keyword_ct__02 foreign key (ct_core_keyword_id) references ct_core_keyword (id) on delete restrict on update restrict;

alter table ct_ncats_grant_publication add constraint fk_ct_ncats_grant_publication_01 foreign key (ct_ncats_grant_id) references ct_ncats_grant (id) on delete restrict on update restrict;

alter table ct_ncats_grant_publication add constraint fk_ct_ncats_grant_publication_02 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_group_principal add constraint fk_ct_core_group_principal_ct_01 foreign key (ct_core_group_id) references ct_core_group (id) on delete restrict on update restrict;

alter table ct_core_group_principal add constraint fk_ct_core_group_principal_ct_02 foreign key (ct_core_principal_id) references ct_core_principal (id) on delete restrict on update restrict;

alter table ct_core_value_attribute add constraint fk_ct_core_value_attribute_ct_01 foreign key (ct_core_keyword_id) references ct_core_keyword (id) on delete restrict on update restrict;

alter table ct_core_value_attribute add constraint fk_ct_core_value_attribute_ct_02 foreign key (ct_core_attribute_id) references ct_core_attribute (id) on delete restrict on update restrict;

alter table ct_core_payload_attribute add constraint fk_ct_core_payload_attribute__01 foreign key (ct_core_payload_id) references ct_core_payload (id) on delete restrict on update restrict;

alter table ct_core_payload_attribute add constraint fk_ct_core_payload_attribute__02 foreign key (ct_core_attribute_id) references ct_core_attribute (id) on delete restrict on update restrict;

alter table ct_ncats_project_annotation add constraint fk_ct_ncats_project_annotatio_01 foreign key (ct_ncats_project_id) references ct_ncats_project (id) on delete restrict on update restrict;

alter table ct_ncats_project_annotation add constraint fk_ct_ncats_project_annotatio_02 foreign key (ct_core_value_id) references ct_core_value (id) on delete restrict on update restrict;

alter table ct_ncats_project_member add constraint fk_ct_ncats_project_member_ct_01 foreign key (ct_ncats_project_id) references ct_ncats_project (id) on delete restrict on update restrict;

alter table ct_ncats_project_member add constraint fk_ct_ncats_project_member_ct_02 foreign key (ct_ncats_employee_id) references ct_ncats_employee (id) on delete restrict on update restrict;

alter table ct_ncats_project_collaborator add constraint fk_ct_ncats_project_collabora_01 foreign key (ct_ncats_project_id) references ct_ncats_project (id) on delete restrict on update restrict;

alter table ct_ncats_project_collaborator add constraint fk_ct_ncats_project_collabora_02 foreign key (ct_core_author_id) references ct_core_author (id) on delete restrict on update restrict;

alter table ct_ncats_project_figure add constraint fk_ct_ncats_project_figure_ct_01 foreign key (ct_ncats_project_id) references ct_ncats_project (id) on delete restrict on update restrict;

alter table ct_ncats_project_figure add constraint fk_ct_ncats_project_figure_ct_02 foreign key (ct_core_figure_id) references ct_core_figure (id) on delete restrict on update restrict;

alter table ct_ncats_project_milestone add constraint fk_ct_ncats_project_milestone_01 foreign key (ct_ncats_project_id) references ct_ncats_project (id) on delete restrict on update restrict;

alter table ct_ncats_project_milestone add constraint fk_ct_ncats_project_milestone_02 foreign key (ct_core_event_id) references ct_core_event (id) on delete restrict on update restrict;

alter table ct_core_publication_keyword add constraint fk_ct_core_publication_keywor_01 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_publication_keyword add constraint fk_ct_core_publication_keywor_02 foreign key (ct_core_keyword_id) references ct_core_keyword (id) on delete restrict on update restrict;

alter table ct_core_publication_mesh add constraint fk_ct_core_publication_mesh_c_01 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_publication_mesh add constraint fk_ct_core_publication_mesh_c_02 foreign key (ct_core_mesh_id) references ct_core_mesh (id) on delete restrict on update restrict;

alter table ct_core_publication_author add constraint fk_ct_core_publication_author_01 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_publication_author add constraint fk_ct_core_publication_author_02 foreign key (ct_core_author_id) references ct_core_author (id) on delete restrict on update restrict;

alter table ct_core_publication_figure add constraint fk_ct_core_publication_figure_01 foreign key (ct_core_publication_id) references ct_core_publication (id) on delete restrict on update restrict;

alter table ct_core_publication_figure add constraint fk_ct_core_publication_figure_02 foreign key (ct_core_figure_id) references ct_core_figure (id) on delete restrict on update restrict;

alter table ct_core_resource_role add constraint fk_ct_core_resource_role_ct_c_01 foreign key (ct_core_resource_id) references ct_core_resource (id) on delete restrict on update restrict;

alter table ct_core_resource_role add constraint fk_ct_core_resource_role_ct_c_02 foreign key (ct_core_role_id) references ct_core_role (id) on delete restrict on update restrict;

alter table ct_core_resource_acl add constraint fk_ct_core_resource_acl_ct_co_01 foreign key (ct_core_resource_id) references ct_core_resource (id) on delete restrict on update restrict;

alter table ct_core_resource_acl add constraint fk_ct_core_resource_acl_ct_co_02 foreign key (ct_core_acl_id) references ct_core_acl (id) on delete restrict on update restrict;

alter table ct_core_stitch_attribute add constraint fk_ct_core_stitch_attribute_c_01 foreign key (ct_core_stitch_id) references ct_core_stitch (id) on delete restrict on update restrict;

alter table ct_core_stitch_attribute add constraint fk_ct_core_stitch_attribute_c_02 foreign key (ct_core_attribute_id) references ct_core_attribute (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ct_core_acl;

drop table if exists ct_core_acl_principal;

drop table if exists ct_core_acl_group;

drop table if exists ct_core_attribute;

drop table if exists ct_core_author;

drop table if exists ct_core_curation;

drop table if exists ct_core_etag;

drop table if exists ct_core_etagref;

drop table if exists ct_core_edit;

drop table if exists ct_ncats_employee;

drop table if exists ct_core_event;

drop table if exists ct_core_event_figure;

drop table if exists ct_core_figure;

drop table if exists ct_ncats_funding;

drop table if exists ct_ncats_grant;

drop table if exists ct_ncats_grant_investigator;

drop table if exists ct_ncats_grant_keyword;

drop table if exists ct_ncats_grant_publication;

drop table if exists ct_core_group;

drop table if exists ct_core_group_principal;

drop table if exists ct_core_investigator;

drop table if exists ct_core_journal;

drop table if exists ct_core_keyword;

drop table if exists ct_core_value_attribute;

drop table if exists ct_core_link;

drop table if exists ct_core_mesh;

drop table if exists ct_ncats_author;

drop table if exists ct_core_organization;

drop table if exists ct_core_payload;

drop table if exists ct_core_payload_attribute;

drop table if exists ct_core_principal;

drop table if exists ct_core_processingstatus;

drop table if exists ct_ncats_project;

drop table if exists ct_ncats_project_annotation;

drop table if exists ct_ncats_project_member;

drop table if exists ct_ncats_project_collaborator;

drop table if exists ct_ncats_project_figure;

drop table if exists ct_ncats_project_milestone;

drop table if exists ct_core_publication;

drop table if exists ct_core_publication_keyword;

drop table if exists ct_core_publication_mesh;

drop table if exists ct_core_publication_author;

drop table if exists ct_core_publication_figure;

drop table if exists ct_core_resource;

drop table if exists ct_core_resource_role;

drop table if exists ct_core_resource_acl;

drop table if exists ct_core_role;

drop table if exists ct_core_stitch;

drop table if exists ct_core_stitch_attribute;

drop table if exists ct_core_vint;

drop table if exists ct_core_vnum;

drop table if exists ct_core_vstr;

drop table if exists ct_core_value;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ct_core_acl_seq;

drop sequence if exists ct_core_attribute_seq;

drop sequence if exists ct_core_author_seq;

drop sequence if exists ct_core_curation_seq;

drop sequence if exists ct_core_etag_seq;

drop sequence if exists ct_core_etagref_seq;

drop sequence if exists ct_core_edit_seq;

drop sequence if exists ct_ncats_employee_seq;

drop sequence if exists ct_core_event_seq;

drop sequence if exists ct_core_figure_seq;

drop sequence if exists ct_ncats_funding_seq;

drop sequence if exists ct_ncats_grant_seq;

drop sequence if exists ct_core_group_seq;

drop sequence if exists ct_core_investigator_seq;

drop sequence if exists ct_core_journal_seq;

drop sequence if exists ct_core_keyword_seq;

drop sequence if exists ct_core_link_seq;

drop sequence if exists ct_core_mesh_seq;

drop sequence if exists ct_ncats_author_seq;

drop sequence if exists ct_core_organization_seq;

drop sequence if exists ct_core_payload_seq;

drop sequence if exists ct_core_principal_seq;

drop sequence if exists ct_core_processingstatus_seq;

drop sequence if exists ct_ncats_project_seq;

drop sequence if exists ct_core_publication_seq;

drop sequence if exists ct_core_resource_seq;

drop sequence if exists ct_core_role_seq;

drop sequence if exists ct_core_stitch_seq;

drop sequence if exists ct_core_vint_seq;

drop sequence if exists ct_core_vnum_seq;

drop sequence if exists ct_core_vstr_seq;

drop sequence if exists ct_core_value_seq;

