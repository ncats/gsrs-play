# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_ginas_agent_modification (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  agent_modification_process varchar(255),
  agent_modification_role   varchar(255),
  agent_modification_type   varchar(255),
  agent_substance_uuid      varchar(40),
  amount_uuid               varchar(40),
  constraint pk_ix_ginas_agent_modification primary key (uuid))
;

create table ix_ginas_amount (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  type                      varchar(255),
  average                   double,
  high_limit                double,
  high                      double,
  low_limit                 double,
  low                       double,
  units                     varchar(255),
  non_numeric_value         varchar(255),
  approval_id               varchar(10),
  constraint pk_ix_ginas_amount primary key (uuid))
;

create table ix_core_attribute (
  id                        bigint not null,
  name                      varchar(255),
  value                     varchar(1024),
  namespace_id              bigint,
  constraint pk_ix_core_attribute primary key (id))
;

create table ix_ginas_code (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  code_system               varchar(255),
  code                      varchar(255) not null,
  comments                  clob,
  type                      varchar(255),
  url                       clob,
  constraint pk_ix_ginas_code primary key (uuid))
;

create table ix_ginas_component (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  type                      varchar(255),
  substance_uuid            varchar(40),
  constraint pk_ix_ginas_component primary key (uuid))
;

create table ix_core_curation (
  id                        bigint not null,
  curator_id                bigint,
  status                    integer,
  timestamp                 timestamp,
  constraint ck_ix_core_curation_status check (status in (0,1,2,3)),
  constraint pk_ix_core_curation primary key (id))
;

create table ix_ginas_disulfide (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  constraint pk_ix_ginas_disulfide primary key (uuid))
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

create table ix_ginas_glycosylation (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  glycosylation_type        varchar(255),
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

create table ix_ginas_material (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  amount_uuid               varchar(40),
  monomer_substance_uuid    varchar(40),
  type                      varchar(255),
  defining                  boolean,
  constraint pk_ix_ginas_material primary key (uuid))
;

create table ix_ginas_modifications (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  constraint pk_ix_ginas_modifications primary key (uuid))
;

create table ix_ginas_moiety (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  structure_id              bigint,
  count                     integer,
  constraint pk_ix_ginas_moiety primary key (uuid))
;

create table ix_ginas_name (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  name                      varchar(255) not null,
  full_name                 clob,
  type                      varchar(32),
  preferred                 boolean,
  constraint pk_ix_ginas_name primary key (uuid))
;

create table ix_ginas_nameorg (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  name_org                  varchar(255) not null,
  deprecated_date           timestamp,
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
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  note                      clob,
  constraint pk_ix_ginas_note primary key (uuid))
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
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  linkage_type              varchar(255),
  constraint pk_ix_ginas_otherlinks primary key (uuid))
;

create table ix_ginas_parameter (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  name                      varchar(255) not null,
  type                      varchar(255),
  value_uuid                varchar(40),
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

create table ix_ginas_physical_modification (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  physical_modification_role varchar(255) not null,
  constraint pk_ix_ginas_physical_modificatio primary key (uuid))
;

create table ix_ginas_physical_parameter (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  parameter_name            varchar(255),
  amount_uuid               varchar(40),
  constraint pk_ix_ginas_physical_parameter primary key (uuid))
;

create table ix_ginas_polymer (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  classification_uuid       varchar(40),
  display_structure_id      bigint,
  idealized_structure_id    bigint,
  constraint pk_ix_ginas_polymer primary key (uuid))
;

create table ix_ginas_polymerclass (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  polymer_class             varchar(255),
  polymer_geometry          varchar(255),
  source_type               varchar(255),
  constraint pk_ix_ginas_polymerclass primary key (uuid))
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

create table ix_ginas_property (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  name                      varchar(255) not null,
  type                      varchar(255),
  property_type             varchar(255),
  value_uuid                varchar(40),
  defining                  boolean,
  constraint pk_ix_ginas_property primary key (uuid))
;

create table ix_ginas_protein (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  protein_type              varchar(255),
  protein_sub_type          varchar(255),
  sequence_origin           varchar(255),
  sequence_type             varchar(255),
  glycosylation_uuid        varchar(40),
  modifications_uuid        varchar(40),
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
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  citation                  varchar(255) not null,
  doc_type                  varchar(255),
  document_date             timestamp,
  public_domain             boolean,
  uploaded_file             varchar(1024),
  id                        varchar(255),
  url                       clob,
  constraint pk_ix_ginas_reference primary key (uuid))
;

create table ix_ginas_relationship (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  amount_uuid               varchar(40),
  comments                  clob,
  interaction_type          varchar(255),
  qualification             varchar(255),
  related_substance_uuid    varchar(40),
  type                      varchar(255),
  constraint pk_ix_ginas_relationship primary key (uuid))
;

create table ix_core_role (
  id                        bigint not null,
  role                      integer,
  principal_id              bigint,
  constraint ck_ix_core_role_role check (role in (0,1,2,3)),
  constraint pk_ix_core_role primary key (id))
;

create table ix_ginas_site (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  subunit_index             integer,
  residue_index             integer,
  constraint pk_ix_ginas_site primary key (uuid))
;

create table ix_core_stitch (
  id                        bigint not null,
  name                      varchar(255),
  impl                      varchar(1024),
  description               clob,
  constraint pk_ix_core_stitch primary key (id))
;

create table ix_ginas_structural_modification (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  structural_modification_type varchar(255) not null,
  location_type             varchar(255),
  residue_modified          varchar(255),
  extent                    varchar(255),
  extent_amount_uuid        varchar(40),
  molecular_fragment_uuid   varchar(40),
  constraint pk_ix_ginas_structural_modificat primary key (uuid))
;

create table ix_ginas_strucdiv (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  developmental_stage       varchar(255),
  fraction_name             varchar(255),
  fraction_material_type    varchar(255),
  organism_family           varchar(255),
  organism_genus            varchar(255),
  organism_species          varchar(255),
  part_location             varchar(255),
  source_material_class     varchar(255),
  source_material_state     varchar(255),
  source_material_type      varchar(255),
  infra_specific_type       varchar(255),
  infra_specific_name       varchar(255),
  hybrid_species_paternal_organism_uuid varchar(40),
  hybrid_species_maternal_organism_uuid varchar(40),
  parent_substance_uuid     varchar(40),
  constraint pk_ix_ginas_strucdiv primary key (uuid))
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

create table ix_ginas_substance (
  dtype                     varchar(10) not null,
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  substance_class           integer,
  status                    varchar(255),
  approved_by               varchar(255),
  approved                  timestamp,
  modifications_uuid        varchar(40),
  approval_id               varchar(10),
  protein_uuid              varchar(40),
  structure_id              bigint,
  polymer_uuid              varchar(40),
  structurally_diverse_uuid varchar(40),
  constraint ck_ix_ginas_substance_substance_class check (substance_class in (0,1,2,3,4,5,6,7,8,9,10,11)),
  constraint pk_ix_ginas_substance primary key (uuid))
;

create table ix_ginas_substancereference (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  ref_pname                 varchar(255),
  refuuid                   varchar(128) not null,
  substance_class           varchar(255),
  approval_id               varchar(32),
  constraint pk_ix_ginas_substancereference primary key (uuid))
;

create table ix_ginas_subunit (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  sequence                  clob,
  subunit_index             integer,
  constraint pk_ix_ginas_subunit primary key (uuid))
;

create table ix_ginas_unit (
  uuid                      varchar(40) not null,
  created                   timestamp,
  last_modified             timestamp,
  last_edited_by_id         bigint,
  deprecated                boolean,
  amap_id                   bigint,
  amount_uuid               varchar(40),
  attachment_count          integer,
  label                     varchar(255),
  structure                 clob,
  type                      varchar(255),
  constraint pk_ix_ginas_unit primary key (uuid))
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

create table ix_ginas_code_access (
  ix_ginas_code_uuid             varchar(40) not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ginas_code_access primary key (ix_ginas_code_uuid, ix_core_principal_id))
;

create table ix_ginas_code_reference (
  ix_ginas_code_uuid             varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_code_reference primary key (ix_ginas_code_uuid, ix_core_value_id))
;

create table ix_ginas_disulfide_site (
  ix_ginas_disulfide_uuid        varchar(40) not null,
  ix_ginas_site_uuid             varchar(40) not null,
  constraint pk_ix_ginas_disulfide_site primary key (ix_ginas_disulfide_uuid, ix_ginas_site_uuid))
;

create table ix_core_event_figure (
  ix_core_event_id               bigint not null,
  ix_core_figure_id              bigint not null,
  constraint pk_ix_core_event_figure primary key (ix_core_event_id, ix_core_figure_id))
;

create table ix_ginas_glycosylation_csite (
  ix_ginas_glycosylation_c_uuid  varchar(40) not null,
  ix_ginas_site_uuid             varchar(40) not null,
  constraint pk_ix_ginas_glycosylation_csite primary key (ix_ginas_glycosylation_c_uuid, ix_ginas_site_uuid))
;

create table ix_ginas_glycosylation_nsite (
  ix_ginas_glycosylation_n_uuid  varchar(40) not null,
  ix_ginas_site_uuid             varchar(40) not null,
  constraint pk_ix_ginas_glycosylation_nsite primary key (ix_ginas_glycosylation_n_uuid, ix_ginas_site_uuid))
;

create table ix_ginas_glycosylation_osite (
  ix_ginas_glycosylation_o_uuid  varchar(40) not null,
  ix_ginas_site_uuid             varchar(40) not null,
  constraint pk_ix_ginas_glycosylation_osite primary key (ix_ginas_glycosylation_o_uuid, ix_ginas_site_uuid))
;

create table ix_core_group_principal (
  ix_core_group_id               bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_core_group_principal primary key (ix_core_group_id, ix_core_principal_id))
;

create table ix_ginas_modifications_agent (
  ix_ginas_modifications_uuid    varchar(40) not null,
  ix_ginas_agent_modification_uuid varchar(40) not null,
  constraint pk_ix_ginas_modifications_agent primary key (ix_ginas_modifications_uuid, ix_ginas_agent_modification_uuid))
;

create table ix_ginas_modifications_physical (
  ix_ginas_modifications_uuid    varchar(40) not null,
  ix_ginas_physical_modification_uuid varchar(40) not null,
  constraint pk_ix_ginas_modifications_physical primary key (ix_ginas_modifications_uuid, ix_ginas_physical_modification_uuid))
;

create table ix_ginas_modifications_structural (
  ix_ginas_modifications_uuid    varchar(40) not null,
  ix_ginas_structural_modification_uuid varchar(40) not null,
  constraint pk_ix_ginas_modifications_structural primary key (ix_ginas_modifications_uuid, ix_ginas_structural_modification_uuid))
;

create table ix_ginas_name_access (
  ix_ginas_name_uuid             varchar(40) not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ginas_name_access primary key (ix_ginas_name_uuid, ix_core_principal_id))
;

create table ix_ginas_name_domain (
  ix_ginas_name_domain_uuid      varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_name_domain primary key (ix_ginas_name_domain_uuid, ix_core_value_id))
;

create table ix_ginas_name_language (
  ix_ginas_name_language_uuid    varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_name_language primary key (ix_ginas_name_language_uuid, ix_core_value_id))
;

create table ix_ginas_name_jurisdiction (
  ix_ginas_name_jurisdiction_uuid varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_name_jurisdiction primary key (ix_ginas_name_jurisdiction_uuid, ix_core_value_id))
;

create table ix_ginas_name_reference (
  ix_ginas_name_reference_uuid   varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_name_reference primary key (ix_ginas_name_reference_uuid, ix_core_value_id))
;

create table ix_ginas_name_nameorg (
  ix_ginas_name_uuid             varchar(40) not null,
  ix_ginas_nameorg_uuid          varchar(40) not null,
  constraint pk_ix_ginas_name_nameorg primary key (ix_ginas_name_uuid, ix_ginas_nameorg_uuid))
;

create table ix_ginas_note_access (
  ix_ginas_note_uuid             varchar(40) not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ginas_note_access primary key (ix_ginas_note_uuid, ix_core_principal_id))
;

create table ix_ginas_note_reference (
  ix_ginas_note_uuid             varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_note_reference primary key (ix_ginas_note_uuid, ix_core_value_id))
;

create table ix_ginas_otherlinks_site (
  ix_ginas_otherlinks_uuid       varchar(40) not null,
  ix_ginas_site_uuid             varchar(40) not null,
  constraint pk_ix_ginas_otherlinks_site primary key (ix_ginas_otherlinks_uuid, ix_ginas_site_uuid))
;

create table ix_core_payload_property (
  ix_core_payload_id             varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_payload_property primary key (ix_core_payload_id, ix_core_value_id))
;

create table ix_ginas_physical_modification_param (
  ix_ginas_physical_modification_uuid varchar(40) not null,
  ix_ginas_physical_parameter_uuid varchar(40) not null,
  constraint pk_ix_ginas_physical_modification_param primary key (ix_ginas_physical_modification_uuid, ix_ginas_physical_parameter_uuid))
;

create table ix_ginas_polymer_material (
  ix_ginas_polymer_uuid          varchar(40) not null,
  ix_ginas_material_uuid         varchar(40) not null,
  constraint pk_ix_ginas_polymer_material primary key (ix_ginas_polymer_uuid, ix_ginas_material_uuid))
;

create table ix_ginas_polymer_unit (
  ix_ginas_polymer_uuid          varchar(40) not null,
  ix_ginas_unit_uuid             varchar(40) not null,
  constraint pk_ix_ginas_polymer_unit primary key (ix_ginas_polymer_uuid, ix_ginas_unit_uuid))
;

create table ix_ginas_polymerclass_sub (
  ix_ginas_polymerclass_uuid     varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_polymerclass_sub primary key (ix_ginas_polymerclass_uuid, ix_core_value_id))
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

create table ix_ginas_property_parameter (
  ix_ginas_property_uuid         varchar(40) not null,
  ix_ginas_parameter_uuid        varchar(40) not null,
  constraint pk_ix_ginas_property_parameter primary key (ix_ginas_property_uuid, ix_ginas_parameter_uuid))
;

create table ix_ginas_property_reference (
  ix_ginas_property_uuid         varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_property_reference primary key (ix_ginas_property_uuid, ix_core_value_id))
;

create table ix_ginas_protein_disulfide (
  ix_ginas_protein_uuid          varchar(40) not null,
  ix_ginas_disulfide_uuid        varchar(40) not null,
  constraint pk_ix_ginas_protein_disulfide primary key (ix_ginas_protein_uuid, ix_ginas_disulfide_uuid))
;

create table ix_ginas_protein_subunit (
  ix_ginas_protein_uuid          varchar(40) not null,
  ix_ginas_subunit_uuid          varchar(40) not null,
  constraint pk_ix_ginas_protein_subunit primary key (ix_ginas_protein_uuid, ix_ginas_subunit_uuid))
;

create table ix_ginas_protein_otherlinks (
  ix_ginas_protein_uuid          varchar(40) not null,
  ix_ginas_otherlinks_uuid       varchar(40) not null,
  constraint pk_ix_ginas_protein_otherlinks primary key (ix_ginas_protein_uuid, ix_ginas_otherlinks_uuid))
;

create table ix_ginas_protein_reference (
  ix_ginas_protein_uuid          varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_protein_reference primary key (ix_ginas_protein_uuid, ix_core_value_id))
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

create table ix_ginas_reference_access (
  ix_ginas_reference_uuid        varchar(40) not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ginas_reference_access primary key (ix_ginas_reference_uuid, ix_core_principal_id))
;

create table ix_ginas_reference_tag (
  ix_ginas_reference_uuid        varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_reference_tag primary key (ix_ginas_reference_uuid, ix_core_value_id))
;

create table ix_ginas_relationship_access (
  ix_ginas_relationship_uuid     varchar(40) not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ginas_relationship_access primary key (ix_ginas_relationship_uuid, ix_core_principal_id))
;

create table ix_ginas_relationship_reference (
  ix_ginas_relationship_uuid     varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_relationship_reference primary key (ix_ginas_relationship_uuid, ix_core_value_id))
;

create table ix_core_stitch_attribute (
  ix_core_stitch_id              bigint not null,
  ix_core_attribute_id           bigint not null,
  constraint pk_ix_core_stitch_attribute primary key (ix_core_stitch_id, ix_core_attribute_id))
;

create table ix_ginas_structural_modification_1 (
  ix_ginas_structural_modification_uuid varchar(40) not null,
  ix_ginas_site_uuid             varchar(40) not null,
  constraint pk_ix_ginas_structural_modification_1 primary key (ix_ginas_structural_modification_uuid, ix_ginas_site_uuid))
;

create table ix_ginas_strucdiv_part (
  ix_ginas_strucdiv_uuid         varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_strucdiv_part primary key (ix_ginas_strucdiv_uuid, ix_core_value_id))
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

create table ix_ginas_substance_access (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ginas_substance_access primary key (ix_ginas_substance_uuid, ix_core_principal_id))
;

create table ix_ginas_substance_name (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_name_uuid             varchar(40) not null,
  constraint pk_ix_ginas_substance_name primary key (ix_ginas_substance_uuid, ix_ginas_name_uuid))
;

create table ix_ginas_substance_code (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_code_uuid             varchar(40) not null,
  constraint pk_ix_ginas_substance_code primary key (ix_ginas_substance_uuid, ix_ginas_code_uuid))
;

create table ix_ginas_substance_note (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_note_uuid             varchar(40) not null,
  constraint pk_ix_ginas_substance_note primary key (ix_ginas_substance_uuid, ix_ginas_note_uuid))
;

create table ix_ginas_substance_property (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_property_uuid         varchar(40) not null,
  constraint pk_ix_ginas_substance_property primary key (ix_ginas_substance_uuid, ix_ginas_property_uuid))
;

create table ix_ginas_substance_relationship (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_relationship_uuid     varchar(40) not null,
  constraint pk_ix_ginas_substance_relationship primary key (ix_ginas_substance_uuid, ix_ginas_relationship_uuid))
;

create table ix_ginas_substance_reference (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_reference_uuid        varchar(40) not null,
  constraint pk_ix_ginas_substance_reference primary key (ix_ginas_substance_uuid, ix_ginas_reference_uuid))
;

create table ix_ginas_substance_tag (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_substance_tag primary key (ix_ginas_substance_uuid, ix_core_value_id))
;

create table ix_ginas_chemical_moiety (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_moiety_uuid           varchar(40) not null,
  constraint pk_ix_ginas_chemical_moiety primary key (ix_ginas_substance_uuid, ix_ginas_moiety_uuid))
;

create table ix_ginas_substance_component (
  ix_ginas_substance_uuid        varchar(40) not null,
  ix_ginas_component_uuid        varchar(40) not null,
  constraint pk_ix_ginas_substance_component primary key (ix_ginas_substance_uuid, ix_ginas_component_uuid))
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

alter table ix_ginas_agent_modification add constraint fk_ix_ginas_agent_modification_1 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_agent_modification_1 on ix_ginas_agent_modification (last_edited_by_id);
alter table ix_ginas_agent_modification add constraint fk_ix_ginas_agent_modification_2 foreign key (agent_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_agent_modification_2 on ix_ginas_agent_modification (agent_substance_uuid);
alter table ix_ginas_agent_modification add constraint fk_ix_ginas_agent_modification_3 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_agent_modification_3 on ix_ginas_agent_modification (amount_uuid);
alter table ix_ginas_amount add constraint fk_ix_ginas_amount_lastEditedB_4 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_amount_lastEditedB_4 on ix_ginas_amount (last_edited_by_id);
alter table ix_core_attribute add constraint fk_ix_core_attribute_namespace_5 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_namespace_5 on ix_core_attribute (namespace_id);
alter table ix_ginas_code add constraint fk_ix_ginas_code_lastEditedBy_6 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_code_lastEditedBy_6 on ix_ginas_code (last_edited_by_id);
alter table ix_ginas_component add constraint fk_ix_ginas_component_lastEdit_7 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_component_lastEdit_7 on ix_ginas_component (last_edited_by_id);
alter table ix_ginas_component add constraint fk_ix_ginas_component_substanc_8 foreign key (substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_component_substanc_8 on ix_ginas_component (substance_uuid);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_9 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_9 on ix_core_curation (curator_id);
alter table ix_ginas_disulfide add constraint fk_ix_ginas_disulfide_lastEdi_10 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_disulfide_lastEdi_10 on ix_ginas_disulfide (last_edited_by_id);
alter table ix_core_etag add constraint fk_ix_core_etag_namespace_11 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_etag_namespace_11 on ix_core_etag (namespace_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_12 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_12 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint fk_ix_core_edit_editor_13 foreign key (editor_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_edit_editor_13 on ix_core_edit (editor_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_14 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_14 on ix_core_figure (parent_id);
alter table ix_ginas_glycosylation add constraint fk_ix_ginas_glycosylation_las_15 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_glycosylation_las_15 on ix_ginas_glycosylation (last_edited_by_id);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organ_16 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organ_16 on ix_core_investigator (organization_id);
alter table ix_ginas_material add constraint fk_ix_ginas_material_lastEdit_17 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_material_lastEdit_17 on ix_ginas_material (last_edited_by_id);
alter table ix_ginas_material add constraint fk_ix_ginas_material_amount_18 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_material_amount_18 on ix_ginas_material (amount_uuid);
alter table ix_ginas_material add constraint fk_ix_ginas_material_monomerS_19 foreign key (monomer_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_material_monomerS_19 on ix_ginas_material (monomer_substance_uuid);
alter table ix_ginas_modifications add constraint fk_ix_ginas_modifications_las_20 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_modifications_las_20 on ix_ginas_modifications (last_edited_by_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_lastEdited_21 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_lastEdited_21 on ix_ginas_moiety (last_edited_by_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_structure_22 foreign key (structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_structure_22 on ix_ginas_moiety (structure_id);
alter table ix_ginas_name add constraint fk_ix_ginas_name_lastEditedBy_23 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_name_lastEditedBy_23 on ix_ginas_name (last_edited_by_id);
alter table ix_ginas_nameorg add constraint fk_ix_ginas_nameorg_lastEdite_24 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_nameorg_lastEdite_24 on ix_ginas_nameorg (last_edited_by_id);
alter table ix_core_namespace add constraint fk_ix_core_namespace_owner_25 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_namespace_owner_25 on ix_core_namespace (owner_id);
alter table ix_ginas_note add constraint fk_ix_ginas_note_lastEditedBy_26 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_note_lastEditedBy_26 on ix_ginas_note (last_edited_by_id);
alter table ix_ginas_otherlinks add constraint fk_ix_ginas_otherlinks_lastEd_27 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_otherlinks_lastEd_27 on ix_ginas_otherlinks (last_edited_by_id);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_lastEdi_28 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_lastEdi_28 on ix_ginas_parameter (last_edited_by_id);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_value_29 foreign key (value_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_value_29 on ix_ginas_parameter (value_uuid);
alter table ix_core_payload add constraint fk_ix_core_payload_namespace_30 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_payload_namespace_30 on ix_core_payload (namespace_id);
alter table ix_ginas_physical_modification add constraint fk_ix_ginas_physical_modifica_31 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physical_modifica_31 on ix_ginas_physical_modification (last_edited_by_id);
alter table ix_ginas_physical_parameter add constraint fk_ix_ginas_physical_paramete_32 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physical_paramete_32 on ix_ginas_physical_parameter (last_edited_by_id);
alter table ix_ginas_physical_parameter add constraint fk_ix_ginas_physical_paramete_33 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_physical_paramete_33 on ix_ginas_physical_parameter (amount_uuid);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_lastEdite_34 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_lastEdite_34 on ix_ginas_polymer (last_edited_by_id);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_classific_35 foreign key (classification_uuid) references ix_ginas_polymerclass (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_classific_35 on ix_ginas_polymer (classification_uuid);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_displaySt_36 foreign key (display_structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_displaySt_36 on ix_ginas_polymer (display_structure_id);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_idealized_37 foreign key (idealized_structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_idealized_37 on ix_ginas_polymer (idealized_structure_id);
alter table ix_ginas_polymerclass add constraint fk_ix_ginas_polymerclass_last_38 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymerclass_last_38 on ix_ginas_polymerclass (last_edited_by_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_namespac_39 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_namespac_39 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_subject_40 foreign key (subject_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_subject_40 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint fk_ix_core_principal_namespac_41 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_principal_namespac_41 on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_42 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_42 on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint fk_ix_core_principal_institut_43 foreign key (institution_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_principal_institut_43 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_owner_44 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_owner_44 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_payload_45 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_payload_45 on ix_core_procjob (payload_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_xref_46 foreign key (xref_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_xref_46 on ix_core_procrecord (xref_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_job_47 foreign key (job_id) references ix_core_procjob (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_job_47 on ix_core_procrecord (job_id);
alter table ix_ginas_property add constraint fk_ix_ginas_property_lastEdit_48 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_property_lastEdit_48 on ix_ginas_property (last_edited_by_id);
alter table ix_ginas_property add constraint fk_ix_ginas_property_value_49 foreign key (value_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_property_value_49 on ix_ginas_property (value_uuid);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_lastEdite_50 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_protein_lastEdite_50 on ix_ginas_protein (last_edited_by_id);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_glycosyla_51 foreign key (glycosylation_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_protein_glycosyla_51 on ix_ginas_protein (glycosylation_uuid);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_modificat_52 foreign key (modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_protein_modificat_52 on ix_ginas_protein (modifications_uuid);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_53 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_53 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journa_54 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journa_54 on ix_core_publication (journal_id);
alter table ix_ginas_reference add constraint fk_ix_ginas_reference_lastEdi_55 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_reference_lastEdi_55 on ix_ginas_reference (last_edited_by_id);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_last_56 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_last_56 on ix_ginas_relationship (last_edited_by_id);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_amou_57 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_amou_57 on ix_ginas_relationship (amount_uuid);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_rela_58 foreign key (related_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_rela_58 on ix_ginas_relationship (related_substance_uuid);
alter table ix_core_role add constraint fk_ix_core_role_principal_59 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_role_principal_59 on ix_core_role (principal_id);
alter table ix_ginas_site add constraint fk_ix_ginas_site_lastEditedBy_60 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_site_lastEditedBy_60 on ix_ginas_site (last_edited_by_id);
alter table ix_ginas_structural_modification add constraint fk_ix_ginas_structural_modifi_61 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_structural_modifi_61 on ix_ginas_structural_modification (last_edited_by_id);
alter table ix_ginas_structural_modification add constraint fk_ix_ginas_structural_modifi_62 foreign key (extent_amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structural_modifi_62 on ix_ginas_structural_modification (extent_amount_uuid);
alter table ix_ginas_structural_modification add constraint fk_ix_ginas_structural_modifi_63 foreign key (molecular_fragment_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structural_modifi_63 on ix_ginas_structural_modification (molecular_fragment_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_lastEdit_64 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_lastEdit_64 on ix_ginas_strucdiv (last_edited_by_id);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_hybridSp_65 foreign key (hybrid_species_paternal_organism_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_hybridSp_65 on ix_ginas_strucdiv (hybrid_species_paternal_organism_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_hybridSp_66 foreign key (hybrid_species_maternal_organism_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_hybridSp_66 on ix_ginas_strucdiv (hybrid_species_maternal_organism_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_parentSu_67 foreign key (parent_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_parentSu_67 on ix_ginas_strucdiv (parent_substance_uuid);
alter table ix_core_structure add constraint fk_ix_core_structure_namespac_68 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_structure_namespac_68 on ix_core_structure (namespace_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_lastEdi_69 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_lastEdi_69 on ix_ginas_substance (last_edited_by_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_modific_70 foreign key (modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_modific_70 on ix_ginas_substance (modifications_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_protein_71 foreign key (protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_protein_71 on ix_ginas_substance (protein_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_structu_72 foreign key (structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_structu_72 on ix_ginas_substance (structure_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_polymer_73 foreign key (polymer_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_polymer_73 on ix_ginas_substance (polymer_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_structu_74 foreign key (structurally_diverse_uuid) references ix_ginas_strucdiv (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_structu_74 on ix_ginas_substance (structurally_diverse_uuid);
alter table ix_ginas_substancereference add constraint fk_ix_ginas_substancereferenc_75 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substancereferenc_75 on ix_ginas_substancereference (last_edited_by_id);
alter table ix_ginas_subunit add constraint fk_ix_ginas_subunit_lastEdite_76 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_subunit_lastEdite_76 on ix_ginas_subunit (last_edited_by_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_lastEditedBy_77 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_unit_lastEditedBy_77 on ix_ginas_unit (last_edited_by_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_amap_78 foreign key (amap_id) references ix_core_value (id) on delete restrict on update restrict;
create index ix_ix_ginas_unit_amap_78 on ix_ginas_unit (amap_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_amount_79 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_unit_amount_79 on ix_ginas_unit (amount_uuid);
alter table ix_core_xref add constraint fk_ix_core_xref_namespace_80 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_xref_namespace_80 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_ginas_code_access add constraint fk_ix_ginas_code_access_ix_gi_01 foreign key (ix_ginas_code_uuid) references ix_ginas_code (uuid) on delete restrict on update restrict;

alter table ix_ginas_code_access add constraint fk_ix_ginas_code_access_ix_co_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_code_reference add constraint fk_ix_ginas_code_reference_ix_01 foreign key (ix_ginas_code_uuid) references ix_ginas_code (uuid) on delete restrict on update restrict;

alter table ix_ginas_code_reference add constraint fk_ix_ginas_code_reference_ix_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_disulfide_site add constraint fk_ix_ginas_disulfide_site_ix_01 foreign key (ix_ginas_disulfide_uuid) references ix_ginas_disulfide (uuid) on delete restrict on update restrict;

alter table ix_ginas_disulfide_site add constraint fk_ix_ginas_disulfide_site_ix_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_csite add constraint fk_ix_ginas_glycosylation_csi_01 foreign key (ix_ginas_glycosylation_c_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_csite add constraint fk_ix_ginas_glycosylation_csi_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_nsite add constraint fk_ix_ginas_glycosylation_nsi_01 foreign key (ix_ginas_glycosylation_n_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_nsite add constraint fk_ix_ginas_glycosylation_nsi_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_osite add constraint fk_ix_ginas_glycosylation_osi_01 foreign key (ix_ginas_glycosylation_o_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_osite add constraint fk_ix_ginas_glycosylation_osi_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_modifications_agent add constraint fk_ix_ginas_modifications_age_01 foreign key (ix_ginas_modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_agent add constraint fk_ix_ginas_modifications_age_02 foreign key (ix_ginas_agent_modification_uuid) references ix_ginas_agent_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_physical add constraint fk_ix_ginas_modifications_phy_01 foreign key (ix_ginas_modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_physical add constraint fk_ix_ginas_modifications_phy_02 foreign key (ix_ginas_physical_modification_uuid) references ix_ginas_physical_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_structural add constraint fk_ix_ginas_modifications_str_01 foreign key (ix_ginas_modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_structural add constraint fk_ix_ginas_modifications_str_02 foreign key (ix_ginas_structural_modification_uuid) references ix_ginas_structural_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_access add constraint fk_ix_ginas_name_access_ix_gi_01 foreign key (ix_ginas_name_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_access add constraint fk_ix_ginas_name_access_ix_co_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_name_domain add constraint fk_ix_ginas_name_domain_ix_gi_01 foreign key (ix_ginas_name_domain_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_domain add constraint fk_ix_ginas_name_domain_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_language add constraint fk_ix_ginas_name_language_ix__01 foreign key (ix_ginas_name_language_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_language add constraint fk_ix_ginas_name_language_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_jurisdiction add constraint fk_ix_ginas_name_jurisdiction_01 foreign key (ix_ginas_name_jurisdiction_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_jurisdiction add constraint fk_ix_ginas_name_jurisdiction_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_reference add constraint fk_ix_ginas_name_reference_ix_01 foreign key (ix_ginas_name_reference_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_reference add constraint fk_ix_ginas_name_reference_ix_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_nameorg add constraint fk_ix_ginas_name_nameorg_ix_g_01 foreign key (ix_ginas_name_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_nameorg add constraint fk_ix_ginas_name_nameorg_ix_g_02 foreign key (ix_ginas_nameorg_uuid) references ix_ginas_nameorg (uuid) on delete restrict on update restrict;

alter table ix_ginas_note_access add constraint fk_ix_ginas_note_access_ix_gi_01 foreign key (ix_ginas_note_uuid) references ix_ginas_note (uuid) on delete restrict on update restrict;

alter table ix_ginas_note_access add constraint fk_ix_ginas_note_access_ix_co_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_note_reference add constraint fk_ix_ginas_note_reference_ix_01 foreign key (ix_ginas_note_uuid) references ix_ginas_note (uuid) on delete restrict on update restrict;

alter table ix_ginas_note_reference add constraint fk_ix_ginas_note_reference_ix_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_otherlinks_site add constraint fk_ix_ginas_otherlinks_site_i_01 foreign key (ix_ginas_otherlinks_uuid) references ix_ginas_otherlinks (uuid) on delete restrict on update restrict;

alter table ix_ginas_otherlinks_site add constraint fk_ix_ginas_otherlinks_site_i_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_i_01 foreign key (ix_core_payload_id) references ix_core_payload (id) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_i_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_physical_modification_param add constraint fk_ix_ginas_physical_modifica_01 foreign key (ix_ginas_physical_modification_uuid) references ix_ginas_physical_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_physical_modification_param add constraint fk_ix_ginas_physical_modifica_02 foreign key (ix_ginas_physical_parameter_uuid) references ix_ginas_physical_parameter (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_material add constraint fk_ix_ginas_polymer_material__01 foreign key (ix_ginas_polymer_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_material add constraint fk_ix_ginas_polymer_material__02 foreign key (ix_ginas_material_uuid) references ix_ginas_material (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_unit add constraint fk_ix_ginas_polymer_unit_ix_g_01 foreign key (ix_ginas_polymer_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_unit add constraint fk_ix_ginas_polymer_unit_ix_g_02 foreign key (ix_ginas_unit_uuid) references ix_ginas_unit (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymerclass_sub add constraint fk_ix_ginas_polymerclass_sub__01 foreign key (ix_ginas_polymerclass_uuid) references ix_ginas_polymerclass (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymerclass_sub add constraint fk_ix_ginas_polymerclass_sub__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_property_parameter add constraint fk_ix_ginas_property_paramete_01 foreign key (ix_ginas_property_uuid) references ix_ginas_property (uuid) on delete restrict on update restrict;

alter table ix_ginas_property_parameter add constraint fk_ix_ginas_property_paramete_02 foreign key (ix_ginas_parameter_uuid) references ix_ginas_parameter (uuid) on delete restrict on update restrict;

alter table ix_ginas_property_reference add constraint fk_ix_ginas_property_referenc_01 foreign key (ix_ginas_property_uuid) references ix_ginas_property (uuid) on delete restrict on update restrict;

alter table ix_ginas_property_reference add constraint fk_ix_ginas_property_referenc_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_protein_disulfide add constraint fk_ix_ginas_protein_disulfide_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_disulfide add constraint fk_ix_ginas_protein_disulfide_02 foreign key (ix_ginas_disulfide_uuid) references ix_ginas_disulfide (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_i_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_i_02 foreign key (ix_ginas_subunit_uuid) references ix_ginas_subunit (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_otherlinks add constraint fk_ix_ginas_protein_otherlink_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_otherlinks add constraint fk_ix_ginas_protein_otherlink_02 foreign key (ix_ginas_otherlinks_uuid) references ix_ginas_otherlinks (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_reference add constraint fk_ix_ginas_protein_reference_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_reference add constraint fk_ix_ginas_protein_reference_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keywor_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keywor_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_i_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_i_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_02 foreign key (ix_core_pubauthor_id) references ix_core_pubauthor (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_ginas_reference_access add constraint fk_ix_ginas_reference_access__01 foreign key (ix_ginas_reference_uuid) references ix_ginas_reference (uuid) on delete restrict on update restrict;

alter table ix_ginas_reference_access add constraint fk_ix_ginas_reference_access__02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_reference_tag add constraint fk_ix_ginas_reference_tag_ix__01 foreign key (ix_ginas_reference_uuid) references ix_ginas_reference (uuid) on delete restrict on update restrict;

alter table ix_ginas_reference_tag add constraint fk_ix_ginas_reference_tag_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_relationship_access add constraint fk_ix_ginas_relationship_acce_01 foreign key (ix_ginas_relationship_uuid) references ix_ginas_relationship (uuid) on delete restrict on update restrict;

alter table ix_ginas_relationship_access add constraint fk_ix_ginas_relationship_acce_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_relationship_reference add constraint fk_ix_ginas_relationship_refe_01 foreign key (ix_ginas_relationship_uuid) references ix_ginas_relationship (uuid) on delete restrict on update restrict;

alter table ix_ginas_relationship_reference add constraint fk_ix_ginas_relationship_refe_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_i_01 foreign key (ix_core_stitch_id) references ix_core_stitch (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_i_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_ginas_structural_modification_1 add constraint fk_ix_ginas_structural_modifi_01 foreign key (ix_ginas_structural_modification_uuid) references ix_ginas_structural_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_structural_modification_1 add constraint fk_ix_ginas_structural_modifi_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_ginas_strucdiv_part add constraint fk_ix_ginas_strucdiv_part_ix__01 foreign key (ix_ginas_strucdiv_uuid) references ix_ginas_strucdiv (uuid) on delete restrict on update restrict;

alter table ix_ginas_strucdiv_part add constraint fk_ix_ginas_strucdiv_part_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix__01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix__02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_ginas_substance_access add constraint fk_ix_ginas_substance_access__01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_access add constraint fk_ix_ginas_substance_access__02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_substance_name add constraint fk_ix_ginas_substance_name_ix_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_name add constraint fk_ix_ginas_substance_name_ix_02 foreign key (ix_ginas_name_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_code add constraint fk_ix_ginas_substance_code_ix_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_code add constraint fk_ix_ginas_substance_code_ix_02 foreign key (ix_ginas_code_uuid) references ix_ginas_code (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_note add constraint fk_ix_ginas_substance_note_ix_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_note add constraint fk_ix_ginas_substance_note_ix_02 foreign key (ix_ginas_note_uuid) references ix_ginas_note (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_property add constraint fk_ix_ginas_substance_propert_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_property add constraint fk_ix_ginas_substance_propert_02 foreign key (ix_ginas_property_uuid) references ix_ginas_property (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_relationship add constraint fk_ix_ginas_substance_relatio_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_relationship add constraint fk_ix_ginas_substance_relatio_02 foreign key (ix_ginas_relationship_uuid) references ix_ginas_relationship (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_reference add constraint fk_ix_ginas_substance_referen_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_reference add constraint fk_ix_ginas_substance_referen_02 foreign key (ix_ginas_reference_uuid) references ix_ginas_reference (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_tag add constraint fk_ix_ginas_substance_tag_ix__01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_tag add constraint fk_ix_ginas_substance_tag_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_chemical_moiety add constraint fk_ix_ginas_chemical_moiety_i_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_chemical_moiety add constraint fk_ix_ginas_chemical_moiety_i_02 foreign key (ix_ginas_moiety_uuid) references ix_ginas_moiety (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_component add constraint fk_ix_ginas_substance_compone_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_component add constraint fk_ix_ginas_substance_compone_02 foreign key (ix_ginas_component_uuid) references ix_ginas_component (uuid) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_01 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ix_core_acl;

drop table if exists ix_core_acl_principal;

drop table if exists ix_core_acl_group;

drop table if exists ix_ginas_agent_modification;

drop table if exists ix_ginas_amount;

drop table if exists ix_core_attribute;

drop table if exists ix_ginas_code;

drop table if exists ix_ginas_code_access;

drop table if exists ix_ginas_code_reference;

drop table if exists ix_ginas_component;

drop table if exists ix_core_curation;

drop table if exists ix_ginas_disulfide;

drop table if exists ix_ginas_disulfide_site;

drop table if exists ix_core_etag;

drop table if exists ix_core_etagref;

drop table if exists ix_core_edit;

drop table if exists ix_core_event;

drop table if exists ix_core_event_figure;

drop table if exists ix_core_figure;

drop table if exists ix_ginas_glycosylation;

drop table if exists ix_ginas_glycosylation_csite;

drop table if exists ix_ginas_glycosylation_nsite;

drop table if exists ix_ginas_glycosylation_osite;

drop table if exists ix_core_group;

drop table if exists ix_core_group_principal;

drop table if exists ix_core_investigator;

drop table if exists ix_core_journal;

drop table if exists ix_ginas_material;

drop table if exists ix_ginas_modifications;

drop table if exists ix_ginas_modifications_agent;

drop table if exists ix_ginas_modifications_physical;

drop table if exists ix_ginas_modifications_structural;

drop table if exists ix_ginas_moiety;

drop table if exists ix_ginas_name;

drop table if exists ix_ginas_name_access;

drop table if exists ix_ginas_name_domain;

drop table if exists ix_ginas_name_language;

drop table if exists ix_ginas_name_jurisdiction;

drop table if exists ix_ginas_name_reference;

drop table if exists ix_ginas_name_nameorg;

drop table if exists ix_ginas_nameorg;

drop table if exists ix_core_namespace;

drop table if exists ix_ginas_note;

drop table if exists ix_ginas_note_access;

drop table if exists ix_ginas_note_reference;

drop table if exists ix_core_organization;

drop table if exists ix_ginas_otherlinks;

drop table if exists ix_ginas_otherlinks_site;

drop table if exists ix_ginas_parameter;

drop table if exists ix_core_payload;

drop table if exists ix_core_payload_property;

drop table if exists ix_ginas_physical_modification;

drop table if exists ix_ginas_physical_modification_param;

drop table if exists ix_ginas_physical_parameter;

drop table if exists ix_ginas_polymer;

drop table if exists ix_ginas_polymer_material;

drop table if exists ix_ginas_polymer_unit;

drop table if exists ix_ginas_polymerclass;

drop table if exists ix_ginas_polymerclass_sub;

drop table if exists ix_core_predicate;

drop table if exists ix_core_predicate_object;

drop table if exists ix_core_predicate_property;

drop table if exists ix_core_principal;

drop table if exists ix_core_procjob;

drop table if exists ix_core_procrecord;

drop table if exists ix_ginas_property;

drop table if exists ix_ginas_property_parameter;

drop table if exists ix_ginas_property_reference;

drop table if exists ix_ginas_protein;

drop table if exists ix_ginas_protein_disulfide;

drop table if exists ix_ginas_protein_subunit;

drop table if exists ix_ginas_protein_otherlinks;

drop table if exists ix_ginas_protein_reference;

drop table if exists ix_core_pubauthor;

drop table if exists ix_core_publication;

drop table if exists ix_core_publication_keyword;

drop table if exists ix_core_publication_mesh;

drop table if exists ix_core_publication_author;

drop table if exists ix_core_publication_figure;

drop table if exists ix_ginas_reference;

drop table if exists ix_ginas_reference_access;

drop table if exists ix_ginas_reference_tag;

drop table if exists ix_ginas_relationship;

drop table if exists ix_ginas_relationship_access;

drop table if exists ix_ginas_relationship_reference;

drop table if exists ix_core_role;

drop table if exists ix_ginas_site;

drop table if exists ix_core_stitch;

drop table if exists ix_core_stitch_attribute;

drop table if exists ix_ginas_structural_modification;

drop table if exists ix_ginas_structural_modification_1;

drop table if exists ix_ginas_strucdiv;

drop table if exists ix_ginas_strucdiv_part;

drop table if exists ix_core_structure;

drop table if exists ix_core_structure_property;

drop table if exists ix_core_structure_link;

drop table if exists ix_ginas_substance;

drop table if exists ix_ginas_substance_access;

drop table if exists ix_ginas_substance_name;

drop table if exists ix_ginas_substance_code;

drop table if exists ix_ginas_substance_note;

drop table if exists ix_ginas_substance_property;

drop table if exists ix_ginas_substance_relationship;

drop table if exists ix_ginas_substance_reference;

drop table if exists ix_ginas_substance_tag;

drop table if exists ix_ginas_substancereference;

drop table if exists ix_ginas_subunit;

drop table if exists ix_ginas_unit;

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

