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
import ix.idg.controllers.DiseaseFactory;

import ix.idg.models.Target;
import ix.idg.models.Disease;
import ix.idg.models.EntityModel;

import ix.utils.Global;

public class DiseaseOntologyRegistry {
    Namespace namespace;
    
    public DiseaseOntologyRegistry () {
        namespace = NamespaceFactory.registerIfAbsent
	    ("Disease Ontology", "http://www.disease-ontology.org");
    }

    /**
     * bulk registration from .obo file
     */
    public void register (InputStream is) throws IOException {
	OboParser obo = new OboParser (is);
	
	Map<String, String> parents  = new HashMap<String, String>();
	Map<String, Disease> diseaseMap = new HashMap<String, Disease>();
	while (obo.next()) {
	    if (obo.obsolete)
		continue;
	    List<Disease> diseases = DiseaseFactory.finder
		.where(Expr.and(Expr.eq("synonyms.label", "DOID"),
				Expr.eq("synonyms.term", obo.id)))
		.findList();
	    if (diseases.isEmpty()) {
		Disease disease = new Disease ();
		disease.name = obo.name;
		disease.description = obo.def;
		Keyword kw = new Keyword ("DOID", obo.id);
		kw.href =
		    "http://www.disease-ontology.org/api/metadata/"+obo.id;
		disease.synonyms.add(kw);
		for (String id : obo.alts) {
		    kw = new Keyword ("DOID", id);
		    //kw.href = "http://www.disease-ontology.org/api/metadata/"+id;
		    disease.synonyms.add(kw);
		}
		for (String alias : obo.synonyms) {
		    disease.synonyms.add(new Keyword ("Keyword", alias));
		}
		for (String xref : obo.xrefs) {
		    
		}
		
		if (obo.parentId != null) {
		    parents.put(obo.id, obo.parentId);
		}
		disease.save();
		diseaseMap.put(obo.id, disease);
		Logger.debug(disease.id+": "+obo.id+" "+obo.name);
	    }
	}

	// now resolve references
	for (Map.Entry<String, String> me : parents.entrySet()) {
	    Disease child = diseaseMap.get(me.getKey());
	    Disease parent = diseaseMap.get(me.getValue());
	    if (parent == null) {
		List<Disease> diseases = DiseaseFactory.finder
		    .where(Expr.and(Expr.eq("synonyms.label", "DOID"),
				    Expr.eq("synonyms.term", me.getValue())))
		    .findList();
		if (diseases.isEmpty()) {
		    Logger.warn("Disease "+child.id+"["+me.getKey()
				+"] references unknown parent "+me.getValue());
		}
		else {
		    parent = diseases.iterator().next();
		}
	    }

	    if (parent != null) {
		XRef xref = createXRef (parent);
		xref.properties.add(new Text ("is_a", parent.name));
		child.links.add(xref);
		child.update();
	    }
	}
	Logger.debug(diseaseMap.size()+" disease(s) registered!");
    }
    
    public XRef createXRef (Object obj) {
        XRef xref = new XRef (obj);
        xref.namespace = namespace;
        return xref;
    }
}
