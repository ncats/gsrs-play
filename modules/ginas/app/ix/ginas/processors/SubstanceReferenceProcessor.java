package ix.ginas.processors;

import ix.core.EntityProcessor;
import ix.ginas.controllers.v1.SubstanceFactory;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Egor Puzanov
 */

public class SubstanceReferenceProcessor implements EntityProcessor<SubstanceReference>{

    private static Pattern fakeIdPattern = Pattern.compile("FAKE_ID:([0-9A-Z]{10})");
    public String codeSystem;

    public SubstanceReferenceProcessor() {
        this(new HashMap<String, String>());
    }

    public SubstanceReferenceProcessor(Map m){
        codeSystem = Optional.ofNullable((String) m.get("codeSystem")).orElse("UNII");
    }

    @Override
    public void prePersist(SubstanceReference obj) throws ix.core.EntityProcessor.FailProcessingException {
        Substance relatedSubstance = null;
        if (relatedSubstance == null) {
            if (obj.refuuid != null && !obj.refuuid.isEmpty()) {
                Matcher matcher = fakeIdPattern.matcher(obj.refuuid);
                if (matcher.find()) {
                    obj.setApprovalID(matcher.group(1));
                    obj.setRefuuid(UUID.randomUUID().toString());
                } else {
                    relatedSubstance = SubstanceFactory.getSubstance(obj.refuuid);
                }
            } else {
                obj.setRefuuid(UUID.randomUUID().toString());
            }
        }
        if (relatedSubstance == null) {
            relatedSubstance = SubstanceFactory.getSubstancesWithExactCode(100, 0, obj.approvalID, codeSystem).stream().findFirst().orElse(null);
        }
        if (relatedSubstance == null) {
            relatedSubstance = SubstanceFactory.getSubstancesWithExactName(100, 0, obj.refPname).stream().findFirst().orElse(null);
        }
        if (relatedSubstance instanceof Substance) {
            obj.setRefuuid(relatedSubstance.getUuid().toString());
            obj.setRefPname(relatedSubstance.getName());
            if ( obj.refPname != null && obj.refPname.getBytes().length > 1023) {
                obj.setRefPname(Util.getStringConverter().truncate(obj.refPname, 1023));
            }
            obj.setApprovalID(relatedSubstance.approvalID);
            obj.setSubstanceClass(Substance.SubstanceClass.reference.toString());
        }
    }

    @Override
    public void postPersist(SubstanceReference obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void preRemove(SubstanceReference obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void postRemove(SubstanceReference obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void preUpdate(SubstanceReference obj) throws ix.core.EntityProcessor.FailProcessingException {
        prePersist(obj);
    }

    @Override
    public void postUpdate(SubstanceReference obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void postLoad(SubstanceReference obj) throws ix.core.EntityProcessor.FailProcessingException {
        // TODO Auto-generated method stub

    }

}