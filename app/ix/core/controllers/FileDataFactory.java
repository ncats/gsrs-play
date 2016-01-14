package ix.core.controllers;

import ix.core.models.FileData;

import java.util.UUID;

import play.db.ebean.Model;
import play.mvc.Result;


public class FileDataFactory extends EntityFactory {
    public static final Model.Finder<UUID, FileData> finder = 
        new Model.Finder(UUID.class, FileData.class);
    

    public static FileData getFileData (UUID id) {
        return getEntity (id, finder);
    }
    
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result get (UUID id, String select) {
        return get (id, select, finder);
    }

    public static Result field (UUID id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        throw new UnsupportedOperationException
            ("create operation not supported!");
    }

    public static Result delete (UUID id) {
        throw new UnsupportedOperationException
            ("delete operation not supported!");
    }
}
