package ix.core.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by katzelda on 9/14/16.
 */
public class FilteredPrintStream extends PrintStream {

    private AtomicBoolean enabled = new AtomicBoolean(true);

    private final PrintStream printStream;
    private final LinkedList<WriterFilter> filters = new LinkedList<>();


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
                        return;
                }

                consumer.accept(obj);

            }

        }
    }

    private synchronized void removeFilter(WriterFilter filter){
        //this is probably always the first filter...
        //but just incase do a remove( obj) instead of a pop
       this.filters.remove(filter);
    }

    public synchronized FilterSession newFilter(WriterFilter filter){
        Objects.requireNonNull(filter);
        this.filters.push(filter);
        return new FilterSessionImpl(filter);
    }


    public interface FilterSession extends Closeable{
        @Override
        void close();

        WriterFilter getFilter();


    }

    private class FilterSessionImpl implements FilterSession{

        private WriterFilter filter;

        public FilterSessionImpl(WriterFilter filter) {
            this.filter = filter;
        }

        @Override
        public void close() {
            removeFilter(filter);
        }

        @Override
        public WriterFilter getFilter() {
            return filter;
        }


    }
    @FunctionalInterface
    private interface ThrowingRunnable<T extends Throwable>{
        void run() throws T;
    }
    public interface WriterFilter extends Predicate<StackTraceElement> {
        default WriterFilter and(WriterFilter f){
            return (t)-> test(t) && f.test(t);
        }

        default WriterFilter or(WriterFilter f){
            return (t)-> test(t) || f.test(t);
        }

        default WriterFilter not(){
            return (t)-> !test(t);
        }
    }

    private  static class  MultipleFilterSession implements FilterSession{

        private List<FilterSession> sessions;

        public MultipleFilterSession( List<FilterSession> sessions){
            this.sessions = new ArrayList<>(sessions);
        }
        @Override
        public void close() {
            for(FilterSession s : sessions){
                s.close();
            }
        }

        @Override
        public WriterFilter getFilter() {
            return null;
        }
    }


}
