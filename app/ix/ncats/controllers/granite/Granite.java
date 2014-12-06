package ix.ncats.controllers.granite;

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
import ix.core.models.Predicate;
import ix.core.models.XRef;
import ix.core.models.VInt;
import ix.core.models.VNum;
import ix.core.models.VStr;
import ix.core.models.Value;
import ix.core.models.VRange;
import ix.core.models.VBin;
import ix.core.models.VIntArray;

public class Granite extends Controller {

    static Form<Grant> grantForm = Form.form(Grant.class);

    static class DefaultGrantListener implements GrantListener {
        List<Grant> subjects = new ArrayList<Grant>();
        String[] predicates = new String[]{
            "ParentOf",
            "ChildOf",
            "MemberOf",
            "IsA",
            "RelatedTo",
            "NeighborOf",
            "InstanceOf",
            "InhibitorOf",
            "MetaboliteOf"
        };
        Random rand = new Random ();
        int saved;
        GrantXmlParser parser;

        DefaultGrantListener (GrantXmlParser parser) {
            this.parser = parser;
        }

        public void newGrant (Grant newg) {
            //Logger.info("yeah.. new grant "+g.applicationId);
            Grant g = GrantFactory.finder
                .where().eq("applicationId", newg.applicationId)
                .findUnique();
            if (g == null) {
                newg.save();
                randomPredicates (newg);
                ++saved;
            }

            int count = parser.getCount();
            if (count % 100 == 0) {
                Logger.debug(count+" grants parsed; "+saved+" are saved!");
            }
        }

        void randomPredicates (Grant g) {
            if (rand.nextDouble() < .5) {                
                if (!subjects.isEmpty()) {
                    XRef subject = new XRef (g);
                    subject.save();

                    // randomly create predicates
                    int np = rand.nextInt(predicates.length);
                    Set<String> preds = new HashSet<String>();
                    for (int i = 0; i < np; ++i) {
                        preds.add
                            (predicates[rand.nextInt
                                        (predicates.length)]);
                    }
                    Logger.debug(g.id +": generating "
                                 +preds.size()+" predicates...");
                    
                    for (String pred : preds) {
                        Predicate p = new Predicate (pred);
                        p.subject = subject;
                        addProperties (p.properties);
                        
                        int op = rand.nextInt(subjects.size());
                        BitSet bs = new BitSet (subjects.size());
                        for (int j = 0; j < op; ++j)
                            bs.set(rand.nextInt(subjects.size()));
                        
                        if (!bs.isEmpty()) {
                            for (int j = bs.nextSetBit(0); j>=0;
                                 j = bs.nextSetBit(j+1)) {
                                XRef ref = createXRef (subjects.get(j));
                                p.objects.add(ref);
                            }
                            p.save();
                            Logger.debug("..."+p.id+" " +p.predicate+" "
                                         +bs.cardinality() +" objects");
                        }
                    }
                }

                subjects.add(g);
            }
        }

        char[] alpha = { 'a','b','c','d','e','f','g','h','i','j','k','l','m',
                         'n','p','q','r','s','t','u','v','w','x','y','z'};
        String randStr () {
            int len = rand.nextInt(20);
            StringBuilder sb = new StringBuilder ();
            for (int i = 0; i < len; ++i) {
                sb.append(alpha[rand.nextInt(alpha.length)]);
            }
            return sb.toString();
        }

        XRef createXRef (Object obj) {
            XRef ref = new XRef (obj);
            addProperties (ref.properties);
            return ref;
        }

        void addProperties (List<Value> props) {
            if (rand.nextDouble() < .5)
                props.add(new VInt ("VInt", (long)rand.nextInt()));
            if (rand.nextInt(2) == 0)
                props.add(new VStr ("VStr", randStr ()));
            if (rand.nextInt(2) == 1)
                props.add(new VNum ("VNum", rand.nextDouble()));
            if (rand.nextInt(2) == 0) {
                int lval = rand.nextInt(1000);
                int rval = lval + rand.nextInt(Math.max(1, 1000-lval));
                VRange range = new VRange ("VRange", 
                                           (double)lval, (double)rval);
                range.average = (double)(lval+rand.nextInt
                                         (Math.max(1, rval-lval)));
                props.add(range);
            }
            if (rand.nextInt(2) == 1) {
                byte[] b = new byte[rand.nextInt(1024)];
                rand.nextBytes(b);
                VBin bin = new VBin ("VBin", b);
                props.add(bin);
            }
            if (rand.nextInt(2) == 1) {
                /*
                int[] ia = new int[rand.nextInt(1024)];
                for (int i = 0; i < ia.length; ++i)
                    ia[i] = rand.nextInt();
                */
                int[] ia = new int[]{1,2,3,4,5,6,7,9,9,8,7,6,5,4,3,2,1,0};
                props.add(new VIntArray ("VIntArray", ia));
            }
        }
    }

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
                   (GrantFactory.getCount(), grantForm));
    }

    public static Result newGrant () {
        Form<Grant> filled = grantForm.bindFromRequest();
        if (filled.hasErrors()) {
            return badRequest (ix.ncats.views.html.granite.render
                               (GrantFactory.getCount(), filled));
        }
        else {
            Grant g = filled.get();
            g.save();

            return redirect (routes.Granite.index());
        }
    }

    public static Result deleteGrant (Long id) {
        Logger.debug("Deleting grant "+id+"...");
        GrantFactory.delete(id);
        return redirect (routes.Granite.delete());
    }

    public static Result delete () {
        return ok (ix.ncats.views.html.granite2.render
                   (GrantFactory.getCount(), GrantFactory.filter(10, 0)));
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
        parser.addGrantListener(new DefaultGrantListener (parser));
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
                return redirect (routes.Granite.index());
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

                return redirect (routes.Granite.index());
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

                return redirect (routes.Granite.index());
            }
            catch (Exception ex) {
                Logger.trace("Can't load publications", ex);
                return internalServerError (ex.getMessage());
            }
        }
        return noContent ();
    }

}
