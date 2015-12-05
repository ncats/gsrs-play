package ix.ginas.utils;

import ix.core.chem.StructureProcessor;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.ginas.models.v1.ChemicalSubstance;
import ix.ginas.models.v1.GinasChemicalStructure;
import ix.ginas.models.v1.Moiety;
import ix.ginas.models.v1.Name;
import ix.ginas.models.v1.Substance;
import play.Logger;

import java.util.ArrayList;
import java.util.List;

public class Validation {
        
    public static List<GinasProcessingMessage> validateAndPrepare(Substance s, GinasProcessingStrategy strat){
        List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
        if(s==null){
            gpm.add(GinasProcessingMessage.ERROR_MESSAGE("Substance cannot be parsed"));
            return gpm;
        }
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
            List<Substance> sr=ix.ginas.controllers.v1.SubstanceFactory.getSubstancesWithExactName(100, 0, n.name);
            if(sr!=null && !sr.isEmpty()){
                Substance s2=sr.iterator().next();
                if(!s2.uuid.toString().equals(s.uuid.toString())){
                    GinasProcessingMessage mes = GinasProcessingMessage
                        .WARNING_MESSAGE("Name '"
                                         + n.name
                                         + "' collides with existing name for substance:"
                                         + s2.getName() + " [" + s2.getApprovalID()
                                         + "]");
                    gpm.add(mes);
                    strat.processMessage(mes);
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
        return gpm;
    }
    public static List<GinasProcessingMessage> validateAndPrepareChemical(ChemicalSubstance cs, GinasProcessingStrategy strat){
        List<GinasProcessingMessage> gpm=new ArrayList<GinasProcessingMessage>();
        
        String payload = cs.structure.molfile;
        if (payload != null) {
            List<Moiety> moietiesForSub = new ArrayList<Moiety>();
            List<Structure> moieties = new ArrayList<Structure>();
            Structure struc = StructureProcessor.instrument
                (payload, moieties, false); // don't standardize
            
            //struc.count
            for(Structure m: moieties){
                Moiety m2= new Moiety();
                m2.structure=(GinasChemicalStructure) m;
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
                    Structure struc2=cs.structure;
                    //String omol = cs.structure.molfile;
                    cs.structure=(GinasChemicalStructure) struc;
                    cs.structure.molfile=struc2.molfile;
                    if(struc2.stereoChemistry!=null){
                        cs.structure.stereoChemistry=struc2.stereoChemistry;
                    }
                    if(struc2.opticalActivity!=null){
                        cs.structure.opticalActivity=struc2.opticalActivity;
                    }
                    if(struc2.stereoComments!=null){
                        cs.structure.stereoComments=struc2.stereoComments;
                    }
                    if(struc2.atropisomerism!=null){
                        cs.structure.atropisomerism=struc2.atropisomerism;
                    }
                                        
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
           
            
            try {
                List<Substance> sr=ix.ginas.controllers.v1.SubstanceFactory.getCollsionChemicalSubstances(100, 0, cs);
                                
                if(sr!=null && !sr.isEmpty()){
                                        
                    int dupes=0;
                    for(Substance s:sr){
                                
                        if(cs.uuid==null || !s.uuid.toString().equals(cs.uuid.toString())){
                            dupes++;
                        }
                    }
                    if(dupes>0){
                        GinasProcessingMessage mes=GinasProcessingMessage.WARNING_MESSAGE("Structure has " + dupes +" possible duplicate(s)");
                        gpm.add(mes);
                        strat.processMessage(mes);                              
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        return gpm;
    }
}
