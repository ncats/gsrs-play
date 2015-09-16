package ix.idg.controllers;

import ix.idg.models.Target;

public interface Commons {
    public static final String SOURCE = "Data Source";
    
    public static final String IDG_DISEASE = "IDG Disease";
    public static final String IDG_DEVELOPMENT = Target.IDG_DEVELOPMENT;
    public static final String IDG_FAMILY = Target.IDG_FAMILY;
    public static final String IDG_DRUG = "IDG Drug";
    public static final String IDG_ZSCORE = "IDG Z-score";
    public static final String IDG_CONF = "IDG Confidence";
    public static final String IDG_GENERIF = "IDG GeneRIF";
    public static final String IDG_TARGET = "IDG Target";
    public static final String IDG_SMILES = "IDG SMILES";
    public static final String IDG_TISSUE = "IDG Tissue";
    public static final String IDG_TISSUE_REF = "IDG Tissue Ref";

    public static final String OMIM_GENE = "OMIM Gene";
    public static final String GWAS_TRAIT = "GWAS Trait";
    public static final String IMPC_TERM = "IMPC Term";
    public static final String MGI_TERM = "MGI Term";
    public static final String OMIM_TERM = "OMIM Term";
    
    public static final String REACTOME_REF = "Reactome Pathway Ref";
    
    public static final String GTEx_TISSUE = "GTEx Tissue";
    public static final String GTEx_EXPR = "GTEx Expression";
    public static final String HPM_TISSUE = "HPM Tissue";
    public static final String HPM_EXPR = "HPM Expression";
    public static final String HPA_RNA_TISSUE = "HPA-RNA Tissue";
    public static final String HPA_IHC_TISSUE = "HPA-IHC Tissue";

    public static final String PATENT_COUNT = "Patent Count";

    public static final String GO_COMPONENT = "GO Component";
    public static final String GO_PROCESS = "GO Process";
    public static final String GO_FUNCTION = "GO Function";

    public static final String PANTHER_PROTEIN_CLASS = "PANTHER Protein Class";
    public static final String PANTHER_PROTEIN_ANCESTRY =
        "PANTHER Protein Ancestry";
    
    public static final String DTO_PROTEIN_CLASS = "DTO Protein Class";
    public static final String DTO_PROTEIN_ANCESTRY = "DTO Protein Ancestry";
    
    public static final String TINX_NOVELTY = "TINX Novelty";
    public static final String TINX_IMPORTANCE = "TINX Importance";
    public static final String TINX_PUBLICATION = "TINX Publication";

    public static final String WHO_ATC = "WHO ATC";
    public static final String ATC_ANCESTRY =  "ATC Ancestry";
    
    public static final String ChEMBL = "ChEMBL";
    public static final String ChEMBL_ID = "ChEMBL ID";
    public static final String ChEMBL_MECHANISM = "ChEMBL Mechanism";
    public static final String ChEMBL_MOA_MODE = "ChEBML MOA Mode";
    public static final String ChEMBL_MECHANISM_COMMENT = "ChEMBL Mechanism Comment";
    public static final String ChEMBL_SYNONYM = "ChEMBL Synonym";
    public static final String ChEMBL_MOLFILE = "ChEMBL Molfile";
    public static final String ChEMBL_INCHI = "ChEMBL InChI";
    public static final String ChEMBL_INCHI_KEY = "ChEMBL InChI Key";
    public static final String ChEMBL_SMILES = "ChEMBL Canonical SMILES";
    public static final String ChEMBL_PROTEIN_CLASS = "ChEMBL Protein Class";
    public static final String ChEMBL_PROTEIN_ANCESTRY =
        "ChEMBL Protein Ancestry";
    public static final String ChEMBL_ACTIVITY_ID = "ChEMBL Activity ID";
    public static final String ChEMBL_MOLREGNO = "ChEMBL Molregno";

    public static final String UNIPROT_ACCESSION = "UniProt Accession";
    public static final String UNIPROT_GENE = "UniProt Gene";
    public static final String UNIPROT_DISEASE = "UniProt Disease";
    public static final String UNIPROT_DISEASE_RELEVANCE =
        "UniProt Disease Relevance";
    public static final String UNIPROT_TARGET = "UniProt Target";    
    public static final String UNIPROT_KEYWORD = "UniProt Keyword";
    public static final String UNIPROT_ORGANISM = "UniProt Organism";
    public static final String UNIPROT_SHORTNAME = "UniProt Shortname";
    public static final String UNIPROT_FULLNAME = "UniProt Fullname";
    public static final String UNIPROT_NAME = "UniProt Name";
    public static final String UNIPROT_TISSUE = "UniProt Tissue";
}
