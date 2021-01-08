package ix.core.validator;

import ix.core.util.CachedSupplier;
import ix.core.initializers.LoadValidatorInitializer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by katzelda on 5/7/18.
 */
public class ValidatorFactory {

    private static CachedSupplier<ValidatorFactory> instance = new CachedSupplier<>(()-> LoadValidatorInitializer.getInstance().newFactory());

    public static ValidatorFactory getInstance(){
        return instance.get();
    }


    private final Map<ValidatorPlugin, LoadValidatorInitializer.ValidatorConfig> plugins = new LinkedHashMap<>();
    public ValidatorFactory(List<LoadValidatorInitializer.ValidatorConfig> configs){
       for(LoadValidatorInitializer.ValidatorConfig conf : configs){
           try {
               ValidatorPlugin p  = (ValidatorPlugin) conf.newValidatorPlugin();
               plugins.put(p, conf);
           } catch (Exception e) {
               e.printStackTrace();
           }

       }
    }


    public <T> Validator<T> createValidatorFor(T newValue, T oldValue, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType){
        return plugins.entrySet().stream()
                .filter( e-> e.getValue().meetsFilterCriteria(newValue, methodType) && e.getKey().supports(newValue, oldValue, methodType))
                .map(e -> (Validator<T>) e.getKey())
//                .peek(v -> System.out.println("running validator : " + v))
                .reduce(Validator.emptyValid(), Validator::combine);
    }




}
