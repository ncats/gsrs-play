package ix.core.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.StreamUtil;
import ix.core.util.pojopointer.ArrayPath;
import ix.core.util.pojopointer.IDFilterPath;
import ix.core.util.pojopointer.IdentityPath;
import ix.core.util.pojopointer.PojoPointer;
import ix.utils.Tuple;
import ix.utils.Util;

public class PatchChange {
    public static enum PatchOperation {
		ADD("add"), 
		REMOVE("remove"), 
		REPLACE("replace"), 
		COPY("copy"), 
		MOVE("move"), 
		MERGE("merge");
		private final String opName;

		PatchOperation(String name) {
			this.opName = name;
		}

		public String toName() {
			return this.opName;
		}
	}

	private PatchChange.PatchOperation op;
	
	public PatchChange.PatchOperation getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = PatchOperation.valueOf(op.toUpperCase());
    }

    public PojoPointer getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = PojoPointer.fromURIPath(path);
    }

    public PojoPointer getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = PojoPointer.fromURIPath(from);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private PojoPointer path; //JsonPointer
	private PojoPointer from;
	private Object value;
	
	

	public List<JsonNode> asJsonChange(EntityWrapper<?> wrapped) {
	    JsonPointer jp = toJsonPointer(path, wrapped);
	    ObjectMapper om = new ObjectMapper();
		if(this.op == PatchOperation.MERGE){
            JsonNode jsn = om.valueToTree(value);
            return StreamUtil.forIterator(jsn.fields())
                      .map(Tuple::of)
                      .map(t->{
                          ObjectNode ob = (new ObjectMapper()).createObjectNode();
                          ob.put("op", PatchOperation.REPLACE.toName());
                          ob.put("path", jp.toString() + "/" + t.k());
                          ob.put("value", t.v());
                          return ob;
                      })
                      .collect(Collectors.toList());
        }
		
		List<JsonNode> jslist = new ArrayList<JsonNode>();
		
		ObjectNode ob = om.createObjectNode();
		
		ob.put("op", op.toName());
		ob.put("path", jp.toString());
		if (from != null) {
			JsonPointer jpfrom = toJsonPointer(from, wrapped);
			ob.put("from", jpfrom.toString());
		}
		if(value!=null){
		    
		    
		    ob.put("value", EntityWrapper.of(value).toFullJsonNode());
		}else{
		    ob.put("value", (String)null);
		}
		
		jslist.add(ob);
		return jslist;
	}

	private static JsonPointer toJsonPointer(PojoPointer enhanced, EntityWrapper<?> obj) {
		PojoPointer pcur = enhanced;
		EntityWrapper<Object> currentThing = (EntityWrapper<Object>) obj;
		PojoPointer pstd = new IdentityPath();
		PojoPointer pstdtot = pstd;

		while (pcur != null) {
			if (!(pcur instanceof IdentityPath)) {
				PojoPointer pnext = pcur.headOnly();
				if(pcur.hasTail() || pcur instanceof IDFilterPath){
				    EntityWrapper<Object> val = (EntityWrapper<Object>) currentThing.at(pnext).get();
	                if (pnext instanceof IDFilterPath) {
	                    Tuple<Integer, Object> tup = currentThing.streamArrayElements()
                            	                            .map(Util.toIndexedTuple())
                            	                            .filter(t -> t.v() == val.getValue())
                            	                            .findFirst()
                            	                            .get();
	                    pstd.tail(new ArrayPath(tup.k()));
	                } else {
	                    pstd.tail(pnext);
	                }
	                currentThing = val;
				}else{
				   pstd.tail(pnext);
				}
				pstd = pstd.tail();
			}
			pcur = pcur.tail();
		}
		return pstdtot.toJsonPointer();
	}

	public static class Builder {
		private PatchChange.PatchOperation op;
		private PojoPointer path;
		private PojoPointer from;
		private Object value;

		public Builder op(PatchChange.PatchOperation op) {
			this.op = op;
			return this;
		}

		public Builder path(PojoPointer path) {
			this.path = path;
			return this;
		}

		public Builder from(PojoPointer from) {
			this.from = from;
			return this;
		}

		public Builder value(Object value) {
			this.value = value;
			return this;
		}

		public PatchChange build() {
			return new PatchChange(this);
		}
	}

	private PatchChange(Builder builder) {
		this.op = builder.op;
		this.path = builder.path;
		this.from = builder.from;
		this.value = builder.value;
	}
	
	public PatchChange(){}
}