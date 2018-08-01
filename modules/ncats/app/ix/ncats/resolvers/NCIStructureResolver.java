package ix.ncats.resolvers;

import java.net.MalformedURLException;
import java.net.URL;

import ix.utils.Util;

public class NCIStructureResolver extends AbstractStructureResolver {
    public static final String NCI_RESOLVER1 =
        "https://cactus.nci.nih.gov/chemical/structure";

    
    public NCIStructureResolver () {
        super ("NCI");
    }

    @Override
    protected AbstractStructureResolver.UrlAndFormat[] resolvers (String name) throws MalformedURLException {
        return  new AbstractStructureResolver.UrlAndFormat[] {
            new AbstractStructureResolver.UrlAndFormat(new URL (NCI_RESOLVER1+"/"+Util.URLEncode(name)+"/sdf"), "sdf"),
            new AbstractStructureResolver.UrlAndFormat(new URL (NCI_RESOLVER1+"/"+Util.URLEncode(name)+"/smiles"), "smiles")
        };
    }


}
