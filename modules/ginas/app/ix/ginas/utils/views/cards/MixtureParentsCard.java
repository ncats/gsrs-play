package ix.ginas.utils.views.cards;

import ix.ginas.controllers.v1.SubstanceHierarchyFinder;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.Substance;
import play.twirl.api.Html;

public class MixtureParentsCard extends CollectionDetailCard<MixtureSubstance>{

    Substance s;

    public MixtureParentsCard(Substance s){
        super(SubstanceHierarchyFinder.getMixturesContaining(s), "Mixtures", "parentMix");
        this.s=s;

    }

    @Override
    public Html innerContent() {
        return ix.ginas.views.html.details.properties.mixtureparents.render(asList());
    }



}