package ix.ginas.models.v1;

import ix.ginas.models.GinasSubData;
import ix.ginas.models.KeywordListDeserializer;

public class TagListDeserializer extends KeywordListDeserializer {
    public TagListDeserializer () {
        super (GinasSubData.TAG);
    }
}
