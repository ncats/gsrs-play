package ix.core.controllers.services;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class IndexerServiceRequest extends AbstractService{
    protected IndexerServiceRequest() {
        super("textIndex");
    }

    public void deleteDocuments(Query query){
        post(query, "deleteDocsMatching");
    }

    public static void removeDocument(Document doc){
        post(doc, "removeDoc");
    }

    public static void addDocument(Document doc){
        post(doc, "addDoc");
    }

    private static void post(Object doc, String path) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(doc);
        }catch (IOException e){
            e.printStackTrace();
            throw new IllegalStateException("could not serialize doc ",e);
        }
        String base64 = Base64.getEncoder().encodeToString(bos.toByteArray());
        int status = post(path,base64);
        if(status >=400){
            //TODO throw exception?
            System.err.println("ADD DOC ERRORED OUT "+ status);
        }
    }

    private static int post(String path, String base64) {
        return new IndexerServiceRequest()
                    .createRequestFor(path)
                    .post(base64)
                    .get(5_000)
                    .getStatus();


    }

}
