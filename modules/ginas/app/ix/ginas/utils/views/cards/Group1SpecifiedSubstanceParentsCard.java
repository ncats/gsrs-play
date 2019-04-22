package ix.ginas.utils.views.cards;

import ix.ginas.controllers.v1.SubstanceHierarchyFinder;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

public class Group1SpecifiedSubstanceParentsCard extends CollectionDetailCard<SpecifiedSubstanceGroup1Substance>{

    Substance s;

    public Group1SpecifiedSubstanceParentsCard(Substance s){
        super(SubstanceHierarchyFinder.getG1SSContaining(s), "Specified Substances", "parentG1SS");
        this.s=s;

    }

    @Override
    public Html innerContent() {
        return ix.ginas.views.html.details.properties.group1specifiedsubstanceparents.render(asList());
    }



}
