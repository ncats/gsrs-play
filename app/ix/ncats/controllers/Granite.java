package ix.ncats.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

import play.*;
import play.data.*;
import play.mvc.*;
import views.html.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import ix.ncats.models.Grant;
import ix.ncats.controllers.GrantXmlParser;
import ix.ncats.controllers.GrantAbstractXmlParser;
import ix.ncats.controllers.GrantPubXmlParser;
import ix.ncats.controllers.GrantFactory;
import ix.ncats.controllers.GrantListener;

public class Granite extends Controller {

    static Form<Grant> grantForm = Form.form(Grant.class);

    public static Result index() {
        //return ok(index.render("Your new application is ready."));
        //return ok ("Hello world");
        //return redirect (routes.Application.grants());
        return grantsHtml ();
    }

    public static Result grants (int top, int skip) {
        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(GrantFactory.page(top, skip)));
    }

    public static Result grantsHtml () {
        Http.Request req = request ();
        Map<String, String[]> q = req.queryString();
        for (Map.Entry<String, String[]> e : q.entrySet()) {
            for (String v : e.getValue()) {
                Logger.debug("\""+e.getKey()+"\": "+v);
            }
        }
        return ok (ix.ncats.views.html.granite.render
                   (GrantFactory.getRowCount(), grantForm));
    }

    public static Result newGrant () {
        Form<Grant> filled = grantForm.bindFromRequest();
        if (filled.hasErrors()) {
            return badRequest (ix.ncats.views.html.granite.render
                               (GrantFactory.getRowCount(), filled));
        }
        else {
            Grant g = filled.get();
            g.save();

            return redirect (ix.ncats.controllers.routes.Granite.index());
        }
    }

    public static Result deleteGrant (Long id) {
        GrantFactory.delete(id);
        return redirect (ix.ncats.controllers.routes.Granite.index());
    }

    public static Result filter () {
        if (!request().method().equalsIgnoreCase("POST")) {
            return badRequest ("Only POST is accepted!");
        }

        String content = request().getHeader("Content-Type");
        if (content == null || (content.indexOf("application/json") < 0
                                && content.indexOf("text/json") < 0)) {
            return badRequest ("Mime type \""+content+"\" not supported!");
        }

        try {
            JsonNode json = request().body().asJson();

            JsonNode top = json.get("top");
            JsonNode skip = json.get("skip");
            JsonNode query = json.get("query");
            List<Grant> results = GrantFactory.filter
                (query, top != null && !top.isNull() ? top.asInt() : 0, 
                 skip != null && !skip.isNull() ? skip.asInt() : 10);
            ObjectMapper mapper = new ObjectMapper ();
            return ok (mapper.valueToTree(results));
        }
        catch (Exception ex) {
            return internalServerError (ex.getMessage());
        }
    }

    static void loadGrants (File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        DigestInputStream dis = new DigestInputStream
            (new FileInputStream (file), md);
        /*
                byte[] buf = new byte[1024];
                long size = 0;
                for (int nb; (nb = dis.read(buf, 0, buf.length)) > 0; ) {
                    size += nb;
                }
                dis.close();
                */
        final GrantXmlParser parser = new GrantXmlParser ();
        parser.addGrantListener(new GrantListener () {
                int saved = 0;
                public void newGrant (Grant newg) {
                    //Logger.info("yeah.. new grant "+g.applicationId);
                    Grant g = GrantFactory.finder
                        .where().eq("applicationId", newg.applicationId)
                        .findUnique();
                    if (g == null) {
                        newg.save();
                        ++saved;
                    }

                    int count = parser.getCount();
                    if (count % 100 == 0) {
                        Logger.debug(count+" grants parsed; "
                                     +saved+" are saved!");
                    }
                }
            });
        parser.parse(dis);

        StringBuilder sb = new StringBuilder ();
        byte[] sha = md.digest(); 
        for (int i = 0; i < sha.length; ++i) {
            sb.append(String.format("%1$02x", sha[i] & 0xff));
        }
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, 
                   maxLength = 1000000 * 1024 * 1024)
    public static Result loadMeta () {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest ("File too large!");
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("File");
        if (part != null) {
            String name = part.getFilename();
            String content = part.getContentType();
                
            try {
                File file = part.getFile();
                Logger.debug("file="+name+" content="+content);

                if ("application/zip".equalsIgnoreCase(content)) {
                    byte[] buf = new byte[4096];

                    ZipFile zf = new ZipFile (file);
                    for (Enumeration en = zf.entries(); 
                         en.hasMoreElements(); ) {
                        ZipEntry ze = (ZipEntry)en.nextElement();
                        InputStream is = zf.getInputStream(ze);

                        File temp = File.createTempFile("inx", "xml");
                        Logger.debug("writing to temp "+temp+"...");
                        FileOutputStream fos = new FileOutputStream (temp);
                        for (int nb; (nb = is.read(buf, 0, buf.length)) > 0;) {
                            int i = 0, j = 0;
                            for (; i < nb; ++j) {
                                while (j < nb 
                                       && (buf[j] >= 0x20 
                                           || buf[j] == 0x09 
                                           || buf[j] == 0x0a
                                           || buf[j] == 0x0d)) 
                                    ++j;
                                int d = j - i;
                                if (d > 0) {
                                    fos.write(buf, i, d);
                                }
                                i += d+1;
                            }
                        }
                        fos.close();
                        Logger.debug("processing "+ze.getName()+"...");
                        loadGrants (temp);
                        temp.delete();
                    }
                    zf.close();
                }
                else {
                    loadGrants (file);
                }
                
                //return ok (sb.toString());
                return redirect
                    (ix.ncats.controllers.routes.Granite.index());
            }
            catch (Exception ex) {
                Logger.trace("Can't load file \""+name+"\"; "+content, ex);
                return internalServerError (ex.getMessage());
            }
        }
        return noContent ();
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, 
                   maxLength = 100 * 1024 * 1024)
    public static Result loadAbs () {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest ("File too large!");
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("File");
        if (part != null) {
            try {
                String name = part.getFilename();
                String content = part.getContentType();
                File file = part.getFile();
                
                MessageDigest md = MessageDigest.getInstance("SHA1");
                DigestInputStream dis = new DigestInputStream
                    (new FileInputStream (file), md);
                GrantAbstractXmlParser parser = new GrantAbstractXmlParser ();
                parser.parse(dis);

                Logger.info("file="+name
                            +"; content="+content
                            +"; count="+parser.getCount());

                return redirect (ix.ncats.controllers.routes.Granite.index());
            }
            catch (Exception ex) {
                return internalServerError (ex.getMessage());
            }
        }
        return noContent ();
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, 
                   maxLength = 100 * 1024 * 1024)
    public static Result loadPub () {
        if (request().body().isMaxSizeExceeded()) {
            return badRequest ("File too large!");
        }

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile("File");
        Logger.debug("Loading publications...");
        if (part != null) {
            try {
                String name = part.getFilename();
                String content = part.getContentType();
                File file = part.getFile();
                
                MessageDigest md = MessageDigest.getInstance("SHA1");
                DigestInputStream dis = new DigestInputStream
                    (new FileInputStream (file), md);
                GrantPubXmlParser parser = new GrantPubXmlParser ();
                parser.parse(dis);

                Logger.info("file="+name
                            +"; content="+content
                            +"; count="+parser.getCount());

                return redirect (ix.ncats.controllers.routes.Granite.index());
            }
            catch (Exception ex) {
                Logger.trace("Can't load publications", ex);
                return internalServerError (ex.getMessage());
            }
        }
        return noContent ();
    }

}
