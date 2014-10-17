package ix.ncats.models.clinical;

/**
 * consult http://prsinfo.clinicaltrials.gov/definitions.html
 */
public enum ClinicalPhase {
    Unknown,
        Investigation,
        Approved,
        Phase_0,
        Phase_1,
        Phase_2,
        Phase_12, // combine 1 & 2
        Phase_3,
        Phase_23, // combined 2 & 3
        Phase_4;

    public static ClinicalPhase fromName (String name) {
        return valueOf (ClinicalPhase.class, name.replaceAll("\\s", "_"));
    }
}
