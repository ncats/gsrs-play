package ix.ginas.initializers;

import ix.core.chem.LychiStructureHasher;
import ix.core.chem.StructureHasher;
import ix.core.chem.StructureProcessor;
import ix.core.initializers.Initializer;
import ix.core.util.ConfigHelper;
import ix.core.util.IOUtil;
import play.Application;

/**
 * Created by katzelda on 7/3/19.
 */
public class StructureHasherInitializer implements Initializer{
    @Override
    public void onStart(Application app) {
        String value = ConfigHelper.getOrDefault("ix.structure-hasher", LychiStructureHasher.class.getName());
        System.out.println("found structure hasher to use: " + value);
        try {
            StructureProcessor.setHasher((StructureHasher)IOUtil.getGinasClassLoader().loadClass(value).newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("could not find/load structure hasher class", e);
        }
    }
}
