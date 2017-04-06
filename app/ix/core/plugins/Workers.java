package ix.core.plugins;

import play.Application;
import play.Plugin;
import play.core.j.HttpExecutionContext;
import play.libs.F;
import play.libs.HttpExecution;
import scala.concurrent.ExecutionContext;

import java.util.function.Predicate;

/**
 * Created by katzelda on 3/28/17.
 */
public class Workers extends Plugin{

    public Workers (Application app) {
    }

    @Override
    public void onStart() {

//        cpuIntensive = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.expensive-cpu-operations"));
//        readOnlyExecutionContext = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.simple-db-lookups"));
//        expensiveReadOnlyExecutionContext = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.expensive-db-lookups"));
//
//        writeExecutionContext = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.db-write-operations"));
//        instance=this;
    }

    @Override
    public void onStop() {
       //TODO how do we stop our executor?
//        instance =null;
    }

    @Override
    public boolean enabled() {
       return true;
    }



    public static final class Worker<A> {


        private final F.Promise<A> promise;

        private final ExecutionContext context;

        private Worker(F.Promise<A> promise, ExecutionContext context) {
            this.promise = promise;
            this.context = context;
        }


        public F.Promise<A> toPromise(){
            return promise;
        }


        public <T> Worker<T> andThen(F.Function<? super A, T> function){
            return new Worker<T>(promise.map(function, context), context);
        }

        public <T> Worker<T> andThen(WorkerPool workerPool, F.Function<? super A, T> function){
            ExecutionContext myEc = HttpExecution.fromThread(workerPool.context);
            return new Worker<T>(promise.map(function, myEc), myEc);
        }


    }


    public enum WorkerPool{
        DB_SIMPLE_READ_ONLY("contexts.simple-db-lookups"),
        DB_EXPENSIVE_READ_ONLY("contexts.expensive-cpu-operations"),
        DB_WRITE("contexts.db-write-operations"),
        CPU_INTENSIVE("contexts.expensive-cpu-operations")
        ;

        private ExecutionContext context;

        WorkerPool(String contextLookupId){
            context =  HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup(contextLookupId));
        }

        public <T> Worker<T> newJob(F.Function0<T> function){
            ExecutionContext myEc = HttpExecution.fromThread(context);
            return new Worker( F.Promise.promise(function, myEc), myEc);
        }

    }

}
