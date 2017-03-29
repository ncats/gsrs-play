package ix.ginas.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Created by katzelda on 3/20/17.
 */
public class LocalExportWriter extends OutputStream {

    private volatile boolean success;

    private final OutputStream delegateOutputStream;

    private final GinasApp.ExportListener listener;

    public LocalExportWriter(OutputStream delegateOutputStream, GinasApp.ExportListener listener) {
        this.delegateOutputStream = Objects.requireNonNull(delegateOutputStream);
        this.listener = Objects.requireNonNull(listener);
    }

    @Override
    public void close() throws IOException {
        try {
            delegateOutputStream.close();
            listener.exportCompleted();
        } catch (IOException e) {
            listener.exportHalted(e);
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        wrap(()-> delegateOutputStream.flush());
    }

    @Override
    public void write(byte[] b) throws IOException {
        wrap(()->delegateOutputStream.write(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        wrap(()->delegateOutputStream.write(b, off, len));
    }

    @Override
    public void write(int b) throws IOException {
        wrap(()-> delegateOutputStream.write(b));
    }

    private void wrap(WriteWrapper wrapper) throws IOException{
        try {
            wrapper.write();
        } catch (IOException e) {
           listener.exportHalted(e);
            success =false;
            throw e;
        }
    }

    @FunctionalInterface
    private interface WriteWrapper{
        void write() throws IOException;
    }
}
