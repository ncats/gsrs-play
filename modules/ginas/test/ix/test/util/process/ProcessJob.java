package ix.test.util.process;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class ProcessJob extends FutureTask<Integer> {


    private final ProcessBuilderCallableWrapper wrapper;


    public ProcessJob(ProcessBuilder builder, Consumer<ProcessBuilder> preprocessor, Runnable postProcessor) {
        this( new ProcessBuilderCallableWrapper(builder, preprocessor, postProcessor));
    }
    private ProcessJob(ProcessBuilderCallableWrapper wrapper) {
        super(wrapper);
        this.wrapper = wrapper;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        super.cancel(mayInterruptIfRunning);
        if(mayInterruptIfRunning && wrapper.p.isAlive()){
            wrapper.p.destroyForcibly();
            return true;
        }
        return false;
    }

    private static class ProcessBuilderCallableWrapper implements Callable<Integer>{
        private final ProcessBuilder builder;

        private final Consumer<ProcessBuilder> consumer;
        private final Runnable postProcessor;

        private Process p;

        public ProcessBuilderCallableWrapper(ProcessBuilder builder, Consumer<ProcessBuilder> consumer, Runnable postProcessor) {
            this.builder = Objects.requireNonNull(builder);
            this.consumer = consumer;
            this.postProcessor = postProcessor;
        }

        @Override
        public Integer call() throws Exception {
            if(consumer !=null){
                consumer.accept(builder);
            }
            System.out.println("starting job " + builder.command());
            p = builder.start();
            try {
                return p.waitFor();
            }finally {
            if(postProcessor !=null){
                postProcessor.run();
            }
            }
        }
    }

}
