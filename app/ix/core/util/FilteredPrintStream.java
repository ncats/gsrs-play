package ix.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ix.core.plugins.ConsoleFilterPlugin;

/**
 * Created by katzelda on 9/14/16.
 */
public class FilteredPrintStream extends PrintStream {

    private AtomicBoolean enabled = new AtomicBoolean(true);

    private final PrintStream printStream;
    private final LinkedList<Filter> filters = new LinkedList<>();

    private Consumer<Object> onFilteredOut = null;

    public void setOnFilteredOut(Consumer<Object> onFilteredOut) {
        this.onFilteredOut = onFilteredOut;
    }



    public FilteredPrintStream(PrintStream printStream) {
        super(printStream, true);
        Objects.requireNonNull(printStream);
        this.printStream = printStream;
    }



    public void disableWriting(boolean disable){
        enableWriting(!disable);
    }

    public void enableWriting(boolean enable){
        enabled.compareAndSet(!enable, enable);
    }

    public void print(String s){
        handle(s, printStream::print);
    }
    public void print(Object o){
        handle(o, printStream::print);
    }

    public void println(String s){

       handle(s, printStream::println);
    }
    public void println(Object o){

        handle(o, printStream::println);
    }

    public void flush(){
        printStream.flush();
    }

    @Override
    public synchronized void close(){
        filters.clear();
    }
    @Override
    public void write(int b) {
        super.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
    }

    @Override
    public void print(boolean b) {
        handle(b, printStream::print);
    }

    @Override
    public void print(char c) {
        handle(c, printStream::print);
    }

    @Override
    public void print(int i) {
        handle(i, printStream::print);
    }

    @Override
    public void print(long l) {
        handle(l, printStream::print);
    }

    @Override
    public void print(float f) {
        handle(f, printStream::print);
    }

    @Override
    public void print(double d) {
        handle(d, printStream::print);
    }

    @Override
    public void print(char[] s) {
        handle(s, printStream::print);
    }

    @Override
    public void println() {
        handle(printStream::println);
    }

    @Override
    public void println(boolean x) {
        handle(x, printStream::println);
    }

    @Override
    public void println(char x) {
        handle(x, printStream::println);
    }

    @Override
    public void println(int x) {
        handle(x, printStream::println);
    }

    @Override
    public void println(long x) {
        handle(x, printStream::println);
    }

    @Override
    public void println(float x) {
        handle(x, printStream::println);
    }

    @Override
    public void println(double x) {
        handle(x, printStream::println);
    }

    @Override
    public void println(char[] x) {
        handle(x, printStream::println);
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        handle( ()-> printStream.printf(format, args));
        return this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        handle( ()-> printStream.printf(l, format, args));
        return this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        handle( ()-> printStream.format(format, args));
        return this;
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        handle( ()-> printStream.format(l, format, args));
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq) {
        handle( ()-> printStream.append(csq));
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        handle( ()-> printStream.append(csq, start, end));
        return this;
    }

    @Override
    public PrintStream append(char c) {
        handle( ()-> printStream.append(c));
        return this;
    }

    @Override
    public void write(byte[] b) throws IOException {
        handle(() -> printStream.write(b));
    }


    private <T extends Throwable> void handle(ThrowingRunnable<T> r) throws T{
        if(enabled.get()){
            synchronized (this){

                if(!filters.isEmpty() && !filters.peek().test(new Throwable().getStackTrace()[2])) {
                    //filtered out
                    
                    return;
                }
                r.run();
            }

        }
    }
    private <T> void handle(T obj, Consumer<T> consumer){
        if(enabled.get()){
            synchronized (this){
                StackTraceElement t = new Throwable().getStackTrace()[2];
                if(!filters.isEmpty() && !filters.peek().test(t)) {
                        //filtered out
                        if(onFilteredOut!=null){
                            onFilteredOut.accept(obj);
                        }
                        return;
                }
                consumer.accept(obj);
            }

        }
    }

    private synchronized void removeFilter(Filter filter){
        //this is probably always the first filter...
        //but just incase do a remove( obj) instead of a pop
       this.filters.remove(filter);
    }

    public synchronized FilterSession newFilter(Filter filter){
        Objects.requireNonNull(filter);
        this.filters.push(filter);
        return new FilterSessionImpl(filter);
    }


    public interface FilterSession extends Closeable{
        @Override
        void close();

        Filter getFilter();
        
        default FilterSession withOnSwallowed(Consumer<Object> co){
            throw new UnsupportedOperationException();
        }

    }

    private class FilterSessionImpl implements FilterSession{

        private Filter filter;
        private Consumer<Object> old = FilteredPrintStream.this.onFilteredOut;
        
        public FilterSessionImpl(Filter filter) {
            this.filter = filter;
        }

        @Override
        public void close() {
            removeFilter(filter);
            FilteredPrintStream.this.setOnFilteredOut(old);
        }

        @Override
        public Filter getFilter() {
            return filter;
        }
        
        public FilterSession withOnSwallowed(Consumer<Object> co){
            FilteredPrintStream.this.setOnFilteredOut(co);
            return this;
        }


    }
    @FunctionalInterface
    private interface ThrowingRunnable<T extends Throwable>{
        void run() throws T;
    }
    @FunctionalInterface
    public interface Filter {

        /**
         * Evaluates this predicate on the given argument.
         *
         * @param t the input argument
         * @return {@code true} if the input argument should pass the filter
         * and get printed out,
         * otherwise {@code false}
         */
        boolean test(StackTraceElement t);

        default Filter and(Filter f){
            return (t)-> test(t) && f.test(t);
        }

        default Filter or(Filter f){
            return (t)-> test(t) || f.test(t);
        }

        default Filter not(){
            return (t)-> !test(t);
        }
    }




}
