package ix.ginas.controllers.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.models.Group;
import ix.core.models.Predicate;
import ix.core.models.Principal;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.views.cards.CardConsumer;
import ix.ginas.utils.views.cards.CardViewFetcher;
import ix.ginas.utils.views.cards.DetailCard;
import play.Application;
import play.api.Plugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 4/25/18.
 */
public class CardViewPlugin implements Plugin {

    public static class CardConfig{
        /*
        {
                substanceClass : "ix.ginas.models.v1.Substance",
                cardClass : "ix.ginas.utils.views.cards.srs.ModificationsCard",
                viewType : "Substance"

        },
         */

        public Class substanceClass;
        public Class cardClass;
        public String viewType;
        public Substance.SubstanceClass substanceClassType;

        public Optional<CardConsumer> generateConsumer(Substance s){
            CardConsumer consumer;



                if(substanceClass !=null && !substanceClass.isInstance(s)){
                    return Optional.empty();
                }
                if(substanceClassType !=null && s.substanceClass != substanceClassType){
                    return Optional.empty();
                }
                 //TODO handle viewTypes for now everything is the same view anyway

            Class<? extends Substance> constructorArgType = substanceClass ==null ? Substance.class : substanceClass;
            try {
                return Optional.of((CardConsumer) MethodHandles.lookup().findConstructor(cardClass, MethodType.methodType(void.class, constructorArgType))
                        .invoke(s));
            }catch(NoSuchMethodException e2){
                try {
                    return Optional.of((CardConsumer) cardClass.newInstance());
                } catch (Throwable e) {
                    throw new IllegalStateException("could not instantiate card class from empty constructor for " + cardClass, e);
                }
            } catch (Throwable e ) {


                    throw new IllegalStateException("could not instantiate card class constructor for " + cardClass, e);

            }



        }
    }
    private final Application app;

    private static CardViewPlugin INSTANCE;

    private List<CardConfig> cardConfigs;

    public CardViewPlugin(Application app) {
        this.app = app;

    }
    @Override
    public boolean enabled() {
        return true;
    }

    public void onStart(){
        INSTANCE = this;
        ObjectMapper mapper = new ObjectMapper();
        String key = "cardView.substanceDetails";
        List<Map<String, Object>> objectList = app.configuration().getObjectList(key);
        if(objectList ==null){
            throw new IllegalStateException("card Views not set please set " + key);
        }
        cardConfigs = objectList
                                        .stream().sequential()
                                        .map(m-> mapper.convertValue(m, CardConfig.class))
                                        .collect(Collectors.toList());


    }

    @Override
    public void onStop() {
        cardConfigs.clear();
        INSTANCE=null;
    }

    public List<DetailCard> getDetailCardsFor(Substance s){
       //TODO add support for groups for current user
//Should not be necessary, but forces loading from database.
        //This really needs to be looked into
//        String cjson= EntityUtils.EntityWrapper.of(s).toCompactJson();

        List<DetailCard> cards = new ArrayList<>();
        cardConfigs.stream().map(cc -> cc.generateConsumer(s))
                                .filter(Optional::isPresent)
                                .forEach(consumer-> consumer.get().consumeCards(s, cards::add));


        cards.removeIf(c->!c.isVisble());
        return cards;
    }

    public static CardViewPlugin getInstance(){
        return INSTANCE;
    }
}
