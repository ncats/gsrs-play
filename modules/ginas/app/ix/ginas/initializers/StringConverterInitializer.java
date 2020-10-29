package ix.ginas.initializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Map;

import ix.core.initializers.Initializer;
import ix.core.util.IOUtil;
import ix.ginas.models.converters.StringConverter;
import ix.utils.Util;

import play.Application;
import play.Logger;


public class StringConverterInitializer implements Initializer {

    private StringConverterConfig config = null;

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

    }

    @Override
    public Initializer initializeWith(Map<String, ?> m)
    {
        m.remove("class");
        config = mapper.get().convertValue(m, StringConverterConfig.class);
        return this;
    }

    @Override
    public void onStart(Application app) {
        if(config != null){
            try {
                StringConverter stringConverter = (StringConverter) config.converterClass.newInstance();
                Logger.debug("setting string converter to " + stringConverter);
                Util.setStringConverter(stringConverter);
            } catch (Throwable e) {
                throw new IllegalStateException("could not instantiate string converter", e);
            }
        }
    }
}
