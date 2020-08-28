package ix.ginas.initializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import ix.core.initializers.Initializer;
import ix.core.util.IOUtil;
import ix.ginas.models.converters.PlainStringConverter;
import ix.ginas.models.converters.StringConverter;
import ix.utils.Util;
import play.Application;
import play.Logger;

import java.util.Map;

public class StringConverterInitializer implements Initializer {
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
    public static class StringConverterConfig{


        public Class converterClass;
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
        Object obj = app.configuration().getObject("ix.ginas.stringConverter");
        if(obj != null){
            StringConverter stringConverter;
            StringConverterConfig config = mapper.get().convertValue(obj, StringConverterConfig.class);
            if (config.parameters == null) {
                try {
                    stringConverter = (StringConverter) config.converterClass.newInstance();
                } catch (Throwable e) {
                    throw new IllegalStateException("could not instantiate string converter", e);
                }
            } else {
                stringConverter = (StringConverter) mapper.get().convertValue(config.parameters, config.converterClass);
            }
            Logger.debug("setting string converter to " + stringConverter);
            Util.setStringConverter(stringConverter);
        }
    }
}
