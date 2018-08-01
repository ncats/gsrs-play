package ix.ginas.utils.views.cards;

import ix.ginas.controllers.ViewType;
import ix.ginas.models.v1.Substance;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by katzelda on 4/26/18.
 */
public interface CardConsumer {

    void consumeCards(Substance s, Consumer<? super DetailCard> consumer);

    default CardConsumer filter(Predicate<Substance> predicate){

        return (sub, consumer)-> {
            if(predicate.test(sub)){
                consumeCards(sub,consumer);
            }
        };

    }
}
