package ix.ncats.resolvers;

import java.net.MalformedURLException;
import java.net.URL;

import ix.utils.Util;

public class NCIStructureResolver extends AbstractStructureResolver {
    public static final String NCI_RESOLVER1 =
        "http://cactus.nci.nih.gov/chemical/structure";
    public static final String NCI_RESOLVER2 =
        "https://tripod.nih.gov/chemical/structure";
    
    public NCIStructureResolver () {
        super ("NCI");
    }

    @Override
    protected URL[] resolvers (String name) throws MalformedURLException {
        return  new URL[] {
            new URL (NCI_RESOLVER1+"/"+Util.URLEncode(name)+"/sdf"),
            new URL (NCI_RESOLVER2+"/"+Util.URLEncode(name)+"/sdf")
        };
    }
}
