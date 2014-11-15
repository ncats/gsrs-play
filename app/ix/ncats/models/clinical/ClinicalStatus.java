package ix.ncats.models.clinical;

/**
 * http://clinicaltrials.gov/ct2/help/glossary/recruitment-status
 */
public enum ClinicalStatus {
    Unknown,
        NotYetRecruiting,
        Completed,
        Terminated,
        Recruiting,
        Withdrawn,
        Available,
        Active,
        ActiveNotRecruiting,
        Suspended,
        EnrollingByInvitation;

    public static ClinicalStatus fromName (String name) {
        if ("Not yet recruiting".equalsIgnoreCase(name))
            return NotYetRecruiting;
        if ("Recruiting".equalsIgnoreCase(name))
            return Recruiting;
        if ("Active, not recruiting".equalsIgnoreCase(name))
            return ActiveNotRecruiting;
        if ("Completed".equalsIgnoreCase(name))
            return Completed;
        if ("Terminated".equalsIgnoreCase(name))
            return Terminated;
        if ("Withdrawn".equalsIgnoreCase(name))
            return Withdrawn;
        if ("Suspended".equalsIgnoreCase(name))
            return Suspended;
        if ("Enrolling by invitation".equalsIgnoreCase(name))
            return EnrollingByInvitation;

        return Unknown;
    }
}
