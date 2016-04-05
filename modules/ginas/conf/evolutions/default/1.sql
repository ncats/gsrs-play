# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        number(19) not null,
  perm                      number(10),
  constraint c_af397dfb check (perm in (0,1,2,3,4,5,6)),
  constraint c_e5229e11 primary key (id))
;

create table ix_ginas_agentmod (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  agent_modification_process varchar2(255),
  agent_modification_role   varchar2(255),
  agent_modification_type   varchar2(255),
  agent_substance_uuid      varchar2(40),
  amount_uuid               varchar2(40),
  modification_group        varchar2(255),
  internal_version          number(19) not null,
  constraint c_41d1a72a primary key (uuid))
;

create table ix_ginas_amount (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  type                      varchar2(255),
  average                   number(19,4),
  high_limit                number(19,4),
  high                      number(19,4),
  low_limit                 number(19,4),
  low                       number(19,4),
  units                     varchar2(255),
  non_numeric_value         varchar2(255),
  approval_id               varchar2(10),
  internal_version          number(19) not null,
  constraint c_bd8a17ea primary key (uuid))
;

create table ix_core_attribute (
  id                        number(19) not null,
  name                      varchar2(255),
  value                     varchar2(1024),
  namespace_id              number(19),
  constraint c_89720b65 primary key (id))
;

create table ix_core_backup (
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  refid                     varchar2(255),
  kind                      varchar2(255),
  data                      blob,
  sha1                      varchar2(255),
  compressed                number(1),
  version                   number(19) not null,
  constraint c_2884574a unique (refid),
  constraint c_6ff32a89 primary key (id))
;

create table ix_ginas_code (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  code_system               varchar2(255),
  code                      varchar2(255) not null,
  comments                  clob,
  code_text                 clob,
  type                      varchar2(255),
  url                       clob,
  internal_version          number(19) not null,
  constraint c_a189cb43 primary key (uuid))
;

create table ix_ginas_component (
  dtype                     varchar2(10) not null,
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  type                      varchar2(255),
  substance_uuid            varchar2(40),
  internal_version          number(19) not null,
  role                      varchar2(255),
  amount_uuid               varchar2(40),
  constraint c_e63019e1 primary key (uuid))
;

create table ix_ginas_controlled_vocab (
  dtype                     varchar2(10) not null,
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  domain                    varchar2(255),
  vocabulary_term_type      varchar2(255),
  editable                  number(1),
  version                   number(19) not null,
  constraint c_19a6a852 unique (domain),
  constraint c_946621fd primary key (id))
;

create table ix_core_curation (
  id                        number(19) not null,
  curator_id                number(19),
  status                    number(10),
  timestamp                 timestamp,
  constraint c_0e6efb2d check (status in (0,1,2,3)),
  constraint c_5e8cc0d6 primary key (id))
;

create table ix_core_etag (
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  etag                      varchar2(16),
  uri                       varchar2(4000),
  path                      varchar2(255),
  method                    varchar2(10),
  sha1                      varchar2(40),
  total                     number(10),
  count                     number(10),
  skip                      number(10),
  top                       number(10),
  status                    number(10),
  query                     varchar2(2048),
  filter                    varchar2(4000),
  version                   number(19) not null,
  constraint c_116a77ea unique (etag),
  constraint c_5e315d9a primary key (id))
;

create table ix_core_etagref (
  id                        number(19) not null,
  etag_id                   number(19),
  ref_id                    number(19),
  constraint c_72669a72 primary key (id))
;

create table ix_core_edit (
  id                        varchar2(40) not null,
  created                   number(19),
  refid                     varchar2(255),
  kind                      varchar2(255),
  batch                     varchar2(64),
  editor_id                 number(19),
  path                      varchar2(1024),
  comments                  clob,
  version                   varchar2(255),
  old_value                 clob,
  new_value                 clob,
  constraint c_a0121b77 primary key (id))
;

create table ix_core_event (
  id                        number(19) not null,
  title                     varchar2(255),
  description               clob,
  url                       varchar2(1024),
  start_time                number(19),
  end_time                  number(19),
  unit                      number(10),
  constraint c_4b57cbc7 check (unit in (0,1,2,3,4,5,6,7)),
  constraint c_3a56e5b7 primary key (id))
;

create table ix_core_figure (
  dtype                     varchar2(10) not null,
  id                        number(19) not null,
  caption                   varchar2(255),
  mime_type                 varchar2(255),
  url                       varchar2(1024),
  data                      blob,
  data_size                 number(10),
  sha1                      varchar2(140),
  parent_id                 number(19),
  constraint c_f2312002 primary key (id))
;

create table ix_core_filedata (
  dtype                     varchar2(10) not null,
  id                        varchar2(40) not null,
  mime_type                 varchar2(255),
  data                      blob,
  data_size                 number(19),
  sha1                      varchar2(140),
  constraint c_a1257e12 primary key (id))
;

create table ix_ginas_reference_cit (
  id                        number(19) not null,
  entity_type               varchar2(255),
  constraint c_faa1a29d primary key (id))
;

create table ix_ginas_glycosylation (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  c_glycosylation_sites_uuid varchar2(40),
  n_glycosylation_sites_uuid varchar2(40),
  o_glycosylation_sites_uuid varchar2(40),
  glycosylation_type        varchar2(255),
  internal_version          number(19) not null,
  constraint c_d7c7ba3e primary key (uuid))
;

create table ix_core_group (
  id                        number(19) not null,
  name                      varchar2(255),
  constraint c_3aa2c124 unique (name),
  constraint c_c402a563 primary key (id))
;

create table ix_core_investigator (
  id                        number(19) not null,
  name                      varchar2(255),
  pi_id                     number(19),
  organization_id           number(19),
  role                      number(10),
  constraint c_3c2d07c0 check (role in (0,1)),
  constraint c_9ef40c02 primary key (id))
;

create table ix_core_journal (
  id                        number(19) not null,
  issn                      varchar2(10),
  volume                    varchar2(255),
  issue                     varchar2(255),
  year                      number(10),
  month                     varchar2(10),
  title                     varchar2(1024),
  iso_abbr                  varchar2(255),
  factor                    number(19,4),
  constraint c_bfbfdc72 primary key (id))
;

create table ix_ginas_linkage (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  linkage                   varchar2(255),
  site_container_uuid       varchar2(40),
  internal_version          number(19) not null,
  constraint c_ecf3403d primary key (uuid))
;

create table ix_ginas_material (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  amount_uuid               varchar2(40),
  monomer_substance_uuid    varchar2(40),
  type                      varchar2(255),
  defining                  number(1),
  internal_version          number(19) not null,
  constraint c_9c93c276 primary key (uuid))
;

create table ix_ginas_mixture (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  parent_substance_uuid     varchar2(40),
  internal_version          number(19) not null,
  constraint c_2c9974d5 primary key (uuid))
;

create table ix_ginas_modifications (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  internal_version          number(19) not null,
  constraint c_ce402ea5 primary key (uuid))
;

create table ix_ginas_moiety (
  uuid                      varchar2(40) not null,
  chemical_substance_uuid   varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  structure_id              varchar2(40),
  count_uuid                varchar2(40),
  inner_uuid                varchar2(255),
  internal_version          number(19) not null,
  constraint c_ce8c5912 unique (inner_uuid),
  constraint c_6cff583e primary key (uuid))
;

create table ix_ginas_name (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  name                      varchar2(255) not null,
  full_name                 clob,
  type                      varchar2(32),
  domains                   clob,
  languages                 clob,
  name_jurisdiction         clob,
  preferred                 number(1),
  display_name              number(1),
  internal_version          number(19) not null,
  constraint c_101f87f9 primary key (uuid))
;

create table ix_ginas_nameorg (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  name_org                  varchar2(255) not null,
  deprecated_date           timestamp,
  internal_version          number(19) not null,
  constraint c_d59f610a primary key (uuid))
;

create table ix_core_namespace (
  id                        number(19) not null,
  name                      varchar2(255),
  owner_id                  number(19),
  location                  varchar2(1024),
  modifier                  number(10),
  constraint c_ca752733 check (modifier in (0,1,2)),
  constraint c_2b9ef5c1 unique (name),
  constraint c_8ff2596b primary key (id))
;

create table ix_ginas_note (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  note                      clob,
  internal_version          number(19) not null,
  constraint c_a23c7c27 primary key (uuid))
;

create table ix_ginas_nucleicacid (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  modifications_uuid        varchar2(40),
  nucleic_acid_type         varchar2(255),
  nucleic_acid_sub_type     varchar2(255),
  sequence_origin           varchar2(255),
  sequence_type             varchar2(255),
  internal_version          number(19) not null,
  constraint c_9c3f8860 primary key (uuid))
;

create table ix_core_organization (
  id                        number(19) not null,
  duns                      varchar2(10),
  name                      varchar2(255),
  department                varchar2(255),
  city                      varchar2(255),
  state                     varchar2(128),
  zipcode                   varchar2(64),
  district                  varchar2(255),
  country                   varchar2(255),
  fips                      varchar2(3),
  longitude                 number(19,4),
  latitude                  number(19,4),
  constraint c_a5f8ca4a primary key (id))
;

create table ix_ginas_otherlinks (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  linkage_type              varchar2(255),
  site_container_uuid       varchar2(40),
  internal_version          number(19) not null,
  constraint c_7a4ebee1 primary key (uuid))
;

create table ix_ginas_parameter (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  name                      varchar2(255) not null,
  type                      varchar2(255),
  value_uuid                varchar2(40),
  internal_version          number(19) not null,
  constraint c_52b30857 primary key (uuid))
;

create table ix_core_payload (
  id                        varchar2(40) not null,
  namespace_id              number(19),
  created                   timestamp,
  name                      varchar2(1024),
  sha1                      varchar2(40),
  mime_type                 varchar2(128),
  capacity                  number(19),
  constraint c_d3000c18 primary key (id))
;

create table ix_ginas_physicalmod (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  physical_modification_role varchar2(255),
  modification_group        varchar2(255),
  internal_version          number(19) not null,
  constraint c_9b3823fb primary key (uuid))
;

create table ix_ginas_physicalpar (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  parameter_name            varchar2(255),
  amount_uuid               varchar2(40),
  internal_version          number(19) not null,
  constraint c_885bf328 primary key (uuid))
;

create table ix_ginas_polymer (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  classification_uuid       varchar2(40),
  display_structure_id      varchar2(40),
  idealized_structure_id    varchar2(40),
  internal_version          number(19) not null,
  constraint c_888cfcc6 primary key (uuid))
;

create table polymer_classification (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  polymer_class             varchar2(255),
  polymer_geometry          varchar2(255),
  polymer_subclass          clob,
  source_type               varchar2(255),
  parent_substance_uuid     varchar2(40),
  internal_version          number(19) not null,
  constraint c_5fbbee1f primary key (uuid))
;

create table ix_core_predicate (
  dtype                     varchar2(10) not null,
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  subject_id                number(19),
  predicate                 varchar2(255) not null,
  version                   number(19) not null,
  constraint c_723dbe43 primary key (id))
;

create table ix_core_principal (
  dtype                     varchar2(10) not null,
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  provider                  varchar2(255),
  username                  varchar2(255),
  email                     varchar2(255),
  is_admin                  number(1),
  uri                       varchar2(1024),
  selfie_id                 number(19),
  version                   number(19) not null,
  lastname                  varchar2(255),
  forename                  varchar2(255),
  initials                  varchar2(255),
  prefname                  varchar2(255),
  suffix                    varchar2(20),
  affiliation               clob,
  orcid                     varchar2(255),
  institution_id            number(19),
  constraint c_117ea9e1 unique (username),
  constraint c_42ce36fb primary key (id))
;

create table ix_core_procjob (
  id                        number(19) not null,
  status                    number(10),
  job_start                 number(19),
  job_stop                  number(19),
  message                   clob,
  statistics                clob,
  owner_id                  number(19),
  payload_id                varchar2(40),
  last_update               timestamp not null,
  constraint c_390fae18 check (status in (0,1,2,3,4,5,6)),
  constraint c_89a4b31f primary key (id))
;

create table ix_core_procrec (
  id                        number(19) not null,
  rec_start                 number(19),
  rec_stop                  number(19),
  name                      varchar2(128),
  status                    number(10),
  message                   clob,
  xref_id                   number(19),
  job_id                    number(19),
  last_update               timestamp not null,
  constraint c_99b9493f check (status in (0,1,2,3,4)),
  constraint c_2b9203f9 primary key (id))
;

create table ix_ginas_property (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  name                      varchar2(255) not null,
  type                      varchar2(255),
  property_type             varchar2(255),
  value_uuid                varchar2(40),
  defining                  number(1),
  internal_version          number(19) not null,
  constraint c_62907420 primary key (uuid))
;

create table ix_ginas_protein (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  protein_type              varchar2(255),
  protein_sub_type          varchar2(255),
  sequence_origin           varchar2(255),
  sequence_type             varchar2(255),
  disulf_json               clob,
  glycosylation_uuid        varchar2(40),
  modifications_uuid        varchar2(40),
  internal_version          number(19) not null,
  constraint c_eb968275 primary key (uuid))
;

create table ix_core_pubauthor (
  id                        number(19) not null,
  position                  number(10),
  is_last                   number(1),
  correspondence            number(1),
  author_id                 number(19),
  constraint c_a48a2d2b primary key (id))
;

create table ix_core_publication (
  id                        number(19) not null,
  pmid                      number(19),
  pmcid                     varchar2(255),
  title                     clob,
  pages                     varchar2(255),
  doi                       varchar2(255),
  abstract_text             clob,
  journal_id                number(19),
  constraint c_d2188a90 unique (pmid),
  constraint c_388c2569 unique (pmcid),
  constraint c_9e23d970 primary key (id))
;

create table ix_ginas_reference (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  owner_uuid                varchar2(40),
  citation                  clob,
  doc_type                  varchar2(255),
  document_date             timestamp,
  public_domain             number(1),
  tags                      clob,
  uploaded_file             varchar2(1024),
  id                        varchar2(255),
  url                       clob,
  internal_version          number(19) not null,
  constraint c_dbc860d9 primary key (uuid))
;

create table ix_ginas_relationship (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  amount_uuid               varchar2(40),
  comments                  clob,
  interaction_type          varchar2(255),
  qualification             varchar2(255),
  related_substance_uuid    varchar2(40),
  mediator_substance_uuid   varchar2(40),
  type                      varchar2(255),
  internal_version          number(19) not null,
  constraint c_2dc7271b primary key (uuid))
;

create table ix_core_session (
  id                        varchar2(40) not null,
  profile_id                number(19),
  created                   number(19),
  accessed                  number(19),
  location                  varchar2(255),
  expired                   number(1),
  constraint c_fa9b3e4c primary key (id))
;

create table ix_ginas_site_lob (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  sites_short_hand          clob,
  sites_json                clob,
  site_count                number(19),
  site_type                 varchar2(255),
  internal_version          number(19) not null,
  constraint c_d4127f3b primary key (uuid))
;

create table ix_ginas_ssg1 (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  internal_version          number(19) not null,
  constraint c_54bfad15 primary key (uuid))
;

create table ix_core_stitch (
  id                        number(19) not null,
  name                      varchar2(255),
  impl                      varchar2(1024),
  description               clob,
  constraint c_a0825537 primary key (id))
;

create table ix_ginas_structuralmod (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  structural_modification_type varchar2(255),
  location_type             varchar2(255),
  residue_modified          varchar2(255),
  site_container_uuid       varchar2(40),
  extent                    varchar2(255),
  extent_amount_uuid        varchar2(40),
  molecular_fragment_uuid   varchar2(40),
  moleculare_fragment_role  varchar2(255),
  modification_group        varchar2(255),
  internal_version          number(19) not null,
  constraint c_6f682025 primary key (uuid))
;

create table ix_ginas_strucdiv (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  source_material_class     varchar2(255),
  source_material_type      varchar2(255),
  source_material_state     varchar2(255),
  organism_family           varchar2(255),
  organism_genus            varchar2(255),
  organism_species          varchar2(255),
  organism_author           varchar2(255),
  part_location             varchar2(255),
  part                      clob,
  infra_specific_type       varchar2(255),
  infra_specific_name       varchar2(255),
  developmental_stage       varchar2(255),
  fraction_name             varchar2(255),
  fraction_material_type    varchar2(255),
  paternal_uuid             varchar2(40),
  maternal_uuid             varchar2(40),
  parent_substance_uuid     varchar2(40),
  internal_version          number(19) not null,
  constraint c_5d3dccde primary key (uuid))
;

create table ix_core_structure (
  dtype                     varchar2(10) not null,
  id                        varchar2(40) not null,
  created                   timestamp,
  last_edited               timestamp,
  deprecated                number(1),
  digest                    varchar2(128),
  molfile                   clob,
  smiles                    clob,
  formula                   varchar2(255),
  stereo                    number(10),
  optical                   number(10),
  atropi                    number(10),
  stereo_comments           clob,
  stereo_centers            number(10),
  defined_stereo            number(10),
  ez_centers                number(10),
  charge                    number(10),
  mwt                       number(19,4),
  count                     number(10),
  version                   number(19) not null,
  created_by_id             number(19),
  last_edited_by_id         number(19),
  record_access             raw(255),
  record_reference_id       number(19),
  constraint c_9cf884cc check (stereo in (0,1,2,3,4,5)),
  constraint c_59012567 check (optical in (0,1,2,3,4)),
  constraint c_a16983a4 check (atropi in (0,1,2)),
  constraint c_cfe649b3 primary key (id))
;

create table ix_ginas_substance (
  dtype                     varchar2(10) not null,
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  definition_type           number(10),
  class                     number(10),
  status                    varchar2(255),
  version                   varchar2(255),
  approved_by_id            number(19),
  approved                  timestamp,
  modifications_uuid        varchar2(40),
  approval_id               varchar2(10),
  internal_version          number(19) not null,
  structure_id              varchar2(40),
  mixture_uuid              varchar2(40),
  nucleic_acid_uuid         varchar2(40),
  polymer_uuid              varchar2(40),
  protein_uuid              varchar2(40),
  specified_substance_uuid  varchar2(40),
  structurally_diverse_uuid varchar2(40),
  constraint c_0b6ac7e0 check (definition_type in (0,1)),
  constraint c_e8156539 check (class in (0,1,2,3,4,5,6,7,8,9,10,11,12)),
  constraint c_f72e5643 primary key (uuid))
;

create table ix_ginas_substanceref (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  ref_pname                 varchar2(1024),
  refuuid                   varchar2(128),
  substance_class           varchar2(255),
  approval_id               varchar2(32),
  internal_version          number(19) not null,
  constraint c_6e7c9633 primary key (uuid))
;

create table ix_ginas_subunit (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  sequence                  clob,
  subunit_index             number(10),
  internal_version          number(19) not null,
  constraint c_5b6386a1 primary key (uuid))
;

create table ix_ginas_sugar (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  sugar                     varchar2(255),
  site_container_uuid       varchar2(40),
  internal_version          number(19) not null,
  constraint c_945cbade primary key (uuid))
;

create table ix_core_timeline (
  id                        number(19) not null,
  name                      varchar2(255),
  constraint c_60549e6c primary key (id))
;

create table ix_ginas_unit (
  uuid                      varchar2(40) not null,
  current_version           number(10),
  created                   timestamp,
  created_by_id             number(19),
  last_edited               timestamp,
  last_edited_by_id         number(19),
  deprecated                number(1),
  record_access             raw(255),
  record_reference_id       number(19),
  owner_uuid                varchar2(40),
  amap_id                   number(19),
  amount_uuid               varchar2(40),
  attachment_count          number(10),
  label                     varchar2(255),
  structure                 clob,
  type                      varchar2(255),
  attachmentMap             clob,
  internal_version          number(19) not null,
  constraint c_56882a56 primary key (uuid))
;

create table ix_core_userprof (
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  user_id                   number(19),
  active                    number(1),
  hashp                     varchar2(255),
  salt                      varchar2(255),
  system_auth               number(1),
  roles_json                clob,
  apikey                    varchar2(255),
  version                   number(19) not null,
  constraint c_111364d8 primary key (id))
;

create table ix_core_value (
  dtype                     varchar2(10) not null,
  id                        number(19) not null,
  label                     varchar2(255),
  term                      varchar2(255),
  href                      clob,
  major_topic               number(1),
  heading                   varchar2(1024),
  text                      clob,
  data                      blob,
  data_size                 number(10),
  sha1                      varchar2(40),
  mime_type                 varchar2(32),
  intval                    number(19),
  numval                    number(19,4),
  unit                      varchar2(255),
  lval                      number(19,4),
  rval                      number(19,4),
  average                   number(19,4),
  strval                    varchar2(1024),
  constraint c_c279d231 primary key (id))
;

create table ix_ginas_vocabulary_term (
  dtype                     varchar2(10) not null,
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  value                     varchar2(255),
  display                   varchar2(255),
  description               varchar2(255),
  origin                    varchar2(255),
  filter                    varchar2(255),
  hidden                    number(1),
  version                   number(19) not null,
  system_category           varchar2(255),
  regex                     varchar2(255),
  fragment_structure        varchar2(255),
  simplified_structure      varchar2(255),
  constraint c_22b63beb primary key (id))
;

create table ix_core_xref (
  id                        number(19) not null,
  namespace_id              number(19),
  created                   timestamp,
  modified                  timestamp,
  deprecated                number(1),
  refid                     varchar2(40) not null,
  kind                      varchar2(255) not null,
  version                   number(19) not null,
  constraint c_cd0a1611 primary key (id))
;


create table ix_core_acl_principal (
  ix_core_acl_id                 number(19) not null,
  ix_core_principal_id           number(19) not null,
  constraint c_7577f2c1 primary key (ix_core_acl_id, ix_core_principal_id))
;

create table ix_core_acl_group (
  ix_core_acl_id                 number(19) not null,
  ix_core_group_id               number(19) not null,
  constraint c_33c9295d primary key (ix_core_acl_id, ix_core_group_id))
;

create table ix_ginas_controlled_vocab_core (
  ix_ginas_controlled_vocab_id   number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_4d7ef791 primary key (ix_ginas_controlled_vocab_id, ix_core_value_id))
;

create table ix_ginas_cv_terms (
  ix_ginas_controlled_vocab_id   number(19) not null,
  ix_ginas_vocabulary_term_id    number(19) not null,
  constraint c_574116d5 primary key (ix_ginas_controlled_vocab_id, ix_ginas_vocabulary_term_id))
;

create table ix_core_event_prop (
  ix_core_event_id               number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_16047919 primary key (ix_core_event_id, ix_core_value_id))
;

create table ix_core_event_link (
  ix_core_event_id               number(19) not null,
  ix_core_xref_id                number(19) not null,
  constraint c_7543a7f0 primary key (ix_core_event_id, ix_core_xref_id))
;

create table ix_ginas_reference_cit_core_va (
  ix_ginas_reference_cit_id      number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_f0384e13 primary key (ix_ginas_reference_cit_id, ix_core_value_id))
;

create table ix_core_group_principal (
  ix_core_group_id               number(19) not null,
  ix_core_principal_id           number(19) not null,
  constraint c_07dc78d7 primary key (ix_core_group_id, ix_core_principal_id))
;

create table ix_ginas_nucleicacid_subunits (
  ix_ginas_nucleicacid_uuid      varchar2(40) not null,
  ix_ginas_subunit_uuid          varchar2(40) not null,
  constraint c_c8c8deb8 primary key (ix_ginas_nucleicacid_uuid, ix_ginas_subunit_uuid))
;

create table ix_core_payload_property (
  ix_core_payload_id             varchar2(40) not null,
  ix_core_value_id               number(19) not null,
  constraint c_c4a0dc96 primary key (ix_core_payload_id, ix_core_value_id))
;

create table ix_core_predicate_object (
  ix_core_predicate_id           number(19) not null,
  ix_core_xref_id                number(19) not null,
  constraint c_f30cec27 primary key (ix_core_predicate_id, ix_core_xref_id))
;

create table ix_core_predicate_property (
  ix_core_predicate_id           number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_454745bd primary key (ix_core_predicate_id, ix_core_value_id))
;

create table ix_core_procjob_key (
  ix_core_procjob_id             number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_bba0dcd8 primary key (ix_core_procjob_id, ix_core_value_id))
;

create table ix_core_procrec_prop (
  ix_core_procrec_id             number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_b6185455 primary key (ix_core_procrec_id, ix_core_value_id))
;

create table ix_ginas_protein_subunit (
  ix_ginas_protein_uuid          varchar2(40) not null,
  ix_ginas_subunit_uuid          varchar2(40) not null,
  constraint c_d717be3b primary key (ix_ginas_protein_uuid, ix_ginas_subunit_uuid))
;

create table ix_core_publication_keyword (
  ix_core_publication_id         number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_24488528 primary key (ix_core_publication_id, ix_core_value_id))
;

create table ix_core_publication_mesh (
  ix_core_publication_id         number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_ceb05d43 primary key (ix_core_publication_id, ix_core_value_id))
;

create table ix_core_publication_author (
  ix_core_publication_id         number(19) not null,
  ix_core_pubauthor_id           number(19) not null,
  constraint c_9085162f primary key (ix_core_publication_id, ix_core_pubauthor_id))
;

create table ix_core_publication_figure (
  ix_core_publication_id         number(19) not null,
  ix_core_figure_id              number(19) not null,
  constraint c_5ec45b05 primary key (ix_core_publication_id, ix_core_figure_id))
;

create table ix_core_stitch_attribute (
  ix_core_stitch_id              number(19) not null,
  ix_core_attribute_id           number(19) not null,
  constraint c_1f370c96 primary key (ix_core_stitch_id, ix_core_attribute_id))
;

create table ix_core_structure_property (
  ix_core_structure_id           varchar2(40) not null,
  ix_core_value_id               number(19) not null,
  constraint c_110ecac9 primary key (ix_core_structure_id, ix_core_value_id))
;

create table ix_core_structure_link (
  ix_core_structure_id           varchar2(40) not null,
  ix_core_xref_id                number(19) not null,
  constraint c_0d8740b8 primary key (ix_core_structure_id, ix_core_xref_id))
;

create table ix_ginas_substance_tags (
  ix_ginas_substance_uuid        varchar2(40) not null,
  ix_core_value_id               number(19) not null,
  constraint c_ab55aa47 primary key (ix_ginas_substance_uuid, ix_core_value_id))
;

create table ix_core_timeline_event (
  ix_core_timeline_id            number(19) not null,
  ix_core_event_id               number(19) not null,
  constraint c_5087d2a7 primary key (ix_core_timeline_id, ix_core_event_id))
;

create table ix_core_userprof_prop (
  ix_core_userprof_id            number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_33499675 primary key (ix_core_userprof_id, ix_core_value_id))
;

create table ix_core_xref_property (
  ix_core_xref_id                number(19) not null,
  ix_core_value_id               number(19) not null,
  constraint c_ba26f121 primary key (ix_core_xref_id, ix_core_value_id))
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

create sequence ix_ginas_reference_cit_seq;

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

alter table ix_ginas_agentmod add constraint c_cf28c18f foreign key (created_by_id) references ix_core_principal (id);
create index i_40104612 on ix_ginas_agentmod (created_by_id);
alter table ix_ginas_agentmod add constraint c_485933c6 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_37d76ad3 on ix_ginas_agentmod (last_edited_by_id);
alter table ix_ginas_agentmod add constraint c_f4298478 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_f5b17f29 on ix_ginas_agentmod (record_reference_id);
alter table ix_ginas_agentmod add constraint c_7c82abb5 foreign key (owner_uuid) references ix_ginas_modifications (uuid);
create index i_3d3a4323 on ix_ginas_agentmod (owner_uuid);
alter table ix_ginas_agentmod add constraint c_72d0f2a0 foreign key (agent_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_4fd3a4e3 on ix_ginas_agentmod (agent_substance_uuid);
alter table ix_ginas_agentmod add constraint c_54080e63 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_4e48f90b on ix_ginas_agentmod (amount_uuid);
alter table ix_ginas_amount add constraint c_20140075 foreign key (created_by_id) references ix_core_principal (id);
create index i_d4f6da26 on ix_ginas_amount (created_by_id);
alter table ix_ginas_amount add constraint c_36581ea6 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_6b4ebc58 on ix_ginas_amount (last_edited_by_id);
alter table ix_ginas_amount add constraint c_9fe16bf1 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_13dbd840 on ix_ginas_amount (record_reference_id);
alter table ix_core_attribute add constraint c_3e31cce8 foreign key (namespace_id) references ix_core_namespace (id);
create index i_a9369a2f on ix_core_attribute (namespace_id);
alter table ix_core_backup add constraint c_e26271ea foreign key (namespace_id) references ix_core_namespace (id);
create index i_d1c48aba on ix_core_backup (namespace_id);
alter table ix_ginas_code add constraint c_975daf60 foreign key (created_by_id) references ix_core_principal (id);
create index i_3641541d on ix_ginas_code (created_by_id);
alter table ix_ginas_code add constraint c_3d7a7481 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_ef72b443 on ix_ginas_code (last_edited_by_id);
alter table ix_ginas_code add constraint c_5c6b015f foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_66ec7dff on ix_ginas_code (record_reference_id);
alter table ix_ginas_code add constraint c_853413ad foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_f3baa43d on ix_ginas_code (owner_uuid);
alter table ix_ginas_component add constraint c_e99090a6 foreign key (created_by_id) references ix_core_principal (id);
create index i_be64cb89 on ix_ginas_component (created_by_id);
alter table ix_ginas_component add constraint c_e627741b foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_b6e5c6d9 on ix_ginas_component (last_edited_by_id);
alter table ix_ginas_component add constraint c_dd26c7cf foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_a4243a97 on ix_ginas_component (record_reference_id);
alter table ix_ginas_component add constraint c_a9f00c57 foreign key (owner_uuid) references ix_ginas_mixture (uuid);
create index i_6493f751 on ix_ginas_component (owner_uuid);
alter table ix_ginas_component add constraint c_14f2d102 foreign key (substance_uuid) references ix_ginas_substanceref (uuid);
create index i_20934594 on ix_ginas_component (substance_uuid);
alter table ix_ginas_component add constraint c_6fa59df4 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_6351e1c0 on ix_ginas_component (amount_uuid);
alter table ix_ginas_controlled_vocab add constraint c_7a5d51d2 foreign key (namespace_id) references ix_core_namespace (id);
create index i_adc2e18c on ix_ginas_controlled_vocab (namespace_id);
alter table ix_core_curation add constraint c_c3a22c63 foreign key (curator_id) references ix_core_principal (id);
create index i_ca01e3b6 on ix_core_curation (curator_id);
alter table ix_core_etag add constraint c_e8bdda17 foreign key (namespace_id) references ix_core_namespace (id);
create index i_2a727bcc on ix_core_etag (namespace_id);
alter table ix_core_etagref add constraint c_568c094e foreign key (etag_id) references ix_core_etag (id);
create index i_b61dc851 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint c_c7508eff foreign key (editor_id) references ix_core_principal (id);
create index i_d0e53810 on ix_core_edit (editor_id);
alter table ix_core_figure add constraint c_e4fef427 foreign key (parent_id) references ix_core_figure (id);
create index i_b5ab4fbf on ix_core_figure (parent_id);
alter table ix_ginas_glycosylation add constraint c_ccc4fbb0 foreign key (created_by_id) references ix_core_principal (id);
create index i_09f34693 on ix_ginas_glycosylation (created_by_id);
alter table ix_ginas_glycosylation add constraint c_0fb70289 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_1ccd8ccf on ix_ginas_glycosylation (last_edited_by_id);
alter table ix_ginas_glycosylation add constraint c_67772806 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_42cb306e on ix_ginas_glycosylation (record_reference_id);
alter table ix_ginas_glycosylation add constraint c_62701e2a foreign key (c_glycosylation_sites_uuid) references ix_ginas_site_lob (uuid);
create index i_f34efb35 on ix_ginas_glycosylation (c_glycosylation_sites_uuid);
alter table ix_ginas_glycosylation add constraint c_de413a97 foreign key (n_glycosylation_sites_uuid) references ix_ginas_site_lob (uuid);
create index i_ba810b2e on ix_ginas_glycosylation (n_glycosylation_sites_uuid);
alter table ix_ginas_glycosylation add constraint c_d0f818e9 foreign key (o_glycosylation_sites_uuid) references ix_ginas_site_lob (uuid);
create index i_8bf62945 on ix_ginas_glycosylation (o_glycosylation_sites_uuid);
alter table ix_core_investigator add constraint c_816c5e1e foreign key (organization_id) references ix_core_organization (id);
create index i_f83c9ed0 on ix_core_investigator (organization_id);
alter table ix_ginas_linkage add constraint c_649686c7 foreign key (created_by_id) references ix_core_principal (id);
create index i_e8739973 on ix_ginas_linkage (created_by_id);
alter table ix_ginas_linkage add constraint c_f43e6689 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_df7d8ea3 on ix_ginas_linkage (last_edited_by_id);
alter table ix_ginas_linkage add constraint c_c86a02f1 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_33bbd500 on ix_ginas_linkage (record_reference_id);
alter table ix_ginas_linkage add constraint c_79c64ab0 foreign key (owner_uuid) references ix_ginas_nucleicacid (uuid);
create index i_85cfb040 on ix_ginas_linkage (owner_uuid);
alter table ix_ginas_linkage add constraint c_8d86271a foreign key (site_container_uuid) references ix_ginas_site_lob (uuid);
create index i_e25eba20 on ix_ginas_linkage (site_container_uuid);
alter table ix_ginas_material add constraint c_5f5eb83e foreign key (created_by_id) references ix_core_principal (id);
create index i_45faed01 on ix_ginas_material (created_by_id);
alter table ix_ginas_material add constraint c_24acf2ab foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_35a2433f on ix_ginas_material (last_edited_by_id);
alter table ix_ginas_material add constraint c_a83e3941 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_676c9e6f on ix_ginas_material (record_reference_id);
alter table ix_ginas_material add constraint c_b1fc86f6 foreign key (owner_uuid) references ix_ginas_polymer (uuid);
create index i_74073cec on ix_ginas_material (owner_uuid);
alter table ix_ginas_material add constraint c_76ceab96 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_3f48cb00 on ix_ginas_material (amount_uuid);
alter table ix_ginas_material add constraint c_43929cff foreign key (monomer_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_5c572fcd on ix_ginas_material (monomer_substance_uuid);
alter table ix_ginas_mixture add constraint c_9061d3d6 foreign key (created_by_id) references ix_core_principal (id);
create index i_c47792ae on ix_ginas_mixture (created_by_id);
alter table ix_ginas_mixture add constraint c_332a4bc6 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_f424b841 on ix_ginas_mixture (last_edited_by_id);
alter table ix_ginas_mixture add constraint c_51687b3d foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_55bf8d50 on ix_ginas_mixture (record_reference_id);
alter table ix_ginas_mixture add constraint c_c0b32caa foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_2923635e on ix_ginas_mixture (parent_substance_uuid);
alter table ix_ginas_modifications add constraint c_2bcfd4d4 foreign key (created_by_id) references ix_core_principal (id);
create index i_f906acbc on ix_ginas_modifications (created_by_id);
alter table ix_ginas_modifications add constraint c_31b01533 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_1644daf6 on ix_ginas_modifications (last_edited_by_id);
alter table ix_ginas_modifications add constraint c_4dba7547 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_c7c9a715 on ix_ginas_modifications (record_reference_id);
alter table ix_ginas_moiety add constraint c_dac5cff6 foreign key (chemical_substance_uuid) references ix_ginas_substance (uuid);
create index i_95573d34 on ix_ginas_moiety (chemical_substance_uuid);
alter table ix_ginas_moiety add constraint c_51c90d08 foreign key (created_by_id) references ix_core_principal (id);
create index i_f2e831a2 on ix_ginas_moiety (created_by_id);
alter table ix_ginas_moiety add constraint c_d4895b59 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_a3970a90 on ix_ginas_moiety (last_edited_by_id);
alter table ix_ginas_moiety add constraint c_7f58577d foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_d2935dfa on ix_ginas_moiety (record_reference_id);
alter table ix_ginas_moiety add constraint c_3fdff7ae foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_ac7963da on ix_ginas_moiety (owner_uuid);
alter table ix_ginas_moiety add constraint c_6613649c foreign key (structure_id) references ix_core_structure (id);
create index i_48c2a77d on ix_ginas_moiety (structure_id);
alter table ix_ginas_moiety add constraint c_f5eb1502 foreign key (count_uuid) references ix_ginas_amount (uuid);
create index i_211019b5 on ix_ginas_moiety (count_uuid);
alter table ix_ginas_name add constraint c_02646c0d foreign key (created_by_id) references ix_core_principal (id);
create index i_cde8aeab on ix_ginas_name (created_by_id);
alter table ix_ginas_name add constraint c_05efe407 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_9333347d on ix_ginas_name (last_edited_by_id);
alter table ix_ginas_name add constraint c_9073200c foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_1f96a7fa on ix_ginas_name (record_reference_id);
alter table ix_ginas_name add constraint c_29d6f0cd foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_36dfce0e on ix_ginas_name (owner_uuid);
alter table ix_ginas_nameorg add constraint c_ccf9051b foreign key (created_by_id) references ix_core_principal (id);
create index i_4626ae62 on ix_ginas_nameorg (created_by_id);
alter table ix_ginas_nameorg add constraint c_32d0ea5c foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_0cebb8c7 on ix_ginas_nameorg (last_edited_by_id);
alter table ix_ginas_nameorg add constraint c_2f502ac7 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_31fe7d42 on ix_ginas_nameorg (record_reference_id);
alter table ix_ginas_nameorg add constraint c_00dfdb4d foreign key (owner_uuid) references ix_ginas_name (uuid);
create index i_7e1e5c31 on ix_ginas_nameorg (owner_uuid);
alter table ix_core_namespace add constraint c_07245f9e foreign key (owner_id) references ix_core_principal (id);
create index i_ef6e1faf on ix_core_namespace (owner_id);
alter table ix_ginas_note add constraint c_0f89596b foreign key (created_by_id) references ix_core_principal (id);
create index i_9dc45b30 on ix_ginas_note (created_by_id);
alter table ix_ginas_note add constraint c_7342e2d9 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_cb73ed44 on ix_ginas_note (last_edited_by_id);
alter table ix_ginas_note add constraint c_d7c87537 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_5c0e8b1b on ix_ginas_note (record_reference_id);
alter table ix_ginas_note add constraint c_790f67ec foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_6d05dce4 on ix_ginas_note (owner_uuid);
alter table ix_ginas_nucleicacid add constraint c_9b2bec8f foreign key (created_by_id) references ix_core_principal (id);
create index i_e9c1d034 on ix_ginas_nucleicacid (created_by_id);
alter table ix_ginas_nucleicacid add constraint c_64a1c3ed foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_50c4ba5f on ix_ginas_nucleicacid (last_edited_by_id);
alter table ix_ginas_nucleicacid add constraint c_0cee3c99 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_1010df1e on ix_ginas_nucleicacid (record_reference_id);
alter table ix_ginas_nucleicacid add constraint c_fdb95156 foreign key (modifications_uuid) references ix_ginas_modifications (uuid);
create index i_1fb2d1c9 on ix_ginas_nucleicacid (modifications_uuid);
alter table ix_ginas_otherlinks add constraint c_fb97994d foreign key (created_by_id) references ix_core_principal (id);
create index i_58eedc30 on ix_ginas_otherlinks (created_by_id);
alter table ix_ginas_otherlinks add constraint c_80bd3959 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_efe481d0 on ix_ginas_otherlinks (last_edited_by_id);
alter table ix_ginas_otherlinks add constraint c_51e36982 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_d639c2c1 on ix_ginas_otherlinks (record_reference_id);
alter table ix_ginas_otherlinks add constraint c_ca56a77b foreign key (owner_uuid) references ix_ginas_protein (uuid);
create index i_dd56f270 on ix_ginas_otherlinks (owner_uuid);
alter table ix_ginas_otherlinks add constraint c_059999d4 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid);
create index i_7dab77a1 on ix_ginas_otherlinks (site_container_uuid);
alter table ix_ginas_parameter add constraint c_1537abc0 foreign key (created_by_id) references ix_core_principal (id);
create index i_df37b26e on ix_ginas_parameter (created_by_id);
alter table ix_ginas_parameter add constraint c_371f5a25 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_5edd82f9 on ix_ginas_parameter (last_edited_by_id);
alter table ix_ginas_parameter add constraint c_c8a41b63 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_69876a2f on ix_ginas_parameter (record_reference_id);
alter table ix_ginas_parameter add constraint c_15d81602 foreign key (owner_uuid) references ix_ginas_property (uuid);
create index i_30081462 on ix_ginas_parameter (owner_uuid);
alter table ix_ginas_parameter add constraint c_cbf85e13 foreign key (value_uuid) references ix_ginas_amount (uuid);
create index i_3c60f546 on ix_ginas_parameter (value_uuid);
alter table ix_core_payload add constraint c_4ce35870 foreign key (namespace_id) references ix_core_namespace (id);
create index i_b694cffb on ix_core_payload (namespace_id);
alter table ix_ginas_physicalmod add constraint c_91079f8c foreign key (created_by_id) references ix_core_principal (id);
create index i_8a67a02a on ix_ginas_physicalmod (created_by_id);
alter table ix_ginas_physicalmod add constraint c_29c58355 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_690865cb on ix_ginas_physicalmod (last_edited_by_id);
alter table ix_ginas_physicalmod add constraint c_9b82a913 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_21f5dda3 on ix_ginas_physicalmod (record_reference_id);
alter table ix_ginas_physicalmod add constraint c_c89665e4 foreign key (owner_uuid) references ix_ginas_modifications (uuid);
create index i_c4263c3a on ix_ginas_physicalmod (owner_uuid);
alter table ix_ginas_physicalpar add constraint c_e5938a16 foreign key (created_by_id) references ix_core_principal (id);
create index i_17056de7 on ix_ginas_physicalpar (created_by_id);
alter table ix_ginas_physicalpar add constraint c_2dfa08da foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_5194fda6 on ix_ginas_physicalpar (last_edited_by_id);
alter table ix_ginas_physicalpar add constraint c_7993e55c foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_da24d48b on ix_ginas_physicalpar (record_reference_id);
alter table ix_ginas_physicalpar add constraint c_0e18f1da foreign key (owner_uuid) references ix_ginas_physicalmod (uuid);
create index i_528e7094 on ix_ginas_physicalpar (owner_uuid);
alter table ix_ginas_physicalpar add constraint c_38355925 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_dbbbe9cc on ix_ginas_physicalpar (amount_uuid);
alter table ix_ginas_polymer add constraint c_e606cb8a foreign key (created_by_id) references ix_core_principal (id);
create index i_f574fa13 on ix_ginas_polymer (created_by_id);
alter table ix_ginas_polymer add constraint c_7360a632 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_31d6f1a9 on ix_ginas_polymer (last_edited_by_id);
alter table ix_ginas_polymer add constraint c_00a77c52 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_d7c60dd1 on ix_ginas_polymer (record_reference_id);
alter table ix_ginas_polymer add constraint c_e6727f22 foreign key (classification_uuid) references polymer_classification (uuid);
create index i_f2169006 on ix_ginas_polymer (classification_uuid);
alter table ix_ginas_polymer add constraint c_00a33518 foreign key (display_structure_id) references ix_core_structure (id);
create index i_73134ad5 on ix_ginas_polymer (display_structure_id);
alter table ix_ginas_polymer add constraint c_4952c82c foreign key (idealized_structure_id) references ix_core_structure (id);
create index i_4114c4ea on ix_ginas_polymer (idealized_structure_id);
alter table polymer_classification add constraint c_40f04904 foreign key (created_by_id) references ix_core_principal (id);
create index i_177560f7 on polymer_classification (created_by_id);
alter table polymer_classification add constraint c_b9b49fe1 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_fa52488d on polymer_classification (last_edited_by_id);
alter table polymer_classification add constraint c_9660c876 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_44fe9982 on polymer_classification (record_reference_id);
alter table polymer_classification add constraint c_ba754e17 foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_604e0e10 on polymer_classification (parent_substance_uuid);
alter table ix_core_predicate add constraint c_5fe0a350 foreign key (namespace_id) references ix_core_namespace (id);
create index i_3f023d7e on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint c_7ebf98d3 foreign key (subject_id) references ix_core_xref (id);
create index i_47d51358 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint c_d29ca08e foreign key (namespace_id) references ix_core_namespace (id);
create index i_a283ed71 on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint c_9644ccf6 foreign key (selfie_id) references ix_core_figure (id);
create index i_7ceabf10 on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint c_fab6d8f5 foreign key (institution_id) references ix_core_organization (id);
create index i_89399bc7 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint c_1e40e53d foreign key (owner_id) references ix_core_principal (id);
create index i_acb733a7 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint c_917ba167 foreign key (payload_id) references ix_core_payload (id);
create index i_ba98a325 on ix_core_procjob (payload_id);
alter table ix_core_procrec add constraint c_ce94911b foreign key (xref_id) references ix_core_xref (id);
create index i_29b53ea7 on ix_core_procrec (xref_id);
alter table ix_core_procrec add constraint c_c1339957 foreign key (job_id) references ix_core_procjob (id);
create index i_4cb51c05 on ix_core_procrec (job_id);
alter table ix_ginas_property add constraint c_3e27734f foreign key (created_by_id) references ix_core_principal (id);
create index i_f3d7cb4d on ix_ginas_property (created_by_id);
alter table ix_ginas_property add constraint c_c22b35e0 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_1019ae9f on ix_ginas_property (last_edited_by_id);
alter table ix_ginas_property add constraint c_3181be0b foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_f7808297 on ix_ginas_property (record_reference_id);
alter table ix_ginas_property add constraint c_f2be18ef foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_bc401b26 on ix_ginas_property (owner_uuid);
alter table ix_ginas_property add constraint c_f4187878 foreign key (value_uuid) references ix_ginas_amount (uuid);
create index i_34cebac3 on ix_ginas_property (value_uuid);
alter table ix_ginas_protein add constraint c_f361adfc foreign key (created_by_id) references ix_core_principal (id);
create index i_ee5ca625 on ix_ginas_protein (created_by_id);
alter table ix_ginas_protein add constraint c_84d731e6 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_b05e5485 on ix_ginas_protein (last_edited_by_id);
alter table ix_ginas_protein add constraint c_c4678a70 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_8c3925e3 on ix_ginas_protein (record_reference_id);
alter table ix_ginas_protein add constraint c_5015ea60 foreign key (glycosylation_uuid) references ix_ginas_glycosylation (uuid);
create index i_4f9e5804 on ix_ginas_protein (glycosylation_uuid);
alter table ix_ginas_protein add constraint c_a3dcbee5 foreign key (modifications_uuid) references ix_ginas_modifications (uuid);
create index i_797d941e on ix_ginas_protein (modifications_uuid);
alter table ix_core_pubauthor add constraint c_4328b855 foreign key (author_id) references ix_core_principal (id);
create index i_d63b7b40 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint c_2f27e279 foreign key (journal_id) references ix_core_journal (id);
create index i_19246e48 on ix_core_publication (journal_id);
alter table ix_ginas_reference add constraint c_2a865464 foreign key (created_by_id) references ix_core_principal (id);
create index i_e6860ce8 on ix_ginas_reference (created_by_id);
alter table ix_ginas_reference add constraint c_8d8ddf9f foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_02d79400 on ix_ginas_reference (last_edited_by_id);
alter table ix_ginas_reference add constraint c_cad020a5 foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_309e4f12 on ix_ginas_reference (owner_uuid);
alter table ix_ginas_relationship add constraint c_bb25f9ca foreign key (created_by_id) references ix_core_principal (id);
create index i_ea36a678 on ix_ginas_relationship (created_by_id);
alter table ix_ginas_relationship add constraint c_35b0ad54 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_811543e9 on ix_ginas_relationship (last_edited_by_id);
alter table ix_ginas_relationship add constraint c_293f7d3e foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_b9afea6a on ix_ginas_relationship (record_reference_id);
alter table ix_ginas_relationship add constraint c_d3266069 foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_0266db31 on ix_ginas_relationship (owner_uuid);
alter table ix_ginas_relationship add constraint c_56021fd4 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_b9713ecd on ix_ginas_relationship (amount_uuid);
alter table ix_ginas_relationship add constraint c_24888cbc foreign key (related_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_992d22dc on ix_ginas_relationship (related_substance_uuid);
alter table ix_ginas_relationship add constraint c_0475a8d2 foreign key (mediator_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_167c5fcc on ix_ginas_relationship (mediator_substance_uuid);
alter table ix_core_session add constraint c_5c14ee2c foreign key (profile_id) references ix_core_userprof (id);
create index i_acfed580 on ix_core_session (profile_id);
alter table ix_ginas_site_lob add constraint c_288533ad foreign key (created_by_id) references ix_core_principal (id);
create index i_c4c83317 on ix_ginas_site_lob (created_by_id);
alter table ix_ginas_site_lob add constraint c_a30ba1e0 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_f5140a00 on ix_ginas_site_lob (last_edited_by_id);
alter table ix_ginas_site_lob add constraint c_60f6bf28 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_01b3060b on ix_ginas_site_lob (record_reference_id);
alter table ix_ginas_ssg1 add constraint c_7bbd1ba2 foreign key (created_by_id) references ix_core_principal (id);
create index i_2a42ecb1 on ix_ginas_ssg1 (created_by_id);
alter table ix_ginas_ssg1 add constraint c_b2f1be4c foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_7c05538c on ix_ginas_ssg1 (last_edited_by_id);
alter table ix_ginas_ssg1 add constraint c_071f0531 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_cf9ed6de on ix_ginas_ssg1 (record_reference_id);
alter table ix_ginas_structuralmod add constraint c_3c405236 foreign key (created_by_id) references ix_core_principal (id);
create index i_11f38afe on ix_ginas_structuralmod (created_by_id);
alter table ix_ginas_structuralmod add constraint c_53ca3638 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_5df8155b on ix_ginas_structuralmod (last_edited_by_id);
alter table ix_ginas_structuralmod add constraint c_ecf9f511 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_e1f35551 on ix_ginas_structuralmod (record_reference_id);
alter table ix_ginas_structuralmod add constraint c_3d7e392a foreign key (owner_uuid) references ix_ginas_modifications (uuid);
create index i_bb7261c4 on ix_ginas_structuralmod (owner_uuid);
alter table ix_ginas_structuralmod add constraint c_ac356e74 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid);
create index i_a23427ba on ix_ginas_structuralmod (site_container_uuid);
alter table ix_ginas_structuralmod add constraint c_86118187 foreign key (extent_amount_uuid) references ix_ginas_amount (uuid);
create index i_26e4ce83 on ix_ginas_structuralmod (extent_amount_uuid);
alter table ix_ginas_structuralmod add constraint c_e8a865a8 foreign key (molecular_fragment_uuid) references ix_ginas_substanceref (uuid);
create index i_1ef24f87 on ix_ginas_structuralmod (molecular_fragment_uuid);
alter table ix_ginas_strucdiv add constraint c_d909a9d0 foreign key (created_by_id) references ix_core_principal (id);
create index i_342fa032 on ix_ginas_strucdiv (created_by_id);
alter table ix_ginas_strucdiv add constraint c_bd0cf319 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_ad8ecc47 on ix_ginas_strucdiv (last_edited_by_id);
alter table ix_ginas_strucdiv add constraint c_7613dc8c foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_b3217eba on ix_ginas_strucdiv (record_reference_id);
alter table ix_ginas_strucdiv add constraint c_221aa9d8 foreign key (paternal_uuid) references ix_ginas_substanceref (uuid);
create index i_3c1e7d32 on ix_ginas_strucdiv (paternal_uuid);
alter table ix_ginas_strucdiv add constraint c_c62dfebf foreign key (maternal_uuid) references ix_ginas_substanceref (uuid);
create index i_d6849d4d on ix_ginas_strucdiv (maternal_uuid);
alter table ix_ginas_strucdiv add constraint c_249b8801 foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_910cfa58 on ix_ginas_strucdiv (parent_substance_uuid);
alter table ix_core_structure add constraint c_3120f6ed foreign key (created_by_id) references ix_core_principal (id);
create index i_4e904402 on ix_core_structure (created_by_id);
alter table ix_core_structure add constraint c_da89d943 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_716c75d7 on ix_core_structure (last_edited_by_id);
alter table ix_core_structure add constraint c_2cb64d27 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_91d40971 on ix_core_structure (record_reference_id);
alter table ix_ginas_substance add constraint c_fc9b7e01 foreign key (created_by_id) references ix_core_principal (id);
create index i_39b7b037 on ix_ginas_substance (created_by_id);
alter table ix_ginas_substance add constraint c_e3530c39 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_9814ff75 on ix_ginas_substance (last_edited_by_id);
alter table ix_ginas_substance add constraint c_9ef20d49 foreign key (approved_by_id) references ix_core_principal (id);
create index i_f3cf4093 on ix_ginas_substance (approved_by_id);
alter table ix_ginas_substance add constraint c_c6903b12 foreign key (modifications_uuid) references ix_ginas_modifications (uuid);
create index i_5b18ec21 on ix_ginas_substance (modifications_uuid);
alter table ix_ginas_substance add constraint c_f405533e foreign key (structure_id) references ix_core_structure (id);
create index i_6fb58813 on ix_ginas_substance (structure_id);
alter table ix_ginas_substance add constraint c_6aace08a foreign key (mixture_uuid) references ix_ginas_mixture (uuid);
create index i_2ce991b9 on ix_ginas_substance (mixture_uuid);
alter table ix_ginas_substance add constraint c_66ff50ce foreign key (nucleic_acid_uuid) references ix_ginas_nucleicacid (uuid);
create index i_5ebe1e5f on ix_ginas_substance (nucleic_acid_uuid);
alter table ix_ginas_substance add constraint c_cac7dadb foreign key (polymer_uuid) references ix_ginas_polymer (uuid);
create index i_3eea402d on ix_ginas_substance (polymer_uuid);
alter table ix_ginas_substance add constraint c_becbcf58 foreign key (protein_uuid) references ix_ginas_protein (uuid);
create index i_dd5dfd1f on ix_ginas_substance (protein_uuid);
alter table ix_ginas_substance add constraint c_4c6d4b8b foreign key (specified_substance_uuid) references ix_ginas_ssg1 (uuid);
create index i_9fa608e3 on ix_ginas_substance (specified_substance_uuid);
alter table ix_ginas_substance add constraint c_8395cdfa foreign key (structurally_diverse_uuid) references ix_ginas_strucdiv (uuid);
create index i_cfeba938 on ix_ginas_substance (structurally_diverse_uuid);
alter table ix_ginas_substanceref add constraint c_23ffd682 foreign key (created_by_id) references ix_core_principal (id);
create index i_e4a3f870 on ix_ginas_substanceref (created_by_id);
alter table ix_ginas_substanceref add constraint c_5fc8a5c8 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_a237ee08 on ix_ginas_substanceref (last_edited_by_id);
alter table ix_ginas_substanceref add constraint c_d2ac68c4 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_ca998ec0 on ix_ginas_substanceref (record_reference_id);
alter table ix_ginas_subunit add constraint c_914705fb foreign key (created_by_id) references ix_core_principal (id);
create index i_ef8b93fa on ix_ginas_subunit (created_by_id);
alter table ix_ginas_subunit add constraint c_9fdbc9d8 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_559416ef on ix_ginas_subunit (last_edited_by_id);
alter table ix_ginas_subunit add constraint c_cbc41a7d foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_09539c28 on ix_ginas_subunit (record_reference_id);
alter table ix_ginas_sugar add constraint c_199db735 foreign key (created_by_id) references ix_core_principal (id);
create index i_72931c77 on ix_ginas_sugar (created_by_id);
alter table ix_ginas_sugar add constraint c_aad6ddea foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_e5bb93d4 on ix_ginas_sugar (last_edited_by_id);
alter table ix_ginas_sugar add constraint c_9e96e80c foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_6b84f684 on ix_ginas_sugar (record_reference_id);
alter table ix_ginas_sugar add constraint c_08effe90 foreign key (owner_uuid) references ix_ginas_nucleicacid (uuid);
create index i_5b7eb5a9 on ix_ginas_sugar (owner_uuid);
alter table ix_ginas_sugar add constraint c_23739de7 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid);
create index i_41cf1fcd on ix_ginas_sugar (site_container_uuid);
alter table ix_ginas_unit add constraint c_cb37ef97 foreign key (created_by_id) references ix_core_principal (id);
create index i_eb63495d on ix_ginas_unit (created_by_id);
alter table ix_ginas_unit add constraint c_aad6a47d foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_496f2fe0 on ix_ginas_unit (last_edited_by_id);
alter table ix_ginas_unit add constraint c_ac067109 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_5ad398ed on ix_ginas_unit (record_reference_id);
alter table ix_ginas_unit add constraint c_20347d8f foreign key (owner_uuid) references ix_ginas_polymer (uuid);
create index i_4401919c on ix_ginas_unit (owner_uuid);
alter table ix_ginas_unit add constraint c_6c057874 foreign key (amap_id) references ix_core_value (id);
create index i_176dfa73 on ix_ginas_unit (amap_id);
alter table ix_ginas_unit add constraint c_e7af4999 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_8fd8c916 on ix_ginas_unit (amount_uuid);
alter table ix_core_userprof add constraint c_8176849c foreign key (namespace_id) references ix_core_namespace (id);
create index i_3c5c7ee3 on ix_core_userprof (namespace_id);
alter table ix_core_userprof add constraint c_4da99a6b foreign key (user_id) references ix_core_principal (id);
create index i_11c516eb on ix_core_userprof (user_id);
alter table ix_ginas_vocabulary_term add constraint c_1c11f047 foreign key (namespace_id) references ix_core_namespace (id);
create index i_79cce622 on ix_ginas_vocabulary_term (namespace_id);
alter table ix_core_xref add constraint c_76369a3a foreign key (namespace_id) references ix_core_namespace (id);
create index i_04e48418 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint c_deeb9ed8 foreign key (ix_core_acl_id) references ix_core_acl (id);

alter table ix_core_acl_principal add constraint c_2682a8a2 foreign key (ix_core_principal_id) references ix_core_principal (id);

alter table ix_core_acl_group add constraint c_4d670747 foreign key (ix_core_acl_id) references ix_core_acl (id);

alter table ix_core_acl_group add constraint c_d35484d1 foreign key (ix_core_group_id) references ix_core_group (id);

alter table ix_ginas_controlled_vocab_core add constraint c_43f2af66 foreign key (ix_ginas_controlled_vocab_id) references ix_ginas_controlled_vocab (id);

alter table ix_ginas_controlled_vocab_core add constraint c_12ca4bf2 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_ginas_cv_terms add constraint c_63b63088 foreign key (ix_ginas_controlled_vocab_id) references ix_ginas_controlled_vocab (id);

alter table ix_ginas_cv_terms add constraint c_047b7e72 foreign key (ix_ginas_vocabulary_term_id) references ix_ginas_vocabulary_term (id);

alter table ix_core_event_prop add constraint c_64454b09 foreign key (ix_core_event_id) references ix_core_event (id);

alter table ix_core_event_prop add constraint c_6d6af509 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_event_link add constraint c_e3942957 foreign key (ix_core_event_id) references ix_core_event (id);

alter table ix_core_event_link add constraint c_7425ab47 foreign key (ix_core_xref_id) references ix_core_xref (id);

alter table ix_ginas_reference_cit_core_va add constraint c_60d03ddc foreign key (ix_ginas_reference_cit_id) references ix_ginas_reference_cit (id);

alter table ix_ginas_reference_cit_core_va add constraint c_834bd4f5 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_group_principal add constraint c_578d58ad foreign key (ix_core_group_id) references ix_core_group (id);

alter table ix_core_group_principal add constraint c_3c2e01b2 foreign key (ix_core_principal_id) references ix_core_principal (id);

alter table ix_ginas_nucleicacid_subunits add constraint c_86b4da1e foreign key (ix_ginas_nucleicacid_uuid) references ix_ginas_nucleicacid (uuid);

alter table ix_ginas_nucleicacid_subunits add constraint c_4ec43b2f foreign key (ix_ginas_subunit_uuid) references ix_ginas_subunit (uuid);

alter table ix_core_payload_property add constraint c_6521abbd foreign key (ix_core_payload_id) references ix_core_payload (id);

alter table ix_core_payload_property add constraint c_d95b690e foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_predicate_object add constraint c_641bd567 foreign key (ix_core_predicate_id) references ix_core_predicate (id);

alter table ix_core_predicate_object add constraint c_6adb2925 foreign key (ix_core_xref_id) references ix_core_xref (id);

alter table ix_core_predicate_property add constraint c_cc70de5e foreign key (ix_core_predicate_id) references ix_core_predicate (id);

alter table ix_core_predicate_property add constraint c_dc109698 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_procjob_key add constraint c_66b33587 foreign key (ix_core_procjob_id) references ix_core_procjob (id);

alter table ix_core_procjob_key add constraint c_5621e5ec foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_procrec_prop add constraint c_bc4a3457 foreign key (ix_core_procrec_id) references ix_core_procrec (id);

alter table ix_core_procrec_prop add constraint c_1ea928b9 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_ginas_protein_subunit add constraint c_32007e39 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid);

alter table ix_ginas_protein_subunit add constraint c_77c360f0 foreign key (ix_ginas_subunit_uuid) references ix_ginas_subunit (uuid);

alter table ix_core_publication_keyword add constraint c_c8ca19b6 foreign key (ix_core_publication_id) references ix_core_publication (id);

alter table ix_core_publication_keyword add constraint c_85d68489 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_publication_mesh add constraint c_3275cdf2 foreign key (ix_core_publication_id) references ix_core_publication (id);

alter table ix_core_publication_mesh add constraint c_a018f859 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_publication_author add constraint c_c8d06282 foreign key (ix_core_publication_id) references ix_core_publication (id);

alter table ix_core_publication_author add constraint c_ac730b60 foreign key (ix_core_pubauthor_id) references ix_core_pubauthor (id);

alter table ix_core_publication_figure add constraint c_e8cdf361 foreign key (ix_core_publication_id) references ix_core_publication (id);

alter table ix_core_publication_figure add constraint c_dc463e37 foreign key (ix_core_figure_id) references ix_core_figure (id);

alter table ix_core_stitch_attribute add constraint c_22d6b19d foreign key (ix_core_stitch_id) references ix_core_stitch (id);

alter table ix_core_stitch_attribute add constraint c_2fcbfec0 foreign key (ix_core_attribute_id) references ix_core_attribute (id);

alter table ix_core_structure_property add constraint c_55065d17 foreign key (ix_core_structure_id) references ix_core_structure (id);

alter table ix_core_structure_property add constraint c_5676973e foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_structure_link add constraint c_afe0129f foreign key (ix_core_structure_id) references ix_core_structure (id);

alter table ix_core_structure_link add constraint c_59b95213 foreign key (ix_core_xref_id) references ix_core_xref (id);

alter table ix_ginas_substance_tags add constraint c_bcf37c5e foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid);

alter table ix_ginas_substance_tags add constraint c_edccdc2d foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_timeline_event add constraint c_35ce9e02 foreign key (ix_core_timeline_id) references ix_core_timeline (id);

alter table ix_core_timeline_event add constraint c_b9a9c043 foreign key (ix_core_event_id) references ix_core_event (id);

alter table ix_core_userprof_prop add constraint c_1e9aa8b0 foreign key (ix_core_userprof_id) references ix_core_userprof (id);

alter table ix_core_userprof_prop add constraint c_678f1797 foreign key (ix_core_value_id) references ix_core_value (id);

alter table ix_core_xref_property add constraint c_937a13ba foreign key (ix_core_xref_id) references ix_core_xref (id);

alter table ix_core_xref_property add constraint c_c354f21a foreign key (ix_core_value_id) references ix_core_value (id);

CREATE INDEX xref_refid_index on ix_core_xref (refid);
CREATE INDEX xref_kind_index on ix_core_xref (kind);
CREATE INDEX value_label_index on ix_core_value (label);
CREATE INDEX value_term_index on ix_core_value (term);
CREATE INDEX sub_approval_index on ix_ginas_substance (approval_id);
CREATE INDEX name_index on ix_ginas_name (name);
CREATE INDEX code_index on ix_ginas_code (code);
CREATE INDEX ref_id_index on ix_ginas_reference (id);
CREATE INDEX interaction_index on ix_ginas_relationship (interaction_type);
CREATE INDEX qualification_index on ix_ginas_relationship (qualification);
CREATE INDEX type_index on ix_ginas_relationship (type);
# --- !Downs

drop table ix_core_acl cascade constraints purge;

drop table ix_core_acl_principal cascade constraints purge;

drop table ix_core_acl_group cascade constraints purge;

drop table ix_ginas_agentmod cascade constraints purge;

drop table ix_ginas_amount cascade constraints purge;

drop table ix_core_attribute cascade constraints purge;

drop table ix_core_backup cascade constraints purge;

drop table ix_ginas_code cascade constraints purge;

drop table ix_ginas_component cascade constraints purge;

drop table ix_ginas_controlled_vocab cascade constraints purge;

drop table ix_ginas_controlled_vocab_core cascade constraints purge;

drop table ix_ginas_cv_terms cascade constraints purge;

drop table ix_core_curation cascade constraints purge;

drop table ix_core_etag cascade constraints purge;

drop table ix_core_etagref cascade constraints purge;

drop table ix_core_edit cascade constraints purge;

drop table ix_core_event cascade constraints purge;

drop table ix_core_event_prop cascade constraints purge;

drop table ix_core_event_link cascade constraints purge;

drop table ix_core_figure cascade constraints purge;

drop table ix_core_filedata cascade constraints purge;

drop table ix_ginas_reference_cit cascade constraints purge;

drop table ix_ginas_reference_cit_core_va cascade constraints purge;

drop table ix_ginas_glycosylation cascade constraints purge;

drop table ix_core_group cascade constraints purge;

drop table ix_core_group_principal cascade constraints purge;

drop table ix_core_investigator cascade constraints purge;

drop table ix_core_journal cascade constraints purge;

drop table ix_ginas_linkage cascade constraints purge;

drop table ix_ginas_material cascade constraints purge;

drop table ix_ginas_mixture cascade constraints purge;

drop table ix_ginas_modifications cascade constraints purge;

drop table ix_ginas_moiety cascade constraints purge;

drop table ix_ginas_name cascade constraints purge;

drop table ix_ginas_nameorg cascade constraints purge;

drop table ix_core_namespace cascade constraints purge;

drop table ix_ginas_note cascade constraints purge;

drop table ix_ginas_nucleicacid cascade constraints purge;

drop table ix_ginas_nucleicacid_subunits cascade constraints purge;

drop table ix_core_organization cascade constraints purge;

drop table ix_ginas_otherlinks cascade constraints purge;

drop table ix_ginas_parameter cascade constraints purge;

drop table ix_core_payload cascade constraints purge;

drop table ix_core_payload_property cascade constraints purge;

drop table ix_ginas_physicalmod cascade constraints purge;

drop table ix_ginas_physicalpar cascade constraints purge;

drop table ix_ginas_polymer cascade constraints purge;

drop table polymer_classification cascade constraints purge;

drop table ix_core_predicate cascade constraints purge;

drop table ix_core_predicate_object cascade constraints purge;

drop table ix_core_predicate_property cascade constraints purge;

drop table ix_core_principal cascade constraints purge;

drop table ix_core_procjob cascade constraints purge;

drop table ix_core_procjob_key cascade constraints purge;

drop table ix_core_procrec cascade constraints purge;

drop table ix_core_procrec_prop cascade constraints purge;

drop table ix_ginas_property cascade constraints purge;

drop table ix_ginas_protein cascade constraints purge;

drop table ix_ginas_protein_subunit cascade constraints purge;

drop table ix_core_pubauthor cascade constraints purge;

drop table ix_core_publication cascade constraints purge;

drop table ix_core_publication_keyword cascade constraints purge;

drop table ix_core_publication_mesh cascade constraints purge;

drop table ix_core_publication_author cascade constraints purge;

drop table ix_core_publication_figure cascade constraints purge;

drop table ix_ginas_reference cascade constraints purge;

drop table ix_ginas_relationship cascade constraints purge;

drop table ix_core_session cascade constraints purge;

drop table ix_ginas_site_lob cascade constraints purge;

drop table ix_ginas_ssg1 cascade constraints purge;

drop table ix_core_stitch cascade constraints purge;

drop table ix_core_stitch_attribute cascade constraints purge;

drop table ix_ginas_structuralmod cascade constraints purge;

drop table ix_ginas_strucdiv cascade constraints purge;

drop table ix_core_structure cascade constraints purge;

drop table ix_core_structure_property cascade constraints purge;

drop table ix_core_structure_link cascade constraints purge;

drop table ix_ginas_substance cascade constraints purge;

drop table ix_ginas_substance_tags cascade constraints purge;

drop table ix_ginas_substanceref cascade constraints purge;

drop table ix_ginas_subunit cascade constraints purge;

drop table ix_ginas_sugar cascade constraints purge;

drop table ix_core_timeline cascade constraints purge;

drop table ix_core_timeline_event cascade constraints purge;

drop table ix_ginas_unit cascade constraints purge;

drop table ix_core_userprof cascade constraints purge;

drop table ix_core_userprof_prop cascade constraints purge;

drop table ix_core_value cascade constraints purge;

drop table ix_ginas_vocabulary_term cascade constraints purge;

drop table ix_core_xref cascade constraints purge;

drop table ix_core_xref_property cascade constraints purge;

drop sequence ix_core_acl_seq;

drop sequence ix_core_attribute_seq;

drop sequence ix_core_backup_seq;

drop sequence ix_ginas_controlled_vocab_seq;

drop sequence ix_core_curation_seq;

drop sequence ix_core_etag_seq;

drop sequence ix_core_etagref_seq;

drop sequence ix_core_event_seq;

drop sequence ix_core_figure_seq;

drop sequence ix_ginas_reference_cit_seq;

drop sequence ix_core_group_seq;

drop sequence ix_core_investigator_seq;

drop sequence ix_core_journal_seq;

drop sequence ix_core_namespace_seq;

drop sequence ix_core_organization_seq;

drop sequence ix_core_predicate_seq;

drop sequence ix_core_principal_seq;

drop sequence ix_core_procjob_seq;

drop sequence ix_core_procrec_seq;

drop sequence ix_core_pubauthor_seq;

drop sequence ix_core_publication_seq;

drop sequence ix_core_stitch_seq;

drop sequence ix_core_timeline_seq;

drop sequence ix_core_userprof_seq;

drop sequence ix_core_value_seq;

drop sequence ix_ginas_vocabulary_term_seq;

drop sequence ix_core_xref_seq;

