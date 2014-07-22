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
import com.avaje.ebean.Expr;
import models.granite.Grant;
import models.core.Publication;
import controllers.core.PublicationFactory;


public class GrantPubXmlParser extends DefaultHandler {
    StringBuilder content = new StringBuilder ();
    int count;
    Long pmid;
    String proj;

    public GrantPubXmlParser () {
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

    @Override
    public void characters (char[] ch, int start, int length) {
        for (int i = start, j = 0; j < length; ++j, ++i) {
            content.append(ch[i]);
        }
    }

    @Override
    public void startDocument () {
        count = 0;
    }

    @Override
    public void startElement (String uri, String localName, 
                              String qName, Attributes attrs) {
        if (qName.equals("row")) {
            proj = null;
            pmid = null;
        }
        content.setLength(0);
    }

    @Override
    public void endElement (String uri, String localName, String qName) {
        String value = content.toString().trim();
        if (value.length() == 0)
            ;
        else if (qName.equals("row")) {
            if (proj != null) {
                List<Grant> grants = GrantFactory.finder
                    .where().eq("coreProjectNum", proj)
                    .findList();
                if (!grants.isEmpty()) {
                    List<Publication> pubs = PublicationFactory.finder
                        .where().eq("pmid", pmid).findList();

                    Publication pub;
                    if (!pubs.isEmpty())
                        pub = pubs.iterator().next();
                    else {
                        pub = new Publication ();
                        pub.pmid = pmid;
                    }

                    for (Grant g : grants) {
                        g.publications.add(pub);
                        g.update();
                    }

                    if (++count % 100 == 0) {
                        Logger.debug(count+": "+proj+" "+pmid);
                    }
                }
            }
        }
        else if (qName.equals("PROJECT_NUMBER")) {
            proj = value;
        }
        else if (qName.equals("PMID")) {
            try {
                pmid = Long.parseLong(value);
            }
            catch (NumberFormatException ex) {
                Logger.error("Bogus PMID "+value);
            }
        }
    }    
}
