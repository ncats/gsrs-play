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
import ix.core.controllers.PredicateFactory;
import ix.core.controllers.NamespaceFactory;
import ix.idg.controllers.DiseaseFactory;

import ix.idg.models.Target;
import ix.idg.models.Disease;

import ix.utils.Global;


public class UniprotRegistry extends DefaultHandler {
    StringBuilder content = new StringBuilder ();
    Target target;
    XRef xref;
    Disease disease;
    String commentType;
    Integer refkey;
    Keyword keyword;
    Set<Integer> evidence = new HashSet<Integer>();
    Map<Long, Publication> pubs = new HashMap<Long, Publication>();
    Map<Integer, Publication> pubkeys = new HashMap<Integer, Publication>();
    Map<String, String> values = new HashMap<String, String>();
    
    Namespace namespace;
    LinkedList<String> path = new LinkedList<String>();
    int npubs;

    public UniprotRegistry () {
        namespace = NamespaceFactory.get("UniProt");
        if (namespace == null) {
            namespace = Namespace.newPublic("UniProt");
            namespace.location = "http://www.uniprot.org";
            namespace.save();
            Logger.debug("New namespace created: "
                         +namespace.id+" "+namespace.name);
        }
    }

    public void register (String acc) throws Exception {
        URI u = new URI ("http://www.uniprot.org/uniprot/"+acc+".xml");
        register (u.toURL().openStream());
    }

    public void register (InputStream is) throws Exception {
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
        pubs.clear();
        pubkeys.clear();
        values.clear();
        evidence.clear();
    }

    @Override
    public void endDocument () {
        target.save();
        Logger.debug("Target "+target.id+" \""+target.name+"\" added!");
    }

    @Override
    public void startElement (String uri, String localName, 
                              String qName, Attributes attrs) {
        content.setLength(0);
        values.remove(qName);
        String parent = path.peek();
        
        if (qName.equals("dbReference")) {
            String type = attrs.getValue("type");
            if ("pubmed".equalsIgnoreCase(type) /*&& ++npubs <= 80*/) {
                String id = attrs.getValue("id");
                try {
                    long pmid = Long.parseLong(id);
                    Publication pub = pubs.get(pmid);
                    if (pub == null) {
                        pub = PublicationFactory.fetchIfAbsent(pmid);
                        target.publications.add(pub);
                        pubs.put(pmid, pub);
                    }
                    pubkeys.put(refkey, pub);
                    xref = createXRef (pub);
                }
                catch (NumberFormatException ex) {
                    Logger.warn("Bogus PMID "+id);
                }
            }
            else if ("disease".equals(parent)) {
                if (disease != null) {
                    String id = attrs.getValue("id");
		    Keyword kw = new Keyword (type, id);
		    if ("MIM".equals(type))
			kw.url = "http://www.omim.org/entry/"+id;
                    disease.synonyms.add(kw);
                }
            }
        }
        else if (qName.equals("reference")) {
            xref = null;
            refkey = Integer.parseInt(attrs.getValue("key"));
        }
        else if (qName.equals("comment")) {
            evidence.clear();
            String ev = attrs.getValue("evidence");
            if (ev != null) {
                for (String s : ev.split("\\s")) {
                    evidence.add(Integer.parseInt(s));
                }
            }
            commentType = attrs.getValue("type");
        }
        else if (qName.equals("disease")) {
            String id = attrs.getValue("id");
            List<Disease> diseases = DiseaseFactory.finder.where
                (Expr.and(Expr.eq("synonyms.label", "UniProt"),
                          Expr.eq("synonyms.term", id))).findList();
            if (diseases.isEmpty()) {
                disease = new Disease ();
		Keyword kw = new Keyword ("UniProt", id);
		kw.url = "http://www.uniprot.org/diseases/"+id;
                disease.synonyms.add(kw);
            }
            else {
                disease = null;
            }
        }
        else if (qName.equals("keyword")) {
            keyword = new Keyword ();
	    keyword.label = "Keyword";
            keyword.url = "http://www.uniprot.org/keywords/"
		+attrs.getValue("id");
        }
        path.push(qName);
    }

    @Override
    public void endElement (String uri, String localName, String qName) {
        String value = content.toString().trim();
        String p = getPath ();
        path.pop();
        
        String parent = path.peek();
        values.put(qName, value);
        //Logger.debug(p+"="+value);
        
        if (qName.equals("accession")) {
	    Keyword kw = new Keyword (value);
	    kw.label = "UniProt Accession";
	    kw.url = "http://www.uniprot.org/uniprot/"+value;
	    target.synonyms.add(kw);
        }
	else if (qName.equals("shortName")) {
	    Keyword kw = new Keyword (value);
	    kw.label = "UniProt Shortname";
	    kw.url = "http://www.uniprot.org/uniprot/"+value;
	    target.synonyms.add(kw);
	}
        else if (qName.equals("fullName")) {
            if ("recommendedName".equals(parent)) 
                target.name = value;
            else {
                Keyword kw = new Keyword (value);
                kw.label = "UniProt Fullname";
                target.synonyms.add(kw);
            }
        }
        else if (qName.equals("name")) {
            if ("entry".equals(parent)) {
                Keyword kw = new Keyword (value);
                kw.label = "UniProt Name";
                target.synonyms.add(kw);
            }
            else if ("disease".equals(parent)) {
                if (disease != null)
                    disease.name = value;
            }
        }
        else if (qName.equals("description")) {
            if ("disease".equals(parent)) {
                if (disease != null)
                    disease.description = value;
            }
        }
        else if (qName.equals("scope")) {
            if ("reference".equals(parent)) {
                if (xref != null) {
                    xref.properties.add(new Text (p, value));
                }
            }
        }
        else if (qName.equals("tissue")) {
            if (xref != null) {
                xref.properties.add(new Text (p, value));
            }
        }
        else if (qName.equals("reference")) {
            if (xref != null)
                xref.save();
        }
        else if (qName.equals("disease")) {
            if (disease != null) {
                disease.save();
                Logger.debug("New disease "
                             +disease.id+" \""+disease.name+"\" added!");
            }
        }
        else if (qName.equals("comment")) {
            String text = values.get("text");
            if ("disease".equals(commentType)) {
                XRef xref = createXRef (disease);
                xref.properties.add(new Text (value, disease.name));
                target.links.add(xref);
            }
            else {
                target.properties.add(new Text (commentType, text));
            }
            commentType = null;
        }
        else if (qName.equals("keyword")) {
	    keyword.term = value;
            target.properties.add(keyword);
        }
    }

    String getPath () {
        StringBuilder sb = new StringBuilder ();
        for (String p : path) {
            sb.insert(0, "/"+p);
        }
        return sb.toString();
    }

    XRef createXRef (Object obj) {
        XRef xref = new XRef (obj);
        xref.namespace = namespace;
        return xref;
    }

    public Target getTarget () { return target; }
}
