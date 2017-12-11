package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.adapters.EntityPersistAdapter;
import ix.core.controllers.PayloadFactory;
import ix.core.models.Payload;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Substance;
import ix.seqaln.SequenceIndexer;
import org.jcvi.jillion.fasta.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NewFastaFileReferenceProcessor implements EntityProcessor<Substance> {
    private static Pattern PAYLOAD_UUID_PATTERN = Pattern.compile("payload\\((.+?)\\)");

    public NewFastaFileReferenceProcessor(){
        System.out.println("new fasta file processor");
    }

    @Override
    public void postUpdate(Substance obj) throws FailProcessingException {
        System.out.println("post update");
        update(obj);

    }

    @Override
    public void postPersist(Substance obj) throws FailProcessingException {
        System.out.println("post persist");
        update(obj);


    }

    private void update(Substance obj){
        System.out.println("here obj type =" + obj.getClass());
//        if( !(obj instanceof ProteinSubstance) && !(obj instanceof NucleicAcidSubstance)){
//            return;
//        }
        obj.references.stream()
                .peek(r->System.out.println("uploaded file = " + r.uploadedFile))
                .filter(r -> r.uploadedFile !=null)

                .flatMap(r-> {
                            if(r.tags.stream()
                                    .peek(k->System.out.println(k))
                                    .filter(k-> k.term.equalsIgnoreCase("fasta"))
                                    .findAny()
                                    .isPresent()) {
                                Matcher m = PAYLOAD_UUID_PATTERN.matcher(r.uploadedFile);
                                if (m.find()) {
                                    String uuid = m.group(1);
                                    System.out.println("found payload "+ uuid);
                                    Payload payload = PayloadFactory.getPayload(UUID.fromString(uuid));
                                    return Stream.of(payload);
                                }
                            }
                            return Stream.empty();
                        }
                ).forEach(payload-> {
            File f = PayloadFactory.getFile(payload);

            SequenceIndexer indexer = EntityPersistAdapter.getSequenceIndexer();

            try {
                FastaFileParser.create(f).parse(new FastaVisitor() {
                    @Override
                    public FastaRecordVisitor visitDefline(FastaVisitorCallback fastaVisitorCallback, String id, String comment) {
                        //TODO process comments
                        return new AbstractFastaRecordVisitor(id, comment) {
                            @Override
                            protected void visitRecord(String id, String comment, String seq) {
                                try {
                                    System.out.println("adding seq:" + seq);
                                    indexer.add(">"+obj.uuid +"|"+id, seq);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                    }

                    @Override
                    public void visitEnd() {

                    }

                    @Override
                    public void halted() {

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
