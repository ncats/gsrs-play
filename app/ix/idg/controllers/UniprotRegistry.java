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

import play.Play;
import play.Logger;
import play.libs.ws.*;
import play.db.ebean.Model;
import play.db.ebean.Transactional;
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

import ix.core.plugins.IxContext;
import ix.utils.Global;
import ix.utils.Util;


public class UniprotRegistry extends DefaultHandler {
    public static final String ACCESSION = "UniProt Accession";
    public static final String GENE = "UniProt Gene";
    public static final String DISEASE = "UniProt Disease";
    public static final String DISEASE_RELEVANCE = "UniProt Disease Relevance";
    public static final String TARGET = "UniProt Target";    
    public static final String KEYWORD = "UniProt Keyword";
    public static final String ORGANISM = "UniProt Organism";
    public static final String SHORTNAME = "UniProt Shortname";
    public static final String FULLNAME = "UniProt Fullname";
    public static final String NAME = "UniProt Name";
    public static final String TISSUE = "UniProt Tissue";

    static final int TIMEOUT = 5000; // 5s
    static public Namespace namespace = NamespaceFactory.registerIfAbsent
        ("UniProt", "http://www.uniprot.org");

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
    
    LinkedList<String> path = new LinkedList<String>();
    int npubs;
    File cacheDir;

    public UniprotRegistry () {
        IxContext ctx = Play.application().plugin(IxContext.class);
        cacheDir = new File (ctx.cache(), "uniprot");
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    File getCacheFile (String acc) {
        String name = acc.substring(0, 2);
        return new File (new File (cacheDir, name), acc+".xml");
    }

    public void register (Target target, String acc) throws Exception {
        File file = getCacheFile (acc);
        if (file.exists() && file.length() > 0l) {
            Logger.debug("Cached file: "+file+" "+file.length());
            register (target, new FileInputStream (file));
        }
        else {
            file.getParentFile().mkdirs(); 
            WSRequestHolder ws = WS
                //.url("http://www.uniprot.org/uniprot/"+acc+".xml")
                .url("https://tripod.nih.gov/uniprot/"+acc)
                .setHeader("User-Agent", Util.randomUserAgent())
                .setTimeout(TIMEOUT)
                .setFollowRedirects(true);
            byte[] buf = ws.get().get(TIMEOUT).asByteArray();
            // cache the download
            FileOutputStream fos = new FileOutputStream (file);
            fos.write(buf, 0, buf.length);
            fos.close();
            Logger.debug("Cached "+acc+" ("+buf.length+" bytes)");
            // now register
            register (target, new ByteArrayInputStream (buf));
        }
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
        target.namespace = namespace;
    }

    @Override
    @Transactional
    public void endDocument () {
        //Logger.debug("About to register target\n"+EntityFactory.getEntityMapper().toJson(target, true));
        target.save();
        // create the other direction
        for (XRef ref : target.links) {
            Model obj = (Model)ref.deRef();
            if (obj instanceof EntityModel) {
                XRef xref = createXRef (target);
                for (Keyword kw : target.synonyms) {
                    if (ACCESSION.equals(kw.label)) {
                        Keyword uni = KeywordFactory.registerIfAbsent
                            (TARGET, target.name, kw.href);
                        xref.properties.add(uni);
                        break; // just grab the first one
                    }
                }
                ((EntityModel)obj).getLinks().add(xref);
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
                        pub = PublicationFactory.registerIfAbsent(pmid);
                        if (pub != null) {
                            target.publications.add(pub);
                            pubs.put(pmid, pub);
                        }
                    }
                    pubkeys.put(refkey, pub);
                    xref = createXRef (pub);
                    //xref.properties.add(new Text ("Title", pub.title));
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
                (Expr.and(Expr.eq("synonyms.label", namespace.name),
                          Expr.eq("synonyms.term", id))).findList();
            if (diseases.isEmpty()) {
                disease = new Disease ();
                disease.namespace = namespace;
                Logger.debug("New disease "+id);
                Keyword kw = KeywordFactory.registerIfAbsent
                    (namespace.name, id, "http://www.uniprot.org/diseases/"+id);
                disease.synonyms.add(kw);
            }
            else {
                disease = diseases.iterator().next();
                Logger.debug("Disease "+id+" is already in db!\n"
                             +EntityFactory.getEntityMapper()
                             .toJson(disease, true));
            }
        }
        else if (qName.equals("keyword")) {
            keyword = new Keyword ();
            keyword.label = KEYWORD;
            keyword.href = "http://www.uniprot.org/keywords/"
                +attrs.getValue("id");
        }
        else if (qName.equals("name")) {
            if ("organism".equals(parent)) {
                String type = attrs.getValue("type");
                if ("scientific".equals(type)) {
                    organism = new Keyword ();
                    organism.label = ORGANISM;
                }
                else {
                    organism = null;
                }
            }
            else if ("gene".equals(parent)) {
                String type = attrs.getValue("type");
                if ("primary".equals(type)) {
                    gene = new Gene ();
                    gene.namespace = namespace;
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
            kw.label = ACCESSION;
            kw.href = "http://www.uniprot.org/uniprot/"+value;
            target.synonyms.add(kw);
        }
        else if (qName.equals("shortName")) {
            Keyword kw = new Keyword (value);
            kw.label = SHORTNAME;
            target.synonyms.add(kw);
        }
        else if (qName.equals("fullName")) {
            if ("recommendedName".equals(parent)) 
                target.name = value;
            else {
                Keyword kw = new Keyword (value);
                kw.label = FULLNAME;
                target.synonyms.add(kw);
            }
        }
        else if (qName.equals("name")) {
            if ("entry".equals(parent)) {
                Keyword kw = new Keyword (value);
                kw.label = NAME;
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
                        Keyword kw = new Keyword (GENE, value);
                        gene.synonyms.add(kw);
                        gene.update();
                    }
                }
                // also add gene as synonym
                Keyword kw = new Keyword (GENE, value);
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
                    (TISSUE, value, null);
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
                            (DISEASE, disease.name, kw.href);
                        xref.properties.add(uni);
                    }
                }
                
                if ("disease".equals(commentType)) {
                    xref.properties.add(new Text
                                        (DISEASE_RELEVANCE, value));
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
