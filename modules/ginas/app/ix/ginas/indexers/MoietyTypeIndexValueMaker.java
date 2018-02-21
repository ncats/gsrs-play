package ix.ginas.indexers;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import javassist.expr.Instanceof;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by VenkataSaiRa.Chavali on 4/20/2017.
 */
public class MoietyTypeIndexValueMaker implements IndexValueMaker<Substance> {

    @Override
    public void createIndexableValues(Substance substance, Consumer<IndexableValue> consumer) {
//        substance.getPreferredName().ifPresent( name -> {
//            substance.getActiveMoieties().stream()
//                    .filter(r -> r.relatedSubstance.refPname.equals(name.getName()))
//                    .findAny().ifPresent(r ->
//                    {
//                        System.out.println("Adding Active Moiety :" + substance.getBestId());
//                        consumer.accept(IndexableValue.simpleFacetStringValue("Moiety Type", "Active Moiety"));
//                    }
//            );
//        });

        Optional<Relationship> rel= substance.getActiveMoieties().stream()
                .filter(r -> r.relatedSubstance.refuuid.equals(substance.uuid.toString()))
                .findAny();

        if(rel.isPresent()) {
            consumer.accept(IndexableValue.simpleFacetStringValue("Moiety Type", "Active Moiety"));
            return;
        }


        if(substance instanceof ChemicalSubstance)
        {
            Integer charge = ((ChemicalSubstance)substance).structure.charge;
            if(null != charge && charge != 0)
            {
                consumer.accept(IndexableValue.simpleFacetStringValue("Moiety Type", "Ionic Moiety"));
                return;
            }

            if(((ChemicalSubstance)substance).structure.molfile.contains(" * ") ||
                    ((ChemicalSubstance)substance).structure.molfile.contains(" A ")
                    ){
                consumer.accept(IndexableValue.simpleFacetStringValue("Moiety Type", "Fragment Moiety"));
                return;
            }
        }

         rel = substance.getActiveMoieties().stream()
                .filter(r -> !(r.relatedSubstance.refuuid.equals(substance.uuid.toString())))
                .findAny();
        if(rel.isPresent()) {
            consumer.accept(IndexableValue.simpleFacetStringValue("Moiety Type", "Salt or Solvate"));
            return;
        }


    }
}