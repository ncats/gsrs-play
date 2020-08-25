package ix.core.search.text;

import java.io.File;
import java.io.IOException;

public interface IndexerServiceFactory {
    IndexerService createInMemory() throws IOException;

    IndexerService createForDir(File dir) throws IOException;
}
