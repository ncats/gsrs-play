package ix.ncats.services;

import ix.core.models.Structure;
import ix.core.services.RendererService;
import ix.ncats.controllers.App;

public enum DefaultRenderer implements RendererService {
     INSTANCE;
    @Override
    public byte[] render(Structure struc, String format, int size, int[] atomMap, Boolean showStereo) throws Exception {
        return App.render(struc,format,size,atomMap);
    }
}
