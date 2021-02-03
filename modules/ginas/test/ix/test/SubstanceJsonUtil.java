package ix.test;

import static org.junit.Assert.assertTrue;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.models.Keyword;
import ix.ginas.modelBuilders.SubstanceBuilder;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.v1.*;
import play.libs.ws.WSResponse;
import util.json.JsonUtil;
import util.json.JsonUtil.JsonNodeBuilder;

public final class SubstanceJsonUtil {

	private SubstanceJsonUtil(){
		//can not instantiate
	}
	
	public static void assertEquals(JsonNode a, JsonNode b, Comparator<JsonNode> comparator){

		if(!equals(a,b, comparator)){
			throw new AssertionError("expected : " + a +" but was : " + b);
		}
	}

	public static boolean equals(JsonNode a, JsonNode b, Comparator<JsonNode> comparator) {
		if(a instanceof ObjectNode){
			return objectNodeEqualTraversal((ObjectNode) a, b, comparator);
		}else if( a instanceof ArrayNode){
			return arrayNodeEqualTraversal((ArrayNode) a, b, comparator);
		}else{
			return comparator.compare(a, b) ==0;
		}
	}

	private static <K, V> Map<K, V> toMap(Iterator<Map.Entry<K,V>> iterator){
		LinkedHashMap<K,V> map = new LinkedHashMap<>();
		while(iterator.hasNext()){
			Map.Entry<K,V> e = iterator.next();
			map.put(e.getKey(), e.getValue());
		}
		return map;
	}
	private static boolean objectNodeEqualTraversal(ObjectNode a, JsonNode o, Comparator<JsonNode> comparator){
		if (!(o instanceof ObjectNode)) {
			return false;
		}
		ObjectNode other = (ObjectNode) o;
		Map<String, JsonNode> m1 = toMap(a.fields());
		Map<String, JsonNode> m2 = toMap(other.fields());

		final int len = m1.size();
		if (m2.size() != len) {
		    System.out.println("obj node size notequal "+ m2.size() + "vs "+ len);
			return false;
		}

		for (Map.Entry<String, JsonNode> entry : m1.entrySet()) {
			JsonNode v2 = m2.get(entry.getKey());
			if ((v2 == null) || !equals(entry.getValue(), v2,comparator)) {
				System.out.println("FAILED : \n"+entry.getValue()+"\n"+ v2);
				return false;
			}
		}
		return true;
	}

	private static <T> List<T> toList(Iterator<T> iter){
		List<T> list = new ArrayList<>();
		while(iter.hasNext()){
			list.add(iter.next());
		}
		return list;
	}
	private static boolean arrayNodeEqualTraversal(ArrayNode a, JsonNode o, Comparator<JsonNode> comparator){

			if (!(o instanceof ArrayNode)) {
				return false;
			}
			ArrayNode other = (ArrayNode) o;
			final int len = a.size();
			if (other.size() != len) {
				return false;
			}
			List<JsonNode> aList = toList(a.iterator());
			aList.sort(comparator);

			List<JsonNode> oList = toList(o.iterator());
			oList.sort(comparator);


			Iterator<JsonNode> aIter = aList.iterator();
			Iterator<JsonNode> oIter = oList.iterator();
			for(; aIter.hasNext() & oIter.hasNext(); ){
				if(! equals(aIter.next(), oIter.next(), comparator)){
					System.out.println("FAILED\n" + aList+"\n"+ oList);
					return false;
				}

			}

			return true;


	}

	/**
	 * Normalizes a substance JSON to be as expected for a typical submission.
	 * Specifically, removing approval information, changing status to pending,
	 * and ensuring that the public domain tag is added to the first
	 * reference.
	 * @param substance
	 * @return
	 */
	public static JsonNode prepareUnapprovedPublic(JsonNode substance){
		
		JsonNodeBuilder jnb=new JsonUtil.JsonNodeBuilder(substance)
				.remove("/approvalID")
				.remove("/approved")
				.remove("/approvedBy")
				.set("/status", "pending")
				.ignoreMissing();
		boolean hasReferences=false;
		if(substance.at("/references")!=null){
			for(JsonNode jsn:substance.at("/references")){
				hasReferences=true;break;
			}
		}
		
		if(hasReferences){
			jnb=jnb.add("/references/0/tags/-", Reference.PUBLIC_DOMAIN_REF);
			jnb=jnb.set("/references/0/publicDomain", true);
			jnb=jnb.set("/references/0/access", Collections.emptyList());
		}else{
			List<String> acc=new ArrayList<String>();
			acc.add("protected");
			jnb = jnb.set("/access", acc);
			
		}
		
		return SubstanceBuilder.from(jnb.build())
		
		                .andThen(s->{
		                	s.relationships
		                	 .stream()
		                	 .map(r->r.relatedSubstance)
		                	 .filter(r->r.approvalID!=null)
		                	 .forEach(r->{
		                		 r.approvalID=null;
		                	 });

		                })
				.andThen(s->{
					s.names.stream()
							.filter(n-> !n.isPublic())
							.forEach(n-> {
								n.setAccess(Collections.emptySet());

								Reference r = createNewPublicDomainRef();
								n.addReference(r, s);
							});
				})

		                .onSubstanceClass( (cls, s) ->{
		                	switch(cls){
								case protein:
								{
									GinasCommonSubData protein = ((ProteinSubstance) s).protein;

									removeUnusedReferencesAndAddPublicIfNeeded(s, protein);

									break;
	}
								case nucleicAcid:
								{
									NucleicAcid na = ((NucleicAcidSubstance)s).nucleicAcid;
									removeUnusedReferencesAndAddPublicIfNeeded(s, na);

									break;
								}
								case chemical:
								{
									ChemicalSubstance cs = (ChemicalSubstance)s;

									removeUnusedReferencesAndAddPublicIfNeeded(s, cs.structure);


									break;
								}
								case mixture:
								{
									removeUnusedReferencesAndAddPublicIfNeeded(s, ((MixtureSubstance)s).mixture);
									break;
								}
								case  polymer:
								{
									removeUnusedReferencesAndAddPublicIfNeeded(s, ((PolymerSubstance)s).polymer);
									break;
								}
								case structurallyDiverse:
								{
									removeUnusedReferencesAndAddPublicIfNeeded(s, ((StructurallyDiverseSubstance)s).structurallyDiverse);
									break;
								}
								case specifiedSubstanceG1:
								{
									removeUnusedReferencesAndAddPublicIfNeeded(s, ((SpecifiedSubstanceGroup1Substance)s).specifiedSubstance);
									break;
								}
							}
						})
				.buildJson();

	}

	private static void removeUnusedReferencesAndAddPublicIfNeeded(Substance s, GinasAccessReferenceControlled definingElement) {
		Set<Keyword> kept = new HashSet<>();
		boolean hasPublicDomainRef=false;
		for(Keyword k : definingElement.getReferences()){

                String value = k.getValue();
                Reference referenceByUUID = s.getReferenceByUUID(value);
                if(null != referenceByUUID){
					if(referenceByUUID.isPublic() && referenceByUUID.publicDomain){
						hasPublicDomainRef = true;
					}
                    kept.add(k);
                }
            }
		definingElement.setReferences(kept);
		if(!hasPublicDomainRef || definingElement.getReferences().isEmpty()){
            Reference r = createNewPublicDomainRef();
            definingElement.addReference(r, s);
        }
	}

	private static Reference createNewPublicDomainRef(){
		Reference r = new Reference();
		r.publicDomain = true;
		r.setAccess(Collections.emptySet());
		r.addTag(Reference.PUBLIC_DOMAIN_REF);
		r.getOrGenerateUUID();
		return r;
	}
	public static JsonNode prepareUnapproved(JsonNode substance){
		
		return new JsonUtil.JsonNodeBuilder(substance)
				.remove("/approvalID")
				.remove("/approved")
				.remove("/approvedBy")
				
				.set("/status", "pending")
				.ignoreMissing().build();
		
		
	}

	public static JsonNode ensureFailure(WSResponse response){
		int status = response.getStatus();
		String body = response.getBody();
		System.out.println("Response is:" + body);
		assertTrue("Expected failure code, got:" + status +"\n"+body, status != 200 && status != 201);
		try{
			return response.asJson();
		}catch(Exception e){
				return new ObjectMapper().createObjectNode();
		}
	}

	public static JsonNode ensurePass(WSResponse response){
		int status = response.getStatus();
		try{
			assertTrue("Expected pass code, got:" + status +" message = " + response.getStatusText(), status == 200 || status == 201);
		}catch(Throwable e){
			System.err.println(response.getBody());
			throw e;
		}
		return response.asJson();
	}


	public static boolean isLiteralNull( JsonNode js){
		return js.isNull();
	}

	public static void ensureIsValid(JsonNode js){
		try{
			assertTrue( isValid(js));
		}catch(Throwable e){
			System.err.println(js.toString());
			throw e;
		}
	}
	
	public static boolean isValid(JsonNode js){
		return js.get("valid").asBoolean();
	}

    public static String getApprovalStatus(JsonNode js){
        return js.get("status").asText().toLowerCase();
    }

    public static String getApprovalId(JsonNode js){
        return js.get("approvalID").asText();
    }

	public static String getRefUuidOnFirstRelationship(JsonNode js){
		JsonNode relations = js.get("relationships").get(0);
		JsonNode relatedSubs = relations.get("relatedSubstance");
		return relatedSubs.get("refuuid").asText();
	}
	public static String getTypeOnFirstRelationship(JsonNode js){
		return js.at("/relationships/0/type").asText();
	}


}
