# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_core_attribute (
  id                        bigint not null,
  name                      varchar(255),
  value                     varchar(1024),
  namespace_id              bigint,
  constraint pk_ix_core_attribute primary key (id))
;

create table ix_core_curation (
  id                        bigint not null,
  curator_id                bigint,
  status                    integer,
  timestamp                 timestamp,
  constraint ck_ix_core_curation_status check (status in (0,1,2,3)),
  constraint pk_ix_core_curation primary key (id))
;

create table disease (
  disease_name              integer,
  constraint ck_disease_disease_name check (disease_name in (0,1,2,3)))
;

create table ix_core_etag (
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  etag                      varchar(16),
  uri                       varchar(4000),
  path                      varchar(255),
  method                    varchar(10),
  sha1                      varchar(40),
  total                     integer,
  count                     integer,
  skip                      integer,
  top                       integer,
  status                    integer,
  query                     varchar(2048),
  filter                    varchar(4000),
  version                   bigint not null,
  constraint uq_ix_core_etag_etag unique (etag),
  constraint pk_ix_core_etag primary key (id))
;

create table ix_core_etagref (
  id                        bigint not null,
  etag_id                   bigint,
  ref_id                    bigint,
  constraint pk_ix_core_etagref primary key (id))
;

create table ix_core_edit (
  id                        varchar(40) not null,
  created                   timestamp,
  refid                     varchar(255),
  kind                      varchar(255),
  editor_id                 bigint,
  path                      varchar(1024),
  comments                  clob,
  old_value                 clob,
  new_value                 clob,
  constraint pk_ix_core_edit primary key (id))
;

create table ix_core_event (
  id                        bigint not null,
  title                     varchar(1024),
  description               clob,
  url                       varchar(1024),
  event_start               timestamp,
  event_end                 timestamp,
  is_duration               boolean,
  constraint pk_ix_core_event primary key (id))
;

create table ix_core_figure (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  caption                   varchar(255),
  mime_type                 varchar(255),
  url                       varchar(1024),
  data                      blob,
  data_size                 integer,
  sha1                      varchar(140),
  parent_id                 bigint,
  constraint pk_ix_core_figure primary key (id))
;

create table ix_core_group (
  id                        bigint not null,
  name                      varchar(255),
  constraint uq_ix_core_group_name unique (name),
  constraint pk_ix_core_group primary key (id))
;

create table ix_core_investigator (
  id                        bigint not null,
  name                      varchar(255),
  pi_id                     bigint,
  organization_id           bigint,
  role                      integer,
  constraint ck_ix_core_investigator_role check (role in (0,1)),
  constraint pk_ix_core_investigator primary key (id))
;

create table ix_core_journal (
  id                        bigint not null,
  issn                      varchar(10),
  volume                    varchar(255),
  issue                     varchar(255),
  year                      integer,
  month                     varchar(10),
  title                     varchar(1024),
  iso_abbr                  varchar(255),
  factor                    double,
  constraint pk_ix_core_journal primary key (id))
;

create table ix_core_namespace (
  id                        bigint not null,
  name                      varchar(255),
  owner_id                  bigint,
  location                  varchar(1024),
  modifier                  integer,
  constraint ck_ix_core_namespace_modifier check (modifier in (0,1,2)),
  constraint uq_ix_core_namespace_name unique (name),
  constraint pk_ix_core_namespace primary key (id))
;

create table ix_core_organization (
  id                        bigint not null,
  duns                      varchar(10),
  name                      varchar(255),
  department                varchar(255),
  city                      varchar(255),
  state                     varchar(128),
  zipcode                   varchar(64),
  district                  varchar(255),
  country                   varchar(255),
  fips                      varchar(3),
  longitude                 double,
  latitude                  double,
  constraint pk_ix_core_organization primary key (id))
;

create table patient (
  id                        bigint not null,
  created                   timestamp,
  last_modified             timestamp,
  age                       integer,
  gender                    integer,
  condition                 integer,
  country                   integer,
  surgery                   boolean,
  previous_failure          boolean,
  patient_notes             clob,
  constraint ck_patient_age check (age in (0,1,2,3)),
  constraint ck_patient_gender check (gender in (0,1,2)),
  constraint ck_patient_condition check (condition in (0,1,2,3,4,5)),
  constraint ck_patient_country check (country in (0)),
  constraint pk_patient primary key (id))
;

create table ix_core_payload (
  id                        varchar(40) not null,
  namespace_id              bigint,
  created                   timestamp,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime_type                 varchar(128),
  capacity                  bigint,
  constraint pk_ix_core_payload primary key (id))
;

create table ix_core_predicate (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  subject_id                bigint,
  predicate                 varchar(255) not null,
  version                   bigint not null,
  constraint pk_ix_core_predicate primary key (id))
;

create table ix_core_principal (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  provider                  varchar(255),
  username                  varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  selfie_id                 bigint,
  version                   bigint not null,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  prefname                  varchar(255),
  suffix                    varchar(20),
  affiliation               clob,
  orcid                     varchar(255),
  institution_id            bigint,
  constraint pk_ix_core_principal primary key (id))
;

create table ix_core_procjob (
  id                        bigint not null,
  jobkey                    varchar(255) not null,
  invoker                   varchar(255),
  status                    integer,
  job_start                 bigint,
  job_stop                  bigint,
  message                   clob,
  owner_id                  bigint,
  payload_id                varchar(40),
  constraint ck_ix_core_procjob_status check (status in (0,1,2,3,4,5,6)),
  constraint uq_ix_core_procjob_jobkey unique (jobkey),
  constraint pk_ix_core_procjob primary key (id))
;

create table ix_core_procrecord (
  id                        bigint not null,
  rec_start                 bigint,
  rec_stop                  bigint,
  name                      varchar(128),
  status                    integer,
  message                   clob,
  xref_id                   bigint,
  job_id                    bigint,
  constraint ck_ix_core_procrecord_status check (status in (0,1,2,3)),
  constraint pk_ix_core_procrecord primary key (id))
;

create table ix_core_pubauthor (
  id                        bigint not null,
  position                  integer,
  is_last                   boolean,
  correspondence            boolean,
  author_id                 bigint,
  constraint pk_ix_core_pubauthor primary key (id))
;

create table ix_core_publication (
  id                        bigint not null,
  pmid                      bigint,
  pmcid                     varchar(255),
  title                     clob,
  pages                     varchar(255),
  doi                       varchar(255),
  abstract_text             clob,
  journal_id                bigint,
  constraint uq_ix_core_publication_pmid unique (pmid),
  constraint uq_ix_core_publication_pmcid unique (pmcid),
  constraint pk_ix_core_publication primary key (id))
;

create table reference (
  year                      varchar(255),
  pmid                      integer,
  doi                       varchar(255),
  title                     varchar(255),
  url                       varchar(255),
  ref_abstract              clob,
  ref_type                  integer,
  article_type              integer,
  type_of_study             integer,
  aim_of_study              integer,
  treat_or_pre              integer,
  language                  integer,
  full_text_available       boolean,
  full_text_in_repository   boolean,
  constraint ck_reference_ref_type check (ref_type in (0,1)),
  constraint ck_reference_article_type check (article_type in (0,1,2)),
  constraint ck_reference_type_of_study check (type_of_study in (0,1,2,3,4,5)),
  constraint ck_reference_aim_of_study check (aim_of_study in (0,1,2)),
  constraint ck_reference_treat_or_pre check (treat_or_pre in (0,1,2)),
  constraint ck_reference_language check (language in ()))
;

create table ix_core_role (
  id                        bigint not null,
  role                      integer,
  principal_id              bigint,
  constraint ck_ix_core_role_role check (role in (0,1,2,3)),
  constraint pk_ix_core_role primary key (id))
;

create table ix_core_stitch (
  id                        bigint not null,
  name                      varchar(255),
  impl                      varchar(1024),
  description               clob,
  constraint pk_ix_core_stitch primary key (id))
;

create table ix_core_structure (
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  digest                    varchar(128),
  molfile                   clob,
  smiles                    clob,
  formula                   varchar(255),
  stereo                    integer,
  optical                   integer,
  atropi                    integer,
  stereo_comments           clob,
  stereo_centers            integer,
  defined_stereo            integer,
  ez_centers                integer,
  charge                    integer,
  mwt                       double,
  version                   bigint not null,
  constraint ck_ix_core_structure_stereo check (stereo in (0,1,2,3,4,5)),
  constraint ck_ix_core_structure_optical check (optical in (0,1,2,3,4)),
  constraint ck_ix_core_structure_atropi check (atropi in (0,1,2)),
  constraint pk_ix_core_structure primary key (id))
;

create table treatment (
  treatment_name            varchar(255),
  dose                      integer,
  treatment_duration        integer,
  frequency                 integer,
  regimen_id                integer,
  dosage_unit               integer,
  frequency_unit            integer,
  duration_unit             integer,
  route                     integer,
  treatment_notes           clob,
  constraint ck_treatment_dosage_unit check (dosage_unit in (0,1,2,3,4,5,6,7)),
  constraint ck_treatment_frequency_unit check (frequency_unit in (0,1,2,3,4,5)),
  constraint ck_treatment_duration_unit check (duration_unit in (0,1,2,3,4,5)),
  constraint ck_treatment_route check (route in (0,1,2,3,4,5,6)))
;

create table ix_core_value (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  label                     varchar(255),
  text                      clob,
  numval                    double,
  unit                      varchar(255),
  data                      blob,
  data_size                 integer,
  sha1                      varchar(40),
  mime_type                 varchar(32),
  term                      varchar(255),
  href                      clob,
  lval                      double,
  rval                      double,
  average                   double,
  strval                    varchar(1024),
  intval                    bigint,
  major_topic               boolean,
  heading                   varchar(1024),
  constraint pk_ix_core_value primary key (id))
;

create table ix_core_xref (
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  refid                     varchar(40) not null,
  kind                      varchar(255) not null,
  version                   bigint not null,
  constraint pk_ix_core_xref primary key (id))
;


create table ix_core_acl_principal (
  ix_core_acl_id                 bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_core_acl_principal primary key (ix_core_acl_id, ix_core_principal_id))
;

create table ix_core_acl_group (
  ix_core_acl_id                 bigint not null,
  ix_core_group_id               bigint not null,
  constraint pk_ix_core_acl_group primary key (ix_core_acl_id, ix_core_group_id))
;

create table ix_core_event_figure (
  ix_core_event_id               bigint not null,
  ix_core_figure_id              bigint not null,
  constraint pk_ix_core_event_figure primary key (ix_core_event_id, ix_core_figure_id))
;

create table ix_core_group_principal (
  ix_core_group_id               bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_core_group_principal primary key (ix_core_group_id, ix_core_principal_id))
;

create table ix_core_payload_property (
  ix_core_payload_id             varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_payload_property primary key (ix_core_payload_id, ix_core_value_id))
;

create table ix_core_predicate_object (
  ix_core_predicate_id           bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_core_predicate_object primary key (ix_core_predicate_id, ix_core_xref_id))
;

create table ix_core_predicate_property (
  ix_core_predicate_id           bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_predicate_property primary key (ix_core_predicate_id, ix_core_value_id))
;

create table ix_core_publication_keyword (
  ix_core_publication_id         bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_publication_keyword primary key (ix_core_publication_id, ix_core_value_id))
;

create table ix_core_publication_mesh (
  ix_core_publication_id         bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_publication_mesh primary key (ix_core_publication_id, ix_core_value_id))
;

create table ix_core_publication_author (
  ix_core_publication_id         bigint not null,
  ix_core_pubauthor_id           bigint not null,
  constraint pk_ix_core_publication_author primary key (ix_core_publication_id, ix_core_pubauthor_id))
;

create table ix_core_publication_figure (
  ix_core_publication_id         bigint not null,
  ix_core_figure_id              bigint not null,
  constraint pk_ix_core_publication_figure primary key (ix_core_publication_id, ix_core_figure_id))
;

create table ix_core_stitch_attribute (
  ix_core_stitch_id              bigint not null,
  ix_core_attribute_id           bigint not null,
  constraint pk_ix_core_stitch_attribute primary key (ix_core_stitch_id, ix_core_attribute_id))
;

create table ix_core_structure_property (
  ix_core_structure_id           bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_structure_property primary key (ix_core_structure_id, ix_core_value_id))
;

create table ix_core_structure_link (
  ix_core_structure_id           bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_core_structure_link primary key (ix_core_structure_id, ix_core_xref_id))
;

create table ix_core_xref_property (
  ix_core_xref_id                bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_xref_property primary key (ix_core_xref_id, ix_core_value_id))
;
create sequence ix_core_acl_seq;

create sequence ix_core_attribute_seq;

create sequence ix_core_curation_seq;

create sequence ix_core_etag_seq;

create sequence ix_core_etagref_seq;

create sequence ix_core_event_seq;

create sequence ix_core_figure_seq;

create sequence ix_core_group_seq;

create sequence ix_core_investigator_seq;

create sequence ix_core_journal_seq;

create sequence ix_core_namespace_seq;

create sequence ix_core_organization_seq;

create sequence patient_seq;

create sequence ix_core_predicate_seq;

create sequence ix_core_principal_seq;

create sequence ix_core_procjob_seq;

create sequence ix_core_procrecord_seq;

create sequence ix_core_pubauthor_seq;

create sequence ix_core_publication_seq;

create sequence ix_core_role_seq;

create sequence ix_core_stitch_seq;

create sequence ix_core_structure_seq;

create sequence ix_core_value_seq;

create sequence ix_core_xref_seq;

alter table ix_core_attribute add constraint fk_ix_core_attribute_namespace_1 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_namespace_1 on ix_core_attribute (namespace_id);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_2 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_2 on ix_core_curation (curator_id);
alter table ix_core_etag add constraint fk_ix_core_etag_namespace_3 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_etag_namespace_3 on ix_core_etag (namespace_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_4 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_4 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint fk_ix_core_edit_editor_5 foreign key (editor_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_edit_editor_5 on ix_core_edit (editor_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_6 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_6 on ix_core_figure (parent_id);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organi_7 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organi_7 on ix_core_investigator (organization_id);
alter table ix_core_namespace add constraint fk_ix_core_namespace_owner_8 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_namespace_owner_8 on ix_core_namespace (owner_id);
alter table ix_core_payload add constraint fk_ix_core_payload_namespace_9 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_payload_namespace_9 on ix_core_payload (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_namespac_10 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_namespac_10 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_subject_11 foreign key (subject_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_subject_11 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint fk_ix_core_principal_namespac_12 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_principal_namespac_12 on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_13 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_13 on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint fk_ix_core_principal_institut_14 foreign key (institution_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_principal_institut_14 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_owner_15 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_owner_15 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_payload_16 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_payload_16 on ix_core_procjob (payload_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_xref_17 foreign key (xref_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_xref_17 on ix_core_procrecord (xref_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_job_18 foreign key (job_id) references ix_core_procjob (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_job_18 on ix_core_procrecord (job_id);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_19 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_19 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journa_20 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journa_20 on ix_core_publication (journal_id);
alter table ix_core_role add constraint fk_ix_core_role_principal_21 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_role_principal_21 on ix_core_role (principal_id);
alter table ix_core_structure add constraint fk_ix_core_structure_namespac_22 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_structure_namespac_22 on ix_core_structure (namespace_id);
alter table ix_core_xref add constraint fk_ix_core_xref_namespace_23 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_xref_namespace_23 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_i_01 foreign key (ix_core_payload_id) references ix_core_payload (id) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_i_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keywor_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keywor_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_i_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_i_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_02 foreign key (ix_core_pubauthor_id) references ix_core_pubauthor (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_i_01 foreign key (ix_core_stitch_id) references ix_core_stitch (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_i_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix__01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix__02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_01 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ix_core_acl;

drop table if exists ix_core_acl_principal;

drop table if exists ix_core_acl_group;

drop table if exists ix_core_attribute;

drop table if exists ix_core_curation;

drop table if exists disease;

drop table if exists ix_core_etag;

drop table if exists ix_core_etagref;

drop table if exists ix_core_edit;

drop table if exists ix_core_event;

drop table if exists ix_core_event_figure;

drop table if exists ix_core_figure;

drop table if exists ix_core_group;

drop table if exists ix_core_group_principal;

drop table if exists ix_core_investigator;

drop table if exists ix_core_journal;

drop table if exists ix_core_namespace;

drop table if exists ix_core_organization;

drop table if exists patient;

drop table if exists ix_core_payload;

drop table if exists ix_core_payload_property;

drop table if exists ix_core_predicate;

drop table if exists ix_core_predicate_object;

drop table if exists ix_core_predicate_property;

drop table if exists ix_core_principal;

drop table if exists ix_core_procjob;

drop table if exists ix_core_procrecord;

drop table if exists ix_core_pubauthor;

drop table if exists ix_core_publication;

drop table if exists ix_core_publication_keyword;

drop table if exists ix_core_publication_mesh;

drop table if exists ix_core_publication_author;

drop table if exists ix_core_publication_figure;

drop table if exists reference;

drop table if exists ix_core_role;

drop table if exists ix_core_stitch;

drop table if exists ix_core_stitch_attribute;

drop table if exists ix_core_structure;

drop table if exists ix_core_structure_property;

drop table if exists ix_core_structure_link;

drop table if exists treatment;

drop table if exists ix_core_value;

drop table if exists ix_core_xref;

drop table if exists ix_core_xref_property;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ix_core_acl_seq;

drop sequence if exists ix_core_attribute_seq;

drop sequence if exists ix_core_curation_seq;

drop sequence if exists ix_core_etag_seq;

drop sequence if exists ix_core_etagref_seq;

drop sequence if exists ix_core_event_seq;

drop sequence if exists ix_core_figure_seq;

drop sequence if exists ix_core_group_seq;

drop sequence if exists ix_core_investigator_seq;

drop sequence if exists ix_core_journal_seq;

drop sequence if exists ix_core_namespace_seq;

drop sequence if exists ix_core_organization_seq;

drop sequence if exists patient_seq;

drop sequence if exists ix_core_predicate_seq;

drop sequence if exists ix_core_principal_seq;

drop sequence if exists ix_core_procjob_seq;

drop sequence if exists ix_core_procrecord_seq;

drop sequence if exists ix_core_pubauthor_seq;

drop sequence if exists ix_core_publication_seq;

drop sequence if exists ix_core_role_seq;

drop sequence if exists ix_core_stitch_seq;

drop sequence if exists ix_core_structure_seq;

drop sequence if exists ix_core_value_seq;

drop sequence if exists ix_core_xref_seq;

