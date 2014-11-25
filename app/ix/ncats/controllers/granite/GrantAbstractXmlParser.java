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
import ix.ncats.models.Grant;
import ix.utils.Global;


public class GrantAbstractXmlParser extends DefaultHandler {
    StringBuilder content = new StringBuilder ();
    Grant grant;
    int count;
    Global g = Global.getInstance();

    public GrantAbstractXmlParser () {
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
    public void startDocument () {
        count = 0;
    }

    @Override
    public void startElement (String uri, String localName, 
                              String qName, Attributes attrs) {
        if (qName.equals("row"))
            grant = null;
        content.setLength(0);
    }

    @Override
    public void endElement (String uri, String localName, String qName) {
        String value = content.toString().trim();
        if (value.length() == 0)
            ;
        else if (qName.equals("row")) {
        }
        else if (qName.equals("APPLICATION_ID")) {
            try {
                long appId = Long.parseLong(value);
                List<Grant> grants = GrantFactory.finder.where().eq
                    ("applicationId", appId).findList();
                if (!grants.isEmpty())
                    grant = grants.iterator().next();
                else
                    Logger.debug("Can't find application "+appId);
            }
            catch (Exception ex) {
                Logger.debug("Can't search for grant "+value);
            }
        }
        else if (qName.equals("ABSTRACT_TEXT")) {
            if (grant != null) {
                grant.projectAbstract = value;
                grant.update();
                if (++count % 100 == 0) {
                    Logger.debug(count+": grant "+grant.id+"/"
                                 +grant.applicationId+" updated!");
                }
            }
        }
    }
}

