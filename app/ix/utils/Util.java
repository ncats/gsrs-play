package ix.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;

import ix.core.util.CachedSupplier;
import ix.core.util.TimeUtil;
import play.Logger;
import play.Play;
import play.db.ebean.Model;
import play.mvc.Http;

public class Util {
    static public final String[] UserAgents = {
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10; rv:33.0) Gecko/20100101 Firefox/33.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A",
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1944.0 Safari/537.36"
    };
    public static CachedSupplier<Long> TIME_RESOLUTION_MS=CachedSupplier.of(()->Play.application().configuration().getLong("ix.tokenexpiretime",(long)(3600*1000*24)));


    private static int BUFFER_SIZE = 8192; //8K

    static Random rand = new Random ();
    public static String randomUserAgent () {
        return UserAgents[rand.nextInt(UserAgents.length)];
    }


    /**
     * This method is only to get what the current stack trace would be for debugging
     * It returns a string which is essentially the stack trace of a thrown and caught
     * exception.
     * @return
     */
    public static String getExecutionPath(){
    	StringBuilder sb=new StringBuilder();
    	
    	try{
    		throw new Exception("");
    	}catch(Exception e){
    		StackTraceElement[] stes=e.getStackTrace();
    		for(int i=1;i<stes.length;i++){
    			StackTraceElement ste=stes[i];
    			sb.append("\n +" +ste.toString());
    		}
    	}
    	return sb.toString();
    }
    
    public static void printAllExecutingStackTraces(){
    	Thread.getAllStackTraces().entrySet().stream()
		.filter(e->Arrays.stream(e.getValue()).filter(s->s.getClassName().contains("ix.")).findAny().isPresent())
		.forEach(c->{
			for(StackTraceElement ste: c.getValue()){
				System.out.println(c.getKey() + "\t" + ste.toString());
			}
		});
    }
    
    public static void printExecutionStackTrace(){
    	System.out.println(getExecutionPath());
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
    public static String sha1 (byte[] bytes) {
        if (bytes == null)
            return null;
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            
            md.update(bytes);
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
    public static InputStream getUncompressedInputStream(
    		InputStream is,
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
    public static byte[] compress(byte[] data) throws IOException {  
    	   Deflater deflater = new Deflater();  
    	   deflater.setInput(data);  

    	   deflater.finish();
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer); // returns the generated code... index
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toByteArray();
        }

    }  
    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        if(data ==null ){
            return new byte[0];
        }
        Inflater inflater = new Inflater();

        inflater.setInput(data);

        //initialize to 10x the compressed size
        //I guess we can parse the compressed data to read the uncompressed size from the ZIP header
        //but this will be a faster approximation.
        //the buffer will grow as needed but if we make it too small,
        //then we will have several resizing operations which will cause the arry to get
        //copied over and over again.
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length *10)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }

            return outputStream.toByteArray();
        }
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
    	long TIMESTAMP= TimeUtil.getCurrentTimeMillis();
        return (long) Math.floor(TIMESTAMP/getTimeResolutionMS());
    }
    public static long getTimeResolutionMS(){
    	return TIME_RESOLUTION_MS.get().longValue();
    }
    
    
    public static InputStream getFile(String file, String path) throws Exception{
    	if(path==null)path="";
    	if(!Play.isProd()){
    		return new FileInputStream(Play.application().getFile(path + file));
    	}else{
    		return Play.application().resource(path + file).openStream();	
    	}
    }
    
    public static InputStream getFile(String file) throws Exception{
    	return getFile(file,null);
    }
    
    
    //only here for testing purposes
    public static void debugSpin(long milliseconds) {
    	if(milliseconds<=0)return;
    	if(Play.isProd())return;
        long sleepTime = milliseconds*1000000L; // convert to nanoseconds
        long startTime = System.nanoTime();
        while ((System.nanoTime() - startTime) < sleepTime) {} //Yes, it's pegging the CPU, that's intentional
    }
    
    public static byte[] serialize (Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        ObjectOutputStream oos = new ObjectOutputStream (bos);
        oos.writeObject(obj);
        oos.close();
        return bos.toByteArray();
    }
    

    public static void tryToDeleteRecursively(File dir) throws IOException {
        if(!dir.exists()){
            return;
        }
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {


            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                //we've have NFS problems where there are lock
                //objects that we can't delete
                //should be safe to keep them and delete every other file.
                if(		!file.toFile().getName().startsWith(".nfs")
                    //&& !file.toFile().getName().endsWith(".cfs")
                        ){
                    //use new delete method which throws IOException
                    //if it can't delete instead of returning flag
                    //so we will know the reason why it failed.
                    try{
                        //System.out.println("Deleting:" + file);
                        Files.delete(file);
                    }catch(IOException e){
                        System.out.println(e.getMessage());
                    }
                }
                else{
                    //System.out.println("found nfs file " + file.toString());
                }


                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                                                      IOException exc)
                    throws IOException{

                FileVisitResult fvr= super.postVisitDirectory(dir, exc);
                File dirfile=dir.toFile();
                try{
                    //System.out.println("Deleting:" + dirfile);
                    Files.delete(dir);
                }catch(IOException e){
                    System.out.println("unable to delete:" + e.getMessage());
                }
                return fvr;
            }

        });
    }

	public static String randvar (int size) {
	    Random rand = new Random ();
	    char[] alpha = {'a','b','c','d','e','f','g','h','i','j','k',
	                    'l','m','n','o','p','q','r','s','t','u','v',
	                    'x','y','z'};
	    StringBuilder sb = new StringBuilder ();
	    for (int i = 0; i < size; ++i)
	        sb.append(alpha[rand.nextInt(alpha.length)]);
	    return sb.toString();
	}

	public static String hashvar (int size, Object o) {
	    char[] alpha = {'a','b','c','d','e','f','g','h','i','j','k',
	                    'l','m','n','o','p','q','r','s','t','u','v',
	                    'x','y','z'};
	    
	    StringBuilder sb = new StringBuilder ();
	    int ohash=o.hashCode();
	    for (int i = 0; i < size; ++i){
	            int p=Math.abs((ohash%alpha.length));
	        sb.append(alpha[p]);
	        ohash+=(ohash+"").toString().hashCode();
	    }
	    return sb.toString();
	}

	public static String randvar () {
	    return randvar (5);
	}
	
	public static Object getAsNativeID(String idv){
    	if(idv.chars().allMatch( Character::isDigit )){
    		return new Long(Long.parseLong(idv));
    	}else{
    		return idv;
    	}
	}
	
	
	public static <K> void forEachIndex(Collection<K> it, BiConsumer<Integer,K> process){
		asIndexItemStream(it).forEach(t->process.accept(t.k(), t.v()));
	}
	public static <K> Stream<Tuple<Integer,K>> asIndexItemStream(Collection<K> l){
		List<K> src;
		if(l instanceof List){
			src=(List<K>) l;
		}else{
			src=l.stream().collect(Collectors.toList());
		}
		return IntStream
				.range(0,l.size())
				.mapToObj(i -> new Tuple<Integer,K>(i,src.get(i)));
	}

	
	public static <T> Collection<T> combine(Collection<T> c1, Collection<T> c2){
		c1.addAll(c2);
		return c1;
	}
	
	public static <T> Set<T> combine(Set<T> c1, Set<T> c2){
		c1.addAll(c2);
		return c1;
	}
	
	
	private static class CounterFunction<K> implements Function<K,Tuple<Integer,K>>{
		AtomicInteger count= new AtomicInteger();
		
		@Override
		public Tuple<Integer, K> apply(K k) {
			
			return new Tuple<Integer,K>(count.getAndIncrement(),k);
		}
	}
	
	public static <K> Function<K,Tuple<Integer,K>> toIndexedTuple(){
		return new CounterFunction<K>();
	}
	
	
	
	
	public static <K> Map<String,List<K>> groupToMap(Collection<K> s, Function<K,String> namer){
		return groupToMap(s.stream(),namer);
	}
	public static <K> Map<String,List<K>> groupToMap(Stream<K> s, Function<K,String> namer){
		return s.collect(Collectors.groupingBy(namer));
	}
	
	
	
	
	
	// Used for simple case-insensitive literal string replacement
	// From:http://stackoverflow.com/questions/5054995/how-to-replace-case-insensitive-literal-substrings-in-java
	public static String replaceIgnoreCase(String source, String target, String replacement) {
		StringBuilder sbSource = new StringBuilder(source);
		StringBuilder sbSourceLower = new StringBuilder(source.toLowerCase());
		String searchString = target.toLowerCase();

		int idx = 0;
		while ((idx = sbSourceLower.indexOf(searchString, idx)) != -1) {
			sbSource.replace(idx, idx + searchString.length(), replacement);
			sbSourceLower.replace(idx, idx + searchString.length(), replacement);
			idx += replacement.length();
		}
		sbSourceLower.setLength(0);
		sbSourceLower.trimToSize();
		sbSourceLower = null;

		return sbSource.toString();
	}
	
	
	public static <T> Comparator<T> comparitor(Stream<T> order){
		return comparitor(t->t, order);
	}
	
	public static <T, V> Comparator<V> comparitor(Function<V,T> namer,Stream<T> order){
		Map<T,Integer> mapOrder=order.map(toIndexedTuple())
								     .collect(Collectors.toMap(t->t.v(), t->t.k()));
		return (a,b)->{
			T k1=namer.apply(a);
			T k2=namer.apply(b);
			Integer i1=mapOrder.getOrDefault(k1, Integer.MAX_VALUE);
			Integer i2=mapOrder.getOrDefault(k2, Integer.MAX_VALUE);
			return Integer.compare(i1, i2);
		};
	}

	
	public static <I,T> CachedSupplier<Model.Finder<I,T>> finderFor(Class<I> cls, Class<T> vclass){
		return CachedSupplier.of(()->{
			return new Model.Finder<>(cls, vclass);
		});
	}

	
	public static <T,V>  Map<T,V> toMap(T t, V v){
		Map<T,V> toMap = new HashMap<T,V>();
		toMap.put(t, v);
		return toMap;
	}
	
	public static class MapBuilder<T,V>{
		private Map<T,V> _map = new HashMap<>();
		
		public MapBuilder<T,V> put(T t,V v){
			_map.put(t, v);
			return this;
		}
		
		public Map<T,V> build(){
			return this._map;
		}
		public static <T,V> MapBuilder<T,V> putNew(T t, V v){
			MapBuilder<T,V> mapBuilder= new MapBuilder<>();
			return mapBuilder.put(t, v);
		}
	}

	//And all expressions together
	//TODO: There has got to be a cleaner way
	public static Expression andAll(Expression... e) {
		Expression retExpr = e[0];
		for (Expression expr : e) {
			retExpr = com.avaje.ebean.Expr.and(retExpr, expr);
		}
		return retExpr;
	}


	//Or all expressions together
	//TODO: There has got to be a cleaner way
	public static Expression orAll(Expression... e) {
		Expression retExpr = e[0];
		for (Expression expr : e) {
			retExpr = com.avaje.ebean.Expr.or(retExpr, expr);
		}
		return retExpr;
	}
}
