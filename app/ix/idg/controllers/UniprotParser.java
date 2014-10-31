package ix.idg.controllers;

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

import ix.core.models.*;
import ix.core.controllers.PublicationFactory;
import ix.idg.models.Target;
import ix.utils.Global;


public class UniprotParser extends DefaultHandler {
    StringBuilder content = new StringBuilder ();
    Target target;
    Resource resource;
    LinkedList<String> path = new LinkedList<String>();
    Attribute source;
    int npubs;

    public UniprotParser () {
        resource = Resource.newPublic("UniProt");
        resource.url = "http://www.uniprot.org";
    }

    public void parse (String uri) throws Exception {
        source = new Attribute ("source", uri);
        source.resource = resource;
        URI u = new URI (uri);
        parse (u.toURL().openStream());
    }

    public void parse (InputStream is) throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(is, this);
    }

    @Override
    public void characters (char[] ch, int start, int length) {
        for (int i = start, j = 0; j < length; ++j, ++i) {
            content.append(ch[i]);
        }
    }

    @Override
    public void startDocument () {
        target = new Target ();
        npubs = 0;
    }

    @Override
    public void startElement (String uri, String localName, 
                              String qName, Attributes attrs) {
        content.setLength(0);
        path.push(qName);
        if (qName.equals("dbReference")) {
            String type = attrs.getValue("type");
            if ("pubmed".equalsIgnoreCase(type) && ++npubs <= 5) {
                String id = attrs.getValue("id");
                try {
                    long pmid = Long.parseLong(id);
                    Publication pub = PublicationFactory.fetchIfAbsent(pmid);
                    target.publications.add(pub);
                }
                catch (NumberFormatException ex) {
                    Logger.warn("Bogus PMID "+id);
                }
            }
        }
    }

    @Override
    public void endElement (String uri, String localName, String qName) {
        String value = content.toString().trim();

        Attribute p = new Attribute (resource, "path", getPath());
        path.pop();

        if (qName.equals("accession") 
            || qName.equals("shortName")) {
            target.synonyms.add(new Keyword (value, source, p));
        }
        else if (qName.equals("fullName")) {
            if ("recommendedName".equals(path.peek())) 
                target.name = value;
            else {
                target.synonyms.add(new Keyword (value, source, p));
            }
        }
        else if (qName.equals("name")) {
        }
    }

    String getPath () {
        StringBuilder sb = new StringBuilder ();
        for (String p : path) {
            sb.insert(0, "/"+p);
        }
        return sb.toString();
    }

    public Target getTarget () { return target; }
}
