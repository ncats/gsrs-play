package ix.ncats.resolvers;

import ix.core.util.ConfigHelper;
import play.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.*;
import java.util.Properties;

import ix.core.models.Structure;
import ix.core.chem.StructureProcessor;

import chemaxon.formats.MolImporter;

public abstract class AbstractStructureResolver implements Resolver<Structure> {
    static final int MAX_TRIES = 5;
    
    protected String name;
    protected int maxtries = MAX_TRIES;
    protected int readTimeout = 5000; // read timeout 5s
    protected int connTimeout = 2000; // connect timeout 2s
    public static final boolean PROXY_ENABLED = ConfigHelper.getBoolean("ix.proxy.enabled",false);
    private static String PROXY_NAME;
    private static int PORT_NUMBER;

    //Static method to instantiate the parameter values for calling/connecting to the proxy server
    static {
        if (PROXY_ENABLED) {
            PROXY_NAME = ConfigHelper.getOrDefault("ix.proxy.name","domain name");
            PORT_NUMBER = ConfigHelper.getInt("ix.proxy.port",0);
        }
    }

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

                        // If the Proxy flag is enabled in the Config file, it connects to proxy or it wouldn't
                        Proxy proxy = null;
                        if(PROXY_ENABLED) {
                            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_NAME, PORT_NUMBER));
                            System.out.println("Checking for Config file values");
                            System.out.println("Proxy Name is :"+PROXY_NAME+"\n"+"Port Number is :"+PORT_NUMBER);
                        }
                        else
                        {
                            proxy = Proxy.NO_PROXY;
                        }
                        HttpURLConnection con = (HttpURLConnection)url.openConnection(proxy);
                        con.connect();

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
