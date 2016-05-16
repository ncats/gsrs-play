package ix.core.controllers;

import ix.core.NamedResource;
import ix.core.controllers.EntityFactory.FetchOptions;
import ix.core.models.ProcessingJob;
import ix.core.models.ProcessingRecord;
import ix.core.util.Java8Util;
import ix.ginas.models.v1.Substance;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.avaje.ebean.FutureRowCount;

import play.db.ebean.Model;
import play.mvc.Result;

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
        return finder.setDistinct(false).where().eq("payload.id", uuid).findList();
    }
    public static List<ProcessingJob> getProcessingJobs
    (int top, int skip, String filter) {
	    return filter (new FetchOptions (top, skip, filter), finder);
	}

    public static ProcessingJob getJob (String key) {
    	//finder.setDistinct(false).where().eq("keys.term", key).findUnique();
    	
    	// This is because the built SQL for oracle includes a "DISTINCT"
    	// statement, which doesn't appear to be extractable.
    	List<ProcessingJob> gotJobsv= finder.findList();
    	for(ProcessingJob pj : gotJobsv){
    		if(pj.hasKey(key))return pj;
    	}
    	return null;
    }
    
    public static Integer getCount () 
            throws InterruptedException, ExecutionException {
            return ProcessingJobFactory.getCount(finder);
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
            return Java8Util.ok (getEntityMapper().valueToTree(getJobRecords (id)));
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

