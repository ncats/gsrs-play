package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.models.Edit;
import ix.core.util.EntityUtils;
import ix.ginas.models.v1.Substance;

import java.util.Optional;

public abstract class PersistedSubstanceProcessor implements EntityProcessor<Substance> {


    protected abstract void handleNewSubstance(Substance substance);

    protected abstract void handleUpdatedSubstance(Substance oldSubstance, Substance newSubstance);

    @Override
    public void postUpdate(Substance obj) throws FailProcessingException {
        fireNewOrUpdate(obj);
    }

    @Override
    public void postPersist(Substance obj) throws FailProcessingException {
        fireNewOrUpdate(obj);

    }



    public void fireNewOrUpdate(Substance obj) {
        int version;
        try {
            version = Integer.parseInt(obj.version);
        }catch(RuntimeException e){
            e.printStackTrace();
            throw e;
        }

        if(version ==1){
            handleNewSubstance(obj);
        }else{
            String oldVersion = Integer.toString(version -1);
            Optional<Edit> oldEdit = obj.getEdits().stream()
//                    .peek(e-> System.out.println("edit " + e + " has version "+ e.version))
                    .filter(e-> oldVersion.equals(e.version)).findAny();

            if(oldEdit.isPresent()){
                try{
                    Edit e = oldEdit.get();
                    Substance oldSubstance = (Substance) EntityUtils
                            .getEntityInfoFor(e.kind)
                            .fromJsonNode(e.getOldValueReference().rawJson());

                    handleUpdatedSubstance(oldSubstance, obj);
                }catch(Exception ex){
                    throw new IllegalArgumentException(ex);
                }
            }else{
//                System.out.println("no edit?");
                //no old edit and not version 1 ?
                //assume new substance ?
                handleNewSubstance(obj);
            }
        }
    }

}
