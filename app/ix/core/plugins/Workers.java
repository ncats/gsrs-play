package ix.core.plugins;

import play.Application;
import play.Plugin;
import play.libs.F;
import play.libs.HttpExecution;
import scala.concurrent.ExecutionContext;

/**
 * Created by katzelda on 3/28/17.
 */
public class Workers extends Plugin{

    private ExecutionContext readOnlyExecutionContext, expensiveReadOnlyExecutionContext, writeExecutionContext, cpuIntensive;

    private static Workers instance;

    public static WorkerExecutionContext SIMPLE_READ_ONLY, EXPENSIVE_READ_ONLY, WRITE, CPU_INTENSIVE;
    public Workers (Application app) {
        System.out.println("HERE!!!!!! IN WORKER");
    }

    @Override
    public void onStart() {

        System.out.println("STARTING WORKERS!!!!!!");
        cpuIntensive = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.expensive-cpu-operations"));
        readOnlyExecutionContext = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.simple-db-lookups"));
        expensiveReadOnlyExecutionContext = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.expensive-db-lookups"));

        writeExecutionContext = HttpExecution.fromThread(play.libs.Akka.system().dispatchers().lookup("contexts.db-write-operations"));
        instance=this;
        SIMPLE_READ_ONLY = new ReadOnlyContext();
        EXPENSIVE_READ_ONLY = new ExpensiveReadOnlyContext();
        WRITE = new WriteContext();
        CPU_INTENSIVE = new CpuContext();

    }

    @Override
    public void onStop() {
       //TODO how do we stop our executor?
        instance =null;
    }

    @Override
    public boolean enabled() {
       return true;
    }



    public interface WorkerExecutionContext{


        <T>  F.Promise<T> promise(F.Function0<T> function);

        <T, A> F.Promise<A> andThen(F.Promise<T> before, F.Function<? super T, A> function);
    }

    private abstract class AbstractWorkerExecutionContext implements WorkerExecutionContext{
        abstract ExecutionContext getExecutionContext();

        @Override
        public <T, A> F.Promise<A> andThen(F.Promise<T> before, F.Function<? super T, A> function) {
            ExecutionContext myEc = HttpExecution.fromThread(getExecutionContext());
            return before.map(function, myEc);
        }
        @Override
        public <T> F.Promise<T> promise(F.Function0<T> function){
            return F.Promise.promise(function, HttpExecution.fromThread(getExecutionContext()));
        }



    }

    private class CpuContext extends AbstractWorkerExecutionContext{
        @Override
        ExecutionContext getExecutionContext() {
            return instance.cpuIntensive;
        }
    }
    private class ReadOnlyContext extends AbstractWorkerExecutionContext{
        @Override
        ExecutionContext getExecutionContext() {
            return instance.readOnlyExecutionContext;
        }
    }

    private class ExpensiveReadOnlyContext extends AbstractWorkerExecutionContext{
        @Override
        ExecutionContext getExecutionContext() {
            return instance.expensiveReadOnlyExecutionContext;
        }
    }

    private class WriteContext extends AbstractWorkerExecutionContext{
        @Override
        ExecutionContext getExecutionContext() {
            return instance.writeExecutionContext;
        }
    }
}
