package ix.ginas.initializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Map;

import ix.core.initializers.Initializer;
import ix.core.util.ConfigHelper;
import ix.core.util.IOUtil;
import ix.ginas.models.converters.PlainStringConverter;
import ix.ginas.models.converters.StringConverter;
import ix.utils.Util;

import play.Application;
import play.Logger;


public class StringConverterInitializer implements Initializer {


    @Override
    public void onStart(Application app) {

        try {
            Class<?> converterClass = ConfigHelper.getClass("ix.name.converterClass");
            if(converterClass ==null){
                converterClass = PlainStringConverter.class;
            }
            StringConverter stringConverter = (StringConverter) converterClass.newInstance();
            Logger.debug("setting string converter to " + stringConverter);
            Util.setStringConverter(stringConverter);
        } catch (Throwable e) {
            throw new IllegalStateException("could not instantiate string converter", e);
        }
    }
}
