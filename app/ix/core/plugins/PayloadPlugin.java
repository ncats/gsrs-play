package ix.core.plugins;

import ix.core.controllers.FileDataFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.models.FileData;
import ix.core.models.Payload;
import ix.utils.Global;
import ix.utils.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import play.Application;
import play.Logger;
import play.Play;
import play.Plugin;
import play.mvc.Http;

public class PayloadPlugin extends Plugin {
    private static final String IX_CORE_FILES_PERSIST_LOCATION = "ix.core.files.persist.location";
	private static final String PERSIST_LOCATION_DB = "<DB>";
	private static final String PERSIST_LOCATION_FILE = "<NULL>";
	
	
	private final Application app;
    private IxContext ctx;
    
    public enum PayloadPersistType{
    	TEMP,
    	PERM
    }
    private String storageLocation;
   

    public PayloadPlugin (Application app) {
        this.app = app;
        storageLocation = app
    			.configuration()
    			.getString(PayloadPlugin.IX_CORE_FILES_PERSIST_LOCATION,
    					PayloadPlugin.PERSIST_LOCATION_FILE);
    }

    public void onStart () {
        Logger.info("Loading plugin "+getClass().getName()+"...");
        ctx = app.plugin(IxContext.class);
        if (ctx == null)
            throw new IllegalStateException
                ("IxContext plugin is not loaded!");
    }

    public void onStop () {
        Logger.info("Plugin "+getClass().getName()+" stopped!");
    }

    public boolean enabled () { return true; }
    
    
    public Payload createPayload (String name, String mime, InputStream is, PayloadPersistType persistType)
        throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        File tmp = File.createTempFile("___", ".tmp", ctx.payload);
        FileOutputStream fos = new FileOutputStream (tmp);
        DigestInputStream dis = new DigestInputStream (is, md);
        
        byte[] buf = new byte[2048];
        Payload payload = new Payload ();            
        payload.size = 0l;
        for (int nb; (nb = dis.read(buf, 0, buf.length)) > 0; ) {
            fos.write(buf, 0, nb);
            payload.size += nb;
        }
        dis.close();
        fos.close();
        
        payload.sha1 = Util.toHex(md.digest());
        List<Payload> found =
            PayloadFactory.finder.where().eq("sha1", payload.sha1).findList();
        
        boolean save=true;
        if (!found.isEmpty()){
            payload = found.iterator().next();
            Logger.debug("payload already loaded as "+payload.id);
            try{
            	File f=PayloadFactory.getFile(payload);
            	if(!f.exists()){
            		Logger.error("Payload deleted");
            	}
            	save=false;
            }catch(Exception e){
            	Logger.debug(payload.name+" file not found");
            }
        }
        
        if (save) {
            payload.name = name;
            payload.mimeType = mime;
            
            payload.save();
            if (payload.id != null) {
                persistFile(tmp,payload,persistType);
            }
            Logger.debug(payload.name+" => "+payload.id + " " +payload.sha1);
        }else{
        	if(getPayloadFile(payload)==null){
        		 persistFile(tmp,payload,persistType);
        	}
        }
        
        
        return payload;
    }
    
    public String getUrlForPayload(Payload p){
    	return Global.getRef(p)+"?format=raw";
    }
    
    private File persistFile(File tmpFile, Payload payload, PayloadPersistType ptype){
    	//file system persist
    	File saveFile = new File (ctx.payload, payload.id.toString());
    	tmpFile.renameTo(saveFile);
    	
    	//database persist
    	if(ptype==PayloadPersistType.PERM){
			if(storageLocation.equals(PayloadPlugin.PERSIST_LOCATION_DB)){
		    	List<FileData> found =
		                FileDataFactory.finder.where().eq("sha1", payload.sha1).findList();
		    	if (found.isEmpty()){
			    	try {
			    		FileData fd = new FileData();
						fd.data=inputStreamToByteArray(getPayloadAsStream(payload));
						fd.mimeType=payload.mimeType;
						fd.sha1=payload.sha1;
						fd.size=payload.size;
						fd.save();
					} catch (IOException e) {
						e.printStackTrace();
					}
		    	}
    		}else if(storageLocation.equals(PayloadPlugin.PERSIST_LOCATION_FILE)){
    			//do nothing
    		}else{
    			File f = new File(storageLocation);
    			if(!f.exists()){
    				f.mkdirs();
    			}
    			File newLoc = new File(f,payload.id.toString());
    			if(!newLoc.exists()){
    				try {
						Files.copy(saveFile.toPath(),newLoc.toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
    			}
    		}
    	}
    	return saveFile;
    }
    private FileData getFileDataFromPayload(Payload payload){
    	List<FileData> found =
                FileDataFactory.finder.where().eq("sha1", payload.sha1).findList();
    	if(found.isEmpty()){
    		return null;
    	}
    	return found.iterator().next();
    }
    private File saveStreamLocal(Payload p, InputStream is) throws IOException, NoSuchAlgorithmException{
    	 File tmp = File.createTempFile("___", ".tmp", ctx.payload);
    	 MessageDigest md = MessageDigest.getInstance("SHA1");
    	 FileOutputStream fos = new FileOutputStream (tmp);
         DigestInputStream dis = new DigestInputStream (is, md);
         
         byte[] buf = new byte[2048];
         Payload payload = new Payload ();            
         payload.size = 0l;
         for (int nb; (nb = dis.read(buf, 0, buf.length)) > 0; ) {
             fos.write(buf, 0, nb);
             payload.size += nb;
         }
         dis.close();
         fos.close();
         payload.sha1 = Util.toHex(md.digest());
         if(!payload.sha1.equals(p.sha1)){
        	 Logger.warn("Recorded SHA1 different than computed SHA1");
         }
         return persistFile(tmp, p,PayloadPersistType.PERM);
    }

    public Payload createPayload (String name, String mime, byte[] content)
        throws Exception {
        return createPayload (name, mime, new ByteArrayInputStream (content), PayloadPersistType.TEMP);
    }

    public Payload createPayload (String name, String mime, String content)
        throws Exception {
        return createPayload (name, mime, content.getBytes("utf8"));
    }
    private byte[] inputStreamToByteArray(InputStream is) throws IOException{
    			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    			int nRead;
    			byte[] data = new byte[16384];

    			while ((nRead = is.read(data, 0, data.length)) != -1) {
    			  buffer.write(data, 0, nRead);
    			}

    			buffer.flush();

    			return buffer.toByteArray();
    }
    /**
     * Create a payload from a form submission. If there is no
     * multi-part data associated with that field, returns null.
     * 
     * The persistType gives a hint as to how the data is to
     * be persisted. PayloadPersistType.TEMP would imply that the
     * data is not expected to be used in a long term fashion, and
     * can be deleted from its persistence area after some time.
     * 
     * PayloadPersistType.PERM implies that the data is meant to be
     * kept until explicitly removed.
     * 
     * @param field
     * @param request
     * @param persistType
     * @return
     * @throws IOException
     */
    public Payload parseMultiPart (String field, Http.Request request, PayloadPersistType persistType)
        throws IOException {
        
        Http.MultipartFormData body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart part = null;    
        if (body != null) {
            part = body.getFile(field);
            if (part == null) {
                Logger.warn("Unable to parse field "
                            +field+" in multi-part request!");
                return null;
            }
        }
        else {
            Logger.warn("Request is not multi-part!");
            return null;
        }
        Logger.debug("file="+part.getFilename()
                     +" content="+part.getContentType());
        
        Payload payload = null;
        try {
            payload = createPayload (part.getFilename(),
                                     part.getContentType(),
                                     new FileInputStream (part.getFile()),persistType);
        }
        catch (Throwable t) {
            Logger.trace("Can't save payload", t);
        }
        
        return payload;
    }
    /**
     * By defualt, 
     * @param field
     * @param request
     * @return
     * @throws IOException
     */
    public Payload parseMultiPart (String field, Http.Request request)
            throws IOException {
            return parseMultiPart(field,request,PayloadPersistType.TEMP);
        }

    public File getPayloadFile (Payload pl) {
        File file = new File (ctx.payload, pl.id.toString());
        if (!file.exists()) {
        	if(storageLocation.equals(PayloadPlugin.PERSIST_LOCATION_DB)){
        		FileData fd=getFileDataFromPayload(pl);
            	if(fd!=null){
            		try{
            			file = saveStreamLocal(pl,new ByteArrayInputStream(fd.data));
            			return file;
            		}catch(Exception e){
            			Logger.warn("Error caching file:" + e.getMessage());
            		}
            	}
                return null;
    		}else if(storageLocation.equals(PayloadPlugin.PERSIST_LOCATION_FILE)){
    			return null;
    		}else{
    			File f = new File(storageLocation);
    			if(!f.exists()){
    				return null;
    			}
    			File newLoc = new File(f,pl.id.toString());
    			if(newLoc.exists()){
    				try{
	    				file = saveStreamLocal(pl,new FileInputStream(newLoc));
	        			return file;
    				}catch(Exception e){
    					e.printStackTrace();
    				}
    			}
    			return null;
    		}
        	
        }
        return file;
    }
    
    
    public InputStream getPayloadAsStream (Payload pl) {
        File file = getPayloadFile (pl);
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
    public InputStream getPayloadAsStreamUncompressed(Payload pl){
        InputStream is = getPayloadAsStream(pl);
        if(is==null)return null;
        try {
            return ix.utils.Util.getUncompressedInputStreamRecursive(is);
        } catch (IOException e) {
            Logger.trace("Problem uncompressing stream", e);
        }
        return null;
    }
}
