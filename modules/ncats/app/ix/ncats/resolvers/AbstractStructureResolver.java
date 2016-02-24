package ix.ncats.resolvers;

import play.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import ix.core.models.Structure;
import ix.core.chem.StructureProcessor;

import chemaxon.formats.MolImporter;

public abstract class AbstractStructureResolver implements Resolver<Structure> {
    static final int MAX_TRIES = 5;
    
    protected String name;
    protected int maxtries = MAX_TRIES;
    protected int readTimeout = 5000; // read timeout 5s
    protected int connTimeout = 2000; // connect timeout 2s

    protected AbstractStructureResolver (String name) {
        if (name == null)
            throw new IllegalArgumentException ("Invalid resolver name: "+name);
        this.name = name;
        
    }

    public String getName () { return name; }
    public Class<Structure> getType () { return Structure.class; }
    
    public void setMaxTries (int tries) { maxtries = tries; }
    public int getMaxTries () { return maxtries; }
    public void setReadTimeout (int timeout) { readTimeout = timeout; }
    public int getReadTimeout () { return readTimeout; }
    public void setConnectTimeout (int timeout) { connTimeout = timeout; }
    public int getConnectTimeout () { return connTimeout; }

    protected Structure resolve (InputStream is) throws IOException {
        MolImporter mi = new MolImporter (is);
        try {
            Structure struc =
                StructureProcessor.instrument(mi.read(), null, true);
            struc.save();
            return struc;
        }
        finally {
            mi.close();
        }
    }

    protected abstract URL[] resolvers (String name)
        throws MalformedURLException;

    public Structure resolve (String name) {
        try {
            URL[] urls = resolvers (name);        
            for (int tries = 0; tries < maxtries; ++tries) {
                try {
                    for (URL url : urls) {
                        HttpURLConnection con =
                            (HttpURLConnection)url.openConnection();
                        con.setReadTimeout(readTimeout);
                        con.setConnectTimeout(connTimeout);
                        
                        int status = con.getResponseCode();
                        Logger.debug("Resolving "+url+"..."+status);
                        if (status == HttpURLConnection.HTTP_NOT_FOUND)
                            return null;

                        return resolve (con.getInputStream());
                    }
                }
                catch (Exception ex) {
                    Logger.warn("Fail to resolve \""+name+"\"; "
                                +tries+"/"+maxtries+" attempts");
                    Thread.sleep(500); // 
                }
            }
        }
        catch (Exception ex) {
            Logger.error("Fail to resolve \""+name+"\"", ex);
        }
        return null;
    }
}
