package ix.ginas.exporters;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.Principal;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Global;

/**
 * Created by katzelda on 4/18/17.
 */
@JSONEntity(name = "metadata")
public class ExportMetaData {
    
    
    public String id = UUID.randomUUID().toString();
    
    public long numRecords;
    public Long totalRecotds=null;
    
    
    public String collectionId;
    public String originalQuery;
    public String username;
    public boolean publicOnly;
    public String extension;
    
    
    public String filename;
    
    public String displayfilename;
    
    


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    
    public void setDisplayFilename(String filename) {
        this.displayfilename = filename;
    }
    
    public String getDisplayFilename() {
        if(this.displayfilename!=null){
            return this.displayfilename;
        }else{
            return this.getFilename();
        }
    }



    public Long started,finished;

    public ExportMetaData(){}

    public ExportMetaData(String collectionId, String originalQuery, Principal principal, boolean publicOnly, String extension) {
        this.collectionId = collectionId;
        this.originalQuery = originalQuery;
        this.username = principal.username;
        this.publicOnly = publicOnly;
        this.extension = extension;
    }
    
    
    public boolean isComplete(){
        return this.finished!=null;
    }
    
    //TODO: move status info here for better details
    public String getStatus(){
        if(this.isComplete()){
            return "COMPLETE";
        }else{
            return "RUNNING";
        }
    }
    
    
    public String getSelf(){
        return Global.getHost() + ix.ginas.controllers.routes.GinasApp.getStatusFor(this.id).url();
    }
    
    
    public String getDownloadUrl(){
        if(this.isComplete()){
            return Global.getHost() + ix.ginas.controllers.routes.GinasApp.downloadExport(this.id).url();    
        }else{
            return null;
        }
        
    }
    
    
    
    

    public String getCollectionId() {
        return collectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExportMetaData that = (ExportMetaData) o;

        if (numRecords != that.numRecords) return false;
        if (publicOnly != that.publicOnly) return false;
        if (!collectionId.equals(that.collectionId)) return false;
        if (originalQuery != null ? !originalQuery.equals(that.originalQuery) : that.originalQuery != null)
            return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
        if (started != null ? !started.equals(that.started) : that.started != null) return false;
        return finished != null ? finished.equals(that.finished) : that.finished == null;

    }
    
    

    @Override
    public int hashCode() {
        long result = numRecords;
        result = 31 * result + collectionId.hashCode();
        result = 31 * result + (originalQuery != null ? originalQuery.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (publicOnly ? 1 : 0);
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (started != null ? started.hashCode() : 0);
        result = 31 * result + (finished != null ? finished.hashCode() : 0);
        return (int) result;
    }
}
