package controllers.granite;

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
import models.granite.*;
import models.core.Keyword;

public class GrantXmlParser extends DefaultHandler {
    List<GrantListener> listeners = new ArrayList<GrantListener>();
    StringBuilder content = new StringBuilder ();
    Grant grant;
    int count;
    LinkedList<String> path = new LinkedList<String>();
    DateFormat df = new SimpleDateFormat ("MM/dd/yyyy");

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
        for (int i = start, j = 0; j < length; ++j, ++i) {
            content.append(ch[i]);
        }
    }

    @Override
    public void startElement (String uri, String localName, 
                              String qName, Attributes attrs) {
        if (qName.equals("row")) { 
            grant = new Grant ();
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
        if (value.length() == 0)
            ;
        else if (qName.equals("row")) {
            //Logger.info(count+": grant parsed "+grant.applicationId);
            for (GrantListener l : listeners)
                l.newGrant(grant);
            ++count;
        }
        else if (qName.equals("APPLICATION_ID"))
            grant.applicationId = Long.parseLong(value);
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
                value = value.substring(pos+25);
            grant.publicHealthRelevance = value;
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
    }
}
