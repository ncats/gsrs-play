package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.core.controllers.AdminFactory;
import ix.core.models.Group;
import ix.ginas.models.v1.Code;
import ix.ginas.utils.GinasGlobal;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author Egor Puzanov
 */

public class SetAccessCodeProcessor implements EntityProcessor<Code>{

    public Map<String, Set<Group>> codeSystemAccess = new HashMap<String, Set<Group>>();

    public SetAccessCodeProcessor(){
        this(new HashMap<String, HashMap<String, List<String>>>());
    }

    public SetAccessCodeProcessor(Map m){
        Runnable r = new Runnable(){
            @Override
            public void run(){
                Map<String, List<String>> csa = Optional.ofNullable((Map<String, List<String>>) m.get("codeSystemAccess")).orElse(new HashMap<String, List<String>>());
                csa.forEach((codeSystem, groups) -> {
                    Set<Group> access = new LinkedHashSet<Group>();
                    for ( String g : groups ) {
                        access.add(AdminFactory.registerGroupIfAbsent(new Group(g)));
                    }
                    codeSystemAccess.put(codeSystem, access);
                });
            }
        };
        GinasGlobal.runAfterStart(r);
    }

    @Override
    public void prePersist(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
        Set<Group> defaultAccess = codeSystemAccess.get("*");
        Set<Group> access = codeSystemAccess.getOrDefault(obj.codeSystem, defaultAccess);
        if(access != null){
            obj.setAccess(access);
        }
    }

    @Override
    public void postPersist(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void preRemove(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void postRemove(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void preUpdate(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
        prePersist(obj);
    }

    @Override
    public void postUpdate(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void postLoad(Code obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

}
