package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.models.Edit;
import ix.core.util.EntityUtils;
import ix.ginas.models.v1.Substance;

import java.util.Optional;

/**
 * Created by katzelda on 12/18/17.
 */
public abstract class AbstractPersistedSubstanceProcessor implements EntityProcessor<Substance> {
    @Override
    public void postPersist(Substance obj) throws FailProcessingException {
        int version = Integer.parseInt(obj.version);
        if(version ==1){
            handleNewSubstance(obj);
        }else{
            String oldVersion = Integer.toString(version -1);
            Optional<Edit> oldEdit = obj.getEdits().stream().filter(e-> oldVersion.equals(e.version)).findAny();
            if(oldEdit.isPresent()){
                try{
                    Edit e = oldEdit.get();
                    Substance oldSubstance = (Substance) EntityUtils
                            .getEntityInfoFor(e.kind)
                            .fromJsonNode(e.getOldValue().rawJson());

                    handleUpdatedSubstance(oldSubstance, obj);
                }catch(Exception ex){
                    throw new IllegalArgumentException(ex);
                }
            }else{
                //no old edit and not version 1 ?
                //assume new substance ?
                handleNewSubstance(obj);
            }
        }

    }

    protected abstract void handleUpdatedSubstance(Substance oldSubstance, Substance currentSubstance);

    protected abstract void handleNewSubstance(Substance obj);
}
