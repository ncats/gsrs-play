package ix.test.exporters;

import ix.ginas.exporters.ExportDir;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;

import static org.junit.Assert.*;
public class ExportDirTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private  ExportDir<MyMetaData> exportDir;

    @Before
    public void setup(){
        exportDir = new ExportDir(tmpDir.getRoot(), MyMetaData.class);
    }
    @Test
    public void putAndGetFileWithMetaData() throws IOException{
        MyMetaData expected = new MyMetaData("foo", "bar");
        ExportDir.ExportFile<MyMetaData> exportFile = exportDir.createFile("example", expected );

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exportFile.getBufferedOutputStream()))){
            writer.write("This is a test");
        }

        MyMetaData updated = exportFile.getMetaData().get();
        updated.bar="newBar";

        exportFile.saveMetaData(updated);

        assertEquals(updated, exportFile.getMetaData().get());

        File actualFile = exportFile.getFile();

        assertEquals(new File(tmpDir.getRoot(), "example").getAbsolutePath(), actualFile.getAbsolutePath());

        try(InputStream in = exportFile.getInputStreamOutputStream()) {
            assertEquals("This is a test", IOUtils.toString(in));
        }

    }

    @Test
    public void putAndGetFileNOMetaData() throws IOException{
        ExportDir.ExportFile<MyMetaData> exportFile = exportDir.createFile("example", null );

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exportFile.getBufferedOutputStream()))){
            writer.write("This is a test2");
        }

        assertFalse(exportFile.getMetaData().isPresent());

        File actualFile = exportFile.getFile();

        assertEquals(new File(tmpDir.getRoot(), "example").getAbsolutePath(), actualFile.getAbsolutePath());

        try(InputStream in = exportFile.getInputStreamOutputStream()) {
            assertEquals("This is a test2", IOUtils.toString(in));
        }

    }


    public static class MyMetaData{
        public String foo;
        public String bar;

        public MyMetaData(){}

        public MyMetaData(String foo, String bar) {
            this.foo = foo;
            this.bar = bar;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyMetaData that = (MyMetaData) o;

            if (foo != null ? !foo.equals(that.foo) : that.foo != null) return false;
            return bar != null ? bar.equals(that.bar) : that.bar == null;
        }

        @Override
        public int hashCode() {
            int result = foo != null ? foo.hashCode() : 0;
            result = 31 * result + (bar != null ? bar.hashCode() : 0);
            return result;
        }

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
        }
    }
}
