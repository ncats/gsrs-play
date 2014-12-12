package ix.ncats.controllers.clinical;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;
import play.libs.ws.*;
import play.libs.F;

import ix.core.NamedResource;
import ix.core.models.Keyword;
import ix.ncats.models.clinical.*;
import ix.core.controllers.EntityFactory;
import ix.utils.Global;

import com.fasterxml.jackson.databind.ObjectMapper;

@NamedResource(name="clinicaltrials",type=ClinicalTrial.class)
public class ClinicalTrialFactory extends EntityFactory {
    static final public Model.Finder<Long, ClinicalTrial> finder = 
        new Model.Finder(Long.class, ClinicalTrial.class);

    public static List<ClinicalTrial> all () { return all (finder); }
    public static ClinicalTrial getEmployee (Long id) {
        return getEntity (id, finder);
    }
    public static Result count () {
        return count (finder);
    }
    public static Result page (int top, int skip) {
        return ClinicalTrialFactory.page(top, skip, null, null);
    }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result edits (Long id) {
        return edits (id, ClinicalTrial.class);
    }

    public static Result get (Long id, String expand) {
        return get (id, expand, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (ClinicalTrial.class, finder);
    }

    public static Result update (Long id, String field) {
        return badRequest ("Not supported operation");
    }

    public static Result delete (Long id) {
        return badRequest ("Not supported operation");
    }

    /**
     * For a given NCT id if it doesn't exist, then fetch it directly
     * from clinicaltrials.gov and save it locally.
     */
    public static Result createIfAbsent (String nctId) {
        ClinicalTrial ct = finder.where().eq("nctId", nctId).findUnique();
        if (ct == null) {
            try {
                CtXmlParser parser = new CtXmlParser 
                    ("http://clinicaltrials.gov/ct2/show/"
                     +nctId+"?displayxml=true");
                ct = parser.getCt();
                ct.save();
            }
            catch (Exception ex) {
                Logger.trace("Can't parse clinical trial "+nctId, ex);
                return badRequest ("Can't retrieve clinical trial "+nctId);
            }
        }

        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(ct));
    }

    public static Result loader () {
        DynamicForm requestData = Form.form().bindFromRequest();
        String uri = requestData.get("url");
        String key = requestData.get("api-key");

        int count = 0, newcnt = 0;
        Logger.debug("url=\""+uri+"\" key=\""+key+"\"");
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("load-file");

        File file = null;
        if (part != null) {
            String name = part.getFilename();
            String content = part.getContentType();
            file = part.getFile();
            Logger.debug("file="+name+" content="+content);
        }
        else {
            long total = 0l, last = System.currentTimeMillis();
            try {
                URL url = new URL (uri);
                file = File.createTempFile("inx", ".zip");
                Logger.debug("Downloading "+uri+" to temp "+file);
                FileOutputStream fos = new FileOutputStream (file);
                InputStream is = url.openStream();
                byte[] buf = new byte[1024];
                for (int nb; (nb = is.read(buf, 0, buf.length)) > 0; ) {
                    fos.write(buf, 0, nb);
                    total += nb;

                    long now = System.currentTimeMillis();
                    if (now - last >= 10000) {
                        Logger.debug(String.format
                                     ("%1$10.2f Mb", total/(1024.0*1024.0))
                                     +" "+new java.util.Date());
                        last = now;
                    }
                }
                fos.close();
            }
            catch (IOException ex) {
                return badRequest ("Unable to process url: "+uri);
            }
        }

        try {
            CtXmlParser parser = new CtXmlParser ();
            parser.setApiKey(key);
            
            ZipFile zf = new ZipFile (file);
            for (Enumeration<?> en = zf.entries(); 
                 en.hasMoreElements();) {
                ZipEntry ze = (ZipEntry)en.nextElement();

                if (Global.DEBUG(1))
                    Logger.debug("Processing "+ze.getName());
                try {
                    parser.parse(zf.getInputStream(ze));
                    ClinicalTrial newct = parser.getCt();
                    ClinicalTrial ct = finder.where().eq
                        ("nctId", newct.nctId).findUnique();
                    if (ct == null) {
                        Logger.debug("Creating new instance "
                                     +newct.nctId+" "+newcnt);

                        newct.save();
                        ct = newct;
                        ++newcnt;
                    }
                    ++count;
                }
                catch (IOException ex) {
                    Logger.trace("Can't parse entry "+ze.getName()
                                 +" in "+uri, ex);
                }
                //Logger.debug("--"+ze.getName());
            }
            zf.close();

            return ok (count+" total trials, of which "+newcnt+" are new!");
        }
        catch (Throwable t) {
            Logger.trace("Can't process file "+file, t);
        }

        return internalServerError ("Can't process request");
    }

    public static Result index () {
        return ok (ix.ncats.views.html
                   .clinical.ctloader.render("ClinicalTrials.gov Loader"));
    }
}
