package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.ginas.models.v1.Name;
import ix.utils.Util;

public class StdNameProcessor implements EntityProcessor<Name>{

    @Override
    public void prePersist(Name obj) {
        obj.setStdName(Util.getStringConverter().toStd(obj.getName()));
    }

    @Override
    public void preUpdate(Name obj) {
        prePersist(obj);
    }
}
