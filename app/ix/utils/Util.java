package ix.utils;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.*;

import play.Logger;
import play.Play;
import play.mvc.Http;

public class Util {
    static public final String[] UserAgents = {
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10; rv:33.0) Gecko/20100101 Firefox/33.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A",
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36"
    };
    public static long TIME_RESOLUTION_MS=Play.application().configuration().getLong("ix.tokenexpiretime",(long)(3600*1000*24));
    
    static Random rand = new Random ();
    public static String randomUserAgent () {
        return UserAgents[rand.nextInt(UserAgents.length)];
    }

    public static String sha1 (Http.Request req) {
        return sha1 (req, (String[])null);
    }
    
    public static String sha1 (Http.Request req, String... params) {
        String path = req.method()+"/"+req.path();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(path.getBytes("utf8"));

            Set<String> uparams = new TreeSet<String>();
            if (params != null && params.length > 0) {
                for (String p : params) {
                    uparams.add(p);
                }
            }
            else {
                uparams.addAll(req.queryString().keySet());
            }

            Set<String> sorted = new TreeSet (req.queryString().keySet());
            for (String key : sorted) {
                if (uparams.contains(key)) {
                    String[] values = req.queryString().get(key);
                    if (values != null) {
                        Arrays.sort(values);
                        md.update(key.getBytes("utf8"));
                        for (String v : values)
                            md.update(v.getBytes("utf8"));
                    }
                }
            }

            return toHex (md.digest());
        }
        catch (Exception ex) {
            Logger.trace("Can't generate hash for request: "+req.uri(), ex);
        }
        return null;
    }

    public static String toHex (byte[] d) {
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < d.length; ++i)
            sb.append(String.format("%1$02x", d[i]& 0xff));
        return sb.toString();
    }

    public static String sha1 (String... values) {
        if (values == null)
            return null;
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            for (String v : values) {
                md.update(v.getBytes("utf8"));
            }
            return toHex (md.digest());
        }
        catch (Exception ex) {
            Logger.trace("Can't generate sha1 hash!", ex);
        }
        return null;
    }

    public static String URLEncode (String value) {
        try {
            String decode = URLDecoder.decode(value, "UTF-8");
            return decode.replaceAll("[\\s+]", "%20");
        }
        catch (Exception ex) {
            Logger.trace("Can't decode value: "+value, ex);
        }
        return value;
    }
    
    private Util () {
    }

    /**
     * Returns an uncompressed InputStream from possibly compressed one.
     * 
     * @param is
     * @param uncompressed
     * @return
     * @throws IOException
     */
    public static InputStream getUncompressedInputStream(InputStream is,
                                                         boolean[] uncompressed) throws IOException {
        InputStream retStream = new BufferedInputStream(is);
        // if(true)return retStream;
        retStream.mark(100);
        if (uncompressed != null) {
            uncompressed[0] = false;
        }
        try {
            ZipInputStream zis = new ZipInputStream(retStream);
            ZipEntry entry;
            boolean got = false;
            // while there are entries I process them
            while ((entry = zis.getNextEntry()) != null) {
                got = true;

                // entry.
                retStream = zis;
                break;
            }
            if (!got)
                throw new IllegalStateException("Oops");
        } catch (Exception ex) {
            retStream.reset();
            // try as gzip
            try {
                GZIPInputStream gzis = new GZIPInputStream(retStream);
                retStream = gzis;
            } catch (IOException e) {
                retStream.reset();
                if (uncompressed != null) {
                    uncompressed[0] = true;
                }
                // retStream = new FileInputStream (file);
            }
            // try as plain txt file
        }
        return retStream;

    }
    
    public static String encrypt(String clearTextPassword, String salt) {
        String text = "---" + clearTextPassword + "---" + salt + "---";
        return Util.sha1(text);
    }
    
    public static String generateRandomString(int len){
    	String alpha="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwzyz";
    	String k="";
    	for(int i=0;i<len;i++){
    		int l=(int)(Math.random()*alpha.length());
    		k+=alpha.substring(l,l+1);
    	}
    	return k;
    }

    /**
     * Returns an uncompressed inputstream from possibly multiply compressed
     * stream
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public static InputStream getUncompressedInputStreamRecursive(InputStream is)
        throws IOException {
        boolean[] test = new boolean[1];
        test[0] = false;
        InputStream is2 = is;
        while (!test[0]) {
            is2 = getUncompressedInputStream(is2, test);
        }
        return is2;
    }
    
    public static long getCanonicalCacheTimeStamp(){
    	long TIMESTAMP=(new Date()).getTime();
    	long date=(long)(Math.floor(TIMESTAMP/getTimeResolutionMS()));
    	return date;
    }
    public static long getTimeResolutionMS(){
    	long timeresolution=3600*1000*24;
    	return timeresolution;
    }
}
