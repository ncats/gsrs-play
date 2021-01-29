package ix.ginas.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.io.ChemicalReader;
import gov.nih.ncats.molwitch.io.ChemicalReaderFactory;
import gov.nih.ncats.molwitch.io.StandardChemFormats;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.PayloadFactory;
import ix.core.models.Payload;
import ix.core.processing.RecordExtractor;
import ix.core.processing.RecordTransformer;
import ix.core.stats.Estimate;
import ix.core.util.IOUtil;
import ix.ginas.modelBuilders.ChemicalSubstanceBuilder;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.utils.validation.DefaultSubstanceValidator;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DefaultSdfLoader extends RecordExtractor<JsonNode>{

    private List<SubstanceProcessor<Chemical, ChemicalSubstanceBuilder>> processors = Arrays.asList( new SdfProcessor());

    private ChemicalReader reader;

    public DefaultSdfLoader(InputStream is) {
        super(is);
        if(is==null){
            return;
        }
        PushbackInputStream pis = new PushbackInputStream(is);




        try {
            int firstValue = is.read();
            if(firstValue == -1){
                //empty
                reader= null;
            }else {
                pis.unread(firstValue);
                reader = ChemicalReaderFactory.newReader(StandardChemFormats.SDF, is);

            }
        } catch (Exception e) {
            e.printStackTrace();
            IOUtil.closeQuietly(reader);
        }
    }

    @Override
    public Estimate estimateRecordCount(Payload p) {
        InputStream pis= PayloadFactory.getStream(p);

        int count=0;
        //count number of '$$$$" which are record separators
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(pis))){
            String line;
            while( (line = reader.readLine()) !=null){
                if(line.startsWith("$$$$")){
                    count++;
                }
            }
        }catch(IOException e){
            return new Estimate(0, Estimate.TYPE.UNKNOWN);
        }

        //probably a mol file
        if(count==0){
            count =1;
        }
        return new Estimate(count, Estimate.TYPE.EXACT);
    }

    @Override
    public JsonNode getNextRecord() throws Exception {
        if(reader==null){
            return null;
        }
        if(!reader.canRead()){
            return null;
        }
        Chemical c = reader.read();
        ChemicalSubstanceBuilder builder = new ChemicalSubstanceBuilder();
        //TODO after discussion with Tyler we should maybe
        //change this to use the actual source text
        //vs exporting using a config parameter?

//        if(c.getSource().isPresent()){
//            builder.setStructure(c.getSource().get().getData());
//        }else {
//            builder.setStructure(c.toSd());
//        }
        builder.setStructure(c.toMol());

        for(SubstanceProcessor<Chemical, ChemicalSubstanceBuilder> processor : processors){
            builder = processor.process(c, builder);
        }


        return builder.buildJson();
    }

    @Override
    public void close() {
        IOUtil.closeQuietly(reader);
    }

    @Override
    public RecordExtractor<JsonNode> makeNewExtractor(InputStream is) {
        return new DefaultSdfLoader(is);
    }

    @Override
    public RecordTransformer getTransformer() {
        return new GinasUtils.GinasSubstanceTransformer(DefaultSubstanceValidator.BATCH_SUBSTANCE_VALIDATOR(GinasUtils.DEFAULT_BATCH_STRATEGY.get()));
    }
}
