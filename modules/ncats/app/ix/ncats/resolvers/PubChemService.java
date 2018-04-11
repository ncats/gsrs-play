package ix.ncats.resolvers;

import ix.core.models.Structure;
import ix.core.util.TimeUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * {@link PubChemStructureResolver} wrapper since
 * PubChem requires a 5 sec wait between requests.
 *
 * Created by katzelda on 4/10/18.
 */
public enum PubChemService implements Resolver<Structure>{

    INSTANCE;

    private static final int MIN_WAIT_TIME = 5_000;
    private long lastRun=0;

    private final PubChemStructureResolver resolver = new PubChemStructureResolver();

    public synchronized Structure resolve(String name) {
        long currentTime = TimeUtil.getCurrentTimeMillis();
        long delta = currentTime - lastRun;
        lastRun = currentTime;

        if(delta < MIN_WAIT_TIME){
            try {
                Thread.sleep(MIN_WAIT_TIME- delta);
            } catch (InterruptedException e) {
                return null;
            }
        }

        return resolver.resolve(name);


    }


    @Override
    public Class<Structure> getType() {
        return resolver.getType();
    }

    @Override
    public String getName() {
        return resolver.getName();
    }

}
