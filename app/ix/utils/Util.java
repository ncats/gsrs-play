package ix.utils;

import java.util.*;
import java.security.*;

import play.Logger;
import play.mvc.Http;

public class Util {
    public static String sha1Request (Http.Request req, String... params) {
        String path = req.method()+"/"+req.path();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(path.getBytes("utf8"));

            Set<String> uparams = new TreeSet<String>();
            for (String p : params) {
                uparams.add(p);
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
    
    private Util () {
    }
}
