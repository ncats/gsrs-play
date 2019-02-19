package ix.ginas.utils.validation.validators;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import ix.core.models.Payload;
import ix.core.plugins.PayloadPlugin;
import ix.core.plugins.SequenceIndexerPlugin;
import ix.core.util.CachedSupplier;
import ix.core.util.StreamUtil;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidatorCallback;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.DisulfideLink;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.Protein;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Site;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Subunit;
import ix.ginas.utils.ProteinUtils;
import ix.ginas.utils.validation.ValidationUtils;
import ix.seqaln.SequenceIndexer.CutoffType;
import ix.utils.Tuple;
import play.Logger;
import play.Play;
import play.mvc.Call;

/**
 * Created by katzelda on 5/14/18.
 */
public class ProteinValidator extends AbstractValidatorPlugin<Substance> {
    private CachedSupplier<PayloadPlugin> _payload = CachedSupplier.of(()-> Play.application().plugin(PayloadPlugin.class));

    private CachedSupplier<SequenceIndexerPlugin> _seqIndexer = CachedSupplier.of(()-> Play.application().plugin(SequenceIndexerPlugin.class));

    @Override
    public void validate(Substance objnew, Substance objold, ValidatorCallback callback) {

        ProteinSubstance cs = (ProteinSubstance) objnew;
        
        List<GinasProcessingMessage> gpm = new ArrayList<GinasProcessingMessage>();
        if (cs.protein == null) {
            callback.addMessage(GinasProcessingMessage
                    .ERROR_MESSAGE("Protein substance must have a protein element"));
        } else {
            if(cs.protein.subunits.isEmpty() ){
                if(Substance.SubstanceDefinitionLevel.INCOMPLETE.equals(cs.definitionLevel)){
                    callback.addMessage(GinasProcessingMessage.WARNING_MESSAGE("Having no subunits is allowed but discouraged for incomplete protein records."));
                }else{
                    callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("Complete protein substance must have at least one Subunit element. Please add a subunit, or mark as incomplete."));
                }
            }
            for (int i = 0; i < cs.protein.subunits.size(); i++) {
                Subunit su = cs.protein.subunits.get(i);
                if (su.subunitIndex == null) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .WARNING_MESSAGE(
                                    "Protein subunit (at "
                                            + (i + 1)
                                            + " position) has no subunit index, defaulting to:"
                                            + (i + 1)).appliableChange(true);
                    Integer newValue = i +1;
                    callback.addMessage(mes, () -> su.subunitIndex = newValue);

                    }
                }
            }

            for (DisulfideLink l : cs.protein.getDisulfideLinks()) {

                List<Site> sites = l.getSites();
                if (sites.size() != 2) {
                    GinasProcessingMessage mes = GinasProcessingMessage
                            .ERROR_MESSAGE("Disulfide Link \""
                                    + sites.toString() + "\" has "
                                    + sites.size() + " sites, should have 2");
                    callback.addMessage(mes);
                } else {
                    for (Site s : sites) {
                        String res = cs.protein.getResidueAt(s);
                        if (res == null) {
                            GinasProcessingMessage mes = GinasProcessingMessage
                                    .ERROR_MESSAGE("Site \"" + s.toString()
                                            + "\" does not exist");
                            callback.addMessage(mes);
                        } else {
                            if (!res.equalsIgnoreCase("C")) {
                                GinasProcessingMessage mes = GinasProcessingMessage
                                        .ERROR_MESSAGE("Site \""
                                                + s.toString()
                                                + "\" in disulfide link is not a Cysteine, found: \""
                                                + res + "\"");
                                callback.addMessage(mes);
                            }
                        }
                    }
                }

            }

            Set<String> unknownRes = new HashSet<String>();
            double tot = ProteinUtils.generateProteinWeight(cs, unknownRes);
            if (unknownRes.size() > 0) {
                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Protein has unknown amino acid residues: "
                                + unknownRes.toString());
                callback.addMessage(mes);
            }

            List<Property> molprops = ProteinUtils.getMolWeightProperties(cs);
            if (molprops.size() <= 0) {

                GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE(
                                "Protein has no molecular weight, defaulting to calculated value of: "
                                        + String.format("%.2f", tot)).appliableChange(true);
                callback.addMessage(mes, ()-> {

                    cs.properties.add(ProteinUtils.makeMolWeightProperty(tot));
                    if (!unknownRes.isEmpty()) {
                        GinasProcessingMessage mes2 = GinasProcessingMessage
                                .WARNING_MESSAGE("Calculated protein weight questionable, due to unknown amino acid residues: "
                                        + unknownRes.toString());
                        callback.addMessage(mes2);
                    }
                });

            } else {
                for(Property p :molprops){
                    if (p.getValue() != null) {
                        Double avg=p.getValue().average;
                        if(avg==null)continue;
                        double delta = tot - avg;
                        double pdiff = delta / (avg);

                        int len = 0;
                        for (Subunit su : cs.protein.subunits) {
                            len += su.sequence.length();
                        }
                        double avgoff = delta / len;
                        if (Math.abs(pdiff) > .05) {
                            callback.addMessage(GinasProcessingMessage
                                    .WARNING_MESSAGE(
                                            "Calculated weight ["
                                                    + String.format("%.2f", tot)
                                                    + "] is greater than 5% off of given weight ["
                                                    + String.format("%.2f", p.getValue().average) + "]"));
                            //katzelda May 2018 - turn off appliable change since there isn't anything to change it to.
//                                    .appliableChange(true));
                        }
                        //if it gets this far, break out of the properties
                        break;
                    }
                }
            }

        boolean sequenceHasChanged = sequenceHasChanged(cs, objold);

        if(sequenceHasChanged) {
            validateSequenceDuplicates(cs, callback);
        }
        if (!cs.protein.getSubunits().isEmpty()) {
            ValidationUtils.validateReference(cs, cs.protein, callback, ValidationUtils.ReferenceAction.FAIL);
        }
    }

    private static boolean sequenceHasChanged(ProteinSubstance cs, Substance o) {
        if(o ==null || !(o instanceof ProteinSubstance)){
            return true;
        }
        ProteinSubstance old = (ProteinSubstance) o;
        Protein newProtein = cs.protein;
        Protein oldProtein = old.protein;

        if(oldProtein ==null){
            return newProtein !=null;
        }
        List<Subunit> newSubs = newProtein.getSubunits();
        List<Subunit> oldSubs = oldProtein.getSubunits();
        if(newSubs.size() != oldSubs.size()){
            return true;
        }
        int size = newSubs.size();
        for(int i=0; i< size; i++){
            Subunit newSub = newSubs.get(i);
            Subunit oldSub = oldSubs.get(i);

            //handles null
            if(!Objects.equals(newSub.sequence, oldSub.sequence)){
                return true;
            }
        }
        return false;
    }

    private void validateSequenceDuplicates(
            ProteinSubstance proteinsubstance, ValidatorCallback callback) {

        try {
        	proteinsubstance.protein.subunits
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

                List<Function<String,List<Tuple<Double,Tuple<ProteinSubstance,Subunit>>>>> searchers = new ArrayList<>();

                //Simplified searcher, using lucene direct index
                searchers.add(seq->{
                	try{
                	List<Tuple<ProteinSubstance,Subunit>> simpleResults=SubstanceFactory.executeSimpleExactProteinSubunitSearch(su);

                	return simpleResults.stream()
				                	  .map(t->{
				                		  return Tuple.of(1.0,t);
				                	  })
				                      .filter(t->!t.v().k().getOrGenerateUUID().equals(proteinsubstance.getOrGenerateUUID()))
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
        			           							.search(seq, SubstanceFactory.SEQUENCE_IDENTITY_CUTOFF, CutoffType.GLOBAL, "Protein"))
                     .map(suResult->{
                    	 return Tuple.of(suResult.score,SubstanceFactory.getSubstanceAndSubunitFromSubunitID(suResult.id));
                     })
                     .filter(op->op.v().isPresent())
                     .map(Tuple.vmap(opT->opT.get()))
                     .filter(t->!t.v().k().getOrGenerateUUID().equals(proteinsubstance.getOrGenerateUUID()))
                     .filter(t->(t.v().k() instanceof ProteinSubstance))
                     .map(t->{
                    	 //TODO: could easily be cleaned up.
                    	 ProteinSubstance ps=(ProteinSubstance)t.v().k();
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
                             l.href = call.url() + "&type=sequence&identity=" + SubstanceFactory.SEQUENCE_IDENTITY_CUTOFF + "&identityType=SUB&seqType=Protein";
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
                                     	 Tuple<ProteinSubstance,Subunit> tup=tupTotal.v();
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
                    .ERROR_MESSAGE("Error performing seqeunce search on protein:"
                            + e.getMessage()));
        }
    }

}
