package ix.core.controllers.services;

import gov.nih.ncats.molwitch.io.CtTableCleaner;
import gov.nih.ncats.molwitch.renderer.RendererOptions;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.services.AbstractService;
import ix.core.models.Structure;
import play.libs.F;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Make a single request to the Renderer API microservice
 */
public class RendererServiceRequest extends AbstractService {

    private static int DEFAULT_SIZE = 250;
    private static String DEFAULT_FORMAT = "png";

    public static RendererServiceRequest createFor(Structure struc, int size){
        //this code was borrowed from App.render() to populate the property map and caption text
        Map<String, Boolean> newDisplay = new HashMap<>();
        newDisplay.put(RendererOptions.DrawOptions.DRAW_STEREO_LABELS_AS_RELATIVE.name(),
                Structure.Stereo.RACEMIC.equals(struc.stereoChemistry));

        String bottomText=null;
        String topText=null;

        if(!Structure.Optical.UNSPECIFIED.equals(struc.opticalActivity)
                && struc.opticalActivity!=null){
            if(struc.definedStereo>0){
                if(Structure.Optical.PLUS_MINUS.equals(struc.opticalActivity)){
                    if(Structure.Stereo.EPIMERIC.equals(struc.stereoChemistry)
                            || Structure.Stereo.RACEMIC.equals(struc.stereoChemistry)
                            || Structure.Stereo.MIXED.equals(struc.stereoChemistry)){
                        bottomText = "relative stereochemistry";
                    }
                }
            }
            if(struc.opticalActivity== Structure.Optical.PLUS){
                bottomText = "optical activity: (+)";
                if(Structure.Stereo.UNKNOWN.equals(struc.stereoChemistry)){
                    newDisplay.put(RendererOptions.DrawOptions.DRAW_STEREO_LABELS_AS_STARRED.name(), true);
                }
            } else if(struc.opticalActivity== Structure.Optical.MINUS) {
                bottomText = "optical activity: (-)";
                if(Structure.Stereo.UNKNOWN.equals(struc.stereoChemistry)){
                    newDisplay.put(RendererOptions.DrawOptions.DRAW_STEREO_LABELS_AS_STARRED.name(), true);
                }
            }
        }

        if(size>250){
            if(!Structure.Stereo.ACHIRAL.equals(struc.stereoChemistry))
                newDisplay.put(RendererOptions.DrawOptions.DRAW_STEREO_LABELS.name(), true);
        }

        RendererRequestObj requestObj = new RendererRequestObj();
        requestObj.structure = fixMolIfNeeded(struc);
        requestObj.captionBottom = bottomText;
        requestObj.captureTop = topText;
        requestObj.properties = newDisplay;

        return new RendererServiceRequest(requestObj, size);
    }

    private static String fixMolIfNeeded(Structure struc ){
        if(struc.molfile ==null){
            return struc.smiles;
        }
        try {
            return CtTableCleaner.clean(struc.molfile);
        } catch (IOException e) {
            e.printStackTrace();
            return struc.molfile;
        }
    }

    private int size = DEFAULT_SIZE;
    private String format = DEFAULT_FORMAT;
    private RendererRequestObj rendererRequestObj;

    private RendererServiceRequest(RendererRequestObj rendererRequestObj, int size) {
        super("renderer");
        this.rendererRequestObj = Objects.requireNonNull(rendererRequestObj);
        this.size = size < 1 ? DEFAULT_SIZE : size;
    }

    public RendererServiceRequest format(String format){
        if(format ==null){
            this.format = DEFAULT_FORMAT;
        }else{
            this.format = format;
        }
        return this;
    }
    public RendererServiceRequest size(int size){
        if(size <1){
            this.size = DEFAULT_SIZE;
        }else{
            this.size = size;
        }
        return this;
    }
    public RendererServiceRequest atomMap(int[] amap){
        rendererRequestObj.atomMap = amap;
        return this;
    }
    public RendererServiceRequest markStereo(Boolean markStereo){
        rendererRequestObj.markStereo = markStereo;
        return this;
    }

    public F.Promise<byte[]> execute(){
        WSRequestHolder holder =  createRequestFor("/structure");
//        System.out.println("url = " + holder.getUrl());
    return holder
                .setQueryParameter("format", format)
                .setQueryParameter("size", Integer.toString(size))
                .setContentType("application/json")
                .post(EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().toJson(rendererRequestObj))
                .map(new F.Function<WSResponse, byte[]>() {
                    public byte[] apply(WSResponse response) {
                        return response.asByteArray();
                    }
                });



    }

    public static class RendererRequestObj{
        public String structure;
        public String captionBottom;
        public String captureTop;
        public Boolean markStereo;
        public int[] atomMap;
        public Map<String, Boolean> properties;

    }
}
