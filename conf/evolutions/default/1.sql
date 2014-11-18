# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint auto_increment not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_ncats_clinical_arm (
  id                        bigint auto_increment not null,
  label                     varchar(255),
  description               longtext,
  type                      varchar(255),
  constraint pk_ix_ncats_clinical_arm primary key (id))
;

create table ix_core_attribute (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  value                     varchar(1024),
  resource_id               bigint,
  constraint pk_ix_core_attribute primary key (id))
;

create table ix_ncats_clinical_trial (
  id                        bigint auto_increment not null,
  nct_id                    varchar(15),
  url                       varchar(255),
  title                     varchar(1024),
  official_title            longtext,
  summary                   longtext,
  description               longtext,
  sponsor                   varchar(1024),
  study_type                varchar(255),
  study_design              varchar(255),
  start_date                datetime,
  completion_date           datetime,
  first_received_date       datetime,
  last_changed_date         datetime,
  verification_date         datetime,
  first_received_results_date datetime,
  has_results               tinyint(1) default 0,
  status                    varchar(255),
  phase                     varchar(255),
  eligibility_id            bigint,
  constraint uq_ix_ncats_clinical_trial_nct_id unique (nct_id),
  constraint pk_ix_ncats_clinical_trial primary key (id))
;

create table ix_ncats_clinical_cohort (
  id                        bigint auto_increment not null,
  label                     varchar(255),
  description               longtext,
  constraint pk_ix_ncats_clinical_cohort primary key (id))
;

create table ix_ncats_clinical_condition (
  id                        bigint auto_increment not null,
  name                      varchar(1024),
  is_rare_disease           tinyint(1) default 0,
  constraint pk_ix_ncats_clinical_condition primary key (id))
;

create table ix_core_curation (
  id                        bigint auto_increment not null,
  curator_id                bigint,
  status                    integer,
  timestamp                 datetime,
  constraint ck_ix_core_curation_status check (status in (0,1,2)),
  constraint pk_ix_core_curation primary key (id))
;

create table ix_core_etag (
  id                        bigint auto_increment not null,
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
  timestamp                 datetime,
  modified                  datetime,
  query                     varchar(2048),
  filter                    varchar(4000),
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
  id                        bigint auto_increment not null,
  type                      varchar(255),
  refid                     bigint,
  timestamp                 datetime,
  principal_id              bigint,
  path                      varchar(1024),
  comments                  longtext,
  old_value                 longtext,
  new_value                 longtext,
  constraint pk_ix_core_edit primary key (id))
;

create table ix_ncats_clinical_eligibility (
  id                        bigint auto_increment not null,
  gender                    varchar(32),
  min_age                   varchar(255),
  max_age                   varchar(255),
  healthy_volunteers        tinyint(1) default 0,
  criteria                  longtext,
  constraint pk_ix_ncats_clinical_eligibility primary key (id))
;

create table ix_idg_entity (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  name                      varchar(1024),
  description               longtext,
  family                    varchar(128),
  idg_class                 varchar(10),
  constraint pk_ix_idg_entity primary key (id))
;

create table ix_core_event (
  id                        bigint auto_increment not null,
  title                     varchar(1024),
  description               longtext,
  url                       varchar(1024),
  start                     datetime,
  end                       datetime,
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
  size                      integer,
  sha1                      varchar(140),
  parent_id                 bigint,
  constraint pk_ix_core_figure primary key (id))
;

create table ix_ncats_funding (
  id                        bigint auto_increment not null,
  grant_id                  bigint not null,
  ic                        varchar(255),
  amount                    integer,
  constraint pk_ix_ncats_funding primary key (id))
;

create table ix_core_gene (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  constraint pk_ix_core_gene primary key (id))
;

create table ix_ncats_grant (
  id                        bigint auto_increment not null,
  application_id            bigint,
  activity                  varchar(255),
  administering_ic          varchar(255),
  application_type          integer,
  is_arra_funded            tinyint(1) default 0,
  award_notice_date         datetime,
  budget_start              datetime,
  budget_end                datetime,
  cfda_code                 integer,
  foa_number                varchar(255),
  full_project_num          varchar(255),
  subproject_id             bigint,
  fiscal_year               integer,
  ic_name                   varchar(255),
  ed_inst_type              varchar(255),
  nih_spending_cats         varchar(255),
  program_officer_name      varchar(255),
  project_start             datetime,
  project_end               datetime,
  core_project_num          varchar(255),
  project_title             varchar(255),
  public_health_relevance   longtext,
  serial_number             bigint,
  study_section             varchar(255),
  study_section_name        varchar(255),
  suffix                    varchar(255),
  funding_mechanism         varchar(255),
  total_cost                integer,
  total_cost_subproject     integer,
  project_abstract          longtext,
  constraint pk_ix_ncats_grant primary key (id))
;

create table ix_core_group (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  constraint uq_ix_core_group_name unique (name),
  constraint pk_ix_core_group primary key (id))
;

create table ix_ncats_clinical_intervention (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  description               longtext,
  type                      varchar(255),
  constraint pk_ix_ncats_clinical_intervention primary key (id))
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

create table ix_ncats_clinical_outcome (
  id                        bigint auto_increment not null,
  type                      integer,
  measure                   varchar(255),
  timeframe                 varchar(255),
  description               longtext,
  safety_issue              tinyint(1) default 0,
  constraint ck_ix_ncats_clinical_outcome_type check (type in (0,1,2,3)),
  constraint pk_ix_ncats_clinical_outcome primary key (id))
;

create table ix_core_payload (
  id                        bigint auto_increment not null,
  name                      varchar(1024),
  sha1                      varchar(40),
  mime                      varchar(128),
  size                      bigint,
  created                   datetime,
  constraint pk_ix_core_payload primary key (id))
;

create table ix_core_predicate (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  name                      varchar(255) not null,
  object_id                 bigint,
  constraint pk_ix_core_predicate primary key (id))
;

create table ix_core_principal (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  username                  varchar(255),
  email                     varchar(255),
  admin                     tinyint(1) default 0,
  uri                       varchar(1024),
  pkey                      varchar(256),
  selfie_id                 bigint,
  lastname                  varchar(255),
  forename                  varchar(255),
  initials                  varchar(255),
  suffix                    varchar(20),
  affiliation               varchar(1024),
  orcid                     varchar(20),
  url                       varchar(1024),
  ncats_employee            tinyint(1) default 0,
  dn                        varchar(1024),
  uid                       bigint,
  phone                     varchar(15),
  biography                 longtext,
  title                     varchar(255),
  research                  longtext,
  is_lead                   tinyint(1) default 0,
  dept                      integer,
  role                      integer,
  constraint ck_ix_core_principal_dept check (dept in (0,1,2,3)),
  constraint ck_ix_core_principal_role check (role in (0,1,2,3,4)),
  constraint uq_ix_core_principal_pkey unique (pkey),
  constraint pk_ix_core_principal primary key (id))
;

create table ix_core_processingstatus (
  id                        bigint auto_increment not null,
  status                    integer,
  message                   varchar(4000),
  payload_id                bigint,
  constraint ck_ix_core_processingstatus_status check (status in (0,1,2)),
  constraint pk_ix_core_processingstatus primary key (id))
;

create table ix_ncats_program (
  id                        bigint auto_increment not null,
  name                      varchar(64),
  fullname                  varchar(255),
  constraint pk_ix_ncats_program primary key (id))
;

create table ix_ncats_project (
  id                        bigint auto_increment not null,
  title                     varchar(1024),
  objective                 longtext,
  scope                     longtext,
  opportunities             longtext,
  team                      varchar(255),
  is_public                 tinyint(1) default 0,
  curation_id               bigint,
  constraint pk_ix_ncats_project primary key (id))
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
  title                     varchar(1024),
  pages                     varchar(255),
  doi                       varchar(255),
  abstract_text             longtext,
  journal_id                bigint,
  constraint uq_ix_core_publication_pmid unique (pmid),
  constraint uq_ix_core_publication_pmcid unique (pmcid),
  constraint pk_ix_core_publication primary key (id))
;

create table ix_core_resource (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  url                       varchar(1024),
  modifier                  integer,
  constraint ck_ix_core_resource_modifier check (modifier in (0,1,2)),
  constraint uq_ix_core_resource_name unique (name),
  constraint pk_ix_core_resource primary key (id))
;

create table ix_core_role (
  id                        bigint auto_increment not null,
  role                      integer,
  principal_id              bigint,
  constraint ck_ix_core_role_role check (role in (0,1,2,3)),
  constraint pk_ix_core_role primary key (id))
;

create table ix_core_stitch (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  impl                      varchar(1024),
  description               longtext,
  constraint pk_ix_core_stitch primary key (id))
;

create table ix_core_value (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  label                     varchar(255),
  curation_id               bigint,
  numval                    double,
  term                      varchar(255),
  strval                    varchar(1024),
  intval                    bigint,
  major_topic               tinyint(1) default 0,
  heading                   varchar(1024),
  constraint pk_ix_core_value primary key (id))
;

create table ix_core_xref (
  id                        bigint auto_increment not null,
  type                      varchar(512) not null,
  xref_id                   bigint,
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

create table ix_idg_entity_annotation (
  ix_idg_entity_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_entity_annotation primary key (ix_idg_entity_id, ix_core_value_id))
;

create table ix_idg_entity_publication (
  ix_idg_entity_id               bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_idg_entity_publication primary key (ix_idg_entity_id, ix_core_publication_id))
;

create table ix_idg_entity_predicate (
  ix_idg_entity_id               bigint not null,
  ix_core_predicate_id           bigint not null,
  constraint pk_ix_idg_entity_predicate primary key (ix_idg_entity_id, ix_core_predicate_id))
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

create table ix_core_payload_attribute (
  ix_core_payload_id             bigint not null,
  ix_core_attribute_id           bigint not null,
  constraint pk_ix_core_payload_attribute primary key (ix_core_payload_id, ix_core_attribute_id))
;

create table ix_core_predicate_annotation (
  ix_core_predicate_id           bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_predicate_annotation primary key (ix_core_predicate_id, ix_core_value_id))
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

create table ix_core_resource_role (
  ix_core_resource_id            bigint not null,
  ix_core_role_id                bigint not null,
  constraint pk_ix_core_resource_role primary key (ix_core_resource_id, ix_core_role_id))
;

create table ix_core_resource_acl (
  ix_core_resource_id            bigint not null,
  ix_core_acl_id                 bigint not null,
  constraint pk_ix_core_resource_acl primary key (ix_core_resource_id, ix_core_acl_id))
;

create table ix_core_stitch_attribute (
  ix_core_stitch_id              bigint not null,
  ix_core_attribute_id           bigint not null,
  constraint pk_ix_core_stitch_attribute primary key (ix_core_stitch_id, ix_core_attribute_id))
;

create table ix_core_value_attribute (
  ix_core_value_id               bigint not null,
  ix_core_attribute_id           bigint not null,
  constraint pk_ix_core_value_attribute primary key (ix_core_value_id, ix_core_attribute_id))
;
alter table ix_core_attribute add constraint fk_ix_core_attribute_resource_1 foreign key (resource_id) references ix_core_resource (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_resource_1 on ix_core_attribute (resource_id);
alter table ix_ncats_clinical_trial add constraint fk_ix_ncats_clinical_trial_eligibility_2 foreign key (eligibility_id) references ix_ncats_clinical_eligibility (id) on delete restrict on update restrict;
create index ix_ix_ncats_clinical_trial_eligibility_2 on ix_ncats_clinical_trial (eligibility_id);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_3 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_3 on ix_core_curation (curator_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_4 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_4 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint fk_ix_core_edit_principal_5 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_edit_principal_5 on ix_core_edit (principal_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_6 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_6 on ix_core_figure (parent_id);
alter table ix_ncats_funding add constraint fk_ix_ncats_funding_ix_ncats_grant_7 foreign key (grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;
create index ix_ix_ncats_funding_ix_ncats_grant_7 on ix_ncats_funding (grant_id);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organization_8 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organization_8 on ix_core_investigator (organization_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_object_9 foreign key (object_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_object_9 on ix_core_predicate (object_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_10 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_10 on ix_core_principal (selfie_id);
alter table ix_core_processingstatus add constraint fk_ix_core_processingstatus_payload_11 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_processingstatus_payload_11 on ix_core_processingstatus (payload_id);
alter table ix_ncats_project add constraint fk_ix_ncats_project_curation_12 foreign key (curation_id) references ix_core_curation (id) on delete restrict on update restrict;
create index ix_ix_ncats_project_curation_12 on ix_ncats_project (curation_id);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_13 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_13 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journal_14 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journal_14 on ix_core_publication (journal_id);
alter table ix_core_role add constraint fk_ix_core_role_principal_15 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_role_principal_15 on ix_core_role (principal_id);
alter table ix_core_value add constraint fk_ix_core_value_curation_16 foreign key (curation_id) references ix_core_curation (id) on delete restrict on update restrict;
create index ix_ix_core_value_curation_16 on ix_core_value (curation_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_core_acl_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core_acl_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core_group_02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_keyword add constraint fk_ix_ncats_clinical_trial_keyword_ix_ncats_clinical_trial_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_keyword add constraint fk_ix_ncats_clinical_trial_keyword_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_sponsor add constraint fk_ix_ncats_clinical_trial_sponsor_ix_ncats_clinical_trial_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_sponsor add constraint fk_ix_ncats_clinical_trial_sponsor_ix_core_organization_02 foreign key (ix_core_organization_id) references ix_core_organization (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_intervention add constraint fk_ix_ncats_clinical_trial_intervention_ix_ncats_clinical_tri_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_intervention add constraint fk_ix_ncats_clinical_trial_intervention_ix_ncats_clinical_int_02 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_condition add constraint fk_ix_ncats_clinical_trial_condition_ix_ncats_clinical_trial_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_condition add constraint fk_ix_ncats_clinical_trial_condition_ix_ncats_clinical_condit_02 foreign key (ix_ncats_clinical_condition_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_outcome add constraint fk_ix_ncats_clinical_trial_outcome_ix_ncats_clinical_trial_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clinical_trial_outcome add constraint fk_ix_ncats_clinical_trial_outcome_ix_ncats_clinical_outcome_02 foreign key (ix_ncats_clinical_outcome_id) references ix_ncats_clinical_outcome (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_location add constraint fk_ix_ncats_clincial_trial_location_ix_ncats_clinical_trial_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_location add constraint fk_ix_ncats_clincial_trial_location_ix_core_organization_02 foreign key (ix_core_organization_id) references ix_core_organization (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_publication add constraint fk_ix_ncats_clincial_trial_publication_ix_ncats_clinical_tria_01 foreign key (ix_ncats_clinical_trial_id) references ix_ncats_clinical_trial (id) on delete restrict on update restrict;

alter table ix_ncats_clincial_trial_publication add constraint fk_ix_ncats_clincial_trial_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_1 add constraint fk__ix_ncats_cca46885_1_ix_ncats_clinical_condition_01 foreign key (ix_ncats_clinical_condition_synonym_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_1 add constraint fk__ix_ncats_cca46885_1_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_2 add constraint fk__ix_ncats_cca46885_2_ix_ncats_clinical_condition_01 foreign key (ix_ncats_clinical_condition_keyword_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_2 add constraint fk__ix_ncats_cca46885_2_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_3 add constraint fk__ix_ncats_cca46885_3_ix_ncats_clinical_condition_01 foreign key (ix_ncats_clinical_condition_wikipedia_id) references ix_ncats_clinical_condition (id) on delete restrict on update restrict;

alter table _ix_ncats_cca46885_3 add constraint fk__ix_ncats_cca46885_3_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_1 add constraint fk__ix_ncats_840372f9_1_ix_ncats_clinical_eligibility_01 foreign key (ix_ncats_clinical_eligibility_inclusion_id) references ix_ncats_clinical_eligibility (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_1 add constraint fk__ix_ncats_840372f9_1_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_2 add constraint fk__ix_ncats_840372f9_2_ix_ncats_clinical_eligibility_01 foreign key (ix_ncats_clinical_eligibility_exclusion_id) references ix_ncats_clinical_eligibility (id) on delete restrict on update restrict;

alter table _ix_ncats_840372f9_2 add constraint fk__ix_ncats_840372f9_2_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_entity_synonym add constraint fk_ix_idg_entity_synonym_ix_idg_entity_01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_synonym add constraint fk_ix_idg_entity_synonym_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_entity_property add constraint fk_ix_idg_entity_property_ix_idg_entity_01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_property add constraint fk_ix_idg_entity_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_entity_annotation add constraint fk_ix_idg_entity_annotation_ix_idg_entity_01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_annotation add constraint fk_ix_idg_entity_annotation_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_entity_publication add constraint fk_ix_idg_entity_publication_ix_idg_entity_01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_publication add constraint fk_ix_idg_entity_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_idg_entity_predicate add constraint fk_ix_idg_entity_predicate_ix_idg_entity_01 foreign key (ix_idg_entity_id) references ix_idg_entity (id) on delete restrict on update restrict;

alter table ix_idg_entity_predicate add constraint fk_ix_idg_entity_predicate_ix_core_predicate_02 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_core_event_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_core_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_core_gene_synonym add constraint fk_ix_core_gene_synonym_ix_core_gene_01 foreign key (ix_core_gene_id) references ix_core_gene (id) on delete restrict on update restrict;

alter table ix_core_gene_synonym add constraint fk_ix_core_gene_synonym_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_grant_investigator add constraint fk_ix_ncats_grant_investigator_ix_ncats_grant_01 foreign key (ix_ncats_grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;

alter table ix_ncats_grant_investigator add constraint fk_ix_ncats_grant_investigator_ix_core_investigator_02 foreign key (ix_core_investigator_id) references ix_core_investigator (id) on delete restrict on update restrict;

alter table ix_ncats_grant_keyword add constraint fk_ix_ncats_grant_keyword_ix_ncats_grant_01 foreign key (ix_ncats_grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;

alter table ix_ncats_grant_keyword add constraint fk_ix_ncats_grant_keyword_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_grant_publication add constraint fk_ix_ncats_grant_publication_ix_ncats_grant_01 foreign key (ix_ncats_grant_id) references ix_ncats_grant (id) on delete restrict on update restrict;

alter table ix_ncats_grant_publication add constraint fk_ix_ncats_grant_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_core_group_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_1 add constraint fk__ix_ncats_4a162ae3_1_ix_ncats_clinical_intervention_01 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_1 add constraint fk__ix_ncats_4a162ae3_1_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_2 add constraint fk__ix_ncats_4a162ae3_2_ix_ncats_clinical_intervention_01 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_2 add constraint fk__ix_ncats_4a162ae3_2_ix_ncats_clinical_arm_02 foreign key (ix_ncats_clinical_arm_id) references ix_ncats_clinical_arm (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_3 add constraint fk__ix_ncats_4a162ae3_3_ix_ncats_clinical_intervention_01 foreign key (ix_ncats_clinical_intervention_id) references ix_ncats_clinical_intervention (id) on delete restrict on update restrict;

alter table _ix_ncats_4a162ae3_3 add constraint fk__ix_ncats_4a162ae3_3_ix_ncats_clinical_cohort_02 foreign key (ix_ncats_clinical_cohort_id) references ix_ncats_clinical_cohort (id) on delete restrict on update restrict;

alter table ix_core_payload_attribute add constraint fk_ix_core_payload_attribute_ix_core_payload_01 foreign key (ix_core_payload_id) references ix_core_payload (id) on delete restrict on update restrict;

alter table ix_core_payload_attribute add constraint fk_ix_core_payload_attribute_ix_core_attribute_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_core_predicate_annotation add constraint fk_ix_core_predicate_annotation_ix_core_predicate_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_annotation add constraint fk_ix_core_predicate_annotation_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_project_program add constraint fk_ix_ncats_project_program_ix_ncats_project_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_program add constraint fk_ix_ncats_project_program_ix_ncats_program_02 foreign key (ix_ncats_program_id) references ix_ncats_program (id) on delete restrict on update restrict;

alter table ix_ncats_project_keyword add constraint fk_ix_ncats_project_keyword_ix_ncats_project_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_keyword add constraint fk_ix_ncats_project_keyword_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_ncats_project_member add constraint fk_ix_ncats_project_member_ix_ncats_project_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_member add constraint fk_ix_ncats_project_member_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ncats_project_collaborator add constraint fk_ix_ncats_project_collaborator_ix_ncats_project_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_collaborator add constraint fk_ix_ncats_project_collaborator_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_ncats_project_figure add constraint fk_ix_ncats_project_figure_ix_ncats_project_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_figure add constraint fk_ix_ncats_project_figure_ix_core_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_ncats_project_milestone add constraint fk_ix_ncats_project_milestone_ix_ncats_project_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_milestone add constraint fk_ix_ncats_project_milestone_ix_core_event_02 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_ncats_project_publication add constraint fk_ix_ncats_project_publication_ix_ncats_project_01 foreign key (ix_ncats_project_id) references ix_ncats_project (id) on delete restrict on update restrict;

alter table ix_ncats_project_publication add constraint fk_ix_ncats_project_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keyword_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keyword_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_ix_core_pubauthor_02 foreign key (ix_core_pubauthor_id) references ix_core_pubauthor (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_ix_core_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_core_resource_role add constraint fk_ix_core_resource_role_ix_core_resource_01 foreign key (ix_core_resource_id) references ix_core_resource (id) on delete restrict on update restrict;

alter table ix_core_resource_role add constraint fk_ix_core_resource_role_ix_core_role_02 foreign key (ix_core_role_id) references ix_core_role (id) on delete restrict on update restrict;

alter table ix_core_resource_acl add constraint fk_ix_core_resource_acl_ix_core_resource_01 foreign key (ix_core_resource_id) references ix_core_resource (id) on delete restrict on update restrict;

alter table ix_core_resource_acl add constraint fk_ix_core_resource_acl_ix_core_acl_02 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_ix_core_stitch_01 foreign key (ix_core_stitch_id) references ix_core_stitch (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_ix_core_attribute_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_core_value_attribute add constraint fk_ix_core_value_attribute_ix_core_value_01 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_value_attribute add constraint fk_ix_core_value_attribute_ix_core_attribute_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table ix_core_acl;

drop table ix_core_acl_principal;

drop table ix_core_acl_group;

drop table ix_ncats_clinical_arm;

drop table ix_core_attribute;

drop table ix_ncats_clinical_trial;

drop table ix_ncats_clinical_trial_keyword;

drop table ix_ncats_clinical_trial_sponsor;

drop table ix_ncats_clinical_trial_intervention;

drop table ix_ncats_clinical_trial_condition;

drop table ix_ncats_clinical_trial_outcome;

drop table ix_ncats_clincial_trial_location;

drop table ix_ncats_clincial_trial_publication;

drop table ix_ncats_clinical_cohort;

drop table ix_ncats_clinical_condition;

drop table _ix_ncats_cca46885_1;

drop table _ix_ncats_cca46885_2;

drop table _ix_ncats_cca46885_3;

drop table ix_core_curation;

drop table ix_core_etag;

drop table ix_core_etagref;

drop table ix_core_edit;

drop table ix_ncats_clinical_eligibility;

drop table _ix_ncats_840372f9_1;

drop table _ix_ncats_840372f9_2;

drop table ix_idg_entity;

drop table ix_idg_entity_synonym;

drop table ix_idg_entity_property;

drop table ix_idg_entity_annotation;

drop table ix_idg_entity_publication;

drop table ix_idg_entity_predicate;

drop table ix_core_event;

drop table ix_core_event_figure;

drop table ix_core_figure;

drop table ix_ncats_funding;

drop table ix_core_gene;

drop table ix_core_gene_synonym;

drop table ix_ncats_grant;

drop table ix_ncats_grant_investigator;

drop table ix_ncats_grant_keyword;

drop table ix_ncats_grant_publication;

drop table ix_core_group;

drop table ix_core_group_principal;

drop table ix_ncats_clinical_intervention;

drop table _ix_ncats_4a162ae3_1;

drop table _ix_ncats_4a162ae3_2;

drop table _ix_ncats_4a162ae3_3;

drop table ix_core_investigator;

drop table ix_core_journal;

drop table ix_core_organization;

drop table ix_ncats_clinical_outcome;

drop table ix_core_payload;

drop table ix_core_payload_attribute;

drop table ix_core_predicate;

drop table ix_core_predicate_annotation;

drop table ix_core_principal;

drop table ix_core_processingstatus;

drop table ix_ncats_program;

drop table ix_ncats_project;

drop table ix_ncats_project_program;

drop table ix_ncats_project_keyword;

drop table ix_ncats_project_member;

drop table ix_ncats_project_collaborator;

drop table ix_ncats_project_figure;

drop table ix_ncats_project_milestone;

drop table ix_ncats_project_publication;

drop table ix_core_pubauthor;

drop table ix_core_publication;

drop table ix_core_publication_keyword;

drop table ix_core_publication_mesh;

drop table ix_core_publication_author;

drop table ix_core_publication_figure;

drop table ix_core_resource;

drop table ix_core_resource_role;

drop table ix_core_resource_acl;

drop table ix_core_role;

drop table ix_core_stitch;

drop table ix_core_stitch_attribute;

drop table ix_core_value;

drop table ix_core_value_attribute;

drop table ix_core_xref;

SET FOREIGN_KEY_CHECKS=1;

