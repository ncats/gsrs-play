package ix.ginas.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nih.ncgc.chemical.Chemical;
import gov.nih.ncgc.chemical.ChemicalReader;
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

    private  Iterator<Chemical> iter;
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
                iter= null;
            }else {
                pis.unread(firstValue);
                ChemicalReader reader = ChemicalReader.DEFAULT_CHEMICAL_FACTORY().createChemicalReader();
                reader.load(pis);
                iter = reader.iterator();
            }
        } catch (Exception e) {
            e.printStackTrace();
            IOUtil.closeQuietly(pis);
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
        if(iter==null){
            return null;
        }
        if(!iter.hasNext()){
            return null;
        }
        Chemical c = iter.next();
        ChemicalSubstanceBuilder builder = new ChemicalSubstanceBuilder();
        builder.setStructure(c.export(Chemical.FORMAT_SDF));

        for(SubstanceProcessor<Chemical, ChemicalSubstanceBuilder> processor : processors){
            builder = processor.process(c, builder);
        }


        return builder.buildJson();
    }

    @Override
    public void close() {
        IOUtil.closeQuietly(is);
    }

    @Override
    public RecordExtractor<JsonNode> makeNewExtractor(InputStream is) {
        return new DefaultSdfLoader(is);
    }

    @Override
    public RecordTransformer getTransformer() {
        return new GinasUtils.GinasSubstanceTransformer(DefaultSubstanceValidator.BATCH_SUBSTANCE_VALIDATOR(GinasUtils.DEFAULT_BATCH_STRATEGY));
    }
}
