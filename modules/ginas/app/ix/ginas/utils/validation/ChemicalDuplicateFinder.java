package ix.ginas.utils.validation;

import java.util.ArrayList;
import java.util.List;

import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Substance;

public class ChemicalDuplicateFinder implements DuplicateFinder<Substance> {

    /**
     * Currently uses the lychi keys for the duplicate matching
     */
    @Override
    public List<Substance> findPossibleDuplicatesFor(Substance sub) {
        int max=10;

        List<Substance> dupeList = new ArrayList<Substance>();
        if(sub instanceof ChemicalSubstance){
            ChemicalSubstance cs = (ChemicalSubstance)sub;
         // System.out.println("Dupe chack");
            String hash = cs.structure.getLychiv3Hash();
            
            dupeList = SubstanceFactory.finder.get()
                                              .where()
                                              .eq("structure.properties.term", hash)
                                              .setMaxRows(max)
                                              .findList();
            
            //
            if(dupeList.size()<max){
                String hash2 = cs.structure.getLychiv3Hash();
                dupeList.addAll(SubstanceFactory.finder.get()
                                            .where()
                                            .eq("moieties.structure.properties.term", hash2)
                                            .setMaxRows(max-dupeList.size())
                                            .findList());
            }
        }
        
        return dupeList;
    }
    
    public static ChemicalDuplicateFinder instance(){
        return new ChemicalDuplicateFinder();
    }

}
