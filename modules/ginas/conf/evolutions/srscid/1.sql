# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table CVM_ADVERSE_EVENTS (
  BDNUM                     varchar(255),
  DRUG                      varchar(255),
  ROUTE_OF_ADMINISTRATION   varchar(255),
  SPECIES                   varchar(255),
  ADVERSE_EVENT             varchar(255),
  AE_COUNT                  integer)
;

create table SRSCID_ADVERSE_EVT_CVM_ALL_V (
  ID                        varchar(255) not null,
  ROUTE_OF_ADMINISTRATION   varchar(255),
  SPECIES                   varchar(255),
  ADVERSE_EVENT             varchar(255),
  AE_COUNT                  integer,
  NAME                      varchar(255),
  BDNUM                     varchar(255),
  UNII                      varchar(255),
  SUBSTANCE_ID              varchar(255),
  constraint pk_SRSCID_ADVERSE_EVT_CVM_ALL_V primary key (ID))
;

create table SRSCID_ADVERSE_EVENT_DME (
  BDNUM                     varchar(255),
  INAME                     varchar(255),
  DME_REACTIONS             varchar(255),
  PTTERM_MEDDRA             varchar(255),
  CASE_COUNT                integer,
  DME_COUNT                 integer,
  DME_COUNT_PERCENT         double)
;

create table SRSCID_ADVERSE_EVT_DME_ALL_V (
  ID                        varchar(255) not null,
  DME_REACTIONS             varchar(255),
  PTTERM_MEDDRA             varchar(255),
  CASE_COUNT                integer,
  DME_COUNT                 integer,
  DME_COUNT_PERCENT         double,
  WEIGHTED_AVG_PRR          Decimal(10,3) default 0.0,
  NAME                      varchar(255),
  BDNUM                     varchar(255),
  UNII                      varchar(255),
  SUBSTANCE_ID              varchar(255),
  constraint pk_SRSCID_ADVERSE_EVT_DME_ALL_V primary key (ID))
;

create table FAERS_DBL_AE_CASE_DETAILS (
  PT                        varchar(255),
  SOC                       varchar(255),
  FPD_PAI                   varchar(255),
  FPD_PROD                  varchar(255),
  FPD_AMOIETY               varchar(255),
  CASE_ID                   varchar(255),
  CASE_VERSION              varchar(255),
  INITIAL_PROCESS_DATE      timestamp,
  LATEST_PROCESS_DATE       timestamp,
  SUSPECT_DRUGS_LIST        varchar(255),
  PS_DRUG_NAME              varchar(255),
  DOSAGE_PRIMARY_SUSPECT    varchar(255),
  ALL_OUTCOMES              varchar(255),
  LLT_LIST                  varchar(255),
  PT_LIST                   varchar(255),
  AGE_IN_YEARS              varchar(255),
  AGE_IN_UNITS              varchar(255),
  ORGANIZATION              varchar(255),
  SEX                       varchar(255),
  INDICATION_PRIMARY_SUSPECT varchar(255),
  CONCOMMITANT_DRUG_LIST    varchar(255),
  COUNTRY                   varchar(255),
  REPORTER_SOURCE           varchar(255),
  MCN                       varchar(255),
  CASE_TYPE                 varchar(255),
  NARR_FROM1TO3900          varchar(255),
  NARR_FROM3901TO4000       varchar(255))
;

create table SRSCID_ADVERSE_EVENT_PT (
  BDNUM                     varchar(255),
  INAME                     varchar(255),
  PT_TERM                   varchar(255),
  PRIM_SOC                  varchar(255),
  CASE_COUNT                integer,
  SOC_COUNT                 integer,
  PT_COUNT                  integer,
  SOC_COUNT_PERCENT         double,
  PT_COUNT_PERCENT          double,
  PT_COUNT_TOTAL_VS_THIS_DRUG integer)
;

create table SRSCID_ADVERSE_EVENT_PT_ALL_V (
  ID                        varchar(255) not null,
  PT_TERM                   varchar(255),
  PRIM_SOC                  varchar(255),
  CASE_COUNT                integer,
  SOC_COUNT                 integer,
  PT_COUNT                  integer,
  SOC_COUNT_PERCENT         double,
  PT_COUNT_PERCENT          double,
  PT_COUNT_TOTAL_VS_THIS_DRUG integer,
  PRR                       double,
  NAME                      varchar(255),
  UNII                      varchar(255),
  SUBSTANCE_ID              varchar(255),
  BDNUM                     varchar(255),
  constraint pk_SRSCID_ADVERSE_EVENT_PT_ALL_V primary key (ID))
;

create table SRSCID_APP_INDICATION_ALL_V (
  ID                        varchar(255) not null,
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  INDICATION                varchar(255),
  constraint pk_SRSCID_APP_INDICATION_ALL_V primary key (ID))
;

create table SRSCID_APPLICATION_TYPE_SRS (
  APPLICATION_TYPE_ID       integer not null,
  APPLICANT_INGRED_NAME     varchar(255),
  BDNUM                     varchar(255),
  BASIS_OF_STRENGTH         varchar(255),
  AVERAGE                   double,
  LOW                       double,
  HIGH                      double,
  INGREDIENT_TYPE           varchar(255),
  UNIT                      varchar(255),
  FARM_SUBSTANCE_ID         integer,
  FARM_SUBSTANCE            varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_ID                integer,
  constraint pk_SRSCID_APPLICATION_TYPE_SRS primary key (APPLICATION_TYPE_ID))
;

create table SRSCID_APPLICATION_ALL_V (
  ID                        varchar(255) not null,
  SUBSTANCE_ID              varchar(255) not null,
  BDNUM                     varchar(255),
  APPLICATION_ID            integer,
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  PRODUCT_NAME              varchar(255),
  SPONSOR_NAME              varchar(255),
  STATUS                    varchar(255),
  DIVISION_CLASS_DESC       varchar(255),
  CENTER                    varchar(255),
  INGREDIENT_TYPE           varchar(255),
  FROMTABLE                 varchar(255),
  IN_DARRTS_DETAIL          varchar(255),
  NAME                      varchar(255),
  UNII                      varchar(255),
  APPCOUNT                  varchar(255),
  APPSRSCOUNT               varchar(255),
  constraint pk_SRSCID_APPLICATION_ALL_V primary key (ID))
;

create table SRSCID_APPLICATION_MV (
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  APP_SUB_TYPE              varchar(255),
  APP_SUB_TYPE_DESC         varchar(255),
  PRODUCT_NAME              varchar(255),
  DOSAGE_FORM_DESC          varchar(255),
  SPONSOR_NAME              varchar(255),
  DIVISION_CLASS            varchar(255),
  DIVISION_CLASS_DESC       varchar(255),
  APP_STATUS                varchar(255),
  APP_STATUS_DATE           varchar(255))
;

create table SRSCID_APPLICATION_TYPE_MV (
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  BDNUM                     varchar(255),
  ACTIVITY                  varchar(255),
  POTENCY                   varchar(255),
  PRODUCT_NO                varchar(255),
  PART_NO                   varchar(255))
;

create table SRSCID_APP_INDICATION_MV (
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  INDICATION                varchar(255),
  INDICATION_PK             integer)
;

create table SRSCID_APP_INDICATION_SRS (
  APP_INDICATION_ID         integer not null,
  INDICATION                varchar(255),
  APPLICATION_ID_FK         integer,
  constraint pk_SRSCID_APP_INDICATION_SRS primary key (APP_INDICATION_ID))
;

create table SRSCID_APPLICATION_SRS (
  APPLICATION_ID            integer not null,
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  APPLICATION_TITLE         varchar(255),
  SPONSOR_NAME              varchar(255),
  NONPROPRIETARY_NAME       varchar(255),
  SUBMIT_DATE               timestamp,
  APP_SUB_TYPE              varchar(255),
  DIVISION_CLASS_DESC       varchar(255),
  STATUS                    varchar(255),
  CENTER                    varchar(255),
  SOURCE                    varchar(255),
  PUBLIC_DOMAIN             varchar(255),
  VERSION                   integer,
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  constraint pk_SRSCID_APPLICATION_SRS primary key (APPLICATION_ID))
;

create table SRSCID_BDNUM_NAME (
  BDNUM                     varchar(255) not null,
  SUBSTANCE_ID              varchar(255),
  UNII                      varchar(255),
  NAME                      varchar(255),
  STATUS                    varchar(255),
  SUBSTANCE_TYPE            varchar(255),
  CLINICALCOUNT             integer,
  constraint pk_SRSCID_BDNUM_NAME primary key (BDNUM))
;

create table SRSCID_BDNUM_NAME_ALL (
  NAME                      varchar(255) not null,
  BDNUM                     varchar(255),
  SUBSTANCE_ID              varchar(255),
  UNII                      varchar(255),
  SUBSTANCE_TYPE            varchar(255),
  constraint pk_SRSCID_BDNUM_NAME_ALL primary key (NAME))
;

create table SRSCID_BIOMARKER (
  ID                        integer not null,
  created_by                varchar(255),
  create_date               timestamp,
  modified_by               varchar(255),
  modify_date               timestamp,
  BDNUM                     varchar(255),
  BIOMARKER_TYPE            varchar(255),
  BIOMARKER_USE_TYPE        varchar(255),
  constraint pk_SRSCID_BIOMARKER primary key (ID))
;

create table SRSCID_BIOMARKER_SOURCE (
  ID                        integer not null,
  SOURCE_TYPE               varchar(255),
  SOURCE_ID                 varchar(255),
  COMMENTS                  varchar(255),
  BIOMARKER_ID              integer,
  constraint pk_SRSCID_BIOMARKER_SOURCE primary key (ID))
;

create table SRSCID_BIOMARKER_SOURCE_TEST (
  source_id                 integer not null,
  test_id                   integer not null)
;

create table SRSCID_BIOMARKER_TEST (
  ID                        integer not null,
  TEST_UNIQUE_ID            varchar(255),
  TEST_NAME                 varchar(255),
  TEST_TYPE                 varchar(255),
  COMMENTS                  varchar(255),
  BIOMARKER_ID              integer,
  constraint pk_SRSCID_BIOMARKER_TEST primary key (ID))
;

create table SRSCID_BIOMARKER_TESTDETAIL (
  ID                        integer not null,
  TEST_DETAILS              varchar(255),
  TEST_MANUFACTURE          varchar(255),
  TEST_SOURCE               varchar(255),
  TEST_SOURCE_TYPE          varchar(255),
  COMMENTS                  varchar(255),
  BIOMARKER_TEST_ID         integer,
  constraint pk_SRSCID_BIOMARKER_TESTDETAIL primary key (ID))
;

create table SRSCID_CLINICAL_TRIAL (
  NCT_NUMBER                varchar(255) not null,
  TITLE                     varchar(255),
  RECRUITMENT               varchar(255),
  CONDITIONS                varchar(255),
  INTERVENTION              varchar(255),
  SPONSOR                   varchar(255),
  PHASES                    varchar(255),
  FUNDED_BYS                varchar(255),
  STUDY_TYPES               varchar(255),
  STUDY_DESIGNS             varchar(255),
  STUDY_RESULTS             varchar(255),
  AGE_GROUPS                varchar(255),
  GENDER                    varchar(255),
  ENROLLMENT                varchar(255),
  OTHER_IDS                 varchar(255),
  ACRONYM                   varchar(255),
  START_DATE                timestamp,
  LAST_VERIFIED             timestamp,
  COMPLETION_DATE           timestamp,
  PRIMARY_COMPLETION_DATE   timestamp,
  FIRST_RECEIVED            timestamp,
  LAST_UPDATED              timestamp,
  OUTCOME_MEASURES          varchar(255),
  URL                       varchar(255),
  LOCATIONS                 varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL primary key (NCT_NUMBER))
;

create table SRSCID_CLINICAL_TRIAL_ALL_V (
  CLINICAL_NUMBER           varchar(255) not null,
  TITLE                     varchar(255),
  RECRUITMENT               varchar(255),
  CONDITIONS                varchar(255),
  INTERVENTION              varchar(255),
  SPONSOR                   varchar(255),
  PHASES                    varchar(255),
  FUNDED_BYS                varchar(255),
  STUDY_TYPES               varchar(255),
  STUDY_DESIGNS             varchar(255),
  STUDY_RESULTS             varchar(255),
  AGE_GROUPS                varchar(255),
  GENDER                    varchar(255),
  ENROLLMENT                varchar(255),
  OTHER_IDS                 varchar(255),
  ACRONYM                   varchar(255),
  START_DATE                timestamp,
  LAST_VERIFIED             timestamp,
  COMPLETION_DATE           timestamp,
  PRIMARY_COMPLETION_DATE   timestamp,
  FIRST_RECEIVED            timestamp,
  LAST_UPDATED              timestamp,
  OUTCOME_MEASURES          varchar(255),
  URL                       varchar(255),
  LOCATIONS                 varchar(255),
  FROM_SOURCE               varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_ALL_V primary key (CLINICAL_NUMBER))
;

create table SRSCID_CLINICAL_APPLICATION (
  ID                        integer not null,
  NCT_NUMBER                varchar(255) not null,
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  CENTER                    varchar(255),
  DECISION                  varchar(255),
  DECISION_DATE             timestamp,
  constraint pk_SRSCID_CLINICAL_APPLICATION primary key (ID))
;

create table SRSCID_CLINICAL_TRIAL_BASE (
  dtype                     varchar(10) not null,
  CLINICAL_ID               varchar(255) not null,
  title                     varchar(255),
  sponsor_name              varchar(255),
  url                       varchar(255),
  country                   varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_BASE primary key (CLINICAL_ID))
;

create table SRSCID_CLINICAL_CONDITION (
  id                        integer not null,
  NCT_NUMBER                varchar(255) not null,
  CONDITIONS                varchar(255),
  constraint pk_SRSCID_CLINICAL_CONDITION primary key (id))
;

create table SRSCID_CLINICAL_TRIAL_DRUG (
  CLINICAL_TRIAL_DRUG_ID    integer not null,
  NCT_NUMBER                varchar(255) not null,
  BDNUM                     varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_DRUG primary key (CLINICAL_TRIAL_DRUG_ID))
;

create table SRSCID_CLINICAL_DRUG_ALL_V (
  ID                        varchar(255) not null,
  CLINICAL_NUMBER           varchar(255) not null,
  BDNUM                     varchar(255),
  constraint pk_SRSCID_CLINICAL_DRUG_ALL_V primary key (ID))
;

create table SRSCID_CLINICAL_TRIAL_EU (
  EUDRACT_NUMBER            varchar(255) not null,
  TITLE                     varchar(255),
  SPONSOR_NAME              varchar(255),
  TRIAL_STATUS              varchar(255),
  DATE_FIRST_ENTERED_DB     timestamp,
  TRIAL_RESULTS             varchar(255),
  NATIONAL_COMPETENT_AUTH   varchar(255),
  COMPETENT_AUTH_DECISION   varchar(255),
  DATE_COMP_AUTH_DECISION   timestamp,
  ETHICS_COM_OPINION_APP    varchar(255),
  ETHICS_COM_OPINION_REASON varchar(255),
  DATE_ETHICS_COM_OPINION   timestamp,
  COUNTRY                   varchar(255),
  URL                       varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_EU primary key (EUDRACT_NUMBER))
;

create table SRSCID_CLINICAL_TRIAL_EU_DRUG (
  ID                        integer not null,
  PRODUCT_ID                integer not null,
  BDNUM                     varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_EU_DRUG primary key (ID))
;

create table SRSCID_CLINICAL_TRIAL_EU_MEDD (
  ID                        integer not null,
  EUDRACT_NUMBER            varchar(255) not null,
  MEDDRA_VERSION            varchar(255),
  MEDDRA_CLASS_CODE         varchar(255),
  MEDDRA_TERM               varchar(255),
  MEDDRA_SYSTEM_ORGAN_CLASS varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_EU_MEDD primary key (ID))
;

create table SRSCID_CLINICAL_TRIAL_EU_MC (
  ID                        integer not null,
  EUDRACT_NUMBER            varchar(255) not null,
  MEDICAL_COND_INVSTGED     varchar(255),
  MEDICAL_COND_INVSTGED_EZ  varchar(255),
  MEDICAL_COND_THERAP_AREA  varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_EU_MC primary key (ID))
;

create table SRSCID_CLINICAL_TRIAL_EU_PROD (
  ID                        integer not null,
  EUDRACT_NUMBER            varchar(255),
  IMP_SECTION               varchar(255),
  PRODUCT_NAME              varchar(255),
  TRADE_NAME                varchar(255),
  IMP_ROLE                  varchar(255),
  IMP_ROUTES_ADMIN          varchar(255),
  PHARMACEUTICAL_FORM       varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_EU_PROD primary key (ID))
;

create table SRSCID_CLINICAL_TRIAL_EU_TERM (
  ID                        integer not null,
  EUDRACT_NUMBER            varchar(255),
  PRODUCT_ID                integer,
  KEY                       varchar(255),
  VALUE                     varchar(255),
  constraint pk_SRSCID_CLINICAL_TRIAL_EU_TERM primary key (ID))
;

create table SRSCID_CODE (
  CODE_ID                   varchar(255),
  CODE                      varchar(255),
  CODE_SYSTEM               varchar(255),
  STRUCTURE_ID              varchar(255),
  UNII                      varchar(255),
  SUBSTANCE_ID              varchar(255))
;

create table SRSCID_INDICATION (
  ID                        integer not null,
  created_by                varchar(255),
  create_date               timestamp,
  modified_by               varchar(255),
  modify_date               timestamp,
  ACTIVE_MOIETY_BDNUM       varchar(255),
  ACTIVE_MOIETY_SUB_ID      varchar(255),
  ACTIVE_SUBSTANCE_BDNUM    varchar(255),
  ACTIVE_SUBSTANCE_SUB_ID   varchar(255),
  INDICATION_ROLE           varchar(255),
  INDICATION_TYPE           varchar(255),
  DISEASE_PRESENTATION      varchar(255),
  DISEASE_PRESENTATION_STAGE varchar(255),
  constraint pk_SRSCID_INDICATION primary key (ID))
;

create table SRSCID_IND_ANATOMICAL_FEATURE (
  ID                        integer not null,
  ANATOMICAL_FEATURE        varchar(255),
  ANATOMICAL_LOCATION       varchar(255),
  CELL_TYPE                 varchar(255),
  ANATOMICAL_SOURCE         varchar(255),
  INDICATION_ID             integer,
  constraint pk_SRSCID_IND_ANATOMICAL_FEATURE primary key (ID))
;

create table SRSCID_IND_CAUSE_OF_AGENT (
  ID                        integer not null,
  CAUSE_OF_AGENT            varchar(255),
  CAUSE_OF_AGENT_TYPE       varchar(255),
  INDICATION_ID             integer,
  constraint pk_SRSCID_IND_CAUSE_OF_AGENT primary key (ID))
;

create table SRSCID_IND_DISEASE (
  ID                        integer not null,
  DISEASE_TYPE              varchar(255),
  DISEASE_TYPE_SOURCE       varchar(255),
  DISEASE_TYPE_SOURCE_ID    varchar(255),
  INDICATION_ID             integer,
  constraint pk_SRSCID_IND_DISEASE primary key (ID))
;

create table SRSCID_IND_GENETIC (
  ID                        integer not null,
  GENE_ID                   varchar(255),
  IMMUTATION                varchar(255),
  PROTEIN                   varchar(255),
  PROTEIN_IMMUTATION        varchar(255),
  GENE_LOCI                 varchar(255),
  INDICATION_ID             integer,
  constraint pk_SRSCID_IND_GENETIC primary key (ID))
;

create table SRSCID_IND_INTERVENTION (
  ID                        integer not null,
  INTERVENTION_SOURCE_TYPE  varchar(255),
  INTERVENTION_SOURCE_ID    varchar(255),
  INTERVENTION_TYPE         varchar(255),
  INTERVENTION_SYSTEM       varchar(255),
  INTERVENTION_SYSTEM_CODE  varchar(255),
  INDICATION                varchar(255),
  INDICATION_ID             integer,
  constraint pk_SRSCID_IND_INTERVENTION primary key (ID))
;

create table SRSCID_IND_SOURCE (
  ID                        integer not null,
  SOURCE_TYPE               varchar(255),
  SOURCE_ID                 varchar(255),
  SOURCE_STATUS             varchar(255),
  URL                       varchar(255),
  TREATMENT                 varchar(255),
  PREVENTION                varchar(255),
  DIAGNOSTIC                varchar(255),
  INDICATION_ID             integer,
  constraint pk_SRSCID_IND_SOURCE primary key (ID))
;

create table SRSCID_IND_VERBATIM (
  ID                        integer not null,
  REFERENCE_SOURCE          varchar(255),
  STATUS                    varchar(255),
  VERBATIM_TEXT             varchar(255),
  INDICATION_ID             integer,
  constraint pk_SRSCID_IND_VERBATIM primary key (ID))
;

create table SRSCID_INAME (
  INAME_ID                  bigint not null,
  BDNUM                     varchar(255),
  INAME                     varchar(4000),
  TYPE                      varchar(255),
  PUBLIC_DOMAIN             varchar(255),
  DEPRECATED                varchar(255),
  LISTING_NAME              varchar(255),
  INGRED_ID                 bigint,
  constraint pk_SRSCID_INAME primary key (INAME_ID))
;

create table SRSCID_INGRED (
  INGRED_ID                 bigint not null,
  BDNUM                     varchar(255),
  CAS_NO                    varchar(255),
  UNII                      varchar(255),
  INGRED_PUBLIC_DOMAIN      varchar(255),
  SUBSTANCE_ID              varchar(255),
  constraint pk_SRSCID_INGRED primary key (INGRED_ID))
;

create table SRSCID_SUBSTANCE (
  SUBSTANCE_ID              varchar(255) not null,
  PRIORITY_BDNUM            varchar(255),
  SUBSTANCE_SOURCE          varchar(255),
  UNII                      varchar(255),
  status                    varchar(255),
  PUBLIC_DOMAIN             varchar(255),
  constraint pk_SRSCID_SUBSTANCE primary key (SUBSTANCE_ID))
;

create table SRSCID_SUBSTANCE_RELATIONSHIP (
  SUBSTANCE_RELATIONSHIP_ID bigint not null,
  PARENT_BDNUM              varchar(255),
  RELATED_BDNUM             varchar(255),
  PARENT_UNII               varchar(255),
  RELATED_UNII              varchar(255),
  PARENT_SUBSTANCE_ID       varchar(255),
  RELATED_SUBSTANCE_ID      varchar(255),
  RELATIONSHIP_TYPE_ID      varchar(255),
  PUBLIC_DOMAIN             varchar(255),
  constraint pk_SRSCID_SUBSTANCE_RELATIONSHIP primary key (SUBSTANCE_RELATIONSHIP_ID))
;

create table SRSCID_LOOKUP (
  LOOKUP_ID                 integer not null,
  LOOKUP_NAME               varchar(255),
  LOOKUP_VALUE              varchar(255),
  LOOKUP_SECOND_VALUE       varchar(255),
  constraint pk_SRSCID_LOOKUP primary key (LOOKUP_ID))
;

create table SRSCID_PRODUCT (
  PRODUCT_ID                integer not null,
  SOURCE                    varchar(255),
  PUBLIC_DOMAIN             varchar(255),
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  PRODUCT_TYPE              varchar(255),
  SOURCE_TYPE               varchar(255),
  UNIT_PRESENTATION         varchar(255),
  ROUTE_OF_ADMINISTRATION   varchar(255),
  STATUS                    varchar(255),
  NONPROPRIETARY_NAME       varchar(255),
  PROPRIETARY_NAME          varchar(255),
  PHARMACEDICAL_DOSAGE_FORM varchar(255),
  COMPOSE_PRODUCT_NAME      varchar(255),
  RELEASE_CHARACTERISTIC    varchar(255),
  STRENGTH_CHARACTERISTIC   varchar(255),
  COUNTRY_CODE              varchar(255),
  LANGUAGE                  varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  constraint pk_SRSCID_PRODUCT primary key (PRODUCT_ID))
;

create table ELIST_PROD_ACTIVE_INGRED_MV (
  PRODUCTID                 varchar(255),
  SUBSTANCEUNII             varchar(255),
  SUBSTANCENAME             varchar(255),
  ACTIVEMOIETY_1_NAME       varchar(255),
  STRENGTHNUMBER            varchar(255),
  STRENGTHNUMERATORUNIT     varchar(255))
;

create table SRSCID_PRODUCT_ALL_V (
  ID                        varchar(255) not null,
  SUBSTANCE_ID              varchar(255) not null,
  BDNUM                     varchar(255),
  PRODUCTID                 varchar(255),
  PRODUCTNDC                varchar(255),
  PRODUCTNAME               varchar(255),
  NONPROPRIETARYNAME        varchar(255),
  LABELERNAME               varchar(255),
  PRODUCTTYPENAME           varchar(255),
  MARKETINGCATEGORYNAME     varchar(255),
  DOSAGEFORMNAME            varchar(255),
  APPLICATIONNUMBER         varchar(255),
  INGREDIENTTYPE            varchar(255),
  ACTIVEMOIETY_1_UNII       varchar(255),
  ACTIVEMOIETY_1_NAME       varchar(255),
  FROMTABLE                 varchar(255),
  NAME                      varchar(255),
  UNII                      varchar(255),
  PRODACTIVECOUNT           varchar(255),
  PRODINACTIVECOUNT         varchar(255),
  constraint pk_SRSCID_PRODUCT_ALL_V primary key (ID))
;

create table ELIST_PROD_CHARACT_MV (
  PRODUCTID                 varchar(255) not null,
  FLAVORNAME                varchar(255),
  COLORNAME                 varchar(255),
  SHAPENAME                 varchar(255),
  NUMBEROFFRAGMENTS         varchar(255),
  SIZE_MM                   varchar(255),
  IMPRINTTEXT               varchar(255),
  constraint pk_ELIST_PROD_CHARACT_MV primary key (PRODUCTID))
;

create table SRSCID_PRODUCT_CODE (
  PRODUCT_CODE_ID           integer not null,
  PRODUCT_CODE              varchar(255),
  PRODUCT_CODE_TYPE         varchar(255),
  COUNTRY_CODE              varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_ID                integer,
  constraint pk_SRSCID_PRODUCT_CODE primary key (PRODUCT_CODE_ID))
;

create table SRSCID_PRODUCT_COMPANY (
  PRODUCT_COMPANY_ID        integer not null,
  COMPANY_NAME              varchar(255),
  COMPANY_ADDRESS           varchar(255),
  company_role              varchar(255),
  COMPANY_CODE              varchar(255),
  COMPANY_CODE_TYPE         varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_ID                integer,
  constraint pk_SRSCID_PRODUCT_COMPANY primary key (PRODUCT_COMPANY_ID))
;

create table SRSCID_PRODUCT_COMPONENT (
  PRODUCT_COMPONENT_ID      integer not null,
  CHAR_SIZE                 varchar(255),
  CHAR_IMPRINTTEXT          varchar(255),
  CHAR_COLOR                varchar(255),
  CHAR_FLAVOR               varchar(255),
  CHAR_SHAPE                varchar(255),
  CHAR_NUM_FRAGMENTS        varchar(255),
  DOSAGE_FORM               varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_ID                integer,
  constraint pk_SRSCID_PRODUCT_COMPONENT primary key (PRODUCT_COMPONENT_ID))
;

create table SRSCID_PRODUCT_EFFECTED (
  ID                        integer not null,
  EFFECTED_PRODUCT          varchar(255),
  FARM_PRODUCT_ID           integer,
  SUBSTANCE_ID              integer,
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  APPLICATION_ID            integer,
  constraint pk_SRSCID_PRODUCT_EFFECTED primary key (ID))
;

create table ELIST_PRODUCT_MV (
  PRODUCTID                 varchar(255),
  PRODUCTNDC                varchar(255),
  PROPRIETARYNAME           varchar(255),
  NONPROPRIETARYNAME        varchar(255),
  LABELERNAME               varchar(255),
  APPLICATIONNUMBER         varchar(255),
  MARKETINGCATEGORYCODE     varchar(255),
  MARKETINGCATEGORYNAME     varchar(255),
  DOSAGEFORMNAME            varchar(255),
  PRODUCTTYPENAME           varchar(255),
  PROPRIETARYNAMESUFFIX     varchar(255),
  STARTMARKETINGDATE        varchar(255),
  ENDMARKETINGDATE          varchar(255),
  IS_LISTED                 varchar(255),
  DOCUMENTID                varchar(255))
;

create table ELIST_PROD_INACTIVE_INGRED_MV (
  PRODUCTID                 varchar(255),
  SUBSTANCEUNII             varchar(255),
  SUBSTANCENAME             varchar(255),
  STRENGTHNUMBER            varchar(255),
  STRENGTHNUMERATORUNIT     varchar(255))
;

create table SRSCID_PRODUCT_INGREDIENT (
  PRODUCT_INGRED_ID         integer not null,
  APPLICANT_INGRED_NAME     varchar(255),
  BDNUM                     varchar(255),
  BASIS_OF_STRENGTH         varchar(255),
  AVERAGE                   double,
  LOW                       double,
  HIGH                      double,
  MANUFACTURER              varchar(255),
  LOT_NO                    varchar(255),
  INGREDIENT_TYPE           varchar(255),
  UNIT                      varchar(255),
  RELEASE_CHARACTERISTIC    varchar(255),
  NOTES                     varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_LOT_ID            integer,
  constraint pk_SRSCID_PRODUCT_INGREDIENT primary key (PRODUCT_INGRED_ID))
;

create table SRSCID_PRODUCT_LOT (
  PRODUCT_LOT_ID            integer not null,
  LOT_NO                    varchar(255),
  LOT_SIZE                  varchar(255),
  EXPIRY_DATE               timestamp,
  MANUFACTURE_DATE          timestamp,
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_COMPONENT_ID      integer,
  constraint pk_SRSCID_PRODUCT_LOT primary key (PRODUCT_LOT_ID))
;

create table SRSCID_PRODUCT_NAME (
  PRODUCT_NAME_ID           integer not null,
  PRODUCT_NAME              varchar(255),
  PRODUCT_NAME_TYPE         varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_ID                integer,
  constraint pk_SRSCID_PRODUCT_NAME primary key (PRODUCT_NAME_ID))
;

create table ELIST_PROD_ADMIN_ROUTE_MV (
  PRODUCTID                 varchar(255),
  ROUTE_CODE                varchar(255),
  ROUTENAME                 varchar(255))
;

create table SRSCID_PRODUCT_SRS (
  PRODUCT_ID                integer not null,
  APP_TYPE                  varchar(255),
  APP_NUMBER                varchar(255),
  PRODUCT_NAME              varchar(255),
  AMOUNT                    double,
  DOSAGE_FORM               varchar(255),
  ROUTE_OF_ADMINISTRATION   varchar(255),
  UNIT_PRESENTATION         varchar(255),
  UNIT                      varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  APPLICATION_ID            integer,
  constraint pk_SRSCID_PRODUCT_SRS primary key (PRODUCT_ID))
;

create table SRSCID_PRODUCT_TECH_EFFECT (
  ID                        integer not null,
  TECHNICAL_EFFECT          varchar(255),
  FARM_TECH_EFFECT_ID       integer,
  SUBSTANCE_ID              integer,
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  APPLICATION_ID            integer,
  constraint pk_SRSCID_PRODUCT_TECH_EFFECT primary key (ID))
;

create table SRSCID_PRODUCT_TERM_PART (
  PRODUCT_TERM_ID           integer not null,
  PRODUCT_TERM              varchar(255),
  PRODUCT_TERM_PART         varchar(255),
  CREATED_BY                varchar(255),
  CREATE_DATE               timestamp,
  MODIFIED_BY               varchar(255),
  MODIFY_DATE               timestamp,
  PRODUCT_NAME_ID           integer,
  constraint pk_SRSCID_PRODUCT_TERM_PART primary key (PRODUCT_TERM_ID))
;

create table SRSCID_SPECIFIED_SUBSTANCE (
  UUID                      varchar(40) not null,
  CURRENT_VERSION           bigint,
  VERSION                   varchar(255),
  created                   timestamp,
  CREATED_BY_ID             bigint,
  last_edited               timestamp,
  LAST_EDITED_BY_ID         bigint,
  DEPRECATED                boolean,
  RECORD_ACCESS             varchar(255),
  OWNER_UUID                varchar(40),
  DTYPE                     varchar(255),
  STATUS                    varchar(255),
  DEFINITION_TYPE           integer,
  DEFINITION_LEVEL          integer,
  APPROVAL_ID               varchar(255),
  APPROVED_BY_ID            bigint,
  APPROVED                  timestamp,
  SUBSTANCE_DETAILS         clob,
  internal_version          bigint not null,
  constraint pk_SRSCID_SPECIFIED_SUBSTANCE primary key (UUID))
;

create table SRSCID_SPECIFIED_SUB_CODE (
  UUID                      varchar(40) not null,
  CURRENT_VERSION           bigint,
  created                   timestamp,
  CREATED_BY_ID             bigint,
  last_edited               timestamp,
  LAST_EDITED_BY_ID         bigint,
  code_system               varchar(255),
  code                      varchar(255) not null,
  comments                  clob,
  code_text                 clob,
  type                      varchar(255),
  url                       clob,
  INTERNAL_REFERENCES       clob,
  OWNER_UUID                varchar(40),
  internal_version          bigint not null,
  constraint pk_SRSCID_SPECIFIED_SUB_CODE primary key (UUID))
;

create table SRSCID_SPECIFIED_SUB_NAME (
  UUID                      varchar(40) not null,
  CURRENT_VERSION           bigint,
  created                   timestamp,
  CREATED_BY_ID             bigint,
  last_edited               timestamp,
  LAST_EDITED_BY_ID         bigint,
  name                      varchar(255) not null,
  full_name                 clob,
  std_name                  clob,
  type                      varchar(32),
  LANGUAGES                 clob,
  preferred                 boolean,
  display_name              boolean,
  INTERNAL_REFERENCES       clob,
  OWNER_UUID                varchar(40),
  internal_version          bigint not null,
  constraint pk_SRSCID_SPECIFIED_SUB_NAME primary key (UUID))
;

create table SRSCID_SPECIFIED_SUB_REFERENCE (
  UUID                      varchar(40) not null,
  CURRENT_VERSION           bigint,
  created                   timestamp,
  CREATED_BY_ID             bigint,
  last_edited               timestamp,
  LAST_EDITED_BY_ID         bigint,
  CITATION                  clob,
  DOC_TYPE                  varchar(255),
  DOCUMENT_DATE             timestamp,
  PUBLIC_DOMAIN             boolean,
  TAGS                      clob,
  URL                       varchar(255),
  ID                        varchar(255),
  OWNER_UUID                varchar(40),
  internal_version          bigint not null,
  constraint pk_SRSCID_SPECIFIED_SUB_REFERENC primary key (UUID))
;

create table SRSCID_SUBSTANCE_ALL_V (
  SUBSTANCE_ID              varchar(255) not null,
  BDNUM                     varchar(255),
  UNII                      varchar(255),
  NAME                      varchar(255),
  STATUS                    varchar(255),
  SUBSTANCE_TYPE            varchar(255),
  CAS_NO                    varchar(255),
  APPCOUNT                  integer,
  APPSRSCOUNT               integer,
  PRODACTIVECOUNT           integer,
  PRODINACTIVECOUNT         integer,
  CLINICALCOUNT             integer,
  CASECOUNT                 integer,
  constraint pk_SRSCID_SUBSTANCE_ALL_V primary key (SUBSTANCE_ID))
;

create table SRSCID_FARM_SUBSTANCE (
  SGS_FARM_SUBSTANCE_ID     integer not null,
  SGS_VERSION               integer,
  SGS_FARM_SUBSTANCE_NM     varchar(255),
  SGS_FARM_CASNO            varchar(255),
  SGS_GSRS_SUBSTANCE_NM     varchar(255),
  SGS_GSRS_BDNUM            varchar(255),
  SGS_GSRS_CAS_NO           varchar(255),
  SGS_GSRS_UNICODE          varchar(255),
  SGS_GSRS_PROCESS_DT       timestamp,
  SGS_FARM_PROCESS_DT       timestamp,
  GSRS_BDNUM_EXISTS         varchar(255),
  FARM_SUBSTANCE_EXISTS     varchar(255),
  GSRS_BDNUM_EXISTS_SUBSTANCE_ID varchar(255),
  FARM_SUB_EXISTS_SUBSTANCE_ID varchar(255),
  constraint pk_SRSCID_FARM_SUBSTANCE primary key (SGS_FARM_SUBSTANCE_ID))
;

create table SRSCID_SEARCH_COUNT_MV (
  UUID                      varchar(255) not null,
  UNII                      varchar(255),
  CODE                      varchar(255),
  APPCOUNT                  integer,
  APPSRSCOUNT               integer,
  PRODACTIVECOUNT           integer,
  PRODINACTIVECOUNT         integer,
  CLINICALCOUNT             integer,
  CASECOUNT                 integer,
  constraint pk_SRSCID_SEARCH_COUNT_MV primary key (UUID))
;

create table SRSCID_USER_LOG (
  ID                        integer not null,
  LOG                       varchar(255),
  SEARCH_VALUE              varchar(255),
  CATEGORY                  varchar(255),
  CREATED_BY                varchar(255),
  CREATED_BY_ID             bigint,
  CREATE_DATE               timestamp,
  constraint pk_SRSCID_USER_LOG primary key (ID))
;

create sequence SRSCID_ADVERSE_EVT_CVM_ALL_V_seq;

create sequence SRSCID_ADVERSE_EVT_DME_ALL_V_seq;

create sequence SRSCID_ADVERSE_EVENT_PT_ALL_V_seq;

create sequence SRSCID_APP_INDICATION_ALL_V_seq;

create sequence SRSCID_SQ_APPLICATION_TYPE_ID;

create sequence SRSCID_APPLICATION_ALL_V_seq;

create sequence SRSCID_SQ_APP_INDICATION_ID;

create sequence SRSCID_SQ_APPLICATION_ID;

create sequence SRSCID_BDNUM_NAME_seq;

create sequence SRSCID_BDNUM_NAME_ALL_seq;

create sequence SRSCID_SQ_BIOMARKER_ID;

create sequence SRSCID_SQ_BIOMARKER_SOURCE_ID;

create sequence SRSCID_BIOMARKER_SOURCE_TEST_seq;

create sequence SRSCID_SQ_BIOMARKER_TEST_ID;

create sequence SRSCID_SQ_BIOMARKER_TESTDET_ID;

create sequence SRSCID_CLINICAL_TRIAL_seq;

create sequence SRSCID_CLINICAL_TRIAL_ALL_V_seq;

create sequence SRSCID_CLINICAL_APPLICATION_seq;

create sequence SRSCID_CLINICAL_TRIAL_BASE_seq;

create sequence SRSCID_CLINICAL_CONDITION_seq;

create sequence SRSCID_SQ_CLINICAL_DRUG_ID;

create sequence SRSCID_CLINICAL_DRUG_ALL_V_seq;

create sequence SRSCID_CLINICAL_TRIAL_EU_seq;

create sequence SRSCID_CLINICAL_TRIAL_EU_DRUG_seq;

create sequence SRSCID_CLINICAL_TRIAL_EU_MEDD_seq;

create sequence SRSCID_CLINICAL_TRIAL_EU_MC_seq;

create sequence SRSCID_CLINICAL_TRIAL_EU_PROD_seq;

create sequence SRSCID_CLINICAL_TRIAL_EU_TERM_seq;

create sequence SRSCID_SQ_INDICATION_ID;

create sequence SRSCID_SQ_IND_ANATOMICAL_ID;

create sequence SRSCID_SQ_IND_CAUSEAGENT_ID;

create sequence SRSCID_SQ_IND_DISEASE_ID;

create sequence SRSCID_SQ_IND_GENETIC_ID;

create sequence SRSCID_SQ_IND_INTERVENTION_ID;

create sequence SRSCID_SQ_IND_SOURCE_ID;

create sequence SRSCID_SQ_IND_VERBATIM_ID;

create sequence SRSCID_SQ_INAME_ID;

create sequence SRSCID_SQ_INGRED_ID;

create sequence SRSCID_SUBSTANCE_seq;

create sequence SRSCID_SQ_SUBST_REL_ID;

create sequence SRSCID_LOOKUP_seq;

create sequence SRSCID_SQ_PRODUCT_TWO_ID;

create sequence SRSCID_PRODUCT_ALL_V_seq;

create sequence ELIST_PROD_CHARACT_MV_seq;

create sequence SRSCID_SQ_PRODUCT_CODE_ID;

create sequence SRSCID_SQ_PRODUCT_COMPANY_ID;

create sequence SRSCID_SQ_PRODUCT_COMPONENT_ID;

create sequence SRSCID_SQ_PRODUCT_EFFECTED_ID;

create sequence SRSCID_SQ_PRODUCT_INGRED_ID;

create sequence SRSCID_SQ_PRODUCT_LOT_ID;

create sequence SRSCID_SQ_PRODUCT_NAME_ID;

create sequence SRSCID_SQ_PRODUCT_ID;

create sequence SRSCID_SQ_PROD_TECH_EFFECT_ID;

create sequence SRSCID_SQ_PRODUCT_TERM_ID;

create sequence SRSCID_SUBSTANCE_ALL_V_seq;

create sequence SRSCID_FARM_SUBSTANCE_seq;

create sequence SRSCID_SEARCH_COUNT_MV_seq;

create sequence SRSCID_SQ_USER_LOG_ID;

alter table SRSCID_APP_INDICATION_ALL_V add constraint fk_SRSCID_APP_INDICATION_ALL_V_1 foreign key (ID) references SRSCID_APPLICATION_ALL_V (ID) on delete restrict on update restrict;
create index ix_SRSCID_APP_INDICATION_ALL_V_1 on SRSCID_APP_INDICATION_ALL_V (ID);
alter table SRSCID_APPLICATION_TYPE_SRS add constraint fk_SRSCID_APPLICATION_TYPE_SRS_2 foreign key (PRODUCT_ID) references SRSCID_PRODUCT_SRS (PRODUCT_ID) on delete restrict on update restrict;
create index ix_SRSCID_APPLICATION_TYPE_SRS_2 on SRSCID_APPLICATION_TYPE_SRS (PRODUCT_ID);
alter table SRSCID_APPLICATION_ALL_V add constraint fk_SRSCID_APPLICATION_ALL_V_SR_3 foreign key (SUBSTANCE_ID) references SRSCID_SUBSTANCE_ALL_V (SUBSTANCE_ID) on delete restrict on update restrict;
create index ix_SRSCID_APPLICATION_ALL_V_SR_3 on SRSCID_APPLICATION_ALL_V (SUBSTANCE_ID);
alter table SRSCID_APP_INDICATION_SRS add constraint fk_SRSCID_APP_INDICATION_SRS_a_4 foreign key (APPLICATION_ID_FK) references SRSCID_APPLICATION_SRS (APPLICATION_ID) on delete restrict on update restrict;
create index ix_SRSCID_APP_INDICATION_SRS_a_4 on SRSCID_APP_INDICATION_SRS (APPLICATION_ID_FK);
alter table SRSCID_BDNUM_NAME_ALL add constraint fk_SRSCID_BDNUM_NAME_ALL_subAl_5 foreign key (SUBSTANCE_ID) references SRSCID_SUBSTANCE_ALL_V (SUBSTANCE_ID) on delete restrict on update restrict;
create index ix_SRSCID_BDNUM_NAME_ALL_subAl_5 on SRSCID_BDNUM_NAME_ALL (SUBSTANCE_ID);
alter table SRSCID_BIOMARKER_SOURCE add constraint fk_SRSCID_BIOMARKER_SOURCE_bio_6 foreign key (BIOMARKER_ID) references SRSCID_BIOMARKER (ID) on delete restrict on update restrict;
create index ix_SRSCID_BIOMARKER_SOURCE_bio_6 on SRSCID_BIOMARKER_SOURCE (BIOMARKER_ID);
alter table SRSCID_BIOMARKER_SOURCE_TEST add constraint fk_SRSCID_BIOMARKER_SOURCE_TES_7 foreign key (SOURCE_ID) references SRSCID_BIOMARKER_SOURCE (ID) on delete restrict on update restrict;
create index ix_SRSCID_BIOMARKER_SOURCE_TES_7 on SRSCID_BIOMARKER_SOURCE_TEST (SOURCE_ID);
alter table SRSCID_BIOMARKER_SOURCE_TEST add constraint fk_SRSCID_BIOMARKER_SOURCE_TES_8 foreign key (TEST_ID) references SRSCID_BIOMARKER_TEST (ID) on delete restrict on update restrict;
create index ix_SRSCID_BIOMARKER_SOURCE_TES_8 on SRSCID_BIOMARKER_SOURCE_TEST (TEST_ID);
alter table SRSCID_BIOMARKER_TEST add constraint fk_SRSCID_BIOMARKER_TEST_bioma_9 foreign key (BIOMARKER_ID) references SRSCID_BIOMARKER (ID) on delete restrict on update restrict;
create index ix_SRSCID_BIOMARKER_TEST_bioma_9 on SRSCID_BIOMARKER_TEST (BIOMARKER_ID);
alter table SRSCID_BIOMARKER_TESTDETAIL add constraint fk_SRSCID_BIOMARKER_TESTDETAI_10 foreign key (BIOMARKER_TEST_ID) references SRSCID_BIOMARKER_TEST (ID) on delete restrict on update restrict;
create index ix_SRSCID_BIOMARKER_TESTDETAI_10 on SRSCID_BIOMARKER_TESTDETAIL (BIOMARKER_TEST_ID);
alter table SRSCID_CLINICAL_APPLICATION add constraint fk_SRSCID_CLINICAL_APPLICATIO_11 foreign key (NCT_NUMBER) references SRSCID_CLINICAL_TRIAL_ALL_V (CLINICAL_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_APPLICATIO_11 on SRSCID_CLINICAL_APPLICATION (NCT_NUMBER);
alter table SRSCID_CLINICAL_APPLICATION add constraint fk_SRSCID_CLINICAL_APPLICATIO_12 foreign key (NCT_NUMBER) references SRSCID_CLINICAL_TRIAL (NCT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_APPLICATIO_12 on SRSCID_CLINICAL_APPLICATION (NCT_NUMBER);
alter table SRSCID_CLINICAL_TRIAL_BASE add constraint fk_SRSCID_CLINICAL_TRIAL_BASE_13 foreign key (CLINICAL_ID) references SRSCID_CLINICAL_TRIAL_EU (EUDRACT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_BASE_13 on SRSCID_CLINICAL_TRIAL_BASE (CLINICAL_ID);
alter table SRSCID_CLINICAL_TRIAL_BASE add constraint fk_SRSCID_CLINICAL_TRIAL_BASE_14 foreign key (CLINICAL_ID) references SRSCID_CLINICAL_TRIAL (NCT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_BASE_14 on SRSCID_CLINICAL_TRIAL_BASE (CLINICAL_ID);
alter table SRSCID_CLINICAL_CONDITION add constraint fk_SRSCID_CLINICAL_CONDITION__15 foreign key (NCT_NUMBER) references SRSCID_CLINICAL_TRIAL_ALL_V (CLINICAL_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_CONDITION__15 on SRSCID_CLINICAL_CONDITION (NCT_NUMBER);
alter table SRSCID_CLINICAL_TRIAL_DRUG add constraint fk_SRSCID_CLINICAL_TRIAL_DRUG_16 foreign key (NCT_NUMBER) references SRSCID_CLINICAL_TRIAL (NCT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_DRUG_16 on SRSCID_CLINICAL_TRIAL_DRUG (NCT_NUMBER);
alter table SRSCID_CLINICAL_TRIAL_DRUG add constraint fk_SRSCID_CLINICAL_TRIAL_DRUG_17 foreign key (BDNUM) references SRSCID_BDNUM_NAME (BDNUM) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_DRUG_17 on SRSCID_CLINICAL_TRIAL_DRUG (BDNUM);
alter table SRSCID_CLINICAL_DRUG_ALL_V add constraint fk_SRSCID_CLINICAL_DRUG_ALL_V_18 foreign key (CLINICAL_NUMBER) references SRSCID_CLINICAL_TRIAL_ALL_V (CLINICAL_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_DRUG_ALL_V_18 on SRSCID_CLINICAL_DRUG_ALL_V (CLINICAL_NUMBER);
alter table SRSCID_CLINICAL_DRUG_ALL_V add constraint fk_SRSCID_CLINICAL_DRUG_ALL_V_19 foreign key (BDNUM) references SRSCID_BDNUM_NAME (BDNUM) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_DRUG_ALL_V_19 on SRSCID_CLINICAL_DRUG_ALL_V (BDNUM);
alter table SRSCID_CLINICAL_TRIAL_EU_DRUG add constraint fk_SRSCID_CLINICAL_TRIAL_EU_D_20 foreign key (PRODUCT_ID) references SRSCID_CLINICAL_TRIAL_EU_PROD (ID) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_EU_D_20 on SRSCID_CLINICAL_TRIAL_EU_DRUG (PRODUCT_ID);
alter table SRSCID_CLINICAL_TRIAL_EU_DRUG add constraint fk_SRSCID_CLINICAL_TRIAL_EU_D_21 foreign key (BDNUM) references SRSCID_BDNUM_NAME (BDNUM) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_EU_D_21 on SRSCID_CLINICAL_TRIAL_EU_DRUG (BDNUM);
alter table SRSCID_CLINICAL_TRIAL_EU_MEDD add constraint fk_SRSCID_CLINICAL_TRIAL_EU_M_22 foreign key (EUDRACT_NUMBER) references SRSCID_CLINICAL_TRIAL_EU (EUDRACT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_EU_M_22 on SRSCID_CLINICAL_TRIAL_EU_MEDD (EUDRACT_NUMBER);
alter table SRSCID_CLINICAL_TRIAL_EU_MC add constraint fk_SRSCID_CLINICAL_TRIAL_EU_M_23 foreign key (EUDRACT_NUMBER) references SRSCID_CLINICAL_TRIAL_EU (EUDRACT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_EU_M_23 on SRSCID_CLINICAL_TRIAL_EU_MC (EUDRACT_NUMBER);
alter table SRSCID_CLINICAL_TRIAL_EU_PROD add constraint fk_SRSCID_CLINICAL_TRIAL_EU_P_24 foreign key (EUDRACT_NUMBER) references SRSCID_CLINICAL_TRIAL_EU (EUDRACT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_EU_P_24 on SRSCID_CLINICAL_TRIAL_EU_PROD (EUDRACT_NUMBER);
alter table SRSCID_CLINICAL_TRIAL_EU_TERM add constraint fk_SRSCID_CLINICAL_TRIAL_EU_T_25 foreign key (EUDRACT_NUMBER) references SRSCID_CLINICAL_TRIAL_EU (EUDRACT_NUMBER) on delete restrict on update restrict;
create index ix_SRSCID_CLINICAL_TRIAL_EU_T_25 on SRSCID_CLINICAL_TRIAL_EU_TERM (EUDRACT_NUMBER);
alter table SRSCID_CODE add constraint fk_SRSCID_CODE_subAllCodeSrs_26 foreign key (SUBSTANCE_ID) references SRSCID_SUBSTANCE_ALL_V (SUBSTANCE_ID) on delete restrict on update restrict;
create index ix_SRSCID_CODE_subAllCodeSrs_26 on SRSCID_CODE (SUBSTANCE_ID);
alter table SRSCID_IND_ANATOMICAL_FEATURE add constraint fk_SRSCID_IND_ANATOMICAL_FEAT_27 foreign key (INDICATION_ID) references SRSCID_INDICATION (ID) on delete restrict on update restrict;
create index ix_SRSCID_IND_ANATOMICAL_FEAT_27 on SRSCID_IND_ANATOMICAL_FEATURE (INDICATION_ID);
alter table SRSCID_IND_CAUSE_OF_AGENT add constraint fk_SRSCID_IND_CAUSE_OF_AGENT__28 foreign key (INDICATION_ID) references SRSCID_INDICATION (ID) on delete restrict on update restrict;
create index ix_SRSCID_IND_CAUSE_OF_AGENT__28 on SRSCID_IND_CAUSE_OF_AGENT (INDICATION_ID);
alter table SRSCID_IND_DISEASE add constraint fk_SRSCID_IND_DISEASE_indFrom_29 foreign key (INDICATION_ID) references SRSCID_INDICATION (ID) on delete restrict on update restrict;
create index ix_SRSCID_IND_DISEASE_indFrom_29 on SRSCID_IND_DISEASE (INDICATION_ID);
alter table SRSCID_IND_GENETIC add constraint fk_SRSCID_IND_GENETIC_indFrom_30 foreign key (INDICATION_ID) references SRSCID_INDICATION (ID) on delete restrict on update restrict;
create index ix_SRSCID_IND_GENETIC_indFrom_30 on SRSCID_IND_GENETIC (INDICATION_ID);
alter table SRSCID_IND_INTERVENTION add constraint fk_SRSCID_IND_INTERVENTION_in_31 foreign key (INDICATION_ID) references SRSCID_INDICATION (ID) on delete restrict on update restrict;
create index ix_SRSCID_IND_INTERVENTION_in_31 on SRSCID_IND_INTERVENTION (INDICATION_ID);
alter table SRSCID_IND_SOURCE add constraint fk_SRSCID_IND_SOURCE_indFromS_32 foreign key (INDICATION_ID) references SRSCID_INDICATION (ID) on delete restrict on update restrict;
create index ix_SRSCID_IND_SOURCE_indFromS_32 on SRSCID_IND_SOURCE (INDICATION_ID);
alter table SRSCID_IND_VERBATIM add constraint fk_SRSCID_IND_VERBATIM_indFro_33 foreign key (INDICATION_ID) references SRSCID_INDICATION (ID) on delete restrict on update restrict;
create index ix_SRSCID_IND_VERBATIM_indFro_33 on SRSCID_IND_VERBATIM (INDICATION_ID);
alter table SRSCID_INAME add constraint fk_SRSCID_INAME_legacyIngred_34 foreign key (INGRED_ID) references SRSCID_INGRED (INGRED_ID) on delete restrict on update restrict;
create index ix_SRSCID_INAME_legacyIngred_34 on SRSCID_INAME (INGRED_ID);
alter table SRSCID_INGRED add constraint fk_SRSCID_INGRED_legacySubst_35 foreign key (SUBSTANCE_ID) references SRSCID_SUBSTANCE (SUBSTANCE_ID) on delete restrict on update restrict;
create index ix_SRSCID_INGRED_legacySubst_35 on SRSCID_INGRED (SUBSTANCE_ID);
alter table SRSCID_SUBSTANCE_RELATIONSHIP add constraint fk_SRSCID_SUBSTANCE_RELATIONS_36 foreign key (PARENT_SUBSTANCE_ID) references SRSCID_SUBSTANCE (SUBSTANCE_ID) on delete restrict on update restrict;
create index ix_SRSCID_SUBSTANCE_RELATIONS_36 on SRSCID_SUBSTANCE_RELATIONSHIP (PARENT_SUBSTANCE_ID);
alter table SRSCID_PRODUCT_ALL_V add constraint fk_SRSCID_PRODUCT_ALL_V_SRSCI_37 foreign key (SUBSTANCE_ID) references SRSCID_SUBSTANCE_ALL_V (SUBSTANCE_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_ALL_V_SRSCI_37 on SRSCID_PRODUCT_ALL_V (SUBSTANCE_ID);
alter table SRSCID_PRODUCT_CODE add constraint fk_SRSCID_PRODUCT_CODE_produc_38 foreign key (PRODUCT_ID) references SRSCID_PRODUCT (PRODUCT_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_CODE_produc_38 on SRSCID_PRODUCT_CODE (PRODUCT_ID);
alter table SRSCID_PRODUCT_COMPANY add constraint fk_SRSCID_PRODUCT_COMPANY_pro_39 foreign key (PRODUCT_ID) references SRSCID_PRODUCT (PRODUCT_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_COMPANY_pro_39 on SRSCID_PRODUCT_COMPANY (PRODUCT_ID);
alter table SRSCID_PRODUCT_COMPONENT add constraint fk_SRSCID_PRODUCT_COMPONENT_p_40 foreign key (PRODUCT_ID) references SRSCID_PRODUCT (PRODUCT_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_COMPONENT_p_40 on SRSCID_PRODUCT_COMPONENT (PRODUCT_ID);
alter table SRSCID_PRODUCT_EFFECTED add constraint fk_SRSCID_PRODUCT_EFFECTED_ap_41 foreign key (APPLICATION_ID) references SRSCID_APPLICATION_SRS (APPLICATION_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_EFFECTED_ap_41 on SRSCID_PRODUCT_EFFECTED (APPLICATION_ID);
alter table ELIST_PRODUCT_MV add constraint fk_ELIST_PRODUCT_MV_prodCharE_42 foreign key (PRODUCTID) references ELIST_PROD_CHARACT_MV (PRODUCTID) on delete restrict on update restrict;
create index ix_ELIST_PRODUCT_MV_prodCharE_42 on ELIST_PRODUCT_MV (PRODUCTID);
alter table SRSCID_PRODUCT_INGREDIENT add constraint fk_SRSCID_PRODUCT_INGREDIENT__43 foreign key (PRODUCT_LOT_ID) references SRSCID_PRODUCT_LOT (PRODUCT_LOT_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_INGREDIENT__43 on SRSCID_PRODUCT_INGREDIENT (PRODUCT_LOT_ID);
alter table SRSCID_PRODUCT_LOT add constraint fk_SRSCID_PRODUCT_LOT_product_44 foreign key (PRODUCT_COMPONENT_ID) references SRSCID_PRODUCT_COMPONENT (PRODUCT_COMPONENT_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_LOT_product_44 on SRSCID_PRODUCT_LOT (PRODUCT_COMPONENT_ID);
alter table SRSCID_PRODUCT_NAME add constraint fk_SRSCID_PRODUCT_NAME_produc_45 foreign key (PRODUCT_ID) references SRSCID_PRODUCT (PRODUCT_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_NAME_produc_45 on SRSCID_PRODUCT_NAME (PRODUCT_ID);
alter table SRSCID_PRODUCT_SRS add constraint fk_SRSCID_PRODUCT_SRS_applica_46 foreign key (APPLICATION_ID) references SRSCID_APPLICATION_SRS (APPLICATION_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_SRS_applica_46 on SRSCID_PRODUCT_SRS (APPLICATION_ID);
alter table SRSCID_PRODUCT_TECH_EFFECT add constraint fk_SRSCID_PRODUCT_TECH_EFFECT_47 foreign key (APPLICATION_ID) references SRSCID_APPLICATION_SRS (APPLICATION_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_TECH_EFFECT_47 on SRSCID_PRODUCT_TECH_EFFECT (APPLICATION_ID);
alter table SRSCID_PRODUCT_TERM_PART add constraint fk_SRSCID_PRODUCT_TERM_PART_p_48 foreign key (PRODUCT_NAME_ID) references SRSCID_PRODUCT_NAME (PRODUCT_NAME_ID) on delete restrict on update restrict;
create index ix_SRSCID_PRODUCT_TERM_PART_p_48 on SRSCID_PRODUCT_TERM_PART (PRODUCT_NAME_ID);
alter table SRSCID_SPECIFIED_SUB_CODE add constraint fk_SRSCID_SPECIFIED_SUB_CODE__49 foreign key (OWNER_UUID) references SRSCID_SPECIFIED_SUBSTANCE (UUID) on delete restrict on update restrict;
create index ix_SRSCID_SPECIFIED_SUB_CODE__49 on SRSCID_SPECIFIED_SUB_CODE (OWNER_UUID);
alter table SRSCID_SPECIFIED_SUB_NAME add constraint fk_SRSCID_SPECIFIED_SUB_NAME__50 foreign key (OWNER_UUID) references SRSCID_SPECIFIED_SUBSTANCE (UUID) on delete restrict on update restrict;
create index ix_SRSCID_SPECIFIED_SUB_NAME__50 on SRSCID_SPECIFIED_SUB_NAME (OWNER_UUID);
alter table SRSCID_SPECIFIED_SUB_REFERENCE add constraint fk_SRSCID_SPECIFIED_SUB_REFER_51 foreign key (OWNER_UUID) references SRSCID_SPECIFIED_SUBSTANCE (UUID) on delete restrict on update restrict;
create index ix_SRSCID_SPECIFIED_SUB_REFER_51 on SRSCID_SPECIFIED_SUB_REFERENCE (OWNER_UUID);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists CVM_ADVERSE_EVENTS;

drop table if exists SRSCID_ADVERSE_EVT_CVM_ALL_V;

drop table if exists SRSCID_ADVERSE_EVENT_DME;

drop table if exists SRSCID_ADVERSE_EVT_DME_ALL_V;

drop table if exists FAERS_DBL_AE_CASE_DETAILS;

drop table if exists SRSCID_ADVERSE_EVENT_PT;

drop table if exists SRSCID_ADVERSE_EVENT_PT_ALL_V;

drop table if exists SRSCID_APP_INDICATION_ALL_V;

drop table if exists SRSCID_APPLICATION_TYPE_SRS;

drop table if exists SRSCID_APPLICATION_ALL_V;

drop table if exists SRSCID_APPLICATION_MV;

drop table if exists SRSCID_APPLICATION_TYPE_MV;

drop table if exists SRSCID_APP_INDICATION_MV;

drop table if exists SRSCID_APP_INDICATION_SRS;

drop table if exists SRSCID_APPLICATION_SRS;

drop table if exists SRSCID_BDNUM_NAME;

drop table if exists SRSCID_BDNUM_NAME_ALL;

drop table if exists SRSCID_BIOMARKER;

drop table if exists SRSCID_BIOMARKER_SOURCE;

drop table if exists SRSCID_BIOMARKER_SOURCE_TEST;

drop table if exists SRSCID_BIOMARKER_TEST;

drop table if exists SRSCID_BIOMARKER_TESTDETAIL;

drop table if exists SRSCID_CLINICAL_TRIAL;

drop table if exists SRSCID_CLINICAL_TRIAL_ALL_V;

drop table if exists SRSCID_CLINICAL_APPLICATION;

drop table if exists SRSCID_CLINICAL_TRIAL_BASE;

drop table if exists SRSCID_CLINICAL_CONDITION;

drop table if exists SRSCID_CLINICAL_TRIAL_DRUG;

drop table if exists SRSCID_CLINICAL_DRUG_ALL_V;

drop table if exists SRSCID_CLINICAL_TRIAL_EU;

drop table if exists SRSCID_CLINICAL_TRIAL_EU_DRUG;

drop table if exists SRSCID_CLINICAL_TRIAL_EU_MEDD;

drop table if exists SRSCID_CLINICAL_TRIAL_EU_MC;

drop table if exists SRSCID_CLINICAL_TRIAL_EU_PROD;

drop table if exists SRSCID_CLINICAL_TRIAL_EU_TERM;

drop table if exists SRSCID_CODE;

drop table if exists SRSCID_INDICATION;

drop table if exists SRSCID_IND_ANATOMICAL_FEATURE;

drop table if exists SRSCID_IND_CAUSE_OF_AGENT;

drop table if exists SRSCID_IND_DISEASE;

drop table if exists SRSCID_IND_GENETIC;

drop table if exists SRSCID_IND_INTERVENTION;

drop table if exists SRSCID_IND_SOURCE;

drop table if exists SRSCID_IND_VERBATIM;

drop table if exists SRSCID_INAME;

drop table if exists SRSCID_INGRED;

drop table if exists SRSCID_SUBSTANCE;

drop table if exists SRSCID_SUBSTANCE_RELATIONSHIP;

drop table if exists SRSCID_LOOKUP;

drop table if exists SRSCID_PRODUCT;

drop table if exists ELIST_PROD_ACTIVE_INGRED_MV;

drop table if exists SRSCID_PRODUCT_ALL_V;

drop table if exists ELIST_PROD_CHARACT_MV;

drop table if exists SRSCID_PRODUCT_CODE;

drop table if exists SRSCID_PRODUCT_COMPANY;

drop table if exists SRSCID_PRODUCT_COMPONENT;

drop table if exists SRSCID_PRODUCT_EFFECTED;

drop table if exists ELIST_PRODUCT_MV;

drop table if exists ELIST_PROD_INACTIVE_INGRED_MV;

drop table if exists SRSCID_PRODUCT_INGREDIENT;

drop table if exists SRSCID_PRODUCT_LOT;

drop table if exists SRSCID_PRODUCT_NAME;

drop table if exists ELIST_PROD_ADMIN_ROUTE_MV;

drop table if exists SRSCID_PRODUCT_SRS;

drop table if exists SRSCID_PRODUCT_TECH_EFFECT;

drop table if exists SRSCID_PRODUCT_TERM_PART;

drop table if exists SRSCID_SPECIFIED_SUBSTANCE;

drop table if exists SRSCID_SPECIFIED_SUB_CODE;

drop table if exists SRSCID_SPECIFIED_SUB_NAME;

drop table if exists SRSCID_SPECIFIED_SUB_REFERENCE;

drop table if exists SRSCID_SUBSTANCE_ALL_V;

drop table if exists SRSCID_FARM_SUBSTANCE;

drop table if exists SRSCID_SEARCH_COUNT_MV;

drop table if exists SRSCID_USER_LOG;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists SRSCID_ADVERSE_EVT_CVM_ALL_V_seq;

drop sequence if exists SRSCID_ADVERSE_EVT_DME_ALL_V_seq;

drop sequence if exists SRSCID_ADVERSE_EVENT_PT_ALL_V_seq;

drop sequence if exists SRSCID_APP_INDICATION_ALL_V_seq;

drop sequence if exists SRSCID_SQ_APPLICATION_TYPE_ID;

drop sequence if exists SRSCID_APPLICATION_ALL_V_seq;

drop sequence if exists SRSCID_SQ_APP_INDICATION_ID;

drop sequence if exists SRSCID_SQ_APPLICATION_ID;

drop sequence if exists SRSCID_BDNUM_NAME_seq;

drop sequence if exists SRSCID_BDNUM_NAME_ALL_seq;

drop sequence if exists SRSCID_SQ_BIOMARKER_ID;

drop sequence if exists SRSCID_SQ_BIOMARKER_SOURCE_ID;

drop sequence if exists SRSCID_BIOMARKER_SOURCE_TEST_seq;

drop sequence if exists SRSCID_SQ_BIOMARKER_TEST_ID;

drop sequence if exists SRSCID_SQ_BIOMARKER_TESTDET_ID;

drop sequence if exists SRSCID_CLINICAL_TRIAL_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_ALL_V_seq;

drop sequence if exists SRSCID_CLINICAL_APPLICATION_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_BASE_seq;

drop sequence if exists SRSCID_CLINICAL_CONDITION_seq;

drop sequence if exists SRSCID_SQ_CLINICAL_DRUG_ID;

drop sequence if exists SRSCID_CLINICAL_DRUG_ALL_V_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_EU_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_EU_DRUG_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_EU_MEDD_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_EU_MC_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_EU_PROD_seq;

drop sequence if exists SRSCID_CLINICAL_TRIAL_EU_TERM_seq;

drop sequence if exists SRSCID_SQ_INDICATION_ID;

drop sequence if exists SRSCID_SQ_IND_ANATOMICAL_ID;

drop sequence if exists SRSCID_SQ_IND_CAUSEAGENT_ID;

drop sequence if exists SRSCID_SQ_IND_DISEASE_ID;

drop sequence if exists SRSCID_SQ_IND_GENETIC_ID;

drop sequence if exists SRSCID_SQ_IND_INTERVENTION_ID;

drop sequence if exists SRSCID_SQ_IND_SOURCE_ID;

drop sequence if exists SRSCID_SQ_IND_VERBATIM_ID;

drop sequence if exists SRSCID_SQ_INAME_ID;

drop sequence if exists SRSCID_SQ_INGRED_ID;

drop sequence if exists SRSCID_SUBSTANCE_seq;

drop sequence if exists SRSCID_SQ_SUBST_REL_ID;

drop sequence if exists SRSCID_LOOKUP_seq;

drop sequence if exists SRSCID_SQ_PRODUCT_TWO_ID;

drop sequence if exists SRSCID_PRODUCT_ALL_V_seq;

drop sequence if exists ELIST_PROD_CHARACT_MV_seq;

drop sequence if exists SRSCID_SQ_PRODUCT_CODE_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_COMPANY_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_COMPONENT_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_EFFECTED_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_INGRED_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_LOT_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_NAME_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_ID;

drop sequence if exists SRSCID_SQ_PROD_TECH_EFFECT_ID;

drop sequence if exists SRSCID_SQ_PRODUCT_TERM_ID;

drop sequence if exists SRSCID_SUBSTANCE_ALL_V_seq;

drop sequence if exists SRSCID_FARM_SUBSTANCE_seq;

drop sequence if exists SRSCID_SEARCH_COUNT_MV_seq;

drop sequence if exists SRSCID_SQ_USER_LOG_ID;

