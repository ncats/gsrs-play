package ix.ginas.exporters;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ix.ginas.controllers.GinasApp;
import ix.ginas.models.v1.Substance;
import ix.core.GinasProcessingMessage;
import ix.ginas.utils.GinasUtils;
import gov.nih.ncgc.chemical.Chemical;

public class SdfExporter implements Exporter<Substance> {

    private final BufferedWriter out;

    public SdfExporter(OutputStream out){
        Objects.requireNonNull(out);
        this.out = new BufferedWriter(new OutputStreamWriter(out));
    }

    public SdfExporter(File outputFile) throws IOException{
        this(new BufferedOutputStream(new FileOutputStream(outputFile)));
    }

    @Override
    public void export(Substance s) throws IOException {
    	List<GinasProcessingMessage> messages = new ArrayList<GinasProcessingMessage>();
        Chemical chem = GinasUtils.substanceToChemical(s, messages);
        try {
            String content = GinasApp.formatMolfile(chem,Chemical.FORMAT_SDF);
            out.write(content);
            out.newLine();

        }catch(Exception e){
            throw new IOException("error exporting to sdf file", e);
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

	@Override
	public String getExtension() {
		return "sdf";
	}
}