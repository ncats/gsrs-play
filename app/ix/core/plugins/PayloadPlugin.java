package ix.core.plugins;

import java.io.*;
import java.util.List;
import java.security.MessageDigest;
import java.security.DigestInputStream;
    
import play.Logger;
import play.Plugin;
import play.Application;
import play.mvc.Http;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;

import ix.core.models.Payload;
import ix.core.controllers.PayloadFactory;
import ix.utils.Util;

public class PayloadPlugin extends Plugin {
    private final Application app;
    private IxContext ctx;
    private File payload;

    public PayloadPlugin (Application app) {
        this.app = app;
    }

    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
        payload = ctx.payload();
    }

    public void onStop () {
        Logger.info("Plugin "+getClass().getName()+" stopped!");
    }

    public boolean enabled () { return true; }
    public Payload parseMultiPart (String field, Http.Request request)
        throws IOException {
        
        Http.MultipartFormData body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = body.getFile(field);
        if (part == null) {
            Logger.warn("Request is not multi-part!");
            return null;
        }
        
        Payload pl = null;
        Logger.debug("file="+part.getFilename()
                     +" content="+part.getContentType());

        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            File tmp = File.createTempFile("___", ".tmp", payload);
            FileOutputStream fos = new FileOutputStream (tmp);
            DigestInputStream dis = new DigestInputStream
                (new FileInputStream (part.getFile()), md);
            
            byte[] buf = new byte[2048];
            pl = new Payload ();            
            pl.size = 0l;
            for (int nb; (nb = dis.read(buf, 0, buf.length)) > 0; ) {
                fos.write(buf, 0, nb);
                pl.size += nb;
            }
            dis.close();
            fos.close();
            
            pl.sha1 = Util.toHex(md.digest());
            List<Payload> found =
                PayloadFactory.finder.where().eq("sha1", pl.sha1).findList();
            if (found.isEmpty()) {
                pl.name = part.getFilename();
                pl.mimeType = part.getContentType();
                
                pl.save();
                if (pl.id != null) {
                    tmp.renameTo(new File (payload, pl.id.toString()));
                }
                Logger.debug(pl.name+" => "+pl.id + " " +pl.sha1);
            }
            else {
                pl = found.iterator().next();
                Logger.debug("payload already loaded as "+pl.id);
            }
        }
        catch (Throwable t) {
            Logger.trace("Can't save payload", t);
            pl = null;
        }
        
        return pl;
    }

    public File getPayload (Payload pl) {
        File file = new File (payload, pl.id.toString());
        if (!file.exists()) {
            return null;
        }
        return file;
    }

    public InputStream getPayloadAsStream (Payload pl) {
        File file = getPayload (pl);
        if (file != null) {
            try {
                return new FileInputStream (file);
            }
            catch (IOException ex) {
                Logger.trace("Can't open file "+file, ex);
            }
        }
        return null;
    }
}
