package ix.core.models;

import java.util.*;
import play.db.ebean.Model;
import javax.persistence.*;


@Entity
@Table(name="ix_core_organization")
public class Organization extends Model {
    @Id
    public Long id; // internal id

    /**
     * For extramural projects, the Data Universal Numbering System 
     * number of the grantee organization or contractor.  The DUNS 
     * number is a unique nine-digit number assigned by Dun and 
     * Bradstreet Information Services. 
     */
    @Column(length=10)
    public String duns;

    /**
     * The name of the educational institution, research organization, 
     * business, or government agency receiving funding for the grant, 
     * contract, cooperative agreement, or intramural project.  
     */
    @Indexable(facet=true, name="Institution")
    public String name;

    /**
     * The departmental affiliation of the contact principal investigator 
     * for a project, using a standardized categorization of departments.
     * Names are available only for medical school departments.
     */
    @Indexable(facet=true, name="Department")
    public String department;

    /**
     * The city in which the business office of the grantee organization
     * or contractor is located.  Note that this may be different from the
     * research performance site.  For all NIH intramural projects, 
     * Bethesda, MD is used. 
     */
    @Indexable(facet=true, name="City")
    public String city;

    /**
     * The state in which the business office of the grantee organization 
     * or contractor is located.  Note that this may be different from the
     * research performance site. 
     */
    @Column(length=128)
    @Indexable(facet=true, name="State")
    public String state;

    /**
     * The zip code in which the business office of the grantee 
     * organization or contractor is located.  Note that this may be
     * different from the research performance site.
     */
    @Column(length=64)
    public String zipcode;

    /**
     * The congressional district in which the business office of the
     * grantee organization or contractor is located.  Note that this
     * may be different from the research performance site. 
     */
    public String district;

    /**
     * The country in which the business office of the grantee organization
     * or contractor is located.  Note that this may be different from the
     * research performance site. 
     */
    @Indexable(facet=true, name="Country")
    public String country;


    /**
     * The country code of the grantee organization or contractor as 
     * defined in the Federal Information Processing Standard. 
     */
    @Column(length=3)
    @Indexable(facet=true, name="Country Code")
    public String fips;

    /**
     * Longitude & Latitude
     */
    public Double longitude;
    public Double latitude;

    public Organization () {
    }
}
