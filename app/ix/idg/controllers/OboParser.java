package ix.idg.controllers;

import java.io.*;
import java.util.*;

public class OboParser {
    public String id;
    public String name;
    public String def;
    public List<String> alts = new ArrayList<String>();
    public List<String> urls = new ArrayList<String>();
    public List<String> synonyms = new ArrayList<String>();
    public List<String> xrefs = new ArrayList<String>();
    
    public String parentId;
    public String parentName;
    public boolean obsolete;
    public String version;
    public String date;
    
    BufferedReader br;
    String line;

    public OboParser (InputStream is) throws IOException {
        br = new BufferedReader (new InputStreamReader (is));
        while ((line = br.readLine()) != null) {
            if (line.startsWith("[Term]"))
                break;
            else if (line.startsWith("format-version:")) {
                version = line.substring(line.indexOf(':')+1).trim();
            }
            else if (line.startsWith("date:")) {
                date = line.substring(line.indexOf(':')+1).trim();
            }
        }
        
        if (line == null)
            throw new IllegalArgumentException ("Not a valid Obo file!");
    }

    void reset () {
        id = null;
        name = null;
        def = null;
        urls.clear();
        alts.clear();
        synonyms.clear();
        xrefs.clear();
        parentId = null;
        parentName = null;
        obsolete = false;
    }
    
    public boolean next () throws IOException {         
        //System.out.println(">>\""+line+"\"");
        boolean next = line.equals("[Term]");
        reset ();
        while ((line = br.readLine()) != null && !line.startsWith("[")) {
            int pos = line.indexOf(':');
            if (pos > 0) {
                String term = line.substring(0, pos);
                String value = line.substring(pos+1).trim();
                if (term.equals("id")) {
                    id = value;
                }
                else if (term.equals("name")) {
                    name = value;
                }
                else if (term.equals("def")) {
                    pos = value.indexOf('"');
                    def = value.substring(pos+1, value.indexOf('"', pos+1));
                }
                else if (term.equals("synonym")) {
                    pos = value.indexOf('"');
                    synonyms.add(value.substring
                                 (pos+1, value.indexOf('"',pos+1)));
                }
                else if (term.equals("xref")) {
                    xrefs.add(value);
                }
                else if (term.equals("is_a")) {
                    String[] toks = value.split("!");
                    parentId = toks[0].trim();
                    parentName = toks[1].trim();
                }
                else if (term.equals("alt_id")) {
                    alts.add(value);
                }
                else if (term.equals("is_obsolete")) {
                    obsolete = Boolean.parseBoolean(value);
                }
            }
        }
        //System.out.println("<<"+line+" "+id);

        return next;
    }

    public static void main (String[] argv) throws Exception {
        OboParser obo = new OboParser (System.in);
        while (obo.next()) {
            System.out.println(obo.id+" "+obo.name+" "+obo.obsolete);
        }
    }
}
