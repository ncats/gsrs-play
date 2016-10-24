package ix.ginas.indexers;

import ix.core.models.Group;
import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexableValue;
import ix.core.search.text.IndexableValueFromRaw;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by mandavag on 10/24/16.
 */
public class RecordAccessIndexValueMaker implements IndexValueMaker<Substance> {


    @Override
    public void createIndexableValues(Substance t, Consumer<IndexableValue> consumer) {

        makeRecordAccessLevelValues(t, consumer);
        makeDisplayNameValues(t, consumer);
       // makeDefinitionLevelAccessValues(t, consumer);
    }

    public void makeRecordAccessLevelValues(Substance t, Consumer<IndexableValue> consumer) {

        Set<Group> groups=t.getAccess();


        if(groups== null || groups.isEmpty())
        {
            consumer.accept(new IndexableValueFromRaw("Record Level Access", "public").dynamic());
        }
        else {
            for (Group grp: groups) {
                consumer.accept(new IndexableValueFromRaw("Record Level Access", grp.name).dynamic());
            }
        }
    }

    public void makeDisplayNameValues(Substance t, Consumer<IndexableValue> consumer) {

        Set<Group> groups= t.getDisplayName().map(n -> n.getAccess()).orElse(null);


        if(groups== null || groups.isEmpty())
        {
            consumer.accept(new IndexableValueFromRaw("Display Name Level Access ", "public").dynamic());
        }
        else {
            for (Group grp: groups) {
                consumer.accept(new IndexableValueFromRaw("Display Name Level Access ", grp.name).dynamic());
            }
        }
    }

/*    public void makeDefinitionLevelAccessValues(Substance t, Consumer<IndexableValue> consumer) {

        Set<Group> groups;

        if(t instanceof ChemicalSubstance){

            ChemicalSubstance chem = (ChemicalSubstance) t;
            groups = chem.structure.getAccess();
        }else {
            groups = t.getAccess();
        }

        if(groups== null || groups.isEmpty())
        {
            consumer.accept(new IndexableValueFromRaw("Definition Level Access", "public").dynamic());
            System.out.println("group Level public");
        }
        else {
            for (Group grp: groups) {
                consumer.accept(new IndexableValueFromRaw("Definition Level Access", grp.name).dynamic());
                System.out.println("group Level : " + grp.name);
            }
        }

    }*/

}
