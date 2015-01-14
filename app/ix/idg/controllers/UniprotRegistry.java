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
import play.db.ebean.Model;
import com.avaje.ebean.Expr;

import ix.core.models.*;
import ix.core.controllers.PublicationFactory;
import ix.core.controllers.PredicateFactory;
import ix.core.controllers.KeywordFactory;
import ix.core.controllers.NamespaceFactory;
import ix.core.controllers.EntityFactory;
import ix.idg.controllers.DiseaseFactory;

import ix.idg.models.Gene;
import ix.idg.models.Target;
import ix.idg.models.Disease;
import ix.idg.models.EntityModel;

import ix.utils.Global;


public class UniprotRegistry extends DefaultHandler {
    StringBuilder content = new StringBuilder ();
    Target target;
    XRef xref;
    Disease disease;
    Gene gene;
    String commentType;
    Integer refkey;
    Keyword keyword;
    Keyword organism;
    Set<Integer> evidence = new HashSet<Integer>();
    Map<Long, Publication> pubs = new HashMap<Long, Publication>();
    Map<Integer, Publication> pubkeys = new HashMap<Integer, Publication>();
    Map<String, String> values = new HashMap<String, String>();
    
    Namespace namespace;
    LinkedList<String> path = new LinkedList<String>();
    int npubs;

    public UniprotRegistry () {
        namespace = NamespaceFactory.registerIfAbsent
	    ("UniProt", "http://www.uniprot.org");
    }

    public void register (Target target, String acc) throws Exception {
        URI u = new URI ("http://www.uniprot.org/uniprot/"+acc+".xml");
        register (target, u.toURL().openStream());
    }

    public void register (String acc) throws Exception {
	register (null, acc);
    }

    public void register (Target target, InputStream is) throws Exception {
	this.target = target;
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
	if (target == null) {
	    target = new Target ();
	}
        npubs = 0;
	gene = null;
	disease = null;
	xref = null;
	organism = null;
	keyword = null;
        pubs.clear();
        pubkeys.clear();
        values.clear();
        evidence.clear();
    }

    @Override
    public void endDocument () {
	//Logger.debug("About to register target\n"+EntityFactory.getEntityMapper().toJson(target, true));
        target.save();
	// create the other direction
	for (XRef ref : target.links) {
	    Model obj = (Model)ref.deRef();
	    if (obj instanceof EntityModel) {
		XRef xref = createXRef (target);
		for (Keyword kw : target.synonyms) {
		    if ("UniProt Accession".equals(kw.label)) {
			Keyword uni = KeywordFactory.registerIfAbsent
			    ("UniProt Target", target.name, kw.href);
			xref.properties.add(uni);
			break; // just grab the first one
		    }
		}
		((EntityModel)obj).links.add(xref);
		obj.update();
	    }
	}
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
		    xref.properties.add(new Text ("Title", pub.title));
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
			kw.href = "http://www.omim.org/entry/"+id;
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
		Logger.debug("New disease "+id);
		Keyword kw = KeywordFactory.registerIfAbsent
		    ("UniProt", id, "http://www.uniprot.org/diseases/"+id);
                disease.synonyms.add(kw);
            }
            else {
                disease = diseases.iterator().next();
		Logger.debug("Disease "+id+" is already in db!\n"
			     +EntityFactory.getEntityMapper().toJson(disease, true));
            }
        }
        else if (qName.equals("keyword")) {
            keyword = new Keyword ();
	    keyword.label = "Keyword";
            keyword.href = "http://www.uniprot.org/keywords/"
		+attrs.getValue("id");
        }
	else if (qName.equals("name")) {
	    if ("organism".equals(parent)) {
		String type = attrs.getValue("type");
		if ("scientific".equals(type)) {
		    organism = new Keyword ();
		    organism.label = "UniProt Organism";
		}
		else {
		    organism = null;
		}
	    }
	    else if ("gene".equals(parent)) {
		String type = attrs.getValue("type");
		if ("primary".equals(type)) {
		    gene = new Gene ();
		}
	    }
	}
        path.push(qName);
	//Logger.debug("++"+getPath ());
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
	    kw.href = "http://www.uniprot.org/uniprot/"+value;
	    target.synonyms.add(kw);
        }
	else if (qName.equals("shortName")) {
	    Keyword kw = new Keyword (value);
	    kw.label = "UniProt Shortname";
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
	    else if ("gene".equals(parent)) {
		if (gene != null) {
		    if (gene.name == null) {
			gene = GeneFactory.registerIfAbsent(value);
			target.links.add(createXRef (gene));
		    }
		    else {
			Keyword kw = new Keyword ("UniProt Gene", value);
			gene.synonyms.add(kw);
			gene.update();
		    }
		}
		// also add gene as synonym
		Keyword kw = new Keyword ("UniProt Gene", value);
		target.synonyms.add(kw);
	    }
	    else if ("organism".equals(parent)) {
		if (organism != null) {
		    organism.term = value;
		    List<Keyword> org = KeywordFactory.finder.where
			(Expr.and(Expr.eq("label", organism.label),
				  Expr.eq("term", organism.term)))
			.findList();
		    if (org.isEmpty()) {
			organism.save();
			target.organism = organism;
		    }
		    else {
			target.organism = org.iterator().next();
		    }
		}
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
		Keyword tissue = KeywordFactory.registerIfAbsent
		    ("UniProt Tissue", value, null);
                xref.properties.add(tissue);
            }
        }
        else if (qName.equals("reference")) {
            if (xref != null) {
                xref.save();
		target.links.add(xref);
	    }
	    refkey = null;
        }
        else if (qName.equals("disease")) {
            if (disease.id == null) {
                disease.save();
                Logger.debug("New disease "
                             +disease.id+" \""+disease.name+"\" added!");
            }
        }
        else if (qName.equals("comment")) {
            String text = values.get("text");
            if (disease != null) {
                XRef xref = createXRef (disease);
		for (Keyword kw : disease.synonyms) {
		    if ("UniProt".equals(kw.label)) {
			Keyword uni = KeywordFactory.registerIfAbsent
			    ("UniProt Disease", disease.name, kw.href);
			xref.properties.add(uni);
		    }
		}
		
		if ("disease".equals(commentType)) {
		    xref.properties.add(new Text
					("UniProt Disease Comment", value));
		}
                target.links.add(xref);
		disease = null;
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
	else if (qName.equals("gene"))
	    gene = null;
	else if (qName.equals("ecNumber")) {
	    Keyword kw = new Keyword ("EC", value);
	    kw.href = "http://enzyme.expasy.org/EC/"+value;
	    target.properties.add(kw);
	}
	//Logger.debug("--"+p);
    }

    String getPath () {
        StringBuilder sb = new StringBuilder ();
        for (String p : path) {
            sb.insert(0, "/"+p);
        }
        return sb.toString();
    }

    public XRef createXRef (Object obj) {
        XRef xref = new XRef (obj);
        xref.namespace = namespace;
        return xref;
    }

    public Target getTarget () { return target; }
}
