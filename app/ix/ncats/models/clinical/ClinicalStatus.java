package ix.ncats.models.clinical;

public enum ClinicalStatus {
    Unknown,
        Completed,
        Terminated,
        Recruiting,
        Withdrawn,
        Available,
        Active;

    public static ClinicalStatus fromName (String name) {
        return valueOf (ClinicalStatus.class, name.replaceAll("\\s", "_"));
    }
}
