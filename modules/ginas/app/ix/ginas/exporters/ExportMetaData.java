package ix.ginas.exporters;

import ix.core.models.Principal;

import java.time.LocalDateTime;

/**
 * Created by katzelda on 4/18/17.
 */
public class ExportMetaData {
    public int numRecords;
    public final String collectionId;
    public final  String originalQuery;
    public final  Principal principal;
    public final  boolean publicOnly;
    public final  String extension;


    public LocalDateTime started,finished;

    public ExportMetaData(String collectionId, String originalQuery, Principal principal, boolean publicOnly, String extension) {
        this.collectionId = collectionId;
        this.originalQuery = originalQuery;
        this.principal = principal;
        this.publicOnly = publicOnly;
        this.extension = extension;
    }
}
