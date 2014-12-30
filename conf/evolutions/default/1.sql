# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_ncats_clinical_arm (
  id                        bigint not null,
  label                     varchar(255),
  description               clob,
  type                      varchar(255),
  constraint pk_ix_ncats_clinical_arm primary key (id))
;

create table ix_core_attribute (
  id                        bigint not null,
  name                      varchar(255),
  value                     varchar(1024),
  namespace_id              bigint,
  constraint pk_ix_core_attribute primary key (id))
;

create table ix_ginas_citation (
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  uuid                      varchar(40) not null,
  citation                  varchar(255),
  doc_type                  varchar(255),
  public_domain             boolean,
  document_date             timestamp,
  constraint pk_ix_ginas_citation primary key (id))
;

create table ix_ncats_clinical_trial (
  id                        bigint not null,
  nct_id                    varchar(15),
  url                       varchar(1024),
  title                     clob,
  official_title            clob,
  summary                   clob,
  description               clob,
  sponsor                   varchar(1024),
  study_type                varchar(255),
  study_design              varchar(255),
  start_date                timestamp,
  completion_date           timestamp,
  first_received_date       timestamp,
  last_changed_date         timestamp,
  verification_date         timestamp,
  first_received_results_date timestamp,
  has_results               boolean,
  status                    varchar(255),
  phase                     varchar(255),
  eligibility_id            bigint,
  constraint uq_ix_ncats_clinical_trial_nct_i unique (nct_id),
  constraint pk_ix_ncats_clinical_trial primary key (id))
;

create table ix_ncats_clinical_cohort (
  id                        bigint not null,
  label                     varchar(255),
  description               clob,
  constraint pk_ix_ncats_clinical_cohort primary key (id))
;

create table ix_ncats_clinical_condition (
  id                        bigint not null,
  name                      varchar(1024),
  is_rare_disease           boolean,
  constraint pk_ix_ncats_clinical_condition primary key (id))
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
  created                   timestamp,
  modified                  timestamp,
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
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  refid                     bigint,
  kind                      varchar(255),
  path                      varchar(1024),
  comments                  clob,
  old_value                 clob,
  new_value                 clob,
  constraint pk_ix_core_edit primary key (id))
;

create table ix_ncats_clinical_eligibility (
  id                        bigint not null,
  gender                    varchar(32),
  min_age                   varchar(255),
  max_age                   varchar(255),
  healthy_volunteers        boolean,
  criteria                  clob,
  constraint pk_ix_ncats_clinical_eligibility primary key (id))
;

create table ix_idg_entity (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  name                      varchar(1024),
  description               clob,
  idg_family                varchar(128),
  idg_class                 varchar(10),
  constraint pk_ix_idg_entity primary key (id))
;

create table ix_core_event (
  id                        bigint not null,
  title                     varchar(1024),
  description               clob,
  url                       varchar(1024),
  start                     timestamp,
  end                       timestamp,
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
  size                      integer,
  sha1                      varchar(140),
  parent_id                 bigint,
  constraint pk_ix_core_figure primary key (id))
;

create table ix_ncats_funding (
  id                        bigint not null,
  grant_id                  bigint not null,
  ic                        varchar(255),
  amount                    integer,
  constraint pk_ix_ncats_funding primary key (id))
;

create table ix_core_gene (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_ix_core_gene primary key (id))
;

create table ix_ncats_grant (
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
  constraint uq_ix_ncats_grant_application_id unique (application_id),
  constraint pk_ix_ncats_grant primary key (id))
;

create table ix_core_group (
  id                        bigint not null,
  name                      varchar(255),
  constraint uq_ix_core_group_name unique (name),
  constraint pk_ix_core_group primary key (id))
;

create table ix_ncats_clinical_intervention (
  id                        bigint not null,
  name                      varchar(255),
  description               clob,
  type                      varchar(255),
  constraint pk_ix_ncats_clinical_interventio primary key (id))
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

create table ix_ginas_name (
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  uuid                      varchar(40) not null,
  name                      varchar(512) not null,
  type                      varchar(10),
  public_domain             boolean,
  preferred                 boolean,
  constraint pk_ix_ginas_name primary key (id))
;

create table ix_core_namespace (
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  name                      varchar(255),
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

create table ix_ncats_clinical_outcome (
  id                        bigint not null,
  type                      integer,
  measure                   varchar(255),
  timeframe                 varchar(255),
  description               clob,
  safety_issue              boolean,
  constraint ck_ix_ncats_clinical_outcome_type check (type in (0,1,2,3)),
  constraint pk_ix_ncats_clinical_outcome primary key (id))
;

create table ix_core_payload (
  id                        bigint not null,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime                      varchar(128),
  size                      bigint,
  created                   timestamp,
  constraint pk_ix_core_payload primary key (id))
;

create table ix_core_predicate (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  namespace_id              bigint,
  subject_id                bigint,
  predicate                 varchar(255) not null,
  constraint pk_ix_core_predicate primary key (id))
;

create table ix_core_principal (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  username                  varchar(255),
  email                     varchar(255),
  admin                     boolean,
  uri                       varchar(1024),
  pkey                      varchar(255),
  selfie_id                 bigint,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  suffix                    varchar(20),
  affiliation               clob,
  orcid                     varchar(255),
  url                       varchar(1024),
  ncats_employee            boolean,
  dn                        varchar(1024),
  uid                       bigint,
  phone                     varchar(15),
  biography                 clob,
  title                     varchar(255),
  research                  clob,
  is_lead                   boolean,
  dept                      integer,
  role                      integer,
  constraint ck_ix_core_principal_dept check (dept in (0,1,2,3)),
  constraint ck_ix_core_principal_role check (role in (0,1,2,3,4)),
  constraint uq_ix_core_principal_pkey unique (pkey),
  constraint pk_ix_core_principal primary key (id))
;

create table ix_core_processingstatus (
  id                        bigint not null,
  status                    integer,
  message                   varchar(4000),
  payload_id                bigint,
  constraint ck_ix_core_processingstatus_status check (status in (0,1,2)),
  constraint pk_ix_core_processingstatus primary key (id))
;

create table ix_ncats_program (
  id                        bigint not null,
  name                      varchar(64),
  fullname                  varchar(255),
  constraint pk_ix_ncats_program primary key (id))
;

create table ix_ncats_project (
  id                        bigint not null,
  title                     varchar(1024),
  objective                 clob,
  scope                     clob,
  opportunities             clob,
  team                      varchar(255),
  is_public                 boolean,
  curation_id               bigint,
  constraint pk_ix_ncats_project primary key (id))
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

create table ix_ginas_structure (
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  uuid                      varchar(40) not null,
  molfile                   clob,
  stereo_chemistry          integer,
  optical_activity          integer,
  atropisomerism            boolean,
  stereo_comments           clob,
  stereo_centers            integer,
  defined_stereo            integer,
  ez_centers                integer,
  charge                    integer,
  mwt                       double,
  count                     integer,
  constraint ck_ix_ginas_structure_stereo_chemistry check (stereo_chemistry in (0,1,2,3,4,5)),
  constraint ck_ix_ginas_structure_optical_activity check (optical_activity in (0,1,2,3,4)),
  constraint pk_ix_ginas_structure primary key (id))
;

create table ix_ginas_substance (
  dtype                     varchar(10) not null,
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  uuid                      varchar(40) not null,
  substance_class           integer,
  structure_id              bigint,
  protein_type              varchar(32),
  sequence_origin           varchar(128),
  sequence_type             varchar(32),
  constraint ck_ix_ginas_substance_substance_class check (substance_class in (0,1,2,3,4,5,6,7,8,9,10)),
  constraint pk_ix_ginas_substance primary key (id))
;

create table ix_ginas_subunit (
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  uuid                      varchar(40) not null,
  sequence                  clob,
  index                     integer,
  constraint pk_ix_ginas_subunit primary key (id))
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
  data                      blob,
  size                      integer,
  sha1                      varchar(40),
  mime_type                 varchar(32),
  bits                      integer,
  depth                     integer,
  term                      varchar(255),
  url                       clob,
  strval                    varchar(1024),
  intval                    bigint,
  major_topic               boolean,
  heading                   varchar(1024),
  constraint pk_ix_core_value primary key (id))
;

create table ix_core_xref (
  id                        bigint not null,
  created                   timestamp,
  modified                  timestamp,
  namespace_id              bigint,
  refid                     bigint not null,
  kind                      varchar(512) not null,
  deprecated                boolean,
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

create table ix_ginas_citation_tag (
  ix_ginas_citation_id           bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_citation_tag primary key (ix_ginas_citation_id, ix_core_value_id))
;

create table ix_ncats_clinical_trial_keyword (
  ix_ncats_clinical_trial_id     bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ncats_clinical_trial_keyword primary key (ix_ncats_clinical_trial_id, ix_core_value_id))
;

create table ix_ncats_clinical_trial_sponsor (
  ix_ncats_clinical_trial_id     bigint not null,
  ix_core_organization_id        bigint not null,
  constraint pk_ix_ncats_clinical_trial_sponsor primary key (ix_ncats_clinical_trial_id, ix_core_organization_id))
;

create table ix_ncats_clinical_trial_intervention (
  ix_ncats_clinical_trial_id     bigint not null,
  ix_ncats_clinical_intervention_id bigint not null,
  constraint pk_ix_ncats_clinical_trial_intervention primary key (ix_ncats_clinical_trial_id, ix_ncats_clinical_intervention_id))
;

create table ix_ncats_clinical_trial_condition (
  ix_ncats_clinical_trial_id     bigint not null,
  ix_ncats_clinical_condition_id bigint not null,
  constraint pk_ix_ncats_clinical_trial_condition primary key (ix_ncats_clinical_trial_id, ix_ncats_clinical_condition_id))
;

create table ix_ncats_clinical_trial_outcome (
  ix_ncats_clinical_trial_id     bigint not null,
  ix_ncats_clinical_outcome_id   bigint not null,
  constraint pk_ix_ncats_clinical_trial_outcome primary key (ix_ncats_clinical_trial_id, ix_ncats_clinical_outcome_id))
;

create table ix_ncats_clincial_trial_location (
  ix_ncats_clinical_trial_id     bigint not null,
  ix_core_organization_id        bigint not null,
  constraint pk_ix_ncats_clincial_trial_location primary key (ix_ncats_clinical_trial_id, ix_core_organization_id))
;

create table ix_ncats_clincial_trial_publication (
  ix_ncats_clinical_trial_id     bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_ncats_clincial_trial_publication primary key (ix_ncats_clinical_trial_id, ix_core_publication_id))
;

create table _ix_ncats_cca46885_1 (
  ix_ncats_clinical_condition_synonym_id bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk__ix_ncats_cca46885_1 primary key (ix_ncats_clinical_condition_synonym_id, ix_core_value_id))
;

create table _ix_ncats_cca46885_2 (
  ix_ncats_clinical_condition_keyword_id bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk__ix_ncats_cca46885_2 primary key (ix_ncats_clinical_condition_keyword_id, ix_core_value_id))
;

create table _ix_ncats_cca46885_3 (
  ix_ncats_clinical_condition_wikipedia_id bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk__ix_ncats_cca46885_3 primary key (ix_ncats_clinical_condition_wikipedia_id, ix_core_value_id))
;

create table ix_core_edit_curation (
  ix_core_edit_id                bigint not null,
  ix_core_curation_id            bigint not null,
  constraint pk_ix_core_edit_curation primary key (ix_core_edit_id, ix_core_curation_id))
;

create table _ix_ncats_840372f9_1 (
  ix_ncats_clinical_eligibility_inclusion_id bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk__ix_ncats_840372f9_1 primary key (ix_ncats_clinical_eligibility_inclusion_id, ix_core_value_id))
;

create table _ix_ncats_840372f9_2 (
  ix_ncats_clinical_eligibility_exclusion_id bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk__ix_ncats_840372f9_2 primary key (ix_ncats_clinical_eligibility_exclusion_id, ix_core_value_id))
;

create table ix_idg_entity_synonym (
  ix_idg_entity_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_entity_synonym primary key (ix_idg_entity_id, ix_core_value_id))
;

create table ix_idg_entity_property (
  ix_idg_entity_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_entity_property primary key (ix_idg_entity_id, ix_core_value_id))
;

create table ix_idg_entity_link (
  ix_idg_entity_id               bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_idg_entity_link primary key (ix_idg_entity_id, ix_core_xref_id))
;

create table ix_idg_entity_publication (
  ix_idg_entity_id               bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_idg_entity_publication primary key (ix_idg_entity_id, ix_core_publication_id))
;

create table ix_core_event_figure (
  ix_core_event_id               bigint not null,
  ix_core_figure_id              bigint not null,
  constraint pk_ix_core_event_figure primary key (ix_core_event_id, ix_core_figure_id))
;

create table ix_core_gene_synonym (
  ix_core_gene_id                bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_gene_synonym primary key (ix_core_gene_id, ix_core_value_id))
;

create table ix_ncats_grant_investigator (
  ix_ncats_grant_id              bigint not null,
  ix_core_investigator_id        bigint not null,
  constraint pk_ix_ncats_grant_investigator primary key (ix_ncats_grant_id, ix_core_investigator_id))
;

create table ix_ncats_grant_keyword (
  ix_ncats_grant_id              bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ncats_grant_keyword primary key (ix_ncats_grant_id, ix_core_value_id))
;

create table ix_ncats_grant_publication (
  ix_ncats_grant_id              bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_ncats_grant_publication primary key (ix_ncats_grant_id, ix_core_publication_id))
;

create table ix_core_group_principal (
  ix_core_group_id               bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_core_group_principal primary key (ix_core_group_id, ix_core_principal_id))
;

create table _ix_ncats_4a162ae3_1 (
  ix_ncats_clinical_intervention_id bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk__ix_ncats_4a162ae3_1 primary key (ix_ncats_clinical_intervention_id, ix_core_value_id))
;

create table _ix_ncats_4a162ae3_2 (
  ix_ncats_clinical_intervention_id bigint not null,
  ix_ncats_clinical_arm_id       bigint not null,
  constraint pk__ix_ncats_4a162ae3_2 primary key (ix_ncats_clinical_intervention_id, ix_ncats_clinical_arm_id))
;

create table _ix_ncats_4a162ae3_3 (
  ix_ncats_clinical_intervention_id bigint not null,
  ix_ncats_clinical_cohort_id    bigint not null,
  constraint pk__ix_ncats_4a162ae3_3 primary key (ix_ncats_clinical_intervention_id, ix_ncats_clinical_cohort_id))
;

create table ix_ginas_name_domain (
  ix_ginas_name_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_name_domain primary key (ix_ginas_name_id, ix_core_value_id))
;

create table ix_core_payload_attribute (
  ix_core_payload_id             bigint not null,
  ix_core_attribute_id           bigint not null,
  constraint pk_ix_core_payload_attribute primary key (ix_core_payload_id, ix_core_attribute_id))
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

create table ix_ncats_project_program (
  ix_ncats_project_id            bigint not null,
  ix_ncats_program_id            bigint not null,
  constraint pk_ix_ncats_project_program primary key (ix_ncats_project_id, ix_ncats_program_id))
;

create table ix_ncats_project_keyword (
  ix_ncats_project_id            bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ncats_project_keyword primary key (ix_ncats_project_id, ix_core_value_id))
;

create table ix_ncats_project_member (
  ix_ncats_project_id            bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ncats_project_member primary key (ix_ncats_project_id, ix_core_principal_id))
;

create table ix_ncats_project_collaborator (
  ix_ncats_project_id            bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_ncats_project_collaborator primary key (ix_ncats_project_id, ix_core_principal_id))
;

create table ix_ncats_project_figure (
  ix_ncats_project_id            bigint not null,
  ix_core_figure_id              bigint not null,
  constraint pk_ix_ncats_project_figure primary key (ix_ncats_project_id, ix_core_figure_id))
;

create table ix_ncats_project_milestone (
  ix_ncats_project_id            bigint not null,
  ix_core_event_id               bigint not null,
  constraint pk_ix_ncats_project_milestone primary key (ix_ncats_project_id, ix_core_event_id))
;

create table ix_ncats_project_publication (
  ix_ncats_project_id            bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_ncats_project_publication primary key (ix_ncats_project_id, ix_core_publication_id))
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

create table ix_ginas_structure_hash (
  ix_ginas_structure_id          bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_structure_hash primary key (ix_ginas_structure_id, ix_core_value_id))
;

create table ix_ginas_structure_fingerprint (
  ix_ginas_structure_id          bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_structure_fingerprint primary key (ix_ginas_structure_id, ix_core_value_id))
;

create table ix_ginas_structure_citation (
  ix_ginas_structure_id          bigint not null,
  ix_ginas_citation_id           bigint not null,
  constraint pk_ix_ginas_structure_citation primary key (ix_ginas_structure_id, ix_ginas_citation_id))
;

create table ix_ginas_substance_name (
  ix_ginas_substance_id          bigint not null,
  ix_ginas_name_id               bigint not null,
  constraint pk_ix_ginas_substance_name primary key (ix_ginas_substance_id, ix_ginas_name_id))
;

create table ix_ginas_substance_code (
  ix_ginas_substance_id          bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_substance_code primary key (ix_ginas_substance_id, ix_core_value_id))
;

create table ix_ginas_substance_property (
  ix_ginas_substance_id          bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_ginas_substance_property primary key (ix_ginas_substance_id, ix_core_value_id))
;

create table ix_ginas_substance_citation (
  ix_ginas_substance_id          bigint not null,
  ix_ginas_citation_id           bigint not null,
  constraint pk_ix_ginas_substance_citation primary key (ix_ginas_substance_id, ix_ginas_citation_id))
;

create table ix_ginas_substance_moiety (
  ix_ginas_substance_id          bigint not null,
  ix_ginas_structure_id          bigint not null,
  constraint pk_ix_ginas_substance_moiety primary key (ix_ginas_substance_id, ix_ginas_structure_id))
;

create table ix_ginas_protein_subunit (
  ix_ginas_substance_id          bigint not null,
  ix_ginas_subunit_id            bigint not null,
  constraint pk_ix_ginas_protein_subunit primary key (ix_ginas_substance_id, ix_ginas_subunit_id))
;

create table ix_core_xref_property (
  ix_core_xref_id                bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_xref_property primary key (ix_core_xref_id, ix_core_value_id))
;
create sequence ix_core_acl_seq;

create sequence ix_ncats_clinical_arm_seq;

create sequence ix_core_attribute_seq;

create sequence ix_ginas_citation_seq;

create sequence ix_ncats_clinical_trial_seq;

create sequence ix_ncats_clinical_cohort_seq;

create sequence ix_ncats_clinical_condition_seq;

create sequence ix_core_curation_seq;

create sequence ix_core_etag_seq;

create sequence ix_core_etagref_seq;

create sequence ix_core_edit_seq;

create sequence ix_ncats_clinical_eligibility_seq;

create sequence ix_idg_entity_seq;

create sequence ix_core_event_seq;

create sequence ix_core_figure_seq;

create sequence ix_ncats_funding_seq;

create sequence ix_core_gene_seq;

create sequence ix_ncats_grant_seq;

create sequence ix_core_group_seq;

create sequence ix_ncats_clinical_intervention_seq;

create sequence ix_core_investigator_seq;

create sequence ix_core_journal_seq;

create sequence ix_ginas_name_seq;

create sequence ix_core_namespace_seq;

create sequence ix_core_organization_seq;

create sequence ix_ncats_clinical_outcome_seq;

create sequence ix_core_payload_seq;

create sequence ix_core_predicate_seq;

create sequence ix_core_principal_seq;

create sequence ix_core_processingstatus_seq;

create sequence ix_ncats_program_seq;

create sequence ix_ncats_project_seq;

create sequence ix_core_pubauthor_seq;

create sequence ix_core_publication_seq;

create sequence ix_core_role_seq;

create sequence ix_core_stitch_seq;

create sequence ix_ginas_structure_seq;

create sequence ix_ginas_substance_seq;

create sequence ix_ginas_subunit_seq;

create sequence ix_core_value_seq;

create sequence ix_core_xref_seq;

alter table ix_core_attribute add constraint fk_ix_core_attribute_namespace_1 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_namespace_1 on ix_core_attribute (namespace_id);
alter table ix_ncats_clinical_trial add constraint fk_ix_ncats_clinical_trial_eli_2 foreign key (eligibility_id) references ix_ncats_clinical_eligibility (id) on delete restrict on update restrict;
create index ix_ix_ncats_clinical_trial_eli_2 on ix_ncats_clinical_trial (eligibility_id);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_3 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_3 on ix_core_curation (curator_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_4 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_4 on ix_core_etagref (etag_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_5 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_5 on ix_core_figure (parent_id);
alter table ix_ncats_funding add constraint fk_ix_ncats_funding_ix_ncats_g_6 foreign key (grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;
create index ix_ix_ncats_funding_ix_ncats_g_6 on ix_ncats_funding (grant_id);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organi_7 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organi_7 on ix_core_investigator (organization_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_namespace_8 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_namespace_8 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_subject_9 foreign key (subject_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_subject_9 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_10 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_10 on ix_core_principal (selfie_id);
alter table ix_core_processingstatus add constraint fk_ix_core_processingstatus_p_11 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_processingstatus_p_11 on ix_core_processingstatus (payload_id);
alter table ix_ncats_project add constraint fk_ix_ncats_project_curation_12 foreign key (curation_id) references ix_core_curation (id) on delete restrict on update restrict;
create index ix_ix_ncats_project_curation_12 on ix_ncats_project (curation_id);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_13 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_13 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journa_14 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journa_14 on ix_core_publication (journal_id);
alter table ix_core_role add constraint fk_ix_core_role_principal_15 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_role_principal_15 on ix_core_role (principal_id);
alter table ix_ginas_substance add constraint fk_ix_ginas_substance_structu_16 foreign key (structure_id) references ix_ginas_structure (id) on delete restrict on update restrict;
create index ix_ix_ginas_substance_structu_16 on ix_ginas_substance (structure_id);
alter table ix_core_xref add constraint fk_ix_core_xref_namespace_17 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_xref_namespace_17 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_c_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core__02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_ginas_citation_tag add constraint fk_ix_ginas_citation_tag_ix_g_01 foreign key (ix_ginas_citation_id) references ix_ginas_citation (id) on delete restrict on update restrict;

alter table ix_ginas_citation_tag add constraint fk_ix_ginas_citation_tag_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_keyword add constraint fk_ix_ncats_clinical_trial_ke_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_keyword add constraint fk_ix_ncats_clinical_trial_ke_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_sponsor add constraint fk_ix_ncats_clinical_trial_sp_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_sponsor add constraint fk_ix_ncats_clinical_trial_sp_02 foreign key (ix_core_organization_id) references ix_core_organization (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_intervention add constraint fk_ix_ncats_clinical_trial_in_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_intervention add constraint fk_ix_ncats_clinical_trial_in_02 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_condition add constraint fk_ix_ncats_clinical_trial_co_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_condition add constraint fk_ix_ncats_clinical_trial_co_02 foreign key (ix_ncats_clinical_condition_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_outcome add constraint fk_ix_ncats_clinical_trial_ou_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_outcome add constraint fk_ix_ncats_clinical_trial_ou_02 foreign key (ix_ncats_clinical_outcome_id) references ix_ncats_clinical_outcome (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_location add constraint fk_ix_ncats_clincial_trial_lo_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_location add constraint fk_ix_ncats_clincial_trial_lo_02 foreign key (ix_core_organization_id) references ix_core_organization (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_publication add constraint fk_ix_ncats_clincial_trial_pu_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_publication add constraint fk_ix_ncats_clincial_trial_pu_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_1 add constraint fk__ix_ncats_cca46885_1_ix_nc_01 foreign key (ix_ncats_clinical_condition_synonym_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_1 add constraint fk__ix_ncats_cca46885_1_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_2 add constraint fk__ix_ncats_cca46885_2_ix_nc_01 foreign key (ix_ncats_clinical_condition_keyword_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_2 add constraint fk__ix_ncats_cca46885_2_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_3 add constraint fk__ix_ncats_cca46885_3_ix_nc_01 foreign key (ix_ncats_clinical_condition_wikipedia_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_3 add constraint fk__ix_ncats_cca46885_3_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_edit_curation add constraint fk_ix_core_edit_curation_ix_c_01 foreign key (ix_core_edit_id) references ix_core_edit (id) on delete restrict on update restrict;

alter table ix_core_edit_curation add constraint fk_ix_core_edit_curation_ix_c_02 foreign key (ix_core_curation_id) references ix_core_curation (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_1 add constraint fk__ix_ncats_840372f9_1_ix_nc_01 foreign key (ix_ncats_clinical_eligibility_inclusion_id) references ix_ncats_clinical_eligibility (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_1 add constraint fk__ix_ncats_840372f9_1_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_2 add constraint fk__ix_ncats_840372f9_2_ix_nc_01 foreign key (ix_ncats_clinical_eligibility_exclusion_id) references ix_ncats_clinical_eligibility (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_2 add constraint fk__ix_ncats_840372f9_2_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_entity_synonym add constraint fk_ix_idg_entity_synonym_ix_i_01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_synonym add constraint fk_ix_idg_entity_synonym_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_entity_property add constraint fk_ix_idg_entity_property_ix__01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_property add constraint fk_ix_idg_entity_property_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_entity_link add constraint fk_ix_idg_entity_link_ix_idg__01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_link add constraint fk_ix_idg_entity_link_ix_core_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_idg_entity_publication add constraint fk_ix_idg_entity_publication__01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_publication add constraint fk_ix_idg_entity_publication__02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_co_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_core_gene_synonym add constraint fk_ix_core_gene_synonym_ix_co_01 foreign key (ix_core_gene_id) references ix_core_gene (id) on delete restrict on update restrict;

alter table ix_core_gene_synonym add constraint fk_ix_core_gene_synonym_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_grant_investigator add constraint fk_ix_ncats_grant_investigato_01 foreign key (ix_ncats_grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;

alter table ix_ncats_grant_investigator add constraint fk_ix_ncats_grant_investigato_02 foreign key (ix_core_investigator_id) references ix_core_investigator (id) on delete restrict on update restrict;

alter table ix_ncats_grant_keyword add constraint fk_ix_ncats_grant_keyword_ix__01 foreign key (ix_ncats_grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;

alter table ix_ncats_grant_keyword add constraint fk_ix_ncats_grant_keyword_ix__02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_grant_publication add constraint fk_ix_ncats_grant_publication_01 foreign key (ix_ncats_grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;

alter table ix_ncats_grant_publication add constraint fk_ix_ncats_grant_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_1 add constraint fk__ix_ncats_4a162ae3_1_ix_nc_01 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_1 add constraint fk__ix_ncats_4a162ae3_1_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_2 add constraint fk__ix_ncats_4a162ae3_2_ix_nc_01 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_2 add constraint fk__ix_ncats_4a162ae3_2_ix_nc_02 foreign key (ix_ncats_clinical_arm_id) references ix_ncats_clinical_arm (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_3 add constraint fk__ix_ncats_4a162ae3_3_ix_nc_01 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_3 add constraint fk__ix_ncats_4a162ae3_3_ix_nc_02 foreign key (ix_ncats_clinical_cohort_id) references ix_ncats_clinical_cohort (id) on delete restrict on update restrict;

alter table ix_ginas_name_domain add constraint fk_ix_ginas_name_domain_ix_gi_01 foreign key (ix_ginas_name_id) references ix_ginas_name (id) on delete restrict on update restrict;

alter table ix_ginas_name_domain add constraint fk_ix_ginas_name_domain_ix_co_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_payload_attribute add constraint fk_ix_core_payload_attribute__01 foreign key (ix_core_payload_id) references ix_core_payload (id) on delete restrict on update restrict;

alter table ix_core_payload_attribute add constraint fk_ix_core_payload_attribute__02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_i_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_project_program add constraint fk_ix_ncats_project_program_i_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_program add constraint fk_ix_ncats_project_program_i_02 foreign key (ix_ncats_program_id) references ix_ncats_program (id) on delete restrict on update restrict;

alter table ix_ncats_project_keyword add constraint fk_ix_ncats_project_keyword_i_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_keyword add constraint fk_ix_ncats_project_keyword_i_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_project_member add constraint fk_ix_ncats_project_member_ix_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_member add constraint fk_ix_ncats_project_member_ix_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ncats_project_collaborator add constraint fk_ix_ncats_project_collabora_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_collaborator add constraint fk_ix_ncats_project_collabora_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ncats_project_figure add constraint fk_ix_ncats_project_figure_ix_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_figure add constraint fk_ix_ncats_project_figure_ix_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_ncats_project_milestone add constraint fk_ix_ncats_project_milestone_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_milestone add constraint fk_ix_ncats_project_milestone_02 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_ncats_project_publication add constraint fk_ix_ncats_project_publicati_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_publication add constraint fk_ix_ncats_project_publicati_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

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

alter table ix_ginas_structure_hash add constraint fk_ix_ginas_structure_hash_ix_01 foreign key (ix_ginas_structure_id) references ix_ginas_structure (id) on delete restrict on update restrict;

alter table ix_ginas_structure_hash add constraint fk_ix_ginas_structure_hash_ix_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_structure_fingerprint add constraint fk_ix_ginas_structure_fingerp_01 foreign key (ix_ginas_structure_id) references ix_ginas_structure (id) on delete restrict on update restrict;

alter table ix_ginas_structure_fingerprint add constraint fk_ix_ginas_structure_fingerp_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_structure_citation add constraint fk_ix_ginas_structure_citatio_01 foreign key (ix_ginas_structure_id) references ix_ginas_structure (id) on delete restrict on update restrict;

alter table ix_ginas_structure_citation add constraint fk_ix_ginas_structure_citatio_02 foreign key (ix_ginas_citation_id) references ix_ginas_citation (id) on delete restrict on update restrict;

alter table ix_ginas_substance_name add constraint fk_ix_ginas_substance_name_ix_01 foreign key (ix_ginas_substance_id) references ix_ginas_substance (id) on delete restrict on update restrict;

alter table ix_ginas_substance_name add constraint fk_ix_ginas_substance_name_ix_02 foreign key (ix_ginas_name_id) references ix_ginas_name (id) on delete restrict on update restrict;

alter table ix_ginas_substance_code add constraint fk_ix_ginas_substance_code_ix_01 foreign key (ix_ginas_substance_id) references ix_ginas_substance (id) on delete restrict on update restrict;

alter table ix_ginas_substance_code add constraint fk_ix_ginas_substance_code_ix_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_substance_property add constraint fk_ix_ginas_substance_propert_01 foreign key (ix_ginas_substance_id) references ix_ginas_substance (id) on delete restrict on update restrict;

alter table ix_ginas_substance_property add constraint fk_ix_ginas_substance_propert_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ginas_substance_citation add constraint fk_ix_ginas_substance_citatio_01 foreign key (ix_ginas_substance_id) references ix_ginas_substance (id) on delete restrict on update restrict;

alter table ix_ginas_substance_citation add constraint fk_ix_ginas_substance_citatio_02 foreign key (ix_ginas_citation_id) references ix_ginas_citation (id) on delete restrict on update restrict;

alter table ix_ginas_substance_moiety add constraint fk_ix_ginas_substance_moiety__01 foreign key (ix_ginas_substance_id) references ix_ginas_substance (id) on delete restrict on update restrict;

alter table ix_ginas_substance_moiety add constraint fk_ix_ginas_substance_moiety__02 foreign key (ix_ginas_structure_id) references ix_ginas_structure (id) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_i_01 foreign key (ix_ginas_substance_id) references ix_ginas_substance (id) on delete restrict on update restrict;

alter table ix_ginas_protein_subunit add constraint fk_ix_ginas_protein_subunit_i_02 foreign key (ix_ginas_subunit_id) references ix_ginas_subunit (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_01 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_c_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists ix_core_acl;

drop table if exists ix_core_acl_principal;

drop table if exists ix_core_acl_group;

drop table if exists ix_ncats_clinical_arm;

drop table if exists ix_core_attribute;

drop table if exists ix_ginas_citation;

drop table if exists ix_ginas_citation_tag;

drop table if exists ix_ncats_clinical_trial;

drop table if exists ix_ncats_clinical_trial_keyword;

drop table if exists ix_ncats_clinical_trial_sponsor;

drop table if exists ix_ncats_clinical_trial_intervention;

drop table if exists ix_ncats_clinical_trial_condition;

drop table if exists ix_ncats_clinical_trial_outcome;

drop table if exists ix_ncats_clincial_trial_location;

drop table if exists ix_ncats_clincial_trial_publication;

drop table if exists ix_ncats_clinical_cohort;

drop table if exists ix_ncats_clinical_condition;

drop table if exists _ix_ncats_cca46885_1;

drop table if exists _ix_ncats_cca46885_2;

drop table if exists _ix_ncats_cca46885_3;

drop table if exists ix_core_curation;

drop table if exists ix_core_etag;

drop table if exists ix_core_etagref;

drop table if exists ix_core_edit;

drop table if exists ix_core_edit_curation;

drop table if exists ix_ncats_clinical_eligibility;

drop table if exists _ix_ncats_840372f9_1;

drop table if exists _ix_ncats_840372f9_2;

drop table if exists ix_idg_entity;

drop table if exists ix_idg_entity_synonym;

drop table if exists ix_idg_entity_property;

drop table if exists ix_idg_entity_link;

drop table if exists ix_idg_entity_publication;

drop table if exists ix_core_event;

drop table if exists ix_core_event_figure;

drop table if exists ix_core_figure;

drop table if exists ix_ncats_funding;

drop table if exists ix_core_gene;

drop table if exists ix_core_gene_synonym;

drop table if exists ix_ncats_grant;

drop table if exists ix_ncats_grant_investigator;

drop table if exists ix_ncats_grant_keyword;

drop table if exists ix_ncats_grant_publication;

drop table if exists ix_core_group;

drop table if exists ix_core_group_principal;

drop table if exists ix_ncats_clinical_intervention;

drop table if exists _ix_ncats_4a162ae3_1;

drop table if exists _ix_ncats_4a162ae3_2;

drop table if exists _ix_ncats_4a162ae3_3;

drop table if exists ix_core_investigator;

drop table if exists ix_core_journal;

drop table if exists ix_ginas_name;

drop table if exists ix_ginas_name_domain;

drop table if exists ix_core_namespace;

drop table if exists ix_core_organization;

drop table if exists ix_ncats_clinical_outcome;

drop table if exists ix_core_payload;

drop table if exists ix_core_payload_attribute;

drop table if exists ix_core_predicate;

drop table if exists ix_core_predicate_object;

drop table if exists ix_core_predicate_property;

drop table if exists ix_core_principal;

drop table if exists ix_core_processingstatus;

drop table if exists ix_ncats_program;

drop table if exists ix_ncats_project;

drop table if exists ix_ncats_project_program;

drop table if exists ix_ncats_project_keyword;

drop table if exists ix_ncats_project_member;

drop table if exists ix_ncats_project_collaborator;

drop table if exists ix_ncats_project_figure;

drop table if exists ix_ncats_project_milestone;

drop table if exists ix_ncats_project_publication;

drop table if exists ix_core_pubauthor;

drop table if exists ix_core_publication;

drop table if exists ix_core_publication_keyword;

drop table if exists ix_core_publication_mesh;

drop table if exists ix_core_publication_author;

drop table if exists ix_core_publication_figure;

drop table if exists ix_core_role;

drop table if exists ix_core_stitch;

drop table if exists ix_core_stitch_attribute;

drop table if exists ix_ginas_structure;

drop table if exists ix_ginas_structure_hash;

drop table if exists ix_ginas_structure_fingerprint;

drop table if exists ix_ginas_structure_citation;

drop table if exists ix_ginas_substance;

drop table if exists ix_ginas_substance_name;

drop table if exists ix_ginas_substance_code;

drop table if exists ix_ginas_substance_property;

drop table if exists ix_ginas_substance_citation;

drop table if exists ix_ginas_subunit;

drop table if exists ix_core_value;

drop table if exists ix_core_xref;

drop table if exists ix_core_xref_property;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists ix_core_acl_seq;

drop sequence if exists ix_ncats_clinical_arm_seq;

drop sequence if exists ix_core_attribute_seq;

drop sequence if exists ix_ginas_citation_seq;

drop sequence if exists ix_ncats_clinical_trial_seq;

drop sequence if exists ix_ncats_clinical_cohort_seq;

drop sequence if exists ix_ncats_clinical_condition_seq;

drop sequence if exists ix_core_curation_seq;

drop sequence if exists ix_core_etag_seq;

drop sequence if exists ix_core_etagref_seq;

drop sequence if exists ix_core_edit_seq;

drop sequence if exists ix_ncats_clinical_eligibility_seq;

drop sequence if exists ix_idg_entity_seq;

drop sequence if exists ix_core_event_seq;

drop sequence if exists ix_core_figure_seq;

drop sequence if exists ix_ncats_funding_seq;

drop sequence if exists ix_core_gene_seq;

drop sequence if exists ix_ncats_grant_seq;

drop sequence if exists ix_core_group_seq;

drop sequence if exists ix_ncats_clinical_intervention_seq;

drop sequence if exists ix_core_investigator_seq;

drop sequence if exists ix_core_journal_seq;

drop sequence if exists ix_ginas_name_seq;

drop sequence if exists ix_core_namespace_seq;

drop sequence if exists ix_core_organization_seq;

drop sequence if exists ix_ncats_clinical_outcome_seq;

drop sequence if exists ix_core_payload_seq;

drop sequence if exists ix_core_predicate_seq;

drop sequence if exists ix_core_principal_seq;

drop sequence if exists ix_core_processingstatus_seq;

drop sequence if exists ix_ncats_program_seq;

drop sequence if exists ix_ncats_project_seq;

drop sequence if exists ix_core_pubauthor_seq;

drop sequence if exists ix_core_publication_seq;

drop sequence if exists ix_core_role_seq;

drop sequence if exists ix_core_stitch_seq;

drop sequence if exists ix_ginas_structure_seq;

drop sequence if exists ix_ginas_substance_seq;

drop sequence if exists ix_ginas_subunit_seq;

drop sequence if exists ix_core_value_seq;

drop sequence if exists ix_core_xref_seq;

