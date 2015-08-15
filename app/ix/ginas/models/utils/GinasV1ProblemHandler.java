package ix.ginas.models.utils;

import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.ginas.models.Ginas;

import java.io.IOException;
import java.util.List;

import play.Logger;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

public class GinasV1ProblemHandler
        extends  DeserializationProblemHandler {
        public GinasV1ProblemHandler () {
        }
        
        public boolean handleUnknownProperty
            (DeserializationContext ctx, JsonParser parser,
             JsonDeserializer deser, Object bean, String property) {

            try {
                boolean parsed = true;
                if ("hash".equals(property)) {
                    Structure struc = (Structure)bean;
                    //Logger.debug("value: "+parser.getText());
                    struc.properties.add(new Keyword
                                         (Structure.H_LyChI_L4,
                                          parser.getText()));
                }
                else if ("references".equals(property)) {
                    //Logger.debug(property+": "+bean.getClass());
                    if (bean instanceof Structure) {
                        Structure struc = (Structure)bean;
                        parseReferences (parser, struc.properties);
                    }
                    else {
                        parsed = false;
                    }
                }
                else if ("count".equals(property)) {
                    if (bean instanceof Structure) {
                        // need to handle this.
                        parser.skipChildren();
                    }
                }
                else {
                    parsed = false;
                }

                if (!parsed) {
                    Logger.warn("Unknown property \""
                                +property+"\" while parsing "
                                +bean+"; skipping it..");
                    Logger.debug("Token: "+parser.getCurrentToken());
                    parser.skipChildren();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }

        int parseReferences (JsonParser parser, List<Value> refs)
            throws IOException {
            int nrefs = 0;
            if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                while (JsonToken.END_ARRAY != parser.nextToken()) {
                    String ref = parser.getValueAsString();
                    refs.add(new Keyword (Ginas.REFERENCE, ref));
                    ++nrefs;
                }
            }
            return nrefs;
        }
    }