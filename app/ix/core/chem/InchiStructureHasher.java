package ix.core.chem;

import gov.nih.ncats.molwitch.Chemical;
import gov.nih.ncats.molwitch.inchi.InChiResult;
import gov.nih.ncats.molwitch.inchi.Inchi;
import ix.core.models.Structure;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Created by katzelda on 7/2/19.
 */
public class InchiStructureHasher implements StructureHasher{

    @Override
    public void hash(Chemical chem, String mol, BiConsumer<String, String> keyValueConsumer) {
        try{
            InChiResult result = Inchi.asStdInchi(chem,true);
            String key = result.getKey();

            //replace all dashes with underscores for lucene searches
            int layerOffset = key.indexOf('-');
            String connectionOnly = key.substring(0, layerOffset);
            StringBuilder stringBuilder = new StringBuilder(key);
            stringBuilder.setCharAt(layerOffset, '_');
            stringBuilder.setCharAt(stringBuilder.length()-2, '_');
            String underscoredInsteadOfDash = stringBuilder.toString();
            keyValueConsumer.accept(Structure.H_InChI_Key, underscoredInsteadOfDash);
            keyValueConsumer.accept(Structure.H_EXACT_HASH, underscoredInsteadOfDash);
            keyValueConsumer.accept(Structure.H_STEREO_INSENSITIVE_HASH, connectionOnly);
        }catch(IOException e){
            e.printStackTrace();
            throw new IllegalArgumentException("error parsing mol",e);
        }
    }
}
