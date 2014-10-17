package ix.utils;

import java.net.URL;
import java.io.InputStreamReader;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import play.GlobalSettings;
import play.Application;
import play.Logger;

import ix.core.models.Publication;
import ix.core.models.Journal;
import ix.core.models.Author;
import ix.core.models.Keyword;
import ix.core.models.Mesh;

public class Eutils {
    static String EUTILS_URL = 
        "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&rettype=xml&id=";

    public static Publication fetchPublication (Long pmid) {
        String uri = EUTILS_URL+pmid;
        Publication pub = null;
        try {
            URL url = new URL (uri);

            org.w3c.dom.Document dom = getDOM (url);
            if (dom == null) {
                Logger.debug("No publication found for "+pmid);
                return null;
            }
            
            NodeList nodes = dom.getElementsByTagName("Article");
            if (nodes.getLength() == 0) {
                Logger.warn("PMID "+pmid+" has no Article element!");
                return null;
            }

            //Logger.info("Parsing "+url+"...");

            pub = new Publication ();
            Element article = (Element)nodes.item(0);
            
            pub.pmid = pmid;

            nodes = article.getElementsByTagName("Journal");
            Element jelm = nodes.getLength() > 0 
                ? (Element)nodes.item(0) : null;
            if (jelm != null) {
                pub.journal = getJournal (jelm);
            }
            
            // title
            nodes = article.getElementsByTagName("ArticleTitle");
            if (nodes.getLength() > 0) {
                pub.title = nodes.item(0).getTextContent();
            }
            
            // abstract
            nodes = article.getElementsByTagName("AbstractText");
            if (nodes.getLength() > 0) {
                StringBuilder text = new StringBuilder ();
                for (int i = 0; i < nodes.getLength(); ++i) {
                    if (text.length() > 0) text.append("\n");
                    Element elm = (Element)nodes.item(i);
                    String label = elm.getAttribute("Label");
                    if (label != null && label.length() > 0) {
                        text.append(label+": ");
                    }
                    text.append(elm.getTextContent());
                }
                pub.abstractText = text.toString();
            }
            
            // authors
            nodes = article.getElementsByTagName("AuthorList");
            if (nodes.getLength() > 0) {
                nodes = ((Element)nodes.item(0))
                    .getElementsByTagName("Author");
                for (int i = 0; i < nodes.getLength(); ++i) {
                    Node n = nodes.item(i);
                    Author author = getAuthor (n);
                    pub.authors.add(author);
                }
            }
                        
            nodes = article.getElementsByTagName("MedlinePgn");
            if (nodes.getLength() > 0) {
                pub.pages = nodes.item(0).getTextContent();
            }
            
            nodes = article.getElementsByTagName("ELocationID");
            if (nodes.getLength() > 0) {
                Element elm = (Element)nodes.item(0);
                if ("doi".equalsIgnoreCase(elm.getAttribute("EIdType"))) {
                    pub.doi = elm.getTextContent();
                }
            }
            
            nodes = dom.getElementsByTagName("ArticleId");
            for (int i = 0; i < nodes.getLength(); ++i) {
                Element elm = (Element)nodes.item(i);
                String idtype = elm.getAttribute("IdType");
                if ("doi".equalsIgnoreCase(idtype)) {
                    pub.doi = elm.getTextContent();
                }
                else if ("pubmed".equalsIgnoreCase(idtype)) {
                }
                else if ("pmc".equalsIgnoreCase(idtype)) 
                    pub.pmcid = elm.getTextContent();
            }
            
            nodes = dom.getElementsByTagName("NameOfSubstance");
            if (nodes.getLength() > 0) {
                List<String> substances = new ArrayList<String>();
                for (int i = 0; i < nodes.getLength(); ++i) {
                    substances.add(nodes.item(i).getTextContent());
                }
            }
            
            nodes = dom.getElementsByTagName("MeshHeading");
            if (nodes.getLength() > 0) {
                String descriptor = null;
                
                for (int i = 0; i < nodes.getLength(); ++i) {
                    Element headElm = (Element)nodes.item(i);
                    NodeList n = headElm
                        .getElementsByTagName("DescriptorName");
                    
                    List<Mesh> heading = new ArrayList<Mesh>();
                    // there must be at least one descriptor name
                    if (n.getLength() > 0) {
                        descriptor = n.item(0).getTextContent();
                    }
                    
                    n = headElm.getElementsByTagName("QualifierName");
                    if (n.getLength() > 0 && descriptor != null) {
                        for (int j = 0; j < n.getLength(); ++j) {
                            Element elm = (Element)n.item(j);
                            String term = descriptor
                                +"/"+elm.getTextContent();
                            Mesh m = new Mesh (term);
                            if ("Y".equalsIgnoreCase
                                (elm.getAttribute("MajorTopicYN")))
                                m.majorTopic = true;
                            heading.add(m);
                        }
                    }
                    
                    if (descriptor == null) {
                        Logger.warn
                            ("MeshHeading has no DescriptorName");
                    }
                    else if (heading.isEmpty())
                        pub.mesh.add(new Mesh (descriptor));
                    else
                        pub.mesh.addAll(heading);
                }
            }

            //Logger.info("pub "+pub+"...");
        }
        catch (Exception ex) {
            Logger.trace("Fetch failed: "+uri, ex);
        }

        return pub;
    }

    static Author getAuthor (Node node) {
        NodeList nodes = ((org.w3c.dom.Element)node)
            .getElementsByTagName("LastName");

        Author author = new Author ();
        if (nodes.getLength() > 0) {
            org.w3c.dom.Element elm = (org.w3c.dom.Element)nodes.item(0);
            author.lastname = elm.getTextContent();
        }

        nodes = ((org.w3c.dom.Element)node)
            .getElementsByTagName("ForeName");
        if (nodes.getLength() > 0) {
            org.w3c.dom.Element elm = (org.w3c.dom.Element)nodes.item(0);
            author.forename = elm.getTextContent();
        }

        nodes = ((org.w3c.dom.Element)node)
            .getElementsByTagName("Initials");
        if (nodes.getLength() > 0) {
            org.w3c.dom.Element elm = (org.w3c.dom.Element)nodes.item(0);
            author.initials = elm.getTextContent();
        }
        
        nodes = ((Element)node).getElementsByTagName("Affiliation");
        if (nodes.getLength() > 0) {
            author.affiliation = ((Element)nodes.item(0)).getTextContent();
        }

        return author;
    }

    static Journal getJournal (Element node) {
        Journal journal = new Journal ();
        NodeList nodes = node.getElementsByTagName("Title");
        if (nodes.getLength() > 0)
            journal.title = nodes.item(0).getTextContent();
        //Logger.info("Journal "+journal.title+"...");

        nodes = node.getElementsByTagName("Volume");
        if (nodes.getLength() > 0) 
            journal.volume = nodes.item(0).getTextContent();

        nodes = node.getElementsByTagName("Issue");
        if (nodes.getLength() > 0)
            journal.issue = Integer.parseInt(nodes.item(0).getTextContent());

        nodes = node.getElementsByTagName("Year");
        if (nodes.getLength() > 0)
            journal.year = Integer.parseInt(nodes.item(0).getTextContent());

        nodes = node.getElementsByTagName("Month");
        if (nodes.getLength() > 0) {
            journal.month = nodes.item(0).getTextContent();
            try {
                int mon = Integer.parseInt(journal.month);
                switch (mon) {
                case 1: journal.month = "Jan"; break;
                case 2: journal.month = "Feb"; break;
                case 3: journal.month = "Mar"; break;
                case 4: journal.month = "Apr"; break;
                case 5: journal.month = "May"; break;
                case 6: journal.month = "Jun"; break;
                case 7: journal.month = "Jul"; break;
                case 8: journal.month = "Aug"; break;
                case 9: journal.month = "Sep"; break;
                case 10: journal.month = "Oct"; break;
                case 11: journal.month = "Nov"; break;
                case 12: journal.month = "Dec"; break;
                default: 
                    Logger.warn("Unknown month: "+journal.month);
                }
            }
            catch (NumberFormatException ex) {
                // do nothing
            }
        }

        nodes = node.getElementsByTagName("ISSN");
        if (nodes.getLength() > 0)
            journal.issn = nodes.item(0).getTextContent();

        nodes = node.getElementsByTagName("ISOAbbreviation");
        if (nodes.getLength() > 0)
            journal.isoAbbr = nodes.item(0).getTextContent();

        return journal;
    }

    static org.w3c.dom.Document getDOM (URL url) throws Exception {
        /*
         * We first read the xml into a utf-8 byte buffer, then use the
         * StringReader to properly convert utf-8 to Java's unicode.
         * If we try to parse the xml directly, for whatever reason it
         * doesn't properly parse the encoded utf-8 stream!
         */
        /*
        ByteArrayOutputStream buffer = new ByteArrayOutputStream ();

        byte[] buf = new byte[1024];
        BufferedInputStream bis = new BufferedInputStream (url.openStream());
        for (int nb; (nb = bis.read(buf, 0, buf.length)) > 0; ) {
            buffer.write(buf, 0, nb);
        }
        bis.close();
        */
        //logger.info("## url="+url+": "+buffer.toString());

        DocumentBuilder db = DocumentBuilderFactory
            .newInstance().newDocumentBuilder();
        /*
        return db.parse
            (new InputSource (new StringReader (buffer.toString())));
        */
        return db.parse(new InputSource
                        (new InputStreamReader (url.openStream())));
    }
}
