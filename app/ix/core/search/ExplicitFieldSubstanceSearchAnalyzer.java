package ix.core.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	public Collection<KVPair> flattenObject(Substance o){
		//if(true)return MapObjectUtils.flatten(MapObjectUtils.ObjectToMap(o));
		
		final List<KVPair> m2 = new ArrayList<KVPair>();		
		
		m2.add(KVPair.make("approvalID", o.approvalID));
		
		Util.forEachIndex(o.names,(i,n)->{
			m2.add(KVPair.make("names.name", n.name));
		});
		
		Util.forEachIndex(o.codes,(i,c)->{
			m2.add(KVPair.make("codes.code", c.code));
			m2.add(KVPair.make("codes.codeSystem", c.codeSystem));
			m2.add(KVPair.make("codes.comments", c.comments));
		});
		
		Util.forEachIndex(o.references,(i,ref)->{
			m2.add(KVPair.make( "references.citation", ref.citation));
			m2.add(KVPair.make( "references.docType", ref.docType));
		});
		
		Util.forEachIndex(o.relationships,(i,r)->{
			m2.add(KVPair.make("relationships.qualification", r.qualification));
			m2.add(KVPair.make("relationships.comments", r.comments));
			m2.add(KVPair.make("relationships.interactionType", r.interactionType));
			m2.add(KVPair.make("relationships.relatedSubstance.refPname", r.relatedSubstance.refPname));
			m2.add(KVPair.make("relationships.relatedSubstance.approvalID", r.relatedSubstance.approvalID));
		});
		
		Util.forEachIndex(o.notes,(i,n)->{
			m2.add(KVPair.make("notes.note", n.note));
		});
		
		return m2;
	}
	
}
