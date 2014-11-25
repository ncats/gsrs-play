package ix.ncats.controllers.granite;

import java.io.*;
import java.util.*;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.xml.parsers.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;

import play.Logger;
import com.avaje.ebean.Expr;
import ix.ncats.models.*;
import ix.core.models.Keyword;
import ix.core.models.Organization;
import ix.core.models.Investigator;
import ix.core.controllers.OrganizationFactory;
import ix.core.controllers.InvestigatorFactory;
import ix.utils.Global;

public class GrantXmlParser extends DefaultHandler {
    
    List<GrantListener> listeners = new ArrayList<GrantListener>();
    StringBuilder content = new StringBuilder ();
    Grant grant;
    int count;
    LinkedList<String> path = new LinkedList<String>();
    DateFormat df = new SimpleDateFormat ("MM/dd/yyyy");
    Organization org;
    LinkedList<Investigator> pis = new LinkedList<Investigator>();

    public GrantXmlParser () {
    }

    public void parse (String uri) throws Exception {
        URI u = new URI (uri);
        parse (u.toURL().openStream());
    }

    public void parse (InputStream is) throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(is, this);
    }

    public int getCount () { return count; }
    
    public void addGrantListener (GrantListener l) {
        listeners.add(l);
    }
    public void removeGrantListener (GrantListener l) {
        listeners.remove(l);
    }

    /**
     * DefaultHandler
     */
    @Override
    public void characters (char[] ch, int start, int length) {
        for (int i = start, j = start+length; i < j; ++i)
            content.append(ch[i]);
    }

    @Override
    public void startElement (String uri, String localName, 
                              String qName, Attributes attrs) {
        if (qName.equals("row")) { 
            grant = new Grant ();
            org = new Organization ();
        }
        else if (qName.equals("PI")) {
            pis.push(new Investigator ());
        }
        content.setLength(0);
        path.push(qName);
    }

    @Override
    public void startDocument () {
        path.clear();
    }

    @Override
    public void endElement (String uri, String localName, String qName) {
        path.pop();
        String parent = path.peek();
        String value = content.toString().trim();

        if (Global.DEBUG(2)) {
            Logger.debug(getClass().getName()+": "+qName+": "+value);
        }

        if (value.length() == 0)
            ;
        else if (qName.equals("row")) {
            if (Global.DEBUG(2)) {
                Logger.debug(getClass().getName()
                             +": parsing "+grant.applicationId+" "+count);
            }

            List<Organization> orgs = OrganizationFactory
                .finder.where(Expr.and
                              (Expr.eq("duns", org.duns),
                               Expr.eq("department", org.department)))
                .findList();

            if (!orgs.isEmpty()) {
                org = orgs.iterator().next();
            }

            //Logger.debug("Organization "+org.name+" ("+org.duns+")");
            Set<Long> unique = new HashSet<Long>();
            for (Investigator inv : pis) {
                if (!unique.contains(inv.piId)) {
                    if (org.id != null) {
                        List<Investigator> invs = InvestigatorFactory.finder
                            .where(Expr.and
                                   (Expr.eq("organization.id", org.id),
                                    Expr.eq("name", inv.name)))
                            .findList();
                        if (invs.isEmpty()) {
                            inv.organization = org;
                            //inv.save();
                        }
                        else {
                            inv = invs.iterator().next();
                        }
                    }
                    else
                        inv.organization = org;
                 
                    unique.add(inv.piId);
                    grant.investigators.add(inv);
                }
            }
            pis.clear();
            org = null;

            //Logger.info(count+": grant parsed "+grant.applicationId);
            for (GrantListener l : listeners)
                l.newGrant(grant);
            ++count;
        }
        else if (qName.equals("APPLICATION_ID")) {
            grant.applicationId = Long.parseLong(value);
        }
        else if (qName.equals("ACTIVITY"))
            grant.activity = value;
        else if (qName.equals("ADMINISTERING_IC"))
            grant.administeringIc = value;
        else if (qName.equals("APPLICATION_TYPE"))
            grant.applicationType = Integer.parseInt(value);
        else if (qName.equals("ARRA_FUNDED"))
            grant.isArraFunded = "Y".equals(value);
        else if (qName.equals("BUDGET_START")) {
            try {
                grant.budgetStart = df.parse(value);
            }
            catch (Exception ex) {
                Logger.error("Grant "+grant.applicationId+": can't parse "
                             +qName+": \""+content+"\"");
            }
        }
        else if (qName.equals("BUDGET_END")) {
            try {
                grant.budgetEnd = df.parse(value);
            }
            catch (Exception ex) {
                Logger.error("Grant "+grant.applicationId+": can't parse "
                             +qName+": \""+content+"\"");
            }
        }
        else if (qName.equals("FOA_NUMBER"))
            grant.foaNumber = value;
        else if (qName.equals("FULL_PROJECT_NUM"))
            grant.fullProjectNum = value;
        else if (qName.equals("PROJECT_TITLE"))
            grant.projectTitle = value;
        else if (qName.equals("PROJECT_START")) {
            try {
                grant.projectStart = df.parse(value);
            }
            catch (Exception ex) {
                Logger.error("Grant "+grant.applicationId+": can't parse "
                             +qName+": \""+content+"\"");
            }
        }
        else if (qName.equals("PROJECT_END")) {
            try {
                grant.projectEnd = df.parse(value);
            }
            catch (Exception ex) {
                Logger.error("Grant "+grant.applicationId+": can't parse "
                             +qName+": \""+content+"\"");

            }
        }
        else if (qName.equals("PHR")) {
            int pos = value.indexOf("PUBLIC HEALTH RELEVANCE:");
            if (pos >= 0)
                value = value.substring(pos+24);
            grant.publicHealthRelevance = value.trim();
        }
        else if (qName.equals("SERIAL_NUMBER")) {
            if (content.length() > 0)
                grant.serialNumber = Long.parseLong(value);
        }
        else if (qName.equals("STUDY_SECTION"))
            grant.studySection = value;
        else if (qName.equals("STUDY_SECTION_NAME"))
            grant.studySectionName = value;
        else if (qName.equals("SUFFIX"))
            grant.suffix = value;
        else if (qName.equals("TOTAL_COST")) {
            if (content.length() > 0)
                grant.totalCost = Integer.parseInt(value);

        }
        else if (qName.equals("FUNDING_ICs")) {
            for (String tok : value.split("[;\\\\]")) {
                String[] toks = tok.split(":");
                if (toks.length == 2) {
                    grant.fundingICs.add(new Funding
                                         (toks[0], Integer.parseInt(toks[1])));
                }
            }
        }
        else if (qName.equals("CORE_PROJECT_NUM"))
            grant.coreProjectNum = value;
        else if (qName.equals("AWARD_NOTICE_DATE")) {
            try {
                grant.awardNoticeDate = df.parse(value);
            }
            catch (Exception ex) {
                Logger.error("Grant "+grant.applicationId+": can't parse "
                             +qName+": \""+content+"\"");
            }
        }
        else if (qName.equals("ED_INST_TYPE"))
            grant.edInstType = value;
        else if (qName.equals("PROGRAM_OFFICER_NAME"))
            grant.programOfficerName = value;
        else if (qName.equals("FY")) 
            grant.fiscalYear = Integer.parseInt(value);
        else if (qName.equals("IC_NAME"))
            grant.icName = value;
        else if (qName.equals("TERM")) {
            if (parent != null && parent.equals("PROJECT_TERMSX")) {
                grant.projectTerms.add(new Keyword (value));
            }
        }
        else if (qName.equals("ORG_CITY")) {
            org.city = value;
        }
        else if (qName.equals("ORG_COUNTRY")) {
            org.country = value;
        }
        else if (qName.equals("ORG_DISTRICT")) {
            org.district = value;
        }
        else if (qName.equals("ORG_DUNS")) {
            org.duns = value;
        }
        else if (qName.equals("ORG_DEPT")) {
            org.department = value;
        }
        else if (qName.equals("ORG_FIPS")) {
            org.fips = value;
        }
        else if (qName.equals("ORG_STATE")) {
            org.state = value;
        }
        else if (qName.equals("ORG_ZIPCODE")) {
            if (value.length() == 9) {
                value = value.substring(0, 5)+"-"+value.substring(5);
            }
            org.zipcode = value;
        }
        else if (qName.equals("ORG_NAME")) {
            org.name = value;
        }
        else if (qName.equals("PI_NAME")) {
            int pos = value.indexOf("(contact");
            Investigator inv = pis.peek();
            if (pos > 0) {
                inv.name = value.substring(0, pos).trim();
                inv.role = Investigator.Role.Contact;
            }
            else {
                inv.name = value;
                inv.role = Investigator.Role.PI;
            }
        }
        else if (qName.equals("PI_ID")) {
            try {
                int pos = value.indexOf(' ');
                if (pos > 0) {
                    value = value.substring(0, pos);
                }
                pis.peek().piId = Long.parseLong(value);
            }
            catch (NumberFormatException ex) {
                Logger.error("Grant "+grant.applicationId
                             +": bogus PI_ID \""+value+"\"");
            }
        }
    }
}
