package ix.core.services;

import ix.core.models.Structure;

public interface RendererService {
    byte[] render(Structure struc, String format, int size, int[] atomMap, Boolean showStereo) throws Exception;
}
