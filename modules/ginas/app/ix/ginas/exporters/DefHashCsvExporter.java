package ix.ginas.exporters;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;

import ix.ginas.models.v1.Substance;
import play.Logger;

/*
Created 29 January 2021 as a test
Mitch Miller
 */
public class DefHashCsvExporter implements Exporter<Substance> {

    private final BufferedWriter out;

    public DefHashCsvExporter(OutputStream outInput) throws IOException{
        Logger.trace("DefHashCsvExporter constructor. out: "+ outInput);
        Objects.requireNonNull(outInput);
        Logger.trace("passed require");
        this.out = new BufferedWriter(new OutputStreamWriter(outInput));
        Logger.trace("set out");
    }

    @Override
    public void export(Substance substance) throws IOException {
        try {
            Logger.trace("DefHashCsvExporter.export");
            //need to write as a string because the json writer
            //will sometimes close the writer depending on which implementation it is...
            String line = String.format("%s\t%s\t%s\t%s", substance.getOrGenerateUUID().toString(),
                    substance.substanceClass.toString(),
                    substance.getDefHashString(), substance.getDefHashKeyString());
            Logger.trace(line);
            out.write(line);
            out.newLine();
        }
        catch (Exception ex) {
            Logger.error("Error during def hash export: " + ex.getMessage());
            ex.printStackTrace();
            throw  new IOException(ex.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
