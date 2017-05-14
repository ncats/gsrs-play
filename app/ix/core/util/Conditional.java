package ix.core.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import ix.utils.Tuple;

public class Conditional<T> {
    
    Predicate<T> pred = null;
    Consumer<T> copAccept = (t)->{};
    Consumer<T> copReject = (t)->{};
    
    private Conditional(Predicate<T> pred){
        this.pred=pred;
    }
    
    public T test(T t){
        return get0(t).k();
    }
    
    public InstantiatedConditional<T> with(T t){
        InstantiatedConditional<T> ic = new InstantiatedConditional<T>();
        ic.c=this;
        ic.t=t;
        return ic;
    }
    
    private Tuple<T,Boolean> get0(T t){
        boolean passed=pred.test(t);
        if(passed){
            copAccept.accept(t);
        }else{
            copReject.accept(t);
        }
        return Tuple.of(t,passed);
    }
    
    public Conditional<T> and(Predicate<T> pred){
        this.pred=this.pred.and(pred);
        return this;
    }
    
    public Conditional<T> ifTrue(Consumer<T> cons){
        copAccept=copAccept.andThen(cons);
        return this;
    }
    
    public Conditional<T> ifFalse(Consumer<T> cons){
        copReject=copReject.andThen(cons);
        return this;
    }
    
    public static <T> Conditional<T> of(Predicate<T> pred){
        return new Conditional<T>(pred);
    }
    
    public static class InstantiatedConditional<T>{
        private Conditional<T> c;
        private T t;
        
        public InstantiatedConditional<T> and(Predicate<T> pred) {
            c.and(pred);
            return this;
        }

        public InstantiatedConditional<T> ifTrue(Consumer<T> cons) {
            c.ifTrue(cons);
            return this;
        }

        public InstantiatedConditional<T> ifFalse(Consumer<T> cons) {
            c.ifFalse(cons);
            return this;
        }
        
        public T test(){
            return c.test(t);
        }
    }

}
