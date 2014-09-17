package crosstalk.ncats.controllers;

import java.io.*;
import java.security.*;
import java.util.*;

import play.*;
import play.data.*;
import play.mvc.*;
import views.html.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import crosstalk.ncats.models.Grant;
import crosstalk.ncats.controllers.GrantXmlParser;
import crosstalk.ncats.controllers.GrantAbstractXmlParser;
import crosstalk.ncats.controllers.GrantPubXmlParser;
import crosstalk.ncats.controllers.GrantFactory;
import crosstalk.ncats.controllers.GrantListener;

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
        return ok (crosstalk.ncats.views.html.granite.render(GrantFactory.all(), grantForm));
    }

    public static Result newGrant () {
        Form<Grant> filled = grantForm.bindFromRequest();
        if (filled.hasErrors()) {
            return badRequest (crosstalk.ncats.views.html.granite.render
                               (GrantFactory.all(), filled));
        }
        else {
            Grant g = filled.get();
            g.save();

            return redirect (crosstalk.ncats.controllers.routes.Granite.index());
        }
    }

    public static Result deleteGrant (Long id) {
        GrantFactory.delete(id);
        return redirect (crosstalk.ncats.controllers.routes.Granite.index());
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
            try {
                String name = part.getFilename();
                String content = part.getContentType();
                File file = part.getFile();
                
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
                        public void newGrant (Grant g) {
                            //Logger.info("yeah.. new grant "+g.applicationId);
                            int count = parser.getCount();
                            if (count % 100 == 0) {
                                Logger.debug(count+" grants loaded!");
                            }
                            g.save();
                        }
                    });
                parser.parse(dis);

                Logger.info("file="+name+"; content="+content);
                
                StringBuilder sb = new StringBuilder ();
                byte[] sha = md.digest(); 
                for (int i = 0; i < sha.length; ++i) {
                    sb.append(String.format("%1$02x", sha[i] & 0xff));
                }
                
                //return ok (sb.toString());
                return redirect (crosstalk.ncats.controllers.routes.Granite.index());
            }
            catch (Exception ex) {
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

                return redirect (crosstalk.ncats.controllers.routes.Granite.index());
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

                return redirect (crosstalk.ncats.controllers.routes.Granite.index());
            }
            catch (Exception ex) {
                return internalServerError (ex.getMessage());
            }
        }
        return noContent ();
    }

}
