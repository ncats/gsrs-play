package ix.ginas.initializers;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import ix.core.initializers.Initializer;
import ix.core.util.IOUtil;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.validation.ValidatorFactory;
import ix.ginas.utils.validation.ValidatorPlugin;
import play.Application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 5/7/18.
 */
public class LoadValidatorInitializer implements Initializer{

    public static class ValidatorConfig{

        private static ThreadLocal<ObjectMapper> mapper = new ThreadLocal<ObjectMapper>(){
            @Override
            protected ObjectMapper initialValue() {
                ObjectMapper mapper= new ObjectMapper();
                TypeFactory tf = TypeFactory.defaultInstance()
                        .withClassLoader(IOUtil.getGinasClassLoader());
                mapper.setTypeFactory(tf);

                return mapper;

            }
        };
        private Class validatorClass;
        /**
         * Additional parameters to initialize in your instance returned by
         * {@link #getValidatorClass()}.
         */
        private Map<String, Object> parameters;
        private Class newObjClass;
        private Substance.SubstanceDefinitionType type;
        private METHOD_TYPE methodType;

        private Substance.SubstanceClass substanceClass;
        public enum METHOD_TYPE{

            CREATE,
            UPDATE,
            APPROVE,
            BATCH,
            IGNORE

            ;

            @JsonValue
            public String jsonValue(){
                return name();
            }
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public ValidatorConfig setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Class getValidatorClass() {
            return validatorClass;
        }

        public void setValidatorClass(Class validatorClass) {
            this.validatorClass = validatorClass;
        }

        public Class getNewObjClass() {
            return newObjClass;
        }

        public void setNewObjClass(Class newObjClass) {
            this.newObjClass = newObjClass;
        }

        public Substance.SubstanceDefinitionType getType() {
            return type;
        }

        public void setType(Substance.SubstanceDefinitionType type) {
            this.type = type;
        }

        public METHOD_TYPE getMethodType() {
            return methodType;
        }

        public void setMethodType(METHOD_TYPE methodType) {
            this.methodType = methodType;
        }

        public Substance.SubstanceClass getSubstanceClass() {
            return substanceClass;
        }

        public void setSubstanceClass(Substance.SubstanceClass substanceClass) {
            this.substanceClass = substanceClass;
        }

        public ValidatorPlugin newValidatorPlugin()  {

            if(parameters ==null){
                return (ValidatorPlugin) mapper.get().convertValue(Collections.emptyMap(), validatorClass);

            }
            return (ValidatorPlugin) mapper.get().convertValue(parameters, validatorClass);

        }
        public  <T> boolean meetsFilterCriteria(T obj, LoadValidatorInitializer.ValidatorConfig.METHOD_TYPE methodType){
            if(!newObjClass.isAssignableFrom(obj.getClass())){
                return false;
            }
            if(obj instanceof Substance){
                Substance s = (Substance) obj;
                if(substanceClass !=null && substanceClass != s.substanceClass){
                    return false;
                }
                if(type !=null && type != s.definitionType){
                    return false;
                }

            }
            if(methodType !=null && methodType != methodType){
                return false;
            }
            return true;
        }
    }
    private List<ValidatorConfig> configs;

    private static LoadValidatorInitializer instance;

    @Override
    public void onStart(Application app) {
        this.instance = this;
        List<?> list = app.configuration().getList("substance.validators");
        if(list == null){
            throw new IllegalStateException("substance validators must be specified in the config");
        }
        ObjectMapper mapper = new ObjectMapper();
        configs = list.stream()
                .map(m-> mapper.convertValue(m, ValidatorConfig.class))
                .collect(Collectors.toList());
    }



    public ValidatorFactory newFactory() {
        return new ValidatorFactory(configs);
    }

    public static LoadValidatorInitializer getInstance(){
        return instance;
    }
}
