package ix.ginas.models.v1;

import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.KeywordListDeserializer;

public class TagListDeserializer extends KeywordListDeserializer {
    public TagListDeserializer () {
        super (GinasCommonSubData.TAG);
    }
}
