package ix.core.search.text;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import java.io.Closeable;

public interface IndexListener extends Closeable {


    Document addDocument(Document doc);
    void deleteDocuments(Query query);
    void removeAll();
}
