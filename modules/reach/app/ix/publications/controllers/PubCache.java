package ix.publications.controllers;

import java.io.*;
import java.util.*;
import java.net.*;

import play.*;
import play.cache.Cache;
import play.data.*;
import play.mvc.*;
import play.libs.ws.*;

import ix.core.plugins.EutilsPlugin;
import ix.core.models.Publication;

public class PubCache extends Controller {
    static final EutilsPlugin Eutils = 
        Play.application().plugin(EutilsPlugin.class);
    
    public static Result index () {
        return ok (ix.publications.views.html.pubcache.render(null));
    }

    public static Result load () {
        DynamicForm requestData = Form.form().bindFromRequest();
        Set<Long> pmids = new TreeSet<Long>();
        
        String text = requestData.get("pmid-field");
        if (text != null) {
            String[] toks = text.split("[;,\\s]+");
            for (String t : toks) {
                try {
                    pmids.add(Long.parseLong(t));
                }
                catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("pmid-file");
        if (part != null) {
            File file = part.getFile();
            try {
                BufferedReader br = new BufferedReader (new FileReader (file));
                for (String line; (line = br.readLine()) != null; ) {
                    for (String t : line.split("[;,\\s]")) {
                        try {
                            pmids.add(Long.parseLong(t));
                        }
                        catch (NumberFormatException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        String mesg = null;
        if (!pmids.isEmpty()) {
            Set<Long> cached = new HashSet<Long>();
            for (Long p : pmids) {
                Publication pub = Eutils.getPublication(p);
                if (pub != null) {
                    Logger.debug(p+": "+pub.title);
                    cached.add(p);
                }
            }
            pmids.removeAll(cached);
            if (!pmids.isEmpty()) {
                mesg = pmids.size()+" out of "+(pmids.size()+cached.size())
                    +" did not loaded!";
            }
            else {
                mesg = cached.size()+" pubmed article(s) cached!";
            }
        }

        return ok (ix.publications.views.html.pubcache.render(mesg));
    }
}
