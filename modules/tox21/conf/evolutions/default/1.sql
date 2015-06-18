# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_qhts_activity (
  id                        bigint not null,
  sample_id                 bigint,
  assay_id                  bigint,
  constraint pk_ix_qhts_activity primary key (id))
;

create table ix_qhts_assay (
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  name                      varchar(1024),
  description               clob,
  version                   bigint not null,
  constraint pk_ix_qhts_assay primary key (id))
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

create table ix_qhts_curve (
  id                        bigint not null,
  conc_id                   bigint,
  response_id               bigint,
  constraint pk_ix_qhts_curve primary key (id))
;

create table ix_qhts_data (
  id                        bigint not null,
  unit                      integer,
  length                    integer,
  data                      blob,
  constraint ck_ix_qhts_data_unit check (unit in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22)),
  constraint pk_ix_qhts_data primary key (id))
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

create table ix_qhts_hillmodel (
  id                        bigint not null,
  ac50                      double,
  hill_coef                 double,
  inf_act                   double,
  zero_act                  double,
  curve_class1              varchar(10),
  curve_class2              varchar(10),
  crc_id                    bigint,
  constraint pk_ix_qhts_hillmodel primary key (id))
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

create table ix_qhts_replicate (
  id                        bigint not null,
  replicate                 integer,
  constraint pk_ix_qhts_replicate primary key (id))
;

create table ix_core_role (
  id                        bigint not null,
  role                      integer,
  principal_id              bigint,
  constraint ck_ix_core_role_role check (role in (0,1,2,3)),
  constraint pk_ix_core_role primary key (id))
;

create table ix_qhts_sample (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  name                      varchar(255),
  structure_id              bigint,
  version                   bigint not null,
  comments                  varchar(1024),
  grade                     integer,
  constraint ck_ix_qhts_sample_grade check (grade in (0,1,2,3,4,5,6,7,8,9,10,11,12,13,14)),
  constraint pk_ix_qhts_sample primary key (id))
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

create table ix_core_value (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  label                     varchar(255),
  text                      clob,
  lval                      double,
  rval                      double,
  average                   double,
  numval                    double,
  unit                      varchar(255),
  data                      blob,
  data_size                 integer,
  sha1                      varchar(40),
  mime_type                 varchar(32),
  term                      varchar(255),
  href                      clob,
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

create table ix_qhts_activity_replicate (
  ix_qhts_activity_id            bigint not null,
  ix_qhts_replicate_id           bigint not null,
  constraint pk_ix_qhts_activity_replicate primary key (ix_qhts_activity_id, ix_qhts_replicate_id))
;

create table ix_qhts_assay_synonym (
  ix_qhts_assay_synonym_id       bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_qhts_assay_synonym primary key (ix_qhts_assay_synonym_id, ix_core_value_id))
;

create table ix_qhts_assay_property (
  ix_qhts_assay_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_qhts_assay_property primary key (ix_qhts_assay_id, ix_core_value_id))
;

create table ix_qhts_assay_link (
  ix_qhts_assay_id               bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_qhts_assay_link primary key (ix_qhts_assay_id, ix_core_xref_id))
;

create table ix_qhts_assay_publication (
  ix_qhts_assay_id               bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_qhts_assay_publication primary key (ix_qhts_assay_id, ix_core_publication_id))
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

create table ix_qhts_hillmodel_property (
  ix_qhts_hillmodel_id           bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_qhts_hillmodel_property primary key (ix_qhts_hillmodel_id, ix_core_value_id))
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

create table ix_qhts_replicate_hill (
  ix_qhts_replicate_id           bigint not null,
  ix_qhts_hillmodel_id           bigint not null,
  constraint pk_ix_qhts_replicate_hill primary key (ix_qhts_replicate_id, ix_qhts_hillmodel_id))
;

create table ix_qhts_replicate_property (
  ix_qhts_replicate_id           bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_qhts_replicate_property primary key (ix_qhts_replicate_id, ix_core_value_id))
;

create table ix_qhts_sample_synonym (
  ix_tox21_sample_synonym_id     bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_qhts_sample_synonym primary key (ix_tox21_sample_synonym_id, ix_core_value_id))
;

create table ix_qhts_sample_property (
  ix_qhts_sample_id              bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_qhts_sample_property primary key (ix_qhts_sample_id, ix_core_value_id))
;

create table ix_qhts_sample_link (
  ix_qhts_sample_id              bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_qhts_sample_link primary key (ix_qhts_sample_id, ix_core_xref_id))
;

create table ix_qhts_sample_publication (
  ix_qhts_sample_id              bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_qhts_sample_publication primary key (ix_qhts_sample_id, ix_core_publication_id))
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

create sequence ix_qhts_activity_seq;

create sequence ix_qhts_assay_seq;

create sequence ix_core_attribute_seq;

create sequence ix_core_curation_seq;

create sequence ix_qhts_curve_seq;

create sequence ix_qhts_data_seq;

create sequence ix_core_etag_seq;

create sequence ix_core_etagref_seq;

create sequence ix_core_event_seq;

create sequence ix_core_figure_seq;

create sequence ix_core_group_seq;

create sequence ix_qhts_hillmodel_seq;

create sequence ix_core_investigator_seq;

create sequence ix_core_journal_seq;

create sequence ix_core_namespace_seq;

create sequence ix_core_organization_seq;

create sequence ix_core_predicate_seq;

create sequence ix_core_principal_seq;

create sequence ix_core_procjob_seq;

create sequence ix_core_procrecord_seq;

create sequence ix_core_pubauthor_seq;

create sequence ix_core_publication_seq;

create sequence ix_qhts_replicate_seq;

create sequence ix_core_role_seq;

create sequence ix_qhts_sample_seq;

create sequence ix_core_stitch_seq;

create sequence ix_core_structure_seq;

create sequence ix_core_value_seq;

create sequence ix_core_xref_seq;

alter table ix_qhts_activity add constraint fk_ix_qhts_activity_sample_1 foreign key (sample_id) references ix_qhts_sample (id) on delete restrict on update restrict;
create index ix_ix_qhts_activity_sample_1 on ix_qhts_activity (sample_id);
alter table ix_qhts_activity add constraint fk_ix_qhts_activity_assay_2 foreign key (assay_id) references ix_qhts_assay (id) on delete restrict on update restrict;
create index ix_ix_qhts_activity_assay_2 on ix_qhts_activity (assay_id);
alter table ix_qhts_assay add constraint fk_ix_qhts_assay_namespace_3 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_qhts_assay_namespace_3 on ix_qhts_assay (namespace_id);
alter table ix_core_attribute add constraint fk_ix_core_attribute_namespace_4 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_namespace_4 on ix_core_attribute (namespace_id);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_5 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_5 on ix_core_curation (curator_id);
alter table ix_qhts_curve add constraint fk_ix_qhts_curve_conc_6 foreign key (conc_id) references ix_qhts_data (id) on delete restrict on update restrict;
create index ix_ix_qhts_curve_conc_6 on ix_qhts_curve (conc_id);
alter table ix_qhts_curve add constraint fk_ix_qhts_curve_response_7 foreign key (response_id) references ix_qhts_data (id) on delete restrict on update restrict;
create index ix_ix_qhts_curve_response_7 on ix_qhts_curve (response_id);
alter table ix_core_etag add constraint fk_ix_core_etag_namespace_8 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_etag_namespace_8 on ix_core_etag (namespace_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_9 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_9 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint fk_ix_core_edit_editor_10 foreign key (editor_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_edit_editor_10 on ix_core_edit (editor_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_11 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_11 on ix_core_figure (parent_id);
alter table ix_qhts_hillmodel add constraint fk_ix_qhts_hillmodel_crc_12 foreign key (crc_id) references ix_qhts_curve (id) on delete restrict on update restrict;
create index ix_ix_qhts_hillmodel_crc_12 on ix_qhts_hillmodel (crc_id);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organ_13 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organ_13 on ix_core_investigator (organization_id);
alter table ix_core_namespace add constraint fk_ix_core_namespace_owner_14 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_namespace_owner_14 on ix_core_namespace (owner_id);
alter table ix_core_payload add constraint fk_ix_core_payload_namespace_15 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_payload_namespace_15 on ix_core_payload (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_namespac_16 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_namespac_16 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_subject_17 foreign key (subject_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_subject_17 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint fk_ix_core_principal_namespac_18 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_principal_namespac_18 on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_19 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_19 on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint fk_ix_core_principal_institut_20 foreign key (institution_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_principal_institut_20 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_owner_21 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_owner_21 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_payload_22 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_payload_22 on ix_core_procjob (payload_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_xref_23 foreign key (xref_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_xref_23 on ix_core_procrecord (xref_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_job_24 foreign key (job_id) references ix_core_procjob (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_job_24 on ix_core_procrecord (job_id);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_25 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_25 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journa_26 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journa_26 on ix_core_publication (journal_id);
alter table ix_core_role add constraint fk_ix_core_role_principal_27 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_role_principal_27 on ix_core_role (principal_id);
alter table ix_qhts_sample add constraint fk_ix_qhts_sample_namespace_28 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_qhts_sample_namespace_28 on ix_qhts_sample (namespace_id);
alter table ix_qhts_sample add constraint fk_ix_qhts_sample_structure_29 foreign key (structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_qhts_sample_structure_29 on ix_qhts_sample (structure_id);
alter table ix_core_structure add constraint fk_ix_core_structure_namespac_30 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_structure_namespac_30 on ix_core_structure (namespace_id);
alter table ix_core_xref add constraint fk_ix_core_xref_namespace_31 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_xref_namespace_31 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_qhts_activity_replicate add constraint fk_ix_qhts_activity_replicate_01 foreign key (ix_qhts_activity_id) references ix_qhts_activity (id) on delete restrict on update restrict;

alter table ix_qhts_activity_replicate add constraint fk_ix_qhts_activity_replicate_02 foreign key (ix_qhts_replicate_id) references ix_qhts_replicate (id) on delete restrict on update restrict;

alter table ix_qhts_assay_synonym add constraint fk_ix_qhts_assay_synonym_ix_q_01 foreign key (ix_qhts_assay_synonym_id) references ix_qhts_assay (id) on delete restrict on update restrict;

alter table ix_qhts_assay_synonym add constraint fk_ix_qhts_assay_synonym_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_qhts_assay_property add constraint fk_ix_qhts_assay_property_ix__01 foreign key (ix_qhts_assay_id) references ix_qhts_assay (id) on delete restrict on update restrict;

alter table ix_qhts_assay_property add constraint fk_ix_qhts_assay_property_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_qhts_assay_link add constraint fk_ix_qhts_assay_link_ix_qhts_01 foreign key (ix_qhts_assay_id) references ix_qhts_assay (id) on delete restrict on update restrict;

alter table ix_qhts_assay_link add constraint fk_ix_qhts_assay_link_ix_core_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_qhts_assay_publication add constraint fk_ix_qhts_assay_publication__01 foreign key (ix_qhts_assay_id) references ix_qhts_assay (id) on delete restrict on update restrict;

alter table ix_qhts_assay_publication add constraint fk_ix_qhts_assay_publication__02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_qhts_hillmodel_property add constraint fk_ix_qhts_hillmodel_property_01 foreign key (ix_qhts_hillmodel_id) references ix_qhts_hillmodel (id) on delete restrict on update restrict;

alter table ix_qhts_hillmodel_property add constraint fk_ix_qhts_hillmodel_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

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

alter table ix_qhts_replicate_hill add constraint fk_ix_qhts_replicate_hill_ix__01 foreign key (ix_qhts_replicate_id) references ix_qhts_replicate (id) on delete restrict on update restrict;

alter table ix_qhts_replicate_hill add constraint fk_ix_qhts_replicate_hill_ix__02 foreign key (ix_qhts_hillmodel_id) references ix_qhts_hillmodel (id) on delete restrict on update restrict;

alter table ix_qhts_replicate_property add constraint fk_ix_qhts_replicate_property_01 foreign key (ix_qhts_replicate_id) references ix_qhts_replicate (id) on delete restrict on update restrict;

alter table ix_qhts_replicate_property add constraint fk_ix_qhts_replicate_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_qhts_sample_synonym add constraint fk_ix_qhts_sample_synonym_ix__01 foreign key (ix_tox21_sample_synonym_id) references ix_qhts_sample (id) on delete restrict on update restrict;

alter table ix_qhts_sample_synonym add constraint fk_ix_qhts_sample_synonym_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_qhts_sample_property add constraint fk_ix_qhts_sample_property_ix_01 foreign key (ix_qhts_sample_id) references ix_qhts_sample (id) on delete restrict on update restrict;

alter table ix_qhts_sample_property add constraint fk_ix_qhts_sample_property_ix_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_qhts_sample_link add constraint fk_ix_qhts_sample_link_ix_qht_01 foreign key (ix_qhts_sample_id) references ix_qhts_sample (id) on delete restrict on update restrict;

alter table ix_qhts_sample_link add constraint fk_ix_qhts_sample_link_ix_cor_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_qhts_sample_publication add constraint fk_ix_qhts_sample_publication_01 foreign key (ix_qhts_sample_id) references ix_qhts_sample (id) on delete restrict on update restrict;

alter table ix_qhts_sample_publication add constraint fk_ix_qhts_sample_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

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

drop table if exists ix_qhts_activity;

drop table if exists ix_qhts_activity_replicate;

drop table if exists ix_qhts_assay;

drop table if exists ix_qhts_assay_synonym;

drop table if exists ix_qhts_assay_property;

drop table if exists ix_qhts_assay_link;

drop table if exists ix_qhts_assay_publication;

drop table if exists ix_core_attribute;

drop table if exists ix_core_curation;

drop table if exists ix_qhts_curve;

drop table if exists ix_qhts_data;

drop table if exists ix_core_etag;

drop table if exists ix_core_etagref;

drop table if exists ix_core_edit;

drop table if exists ix_core_event;

drop table if exists ix_core_event_figure;

drop table if exists ix_core_figure;

drop table if exists ix_core_group;

drop table if exists ix_core_group_principal;

drop table if exists ix_qhts_hillmodel;

drop table if exists ix_qhts_hillmodel_property;

drop table if exists ix_core_investigator;

drop table if exists ix_core_journal;

drop table if exists ix_core_namespace;

drop table if exists ix_core_organization;

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

drop table if exists ix_qhts_replicate;

drop table if exists ix_qhts_replicate_hill;

drop table if exists ix_qhts_replicate_property;

drop table if exists ix_core_role;

drop table if exists ix_qhts_sample;

drop table if exists ix_qhts_sample_synonym;

drop table if exists ix_qhts_sample_property;

drop table if exists ix_qhts_sample_link;

drop table if exists ix_qhts_sample_publication;

drop table if exists ix_core_stitch;

drop table if exists ix_core_stitch_attribute;

drop table if exists ix_core_structure;

drop table if exists ix_core_structure_property;

drop table if exists ix_core_structure_link;

drop table if exists ix_core_value;

drop table if exists ix_core_xref;

drop table if exists ix_core_xref_property;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ix_core_acl_seq;

drop sequence if exists ix_qhts_activity_seq;

drop sequence if exists ix_qhts_assay_seq;

drop sequence if exists ix_core_attribute_seq;

drop sequence if exists ix_core_curation_seq;

drop sequence if exists ix_qhts_curve_seq;

drop sequence if exists ix_qhts_data_seq;

drop sequence if exists ix_core_etag_seq;

drop sequence if exists ix_core_etagref_seq;

drop sequence if exists ix_core_event_seq;

drop sequence if exists ix_core_figure_seq;

drop sequence if exists ix_core_group_seq;

drop sequence if exists ix_qhts_hillmodel_seq;

drop sequence if exists ix_core_investigator_seq;

drop sequence if exists ix_core_journal_seq;

drop sequence if exists ix_core_namespace_seq;

drop sequence if exists ix_core_organization_seq;

drop sequence if exists ix_core_predicate_seq;

drop sequence if exists ix_core_principal_seq;

drop sequence if exists ix_core_procjob_seq;

drop sequence if exists ix_core_procrecord_seq;

drop sequence if exists ix_core_pubauthor_seq;

drop sequence if exists ix_core_publication_seq;

drop sequence if exists ix_qhts_replicate_seq;

drop sequence if exists ix_core_role_seq;

drop sequence if exists ix_qhts_sample_seq;

drop sequence if exists ix_core_stitch_seq;

drop sequence if exists ix_core_structure_seq;

drop sequence if exists ix_core_value_seq;

drop sequence if exists ix_core_xref_seq;

