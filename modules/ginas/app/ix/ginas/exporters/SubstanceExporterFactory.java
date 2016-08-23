package ix.ginas.exporters;

import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;

/**
 * Created by katzelda on 8/23/16.
 */
public interface SubstanceExporterFactory {

    interface Parameters{

        OutputFormat getFormat();
    }

    class OutputFormat{
        private final String extension;
        private final String displayname;

        public OutputFormat(String extension, String displayname) {
            Objects.requireNonNull(extension);
            Objects.requireNonNull(displayname);

            this.extension = extension;
            this.displayname = displayname;
        }

        public String getExtension(){
            return extension;
        }

        public String getDisplayName(){
            return displayname;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OutputFormat that = (OutputFormat) o;

            if (!extension.equals(that.extension)) return false;
            return displayname.equals(that.displayname);

        }

        @Override
        public int hashCode() {
            int result = extension.hashCode();
            result = 31 * result + displayname.hashCode();
            return result;
        }
    }

    boolean supports(Parameters params);

    Set<OutputFormat> getSupportedFormats();

    Exporter<Substance> createNewExporter(OutputStream out, Parameters params) throws IOException;


}
