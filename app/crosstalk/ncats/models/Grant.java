package crosstalk.ncats.models;

import java.util.*;
import play.data.validation.Constraints.*;
import play.db.ebean.Model;
import javax.persistence.*;

import crosstalk.core.models.Publication;
import crosstalk.core.models.Keyword;
import crosstalk.core.models.Investigator;
import crosstalk.core.models.Indexable;

@Entity
@Table(name="ct_ncats_grant")
public class Grant extends Model {

    @Id
    public Long id; // internal id

    /**
     * Description from http://exporter.nih.gov/about.aspx
     */

    /**
     * A unique identifier of the project record in the ExPORTER database. 
     */
    public Long applicationId;

    /**
     * A 3-character code identifying the grant, contract, or intramural 
     * activity through which a project is supported.  Within each funding 
     * mechanism, NIH uses 3-character activity codes (e.g., F32, K08, P01, 
     * R01, T32, etc.) to differentiate the wide variety of research-related 
     * programs NIH supports. A comprehensive list of activity codes for 
     * grants and cooperative agreements may be found on the Types of Grant 
     * Programs Web page.  RePORTER also includes R&D contracts (activity 
     * codes beginning with the letter N) and intramural projects (beginning 
     * with the letter Z). 
     */
    // http://grants.nih.gov/grants/funding/ac_search_results.htm
    @Indexable(facet=true, name="GrantActivity")
    public String activity; 

    /**
     * Administering Institute or Center - A two-character code to 
     * designate the agency, NIH Institute, or Center administering 
     * the grant.  See Institute/Center code definitions.
     */
    public String administeringIc;

    /**
     * A one-digit code to identify the type of application funded:
     * 1 = New application
     * 2 = Competing continuation (also, competing renewal)
     * 3 = Application for additional (supplemental) support.  There are 
           two kinds of type 
     *   3: competing revisions (which are peer-reviewed) and 
            administrative supplements.
     * 4 = Competing extension for an R37 award or first non-competing 
           year of a Fast Track SBIR/STTR award
     * 5 = Non-competing continuation
     * 7 = Change of grantee institution
     * 9 = Change of NIH awarding Institute or Division (on a 
           competing continuation)
    */
    public Integer applicationType;

    /**
     * indicates a project supported by funds appropriated through the 
     * American Recovery and Reinvestment Act of 2009.
     */
    public Boolean isArraFunded;

    /**
     * Award notice date or Notice of Grant Award (NGA) is a legally 
     * binding document stating the government has obligated funds and 
     * which defines the period of support and the terms and conditions 
     * of award. 
     */
    public Date awardNoticeDate;

    /**
     * The date when a project's funding for a particular fiscal year begins. 
     */
    public Date budgetStart;

    /**
     * The date when a project's funding for a particular fiscal year ends.
     */
    public Date budgetEnd;

    /**
     * Federal programs are assigned a number in the Catalog of Federal 
     * Domestic Assistance (CFDA), which is referred to as the "CFDA code." 
     * The CFDA database helps the Federal government track all programs 
     * it has domestically funded. 
     */
    public Integer cfdaCode;

    /**
     * The number of the funding opportunity announcement, if any, under 
     * which the project application was solicited.  Funding opportunity 
     * announcements may be categorized as program announcements, requests 
     * for applications, notices of funding availability, solicitations, 
     * or other names depending on the agency and type of program. Funding 
     * opportunity announcements can be found at Grants.gov/FIND and in 
     * the NIH Guide for Grants and Contracts. 
     */
    public String foaNumber;

    /**
     * Commonly referred to as a grant number, intramural project, or 
     * contract number.  For grants, this unique identification number 
     * is composed of the type code, activity code, Institute/Center 
     * code, serial number, support year, and (optional) a suffix code 
     * to designate amended applications and supplements.
     */
    public String fullProjectNum;

    /**
     * A unique numeric designation assigned to subprojects of a "parent"
     *  multi-project research grant. 
     */
    public Long subprojectId;

    /**
     * The NIH Institute or Center(s) providing funding for a project 
     * are designated by their acronyms (see Institute/Center acronyms).  
     * Each funding IC is followed by a colon (:) and the amount of 
     * funding provided for the fiscal year by that IC.  Multiple ICs 
     * are separated by semicolons (;).  Project funding information is 
     * available only for NIH projects awarded in FY 2008 and later 
     * fiscal years.
     */
    @OneToMany(cascade=CascadeType.ALL)
    public List<Funding> fundingICs = new ArrayList<Funding>();

    /**
     * The fiscal year appropriation from which project funds were 
     * obligated.
     */
    public Integer fiscalYear;

    /**
     * Full name of the administering agency, Institute, or Center.
     */
    @Indexable(facet=true, name="GrantAdminIC")
    public String icName;

    /**
     * Generic name for the grouping of components across an institution 
     * who has applied for or receives NIH funding. The official name as 
     * used by NIH is Major Component Combining Name. 
     */
    @Indexable(facet=true, name="GrantInstitutionSchool")
    public String edInstType;

    /**
     * Congressionally-mandated reporting categories into which NIH 
     * projects are categorized.  Available for fiscal years 2008 and 
     * later.  Each project's spending category designations for each 
     * fiscal year are made available the following year as part of the 
     * next President's Budget request.  See the Research, Condition, 
     * and Disease Categorization System for more information on the 
     * categorization process.
     */
    @Indexable(facet=true, name="GrantSpendingCategory")
    public String nihSpendingCats;

    /**
     * The name(s) of the Principal Investigator(s) designated by the 
     * organization to direct the research project. 
     */
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_ncats_grant_investigator")
    public List<Investigator> investigators = new ArrayList<Investigator>();

    /**
     * An Institute staff member who coordinates the substantive aspects 
     * of a contract from planning the request for proposal to oversight.
     */
    public String programOfficerName;

    /**
     * The start date of a project.  For subprojects of a multi-project 
     * grant, this is the start date of the parent award. 
     */
    public Date projectStart;

    /**
     * The current end date of the project, including any future years 
     * for which commitments have been made.  For subprojects of a 
     * multi-project grant, this is the end date of the parent award.  
     * Upon competitive renewal of a grant, the project end date is 
     * extended by the length of the renewal award. 
     */
    public Date projectEnd;

    /**
     * An identifier for each research project, used to associate the 
     * project with publication and patent records.  This identifier 
     * is not specific to any particular year of the project.  It 
     * consists of the project activity code, administering IC, and 
     * serial number (a concatenation of Activity, Administering_IC, 
     * and Serial_Number). 
     */
    public String coreProjectNum;

    /**
     * Prior to fiscal year 2008, these were thesaurus terms assigned
     * by NIH CRISP indexers.  For projects funded in fiscal year 2008 
     * and later, these are concepts that are mined from the project's
     * title, abstract, and specific aims using an automated text mining 
     * tool.
     */
    @ManyToMany(cascade=CascadeType.ALL)
    @Basic(fetch=FetchType.EAGER)
    @JoinTable(name="ct_ncats_grant_keyword")
    public List<Keyword> projectTerms = new ArrayList<Keyword>();

    /**
     * Title of the funded grant, contract, or intramural (sub)project. 
     */
    @Required
    public String projectTitle;

    /**
     * Submitted as part of a grant application, this statement 
     * articulates a project's potential to improve public health. 
     */
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String publicHealthRelevance;

    /**
     * A six-digit number assigned in serial number order within 
     * each administering organization. 
     */
    public Long serialNumber;

    /**
     * A designator of the legislatively-mandated panel of subject 
     * matter experts that reviewed the research grant application for
     * scientific and technical merit. 
     */
    @Indexable(facet=true, name="GrantStudySectionCode")
    public String studySection;

    /**
     * The full name of a regular standing Study Section that reviewed
     * the research grant application for scientific and technical merit.
     * Applications reviewed by panels other than regular standing study
     * sections are designated by "Special Emphasis Panel."
     */
    @Indexable(facet=true, name="GrantStudySection")
    public String studySectionName;

    /**
     * A suffix to the grant application number that includes the letter
     * "A" and a serial number to identify an amended version of an 
     * original application and/or the letter "S" and serial number 
     * indicating a supplement to the project.
     */
    @Indexable(facet=true, name="GrantSuffix")
    public String suffix;

    /**
     */
    @Indexable(facet=true, name="GrantFundingMechism")
    public String fundingMechanism;

    /**
     * Total project funding from all NIH Institute and Centers for a 
     * given fiscal year. Costs are available only for:
       + NIH and CDC grant awards (only the parent record of 
           multi-project grants) funded in FY 2000 and later fiscal years.
       + NIH intramural projects (activity codes beginning with "Z") in 
           FY 2007 and later fiscal years.
       + NIH contracts (activity codes beginning with "N") in FY 2007 
           and later fiscal years.
     * For multi-project grants, Total_Cost includes funding for all
     * of the constituent subprojects. This field will be blank on 
     * subproject records; the total cost of each subproject is found
     * in Total_Cost_Sub_Project (FY 2000 and later fiscal years only). 
     */
    public Integer totalCost;

    /**
     * Applies to subproject records only.  Total funding for a subproject
     * from all NIH Institute and Centers for a given fiscal year.  
     * Costs are available only for NIH awards funded in FY 2000 and
     * later fiscal years.
     */
    public Integer totalCostSubproject;

    /**
     * An abstract of the research being performed in the project. 
     * For grants, the abstract is supplied to NIH by the grantee.
     */
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String projectAbstract;

    /**
     *  Publications as PubMed identifiers
     */
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ct_ncats_grant_publication")
    public List<Publication> publications = new ArrayList<Publication>();


    public Grant () {
    }
}
