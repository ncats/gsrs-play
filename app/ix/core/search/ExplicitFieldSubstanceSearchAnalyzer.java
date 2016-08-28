package ix.core.search;

import java.util.Map;
import java.util.TreeMap;

import ix.ginas.models.v1.Substance;
import ix.utils.Util;
import play.Play;

public class ExplicitFieldSubstanceSearchAnalyzer extends FieldBasedSearchAnalyzer<Substance>{
	
	public ExplicitFieldSubstanceSearchAnalyzer(){
		recordsToAnalyze=Play.application().configuration()
                .getInt("ix.ginas.maxanalyze", 100);
		enabled=Play.application().configuration()
        .getBoolean("ix.ginas.textanalyzer", false);
	}
	public ExplicitFieldSubstanceSearchAnalyzer(Map m){
		if(m!=null){
			Object enabled=m.get("enabled");
			Object maxanalyze=m.get("maxanalyze");
			if(enabled!=null){
				this.enabled=(Boolean)enabled;
			}
			if(maxanalyze!=null){
				this.recordsToAnalyze=((Number)maxanalyze).intValue();
			}
		}   
	}
	public Map<String,String> flattenObject(Substance o){
		//if(true)return MapObjectUtils.flatten(MapObjectUtils.ObjectToMap(o));
		
		final Map<String,String> m2 = new TreeMap<String,String>();		
		
		Util.forEachIndex(o.names,(i,n)->{
			m2.put("names[" + i + "].name", n.name);
		});
		
		Util.forEachIndex(o.codes,(i,n)->{
			m2.put( "codes[" + i + "].code", n.code);
			m2.put( "codes[" + i + "].codeSystem", n.codeSystem);
			m2.put( "codes[" + i + "].comments", n.comments);
		});
		
		Util.forEachIndex(o.references,(i,n)->{
			m2.put( "references[" + i + "].citation", n.citation);
			m2.put( "references[" + i + "].docType", n.docType);
		});
		
		Util.forEachIndex(o.relationships,(i,n)->{
			m2.put( "relationships[" + i + "].qualification", n.qualification);
			m2.put( "relationships[" + i + "].comments", n.comments);
			m2.put( "relationships[" + i + "].interactionType", n.interactionType);
			m2.put( "relationships[" + i + "].relatedSubstance.refPname", n.relatedSubstance.refPname);
			m2.put( "relationships[" + i + "].relatedSubstance.approvalID", n.relatedSubstance.approvalID);
		});
		
		Util.forEachIndex(o.notes,(i,n)->{
			m2.put( "notes[" + i + "].note", n.note);
		});
		
		return m2;
	}
	
}
