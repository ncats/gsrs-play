# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_ginas_agentmod (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  agent_modification_process varchar(255),
  agent_modification_role   varchar(255),
  agent_modification_type   varchar(255),
  agent_substance_uuid      varchar(40),
  amount_uuid               varchar(40),
  modification_group        varchar(255),
  internal_version          bigint not null,
  constraint pk_ix_ginas_agentmod primary key (uuid))
;

create table ix_ginas_amount (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  type                      varchar(255),
  average                   double,
  high_limit                double,
  high                      double,
  low_limit                 double,
  low                       double,
  units                     varchar(255),
  non_numeric_value         varchar(255),
  approval_id               varchar(10),
  internal_version          bigint not null,
  constraint pk_ix_ginas_amount primary key (uuid))
;

create table ix_core_attribute (
  id                        bigint not null,
  name                      varchar(255),
  value                     varchar(1024),
  namespace_id              bigint,
  constraint pk_ix_core_attribute primary key (id))
;

create table ix_core_backup (
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  refid                     varchar(255),
  kind                      varchar(255),
  data                      blob,
  sha1                      varchar(255),
  compressed                boolean,
  version                   bigint not null,
  constraint uq_ix_core_backup_refid unique (refid),
  constraint pk_ix_core_backup primary key (id))
;

create table ix_ginas_code (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  code_system               varchar(255),
  code                      varchar(255) not null,
  comments                  clob,
  code_text                 clob,
  type                      varchar(255),
  url                       clob,
  internal_version          bigint not null,
  constraint pk_ix_ginas_code primary key (uuid))
;

create table ix_ginas_component (
  dtype                     varchar(10) not null,
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  type                      varchar(255),
  substance_uuid            varchar(40),
  internal_version          bigint not null,
  role                      varchar(255),
  amount_uuid               varchar(40),
  constraint pk_ix_ginas_component primary key (uuid))
;

create table ix_ginas_controlled_vocab (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  domain                    varchar(255),
  vocabulary_term_type      varchar(255),
  editable                  boolean,
  filterable                boolean,
  version                   bigint not null,
  constraint uq_ix_ginas_controlled_vocab_dom unique (domain),
  constraint pk_ix_ginas_controlled_vocab primary key (id))
;

create table ix_core_curation (
  id                        bigint not null,
  curator_id                bigint,
  status                    integer,
  timestamp                 timestamp,
  constraint ck_ix_core_curation_status check (status in (0,1,2,3)),
  constraint pk_ix_core_curation primary key (id))
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
  created                   bigint,
  refid                     varchar(255),
  kind                      varchar(255),
  batch                     varchar(64),
  editor_id                 bigint,
  path                      varchar(1024),
  comments                  clob,
  version                   varchar(255),
  old_value                 clob,
  new_value                 clob,
  constraint pk_ix_core_edit primary key (id))
;

create table ix_core_event (
  id                        bigint not null,
  title                     varchar(255),
  description               clob,
  url                       varchar(1024),
  start_time                bigint,
  end_time                  bigint,
  unit                      integer,
  constraint ck_ix_core_event_unit check (unit in (0,1,2,3,4,5,6,7)),
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

create table ix_core_filedata (
  dtype                     varchar(10) not null,
  id                        varchar(40) not null,
  mime_type                 varchar(255),
  data                      blob,
  data_size                 bigint,
  sha1                      varchar(140),
  constraint pk_ix_core_filedata primary key (id))
;

create table ix_ginas_glycosylation (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  c_glycosylation_sites_uuid varchar(40),
  n_glycosylation_sites_uuid varchar(40),
  o_glycosylation_sites_uuid varchar(40),
  glycosylation_type        varchar(255),
  internal_version          bigint not null,
  constraint pk_ix_ginas_glycosylation primary key (uuid))
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

create table ix_ginas_linkage (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  linkage                   varchar(255),
  site_container_uuid       varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_linkage primary key (uuid))
;

create table ix_ginas_material (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  amount_uuid               varchar(40),
  monomer_substance_uuid    varchar(40),
  type                      varchar(255),
  defining                  boolean,
  internal_version          bigint not null,
  constraint pk_ix_ginas_material primary key (uuid))
;

create table ix_ginas_mixture (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  parent_substance_uuid     varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_mixture primary key (uuid))
;

create table ix_ginas_modifications (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  internal_version          bigint not null,
  constraint pk_ix_ginas_modifications primary key (uuid))
;

create table ix_ginas_moiety (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  structure_id              varchar(40),
  count_uuid                varchar(40),
  inner_uuid                varchar(255),
  internal_version          bigint not null,
  constraint uq_ix_ginas_moiety_inner_uuid unique (inner_uuid),
  constraint pk_ix_ginas_moiety primary key (uuid))
;

create table ix_ginas_name (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  name                      varchar(255) not null,
  full_name                 clob,
  std_name                  clob,
  type                      varchar(32),
  domains                   clob,
  languages                 clob,
  name_jurisdiction         clob,
  preferred                 boolean,
  display_name              boolean,
  internal_version          bigint not null,
  constraint pk_ix_ginas_name primary key (uuid))
;

create table ix_ginas_nameorg (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  name_org                  varchar(255) not null,
  deprecated_date           timestamp,
  internal_version          bigint not null,
  constraint pk_ix_ginas_nameorg primary key (uuid))
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

create table ix_ginas_note (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  note                      clob,
  internal_version          bigint not null,
  constraint pk_ix_ginas_note primary key (uuid))
;

create table ix_ginas_nucleicacid (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  modifications_uuid        varchar(40),
  nucleic_acid_type         varchar(255),
  nucleic_acid_sub_type     varchar(255),
  sequence_origin           varchar(255),
  sequence_type             varchar(255),
  internal_version          bigint not null,
  constraint pk_ix_ginas_nucleicacid primary key (uuid))
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

create table ix_ginas_otherlinks (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  linkage_type              varchar(255),
  site_container_uuid       varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_otherlinks primary key (uuid))
;

create table ix_ginas_parameter (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  referenced_substance_uuid varchar(40),
  name                      varchar(255) not null,
  type                      varchar(255),
  value_uuid                varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_parameter primary key (uuid))
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

create table ix_ginas_physicalmod (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  physical_modification_role varchar(255),
  modification_group        varchar(255),
  internal_version          bigint not null,
  constraint pk_ix_ginas_physicalmod primary key (uuid))
;

create table ix_ginas_physicalpar (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  parameter_name            varchar(255),
  amount_uuid               varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_physicalpar primary key (uuid))
;

create table ix_ginas_polymer (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  classification_uuid       varchar(40),
  display_structure_id      varchar(40),
  idealized_structure_id    varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_polymer primary key (uuid))
;

create table polymer_classification (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  polymer_class             varchar(255),
  polymer_geometry          varchar(255),
  polymer_subclass          clob,
  source_type               varchar(255),
  parent_substance_uuid     varchar(40),
  internal_version          bigint not null,
  constraint pk_polymer_classification primary key (uuid))
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
  is_admin                  boolean,
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
  constraint uq_ix_core_principal_username unique (username),
  constraint pk_ix_core_principal primary key (id))
;

create table ix_core_procjob (
  id                        bigint not null,
  status                    integer,
  job_start                 bigint,
  job_stop                  bigint,
  message                   clob,
  statistics                clob,
  owner_id                  bigint,
  payload_id                varchar(40),
  last_update               timestamp,
  version                   bigint not null,
  constraint ck_ix_core_procjob_status check (status in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_procjob primary key (id))
;

create table ix_core_procrec (
  id                        bigint not null,
  rec_start                 bigint,
  rec_stop                  bigint,
  name                      varchar(128),
  status                    integer,
  message                   clob,
  xref_id                   bigint,
  job_id                    bigint,
  last_update               timestamp not null,
  constraint ck_ix_core_procrec_status check (status in (0,1,2,3,4)),
  constraint pk_ix_core_procrec primary key (id))
;

create table ix_ginas_property (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  name                      varchar(255) not null,
  type                      varchar(255),
  property_type             varchar(255),
  value_uuid                varchar(40),
  referenced_substance_uuid varchar(40),
  defining                  boolean,
  internal_version          bigint not null,
  constraint pk_ix_ginas_property primary key (uuid))
;

create table ix_ginas_protein (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  protein_type              varchar(255),
  protein_sub_type          varchar(255),
  sequence_origin           varchar(255),
  sequence_type             varchar(255),
  disulf_json               clob,
  glycosylation_uuid        varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_protein primary key (uuid))
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

create table ix_ginas_reference (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  owner_uuid                varchar(40),
  citation                  clob,
  doc_type                  varchar(255),
  document_date             timestamp,
  public_domain             boolean,
  tags                      clob,
  uploaded_file             varchar(1024),
  id                        varchar(255),
  url                       clob,
  internal_version          bigint not null,
  constraint pk_ix_ginas_reference primary key (uuid))
;

create table ix_ginas_relationship (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  amount_uuid               varchar(40),
  comments                  clob,
  interaction_type          varchar(255),
  qualification             varchar(255),
  related_substance_uuid    varchar(40),
  mediator_substance_uuid   varchar(40),
  originator_uuid           varchar(255),
  type                      varchar(255),
  internal_version          bigint not null,
  constraint pk_ix_ginas_relationship primary key (uuid))
;

create table ix_core_session (
  id                        varchar(40) not null,
  profile_id                bigint,
  created                   bigint,
  accessed                  bigint,
  location                  varchar(255),
  expired                   boolean,
  constraint pk_ix_core_session primary key (id))
;

create table ix_ginas_site_lob (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  sites_short_hand          clob,
  sites_json                clob,
  site_count                bigint,
  site_type                 varchar(255),
  internal_version          bigint not null,
  constraint pk_ix_ginas_site_lob primary key (uuid))
;

create table ix_ginas_ssg1 (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  internal_version          bigint not null,
  constraint pk_ix_ginas_ssg1 primary key (uuid))
;

create table ix_core_stitch (
  id                        bigint not null,
  name                      varchar(255),
  impl                      varchar(1024),
  description               clob,
  constraint pk_ix_core_stitch primary key (id))
;

create table ix_ginas_structuralmod (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  structural_modification_type varchar(255),
  location_type             varchar(255),
  residue_modified          varchar(255),
  site_container_uuid       varchar(40),
  extent                    varchar(255),
  extent_amount_uuid        varchar(40),
  molecular_fragment_uuid   varchar(40),
  moleculare_fragment_role  varchar(255),
  modification_group        varchar(255),
  internal_version          bigint not null,
  constraint pk_ix_ginas_structuralmod primary key (uuid))
;

create table ix_ginas_strucdiv (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  source_material_class     varchar(255),
  source_material_type      varchar(255),
  source_material_state     varchar(255),
  organism_family           varchar(255),
  organism_genus            varchar(255),
  organism_species          varchar(255),
  organism_author           varchar(255),
  part_location             varchar(255),
  part                      clob,
  infra_specific_type       varchar(255),
  infra_specific_name       varchar(255),
  developmental_stage       varchar(255),
  fraction_name             varchar(255),
  fraction_material_type    varchar(255),
  paternal_uuid             varchar(40),
  maternal_uuid             varchar(40),
  parent_substance_uuid     varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_strucdiv primary key (uuid))
;

create table ix_core_structure (
  dtype                     varchar(10) not null,
  id                        varchar(40) not null,
  created                   timestamp,
  last_edited               timestamp,
  deprecated                boolean,
  digest                    varchar(128),
  molfile                   clob,
  smiles                    clob,
  formula                   varchar(255),
  stereo                    varchar(255),
  optical                   integer,
  atropi                    integer,
  stereo_comments           clob,
  stereo_centers            integer,
  defined_stereo            integer,
  ez_centers                integer,
  charge                    integer,
  mwt                       double,
  count                     integer,
  version                   bigint not null,
  internal_references       clob,
  created_by_id             bigint,
  last_edited_by_id         bigint,
  record_access             varbinary(255),
  constraint ck_ix_core_structure_optical check (optical in (0,1,2,3,4)),
  constraint ck_ix_core_structure_atropi check (atropi in (0,1,2)),
  constraint pk_ix_core_structure primary key (id))
;

create table ix_ginas_substance (
  dtype                     varchar(10) not null,
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  definition_type           integer,
  definition_level          integer,
  class                     integer,
  status                    varchar(255),
  version                   varchar(255),
  approved_by_id            bigint,
  approved                  timestamp,
  change_reason             varchar(255),
  modifications_uuid        varchar(40),
  approval_id               varchar(20),
  internal_version          bigint not null,
  protein_uuid              varchar(40),
  specified_substance_uuid  varchar(40),
  structurally_diverse_uuid varchar(40),
  nucleic_acid_uuid         varchar(40),
  polymer_uuid              varchar(40),
  structure_id              varchar(40),
  mixture_uuid              varchar(40),
  constraint ck_ix_ginas_substance_definition_type check (definition_type in (0,1)),
  constraint ck_ix_ginas_substance_definition_level check (definition_level in (0,1,2,3)),
  constraint ck_ix_ginas_substance_class check (class in (0,1,2,3,4,5,6,7,8,9,10,11,12)),
  constraint pk_ix_ginas_substance primary key (uuid))
;

create table ix_ginas_substanceref (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  ref_pname                 varchar(1024),
  refuuid                   varchar(128),
  substance_class           varchar(255),
  approval_id               varchar(32),
  internal_version          bigint not null,
  constraint pk_ix_ginas_substanceref primary key (uuid))
;

create table ix_ginas_subunit (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  sequence                  clob,
  subunit_index             integer,
  internal_version          bigint not null,
  constraint pk_ix_ginas_subunit primary key (uuid))
;

create table ix_ginas_sugar (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  sugar                     varchar(255),
  site_container_uuid       varchar(40),
  internal_version          bigint not null,
  constraint pk_ix_ginas_sugar primary key (uuid))
;

create table ix_core_timeline (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_ix_core_timeline primary key (id))
;

create table ix_ginas_unit (
  uuid                      varchar(40) not null,
  current_version           integer,
  created                   timestamp,
  created_by_id             bigint,
  last_edited               timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  record_access             varbinary(255),
  internal_references       clob,
  owner_uuid                varchar(40),
  amap_id                   bigint,
  amount_uuid               varchar(40),
  attachment_count          integer,
  label                     varchar(255),
  structure                 clob,
  type                      varchar(255),
  attachmentMap             clob,
  internal_version          bigint not null,
  constraint pk_ix_ginas_unit primary key (uuid))
;

create table ix_core_userprof (
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  user_id                   bigint,
  active                    boolean,
  hashp                     varchar(255),
  salt                      varchar(255),
  system_auth               boolean,
  roles_json                clob,
  apikey                    varchar(255),
  version                   bigint not null,
  constraint pk_ix_core_userprof primary key (id))
;

create table ix_core_value (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  label                     varchar(255),
  lval                      double,
  rval                      double,
  average                   double,
  intval                    bigint,
  numval                    double,
  unit                      varchar(255),
  text                      clob,
  term                      varchar(255),
  href                      clob,
  data                      blob,
  data_size                 integer,
  sha1                      varchar(40),
  mime_type                 varchar(32),
  strval                    varchar(1024),
  major_topic               boolean,
  heading                   varchar(1024),
  constraint pk_ix_core_value primary key (id))
;

create table ix_ginas_vocabulary_term (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  namespace_id              bigint,
  created                   timestamp,
  modified                  timestamp,
  deprecated                boolean,
  owner_id                  bigint,
  value                     varchar(4000),
  display                   varchar(4000),
  description               varchar(4000),
  regex                     varchar(4000),
  origin                    varchar(255),
  filters                   clob,
  hidden                    boolean,
  selected                  boolean,
  version                   bigint not null,
  fragment_structure        varchar(255),
  simplified_structure      varchar(255),
  system_category           varchar(255),
  constraint pk_ix_ginas_vocabulary_term primary key (id))
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

create table ix_ginas_controlled_vocab_core_v (
  ix_ginas_controlled_vocab_id   bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_controlled_vocab_core_v primary key (ix_ginas_controlled_vocab_id, ix_core_value_id))
;

create table ix_core_event_prop (
  ix_core_event_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_event_prop primary key (ix_core_event_id, ix_core_value_id))
;

create table ix_core_event_link (
  ix_core_event_id               bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_core_event_link primary key (ix_core_event_id, ix_core_xref_id))
;

create table ix_core_group_principal (
  ix_core_group_id               bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_core_group_principal primary key (ix_core_group_id, ix_core_principal_id))
;

create table ix_ginas_substance_mix_comp (
  ix_ginas_mixture_uuid          varchar(40) not null,
  ix_ginas_component_uuid        varchar(40) not null,
  constraint pk_ix_ginas_substance_mix_comp primary key (ix_ginas_mixture_uuid, ix_ginas_component_uuid))
;

create table ix_ginas_nucleicacid_subunits (
  ix_ginas_nucleicacid_uuid      varchar(40) not null,
  ix_ginas_subunit_uuid          varchar(40) not null,
  constraint pk_ix_ginas_nucleicacid_subunits primary key (ix_ginas_nucleicacid_uuid, ix_ginas_subunit_uuid))
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

create table ix_core_procjob_key (
  ix_core_procjob_id             bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_procjob_key primary key (ix_core_procjob_id, ix_core_value_id))
;

create table ix_core_procrec_prop (
  ix_core_procrec_id             bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_procrec_prop primary key (ix_core_procrec_id, ix_core_value_id))
;

create table ix_ginas_protein_subunit (
  ix_ginas_protein_uuid          varchar(40) not null,
  ix_ginas_subunit_uuid          varchar(40) not null,
  constraint pk_ix_ginas_protein_subunit primary key (ix_ginas_protein_uuid, ix_ginas_subunit_uuid))
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

create table ix_ginas_substance_ss_comp (
  ix_ginas_ssg1_uuid             varchar(40) not null,
  ix_ginas_component_uuid        varchar(40) not null,
  constraint pk_ix_ginas_substance_ss_comp primary key (ix_ginas_ssg1_uuid, ix_ginas_component_uuid))
;

create table ix_core_stitch_attribute (
  ix_core_stitch_id              bigint not null,
  ix_core_attribute_id           bigint not null,
  constraint pk_ix_core_stitch_attribute primary key (ix_core_stitch_id, ix_core_attribute_id))
;

create table ix_core_structure_property (
  ix_core_structure_id           varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_structure_property primary key (ix_core_structure_id, ix_core_value_id))
;

create table ix_core_structure_link (
  ix_core_structure_id           varchar(40) not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_core_structure_link primary key (ix_core_structure_id, ix_core_xref_id))
;

create table ix_ginas_substance_tags (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_substance_tags primary key (ix_ginas_substance_uuid, ix_core_value_id))
;

create table ix_core_timeline_event (
  ix_core_timeline_id            bigint not null,
  ix_core_event_id               bigint not null,
  constraint pk_ix_core_timeline_event primary key (ix_core_timeline_id, ix_core_event_id))
;

create table ix_core_userprof_prop (
  ix_core_userprof_id            bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_userprof_prop primary key (ix_core_userprof_id, ix_core_value_id))
;

create table ix_core_xref_property (
  ix_core_xref_id                bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_xref_property primary key (ix_core_xref_id, ix_core_value_id))
;
create sequence ix_core_acl_seq;

create sequence ix_core_attribute_seq;

create sequence ix_core_backup_seq;

create sequence ix_ginas_controlled_vocab_seq;

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

create sequence ix_core_predicate_seq;

create sequence ix_core_principal_seq;

create sequence ix_core_procjob_seq;

create sequence ix_core_procrec_seq;

create sequence ix_core_pubauthor_seq;

create sequence ix_core_publication_seq;

create sequence ix_core_stitch_seq;

create sequence ix_core_timeline_seq;

create sequence ix_core_userprof_seq;

create sequence ix_core_value_seq;

create sequence ix_ginas_vocabulary_term_seq;

create sequence ix_core_xref_seq;

alter table ix_ginas_agentmod add constraint fk_ix_ginas_agentmod_createdBy_1 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_agentmod_createdBy_1 on ix_ginas_agentmod (created_by_id);
alter table ix_ginas_agentmod add constraint fk_ix_ginas_agentmod_lastEdite_2 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_agentmod_lastEdite_2 on ix_ginas_agentmod (last_edited_by_id);
alter table ix_ginas_agentmod add constraint fk_ix_ginas_agentmod_owner_3 foreign key (owner_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_agentmod_owner_3 on ix_ginas_agentmod (owner_uuid);
alter table ix_ginas_agentmod add constraint fk_ix_ginas_agentmod_agentSubs_4 foreign key (agent_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_agentmod_agentSubs_4 on ix_ginas_agentmod (agent_substance_uuid);
alter table ix_ginas_agentmod add constraint fk_ix_ginas_agentmod_amount_5 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_agentmod_amount_5 on ix_ginas_agentmod (amount_uuid);
alter table ix_ginas_amount add constraint fk_ix_ginas_amount_createdBy_6 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_amount_createdBy_6 on ix_ginas_amount (created_by_id);
alter table ix_ginas_amount add constraint fk_ix_ginas_amount_lastEditedB_7 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_amount_lastEditedB_7 on ix_ginas_amount (last_edited_by_id);
alter table ix_core_attribute add constraint fk_ix_core_attribute_namespace_8 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_namespace_8 on ix_core_attribute (namespace_id);
alter table ix_core_backup add constraint fk_ix_core_backup_namespace_9 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_backup_namespace_9 on ix_core_backup (namespace_id);
alter table ix_ginas_code add constraint fk_ix_ginas_code_createdBy_10 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_code_createdBy_10 on ix_ginas_code (created_by_id);
alter table ix_ginas_code add constraint fk_ix_ginas_code_lastEditedBy_11 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_code_lastEditedBy_11 on ix_ginas_code (last_edited_by_id);
alter table ix_ginas_code add constraint fk_ix_ginas_code_owner_12 foreign key (owner_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_code_owner_12 on ix_ginas_code (owner_uuid);
alter table ix_ginas_component add constraint fk_ix_ginas_component_created_13 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_component_created_13 on ix_ginas_component (created_by_id);
alter table ix_ginas_component add constraint fk_ix_ginas_component_lastEdi_14 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_component_lastEdi_14 on ix_ginas_component (last_edited_by_id);
alter table ix_ginas_component add constraint fk_ix_ginas_component_substan_15 foreign key (substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_component_substan_15 on ix_ginas_component (substance_uuid);
alter table ix_ginas_component add constraint fk_ix_ginas_component_amount_16 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_component_amount_16 on ix_ginas_component (amount_uuid);
alter table ix_ginas_controlled_vocab add constraint fk_ix_ginas_controlled_vocab__17 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_ginas_controlled_vocab__17 on ix_ginas_controlled_vocab (namespace_id);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_18 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_18 on ix_core_curation (curator_id);
alter table ix_core_etag add constraint fk_ix_core_etag_namespace_19 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_etag_namespace_19 on ix_core_etag (namespace_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_20 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_20 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint fk_ix_core_edit_editor_21 foreign key (editor_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_edit_editor_21 on ix_core_edit (editor_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_22 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_22 on ix_core_figure (parent_id);
alter table ix_ginas_glycosylation add constraint fk_ix_ginas_glycosylation_cre_23 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_glycosylation_cre_23 on ix_ginas_glycosylation (created_by_id);
alter table ix_ginas_glycosylation add constraint fk_ix_ginas_glycosylation_las_24 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_glycosylation_las_24 on ix_ginas_glycosylation (last_edited_by_id);
alter table ix_ginas_glycosylation add constraint fk_ix_ginas_glycosylation__CG_25 foreign key (c_glycosylation_sites_uuid) references ix_ginas_site_lob (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_glycosylation__CG_25 on ix_ginas_glycosylation (c_glycosylation_sites_uuid);
alter table ix_ginas_glycosylation add constraint fk_ix_ginas_glycosylation__NG_26 foreign key (n_glycosylation_sites_uuid) references ix_ginas_site_lob (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_glycosylation__NG_26 on ix_ginas_glycosylation (n_glycosylation_sites_uuid);
alter table ix_ginas_glycosylation add constraint fk_ix_ginas_glycosylation__OG_27 foreign key (o_glycosylation_sites_uuid) references ix_ginas_site_lob (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_glycosylation__OG_27 on ix_ginas_glycosylation (o_glycosylation_sites_uuid);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organ_28 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organ_28 on ix_core_investigator (organization_id);
alter table ix_ginas_linkage add constraint fk_ix_ginas_linkage_createdBy_29 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_linkage_createdBy_29 on ix_ginas_linkage (created_by_id);
alter table ix_ginas_linkage add constraint fk_ix_ginas_linkage_lastEdite_30 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_linkage_lastEdite_30 on ix_ginas_linkage (last_edited_by_id);
alter table ix_ginas_linkage add constraint fk_ix_ginas_linkage_owner_31 foreign key (owner_uuid) references ix_ginas_nucleicacid (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_linkage_owner_31 on ix_ginas_linkage (owner_uuid);
alter table ix_ginas_linkage add constraint fk_ix_ginas_linkage_siteConta_32 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_linkage_siteConta_32 on ix_ginas_linkage (site_container_uuid);
alter table ix_ginas_material add constraint fk_ix_ginas_material_createdB_33 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_material_createdB_33 on ix_ginas_material (created_by_id);
alter table ix_ginas_material add constraint fk_ix_ginas_material_lastEdit_34 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_material_lastEdit_34 on ix_ginas_material (last_edited_by_id);
alter table ix_ginas_material add constraint fk_ix_ginas_material_owner_35 foreign key (owner_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_material_owner_35 on ix_ginas_material (owner_uuid);
alter table ix_ginas_material add constraint fk_ix_ginas_material_amount_36 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_material_amount_36 on ix_ginas_material (amount_uuid);
alter table ix_ginas_material add constraint fk_ix_ginas_material_monomerS_37 foreign key (monomer_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_material_monomerS_37 on ix_ginas_material (monomer_substance_uuid);
alter table ix_ginas_mixture add constraint fk_ix_ginas_mixture_createdBy_38 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_mixture_createdBy_38 on ix_ginas_mixture (created_by_id);
alter table ix_ginas_mixture add constraint fk_ix_ginas_mixture_lastEdite_39 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_mixture_lastEdite_39 on ix_ginas_mixture (last_edited_by_id);
alter table ix_ginas_mixture add constraint fk_ix_ginas_mixture_parentSub_40 foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_mixture_parentSub_40 on ix_ginas_mixture (parent_substance_uuid);
alter table ix_ginas_modifications add constraint fk_ix_ginas_modifications_cre_41 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_modifications_cre_41 on ix_ginas_modifications (created_by_id);
alter table ix_ginas_modifications add constraint fk_ix_ginas_modifications_las_42 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_modifications_las_42 on ix_ginas_modifications (last_edited_by_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_createdBy_43 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_createdBy_43 on ix_ginas_moiety (created_by_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_lastEdited_44 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_lastEdited_44 on ix_ginas_moiety (last_edited_by_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_owner_45 foreign key (owner_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_owner_45 on ix_ginas_moiety (owner_uuid);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_structure_46 foreign key (structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_structure_46 on ix_ginas_moiety (structure_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_count_47 foreign key (count_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_count_47 on ix_ginas_moiety (count_uuid);
alter table ix_ginas_name add constraint fk_ix_ginas_name_createdBy_48 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_name_createdBy_48 on ix_ginas_name (created_by_id);
alter table ix_ginas_name add constraint fk_ix_ginas_name_lastEditedBy_49 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_name_lastEditedBy_49 on ix_ginas_name (last_edited_by_id);
alter table ix_ginas_name add constraint fk_ix_ginas_name_owner_50 foreign key (owner_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_name_owner_50 on ix_ginas_name (owner_uuid);
alter table ix_ginas_nameorg add constraint fk_ix_ginas_nameorg_createdBy_51 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_nameorg_createdBy_51 on ix_ginas_nameorg (created_by_id);
alter table ix_ginas_nameorg add constraint fk_ix_ginas_nameorg_lastEdite_52 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_nameorg_lastEdite_52 on ix_ginas_nameorg (last_edited_by_id);
alter table ix_ginas_nameorg add constraint fk_ix_ginas_nameorg_owner_53 foreign key (owner_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_nameorg_owner_53 on ix_ginas_nameorg (owner_uuid);
alter table ix_core_namespace add constraint fk_ix_core_namespace_owner_54 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_namespace_owner_54 on ix_core_namespace (owner_id);
alter table ix_ginas_note add constraint fk_ix_ginas_note_createdBy_55 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_note_createdBy_55 on ix_ginas_note (created_by_id);
alter table ix_ginas_note add constraint fk_ix_ginas_note_lastEditedBy_56 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_note_lastEditedBy_56 on ix_ginas_note (last_edited_by_id);
alter table ix_ginas_note add constraint fk_ix_ginas_note_owner_57 foreign key (owner_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_note_owner_57 on ix_ginas_note (owner_uuid);
alter table ix_ginas_nucleicacid add constraint fk_ix_ginas_nucleicacid_creat_58 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_nucleicacid_creat_58 on ix_ginas_nucleicacid (created_by_id);
alter table ix_ginas_nucleicacid add constraint fk_ix_ginas_nucleicacid_lastE_59 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_nucleicacid_lastE_59 on ix_ginas_nucleicacid (last_edited_by_id);
alter table ix_ginas_nucleicacid add constraint fk_ix_ginas_nucleicacid_modif_60 foreign key (modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_nucleicacid_modif_60 on ix_ginas_nucleicacid (modifications_uuid);
alter table ix_ginas_otherlinks add constraint fk_ix_ginas_otherlinks_create_61 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_otherlinks_create_61 on ix_ginas_otherlinks (created_by_id);
alter table ix_ginas_otherlinks add constraint fk_ix_ginas_otherlinks_lastEd_62 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_otherlinks_lastEd_62 on ix_ginas_otherlinks (last_edited_by_id);
alter table ix_ginas_otherlinks add constraint fk_ix_ginas_otherlinks_owner_63 foreign key (owner_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_otherlinks_owner_63 on ix_ginas_otherlinks (owner_uuid);
alter table ix_ginas_otherlinks add constraint fk_ix_ginas_otherlinks_siteCo_64 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_otherlinks_siteCo_64 on ix_ginas_otherlinks (site_container_uuid);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_created_65 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_created_65 on ix_ginas_parameter (created_by_id);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_lastEdi_66 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_lastEdi_66 on ix_ginas_parameter (last_edited_by_id);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_owner_67 foreign key (owner_uuid) references ix_ginas_property (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_owner_67 on ix_ginas_parameter (owner_uuid);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_referen_68 foreign key (referenced_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_referen_68 on ix_ginas_parameter (referenced_substance_uuid);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_value_69 foreign key (value_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_value_69 on ix_ginas_parameter (value_uuid);
alter table ix_core_payload add constraint fk_ix_core_payload_namespace_70 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_payload_namespace_70 on ix_core_payload (namespace_id);
alter table ix_ginas_physicalmod add constraint fk_ix_ginas_physicalmod_creat_71 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physicalmod_creat_71 on ix_ginas_physicalmod (created_by_id);
alter table ix_ginas_physicalmod add constraint fk_ix_ginas_physicalmod_lastE_72 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physicalmod_lastE_72 on ix_ginas_physicalmod (last_edited_by_id);
alter table ix_ginas_physicalmod add constraint fk_ix_ginas_physicalmod_owner_73 foreign key (owner_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_physicalmod_owner_73 on ix_ginas_physicalmod (owner_uuid);
alter table ix_ginas_physicalpar add constraint fk_ix_ginas_physicalpar_creat_74 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physicalpar_creat_74 on ix_ginas_physicalpar (created_by_id);
alter table ix_ginas_physicalpar add constraint fk_ix_ginas_physicalpar_lastE_75 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physicalpar_lastE_75 on ix_ginas_physicalpar (last_edited_by_id);
alter table ix_ginas_physicalpar add constraint fk_ix_ginas_physicalpar_owner_76 foreign key (owner_uuid) references ix_ginas_physicalmod (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_physicalpar_owner_76 on ix_ginas_physicalpar (owner_uuid);
alter table ix_ginas_physicalpar add constraint fk_ix_ginas_physicalpar_amoun_77 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_physicalpar_amoun_77 on ix_ginas_physicalpar (amount_uuid);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_createdBy_78 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_createdBy_78 on ix_ginas_polymer (created_by_id);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_lastEdite_79 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_lastEdite_79 on ix_ginas_polymer (last_edited_by_id);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_classific_80 foreign key (classification_uuid) references polymer_classification (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_classific_80 on ix_ginas_polymer (classification_uuid);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_displaySt_81 foreign key (display_structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_displaySt_81 on ix_ginas_polymer (display_structure_id);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_idealized_82 foreign key (idealized_structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_idealized_82 on ix_ginas_polymer (idealized_structure_id);
alter table polymer_classification add constraint fk_polymer_classification_cre_83 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_polymer_classification_cre_83 on polymer_classification (created_by_id);
alter table polymer_classification add constraint fk_polymer_classification_las_84 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_polymer_classification_las_84 on polymer_classification (last_edited_by_id);
alter table polymer_classification add constraint fk_polymer_classification_par_85 foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_polymer_classification_par_85 on polymer_classification (parent_substance_uuid);
alter table ix_core_predicate add constraint fk_ix_core_predicate_namespac_86 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_namespac_86 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_subject_87 foreign key (subject_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_subject_87 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint fk_ix_core_principal_namespac_88 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_principal_namespac_88 on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_89 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_89 on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint fk_ix_core_principal_institut_90 foreign key (institution_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_principal_institut_90 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_owner_91 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_owner_91 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_payload_92 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_payload_92 on ix_core_procjob (payload_id);
alter table ix_core_procrec add constraint fk_ix_core_procrec_xref_93 foreign key (xref_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_procrec_xref_93 on ix_core_procrec (xref_id);
alter table ix_core_procrec add constraint fk_ix_core_procrec_job_94 foreign key (job_id) references ix_core_procjob (id) on delete restrict on update restrict;
create index ix_ix_core_procrec_job_94 on ix_core_procrec (job_id);
alter table ix_ginas_property add constraint fk_ix_ginas_property_createdB_95 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_property_createdB_95 on ix_ginas_property (created_by_id);
alter table ix_ginas_property add constraint fk_ix_ginas_property_lastEdit_96 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_property_lastEdit_96 on ix_ginas_property (last_edited_by_id);
alter table ix_ginas_property add constraint fk_ix_ginas_property_owner_97 foreign key (owner_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_property_owner_97 on ix_ginas_property (owner_uuid);
alter table ix_ginas_property add constraint fk_ix_ginas_property_value_98 foreign key (value_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_property_value_98 on ix_ginas_property (value_uuid);
alter table ix_ginas_property add constraint fk_ix_ginas_property_referenc_99 foreign key (referenced_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_property_referenc_99 on ix_ginas_property (referenced_substance_uuid);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_createdB_100 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_protein_createdB_100 on ix_ginas_protein (created_by_id);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_lastEdit_101 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_protein_lastEdit_101 on ix_ginas_protein (last_edited_by_id);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_glycosyl_102 foreign key (glycosylation_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_protein_glycosyl_102 on ix_ginas_protein (glycosylation_uuid);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_103 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_103 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journ_104 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journ_104 on ix_core_publication (journal_id);
alter table ix_ginas_reference add constraint fk_ix_ginas_reference_create_105 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_reference_create_105 on ix_ginas_reference (created_by_id);
alter table ix_ginas_reference add constraint fk_ix_ginas_reference_lastEd_106 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_reference_lastEd_106 on ix_ginas_reference (last_edited_by_id);
alter table ix_ginas_reference add constraint fk_ix_ginas_reference_owner_107 foreign key (owner_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_reference_owner_107 on ix_ginas_reference (owner_uuid);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_cre_108 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_cre_108 on ix_ginas_relationship (created_by_id);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_las_109 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_las_109 on ix_ginas_relationship (last_edited_by_id);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_own_110 foreign key (owner_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_own_110 on ix_ginas_relationship (owner_uuid);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_amo_111 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_amo_111 on ix_ginas_relationship (amount_uuid);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_rel_112 foreign key (related_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_rel_112 on ix_ginas_relationship (related_substance_uuid);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_med_113 foreign key (mediator_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_med_113 on ix_ginas_relationship (mediator_substance_uuid);
alter table ix_core_session add constraint fk_ix_core_session_profile_114 foreign key (profile_id) references ix_core_userprof (id) on delete restrict on update restrict;
create index ix_ix_core_session_profile_114 on ix_core_session (profile_id);
alter table ix_ginas_site_lob add constraint fk_ix_ginas_site_lob_created_115 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_site_lob_created_115 on ix_ginas_site_lob (created_by_id);
alter table ix_ginas_site_lob add constraint fk_ix_ginas_site_lob_lastEdi_116 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_site_lob_lastEdi_116 on ix_ginas_site_lob (last_edited_by_id);
alter table ix_ginas_ssg1 add constraint fk_ix_ginas_ssg1_createdBy_117 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_ssg1_createdBy_117 on ix_ginas_ssg1 (created_by_id);
alter table ix_ginas_ssg1 add constraint fk_ix_ginas_ssg1_lastEditedB_118 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_ssg1_lastEditedB_118 on ix_ginas_ssg1 (last_edited_by_id);
alter table ix_ginas_structuralmod add constraint fk_ix_ginas_structuralmod_cr_119 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_structuralmod_cr_119 on ix_ginas_structuralmod (created_by_id);
alter table ix_ginas_structuralmod add constraint fk_ix_ginas_structuralmod_la_120 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_structuralmod_la_120 on ix_ginas_structuralmod (last_edited_by_id);
alter table ix_ginas_structuralmod add constraint fk_ix_ginas_structuralmod_ow_121 foreign key (owner_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structuralmod_ow_121 on ix_ginas_structuralmod (owner_uuid);
alter table ix_ginas_structuralmod add constraint fk_ix_ginas_structuralmod_si_122 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structuralmod_si_122 on ix_ginas_structuralmod (site_container_uuid);
alter table ix_ginas_structuralmod add constraint fk_ix_ginas_structuralmod_ex_123 foreign key (extent_amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structuralmod_ex_123 on ix_ginas_structuralmod (extent_amount_uuid);
alter table ix_ginas_structuralmod add constraint fk_ix_ginas_structuralmod_mo_124 foreign key (molecular_fragment_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structuralmod_mo_124 on ix_ginas_structuralmod (molecular_fragment_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_created_125 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_created_125 on ix_ginas_strucdiv (created_by_id);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_lastEdi_126 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_lastEdi_126 on ix_ginas_strucdiv (last_edited_by_id);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_hybridS_127 foreign key (paternal_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_hybridS_127 on ix_ginas_strucdiv (paternal_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_hybridS_128 foreign key (maternal_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_hybridS_128 on ix_ginas_strucdiv (maternal_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_parentS_129 foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_parentS_129 on ix_ginas_strucdiv (parent_substance_uuid);
alter table ix_core_structure add constraint fk_ix_core_structure_created_130 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_structure_created_130 on ix_core_structure (created_by_id);
alter table ix_core_structure add constraint fk_ix_core_structure_lastEdi_131 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_structure_lastEdi_131 on ix_core_structure (last_edited_by_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_create_132 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_create_132 on ix_ginas_substance (created_by_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_lastEd_133 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_lastEd_133 on ix_ginas_substance (last_edited_by_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_approv_134 foreign key (approved_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_approv_134 on ix_ginas_substance (approved_by_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_modifi_135 foreign key (modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_modifi_135 on ix_ginas_substance (modifications_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_protei_136 foreign key (protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_protei_136 on ix_ginas_substance (protein_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_specif_137 foreign key (specified_substance_uuid) references ix_ginas_ssg1 (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_specif_137 on ix_ginas_substance (specified_substance_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_struct_138 foreign key (structurally_diverse_uuid) references ix_ginas_strucdiv (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_struct_138 on ix_ginas_substance (structurally_diverse_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_nuclei_139 foreign key (nucleic_acid_uuid) references ix_ginas_nucleicacid (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_nuclei_139 on ix_ginas_substance (nucleic_acid_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_polyme_140 foreign key (polymer_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_polyme_140 on ix_ginas_substance (polymer_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_struct_141 foreign key (structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_struct_141 on ix_ginas_substance (structure_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_mixtur_142 foreign key (mixture_uuid) references ix_ginas_mixture (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_mixtur_142 on ix_ginas_substance (mixture_uuid);
alter table ix_ginas_substanceref add constraint fk_ix_ginas_substanceref_cre_143 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substanceref_cre_143 on ix_ginas_substanceref (created_by_id);
alter table ix_ginas_substanceref add constraint fk_ix_ginas_substanceref_las_144 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substanceref_las_144 on ix_ginas_substanceref (last_edited_by_id);
alter table ix_ginas_subunit add constraint fk_ix_ginas_subunit_createdB_145 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_subunit_createdB_145 on ix_ginas_subunit (created_by_id);
alter table ix_ginas_subunit add constraint fk_ix_ginas_subunit_lastEdit_146 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_subunit_lastEdit_146 on ix_ginas_subunit (last_edited_by_id);
alter table ix_ginas_sugar add constraint fk_ix_ginas_sugar_createdBy_147 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_sugar_createdBy_147 on ix_ginas_sugar (created_by_id);
alter table ix_ginas_sugar add constraint fk_ix_ginas_sugar_lastEdited_148 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_sugar_lastEdited_148 on ix_ginas_sugar (last_edited_by_id);
alter table ix_ginas_sugar add constraint fk_ix_ginas_sugar_owner_149 foreign key (owner_uuid) references ix_ginas_nucleicacid (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_sugar_owner_149 on ix_ginas_sugar (owner_uuid);
alter table ix_ginas_sugar add constraint fk_ix_ginas_sugar_siteContai_150 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_sugar_siteContai_150 on ix_ginas_sugar (site_container_uuid);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_createdBy_151 foreign key (created_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_unit_createdBy_151 on ix_ginas_unit (created_by_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_lastEditedB_152 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_unit_lastEditedB_152 on ix_ginas_unit (last_edited_by_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_owner_153 foreign key (owner_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_unit_owner_153 on ix_ginas_unit (owner_uuid);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_amap_154 foreign key (amap_id) references ix_core_value (id) on delete restrict on update restrict;
create index ix_ix_ginas_unit_amap_154 on ix_ginas_unit (amap_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_amount_155 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_unit_amount_155 on ix_ginas_unit (amount_uuid);
alter table ix_core_userprof add constraint fk_ix_core_userprof_namespac_156 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_userprof_namespac_156 on ix_core_userprof (namespace_id);
alter table ix_core_userprof add constraint fk_ix_core_userprof_user_157 foreign key (user_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_userprof_user_157 on ix_core_userprof (user_id);
alter table ix_ginas_vocabulary_term add constraint fk_ix_ginas_vocabulary_term__158 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_ginas_vocabulary_term__158 on ix_ginas_vocabulary_term (namespace_id);
alter table ix_ginas_vocabulary_term add constraint fk_ix_ginas_vocabulary_term__159 foreign key (owner_id) references ix_ginas_controlled_vocab (id) on delete restrict on update restrict;
create index ix_ix_ginas_vocabulary_term__159 on ix_ginas_vocabulary_term (owner_id);
alter table ix_core_xref add constraint fk_ix_core_xref_namespace_160 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_xref_namespace_160 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_ginas_controlled_vocab_core_v add constraint fk_ix_ginas_controlled_vocab__01 foreign key (ix_ginas_controlled_vocab_id) references ix_ginas_controlled_vocab (id) on delete restrict on update restrict;

alter table ix_ginas_controlled_vocab_core_v add constraint fk_ix_ginas_controlled_vocab__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_event_prop add constraint fk_ix_core_event_prop_ix_core_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_prop add constraint fk_ix_core_event_prop_ix_core_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_event_link add constraint fk_ix_core_event_link_ix_core_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_link add constraint fk_ix_core_event_link_ix_core_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_substance_mix_comp add constraint fk_ix_ginas_substance_mix_com_01 foreign key (ix_ginas_mixture_uuid) references ix_ginas_mixture (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_mix_comp add constraint fk_ix_ginas_substance_mix_com_02 foreign key (ix_ginas_component_uuid) references ix_ginas_component (uuid) on delete restrict on update restrict;

alter table ix_ginas_nucleicacid_subunits add constraint fk_ix_ginas_nucleicacid_subun_01 foreign key (ix_ginas_nucleicacid_uuid) references ix_ginas_nucleicacid (uuid) on delete restrict on update restrict;

alter table ix_ginas_nucleicacid_subunits add constraint fk_ix_ginas_nucleicacid_subun_02 foreign key (ix_ginas_subunit_uuid) references ix_ginas_subunit (uuid) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_i_01 foreign key (ix_core_payload_id) references ix_core_payload (id) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_i_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_procjob_key add constraint fk_ix_core_procjob_key_ix_cor_01 foreign key (ix_core_procjob_id) references ix_core_procjob (id) on delete restrict on update restrict;

alter table ix_core_procjob_key add constraint fk_ix_core_procjob_key_ix_cor_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_procrec_prop add constraint fk_ix_core_procrec_prop_ix_co_01 foreign key (ix_core_procrec_id) references ix_core_procrec (id) on delete restrict on update restrict;

alter table ix_core_procrec_prop add constraint fk_ix_core_procrec_prop_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_i_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_i_02 foreign key (ix_ginas_subunit_uuid) references ix_ginas_subunit (uuid) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keywor_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keywor_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_i_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_i_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_02 foreign key (ix_core_pubauthor_id) references ix_core_pubauthor (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_ginas_substance_ss_comp add constraint fk_ix_ginas_substance_ss_comp_01 foreign key (ix_ginas_ssg1_uuid) references ix_ginas_ssg1 (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_ss_comp add constraint fk_ix_ginas_substance_ss_comp_02 foreign key (ix_ginas_component_uuid) references ix_ginas_component (uuid) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_i_01 foreign key (ix_core_stitch_id) references ix_core_stitch (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_i_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix__01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix__02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_ginas_substance_tags add constraint fk_ix_ginas_substance_tags_ix_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_tags add constraint fk_ix_ginas_substance_tags_ix_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_timeline_event add constraint fk_ix_core_timeline_event_ix__01 foreign key (ix_core_timeline_id) references ix_core_timeline (id) on delete restrict on update restrict;

alter table ix_core_timeline_event add constraint fk_ix_core_timeline_event_ix__02 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_userprof_prop add constraint fk_ix_core_userprof_prop_ix_c_01 foreign key (ix_core_userprof_id) references ix_core_userprof (id) on delete restrict on update restrict;

alter table ix_core_userprof_prop add constraint fk_ix_core_userprof_prop_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_01 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ix_core_acl;

drop table if exists ix_core_acl_principal;

drop table if exists ix_core_acl_group;

drop table if exists ix_ginas_agentmod;

drop table if exists ix_ginas_amount;

drop table if exists ix_core_attribute;

drop table if exists ix_core_backup;

drop table if exists ix_ginas_code;

drop table if exists ix_ginas_component;

drop table if exists ix_ginas_controlled_vocab;

drop table if exists ix_ginas_controlled_vocab_core_v;

drop table if exists ix_core_curation;

drop table if exists ix_core_etag;

drop table if exists ix_core_etagref;

drop table if exists ix_core_edit;

drop table if exists ix_core_event;

drop table if exists ix_core_event_prop;

drop table if exists ix_core_event_link;

drop table if exists ix_core_figure;

drop table if exists ix_core_filedata;

drop table if exists ix_ginas_glycosylation;

drop table if exists ix_core_group;

drop table if exists ix_core_group_principal;

drop table if exists ix_core_investigator;

drop table if exists ix_core_journal;

drop table if exists ix_ginas_linkage;

drop table if exists ix_ginas_material;

drop table if exists ix_ginas_mixture;

drop table if exists ix_ginas_substance_mix_comp;

drop table if exists ix_ginas_modifications;

drop table if exists ix_ginas_moiety;

drop table if exists ix_ginas_name;

drop table if exists ix_ginas_nameorg;

drop table if exists ix_core_namespace;

drop table if exists ix_ginas_note;

drop table if exists ix_ginas_nucleicacid;

drop table if exists ix_ginas_nucleicacid_subunits;

drop table if exists ix_core_organization;

drop table if exists ix_ginas_otherlinks;

drop table if exists ix_ginas_parameter;

drop table if exists ix_core_payload;

drop table if exists ix_core_payload_property;

drop table if exists ix_ginas_physicalmod;

drop table if exists ix_ginas_physicalpar;

drop table if exists ix_ginas_polymer;

drop table if exists polymer_classification;

drop table if exists ix_core_predicate;

drop table if exists ix_core_predicate_object;

drop table if exists ix_core_predicate_property;

drop table if exists ix_core_principal;

drop table if exists ix_core_procjob;

drop table if exists ix_core_procjob_key;

drop table if exists ix_core_procrec;

drop table if exists ix_core_procrec_prop;

drop table if exists ix_ginas_property;

drop table if exists ix_ginas_protein;

drop table if exists ix_ginas_protein_subunit;

drop table if exists ix_core_pubauthor;

drop table if exists ix_core_publication;

drop table if exists ix_core_publication_keyword;

drop table if exists ix_core_publication_mesh;

drop table if exists ix_core_publication_author;

drop table if exists ix_core_publication_figure;

drop table if exists ix_ginas_reference;

drop table if exists ix_ginas_relationship;

drop table if exists ix_core_session;

drop table if exists ix_ginas_site_lob;

drop table if exists ix_ginas_ssg1;

drop table if exists ix_ginas_substance_ss_comp;

drop table if exists ix_core_stitch;

drop table if exists ix_core_stitch_attribute;

drop table if exists ix_ginas_structuralmod;

drop table if exists ix_ginas_strucdiv;

drop table if exists ix_core_structure;

drop table if exists ix_core_structure_property;

drop table if exists ix_core_structure_link;

drop table if exists ix_ginas_substance;

drop table if exists ix_ginas_substance_tags;

drop table if exists ix_ginas_substanceref;

drop table if exists ix_ginas_subunit;

drop table if exists ix_ginas_sugar;

drop table if exists ix_core_timeline;

drop table if exists ix_core_timeline_event;

drop table if exists ix_ginas_unit;

drop table if exists ix_core_userprof;

drop table if exists ix_core_userprof_prop;

drop table if exists ix_core_value;

drop table if exists ix_ginas_vocabulary_term;

drop table if exists ix_core_xref;

drop table if exists ix_core_xref_property;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ix_core_acl_seq;

drop sequence if exists ix_core_attribute_seq;

drop sequence if exists ix_core_backup_seq;

drop sequence if exists ix_ginas_controlled_vocab_seq;

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

drop sequence if exists ix_core_predicate_seq;

drop sequence if exists ix_core_principal_seq;

drop sequence if exists ix_core_procjob_seq;

drop sequence if exists ix_core_procrec_seq;

drop sequence if exists ix_core_pubauthor_seq;

drop sequence if exists ix_core_publication_seq;

drop sequence if exists ix_core_stitch_seq;

drop sequence if exists ix_core_timeline_seq;

drop sequence if exists ix_core_userprof_seq;

drop sequence if exists ix_core_value_seq;

drop sequence if exists ix_ginas_vocabulary_term_seq;

drop sequence if exists ix_core_xref_seq;

