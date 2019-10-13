package ix.ncats.resolvers;

import gov.nih.ncats.molwitch.Chemical;
import ix.core.util.ConfigHelper;
import play.Logger;

import java.io.*;
import java.net.*;
import java.util.Objects;

import ix.core.models.Structure;
import ix.core.chem.StructureProcessor;


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
        return resolve(is, "sdf");
    }
    protected Structure resolve (InputStream is, String format) throws IOException {
        StringBuilder builder = new StringBuilder();
        //katzelda - April 2018
        //sometimes we have an invalid mol or sdfile
        //jchem and cdk will silently just make an empty object
        //so check to make sure we actually got something right
        //so we read the record and scan it to make sure it's complete
        //if it's not don't even bother.

        boolean isValid= Objects.equals("smiles", format);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
            String line;
            while( (line = reader.readLine()) !=null){
                builder.append(line).append("\n");
                if(!isValid && line.startsWith("M END") || line.contains("$$$$")){
                    isValid = true;
                }
            }
        }

        if(!isValid){
            return null;
        }
            Structure struc =
                StructureProcessor.instrument(builder.toString(), null, true);
            //katzelda - April 2018
            //sometimes we have an invalid mol file
            //jchem and cdk will silently just make an empty object
            //so check to make sure we actually got something right
            Chemical chemical = struc.toChemical();

            if(chemical.getAtomCount() ==0 || chemical.getBondCount() ==0){
                System.out.println("atom or bond count was 0");
                return null;
            }

            struc.save();
            return struc;

    }

    protected abstract UrlAndFormat[] resolvers (String name)
        throws MalformedURLException;

    public Structure resolve (String name) {
        try {
            UrlAndFormat[] urls = resolvers (name);
            for (UrlAndFormat url : urls) {

                for (int tries = 0; tries < maxtries; ++tries) {
                    try {

                        // If the Proxy flag is enabled in the Config file, it connects to proxy or it wouldn't
                        Proxy proxy = null;
                        if (PROXY_ENABLED) {
                            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_NAME, PORT_NUMBER));
                            System.out.println("Checking for Config file values");
                            System.out.println("Proxy Name is :" + PROXY_NAME + "\n" + "Port Number is :" + PORT_NUMBER);
                        } else {
                            proxy = Proxy.NO_PROXY;
                        }
                        HttpURLConnection con = (HttpURLConnection) url.url.openConnection(proxy);
                        con.connect();

                        con.setReadTimeout(readTimeout);
                        con.setConnectTimeout(connTimeout);

                        int status = con.getResponseCode();
                        Logger.debug("Resolving " + url + "..." + status);
                        if (status == HttpURLConnection.HTTP_NOT_FOUND) {
                            break;
                        }
                        Structure s = resolve(con.getInputStream(), url.format);
                        if (s != null) {
                            return s;
                        }
                        //if we get this far something worked
                        break;

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Logger.warn("Fail to resolve \"" + name + "\"; "
                                + tries + "/" + maxtries + " attempts");
                        Thread.sleep(500); //
                    }
                }
            }
        }
        catch (Exception ex) {
            Logger.error("Fail to resolve \""+name+"\"", ex);
        }
        return null;
    }

    protected static class UrlAndFormat{
        public final URL url;
        public final String format;

        public UrlAndFormat(URL url, String format) {
            this.url = url;
            this.format = format;
        }
    }
}
