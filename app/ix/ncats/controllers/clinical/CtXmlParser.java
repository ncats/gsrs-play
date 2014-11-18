package ix.ncats.controllers.clinical;

import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.db.ebean.*;

import ix.core.models.Keyword;
import ix.core.models.Organization;
import ix.core.models.Publication;
import ix.ncats.models.clinical.*;
import ix.core.controllers.PublicationFactory;

import ix.utils.Global;

public class CtXmlParser extends DefaultHandler {
    static final Model.Finder<Long, Condition> condDb = 
        new Model.Finder(Long.class, Condition.class);

    static DateFormat []dateFormats = new DateFormat[]{
        new SimpleDateFormat ("MMM dd, yyyy"),
        new SimpleDateFormat ("MMM yyyy")
    };

    static final String GEO_URL = 
        "https://maps.googleapis.com/maps/api/geocode/json?";

    static final String RARE_DISEASE_URL = 
        "http://clinicaltrials.gov/ct2/search/browse?brwse=ord_alpha_all&brwse-force=true";

    static class LoadRareDiseases {
        Set<String> diseases = new TreeSet<String>();
        LoadRareDiseases () {
            try {
                BufferedReader br = new BufferedReader 
                    (new InputStreamReader 
                     (new URL (RARE_DISEASE_URL).openStream()));
                for (String line; (line = br.readLine()) != null; ) {
                    int pos = line.indexOf("results?cond=");
                    if (pos > 0) {
                        while (line.charAt(pos) != '>')
                            ++pos;
                        int end = ++pos;
                        while (line.charAt(end) != '<')
                            ++end;
                        String disease = line.substring(pos, end);
                        //Logger.debug(disease);
                        diseases.add(disease);
                    }
                }
                Logger.debug(diseases.size()+" rare diseases table created!");
            }
            catch (IOException ex) {
                Logger.trace("Can't parse rare diseases", ex);
            }
        }

        public boolean contains (String disease) {
            return diseases.contains(disease);
        }
    }

    StringBuilder content = new StringBuilder ();
    ClinicalTrial ct;
    LinkedList<String> path = new LinkedList<String>();
    Map<String, Arm> arms = new HashMap<String, Arm>();
    Arm arm;
    Organization facility;
    Outcome outcome;
    Eligibility eli;
    Intervention interv;
    String apiKey;
    ObjectMapper mapper = new ObjectMapper ();
    LoadRareDiseases rareDiseases = new LoadRareDiseases();
    Set<Long> pmids = new HashSet<Long>();

    public CtXmlParser () {}
    public CtXmlParser (String uri) throws Exception {
        parse (uri);
    }

    public ClinicalTrial getCt () { return ct; }

    public void setApiKey (String key) {
        this.apiKey = key;
    }

    public void parse (String uri) throws Exception {
        URI u = new URI (uri);
        parse (u.toURL().openStream());
    }

    public void parse (InputStream is) throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(is, this);
    }

    /**
     * DefaultHandler
     */
    @Override
    public void characters (char[] ch, int start, int length) {
        for (int i = start, j = 0; j < length; ++j, ++i) {
            content.append(ch[i]);
        }
    }

    @Override
    public void startElement (String uri, String localName, 
                              String qName, Attributes attrs) {
        //Logger.debug(">> "+qName);
        if (qName.equals("clinical_study")) {
            ct = new ClinicalTrial ();
        }
        else if (qName.equals("intervention")) {
            ct.interventions.add(interv = new Intervention ());
        }
        else if (qName.equals("facility"))
            ct.locations.add(facility = new Organization ());
        else if (qName.equals("primary_outcome"))
            ct.outcomes.add(outcome = new Outcome (Outcome.Type.Primary));
        else if (qName.equals("secondary_outcome"))
            ct.outcomes.add(outcome = new Outcome (Outcome.Type.Secondary));
        else if (qName.equals("eligibility"))
            ct.eligibility = eli = new Eligibility ();

        content.setLength(0);
        path.push(qName);
    }

    @Override
    public void startDocument () {
        path.clear();
        arms.clear();
        pmids.clear();
    }

    @Override
    public void endElement (String uri, String localName, String qName) {
        if (qName.equals("clinical_study")) {
            if (Global.DEBUG(2))
                Logger.debug("<< Done: "+ct.nctId);

            return;
        }

        path.pop();
        String parent = path.peek();
        String value = content.toString().trim();

        if (value.length() == 0)
            ;
        else if (qName.equals("nct_id"))
            ct.nctId = value;
        else if (qName.equals("url")) {
            if (parent.equals("required_header"))
                ct.url = value;
        }
        else if (qName.equals("brief_title")) {
            ct.title = value;
        }
        else if (qName.equals("official_title")) {
            ct.officialTitle = value;
        }
        else if (qName.equals("agency")) {
            if (parent.equals("lead_sponsor"))
                ct.sponsor = value;
        }
        else if (qName.equals("textblock")) {
            if (parent.equals("brief_summary")) {
                if (ct.summary == null)
                    ct.summary = value;
                else
                    ct.summary = ct.summary+"\n\n"+value;
            }
            else if (parent.equals("detailed_description")) {
                if (ct.description == null)
                    ct.description = value;
                else
                    ct.description = ct.description+"\n\n"+value;
            }
            else if (parent.equals("criteria")) {
                eli.criteria = value;
                /*
                String[] tokens = value.split("[\t\n]+");
                for (int i = 0; i < tokens.length; ++i) {
                    String tok = tokens[i].trim();
                    if (tok.toLowerCase().indexOf("inclusion criteria") >= 0) {
                        while (++i < tokens.length)
                        eli.exclusions.add(new Keyword (last));
                                last = null;
                            }
                            ++toggles;
                        }
                    }
                    else if (tok.toLowerCase()
                             .indexOf("exclusion criteria") >= 0) {
                        if (toggles < 2) {
                            inclusion = false;
                            if (last != null) {
                                eli.inclusions.add(new Keyword (last));
                                last = null;
                            }
                            ++toggles;
                        }
                    }
                    else if (inclusion != null) {
                        tok = tok.trim();
                        if (tok.charAt(0) == '-' || tok.charAt(0) == '+'
                            || tok.charAt(0) == '*') {
                            tok = tok.substring(1).trim();
                            if (last != null) {
                                if (inclusion) {
                                    eli.inclusions.add(new Keyword (last));
                                }
                                else {
                                    eli.exclusions.add(new Keyword (last));
                                }
                            }
                            last = tok;                            
                        }
                        else if (last != null) {
                            last += " "+tok;
                        }
                    }
                }

                if (last != null) {
                    if (inclusion) {
                        eli.inclusions.add(new Keyword (last));
                    }
                    else {
                        eli.exclusions.add(new Keyword (last));
                    }
                }
                */
            }
        }
        else if (qName.equals("overall_status")) {
            ct.status = value;
        }
        else if (qName.equals("phase")) {
            ct.phase = value;
        }
        else if (qName.equals("study_type")) {
            ct.studyType = value;
        }
        else if (qName.equals("study_design")) {
            ct.studyDesign = value;
        }
        else if (qName.equals("intervention_type")) {
            interv.type = value;
        }
        else if (qName.equals("intervention_name")) {
            interv.name = value;
        }
        else if (qName.equals("description")) {
            if (parent.equals("intervention")) {
                interv.description = value;
            }
            else if (parent.equals("arm_group"))
                arm.description = value;
            else if (parent.equals("primary_outcome") 
                     || parent.equals("secondary_outcome"))
                outcome.description = value;
        }
        else if (qName.equals("arm_group_label")) {
            if (parent.equals("arm_group")) {
                arm = new Arm ();
                arm.label = value;
                arms.put(value, arm);
            }
            else if (parent.equals("intervention")) {
                Arm a = arms.get(value);
                if (a != null)
                    interv.arms.add(a);
                else
                    Logger.warn("Unknown arm group: "+value);
            }
        }
        else if (qName.equals("arm_group_type")) {
            arm.type = value;
        }
        else if (qName.equals("condition")) {
            Condition cond = new Condition (value);
            cond.isRareDisease = rareDiseases.contains(value);
            ct.conditions.add(cond);
        }
        else if (qName.equals("keyword") || qName.equals("mesh_term")) {
            ct.keywords.add(new Keyword (value));
        }
        else if (qName.equals("PMID")) {
            try {
                long pmid = Long.parseLong(value);
                if (!pmids.contains(pmid)) {
                    Publication pub = PublicationFactory.fetchIfAbsent(pmid);
                    if (pub != null)
                        ct.publications.add(pub);
                    pmids.add(pmid);
                }
            }
            catch (NumberFormatException ex) {
                Logger.warn("Not a valid PMID: "+value);
            }
        }
        else if (qName.equals("name")) {
            if (parent.equals("facility"))
                facility.name = value;
        }
        else if (qName.equals("city")) {
            if (parent.equals("address"))
                facility.city = value;
        }
        else if (qName.equals("state")) {
            if (parent.equals("address"))
                facility.state = value;
        }
        else if (qName.equals("zip")) {
            if (parent.equals("address")) {
                facility.zipcode = value;
            }
        }
        else if (qName.equals("country")) {
            if (parent.equals("address"))
                facility.country = value;
        }
        else if (qName.equals("facility")) {
            if (apiKey != null && apiKey.length() > 0) {
                String geo = GEO_URL+"address="+facility.city+", "
                    +facility.state+"&components=country:"
                    +facility.country;
                try {
                    URL url = new URL (geo);
                    JsonNode node = mapper.readTree(url.openStream());
                    JsonNode status = node.get("status");
                    if ("ok".equalsIgnoreCase(status.asText())) {
                        JsonNode geoloc = 
                            node.get("results")
                            .get("geometry").get("location");
                        facility.longitude = geoloc.get("lng").asDouble();
                        facility.latitude = geoloc.get("lat").asDouble();
                    }
                }
                catch (Throwable t) {
                    Logger.trace("Unable to geodecoding: "+geo, t);
                }
            }
        }
        else if (qName.equals("measure")) {
            if (parent.equals("primary_outcome") 
                || parent.equals("secondary_outcome"))
                outcome.measure = value;
        }
        else if (qName.equals("time_frame")) {
            if (parent.equals("primary_outcome") 
                || parent.equals("secondary_outcome"))
                outcome.timeframe = value;
        }
        else if (qName.equals("safety_issue")) {
            if (parent.equals("primary_outcome") 
                || parent.equals("secondary_outcome"))
                outcome.safetyIssue = "yes".equalsIgnoreCase(value);
        }
        else if (qName.equals("firstreceived_date")) {
            ct.firstReceivedDate = parseDate (value);
        }
        else if (qName.equals("lastchanged_date")) {
            ct.lastChangedDate = parseDate (value);
        }
        else if (qName.equals("verification_date")) {
            ct.verificationDate = parseDate (value);
        }
        else if (qName.equals("start_date")) {
            ct.startDate = parseDate (value);
        }
        else if (qName.equals("completion_date")) {
            ct.completionDate = parseDate (value);
        }
        else if (qName.equals("firstreceived_results_date")) {
            ct.firstReceivedResultsDate = parseDate (value);
            ct.hasResults = true;
        }
        else if (qName.equals("gender")) {
            if (parent.equals("eligibility"))
                eli.gender = value;
        }
        else if (qName.equals("minimum_age")) {
            if (parent.equals("eligibility"))
                eli.minAge = value;
        }
        else if (qName.equals("maximum_age")) {
            if (parent.equals("eligibility"))
                eli.maxAge = value;
        }
        else if (qName.equals("healthy_volunteers")) {
            if (parent.equals("eligibility"))
                eli.healthyVolunteers = "yes".equalsIgnoreCase(value);
        }

        if (Global.DEBUG(2))
            Logger.debug(">> "+qName+": "+value);
    }

    static Date parseDate (String date) {
        for (DateFormat df : dateFormats) {
            try {
                Date d = df.parse(date);
                if (d != null)
                    return d;
            }
            catch (ParseException ex) {
            }
        }

        Logger.warn("Can't parse date: "+date);
        return null;
    }
}
