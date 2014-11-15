package ix.ncats.models.clinical;

/**
 * http://prsinfo.clinicaltrials.gov/definitions.html#StudyType
 */
public enum StudyType {
    Interventional,
        Observational,
        ExpandedAccess,
        Unknown;

    public static StudyType fromName (String name) {
        if ("Interventional".equalsIgnoreCase(name))
            return Interventional;
        if ("Observational".equalsIgnoreCase(name))
            return Observational;
        if ("Expanded Access".equalsIgnoreCase(name))
            return ExpandedAccess;
        return Unknown;
    }
}
