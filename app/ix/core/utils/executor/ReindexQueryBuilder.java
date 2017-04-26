package ix.core.utils.executor;

import com.avaje.ebean.QueryIterator;
import ix.core.models.Backup;
import ix.core.models.BackupEntity;
import ix.core.util.CloseableIterator;
import ix.core.util.CloseableIterators;
import play.db.ebean.Model;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by katzelda on 5/16/16.
 */
public class ReindexQueryBuilder {

    private Date beforeDate, afterDate;

    private Class<?> type;

    Model.Finder<Long,BackupEntity> finder = new Model.Finder(Long.class, BackupEntity.class);

    private Predicate<BackupEntity> filter;

    private static Predicate<BackupEntity> ALWAYS_ACCEPT =  (o)->true;

    public ReindexQueryBuilder filter(Predicate<BackupEntity> filter){
        this.filter = filter;
        return this;
    }

    public ReindexQueryBuilder after(Date date){
        if(date ==null){
            afterDate = null;
        }else {
            afterDate = new Date(date.getTime());
        }
        return this;
    }

    public ReindexQueryBuilder before(Date date){
        if(date ==null){
            beforeDate = null;
        }else {
            beforeDate = new Date(date.getTime());
        }
        return this;
    }

    public ReindexQueryBuilder ofType(Class<?> type){
        this.type = type;
        return this;
    }


    public ReindexQuery build(){

        Predicate<BackupEntity> combined;

        boolean hasNonTypeFilters =false;

        if(type ==null){
            combined = ALWAYS_ACCEPT;

        }else{
            combined =o-> o.isOfType(type);
        }
        if(afterDate !=null){
            combined = combined.or( o-> o.modified.after(afterDate));
            hasNonTypeFilters=true;
        }
        if(beforeDate !=null){
            combined = combined.or( o-> o.modified.before(beforeDate));
            hasNonTypeFilters=true;
        }
        if(filter !=null){
            combined = combined.or(filter);
            hasNonTypeFilters=true;
        }
        if(combined == ALWAYS_ACCEPT){
            //get all
            return new ReindexAllQuery(finder);
        }else if(hasNonTypeFilters) {
            return new PredicateReindexQuery(finder, combined);
        }
       // return new TypeSpecificReindexQuery(finder, combined);
        return null;
    }

    private static class ReindexAllQuery implements ReindexQuery {
        private final Model.Finder<Long, BackupEntity> finder;

        public ReindexAllQuery(Model.Finder<Long, BackupEntity> finder) {
            this.finder = finder;
        }

        @Override
        public CloseableIterator<BackupEntity> query(ProcessListener listener) {
            return CloseableIterators.wrap(finder.findIterate());
        }
    }

    private static class PredicateReindexQuery implements ReindexQuery {
        private final Model.Finder<Long, BackupEntity> finder;
        private final Predicate<BackupEntity> predicate;

        public PredicateReindexQuery(Model.Finder<Long, BackupEntity> finder,Predicate<BackupEntity> predicate) {
            this.finder = finder;
            this.predicate = predicate;
        }


        @Override
        public CloseableIterator<BackupEntity> query(ProcessListener listener) {
            Objects.requireNonNull(listener);

            return new CloseableIterator<BackupEntity>(){
                //TODO make finder query more efficient
                private QueryIterator<BackupEntity> iter = finder.findIterate();

                private Object done = new Object();

                private Object next;

                {
                    listener.totalRecordsToProcess(finder.findRowCount());
                    updateNext();
                }
                private void updateNext(){
                    int numSkipped =0;
                    while(iter.hasNext()){
                        BackupEntity o = iter.next();
                        if(predicate.test(o)){
                            next = o;
                            break;
                        }
                    }
                    if(numSkipped > 0) {
                        listener.countSkipped(numSkipped);
                    }
                    next = done;
                }

                @Override
                public boolean hasNext() {
                    return next != done;
                }

                @Override
                public BackupEntity next() {
                    if(!hasNext()){
                        throw new NoSuchElementException();
                    }
                    BackupEntity current = (BackupEntity) next;
                    updateNext();
                    return current;
                }

                @Override
                public void close() throws IOException {
                    next = done;
                    iter.close();
                }
            };
        }
    }
}
