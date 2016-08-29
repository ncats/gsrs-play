package ix.ginas.exporters;

import ix.ginas.models.v1.Substance;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;

/**
 * Factory interface for making an {@link Exporter}
 * for {@link Substance}s.  The Factory can return
 * different implementations depending on the {@link Parameters}
 * passed in.
 *
 *
 * Created by katzelda on 8/23/16.
 */
public interface SubstanceExporterFactory {
    /**
     * Configuration Parameters to tell the factory
     * what export options to use.
     */
    interface Parameters{

        OutputFormat getFormat();
    }

    /**
     * The OutputFormat the exporter should be.
     *
     */
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

    /**
     * Can This factory make an Exporter that meets
     * these Parameter requirements.
     * @param params the {@link Parameters} to consider.
     * @return {@code true} if it does support those parameters;
     *      {@code false otherwise}.
     */
    boolean supports(Parameters params);

    /**
     * Get all the {@link OutputFormat}s that this factory
     * can support.
     * @return a Set of {@link OutputFormat}s; should never be null,
     * but could be empty.
     */
    Set<OutputFormat> getSupportedFormats();

    /**
     * Create a new {@link Exporter} using the given {@link Parameters} that will
     * write the export data to the given {@link OutputStream}.
     *
     * @param out the {@link OutputStream} to write to.
     * @param params the {@link Parameters} configuration to tune the Exporter.  These {@link Parameters}
     *               should always be supported.
     *
     * @return a new Exporter; should never be null.
     *
     * @throws IOException if there is a problem creating the Exporter.
     *
     * @see #supports(Parameters)
     */
    Exporter<Substance> createNewExporter(OutputStream out, Parameters params) throws IOException;


}
