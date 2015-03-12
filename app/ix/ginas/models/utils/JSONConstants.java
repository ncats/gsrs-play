package ix.ginas.models.utils;

public class JSONConstants {
    public static final String CV_AMOUNT_UNIT = "CV.AMOUNT_UNIT";
    public static final String CV_AMOUNT_TYPE = "CV.AMOUNT_TYPE";
    public static final String CV_STEREOCHEMISTRY_TYPE = "CV.STEREOCHEMISTRY_TYPE";
    public static final String CV_OPTICAL_ACTIVITY = "CV.OPTICAL_ACTIVITY";
    public static final String CV_NAME_TYPE = "CV.NAME_TYPE";
    public static final String CV_NAME_DOMAIN = "CV.NAME_DOMAIN";
    public static final String CV_LANGUAGE = "CV.LANGUAGE";
    public static final String CV_JURISDICTION = "CV.JURISDICTION";
    public static final String CV_NAME_ORG = "CV.NAME_ORG";
    public static final String CV_DOCUMENT_TYPE = "CV.DOCUMENT_TYPE";
    public static final String CV_DOCUMENT_COLLECTION = "CV.DOCUMENT_COLLECTION";
    public static final String CV_INTERACTION_TYPE = "CV.INTERACTION_TYPE";
    public static final String CV_QUALIFICATION = "CV.QUALIFICAITON";
    public static final String CV_RELATIONSHIP_TYPE = "CV.RELATIONSHIP_TYPE";
    public static final String CV_CODE_SYSTEM = "CV.CODE_SYSTEM";
    public static final String CV_CODE_TYPE = "CV.CODE_TYPE";
    public static final String CV_PROTEIN_SUBTYPE = "CV.PROTEIN_SUBTYPE";
    public static final String CV_NUCLEIC_ACID_TYPE = "CV.NUCLEIC_ACID_TYPE";
    public static final String CV_NUCLEIC_ACID_SUBTYPE = "CV.NUCLEIC_ACID_SUBTYPE";
    
    public static final String[] ENUM_MIXTURE_TYPE = {
        "MUST_BE_PRESENT",
        "MAY_BE_PRESENT"
    };
    
    public static final String[] ENUM_REFERENCE = {
        "reference"
    };
    
    public static final String[] ENUM_BOOLEAN = {
        "Yes","No"
    };
    
    public static final String[] ENUM_EXTENT = {
        "COMPLETE","PARTIAL"
    };
    
    public static final String[] ENUM_STEREO = {
        "ACHIRAL",
        "ABSOLUTE",
        "RACEMIC",
        "EPIMERIC",
        "MIXED",
        "UNKNOWN"
    };
    
    public static final String[] ENUM_OPTICAL = {
        "( + )",
        "( - )",
        "( + / - )",
        "UNSPECIFIED",
        "UNKNOWN"
    };
    
    public static final String[] ENUM_SUBSTANCETYPES = {
        "chemical",
        "protein",
        "nucleicAcid",
        "mixture",
        "polymer",
        "structurallyDiverse",
        "specifiedSubstance",
        "specifiedSubstanceG2",
        "specifiedSubstanceG3",
        "specifiedSubstanceG4",
        "virtual"
    };
    
    public static final String[] ENUM_NAMETYPE = {
        "of",
        "sys",
        "bn",
        "cn",
        "cd"
    };
    
    public static final String[] ENUM_DOCUMENTTYPE = {
        "WIKI",
        "INN",
        "IND",
        "NDA",
        "USP",
        "USPNF",
        "BLA",
        "USANCOUN",
        "JA",
        "IT IS",
        "EP",
        "INN_LIST",
        "SRS",
        "OTHER"
    };
    
    public static final String [] ENUM_PROPERTY_TYPE = {
        "material",
        "amount"
    };
}
