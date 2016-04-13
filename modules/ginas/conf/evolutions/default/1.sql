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
  filterable                number(1),
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
  owner_id                  number(19),
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
alter table ix_ginas_moiety add constraint c_e173b1a0 foreign key (created_by_id) references ix_core_principal (id);
create index i_11c6b862 on ix_ginas_moiety (created_by_id);
alter table ix_ginas_moiety add constraint c_ef77b0fe foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_10dfc25e on ix_ginas_moiety (last_edited_by_id);
alter table ix_ginas_moiety add constraint c_5c136b9a foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_46706d69 on ix_ginas_moiety (record_reference_id);
alter table ix_ginas_moiety add constraint c_02168839 foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_7116fc0f on ix_ginas_moiety (owner_uuid);
alter table ix_ginas_moiety add constraint c_49584cf1 foreign key (structure_id) references ix_core_structure (id);
create index i_88eb2394 on ix_ginas_moiety (structure_id);
alter table ix_ginas_moiety add constraint c_9c36ba97 foreign key (count_uuid) references ix_ginas_amount (uuid);
create index i_5a4a2cda on ix_ginas_moiety (count_uuid);
alter table ix_ginas_name add constraint c_dcaa364a foreign key (created_by_id) references ix_core_principal (id);
create index i_f8253562 on ix_ginas_name (created_by_id);
alter table ix_ginas_name add constraint c_8d01a8d1 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_9fedadf8 on ix_ginas_name (last_edited_by_id);
alter table ix_ginas_name add constraint c_4be6cc53 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_d07be7cb on ix_ginas_name (record_reference_id);
alter table ix_ginas_name add constraint c_f4e84ea4 foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_89e397b5 on ix_ginas_name (owner_uuid);
alter table ix_ginas_nameorg add constraint c_01a301dd foreign key (created_by_id) references ix_core_principal (id);
create index i_9b8c7ba8 on ix_ginas_nameorg (created_by_id);
alter table ix_ginas_nameorg add constraint c_fbf15208 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_5491220b on ix_ginas_nameorg (last_edited_by_id);
alter table ix_ginas_nameorg add constraint c_d53b4f21 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_83b43079 on ix_ginas_nameorg (record_reference_id);
alter table ix_ginas_nameorg add constraint c_0bf45b6e foreign key (owner_uuid) references ix_ginas_name (uuid);
create index i_89e2f6b8 on ix_ginas_nameorg (owner_uuid);
alter table ix_core_namespace add constraint c_acaf9788 foreign key (owner_id) references ix_core_principal (id);
create index i_68ec94ba on ix_core_namespace (owner_id);
alter table ix_ginas_note add constraint c_4dfee42a foreign key (created_by_id) references ix_core_principal (id);
create index i_fd9a575e on ix_ginas_note (created_by_id);
alter table ix_ginas_note add constraint c_63af21ab foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_b23f3e2f on ix_ginas_note (last_edited_by_id);
alter table ix_ginas_note add constraint c_5cfd42f3 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_00edceae on ix_ginas_note (record_reference_id);
alter table ix_ginas_note add constraint c_56be9e69 foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_9a8ea9d1 on ix_ginas_note (owner_uuid);
alter table ix_ginas_nucleicacid add constraint c_e19cd203 foreign key (created_by_id) references ix_core_principal (id);
create index i_6457c219 on ix_ginas_nucleicacid (created_by_id);
alter table ix_ginas_nucleicacid add constraint c_b6f46f48 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_cc90df6b on ix_ginas_nucleicacid (last_edited_by_id);
alter table ix_ginas_nucleicacid add constraint c_01157d2a foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_c69cbf2b on ix_ginas_nucleicacid (record_reference_id);
alter table ix_ginas_nucleicacid add constraint c_af5baadf foreign key (modifications_uuid) references ix_ginas_modifications (uuid);
create index i_2777bffa on ix_ginas_nucleicacid (modifications_uuid);
alter table ix_ginas_otherlinks add constraint c_09d193f8 foreign key (created_by_id) references ix_core_principal (id);
create index i_5aa1a74e on ix_ginas_otherlinks (created_by_id);
alter table ix_ginas_otherlinks add constraint c_4419f969 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_7a55640f on ix_ginas_otherlinks (last_edited_by_id);
alter table ix_ginas_otherlinks add constraint c_471fa21d foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_098f5e71 on ix_ginas_otherlinks (record_reference_id);
alter table ix_ginas_otherlinks add constraint c_398de8a7 foreign key (owner_uuid) references ix_ginas_protein (uuid);
create index i_acb18b2f on ix_ginas_otherlinks (owner_uuid);
alter table ix_ginas_otherlinks add constraint c_fe7bcc1e foreign key (site_container_uuid) references ix_ginas_site_lob (uuid);
create index i_878b68b9 on ix_ginas_otherlinks (site_container_uuid);
alter table ix_ginas_parameter add constraint c_77436290 foreign key (created_by_id) references ix_core_principal (id);
create index i_270b7008 on ix_ginas_parameter (created_by_id);
alter table ix_ginas_parameter add constraint c_b78a45c9 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_0d23b2fd on ix_ginas_parameter (last_edited_by_id);
alter table ix_ginas_parameter add constraint c_e8a350e3 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_24a42c08 on ix_ginas_parameter (record_reference_id);
alter table ix_ginas_parameter add constraint c_595527e7 foreign key (owner_uuid) references ix_ginas_property (uuid);
create index i_94d122b8 on ix_ginas_parameter (owner_uuid);
alter table ix_ginas_parameter add constraint c_5fbf845a foreign key (value_uuid) references ix_ginas_amount (uuid);
create index i_638890b7 on ix_ginas_parameter (value_uuid);
alter table ix_core_payload add constraint c_560d7542 foreign key (namespace_id) references ix_core_namespace (id);
create index i_248df40b on ix_core_payload (namespace_id);
alter table ix_ginas_physicalmod add constraint c_4d745197 foreign key (created_by_id) references ix_core_principal (id);
create index i_a6cce239 on ix_ginas_physicalmod (created_by_id);
alter table ix_ginas_physicalmod add constraint c_878727a9 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_e5a1de4b on ix_ginas_physicalmod (last_edited_by_id);
alter table ix_ginas_physicalmod add constraint c_1dfa2a2e foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_5e0ed64e on ix_ginas_physicalmod (record_reference_id);
alter table ix_ginas_physicalmod add constraint c_422a69e0 foreign key (owner_uuid) references ix_ginas_modifications (uuid);
create index i_b37aaa5b on ix_ginas_physicalmod (owner_uuid);
alter table ix_ginas_physicalpar add constraint c_163b1beb foreign key (created_by_id) references ix_core_principal (id);
create index i_354251cd on ix_ginas_physicalpar (created_by_id);
alter table ix_ginas_physicalpar add constraint c_730fccf8 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_83ed674a on ix_ginas_physicalpar (last_edited_by_id);
alter table ix_ginas_physicalpar add constraint c_e013194a foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_b14aad1d on ix_ginas_physicalpar (record_reference_id);
alter table ix_ginas_physicalpar add constraint c_f729bbe1 foreign key (owner_uuid) references ix_ginas_physicalmod (uuid);
create index i_51c43d63 on ix_ginas_physicalpar (owner_uuid);
alter table ix_ginas_physicalpar add constraint c_da8ea84f foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_0413e8ca on ix_ginas_physicalpar (amount_uuid);
alter table ix_ginas_polymer add constraint c_642c6160 foreign key (created_by_id) references ix_core_principal (id);
create index i_c8f12799 on ix_ginas_polymer (created_by_id);
alter table ix_ginas_polymer add constraint c_b72e5b1f foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_64bb9064 on ix_ginas_polymer (last_edited_by_id);
alter table ix_ginas_polymer add constraint c_50cb6f07 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_4e7285be on ix_ginas_polymer (record_reference_id);
alter table ix_ginas_polymer add constraint c_7d690e58 foreign key (classification_uuid) references polymer_classification (uuid);
create index i_cfd4ce2f on ix_ginas_polymer (classification_uuid);
alter table ix_ginas_polymer add constraint c_5a623490 foreign key (display_structure_id) references ix_core_structure (id);
create index i_92768729 on ix_ginas_polymer (display_structure_id);
alter table ix_ginas_polymer add constraint c_4590922e foreign key (idealized_structure_id) references ix_core_structure (id);
create index i_c3ce4731 on ix_ginas_polymer (idealized_structure_id);
alter table polymer_classification add constraint c_77b3c1ac foreign key (created_by_id) references ix_core_principal (id);
create index i_a483fbd4 on polymer_classification (created_by_id);
alter table polymer_classification add constraint c_40f04904 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_177560f7 on polymer_classification (last_edited_by_id);
alter table polymer_classification add constraint c_b9b49fe1 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_fa52488d on polymer_classification (record_reference_id);
alter table polymer_classification add constraint c_9660c876 foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_44fe9982 on polymer_classification (parent_substance_uuid);
alter table ix_core_predicate add constraint c_0773d723 foreign key (namespace_id) references ix_core_namespace (id);
create index i_f61c5bc0 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint c_b2bf4895 foreign key (subject_id) references ix_core_xref (id);
create index i_bacdd8f1 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint c_d21439fe foreign key (namespace_id) references ix_core_namespace (id);
create index i_f13551bc on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint c_ab33c8be foreign key (selfie_id) references ix_core_figure (id);
create index i_4e17717d on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint c_f96ee53a foreign key (institution_id) references ix_core_organization (id);
create index i_e8b7b156 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint c_7f5f7326 foreign key (owner_id) references ix_core_principal (id);
create index i_addf4b32 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint c_205ee52f foreign key (payload_id) references ix_core_payload (id);
create index i_5efeb9dc on ix_core_procjob (payload_id);
alter table ix_core_procrec add constraint c_ceb285b7 foreign key (xref_id) references ix_core_xref (id);
create index i_4988c32a on ix_core_procrec (xref_id);
alter table ix_core_procrec add constraint c_0b3441fa foreign key (job_id) references ix_core_procjob (id);
create index i_0e42eaff on ix_core_procrec (job_id);
alter table ix_ginas_property add constraint c_68e94e65 foreign key (created_by_id) references ix_core_principal (id);
create index i_df232d71 on ix_ginas_property (created_by_id);
alter table ix_ginas_property add constraint c_cc6e7488 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_1aa07c28 on ix_ginas_property (last_edited_by_id);
alter table ix_ginas_property add constraint c_e3485851 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_593df841 on ix_ginas_property (record_reference_id);
alter table ix_ginas_property add constraint c_25ce6ea2 foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_13d4656a on ix_ginas_property (owner_uuid);
alter table ix_ginas_property add constraint c_2d4654ae foreign key (value_uuid) references ix_ginas_amount (uuid);
create index i_fe3132d6 on ix_ginas_property (value_uuid);
alter table ix_ginas_protein add constraint c_82d593a3 foreign key (created_by_id) references ix_core_principal (id);
create index i_bbd875e0 on ix_ginas_protein (created_by_id);
alter table ix_ginas_protein add constraint c_92889cce foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_f1edb938 on ix_ginas_protein (last_edited_by_id);
alter table ix_ginas_protein add constraint c_4ad0971a foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_9cc86612 on ix_ginas_protein (record_reference_id);
alter table ix_ginas_protein add constraint c_2fb93c09 foreign key (glycosylation_uuid) references ix_ginas_glycosylation (uuid);
create index i_33037f0b on ix_ginas_protein (glycosylation_uuid);
alter table ix_ginas_protein add constraint c_cebdc82a foreign key (modifications_uuid) references ix_ginas_modifications (uuid);
create index i_b14de510 on ix_ginas_protein (modifications_uuid);
alter table ix_core_pubauthor add constraint c_fc45706e foreign key (author_id) references ix_core_principal (id);
create index i_a8b70381 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint c_1202b692 foreign key (journal_id) references ix_core_journal (id);
create index i_7bb19cf0 on ix_core_publication (journal_id);
alter table ix_ginas_reference add constraint c_9a8bc9d3 foreign key (created_by_id) references ix_core_principal (id);
create index i_fb16072d on ix_ginas_reference (created_by_id);
alter table ix_ginas_reference add constraint c_69296f85 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_ae763329 on ix_ginas_reference (last_edited_by_id);
alter table ix_ginas_reference add constraint c_f06d9d85 foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_5e2ddb6b on ix_ginas_reference (owner_uuid);
alter table ix_ginas_relationship add constraint c_d95595bc foreign key (created_by_id) references ix_core_principal (id);
create index i_b18f9a6b on ix_ginas_relationship (created_by_id);
alter table ix_ginas_relationship add constraint c_5dbe3644 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_264b35bd on ix_ginas_relationship (last_edited_by_id);
alter table ix_ginas_relationship add constraint c_b3133f54 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_5cb6f838 on ix_ginas_relationship (record_reference_id);
alter table ix_ginas_relationship add constraint c_4c0417ca foreign key (owner_uuid) references ix_ginas_substance (uuid);
create index i_d119f4e9 on ix_ginas_relationship (owner_uuid);
alter table ix_ginas_relationship add constraint c_d1126180 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_68fee275 on ix_ginas_relationship (amount_uuid);
alter table ix_ginas_relationship add constraint c_6ffdae19 foreign key (related_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_abdcbc34 on ix_ginas_relationship (related_substance_uuid);
alter table ix_ginas_relationship add constraint c_0e132cf7 foreign key (mediator_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_5c75901d on ix_ginas_relationship (mediator_substance_uuid);
alter table ix_core_session add constraint c_710b0456 foreign key (profile_id) references ix_core_userprof (id);
create index i_e6f15a0f on ix_core_session (profile_id);
alter table ix_ginas_site_lob add constraint c_8c7a45e2 foreign key (created_by_id) references ix_core_principal (id);
create index i_b29aa8d6 on ix_ginas_site_lob (created_by_id);
alter table ix_ginas_site_lob add constraint c_e0c951bd foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_247ee039 on ix_ginas_site_lob (last_edited_by_id);
alter table ix_ginas_site_lob add constraint c_f93c2900 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_a084d09a on ix_ginas_site_lob (record_reference_id);
alter table ix_ginas_ssg1 add constraint c_1f1ff56b foreign key (created_by_id) references ix_core_principal (id);
create index i_1b6dad38 on ix_ginas_ssg1 (created_by_id);
alter table ix_ginas_ssg1 add constraint c_2d7356e2 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_1ef16fb1 on ix_ginas_ssg1 (last_edited_by_id);
alter table ix_ginas_ssg1 add constraint c_4e5415a3 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_36ee557b on ix_ginas_ssg1 (record_reference_id);
alter table ix_ginas_structuralmod add constraint c_04804f56 foreign key (created_by_id) references ix_core_principal (id);
create index i_fa522441 on ix_ginas_structuralmod (created_by_id);
alter table ix_ginas_structuralmod add constraint c_3c405236 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_11f38afe on ix_ginas_structuralmod (last_edited_by_id);
alter table ix_ginas_structuralmod add constraint c_53ca3638 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_5df8155b on ix_ginas_structuralmod (record_reference_id);
alter table ix_ginas_structuralmod add constraint c_ecf9f511 foreign key (owner_uuid) references ix_ginas_modifications (uuid);
create index i_e1f35551 on ix_ginas_structuralmod (owner_uuid);
alter table ix_ginas_structuralmod add constraint c_3d7e392a foreign key (site_container_uuid) references ix_ginas_site_lob (uuid);
create index i_bb7261c4 on ix_ginas_structuralmod (site_container_uuid);
alter table ix_ginas_structuralmod add constraint c_ac356e74 foreign key (extent_amount_uuid) references ix_ginas_amount (uuid);
create index i_a23427ba on ix_ginas_structuralmod (extent_amount_uuid);
alter table ix_ginas_structuralmod add constraint c_86118187 foreign key (molecular_fragment_uuid) references ix_ginas_substanceref (uuid);
create index i_26e4ce83 on ix_ginas_structuralmod (molecular_fragment_uuid);
alter table ix_ginas_strucdiv add constraint c_3e61b57c foreign key (created_by_id) references ix_core_principal (id);
create index i_0fa551be on ix_ginas_strucdiv (created_by_id);
alter table ix_ginas_strucdiv add constraint c_836e74b9 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_8ee424ab on ix_ginas_strucdiv (last_edited_by_id);
alter table ix_ginas_strucdiv add constraint c_fab7cb40 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_3719dd9a on ix_ginas_strucdiv (record_reference_id);
alter table ix_ginas_strucdiv add constraint c_7c01dde7 foreign key (paternal_uuid) references ix_ginas_substanceref (uuid);
create index i_4bf046e4 on ix_ginas_strucdiv (paternal_uuid);
alter table ix_ginas_strucdiv add constraint c_221aa9d8 foreign key (maternal_uuid) references ix_ginas_substanceref (uuid);
create index i_3c1e7d32 on ix_ginas_strucdiv (maternal_uuid);
alter table ix_ginas_strucdiv add constraint c_c0247218 foreign key (parent_substance_uuid) references ix_ginas_substanceref (uuid);
create index i_cf50b2a0 on ix_ginas_strucdiv (parent_substance_uuid);
alter table ix_core_structure add constraint c_141e3694 foreign key (created_by_id) references ix_core_principal (id);
create index i_9359c1e9 on ix_core_structure (created_by_id);
alter table ix_core_structure add constraint c_2b0ab2bd foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_612fb62e on ix_core_structure (last_edited_by_id);
alter table ix_core_structure add constraint c_fc8ac98c foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_14329589 on ix_core_structure (record_reference_id);
alter table ix_ginas_substance add constraint c_1bdb3e4a foreign key (created_by_id) references ix_core_principal (id);
create index i_05632bc2 on ix_ginas_substance (created_by_id);
alter table ix_ginas_substance add constraint c_165ef368 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_9e6341cc on ix_ginas_substance (last_edited_by_id);
alter table ix_ginas_substance add constraint c_2c996629 foreign key (approved_by_id) references ix_core_principal (id);
create index i_55f30908 on ix_ginas_substance (approved_by_id);
alter table ix_ginas_substance add constraint c_3be5bd6a foreign key (modifications_uuid) references ix_ginas_modifications (uuid);
create index i_f7c2fdfa on ix_ginas_substance (modifications_uuid);
alter table ix_ginas_substance add constraint c_d25264e8 foreign key (structure_id) references ix_core_structure (id);
create index i_a1fa090a on ix_ginas_substance (structure_id);
alter table ix_ginas_substance add constraint c_a0cd0dbe foreign key (mixture_uuid) references ix_ginas_mixture (uuid);
create index i_2802eebf on ix_ginas_substance (mixture_uuid);
alter table ix_ginas_substance add constraint c_9c488788 foreign key (nucleic_acid_uuid) references ix_ginas_nucleicacid (uuid);
create index i_76b040da on ix_ginas_substance (nucleic_acid_uuid);
alter table ix_ginas_substance add constraint c_e230f0cb foreign key (polymer_uuid) references ix_ginas_polymer (uuid);
create index i_ede6ef1a on ix_ginas_substance (polymer_uuid);
alter table ix_ginas_substance add constraint c_ba665809 foreign key (protein_uuid) references ix_ginas_protein (uuid);
create index i_ac11d63b on ix_ginas_substance (protein_uuid);
alter table ix_ginas_substance add constraint c_745c84f5 foreign key (specified_substance_uuid) references ix_ginas_ssg1 (uuid);
create index i_72a53321 on ix_ginas_substance (specified_substance_uuid);
alter table ix_ginas_substance add constraint c_d8ae89aa foreign key (structurally_diverse_uuid) references ix_ginas_strucdiv (uuid);
create index i_4e5f5336 on ix_ginas_substance (structurally_diverse_uuid);
alter table ix_ginas_substanceref add constraint c_011684a1 foreign key (created_by_id) references ix_core_principal (id);
create index i_7a1f9a20 on ix_ginas_substanceref (created_by_id);
alter table ix_ginas_substanceref add constraint c_0b8f56af foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_4467dfd3 on ix_ginas_substanceref (last_edited_by_id);
alter table ix_ginas_substanceref add constraint c_dbb7104b foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_b80480c8 on ix_ginas_substanceref (record_reference_id);
alter table ix_ginas_subunit add constraint c_0a2b82d0 foreign key (created_by_id) references ix_core_principal (id);
create index i_e09a9a58 on ix_ginas_subunit (created_by_id);
alter table ix_ginas_subunit add constraint c_69c54dc0 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_0d596f25 on ix_ginas_subunit (last_edited_by_id);
alter table ix_ginas_subunit add constraint c_ef0da216 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_bc37a722 on ix_ginas_subunit (record_reference_id);
alter table ix_ginas_sugar add constraint c_75c60afd foreign key (created_by_id) references ix_core_principal (id);
create index i_f3e0e7da on ix_ginas_sugar (created_by_id);
alter table ix_ginas_sugar add constraint c_410dfdda foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_ef88b2cd on ix_ginas_sugar (last_edited_by_id);
alter table ix_ginas_sugar add constraint c_bda998e5 foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_2c39f57a on ix_ginas_sugar (record_reference_id);
alter table ix_ginas_sugar add constraint c_1d94597e foreign key (owner_uuid) references ix_ginas_nucleicacid (uuid);
create index i_e0f61e3c on ix_ginas_sugar (owner_uuid);
alter table ix_ginas_sugar add constraint c_798960f0 foreign key (site_container_uuid) references ix_ginas_site_lob (uuid);
create index i_92cd8082 on ix_ginas_sugar (site_container_uuid);
alter table ix_ginas_unit add constraint c_3cc94638 foreign key (created_by_id) references ix_core_principal (id);
create index i_6306737d on ix_ginas_unit (created_by_id);
alter table ix_ginas_unit add constraint c_f7e41d83 foreign key (last_edited_by_id) references ix_core_principal (id);
create index i_031c2e58 on ix_ginas_unit (last_edited_by_id);
alter table ix_ginas_unit add constraint c_68b2d7ca foreign key (record_reference_id) references ix_ginas_reference_cit (id);
create index i_3755c46b on ix_ginas_unit (record_reference_id);
alter table ix_ginas_unit add constraint c_9f1073e6 foreign key (owner_uuid) references ix_ginas_polymer (uuid);
create index i_67b0edeb on ix_ginas_unit (owner_uuid);
alter table ix_ginas_unit add constraint c_a6620a4b foreign key (amap_id) references ix_core_value (id);
create index i_49337a41 on ix_ginas_unit (amap_id);
alter table ix_ginas_unit add constraint c_f2152f34 foreign key (amount_uuid) references ix_ginas_amount (uuid);
create index i_15043fe9 on ix_ginas_unit (amount_uuid);
alter table ix_core_userprof add constraint c_f83f5bac foreign key (namespace_id) references ix_core_namespace (id);
create index i_835c81be on ix_core_userprof (namespace_id);
alter table ix_core_userprof add constraint c_c15c11ac foreign key (user_id) references ix_core_principal (id);
create index i_152c8ea9 on ix_core_userprof (user_id);
alter table ix_ginas_vocabulary_term add constraint c_9a35336c foreign key (namespace_id) references ix_core_namespace (id);
create index i_3d5e2668 on ix_ginas_vocabulary_term (namespace_id);
alter table ix_ginas_vocabulary_term add constraint c_1c11f047 foreign key (owner_id) references ix_ginas_controlled_vocab (id);
create index i_79cce622 on ix_ginas_vocabulary_term (owner_id);
alter table ix_core_xref add constraint c_76369a3a foreign key (namespace_id) references ix_core_namespace (id);
create index i_04e48418 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint c_deeb9ed8 foreign key (ix_core_acl_id) references ix_core_acl (id);

alter table ix_core_acl_principal add constraint c_2682a8a2 foreign key (ix_core_principal_id) references ix_core_principal (id);

alter table ix_core_acl_group add constraint c_4d670747 foreign key (ix_core_acl_id) references ix_core_acl (id);

alter table ix_core_acl_group add constraint c_d35484d1 foreign key (ix_core_group_id) references ix_core_group (id);

alter table ix_ginas_controlled_vocab_core add constraint c_43f2af66 foreign key (ix_ginas_controlled_vocab_id) references ix_ginas_controlled_vocab (id);

alter table ix_ginas_controlled_vocab_core add constraint c_12ca4bf2 foreign key (ix_core_value_id) references ix_core_value (id);

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

