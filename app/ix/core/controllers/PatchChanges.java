package ix.core.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jsonpatch.JsonPatch;

import ix.core.controllers.PatchChange.Builder;
import ix.core.controllers.PatchChange.PatchOperation;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.pojopointer.PojoPointer;

public class PatchChanges{
    final private List<PatchChange> changes = new ArrayList<PatchChange>();
    
    public List<PatchChange> getChanges() {
        return changes;
    }
    
    public void setChanges(List<PatchChange> changes) {
        this.changes.clear();
        this.changes.addAll(changes);
    }

    public PatchChanges and(PatchChange change){
        this.changes.add(change);
        return this;
    }
    
    public PatchChanges replace(PojoPointer path, Object value){
        return and(b->b.op(PatchOperation.REPLACE)
                       .value(value)
                       .path(path)
                       .build());
    }
    public PatchChanges add(PojoPointer path, Object value){
        return and(b->b.op(PatchOperation.ADD)
                       .value(value)
                       .path(path)
                       .build());
    }
    public PatchChanges remove(PojoPointer path){
        return and(b->b.op(PatchOperation.REMOVE)
                       .path(path)
                       .build());
    }
    public PatchChanges copy(PojoPointer from, PojoPointer to){
        return and(b->b.op(PatchOperation.COPY)
                       .from(from)
                       .path(to)
                       .build());
    }
    
    public PatchChanges move(PojoPointer from, PojoPointer to){
        return and(b->b.op(PatchOperation.MOVE)
                       .from(from)
                       .path(to)
                       .build());
    }
    
    public PatchChanges merge(PojoPointer path, Object value){
        return and(b->b.op(PatchOperation.MERGE)
                       .value(value)
                       .path(path)
                       .build());
    }
    public PatchChanges and(Function<Builder,PatchChange> using){
        return and(using.apply(new PatchChange.Builder()));
    }

    public JsonPatch asJsonPatch(EntityWrapper<?> target) throws IOException{
        ArrayNode an = (new ObjectMapper()).createArrayNode();
        for(PatchChange pc: changes){
            an.addAll(pc.asJsonChange(target));
        }
        return JsonPatch.fromJson(an);
    }
    
}