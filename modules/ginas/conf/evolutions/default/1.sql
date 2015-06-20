# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint auto_increment not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_ginas_agent_modification (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  agent_modification_process varchar(255),
  agent_modification_role   varchar(255),
  agent_modification_type   varchar(255),
  agent_substance_uuid      varchar(40),
  amount_uuid               varchar(40),
  constraint pk_ix_ginas_agent_modification primary key (uuid))
;

create table ix_ginas_amount (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
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
  id                        bigint auto_increment not null,
  name                      varchar(255),
  value                     varchar(1024),
  namespace_id              bigint,
  constraint pk_ix_core_attribute primary key (id))
;

create table ix_ginas_code (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  code_system               varchar(255),
  code                      varchar(255) not null,
  comments                  longtext,
  type                      varchar(255),
  url                       longtext,
  constraint pk_ix_ginas_code primary key (uuid))
;

create table ix_ginas_component (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  type                      varchar(255),
  substance_uuid            varchar(40),
  constraint pk_ix_ginas_component primary key (uuid))
;

create table ix_core_curation (
  id                        bigint auto_increment not null,
  curator_id                bigint,
  status                    integer,
  timestamp                 datetime,
  constraint ck_ix_core_curation_status check (status in (0,1,2,3)),
  constraint pk_ix_core_curation primary key (id))
;

create table ix_ginas_disulfide (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  constraint pk_ix_ginas_disulfide primary key (uuid))
;

create table ix_core_etag (
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
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
  id                        bigint auto_increment not null,
  etag_id                   bigint,
  ref_id                    bigint,
  constraint pk_ix_core_etagref primary key (id))
;

create table ix_core_edit (
  id                        varchar(40) not null,
  created                   datetime,
  refid                     varchar(255),
  kind                      varchar(255),
  editor_id                 bigint,
  path                      varchar(1024),
  comments                  longtext,
  old_value                 longtext,
  new_value                 longtext,
  constraint pk_ix_core_edit primary key (id))
;

create table ix_core_event (
  id                        bigint auto_increment not null,
  title                     varchar(1024),
  description               longtext,
  url                       varchar(1024),
  event_start               datetime,
  event_end                 datetime,
  is_duration               tinyint(1) default 0,
  constraint pk_ix_core_event primary key (id))
;

create table ix_core_figure (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  caption                   varchar(255),
  mime_type                 varchar(255),
  url                       varchar(1024),
  data                      longblob,
  data_size                 integer,
  sha1                      varchar(140),
  parent_id                 bigint,
  constraint pk_ix_core_figure primary key (id))
;

create table ix_ginas_glycosylation (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  glycosylation_type        varchar(255),
  constraint pk_ix_ginas_glycosylation primary key (uuid))
;

create table ix_core_group (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  constraint uq_ix_core_group_name unique (name),
  constraint pk_ix_core_group primary key (id))
;

create table ix_core_investigator (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  pi_id                     bigint,
  organization_id           bigint,
  role                      integer,
  constraint ck_ix_core_investigator_role check (role in (0,1)),
  constraint pk_ix_core_investigator primary key (id))
;

create table ix_core_journal (
  id                        bigint auto_increment not null,
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
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  amount_uuid               varchar(40),
  monomer_substance_uuid    varchar(40),
  type                      varchar(255),
  defining                  tinyint(1) default 0,
  constraint pk_ix_ginas_material primary key (uuid))
;

create table ix_ginas_modifications (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  constraint pk_ix_ginas_modifications primary key (uuid))
;

create table ix_ginas_moiety (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  structure_id              bigint,
  count                     integer,
  constraint pk_ix_ginas_moiety primary key (uuid))
;

create table ix_ginas_name (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  name                      varchar(255) not null,
  full_name                 longtext,
  type                      varchar(32),
  preferred                 tinyint(1) default 0,
  constraint pk_ix_ginas_name primary key (uuid))
;

create table ix_ginas_nameorg (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  name_org                  varchar(255) not null,
  deprecated_date           datetime,
  constraint pk_ix_ginas_nameorg primary key (uuid))
;

create table ix_core_namespace (
  id                        bigint auto_increment not null,
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
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  note                      longtext,
  constraint pk_ix_ginas_note primary key (uuid))
;

create table ix_core_organization (
  id                        bigint auto_increment not null,
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
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  linkage_type              varchar(255),
  constraint pk_ix_ginas_otherlinks primary key (uuid))
;

create table ix_ginas_parameter (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  name                      varchar(255) not null,
  type                      varchar(255),
  value_uuid                varchar(40),
  constraint pk_ix_ginas_parameter primary key (uuid))
;

create table ix_core_payload (
  id                        varchar(40) not null,
  namespace_id              bigint,
  created                   datetime,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime_type                 varchar(128),
  capacity                  bigint,
  constraint pk_ix_core_payload primary key (id))
;

create table ix_ginas_physical_modification (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  physical_modification_role varchar(255) not null,
  constraint pk_ix_ginas_physical_modification primary key (uuid))
;

create table ix_ginas_physical_parameter (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  parameter_name            varchar(255),
  amount_uuid               varchar(40),
  constraint pk_ix_ginas_physical_parameter primary key (uuid))
;

create table ix_ginas_polymer (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  classification_uuid       varchar(40),
  display_structure_id      bigint,
  idealized_structure_id    bigint,
  constraint pk_ix_ginas_polymer primary key (uuid))
;

create table ix_ginas_polymerclass (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  polymer_class             varchar(255),
  polymer_geometry          varchar(255),
  source_type               varchar(255),
  constraint pk_ix_ginas_polymerclass primary key (uuid))
;

create table ix_core_predicate (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
  subject_id                bigint,
  predicate                 varchar(255) not null,
  version                   bigint not null,
  constraint pk_ix_core_predicate primary key (id))
;

create table ix_core_principal (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
  provider                  varchar(255),
  username                  varchar(255),
  email                     varchar(255),
  admin                     tinyint(1) default 0,
  uri                       varchar(1024),
  selfie_id                 bigint,
  version                   bigint not null,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  prefname                  varchar(255),
  suffix                    varchar(20),
  affiliation               longtext,
  orcid                     varchar(255),
  institution_id            bigint,
  constraint pk_ix_core_principal primary key (id))
;

create table ix_core_procjob (
  id                        bigint auto_increment not null,
  jobkey                    varchar(255) not null,
  invoker                   varchar(255),
  status                    integer,
  job_start                 bigint,
  job_stop                  bigint,
  message                   longtext,
  owner_id                  bigint,
  payload_id                varchar(40),
  constraint ck_ix_core_procjob_status check (status in (0,1,2,3,4,5,6)),
  constraint uq_ix_core_procjob_jobkey unique (jobkey),
  constraint pk_ix_core_procjob primary key (id))
;

create table ix_core_procrecord (
  id                        bigint auto_increment not null,
  rec_start                 bigint,
  rec_stop                  bigint,
  name                      varchar(128),
  status                    integer,
  message                   longtext,
  xref_id                   bigint,
  job_id                    bigint,
  constraint ck_ix_core_procrecord_status check (status in (0,1,2,3)),
  constraint pk_ix_core_procrecord primary key (id))
;

create table ix_ginas_property (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  name                      varchar(255) not null,
  type                      varchar(255),
  property_type             varchar(255),
  value_uuid                varchar(40),
  defining                  tinyint(1) default 0,
  constraint pk_ix_ginas_property primary key (uuid))
;

create table ix_ginas_protein (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  protein_type              varchar(255),
  protein_sub_type          varchar(255),
  sequence_origin           varchar(255),
  sequence_type             varchar(255),
  glycosylation_uuid        varchar(40),
  modifications_uuid        varchar(40),
  constraint pk_ix_ginas_protein primary key (uuid))
;

create table ix_core_pubauthor (
  id                        bigint auto_increment not null,
  position                  integer,
  is_last                   tinyint(1) default 0,
  correspondence            tinyint(1) default 0,
  author_id                 bigint,
  constraint pk_ix_core_pubauthor primary key (id))
;

create table ix_core_publication (
  id                        bigint auto_increment not null,
  pmid                      bigint,
  pmcid                     varchar(255),
  title                     longtext,
  pages                     varchar(255),
  doi                       varchar(255),
  abstract_text             longtext,
  journal_id                bigint,
  constraint uq_ix_core_publication_pmid unique (pmid),
  constraint uq_ix_core_publication_pmcid unique (pmcid),
  constraint pk_ix_core_publication primary key (id))
;

create table ix_ginas_reference (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  citation                  varchar(255) not null,
  doc_type                  varchar(255),
  document_date             datetime,
  public_domain             tinyint(1) default 0,
  uploaded_file             varchar(1024),
  id                        varchar(255),
  url                       longtext,
  constraint pk_ix_ginas_reference primary key (uuid))
;

create table ix_ginas_relationship (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  amount_uuid               varchar(40),
  comments                  longtext,
  interaction_type          varchar(255),
  qualification             varchar(255),
  related_substance_uuid    varchar(40),
  type                      varchar(255),
  constraint pk_ix_ginas_relationship primary key (uuid))
;

create table ix_core_role (
  id                        bigint auto_increment not null,
  role                      integer,
  principal_id              bigint,
  constraint ck_ix_core_role_role check (role in (0,1,2,3)),
  constraint pk_ix_core_role primary key (id))
;

create table ix_ginas_site (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  subunit_index             integer,
  residue_index             integer,
  constraint pk_ix_ginas_site primary key (uuid))
;

create table ix_ginas_specifiedsubstancecomponent (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  type                      varchar(255),
  substance_uuid            varchar(40),
  role                      varchar(255),
  constraint pk_ix_ginas_specifiedsubstancecomponent primary key (uuid))
;

create table ix_core_stitch (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  impl                      varchar(1024),
  description               longtext,
  constraint pk_ix_core_stitch primary key (id))
;

create table ix_ginas_structural_modification (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  structural_modification_type varchar(255) not null,
  location_type             varchar(255),
  residue_modified          varchar(255),
  extent                    varchar(255),
  extent_amount_uuid        varchar(40),
  molecular_fragment_uuid   varchar(40),
  constraint pk_ix_ginas_structural_modification primary key (uuid))
;

create table ix_ginas_strucdiv (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
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
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
  digest                    varchar(128),
  molfile                   longtext,
  smiles                    longtext,
  formula                   varchar(255),
  stereo                    integer,
  optical                   integer,
  atropi                    integer,
  stereo_comments           longtext,
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
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  substance_class           integer,
  status                    varchar(255),
  approved_by               varchar(255),
  approved                  datetime,
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
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  ref_pname                 varchar(255),
  refuuid                   varchar(128) not null,
  substance_class           varchar(255),
  approval_id               varchar(32),
  constraint pk_ix_ginas_substancereference primary key (uuid))
;

create table ix_ginas_subunit (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  sequence                  longtext,
  subunit_index             integer,
  constraint pk_ix_ginas_subunit primary key (uuid))
;

create table ix_ginas_unit (
  uuid                      varchar(40) not null,
  created                   datetime,
  last_modified             datetime,
  last_edited_by_id         bigint,
  deprecated                tinyint(1) default 0,
  amap_id                   bigint,
  amount_uuid               varchar(40),
  attachment_count          integer,
  label                     varchar(255),
  structure                 longtext,
  type                      varchar(255),
  constraint pk_ix_ginas_unit primary key (uuid))
;

create table ix_core_value (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  label                     varchar(255),
  text                      longtext,
  numval                    double,
  unit                      varchar(255),
  data                      longblob,
  data_size                 integer,
  sha1                      varchar(40),
  mime_type                 varchar(32),
  term                      varchar(255),
  href                      longtext,
  lval                      double,
  rval                      double,
  average                   double,
  strval                    varchar(1024),
  intval                    bigint,
  major_topic               tinyint(1) default 0,
  heading                   varchar(1024),
  constraint pk_ix_core_value primary key (id))
;

create table ix_core_xref (
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
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
alter table ix_ginas_agent_modification add constraint fk_ix_ginas_agent_modification_lastEditedBy_1 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_agent_modification_lastEditedBy_1 on ix_ginas_agent_modification (last_edited_by_id);
alter table ix_ginas_agent_modification add constraint fk_ix_ginas_agent_modification_agentSubstance_2 foreign key (agent_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_agent_modification_agentSubstance_2 on ix_ginas_agent_modification (agent_substance_uuid);
alter table ix_ginas_agent_modification add constraint fk_ix_ginas_agent_modification_amount_3 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_agent_modification_amount_3 on ix_ginas_agent_modification (amount_uuid);
alter table ix_ginas_amount add constraint fk_ix_ginas_amount_lastEditedBy_4 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_amount_lastEditedBy_4 on ix_ginas_amount (last_edited_by_id);
alter table ix_core_attribute add constraint fk_ix_core_attribute_namespace_5 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_namespace_5 on ix_core_attribute (namespace_id);
alter table ix_ginas_code add constraint fk_ix_ginas_code_lastEditedBy_6 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_code_lastEditedBy_6 on ix_ginas_code (last_edited_by_id);
alter table ix_ginas_component add constraint fk_ix_ginas_component_lastEditedBy_7 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_component_lastEditedBy_7 on ix_ginas_component (last_edited_by_id);
alter table ix_ginas_component add constraint fk_ix_ginas_component_substance_8 foreign key (substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_component_substance_8 on ix_ginas_component (substance_uuid);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_9 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_9 on ix_core_curation (curator_id);
alter table ix_ginas_disulfide add constraint fk_ix_ginas_disulfide_lastEditedBy_10 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_disulfide_lastEditedBy_10 on ix_ginas_disulfide (last_edited_by_id);
alter table ix_core_etag add constraint fk_ix_core_etag_namespace_11 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_etag_namespace_11 on ix_core_etag (namespace_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_12 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_12 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint fk_ix_core_edit_editor_13 foreign key (editor_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_edit_editor_13 on ix_core_edit (editor_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_14 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_14 on ix_core_figure (parent_id);
alter table ix_ginas_glycosylation add constraint fk_ix_ginas_glycosylation_lastEditedBy_15 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_glycosylation_lastEditedBy_15 on ix_ginas_glycosylation (last_edited_by_id);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organization_16 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organization_16 on ix_core_investigator (organization_id);
alter table ix_ginas_material add constraint fk_ix_ginas_material_lastEditedBy_17 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_material_lastEditedBy_17 on ix_ginas_material (last_edited_by_id);
alter table ix_ginas_material add constraint fk_ix_ginas_material_amount_18 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_material_amount_18 on ix_ginas_material (amount_uuid);
alter table ix_ginas_material add constraint fk_ix_ginas_material_monomerSubstance_19 foreign key (monomer_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_material_monomerSubstance_19 on ix_ginas_material (monomer_substance_uuid);
alter table ix_ginas_modifications add constraint fk_ix_ginas_modifications_lastEditedBy_20 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_modifications_lastEditedBy_20 on ix_ginas_modifications (last_edited_by_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_lastEditedBy_21 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_lastEditedBy_21 on ix_ginas_moiety (last_edited_by_id);
alter table ix_ginas_moiety add constraint fk_ix_ginas_moiety_structure_22 foreign key (structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_moiety_structure_22 on ix_ginas_moiety (structure_id);
alter table ix_ginas_name add constraint fk_ix_ginas_name_lastEditedBy_23 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_name_lastEditedBy_23 on ix_ginas_name (last_edited_by_id);
alter table ix_ginas_nameorg add constraint fk_ix_ginas_nameorg_lastEditedBy_24 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_nameorg_lastEditedBy_24 on ix_ginas_nameorg (last_edited_by_id);
alter table ix_core_namespace add constraint fk_ix_core_namespace_owner_25 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_namespace_owner_25 on ix_core_namespace (owner_id);
alter table ix_ginas_note add constraint fk_ix_ginas_note_lastEditedBy_26 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_note_lastEditedBy_26 on ix_ginas_note (last_edited_by_id);
alter table ix_ginas_otherlinks add constraint fk_ix_ginas_otherlinks_lastEditedBy_27 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_otherlinks_lastEditedBy_27 on ix_ginas_otherlinks (last_edited_by_id);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_lastEditedBy_28 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_lastEditedBy_28 on ix_ginas_parameter (last_edited_by_id);
alter table ix_ginas_parameter add constraint fk_ix_ginas_parameter_value_29 foreign key (value_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_parameter_value_29 on ix_ginas_parameter (value_uuid);
alter table ix_core_payload add constraint fk_ix_core_payload_namespace_30 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_payload_namespace_30 on ix_core_payload (namespace_id);
alter table ix_ginas_physical_modification add constraint fk_ix_ginas_physical_modification_lastEditedBy_31 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physical_modification_lastEditedBy_31 on ix_ginas_physical_modification (last_edited_by_id);
alter table ix_ginas_physical_parameter add constraint fk_ix_ginas_physical_parameter_lastEditedBy_32 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_physical_parameter_lastEditedBy_32 on ix_ginas_physical_parameter (last_edited_by_id);
alter table ix_ginas_physical_parameter add constraint fk_ix_ginas_physical_parameter_amount_33 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_physical_parameter_amount_33 on ix_ginas_physical_parameter (amount_uuid);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_lastEditedBy_34 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_lastEditedBy_34 on ix_ginas_polymer (last_edited_by_id);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_classification_35 foreign key (classification_uuid) references ix_ginas_polymerclass (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_classification_35 on ix_ginas_polymer (classification_uuid);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_displayStructure_36 foreign key (display_structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_displayStructure_36 on ix_ginas_polymer (display_structure_id);
alter table ix_ginas_polymer add constraint fk_ix_ginas_polymer_idealizedStructure_37 foreign key (idealized_structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymer_idealizedStructure_37 on ix_ginas_polymer (idealized_structure_id);
alter table ix_ginas_polymerclass add constraint fk_ix_ginas_polymerclass_lastEditedBy_38 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_polymerclass_lastEditedBy_38 on ix_ginas_polymerclass (last_edited_by_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_namespace_39 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_namespace_39 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_subject_40 foreign key (subject_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_subject_40 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint fk_ix_core_principal_namespace_41 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_principal_namespace_41 on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_42 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_42 on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint fk_ix_core_principal_institution_43 foreign key (institution_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_principal_institution_43 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_owner_44 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_owner_44 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_payload_45 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_payload_45 on ix_core_procjob (payload_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_xref_46 foreign key (xref_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_xref_46 on ix_core_procrecord (xref_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_job_47 foreign key (job_id) references ix_core_procjob (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_job_47 on ix_core_procrecord (job_id);
alter table ix_ginas_property add constraint fk_ix_ginas_property_lastEditedBy_48 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_property_lastEditedBy_48 on ix_ginas_property (last_edited_by_id);
alter table ix_ginas_property add constraint fk_ix_ginas_property_value_49 foreign key (value_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_property_value_49 on ix_ginas_property (value_uuid);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_lastEditedBy_50 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_protein_lastEditedBy_50 on ix_ginas_protein (last_edited_by_id);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_glycosylation_51 foreign key (glycosylation_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_protein_glycosylation_51 on ix_ginas_protein (glycosylation_uuid);
alter table ix_ginas_protein add constraint fk_ix_ginas_protein_modifications_52 foreign key (modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_protein_modifications_52 on ix_ginas_protein (modifications_uuid);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_53 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_53 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journal_54 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journal_54 on ix_core_publication (journal_id);
alter table ix_ginas_reference add constraint fk_ix_ginas_reference_lastEditedBy_55 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_reference_lastEditedBy_55 on ix_ginas_reference (last_edited_by_id);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_lastEditedBy_56 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_lastEditedBy_56 on ix_ginas_relationship (last_edited_by_id);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_amount_57 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_amount_57 on ix_ginas_relationship (amount_uuid);
alter table ix_ginas_relationship add constraint fk_ix_ginas_relationship_relatedSubstance_58 foreign key (related_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_relationship_relatedSubstance_58 on ix_ginas_relationship (related_substance_uuid);
alter table ix_core_role add constraint fk_ix_core_role_principal_59 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_role_principal_59 on ix_core_role (principal_id);
alter table ix_ginas_site add constraint fk_ix_ginas_site_lastEditedBy_60 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_site_lastEditedBy_60 on ix_ginas_site (last_edited_by_id);
alter table ix_ginas_specifiedsubstancecomponent add constraint fk_ix_ginas_specifiedsubstancecomponent_lastEditedBy_61 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_specifiedsubstancecomponent_lastEditedBy_61 on ix_ginas_specifiedsubstancecomponent (last_edited_by_id);
alter table ix_ginas_specifiedsubstancecomponent add constraint fk_ix_ginas_specifiedsubstancecomponent_substance_62 foreign key (substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_specifiedsubstancecomponent_substance_62 on ix_ginas_specifiedsubstancecomponent (substance_uuid);
alter table ix_ginas_structural_modification add constraint fk_ix_ginas_structural_modification_lastEditedBy_63 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_structural_modification_lastEditedBy_63 on ix_ginas_structural_modification (last_edited_by_id);
alter table ix_ginas_structural_modification add constraint fk_ix_ginas_structural_modification_extentAmount_64 foreign key (extent_amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structural_modification_extentAmount_64 on ix_ginas_structural_modification (extent_amount_uuid);
alter table ix_ginas_structural_modification add constraint fk_ix_ginas_structural_modification_molecularFragment_65 foreign key (molecular_fragment_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_structural_modification_molecularFragment_65 on ix_ginas_structural_modification (molecular_fragment_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_lastEditedBy_66 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_lastEditedBy_66 on ix_ginas_strucdiv (last_edited_by_id);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_hybridSpeciesPaternalOrganism_67 foreign key (hybrid_species_paternal_organism_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_hybridSpeciesPaternalOrganism_67 on ix_ginas_strucdiv (hybrid_species_paternal_organism_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_hybridSpeciesMaternalOrganism_68 foreign key (hybrid_species_maternal_organism_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_hybridSpeciesMaternalOrganism_68 on ix_ginas_strucdiv (hybrid_species_maternal_organism_uuid);
alter table ix_ginas_strucdiv add constraint fk_ix_ginas_strucdiv_parentSubstance_69 foreign key (parent_substance_uuid) references ix_ginas_substancereference (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_strucdiv_parentSubstance_69 on ix_ginas_strucdiv (parent_substance_uuid);
alter table ix_core_structure add constraint fk_ix_core_structure_namespace_70 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_structure_namespace_70 on ix_core_structure (namespace_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_lastEditedBy_71 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_lastEditedBy_71 on ix_ginas_substance (last_edited_by_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_modifications_72 foreign key (modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_modifications_72 on ix_ginas_substance (modifications_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_protein_73 foreign key (protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_protein_73 on ix_ginas_substance (protein_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_structure_74 foreign key (structure_id) references ix_core_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_structure_74 on ix_ginas_substance (structure_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_polymer_75 foreign key (polymer_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_polymer_75 on ix_ginas_substance (polymer_uuid);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_structurallyDiverse_76 foreign key (structurally_diverse_uuid) references ix_ginas_strucdiv (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_substance_structurallyDiverse_76 on ix_ginas_substance (structurally_diverse_uuid);
alter table ix_ginas_substancereference add constraint fk_ix_ginas_substancereference_lastEditedBy_77 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_substancereference_lastEditedBy_77 on ix_ginas_substancereference (last_edited_by_id);
alter table ix_ginas_subunit add constraint fk_ix_ginas_subunit_lastEditedBy_78 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_subunit_lastEditedBy_78 on ix_ginas_subunit (last_edited_by_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_lastEditedBy_79 foreign key (last_edited_by_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_ginas_unit_lastEditedBy_79 on ix_ginas_unit (last_edited_by_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_amap_80 foreign key (amap_id) references ix_core_value (id) on delete restrict on update restrict;
create index ix_ix_ginas_unit_amap_80 on ix_ginas_unit (amap_id);
alter table ix_ginas_unit add constraint fk_ix_ginas_unit_amount_81 foreign key (amount_uuid) references ix_ginas_amount (uuid) on delete restrict on update restrict;
create index ix_ix_ginas_unit_amount_81 on ix_ginas_unit (amount_uuid);
alter table ix_core_xref add constraint fk_ix_core_xref_namespace_82 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_xref_namespace_82 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_core_acl_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core_acl_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core_group_02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_ginas_code_access add constraint fk_ix_ginas_code_access_ix_ginas_code_01 foreign key (ix_ginas_code_uuid) references ix_ginas_code (uuid) on delete restrict on update restrict;

alter table ix_ginas_code_access add constraint fk_ix_ginas_code_access_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_code_reference add constraint fk_ix_ginas_code_reference_ix_ginas_code_01 foreign key (ix_ginas_code_uuid) references ix_ginas_code (uuid) on delete restrict on update restrict;

alter table ix_ginas_code_reference add constraint fk_ix_ginas_code_reference_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_disulfide_site add constraint fk_ix_ginas_disulfide_site_ix_ginas_disulfide_01 foreign key (ix_ginas_disulfide_uuid) references ix_ginas_disulfide (uuid) on delete restrict on update restrict;

alter table ix_ginas_disulfide_site add constraint fk_ix_ginas_disulfide_site_ix_ginas_site_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_core_event_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_core_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_csite add constraint fk_ix_ginas_glycosylation_csite_ix_ginas_glycosylation_01 foreign key (ix_ginas_glycosylation_c_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_csite add constraint fk_ix_ginas_glycosylation_csite_ix_ginas_site_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_nsite add constraint fk_ix_ginas_glycosylation_nsite_ix_ginas_glycosylation_01 foreign key (ix_ginas_glycosylation_n_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_nsite add constraint fk_ix_ginas_glycosylation_nsite_ix_ginas_site_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_osite add constraint fk_ix_ginas_glycosylation_osite_ix_ginas_glycosylation_01 foreign key (ix_ginas_glycosylation_o_uuid) references ix_ginas_glycosylation (uuid) on delete restrict on update restrict;

alter table ix_ginas_glycosylation_osite add constraint fk_ix_ginas_glycosylation_osite_ix_ginas_site_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_core_group_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_modifications_agent add constraint fk_ix_ginas_modifications_agent_ix_ginas_modifications_01 foreign key (ix_ginas_modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_agent add constraint fk_ix_ginas_modifications_agent_ix_ginas_agent_modification_02 foreign key (ix_ginas_agent_modification_uuid) references ix_ginas_agent_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_physical add constraint fk_ix_ginas_modifications_physical_ix_ginas_modifications_01 foreign key (ix_ginas_modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_physical add constraint fk_ix_ginas_modifications_physical_ix_ginas_physical_modifica_02 foreign key (ix_ginas_physical_modification_uuid) references ix_ginas_physical_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_structural add constraint fk_ix_ginas_modifications_structural_ix_ginas_modifications_01 foreign key (ix_ginas_modifications_uuid) references ix_ginas_modifications (uuid) on delete restrict on update restrict;

alter table ix_ginas_modifications_structural add constraint fk_ix_ginas_modifications_structural_ix_ginas_structural_modi_02 foreign key (ix_ginas_structural_modification_uuid) references ix_ginas_structural_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_access add constraint fk_ix_ginas_name_access_ix_ginas_name_01 foreign key (ix_ginas_name_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_access add constraint fk_ix_ginas_name_access_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_name_domain add constraint fk_ix_ginas_name_domain_ix_ginas_name_01 foreign key (ix_ginas_name_domain_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_domain add constraint fk_ix_ginas_name_domain_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_language add constraint fk_ix_ginas_name_language_ix_ginas_name_01 foreign key (ix_ginas_name_language_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_language add constraint fk_ix_ginas_name_language_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_jurisdiction add constraint fk_ix_ginas_name_jurisdiction_ix_ginas_name_01 foreign key (ix_ginas_name_jurisdiction_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_jurisdiction add constraint fk_ix_ginas_name_jurisdiction_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_reference add constraint fk_ix_ginas_name_reference_ix_ginas_name_01 foreign key (ix_ginas_name_reference_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_reference add constraint fk_ix_ginas_name_reference_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_name_nameorg add constraint fk_ix_ginas_name_nameorg_ix_ginas_name_01 foreign key (ix_ginas_name_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_name_nameorg add constraint fk_ix_ginas_name_nameorg_ix_ginas_nameorg_02 foreign key (ix_ginas_nameorg_uuid) references ix_ginas_nameorg (uuid) on delete restrict on update restrict;

alter table ix_ginas_note_access add constraint fk_ix_ginas_note_access_ix_ginas_note_01 foreign key (ix_ginas_note_uuid) references ix_ginas_note (uuid) on delete restrict on update restrict;

alter table ix_ginas_note_access add constraint fk_ix_ginas_note_access_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_note_reference add constraint fk_ix_ginas_note_reference_ix_ginas_note_01 foreign key (ix_ginas_note_uuid) references ix_ginas_note (uuid) on delete restrict on update restrict;

alter table ix_ginas_note_reference add constraint fk_ix_ginas_note_reference_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_otherlinks_site add constraint fk_ix_ginas_otherlinks_site_ix_ginas_otherlinks_01 foreign key (ix_ginas_otherlinks_uuid) references ix_ginas_otherlinks (uuid) on delete restrict on update restrict;

alter table ix_ginas_otherlinks_site add constraint fk_ix_ginas_otherlinks_site_ix_ginas_site_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_ix_core_payload_01 foreign key (ix_core_payload_id) references ix_core_payload (id) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_physical_modification_param add constraint fk_ix_ginas_physical_modification_param_ix_ginas_physical_mod_01 foreign key (ix_ginas_physical_modification_uuid) references ix_ginas_physical_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_physical_modification_param add constraint fk_ix_ginas_physical_modification_param_ix_ginas_physical_par_02 foreign key (ix_ginas_physical_parameter_uuid) references ix_ginas_physical_parameter (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_material add constraint fk_ix_ginas_polymer_material_ix_ginas_polymer_01 foreign key (ix_ginas_polymer_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_material add constraint fk_ix_ginas_polymer_material_ix_ginas_material_02 foreign key (ix_ginas_material_uuid) references ix_ginas_material (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_unit add constraint fk_ix_ginas_polymer_unit_ix_ginas_polymer_01 foreign key (ix_ginas_polymer_uuid) references ix_ginas_polymer (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymer_unit add constraint fk_ix_ginas_polymer_unit_ix_ginas_unit_02 foreign key (ix_ginas_unit_uuid) references ix_ginas_unit (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymerclass_sub add constraint fk_ix_ginas_polymerclass_sub_ix_ginas_polymerclass_01 foreign key (ix_ginas_polymerclass_uuid) references ix_ginas_polymerclass (uuid) on delete restrict on update restrict;

alter table ix_ginas_polymerclass_sub add constraint fk_ix_ginas_polymerclass_sub_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_ix_core_predicate_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_ix_core_predicate_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_property_parameter add constraint fk_ix_ginas_property_parameter_ix_ginas_property_01 foreign key (ix_ginas_property_uuid) references ix_ginas_property (uuid) on delete restrict on update restrict;

alter table ix_ginas_property_parameter add constraint fk_ix_ginas_property_parameter_ix_ginas_parameter_02 foreign key (ix_ginas_parameter_uuid) references ix_ginas_parameter (uuid) on delete restrict on update restrict;

alter table ix_ginas_property_reference add constraint fk_ix_ginas_property_reference_ix_ginas_property_01 foreign key (ix_ginas_property_uuid) references ix_ginas_property (uuid) on delete restrict on update restrict;

alter table ix_ginas_property_reference add constraint fk_ix_ginas_property_reference_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_protein_disulfide add constraint fk_ix_ginas_protein_disulfide_ix_ginas_protein_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_disulfide add constraint fk_ix_ginas_protein_disulfide_ix_ginas_disulfide_02 foreign key (ix_ginas_disulfide_uuid) references ix_ginas_disulfide (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_ix_ginas_protein_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_ix_ginas_subunit_02 foreign key (ix_ginas_subunit_uuid) references ix_ginas_subunit (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_otherlinks add constraint fk_ix_ginas_protein_otherlinks_ix_ginas_protein_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_otherlinks add constraint fk_ix_ginas_protein_otherlinks_ix_ginas_otherlinks_02 foreign key (ix_ginas_otherlinks_uuid) references ix_ginas_otherlinks (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_reference add constraint fk_ix_ginas_protein_reference_ix_ginas_protein_01 foreign key (ix_ginas_protein_uuid) references ix_ginas_protein (uuid) on delete restrict on update restrict;

alter table ix_ginas_protein_reference add constraint fk_ix_ginas_protein_reference_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keyword_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keyword_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_ix_core_pubauthor_02 foreign key (ix_core_pubauthor_id) references ix_core_pubauthor (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_ix_core_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_ginas_reference_access add constraint fk_ix_ginas_reference_access_ix_ginas_reference_01 foreign key (ix_ginas_reference_uuid) references ix_ginas_reference (uuid) on delete restrict on update restrict;

alter table ix_ginas_reference_access add constraint fk_ix_ginas_reference_access_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_reference_tag add constraint fk_ix_ginas_reference_tag_ix_ginas_reference_01 foreign key (ix_ginas_reference_uuid) references ix_ginas_reference (uuid) on delete restrict on update restrict;

alter table ix_ginas_reference_tag add constraint fk_ix_ginas_reference_tag_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_relationship_access add constraint fk_ix_ginas_relationship_access_ix_ginas_relationship_01 foreign key (ix_ginas_relationship_uuid) references ix_ginas_relationship (uuid) on delete restrict on update restrict;

alter table ix_ginas_relationship_access add constraint fk_ix_ginas_relationship_access_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_relationship_reference add constraint fk_ix_ginas_relationship_reference_ix_ginas_relationship_01 foreign key (ix_ginas_relationship_uuid) references ix_ginas_relationship (uuid) on delete restrict on update restrict;

alter table ix_ginas_relationship_reference add constraint fk_ix_ginas_relationship_reference_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_ix_core_stitch_01 foreign key (ix_core_stitch_id) references ix_core_stitch (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_ix_core_attribute_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_ginas_structural_modification_1 add constraint fk_ix_ginas_structural_modification_1_ix_ginas_structural_mod_01 foreign key (ix_ginas_structural_modification_uuid) references ix_ginas_structural_modification (uuid) on delete restrict on update restrict;

alter table ix_ginas_structural_modification_1 add constraint fk_ix_ginas_structural_modification_1_ix_ginas_site_02 foreign key (ix_ginas_site_uuid) references ix_ginas_site (uuid) on delete restrict on update restrict;

alter table ix_ginas_strucdiv_part add constraint fk_ix_ginas_strucdiv_part_ix_ginas_strucdiv_01 foreign key (ix_ginas_strucdiv_uuid) references ix_ginas_strucdiv (uuid) on delete restrict on update restrict;

alter table ix_ginas_strucdiv_part add constraint fk_ix_ginas_strucdiv_part_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_ix_core_structure_01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix_core_structure_01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_ginas_substance_access add constraint fk_ix_ginas_substance_access_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_access add constraint fk_ix_ginas_substance_access_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ginas_substance_name add constraint fk_ix_ginas_substance_name_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_name add constraint fk_ix_ginas_substance_name_ix_ginas_name_02 foreign key (ix_ginas_name_uuid) references ix_ginas_name (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_code add constraint fk_ix_ginas_substance_code_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_code add constraint fk_ix_ginas_substance_code_ix_ginas_code_02 foreign key (ix_ginas_code_uuid) references ix_ginas_code (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_note add constraint fk_ix_ginas_substance_note_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_note add constraint fk_ix_ginas_substance_note_ix_ginas_note_02 foreign key (ix_ginas_note_uuid) references ix_ginas_note (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_property add constraint fk_ix_ginas_substance_property_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_property add constraint fk_ix_ginas_substance_property_ix_ginas_property_02 foreign key (ix_ginas_property_uuid) references ix_ginas_property (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_relationship add constraint fk_ix_ginas_substance_relationship_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_relationship add constraint fk_ix_ginas_substance_relationship_ix_ginas_relationship_02 foreign key (ix_ginas_relationship_uuid) references ix_ginas_relationship (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_reference add constraint fk_ix_ginas_substance_reference_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_reference add constraint fk_ix_ginas_substance_reference_ix_ginas_reference_02 foreign key (ix_ginas_reference_uuid) references ix_ginas_reference (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_tag add constraint fk_ix_ginas_substance_tag_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_tag add constraint fk_ix_ginas_substance_tag_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_chemical_moiety add constraint fk_ix_ginas_chemical_moiety_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_chemical_moiety add constraint fk_ix_ginas_chemical_moiety_ix_ginas_moiety_02 foreign key (ix_ginas_moiety_uuid) references ix_ginas_moiety (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_component add constraint fk_ix_ginas_substance_component_ix_ginas_substance_01 foreign key (ix_ginas_substance_uuid) references ix_ginas_substance (uuid) on delete restrict on update restrict;

alter table ix_ginas_substance_component add constraint fk_ix_ginas_substance_component_ix_ginas_component_02 foreign key (ix_ginas_component_uuid) references ix_ginas_component (uuid) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_core_xref_01 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table ix_core_acl;

drop table ix_core_acl_principal;

drop table ix_core_acl_group;

drop table ix_ginas_agent_modification;

drop table ix_ginas_amount;

drop table ix_core_attribute;

drop table ix_ginas_code;

drop table ix_ginas_code_access;

drop table ix_ginas_code_reference;

drop table ix_ginas_component;

drop table ix_core_curation;

drop table ix_ginas_disulfide;

drop table ix_ginas_disulfide_site;

drop table ix_core_etag;

drop table ix_core_etagref;

drop table ix_core_edit;

drop table ix_core_event;

drop table ix_core_event_figure;

drop table ix_core_figure;

drop table ix_ginas_glycosylation;

drop table ix_ginas_glycosylation_csite;

drop table ix_ginas_glycosylation_nsite;

drop table ix_ginas_glycosylation_osite;

drop table ix_core_group;

drop table ix_core_group_principal;

drop table ix_core_investigator;

drop table ix_core_journal;

drop table ix_ginas_material;

drop table ix_ginas_modifications;

drop table ix_ginas_modifications_agent;

drop table ix_ginas_modifications_physical;

drop table ix_ginas_modifications_structural;

drop table ix_ginas_moiety;

drop table ix_ginas_name;

drop table ix_ginas_name_access;

drop table ix_ginas_name_domain;

drop table ix_ginas_name_language;

drop table ix_ginas_name_jurisdiction;

drop table ix_ginas_name_reference;

drop table ix_ginas_name_nameorg;

drop table ix_ginas_nameorg;

drop table ix_core_namespace;

drop table ix_ginas_note;

drop table ix_ginas_note_access;

drop table ix_ginas_note_reference;

drop table ix_core_organization;

drop table ix_ginas_otherlinks;

drop table ix_ginas_otherlinks_site;

drop table ix_ginas_parameter;

drop table ix_core_payload;

drop table ix_core_payload_property;

drop table ix_ginas_physical_modification;

drop table ix_ginas_physical_modification_param;

drop table ix_ginas_physical_parameter;

drop table ix_ginas_polymer;

drop table ix_ginas_polymer_material;

drop table ix_ginas_polymer_unit;

drop table ix_ginas_polymerclass;

drop table ix_ginas_polymerclass_sub;

drop table ix_core_predicate;

drop table ix_core_predicate_object;

drop table ix_core_predicate_property;

drop table ix_core_principal;

drop table ix_core_procjob;

drop table ix_core_procrecord;

drop table ix_ginas_property;

drop table ix_ginas_property_parameter;

drop table ix_ginas_property_reference;

drop table ix_ginas_protein;

drop table ix_ginas_protein_disulfide;

drop table ix_ginas_protein_subunit;

drop table ix_ginas_protein_otherlinks;

drop table ix_ginas_protein_reference;

drop table ix_core_pubauthor;

drop table ix_core_publication;

drop table ix_core_publication_keyword;

drop table ix_core_publication_mesh;

drop table ix_core_publication_author;

drop table ix_core_publication_figure;

drop table ix_ginas_reference;

drop table ix_ginas_reference_access;

drop table ix_ginas_reference_tag;

drop table ix_ginas_relationship;

drop table ix_ginas_relationship_access;

drop table ix_ginas_relationship_reference;

drop table ix_core_role;

drop table ix_ginas_site;

drop table ix_ginas_specifiedsubstancecomponent;

drop table ix_core_stitch;

drop table ix_core_stitch_attribute;

drop table ix_ginas_structural_modification;

drop table ix_ginas_structural_modification_1;

drop table ix_ginas_strucdiv;

drop table ix_ginas_strucdiv_part;

drop table ix_core_structure;

drop table ix_core_structure_property;

drop table ix_core_structure_link;

drop table ix_ginas_substance;

drop table ix_ginas_substance_access;

drop table ix_ginas_substance_name;

drop table ix_ginas_substance_code;

drop table ix_ginas_substance_note;

drop table ix_ginas_substance_property;

drop table ix_ginas_substance_relationship;

drop table ix_ginas_substance_reference;

drop table ix_ginas_substance_tag;

drop table ix_ginas_substancereference;

drop table ix_ginas_subunit;

drop table ix_ginas_unit;

drop table ix_core_value;

drop table ix_core_xref;

drop table ix_core_xref_property;

SET FOREIGN_KEY_CHECKS=1;

