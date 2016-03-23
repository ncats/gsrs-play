package ix.test.util;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by katzelda on 3/22/16.
 */
public class MultiThreadInteracter {


    public interface Step{
        void execute() throws Exception;
    }


    public static class Builder{
        private boolean canAddSteps=true;
        private final Set<ThreadBuilder> threadBuilders = new HashSet<>();
        public ThreadBuilder newThread(){
            if(!canAddSteps){
                throw new IllegalStateException("can not add new steps once build() is called");
            }
            ThreadBuilder builder = new ThreadBuilder();

            threadBuilders.add(builder);

            return builder;
        }

        public MultiThreadInteracter build(){
            canAddSteps=false;
            for(ThreadBuilder threadBuilder : threadBuilders){
                threadBuilder.init();
            }
            return new MultiThreadInteracter(this);
        }
    }

    public static class ThreadBuilder{

        private boolean canAddSteps=true;
        private final SortedMap<Integer, Step> steps = new TreeMap<>();

        public ThreadBuilder step(int stepNum, Step step){
            if(!canAddSteps){
                throw new IllegalStateException("can not add new steps once build() is called");
            }
           steps.put(stepNum, step);

            return this;
        }

        public void init() {
            canAddSteps=false;
        }
    }


    private int firstStep;
    private int lastStep;
    private final Builder builder;

    private MultiThreadInteracter(Builder builder){

        this.builder = builder;

        int firstStep = Integer.MAX_VALUE;
        int lastStep = Integer.MIN_VALUE;
        for(ThreadBuilder threadBuilder : builder.threadBuilders){
            firstStep = Math.min(firstStep, threadBuilder.steps.firstKey());
            lastStep = Math.max(lastStep, threadBuilder.steps.lastKey());

        }

    }

    public void run(){

        for(int i=firstStep; i<=lastStep; i++){

            List<Step> steps = new ArrayList<>();
            Integer stepNum = Integer.valueOf(i);
            for(ThreadBuilder threadBuilder : builder.threadBuilders){
                //remove so we GC, don't need the step afterwards anyway
                Step step =threadBuilder.steps.remove(stepNum);
                if(step !=null){
                    steps.add(step);
                }
            }
            if(!steps.isEmpty()){
                runStep(i, steps);
            }


        }
    }

    private void runStep(int stepNum, List<Step> steps) {
        final CountDownLatch latch = new CountDownLatch(steps.size());
        final List<Exception> exceptions = new ArrayList<>();
        for(final Step s : steps) {
            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        latch.await();

                        s.execute();

                    } catch (InterruptedException ignored) {

                    } catch (Exception e) {
                        exceptions.add(e);
                    }

                }
            };
            t.start();
        }

        if(!exceptions.isEmpty()){
            IllegalStateException ex = new IllegalStateException("error running step # " + stepNum);
            for(Exception e : exceptions){
                ex.addSuppressed(e);
            }

            throw ex;
        }

    }


}
