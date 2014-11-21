package ix.ncats.models.clinical;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

import play.db.ebean.Model;
import javax.persistence.*;

import ix.core.models.Indexable;
import ix.core.models.Organization;
import ix.core.models.Keyword;
import ix.core.models.Publication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * based on definition from clinicaltrials.gov
 */
@Entity
@Table(name="ix_ncats_clinical_trial")
public class ClinicalTrial extends Model {
    @Id
    public Long id;

    @Column(length=15,unique=true)
    public String nctId;
    @Column(length=1024)
    public String url;

    @Basic(fetch=FetchType.EAGER)
    @Lob
    public String title;

    @Basic(fetch=FetchType.EAGER)
    @Lob
    public String officialTitle;

    @Lob
    public String summary;
    @Lob
    public String description;

    @Indexable(facet=true,name="Clinical Sponsor")
    @Column(length=1024)
    public String sponsor;

    @Indexable(facet=true,name="Study Type")    
    public String studyType;
    public String studyDesign;

    @Indexable(sortable=true,facet=true,name="Clinical Start Date")
    public Date startDate;
    @Indexable(sortable=true,facet=true,name="Clinical Completion Date")
    public Date completionDate;
    @Indexable(sortable=true)
    public Date firstReceivedDate;
    @Indexable(sortable=true,facet=true,name="Clinical Last Changed Date")
    public Date lastChangedDate;
    @Indexable(sortable=true)
    public Date verificationDate;
    @Indexable(sortable=true,facet=true,name="Clinical Results Received Date")
    public Date firstReceivedResultsDate;

    @Indexable(facet=true,name="Clinical Results")
    public boolean hasResults;

    @Indexable(facet=true,name="Clinical Status")
    public String status;

    @Indexable(facet=true,name="Clinical Phase")
    public String phase;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_trial_keyword")
    public List<Keyword> keywords = new ArrayList<Keyword>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_trial_sponsor")
    public List<Organization> sponsors = new ArrayList<Organization>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_trial_intervention")
    public List<Intervention> interventions = new ArrayList<Intervention>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_trial_condition")
    public List<Condition> conditions = new ArrayList<Condition>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clinical_trial_outcome")
    public List<Outcome> outcomes = new ArrayList<Outcome>();

    @OneToOne(cascade=CascadeType.ALL)
    public Eligibility eligibility;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clincial_trial_location")
    public List<Organization> locations = new ArrayList<Organization>();

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ncats_clincial_trial_publication")
    public List<Publication> publications = new ArrayList<Publication>();

    public ClinicalTrial () {}

    @JsonIgnore
    @Indexable(facet=true, name="Clinical Trial Duration")
    public Integer getTrialDuration () {
        Integer duration = null;
        if (startDate != null && completionDate != null) {
            Calendar start = Calendar.getInstance();
            start.setTime(startDate);
            Calendar end = Calendar.getInstance();
            end.setTime(completionDate);
            duration = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        }
        return duration;
    }
}
