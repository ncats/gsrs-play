package ix.ginas.utils.validation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ix.core.chem.StructureProcessor;
import ix.core.models.Keyword;
import ix.core.models.Payload;
import ix.core.models.Structure;
import ix.core.plugins.PayloadPlugin;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.Component;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.NucleicAcidSubstance;
import ix.ginas.models.v1.Property;
import ix.ginas.models.v1.ProteinSubstance;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.Substance.SubstanceDefinitionType;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.Subunit;
import ix.ginas.utils.CodeSequentialGenerator;
import ix.ginas.utils.GinasProcessingMessage;
import ix.ginas.utils.GinasProcessingMessage.Link;
import ix.ginas.utils.GinasProcessingStrategy;
import ix.ginas.utils.ProteinUtils;
import play.Logger;
import play.Play;
import play.mvc.Call;

public class Validation {
	
	private static CodeSequentialGenerator seqGen=null;
	
	static{
		String codeSystem = Play.application().configuration().getString("ix.ginas.generatedcode.codesystem", null);
		String codeSystemSuffix = Play.application().configuration().getString("ix.ginas.generatedcode.suffix", null);
		int length = Play.application().configuration().getInt("ix.ginas.generatedcode.length", 10);
		boolean padding = Play.application().configuration().getBoolean("ix.ginas.generatedcode.padding", true);
		if(codeSystem!=null){
			seqGen=new CodeSequentialGenerator(length,codeSystemSuffix,padding,codeSystem);
		}
	}
	static final PayloadPlugin _payload =
	        Play.application().plugin(PayloadPlugin.class);
	
	static List<GinasProcessingMessage> validateAndPrepare(Substance s, GinasProcessingStrategy strat){
    	List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
    	try{
	    	
	        if(s==null){
	            gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Substance cannot be parsed"));
	            return gpm;
	        }
	        if(s.uuid==null){
	        	UUID uuid=s.getOrGenerateUUID();
	        	gpm.add(GinasProcessingMessage.INFO_MESSAGE("Substance had no UUID, generated one:" + uuid));
	        }
	        if(s.definitionType == SubstanceDefinitionType.PRIMARY){
		        validateNames(s,gpm,strat);
		        validateCodes(s,gpm,strat);
		        validateRelationships(s,gpm,strat);
		        validateNotes(s,gpm,strat);
		        SubstanceReference sr=s.getPrimaryDefinitionReference();
		        if(sr!=null){
		        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Primary definitions cannot be alternative definitions for other Primary definitions"));
		        }
		        for(SubstanceReference relsub : s.getAlternativeDefinitionReferences()){
		        	Substance subAlternative = SubstanceFactory.getFullSubstance(relsub);
		        	if(subAlternative.isPrimaryDefinition()){
		        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Primary definitions cannot be alternative definitions for other Primary definitions"));
		        	}
		        }
		        
	        }else if(s.definitionType == SubstanceDefinitionType.ALTERNATIVE){
	        	if(s.substanceClass==Substance.SubstanceClass.concept){
	        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Alternative definitions cannot be \"concepts\""));
	        	}
	        	if(s.names!=null && s.names.size()>0){
	        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Alternative definitions cannot have names"));
	        	}
	        	if(s.codes!=null && s.codes.size()>0){
	        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Alternative definitions cannot have codes"));
	        	}
	        	if(s.relationships==null || s.relationships.size()==0){
	        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Alternative definitions must specify a primary substance"));
	        	}else{
	        		if(s.relationships.size()>1){
	        			gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Alternative definitions may only have 1 relationship (to the parent definition)"));
	        		}else{
		        		SubstanceReference sr=s.getPrimaryDefinitionReference();
		        		if(sr==null){
		        			gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Alternative definitions must specify a primary substance"));
		        		}else{
		        			Substance subPrimary = SubstanceFactory.getFullSubstance(sr);
		        			if(subPrimary==null){
		        				gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Primary definition for '" + sr.refPname + "' (" + sr.refuuid+ ") not found"));
		        			}else{
		        				if(subPrimary.definitionType!= SubstanceDefinitionType.PRIMARY){
		        					gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Cannot add alternative definition for '" + sr.refPname + "' (" + sr.refuuid+ "). That definition is not primary."));
		        				}else{
		        					if(subPrimary.substanceClass==Substance.SubstanceClass.concept){
		        						gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Cannot add alternative definition for '" + sr.refPname + "' (" + sr.refuuid+ "). That definition is not definitional substance record."));
		        					}else{
		        						subPrimary.addAlternativeSubstanceDefinitionRelationship(s);
		        						
		        					}
		        					
		        				}
		        			}
		        		}
	        		}
	        	}
	        }
	
	        Logger.info("substance Class " + s.substanceClass);

        
	        switch(s.substanceClass){
		        case chemical:
		            gpm.addAll(validateAndPrepareChemical((ChemicalSubstance) s,strat));
		            break;
		        case concept:
		            break;
		        case mixture:
		        	gpm.addAll(validateAndPrepareMixture((MixtureSubstance) s,strat));
		            break;
		        case nucleicAcid:
		            break;
		        case polymer:
		            break;
		        case protein:
		        	gpm.addAll(validateAndPrepareProtein((ProteinSubstance) s,strat));
		            break;
		        case reference:
		            break;
		        case specifiedSubstanceG1:
		            break;
		        case specifiedSubstanceG2:
		            break;
		        case specifiedSubstanceG3:
		            break;
		        case specifiedSubstanceG4:
		            break;
		        case structurallyDiverse:
		            break;
		        case unspecifiedSubstance:
		            break;
		        default:
		        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Substance class \"" +s.substanceClass + "\" is not valid" ));
		            break;
	        }
	        if(seqGen!=null && s.definitionType == SubstanceDefinitionType.PRIMARY){
		        boolean hasCode = false;
		        for(Code c:s.codes){
		        	if(c.codeSystem.equals(seqGen.getCodeSystem())){
		        		hasCode=true;
		        	}
		        }
		        if(!hasCode){
		        	try{
			        	Code c=seqGen.addCode(s);
			        	//System.out.println("Generating new code:" + c.code);
		        	}catch(Exception e){
		        		e.printStackTrace();
		        	}
		        }
	        }
	        
	        if(GinasProcessingMessage.ALL_VALID(gpm)){
	        	gpm.add(GinasProcessingMessage.SUCCESS_MESSAGE("Substance is valid"));
	        }
        }catch(Exception e){
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Internal error:" + e.getMessage()));
        }
        return gpm;
    }
	
	
	


	private static boolean validateReferenced(Substance s, GinasAccessReferenceControlled data,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat, boolean required){
		
		boolean worked=true;
		
		Set<Keyword> references = data.getReferences();
		if(required && (references == null || references.size()<=0)){
			Logger.info("Where are the references?");
			GinasProcessingMessage gpmerr=GinasProcessingMessage.ERROR_MESSAGE("Data " + data.getClass() + " needs at least 1 reference").appliableChange(true);
			strat.processMessage(gpmerr);
			if(gpmerr.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
				gpmerr.appliedChange=true;
				Reference r = Reference.SYSTEM_ASSUMED();
				s.references.add(r);
				data.addReference(r.getOrGenerateUUID().toString());
            }else{
            	worked=false;	
            }
			gpm.add(gpmerr);
		}else{
			for(Keyword ref:references){
				Reference r = s.getReferenceByUUID(ref.getValue());
				if(r==null){
					gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Reference \"" + ref.getValue() +  "\" not found on substance."));
					worked=false;
				}
			}
		}
		
		return worked;
	}
	
	
	
	private static boolean validateNames(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
		 	boolean preferred=false;
		 	int display=0;
	        List<Name> remnames = new ArrayList<Name>();
	        for(Name n : s.names){
	            if(n == null){
	                GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Null name objects are not allowed").appliableChange(true);
	                gpm.add(mes);
	                strat.processMessage(mes);
	                if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
	                    remnames.add(n);
	                    mes.appliedChange=true;
	                }
	               
	            }else{
	                if(n.preferred){
	                    preferred=true;
	                }
	                if(n.isDisplayName()){
	                	display++;
	                }
	                Pattern p = Pattern.compile("(?:[ \\]])\\[([A-Z0-9]*)\\]");
	                Matcher m=p.matcher(n.name);
	                Set<String> locators = new LinkedHashSet<String>();
                	if(m.find()){
                		do{
                			String loc=m.group(1);
                		
                			System.out.println("LOCATOR:" + loc);
                			locators.add(loc);
                		}while(m.find(m.start(1)));
                	}
                	if(locators.size()>0){
                		GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Names of form \"<NAME> [<TEXT>]\" are transformed to locators. The following locators will be added:" + locators.toString()).appliableChange(true);
    	                gpm.add(mes);
    	                strat.processMessage(mes);
    	                if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
    	                    for(String loc:locators){
    	                    	n.name=n.name.replace("[" + loc + "]", "").trim();
    	                    }
    	                    for(String loc:locators){
    	                    	n.addLocator(s, loc);
    	                    }
    	                }
                	}
                	if(n.languages==null||n.languages.size()==0){
                		GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Must specify a language for each name. Defaults to \"English\"").appliableChange(true);
    	                gpm.add(mes);
    	                strat.processMessage(mes);
    	                if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
    	                    if(n.languages==null)n.languages=new ArrayList<Keyword>();
    	                    n.languages.add(new Keyword("en"));
    	                }
                	}
	            }
	            if(!validateReferenced(s,n,gpm,strat,true)){
	            	return false;
	            }
	        }
	        s.names.removeAll(remnames);
	        if(s.names.size()<=0){
	            GinasProcessingMessage mes=GinasProcessingMessage.ERROR_MESSAGE("Substances must have names");
	            gpm.add(mes);
	            strat.processMessage(mes);
	        }
	                
	        if(!preferred){
	            GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Substances should have at least one (1) preferred name, Default to using:" + s.getName()).appliableChange(true);
	            gpm.add(mes);
	            strat.processMessage(mes);
	            if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
	                if(s.names.size()>0){
	                    Name.sortNames(s.names);
	                    s.names.get(0).preferred=true;
	                    mes.appliedChange=true;
	                }
	            }
	        }
	        if(display==0){
	            GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Substances should have at least one (1) display name, Default to using:" + s.getName()).appliableChange(true);
	            gpm.add(mes);
	            strat.processMessage(mes);
	            if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
	                if(s.names.size()>0){
	                    Name.sortNames(s.names);
	                    s.names.get(0).displayName=true;
	                    mes.appliedChange=true;
	                }
	            }
	        }
	        if(display>1){
	        	 GinasProcessingMessage mes=GinasProcessingMessage.ERROR_MESSAGE("Substance can't have more than one (1) display name. Found " + display );
		         gpm.add(mes);
		         strat.processMessage(mes);
	        }
	                
	                
	        for(Name n : s.names){
	        	try{
	            List<Substance> sr=ix.ginas.controllers.v1.SubstanceFactory.getSubstancesWithExactName(100, 0, n.name);
	            if(sr!=null && !sr.isEmpty()){
	                Substance s2=sr.iterator().next();
	                if(!s2.uuid.toString().equals(s.uuid.toString())){
	                    GinasProcessingMessage mes = GinasProcessingMessage
	                        .WARNING_MESSAGE("Name '"
	                                         + n.name
	                                         + "' collides with existing name for substance:"
	                                         ).addSubstanceLink(s2);
	                    gpm.add(mes);
	                    strat.processMessage(mes);
	                }
	            }
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        }
	        return true;
	}
	private static boolean validateCodes(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
	        List<Code> remnames = new ArrayList<Code>();
	        for(Code n : s.codes){
	            if(n == null){
	                GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Null code objects are not allowed").appliableChange(true);
	                gpm.add(mes);
	                strat.processMessage(mes);
	                if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
	                    remnames.add(n);
	                    mes.appliedChange=true;
	                }
	            }
	            if(!validateReferenced(s,n,gpm,strat,true)){
	            	return false;
	            }
	        }
	        s.codes.removeAll(remnames);
	        for(Code n : s.codes){
	        	try{
	            List<Substance> sr=ix.ginas.controllers.v1.SubstanceFactory.getSubstancesWithExactCode(100, 0, n.code, n.codeSystem);
	            if(sr!=null && !sr.isEmpty()){
	                Substance s2=sr.iterator().next();
	                if(!s2.uuid.toString().equals(s.uuid.toString())){
	                    GinasProcessingMessage mes = GinasProcessingMessage
	                        .WARNING_MESSAGE("Code '"
	                                         + n.code
	                                         + "' collides with existing code & codeSystem for substance:").addSubstanceLink(s2);
	                    gpm.add(mes);
	                    strat.processMessage(mes);
	                }
	            }
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
	        }
	        return true;
	}
	
	
	
	private static boolean validateRelationships(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
        List<Relationship> remnames = new ArrayList<Relationship>();
        for(Relationship n : s.relationships){
            if(n == null){
                GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Null relationship objects are not allowed").appliableChange(true);
                gpm.add(mes);
                strat.processMessage(mes);
                if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
                    remnames.add(n);
                    mes.appliedChange=true;
                }
            }
            if(!validateReferenced(s,n,gpm,strat, true)){
            	return false;
            }
        }
        s.relationships.removeAll(remnames);
        return true;
	}
	private static boolean validateNotes(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
        List<Note> remnames = new ArrayList<Note>();
        for(Note n : s.notes){
            if(n == null){
                GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Null note objects are not allowed").appliableChange(true);
                gpm.add(mes);
                strat.processMessage(mes);
                if(mes.actionType==GinasProcessingMessage.ACTION_TYPE.APPLY_CHANGE){
                    remnames.add(n);
                    mes.appliedChange=true;
                }
            }
            if(!validateReferenced(s,n,gpm,strat,false)){
            	return false;
            }
        }
        s.notes.removeAll(remnames);
        return true;
	}
    private static List<GinasProcessingMessage> validateStructureDuplicates(ChemicalSubstance cs){
    	List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
    	
    	
    	try {
    		
            List<Substance> sr = ix.ginas.controllers.v1.SubstanceFactory.getCollsionChemicalSubstances(100, 0, cs);
                            
            if(sr!=null && !sr.isEmpty()){    
                int dupes=0;
                GinasProcessingMessage mes=null;
                for(Substance s:sr){
                	
                    if(cs.uuid==null || !s.uuid.toString().equals(cs.uuid.toString())){
                    	if(dupes<=0)mes=GinasProcessingMessage.WARNING_MESSAGE("Structure has 1 possible duplicate:");
                        dupes++;
                        mes.addSubstanceLink(s);
                    }
                }
                if(dupes>0){
                	if(dupes>1)
                		mes.message="Structure has " + dupes + " possible duplicates:";
                    gpm.add(mes);                                           
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    	return gpm;
    }
    public static List<GinasProcessingMessage> validateSequenceDuplicates(ProteinSubstance cs){
    	List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
    	try {
    		for(Subunit su : cs.protein.subunits){
    			Payload payload = _payload.createPayload
                        ("Sequence Search", "text/plain", su.sequence);
	            List<Substance> sr=ix.ginas.controllers.v1.SubstanceFactory.getNearCollsionProteinSubstancesToSubunit(10, 0, su);
	            if(sr!=null && !sr.isEmpty()){    
	                int dupes=0;
	                GinasProcessingMessage mes=null;
	                for(Substance s:sr){
	                    if(cs.uuid==null || !s.uuid.toString().equals(cs.uuid.toString())){
	                    	if(dupes<=0){
	                    		mes=GinasProcessingMessage.WARNING_MESSAGE("There is 1 substance with a similar sequence to subunit [" + su.subunitIndex + "]:");
	                    		Link l = new Link();
	                    		Call call = ix.ginas.controllers.routes.GinasApp.substances(payload.id.toString(), 16, 1);	                                     
	                    		l.href=call.url()+"&type=sequence";
	                    		l.text="Perform similarity search on subunit [" + su.subunitIndex + "]";
	                    		
	                    		mes.addLink(l);
	                    	}
	                        dupes++;
	                        mes.addSubstanceLink(s);
	                    }
	                }
	                if(dupes>0){
	                	
	                	if(dupes>1)
	                		mes.message="There are " + dupes + " substances with a similar sequence to subunit [" + su.subunitIndex + "]:";
	                    gpm.add(mes);                                           
	                }
	            }
    		}
        } catch (Exception e) {
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Error performing seqeunce search on protein:" + e.getMessage()));
        }
    	return gpm;
    }

    private static List<? extends GinasProcessingMessage> validateAndPrepareMixture(
			MixtureSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
        if(cs.mixture==null){
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Mixture substance must have a mixture element"));
        }else{
        	if(cs.mixture.components==null || cs.mixture.components.size()<2){
        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Mixture substance must have at least 2 mixture components"));
        	}else{
        		for(Component c:cs.mixture.components){
        			Substance comp=SubstanceFactory.getFullSubstance(c.substance);
        			if(comp==null){
        				gpm.add(GinasProcessingMessage.WARNING_MESSAGE("Mixture substance references \"" + c.substance.getName() + "\" which is not yet registered"));
        			}
        		}
        	}
        }
        return gpm;
	}
    
    private static List<? extends GinasProcessingMessage> validateAndPrepareNa(
			NucleicAcidSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
        if(cs.nucleicAcid==null){
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Nucleic Acid substance must have a nucleicAcid element"));
        }else{
        	if(cs.nucleicAcid.getSubunits()==null || cs.nucleicAcid.getSubunits().size()<1){
        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 subunit"));
        	}else{
        		
        	}
        	if(cs.nucleicAcid.getSugars()==null || cs.nucleicAcid.getSugars().size()<1){
        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified sugar"));
        	}else{
        		
        	}
        	if(cs.nucleicAcid.getLinkages()==null || cs.nucleicAcid.getLinkages().size()<1){
        		gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Nucleic Acid substance must have at least 1 specified linkage"));
        	}else{
        		
        	}
        }
        return gpm;
	}
    
	private static List<? extends GinasProcessingMessage> validateAndPrepareProtein(
			ProteinSubstance cs, GinasProcessingStrategy strat) {
		List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
        if(cs.protein==null){
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Protein substance must have a protein element"));
        }else{
        	double tot=ProteinUtils.generateProteinWeight(cs);
        	List<Property> molprops=ProteinUtils.getMolWeightProperties(cs);
        	if(molprops.size()<=0){
        		GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Protein has no molecular weight, defaulting to calculated value").appliableChange(true);
        		gpm.add(mes);
        		strat.processMessage(mes);
        		switch(mes.actionType){
					case APPLY_CHANGE:
						cs.properties.add(ProteinUtils.makeMolWeightProperty(tot));
						break;
					case DO_NOTHING:
						break;
					case FAIL:
						break;
					case IGNORE:
						break;
					default:
						break;
        		}
        	}else{
        		Property p=molprops.get(0);
        		double delta=tot-p.value.average;
        		double pdiff=delta/(p.value.average);
        		int len=0;
        		for(Subunit su:cs.protein.subunits){
        			len += su.sequence.length();
        		}
        		double avgoff=delta/len;
        		//System.out.println("Diff:" + pdiff + "\t" + avgoff);
        		if(Math.abs(pdiff)>.05){
        			gpm.add(GinasProcessingMessage.WARNING_MESSAGE("Calculated weight [" + tot + "] is greater than 5% off of given weight [" + p.value.average + "]").appliableChange(true));
        		}
        	}
        	System.out.println("calc:" + tot);
        }
        
        strat.addAndProcess(validateSequenceDuplicates(cs), gpm);
        return gpm;
	}
    public static List<? extends GinasProcessingMessage> validateAndPrepareChemical(ChemicalSubstance cs, GinasProcessingStrategy strat){
        List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
        if(cs.structure==null){
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Chemical substance must have a chemical structure"));
        }
        
        try {
			ix.ginas.utils.validation.PeptideInterpreter.Protein p=PeptideInterpreter.getAminoAcidSequence(cs.structure.molfile);
			if(p!=null && p.subunits.size()>=1 && p.subunits.get(0).sequence.length()>2){
				GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Substance may be represented as protein as well. Seqence:[" +p.toString() + "]");
				gpm.add(mes);
	            strat.processMessage(mes);
			}
		} catch (Exception e) {
			
		}
        
        String payload = cs.structure.molfile;
        if (payload != null) {
        	Structure struc=null;
        	
            List<Moiety> moietiesForSub = new ArrayList<Moiety>();
            
            {
	            List<Structure> moieties = new ArrayList<Structure>();
	            struc = StructureProcessor.instrument
	                (payload, moieties, false); // don't standardize
	            for(Structure m: moieties){
	                Moiety m2= new Moiety();
	                m2.structure=new GinasChemicalStructure(m);
	                m2.count=m.count;
	                moietiesForSub.add(m2);
	            }
            }
            if(cs.moieties==null || cs.moieties.size()!=moietiesForSub.size()){
            	
                GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Incorrect number of moeities").appliableChange(true);
                gpm.add(mes);
                strat.processMessage(mes);
                switch(mes.actionType){
	                case APPLY_CHANGE:
	                    cs.moieties=moietiesForSub;
	                    mes.appliedChange=true;
	                    break;
	                case FAIL:
	                    break;
	                case DO_NOTHING:
	                case IGNORE:
	                default:
	                    break;
                }
            }else{
            	for(Moiety m:cs.moieties){
            		Structure struc2 = StructureProcessor.instrument(m.structure.molfile, null, false); // don't standardize
            		strat.addAndProcess(validateChemicalStructure(m.structure,struc2,strat),gpm);
            	}
            }
            strat.addAndProcess(validateChemicalStructure(cs.structure,struc,strat),gpm);
            strat.addAndProcess(validateStructureDuplicates(cs), gpm);
        }else{
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Chemical substance must have a valid chemical structure"));
        	
        }
        return gpm;
    }
    
    private static List<GinasProcessingMessage> validateChemicalStructure(GinasChemicalStructure oldstr, Structure newstr, GinasProcessingStrategy strat){
    	 List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
    	 
    	 String oldhash=null;
    	 String newhash=null;
    	 oldhash=oldstr.getLychiv4Hash();
	     newhash=newstr.getLychiv4Hash();
         if(!newhash.equals(oldhash)){
             GinasProcessingMessage mes
                  = GinasProcessingMessage.INFO_MESSAGE("Given structure hash disagrees with computed").appliableChange(true);
             
             gpm.add(mes);
             strat.processMessage(mes);
             switch(mes.actionType){
	                case APPLY_CHANGE:
	                    Structure struc2=new GinasChemicalStructure(newstr);
	                    oldstr.properties=struc2.properties;
	                    oldstr.charge=struc2.charge;
	                    oldstr.formula=struc2.formula;
	                    oldstr.mwt=struc2.mwt;
	                    oldstr.smiles=struc2.smiles;
	                    oldstr.ezCenters=struc2.ezCenters;
	                    oldstr.definedStereo=struc2.definedStereo;
	                    oldstr.stereoCenters=struc2.stereoCenters;
	                    oldstr.digest=struc2.digest;
	                    mes.appliedChange=true;
	                    break;
	                case FAIL:
	                    break;
	                case DO_NOTHING:
	                case IGNORE:
	                default:
	                    break;
             }
         }
         if(oldstr.digest==null){
        	 oldstr.digest=newstr.digest;
         }
         if(oldstr.smiles==null){
        	 oldstr.smiles=newstr.digest;
         }
         if(oldstr.ezCenters==null){
        	 oldstr.ezCenters=newstr.ezCenters;
         }
         if(oldstr.definedStereo==null){
        	 oldstr.definedStereo=newstr.definedStereo;
         }
         if(oldstr.stereoCenters==null){
        	 oldstr.stereoCenters=newstr.stereoCenters;
         }
         if(oldstr.mwt==null){
        	 oldstr.mwt=newstr.mwt;
         }
         if(oldstr.formula==null){
        	 oldstr.formula=newstr.formula;
         }
         if(oldstr.charge==null){
        	 oldstr.charge=newstr.charge;
         }
         
         return gpm;
    }
}