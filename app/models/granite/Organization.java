package models.granite;

import java.util.*;
import play.db.ebean.Model;
import javax.persistence.*;


@Entity
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
    public String name;

    /**
     * The departmental affiliation of the contact principal investigator 
     * for a project, using a standardized categorization of departments.
     * Names are available only for medical school departments.
     */
    public String department;

    /**
     * The city in which the business office of the grantee organization
     * or contractor is located.  Note that this may be different from the
     * research performance site.  For all NIH intramural projects, 
     * Bethesda, MD is used. 
     */
    public String city;

    /**
     * The state in which the business office of the grantee organization 
     * or contractor is located.  Note that this may be different from the
     * research performance site. 
     */
    @Column(length=20)
    public String state;

    /**
     * The zip code in which the business office of the grantee 
     * organization or contractor is located.  Note that this may be
     * different from the research performance site.
     */
    @Column(length=15)
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
    public String country;


    /**
     * The country code of the grantee organization or contractor as 
     * defined in the Federal Information Processing Standard. 
     */
    @Column(length=3)
    public String fips;

    public Organization () {
    }
}
