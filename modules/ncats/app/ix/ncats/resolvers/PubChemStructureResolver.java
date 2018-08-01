package ix.ncats.resolvers;

import java.net.URL;
import java.net.MalformedURLException;

import ix.utils.Util;

public class PubChemStructureResolver extends AbstractStructureResolver {
    public static final String PUBCHEM_RESOLVER = 
        "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name";
    
    public PubChemStructureResolver () {
        super ("PubChem");
    }

    @Override
    protected AbstractStructureResolver.UrlAndFormat[] resolvers (String name) throws MalformedURLException {
        return  new AbstractStructureResolver.UrlAndFormat[] {
            new AbstractStructureResolver.UrlAndFormat(new URL (PUBCHEM_RESOLVER+ "/"+Util.URLEncode(name)+"/sdf"), "sdf" )
        };
    }
}
