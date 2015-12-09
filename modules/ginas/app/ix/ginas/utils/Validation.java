package ix.ginas.utils;

import ix.core.chem.StructureProcessor;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.Code;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Note;
import ix.ginas.models.v1.Reference;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.Substance;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Logger;

public class Validation {
	public static List<GinasProcessingMessage> validateAndPrepare(Substance s, GinasProcessingStrategy strat){
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
	        validateNames(s,gpm,strat);
	        validateCodes(s,gpm,strat);
	        validateRelationships(s,gpm,strat);
	        validateNotes(s,gpm,strat);
	        
	
	        Logger.info("substance Class " + s.substanceClass);

        
	        switch(s.substanceClass){
		        case chemical:
		            gpm.addAll(validateAndPrepareChemical((ChemicalSubstance) s,strat));
		            break;
		        case concept:
		            break;
		        case mixture:
		            break;
		        case nucleicAcid:
		            break;
		        case polymer:
		            break;
		        case protein:
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
		            break;
	        }
	        if(GinasProcessingMessage.ALL_VALID(gpm)){
	        	gpm.add(GinasProcessingMessage.SUCCESS_MESSAGE("Substance is valid"));
	        }
        }catch(Exception e){
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Internal error:" + e.getMessage()));
        }
        return gpm;
    }
	
	
	public static boolean validateReferenced(Substance s, GinasAccessReferenceControlled data,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat){
		
		boolean worked=true;
		
		Set<Keyword> references = data.getReferences();
		if(references == null || references.size()<=0){
			gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Data " + data.getClass() + " needs at least 1 reference"));
			worked=false;
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
	
	
	
	public static boolean validateNames(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
		 boolean preferred=false;
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
	            if(!validateReferenced(s,n,gpm,strat)){
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
	            GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Substances should have at least one (1) preferred name").appliableChange(true);
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
	                                         ).addLink(s2);
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
	public static boolean validateCodes(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
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
	            if(!validateReferenced(s,n,gpm,strat)){
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
	                                         + "' collides with existing code & codeSystem for substance:").addLink(s2);
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
	public static boolean validateRelationships(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
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
            if(!validateReferenced(s,n,gpm,strat)){
            	return false;
            }
        }
        s.relationships.removeAll(remnames);
        return true;
	}
	public static boolean validateNotes(Substance s,List<GinasProcessingMessage> gpm, GinasProcessingStrategy strat ){
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
            if(!validateReferenced(s,n,gpm,strat)){
            	return false;
            }
        }
        s.relationships.removeAll(remnames);
        return true;
	}
    public static List<GinasProcessingMessage> validateStructureDuplicates(ChemicalSubstance cs){
    	List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
    	try {
            List<Substance> sr=ix.ginas.controllers.v1.SubstanceFactory.getCollsionChemicalSubstances(100, 0, cs);
                            
            if(sr!=null && !sr.isEmpty()){    
                int dupes=0;
                GinasProcessingMessage mes=null;
                for(Substance s:sr){
                	
                    if(cs.uuid==null || !s.uuid.toString().equals(cs.uuid.toString())){
                    	if(dupes<=0)mes=GinasProcessingMessage.WARNING_MESSAGE("Structure has 1 possible duplicate:");
                        dupes++;
                        mes.addLink(s);
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
    
    public static List<GinasProcessingMessage> validateAndPrepareChemical(ChemicalSubstance cs, GinasProcessingStrategy strat){
        List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
        if(cs.structure==null){
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Chemical substance must have a chemical structure"));
        }
        String payload = cs.structure.molfile;
        if (payload != null) {
            List<Moiety> moietiesForSub = new ArrayList<Moiety>();
            List<Structure> moieties = new ArrayList<Structure>();
            Structure struc = StructureProcessor.instrument
                (payload, moieties, false); // don't standardize
            
            //struc.count
            for(Structure m: moieties){
                Moiety m2= new Moiety();
                m2.structure=new GinasChemicalStructure(m);
                m2.count=m.count;
                moietiesForSub.add(m2);
            }
            
            if(cs.moieties.size()<moietiesForSub.size()){
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
            }
            String oldhash=null;
            for (Value val : cs.structure.properties) {
                if (Structure.H_LyChI_L4.equals(val.label)) {
                    oldhash=val.getValue()+"";
                }
            }
            
            String newhash=null;
            for (Value val : struc.properties) {
                if (Structure.H_LyChI_L4.equals(val.label)) {
                    newhash=val.getValue()+"";
                }
            }
            
            if(!newhash.equals(oldhash)){
                GinasProcessingMessage mes=GinasProcessingMessage.INFO_MESSAGE("Given structure hash disagrees with computed").appliableChange(true);
                gpm.add(mes);
                strat.processMessage(mes);
                switch(mes.actionType){
	                case APPLY_CHANGE:
	                    Structure struc2=new GinasChemicalStructure(struc);
	                    cs.structure.properties=struc2.properties;
	                    cs.structure.charge=struc2.charge;
	                    cs.structure.formula=struc2.formula;
	                    cs.structure.mwt=struc2.mwt;
	                    cs.structure.smiles=struc2.smiles;
	                    cs.structure.ezCenters=struc2.ezCenters;
	                    cs.structure.definedStereo=struc2.definedStereo;
	                    cs.structure.stereoCenters=struc2.stereoCenters;
	                    cs.structure.digest=struc2.digest;
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
            strat.addAndProcess(validateStructureDuplicates(cs), gpm);
            
        }else{
        	gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Chemical substance must have a valid chemical structure"));
        	
        }
        return gpm;
    }
}
