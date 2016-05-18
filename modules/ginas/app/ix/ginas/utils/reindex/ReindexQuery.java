package ix.ginas.utils.reindex;

import ix.core.models.BackupEntity;
import ix.core.util.CloseableIterator;

/**
 * Created by katzelda on 5/16/16.
 */
public interface ReindexQuery {

    CloseableIterator<BackupEntity> query(ReIndexListener listener);
}
