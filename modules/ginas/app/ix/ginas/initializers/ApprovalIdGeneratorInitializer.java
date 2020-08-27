package ix.ginas.initializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import ix.core.initializers.Initializer;
import ix.core.util.IOUtil;
import ix.ginas.models.v1.Substance;
import ix.ginas.utils.DefaultApprovalIDGenerator;
import ix.ginas.utils.GinasUtils;
import ix.ginas.utils.NamedIdGenerator;
import play.Application;
import play.Logger;

import java.util.Map;

public class ApprovalIdGeneratorInitializer implements Initializer {
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
    public static class ApprovalIDGeneratorConfig{


        public Class generatorClass;
        /**
         * Parameters to set in your generator instance.
         * These string names must either be set via  setters
         * or with a JsonProperty annotated parameter in a JsonCreator annotated
         * constructor or factory method.
         */
        public Map<String, Object> parameters;

    }


    @Override
    public void onStart(Application app) {
        Object obj = app.configuration().getObject("ix.ginas.approvalIDGenerator");
        NamedIdGenerator<Substance,String> idGenerator;
        if(obj ==null){
            idGenerator = new DefaultApprovalIDGenerator("GID", 8,true, "GID-");
        }else {

            ApprovalIDGeneratorConfig config = mapper.get().convertValue(obj, ApprovalIDGeneratorConfig.class);
            if (config.parameters == null) {
                try {
                    idGenerator = (NamedIdGenerator<Substance, String>) config.generatorClass.newInstance();
                } catch (Throwable e) {
                    throw new IllegalStateException("could not instantiate Approval ID Generator" ,e);
                }
            } else {
                idGenerator = (NamedIdGenerator<Substance, String>) mapper.get().convertValue(config.parameters, config.generatorClass);
            }
        }
        Logger.debug("setting id generator to "+ idGenerator);
        GinasUtils.setApprovalIdGenerator(idGenerator);

    }
}
