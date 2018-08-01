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
import ix.core.validator.GinasProcessingMessage;
import gov.nih.ncgc.chemical.Chemical;

public class SdfExporter implements Exporter<Substance> {
    @FunctionalInterface
    public interface ChemicalModifier {
        void modify(Chemical c, Substance parentSubstance, List<GinasProcessingMessage> messages);
    }


    private static final ChemicalModifier NO_OP_MODIFIER = (c, s, messages) ->{};
    private final BufferedWriter out;

    private final ChemicalModifier modifier;

    public SdfExporter(OutputStream out, ChemicalModifier modifier){
        Objects.requireNonNull(out);
        Objects.requireNonNull(modifier);

        this.out = new BufferedWriter(new OutputStreamWriter(out));
        this.modifier  = modifier;

    }
    public SdfExporter(OutputStream out){
       this(out, NO_OP_MODIFIER);
    }

    public SdfExporter(File outputFile) throws IOException{
        this(new BufferedOutputStream(new FileOutputStream(outputFile)));
    }

    @Override
    public void export(Substance s) throws IOException {

        List<GinasProcessingMessage> warnings = new ArrayList<>();

        Chemical chem = s.toChemical( warnings::add);



        modifier.modify(chem, s, warnings);
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


}