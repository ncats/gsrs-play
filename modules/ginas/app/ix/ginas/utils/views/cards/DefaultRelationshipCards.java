package ix.ginas.utils.views.cards;

import ix.ginas.controllers.ViewType;
import ix.ginas.models.v1.Substance;

import java.util.function.Consumer;

/**
 * Created by katzelda on 4/26/18.
 */
public class DefaultRelationshipCards implements CardConsumer{

    @Override
    public void consumeCards(Substance s, Consumer<? super DetailCard> consumer) {
        consumer.accept(new RelationshipsCard(s));
        consumer.accept(new RelationshipsCard(s, "Metabolites", "metabolites", s.getMetabolites()));
        consumer.accept(new RelationshipsCard(s, "Impurities", "impurities", s.getImpurities()));
        consumer.accept(new RelationshipsCard(s, "Active Moiety", "activemoieties", s.getActiveMoieties()));

    }
}
