# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ix_core_acl (
  id                        bigint auto_increment not null,
  perm                      integer,
  constraint ck_ix_core_acl_perm check (perm in (0,1,2,3,4,5,6)),
  constraint pk_ix_core_acl primary key (id))
;

create table ix_core_attribute (
  id                        bigint auto_increment not null,
  name                      varchar(255),
  value                     varchar(1024),
  namespace_id              bigint,
  constraint pk_ix_core_attribute primary key (id))
;

create table ix_core_curation (
  id                        bigint auto_increment not null,
  curator_id                bigint,
  status                    integer,
  timestamp                 datetime,
  constraint ck_ix_core_curation_status check (status in (0,1,2,3)),
  constraint pk_ix_core_curation primary key (id))
;

create table ix_idg_disease (
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
  name                      varchar(1024),
  description               longtext,
  version                   bigint not null,
  constraint pk_ix_idg_disease primary key (id))
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

create table ix_idg_gene (
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
  name                      varchar(1024),
  description               longtext,
  version                   bigint not null,
  constraint pk_ix_idg_gene primary key (id))
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

create table ix_idg_ligand (
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
  name                      varchar(1024),
  description               longtext,
  version                   bigint not null,
  constraint pk_ix_idg_ligand primary key (id))
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

create table ix_idg_tinx (
  id                        bigint auto_increment not null,
  uniprot_id                varchar(255) not null,
  doid                      varchar(255) not null,
  novelty                   double,
  importance                double,
  constraint pk_ix_idg_tinx primary key (id))
;

create table ix_idg_target (
  id                        bigint auto_increment not null,
  namespace_id              bigint,
  created                   datetime,
  modified                  datetime,
  deprecated                tinyint(1) default 0,
  name                      varchar(1024),
  description               longtext,
  organism_id               bigint,
  idg_family                varchar(128),
  idg_tdl                   integer(10),
  novelty                   double,
  version                   bigint not null,
  constraint ck_ix_idg_target_idg_tdl check (idg_tdl in (0,1,2,3,4,5)),
  constraint pk_ix_idg_target primary key (id))
;

create table ix_core_value (
  dtype                     varchar(10) not null,
  id                        bigint auto_increment not null,
  label                     varchar(255),
  text                      longtext,
  lval                      double,
  rval                      double,
  average                   double,
  numval                    double,
  unit                      varchar(255),
  data                      longblob,
  data_size                 integer,
  sha1                      varchar(40),
  mime_type                 varchar(32),
  term                      varchar(255),
  href                      longtext,
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

create table ix_idg_disease_synonym (
  ix_idg_disease_synonym_id      bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_disease_synonym primary key (ix_idg_disease_synonym_id, ix_core_value_id))
;

create table ix_idg_disease_property (
  ix_idg_disease_id              bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_disease_property primary key (ix_idg_disease_id, ix_core_value_id))
;

create table ix_idg_disease_link (
  ix_idg_disease_id              bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_idg_disease_link primary key (ix_idg_disease_id, ix_core_xref_id))
;

create table ix_idg_disease_publication (
  ix_idg_disease_id              bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_idg_disease_publication primary key (ix_idg_disease_id, ix_core_publication_id))
;

create table ix_core_event_figure (
  ix_core_event_id               bigint not null,
  ix_core_figure_id              bigint not null,
  constraint pk_ix_core_event_figure primary key (ix_core_event_id, ix_core_figure_id))
;

create table ix_idg_gene_synonym (
  ix_idg_gene_synonym_id         bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_gene_synonym primary key (ix_idg_gene_synonym_id, ix_core_value_id))
;

create table ix_idg_gene_property (
  ix_idg_gene_id                 bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_gene_property primary key (ix_idg_gene_id, ix_core_value_id))
;

create table ix_idg_gene_link (
  ix_idg_gene_id                 bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_idg_gene_link primary key (ix_idg_gene_id, ix_core_xref_id))
;

create table ix_idg_gene_publication (
  ix_idg_gene_id                 bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_idg_gene_publication primary key (ix_idg_gene_id, ix_core_publication_id))
;

create table ix_core_group_principal (
  ix_core_group_id               bigint not null,
  ix_core_principal_id           bigint not null,
  constraint pk_ix_core_group_principal primary key (ix_core_group_id, ix_core_principal_id))
;

create table ix_idg_ligand_synonym (
  ix_idg_ligand_synonym_id       bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_ligand_synonym primary key (ix_idg_ligand_synonym_id, ix_core_value_id))
;

create table ix_idg_ligand_property (
  ix_idg_ligand_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_ligand_property primary key (ix_idg_ligand_id, ix_core_value_id))
;

create table ix_idg_ligand_link (
  ix_idg_ligand_id               bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_idg_ligand_link primary key (ix_idg_ligand_id, ix_core_xref_id))
;

create table ix_idg_ligand_publication (
  ix_idg_ligand_id               bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_idg_ligand_publication primary key (ix_idg_ligand_id, ix_core_publication_id))
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

create table ix_idg_target_synonym (
  ix_idg_target_synonym_id       bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_target_synonym primary key (ix_idg_target_synonym_id, ix_core_value_id))
;

create table ix_idg_target_property (
  ix_idg_target_id               bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_idg_target_property primary key (ix_idg_target_id, ix_core_value_id))
;

create table ix_idg_target_link (
  ix_idg_target_id               bigint not null,
  ix_core_xref_id                bigint not null,
  constraint pk_ix_idg_target_link primary key (ix_idg_target_id, ix_core_xref_id))
;

create table ix_idg_target_publication (
  ix_idg_target_id               bigint not null,
  ix_core_publication_id         bigint not null,
  constraint pk_ix_idg_target_publication primary key (ix_idg_target_id, ix_core_publication_id))
;

create table ix_core_xref_property (
  ix_core_xref_id                bigint not null,
  ix_core_value_id               bigint not null,
  constraint pk_ix_core_xref_property primary key (ix_core_xref_id, ix_core_value_id))
;
alter table ix_core_attribute add constraint fk_ix_core_attribute_namespace_1 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_attribute_namespace_1 on ix_core_attribute (namespace_id);
alter table ix_core_curation add constraint fk_ix_core_curation_curator_2 foreign key (curator_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_curation_curator_2 on ix_core_curation (curator_id);
alter table ix_idg_disease add constraint fk_ix_idg_disease_namespace_3 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_idg_disease_namespace_3 on ix_idg_disease (namespace_id);
alter table ix_core_etag add constraint fk_ix_core_etag_namespace_4 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_etag_namespace_4 on ix_core_etag (namespace_id);
alter table ix_core_etagref add constraint fk_ix_core_etagref_etag_5 foreign key (etag_id) references ix_core_etag (id) on delete restrict on update restrict;
create index ix_ix_core_etagref_etag_5 on ix_core_etagref (etag_id);
alter table ix_core_edit add constraint fk_ix_core_edit_editor_6 foreign key (editor_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_edit_editor_6 on ix_core_edit (editor_id);
alter table ix_core_figure add constraint fk_ix_core_figure_parent_7 foreign key (parent_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_figure_parent_7 on ix_core_figure (parent_id);
alter table ix_idg_gene add constraint fk_ix_idg_gene_namespace_8 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_idg_gene_namespace_8 on ix_idg_gene (namespace_id);
alter table ix_core_investigator add constraint fk_ix_core_investigator_organization_9 foreign key (organization_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_investigator_organization_9 on ix_core_investigator (organization_id);
alter table ix_idg_ligand add constraint fk_ix_idg_ligand_namespace_10 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_idg_ligand_namespace_10 on ix_idg_ligand (namespace_id);
alter table ix_core_namespace add constraint fk_ix_core_namespace_owner_11 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_namespace_owner_11 on ix_core_namespace (owner_id);
alter table ix_core_payload add constraint fk_ix_core_payload_namespace_12 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_payload_namespace_12 on ix_core_payload (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_namespace_13 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_namespace_13 on ix_core_predicate (namespace_id);
alter table ix_core_predicate add constraint fk_ix_core_predicate_subject_14 foreign key (subject_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_predicate_subject_14 on ix_core_predicate (subject_id);
alter table ix_core_principal add constraint fk_ix_core_principal_namespace_15 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_principal_namespace_15 on ix_core_principal (namespace_id);
alter table ix_core_principal add constraint fk_ix_core_principal_selfie_16 foreign key (selfie_id) references ix_core_figure (id) on delete restrict on update restrict;
create index ix_ix_core_principal_selfie_16 on ix_core_principal (selfie_id);
alter table ix_core_principal add constraint fk_ix_core_principal_institution_17 foreign key (institution_id) references ix_core_organization (id) on delete restrict on update restrict;
create index ix_ix_core_principal_institution_17 on ix_core_principal (institution_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_owner_18 foreign key (owner_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_owner_18 on ix_core_procjob (owner_id);
alter table ix_core_procjob add constraint fk_ix_core_procjob_payload_19 foreign key (payload_id) references ix_core_payload (id) on delete restrict on update restrict;
create index ix_ix_core_procjob_payload_19 on ix_core_procjob (payload_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_xref_20 foreign key (xref_id) references ix_core_xref (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_xref_20 on ix_core_procrecord (xref_id);
alter table ix_core_procrecord add constraint fk_ix_core_procrecord_job_21 foreign key (job_id) references ix_core_procjob (id) on delete restrict on update restrict;
create index ix_ix_core_procrecord_job_21 on ix_core_procrecord (job_id);
alter table ix_core_pubauthor add constraint fk_ix_core_pubauthor_author_22 foreign key (author_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_pubauthor_author_22 on ix_core_pubauthor (author_id);
alter table ix_core_publication add constraint fk_ix_core_publication_journal_23 foreign key (journal_id) references ix_core_journal (id) on delete restrict on update restrict;
create index ix_ix_core_publication_journal_23 on ix_core_publication (journal_id);
alter table ix_core_role add constraint fk_ix_core_role_principal_24 foreign key (principal_id) references ix_core_principal (id) on delete restrict on update restrict;
create index ix_ix_core_role_principal_24 on ix_core_role (principal_id);
alter table ix_core_structure add constraint fk_ix_core_structure_namespace_25 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_structure_namespace_25 on ix_core_structure (namespace_id);
alter table ix_idg_target add constraint fk_ix_idg_target_namespace_26 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_idg_target_namespace_26 on ix_idg_target (namespace_id);
alter table ix_idg_target add constraint fk_ix_idg_target_organism_27 foreign key (organism_id) references ix_core_value (id) on delete restrict on update restrict;
create index ix_ix_idg_target_organism_27 on ix_idg_target (organism_id);
alter table ix_core_xref add constraint fk_ix_core_xref_namespace_28 foreign key (namespace_id) references ix_core_namespace (id) on delete restrict on update restrict;
create index ix_ix_core_xref_namespace_28 on ix_core_xref (namespace_id);



alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_core_acl_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_principal add constraint fk_ix_core_acl_principal_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core_acl_01 foreign key (ix_core_acl_id) references ix_core_acl (id) on delete restrict on update restrict;

alter table ix_core_acl_group add constraint fk_ix_core_acl_group_ix_core_group_02 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_idg_disease_synonym add constraint fk_ix_idg_disease_synonym_ix_idg_disease_01 foreign key (ix_idg_disease_synonym_id) references ix_idg_disease (id) on delete restrict on update restrict;

alter table ix_idg_disease_synonym add constraint fk_ix_idg_disease_synonym_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_disease_property add constraint fk_ix_idg_disease_property_ix_idg_disease_01 foreign key (ix_idg_disease_id) references ix_idg_disease (id) on delete restrict on update restrict;

alter table ix_idg_disease_property add constraint fk_ix_idg_disease_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_disease_link add constraint fk_ix_idg_disease_link_ix_idg_disease_01 foreign key (ix_idg_disease_id) references ix_idg_disease (id) on delete restrict on update restrict;

alter table ix_idg_disease_link add constraint fk_ix_idg_disease_link_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_idg_disease_publication add constraint fk_ix_idg_disease_publication_ix_idg_disease_01 foreign key (ix_idg_disease_id) references ix_idg_disease (id) on delete restrict on update restrict;

alter table ix_idg_disease_publication add constraint fk_ix_idg_disease_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_core_event_01 foreign key (ix_core_event_id) references ix_core_event (id) on delete restrict on update restrict;

alter table ix_core_event_figure add constraint fk_ix_core_event_figure_ix_core_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_idg_gene_synonym add constraint fk_ix_idg_gene_synonym_ix_idg_gene_01 foreign key (ix_idg_gene_synonym_id) references ix_idg_gene (id) on delete restrict on update restrict;

alter table ix_idg_gene_synonym add constraint fk_ix_idg_gene_synonym_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_gene_property add constraint fk_ix_idg_gene_property_ix_idg_gene_01 foreign key (ix_idg_gene_id) references ix_idg_gene (id) on delete restrict on update restrict;

alter table ix_idg_gene_property add constraint fk_ix_idg_gene_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_gene_link add constraint fk_ix_idg_gene_link_ix_idg_gene_01 foreign key (ix_idg_gene_id) references ix_idg_gene (id) on delete restrict on update restrict;

alter table ix_idg_gene_link add constraint fk_ix_idg_gene_link_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_idg_gene_publication add constraint fk_ix_idg_gene_publication_ix_idg_gene_01 foreign key (ix_idg_gene_id) references ix_idg_gene (id) on delete restrict on update restrict;

alter table ix_idg_gene_publication add constraint fk_ix_idg_gene_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_core_group_01 foreign key (ix_core_group_id) references ix_core_group (id) on delete restrict on update restrict;

alter table ix_core_group_principal add constraint fk_ix_core_group_principal_ix_core_principal_02 foreign key (ix_core_principal_id) references ix_core_principal (id) on delete restrict on update restrict;

alter table ix_idg_ligand_synonym add constraint fk_ix_idg_ligand_synonym_ix_idg_ligand_01 foreign key (ix_idg_ligand_synonym_id) references ix_idg_ligand (id) on delete restrict on update restrict;

alter table ix_idg_ligand_synonym add constraint fk_ix_idg_ligand_synonym_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_ligand_property add constraint fk_ix_idg_ligand_property_ix_idg_ligand_01 foreign key (ix_idg_ligand_id) references ix_idg_ligand (id) on delete restrict on update restrict;

alter table ix_idg_ligand_property add constraint fk_ix_idg_ligand_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_ligand_link add constraint fk_ix_idg_ligand_link_ix_idg_ligand_01 foreign key (ix_idg_ligand_id) references ix_idg_ligand (id) on delete restrict on update restrict;

alter table ix_idg_ligand_link add constraint fk_ix_idg_ligand_link_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_idg_ligand_publication add constraint fk_ix_idg_ligand_publication_ix_idg_ligand_01 foreign key (ix_idg_ligand_id) references ix_idg_ligand (id) on delete restrict on update restrict;

alter table ix_idg_ligand_publication add constraint fk_ix_idg_ligand_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_ix_core_payload_01 foreign key (ix_core_payload_id) references ix_core_payload (id) on delete restrict on update restrict;

alter table ix_core_payload_property add constraint fk_ix_core_payload_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_ix_core_predicate_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_object add constraint fk_ix_core_predicate_object_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_ix_core_predicate_01 foreign key (ix_core_predicate_id) references ix_core_predicate (id) on delete restrict on update restrict;

alter table ix_core_predicate_property add constraint fk_ix_core_predicate_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keyword_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_keyword add constraint fk_ix_core_publication_keyword_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_mesh add constraint fk_ix_core_publication_mesh_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_author add constraint fk_ix_core_publication_author_ix_core_pubauthor_02 foreign key (ix_core_pubauthor_id) references ix_core_pubauthor (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_ix_core_publication_01 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_publication_figure add constraint fk_ix_core_publication_figure_ix_core_figure_02 foreign key (ix_core_figure_id) references ix_core_figure (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_ix_core_stitch_01 foreign key (ix_core_stitch_id) references ix_core_stitch (id) on delete restrict on update restrict;

alter table ix_core_stitch_attribute add constraint fk_ix_core_stitch_attribute_ix_core_attribute_02 foreign key (ix_core_attribute_id) references ix_core_attribute (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_ix_core_structure_01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_property add constraint fk_ix_core_structure_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix_core_structure_01 foreign key (ix_core_structure_id) references ix_core_structure (id) on delete restrict on update restrict;

alter table ix_core_structure_link add constraint fk_ix_core_structure_link_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_idg_target_synonym add constraint fk_ix_idg_target_synonym_ix_idg_target_01 foreign key (ix_idg_target_synonym_id) references ix_idg_target (id) on delete restrict on update restrict;

alter table ix_idg_target_synonym add constraint fk_ix_idg_target_synonym_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_target_property add constraint fk_ix_idg_target_property_ix_idg_target_01 foreign key (ix_idg_target_id) references ix_idg_target (id) on delete restrict on update restrict;

alter table ix_idg_target_property add constraint fk_ix_idg_target_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

alter table ix_idg_target_link add constraint fk_ix_idg_target_link_ix_idg_target_01 foreign key (ix_idg_target_id) references ix_idg_target (id) on delete restrict on update restrict;

alter table ix_idg_target_link add constraint fk_ix_idg_target_link_ix_core_xref_02 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_idg_target_publication add constraint fk_ix_idg_target_publication_ix_idg_target_01 foreign key (ix_idg_target_id) references ix_idg_target (id) on delete restrict on update restrict;

alter table ix_idg_target_publication add constraint fk_ix_idg_target_publication_ix_core_publication_02 foreign key (ix_core_publication_id) references ix_core_publication (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_core_xref_01 foreign key (ix_core_xref_id) references ix_core_xref (id) on delete restrict on update restrict;

alter table ix_core_xref_property add constraint fk_ix_core_xref_property_ix_core_value_02 foreign key (ix_core_value_id) references ix_core_value (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table ix_core_acl;

drop table ix_core_acl_principal;

drop table ix_core_acl_group;

drop table ix_core_attribute;

drop table ix_core_curation;

drop table ix_idg_disease;

drop table ix_idg_disease_synonym;

drop table ix_idg_disease_property;

drop table ix_idg_disease_link;

drop table ix_idg_disease_publication;

drop table ix_core_etag;

drop table ix_core_etagref;

drop table ix_core_edit;

drop table ix_core_event;

drop table ix_core_event_figure;

drop table ix_core_figure;

drop table ix_idg_gene;

drop table ix_idg_gene_synonym;

drop table ix_idg_gene_property;

drop table ix_idg_gene_link;

drop table ix_idg_gene_publication;

drop table ix_core_group;

drop table ix_core_group_principal;

drop table ix_core_investigator;

drop table ix_core_journal;

drop table ix_idg_ligand;

drop table ix_idg_ligand_synonym;

drop table ix_idg_ligand_property;

drop table ix_idg_ligand_link;

drop table ix_idg_ligand_publication;

drop table ix_core_namespace;

drop table ix_core_organization;

drop table ix_core_payload;

drop table ix_core_payload_property;

drop table ix_core_predicate;

drop table ix_core_predicate_object;

drop table ix_core_predicate_property;

drop table ix_core_principal;

drop table ix_core_procjob;

drop table ix_core_procrecord;

drop table ix_core_pubauthor;

drop table ix_core_publication;

drop table ix_core_publication_keyword;

drop table ix_core_publication_mesh;

drop table ix_core_publication_author;

drop table ix_core_publication_figure;

drop table ix_core_role;

drop table ix_core_stitch;

drop table ix_core_stitch_attribute;

drop table ix_core_structure;

drop table ix_core_structure_property;

drop table ix_core_structure_link;

drop table ix_idg_tinx;

drop table ix_idg_target;

drop table ix_idg_target_synonym;

drop table ix_idg_target_property;

drop table ix_idg_target_link;

drop table ix_idg_target_publication;

drop table ix_core_value;

drop table ix_core_xref;

drop table ix_core_xref_property;

SET FOREIGN_KEY_CHECKS=1;

