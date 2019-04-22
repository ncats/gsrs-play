package ix.ginas.utils.validation.validators;

import ix.core.models.Payload;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.util.CachedSupplier;
import ix.core.util.StreamUtil;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.NucleicAcid;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Subunit;
import ix.ginas.utils.NucleicAcidUtils;
import ix.ginas.utils.validation.ValidationUtils;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.utils.Tuple;

import org.jcvi.jillion.core.residue.nt.NucleotideSequence;

import play.Logger;
import play.Play;
import play.mvc.Call;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by katzelda on 5/14/18.
 */
public class NucleicAcidValidator extends AbstractValidatorPlugin<Substance> {

    private CachedSupplier<PayloadPlugin> _payload = CachedSupplier.of(()-> Play.application().plugin(PayloadPlugin.class));
    private CachedSupplier<SequenceIndexerPlugin> _seqIndexer = CachedSupplier.of(()-> Play.application().plugin(SequenceIndexerPlugin.class));

    @Override
    public void validate(Substance s, Substance objold, ValidatorCallback callback) {
        NucleicAcidSubstance cs = (NucleicAcidSubstance)s;
        if (cs.nucleicAcid == null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Nucleic Acid substance must have a nucleicAcid element"));
            return;
        }

            if (cs.nucleicAcid.getSubunits() == null
                    || cs.nucleicAcid.getSubunits().isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have at least 1 subunit"));
            }
            if (cs.nucleicAcid.getSugars() == null
                    || cs.nucleicAcid.getSugars().isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified sugar"));
            }
            if (cs.nucleicAcid.getLinkages() == null
                    || cs.nucleicAcid.getLinkages().isEmpty()) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified linkage"));
            }


            int unspSugars = NucleicAcidUtils
                    .getNumberOfUnspecifiedSugarSites(cs);
            if (unspSugars != 0) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have every base specify a sugar fragment. Missing "
                                + unspSugars + " sites."));
            }


            int unspLinkages = NucleicAcidUtils
                    .getNumberOfUnspecifiedLinkageSites(cs);
            if (unspLinkages != 0) {
                callback.addMessage(GinasProcessingMessage
                        .ERROR_MESSAGE("Nucleic Acid substance must have every linkage specify a linkage fragment. Missing "
                                + unspLinkages + " sites."));
            }

        if(sequenceHasChanged(cs, objold)){
            validateSequence(cs, callback);
        }
        
        if(!cs.nucleicAcid.getSubunits().isEmpty()) {
            ValidationUtils.validateReference(cs, cs.nucleicAcid, callback, ValidationUtils.ReferenceAction.FAIL);
        }
    }

    private void validateSequence(NucleicAcidSubstance s, ValidatorCallback callback){
        List<Subunit> subunits = s.nucleicAcid.getSubunits();

        Set<String> seen = new HashSet<>();
        for(Subunit subunit : subunits){
            if(!seen.add(subunit.sequence)){
                //should we remove as applicable change?
            	//TP: I'm not sure we should even warn about duplicates within a record, tbh. At least with proteins,
            	//it's quite common to have subunits that are identical in sequence within the same record.
                callback.addMessage(GinasProcessingMessage.WARNING_MESSAGE("Duplicate subunit at index " + subunit.subunitIndex));
            }
            NucleotideSequence nucleotideSequence =null;
            try {
                NucleotideSequence.of(subunit.sequence);
            }catch(Exception e){
                //invalid bases
                callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE(
                        "invalid nucleic acid sequence base in subunit " + subunit.subunitIndex + "  " + e.getMessage()));

            }
        }
        validateSequenceDuplicates(s, callback);


    }

    private void validateSequenceDuplicates(
    		NucleicAcidSubstance nucleicAcidSubstance, ValidatorCallback callback) {

        try {
        	nucleicAcidSubstance.nucleicAcid.subunits
            .stream()
            .collect(Collectors.groupingBy(su->su.sequence))
            .entrySet()
            .stream()
            .map(Tuple::of)
            .map(t->t.v())
            .map(l->l.stream().map(su->Tuple.of(su.subunitIndex,su).withKSortOrder())
         		   			 .sorted()
         		   			 .map(t->t.v())
         		   			 .collect(Collectors.toList()))
            .forEach(subs->{
         	   try{
         		   Subunit su=subs.get(0);
         		   String suSet = subs.stream().map(su1->su1.subunitIndex+"").collect(Collectors.joining(","));

         	   Payload payload = _payload.get()
         			                     .createPayload("Sequence Search","text/plain", su.sequence);

                String msgOne = "There is 1 substance with a similar sequence to subunit ["
                        + suSet + "]:";

                String msgMult = "There are ? substances with a similar sequence to subunit ["
                        + suSet + "]:";

                List<Function<String,List<Tuple<Double,Tuple<NucleicAcidSubstance,Subunit>>>>> searchers = new ArrayList<>();

                //Simplified searcher, using lucene direct index
                searchers.add(seq->{
                	try{
                	List<Tuple<NucleicAcidSubstance,Subunit>> simpleResults=SubstanceFactory.executeSimpleExactNucleicAcidSubunitSearch(su);

                	return simpleResults.stream()
				                	  .map(t->{
				                		  return Tuple.of(1.0,t);
				                	  })
				                      .filter(t->!t.v().k().getOrGenerateUUID().equals(nucleicAcidSubstance.getOrGenerateUUID()))
				                      .collect(Collectors.toList());
                	}catch(Exception e){
                	    e.printStackTrace();
                		Logger.warn("Problem performing sequence search on lucene index", e);
                		return new ArrayList<>();
                	}
                });

                //Traditional searcher using sequence indexer
                searchers.add(seq->{
                	return StreamUtil.forEnumeration(_seqIndexer.get()
							.getIndexer()
							.search(seq, SubstanceFactory.SEQUENCE_IDENTITY_CUTOFF, CutoffType.GLOBAL, "NucleicAcid"))
                     .map(suResult->{
                    	 return Tuple.of(suResult.score,SubstanceFactory.getSubstanceAndSubunitFromSubunitID(suResult.id));
                     })
                     .filter(op->op.v().isPresent())
                     .map(Tuple.vmap(opT->opT.get()))
                     .filter(t->!t.v().k().getOrGenerateUUID().equals(nucleicAcidSubstance.getOrGenerateUUID()))
                     .filter(t->(t.v().k() instanceof NucleicAcidSubstance))
                     .map(t->{
                    	 //TODO: could easily be cleaned up.
                    	 NucleicAcidSubstance ps=(NucleicAcidSubstance)t.v().k();
                    	 return Tuple.of(t.k(), Tuple.of(ps, t.v().v()));
                     })
                     //TODO: maybe sort by the similarity?
                     .collect(Collectors.toList());
                });

                searchers.stream()
                         .map(searcher->searcher.apply(su.sequence))
                         .filter(suResults->!suResults.isEmpty())
                         .map(res->res.stream().map(t->t.withKSortOrder(k->k)).sorted().collect(Collectors.toList()))
                         .findFirst()
                         .ifPresent(suResults->{
                             List<GinasProcessingMessage.Link> links = new ArrayList<>();
                             GinasProcessingMessage.Link l = new GinasProcessingMessage.Link();
                             Call call = ix.ginas.controllers.routes.GinasApp
                                     .substances(payload.id.toString(), 16,1);
                             l.href = call.url() + "&type=sequence&identity=" + SubstanceFactory.SEQUENCE_IDENTITY_CUTOFF + "&identityType=SUB&seqType=NucleicAcid";
                             l.text = "(Perform similarity search on subunit ["
                                     + su.subunitIndex + "])";

                             String warnMessage=msgOne;

                             if(suResults.size()>1){
                            	 warnMessage = msgMult.replace("?", suResults.size() +"");
                             }

                             GinasProcessingMessage dupMessage = GinasProcessingMessage
                                     .WARNING_MESSAGE(warnMessage);
                             dupMessage.addLink(l);



                             suResults.stream()
                                      .map(t->t.withKSortOrder(d->d))
                                      .sorted()
                                      .forEach(tupTotal->{
                                     	 Tuple<NucleicAcidSubstance,Subunit> tup=tupTotal.v();
                                     	 double globalScore = tupTotal.k();
                                     	 String globalScoreString = (int)Math.round(globalScore*100) + "%";

                                     	 GinasProcessingMessage.Link l2 = new GinasProcessingMessage.Link();
                                          Call call2 = ix.ginas.controllers.routes.GinasApp.substance(tup.k().uuid.toString());
                                          l2.href = call2.url();
                                          if(globalScore==1){
         	                                 l2.text = "found exact duplicate (" + globalScoreString + ") sequence in " +
         	                                          "Subunit [" +tup.v().subunitIndex + "] of \"" + tup.k().getApprovalIDDisplay() + "\" " +
         	                                                                            "(\"" + tup.k().getName() + "\")";
                                          }else{
                                         	 l2.text = "found approximate duplicate (" + globalScoreString + ") sequence in " +
         	                                          "Subunit [" +tup.v().subunitIndex + "] of \"" + tup.k().getApprovalIDDisplay() + "\" " +
         	                                                                            "(\"" + tup.k().getName() + "\")";
                                          }
                                          links.add(l2);
                                      });


                             dupMessage.addLinks(links);
                             callback.addMessage(dupMessage);
                         });
         	   }catch(Exception e){
         		   e.printStackTrace();
         	   }
            });

        } catch (Exception e) {
        	Logger.error("Problem executing duplicate search function", e);
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Error performing seqeunce search on Nucleic Acid:"
                            + e.getMessage()));
        }
    }


    private boolean sequenceHasChanged(NucleicAcidSubstance s, Substance objold) {
        if(objold ==null || !(objold instanceof NucleicAcidSubstance)){
            //new substance or converted from different type like a concept
            return true;
        }
        NucleicAcid newNa = s.nucleicAcid;
        NucleicAcid oldNa = ((NucleicAcidSubstance)objold).nucleicAcid;

        List<Subunit> newSubunits = newNa.getSubunits();
        List<Subunit> oldSubunits = oldNa.getSubunits();

        if(newSubunits.size() != oldSubunits.size()){
            return true;
        }
        //I guess it's possible someone edits the nucleic acid and adds a subunit
        //anywhere in the list so we have to check everywhere.
        //also only really care about similar sequences not really order
        //so an edit can rearrange the order too...

        Set<String> newSeqs = newSubunits.stream().map(sub-> sub.sequence).collect(Collectors.toSet());
        Set<String> oldSeqs = oldSubunits.stream().map(sub-> sub.sequence).collect(Collectors.toSet());

        return !newSeqs.equals(oldSeqs);
    }
}
