package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.NamedResource;

@NamedResource(name="jobs",
               type=ProcessingJob.class,
               description="Resource for handling processing jobs")
public class ProcessingJobFactory extends EntityFactory {
    public static final Model.Finder<Long, ProcessingJob> finder =
        new Model.Finder(Long.class, ProcessingJob.class);
    public static final Model.Finder<Long, ProcessingRecord> recordFinder =
        new Model.Finder(Long.class, ProcessingRecord.class);

    public static ProcessingJob getJob (Long id) {
        return getEntity (id, finder);
    }
    public static List<ProcessingRecord> getJobRecords (Long id) {
        return recordFinder.where().eq("job.id", id).findList();
    }

    public static List<ProcessingJob> getJobsByPayload (String uuid) {
        return finder.where().eq("payload.id", uuid).findList();
    }

    public static ProcessingJob getJob (String key) {
        return finder.where().eq("keys.term", key).findUnique();
    }
    
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        if (path.equals("records")) {
            return ok (getEntityMapper().valueToTree(getJobRecords (id)));
        }
        return field (id, path, finder);
    }

    public static Result create () {
        throw new UnsupportedOperationException
            ("create operation not supported!");
    }

    public static Result delete (Long id) {
        throw new UnsupportedOperationException
            ("delete operation not supported!");
    }
}

