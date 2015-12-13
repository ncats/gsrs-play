package ix.ginas.models;

import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.ginas.models.v1.GinasChemicalStructure;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class StructureSerializer extends JsonSerializer<GinasChemicalStructure> {
    public StructureSerializer () {}
    public void serializeValue (GinasChemicalStructure struc, JsonGenerator jgen,
                                SerializerProvider provider)
        throws IOException, JsonProcessingException {
        if (struc == null) {
            provider.defaultSerializeNull(jgen);
            return;
        }
        provider.defaultSerializeField("id", struc.id, jgen);
        provider.defaultSerializeField("created", struc.created, jgen);
        provider.defaultSerializeField("lastEdited", struc.lastEdited, jgen);
        provider.defaultSerializeField("deprecated", struc.deprecated, jgen);
        provider.defaultSerializeField("digest", struc.digest, jgen);
        provider.defaultSerializeField("molfile", struc.molfile, jgen);
        provider.defaultSerializeField("smiles", struc.smiles, jgen);
        provider.defaultSerializeField("formula", struc.formula, jgen);
        provider.defaultSerializeField
            ("stereochemistry", struc.stereoChemistry, jgen);
        provider.defaultSerializeField
            ("opticalActivity", struc.opticalActivity, jgen);
        provider.defaultSerializeField
            ("atropisomerism", struc.atropisomerism, jgen);
        provider.defaultSerializeField
            ("stereoComments", struc.stereoComments, jgen);
        provider.defaultSerializeField
            ("stereoCenters", struc.stereoCenters, jgen);
        provider.defaultSerializeField
            ("definedStereo", struc.definedStereo, jgen);
        provider.defaultSerializeField("ezCenters", struc.ezCenters, jgen);
        provider.defaultSerializeField("charge", struc.charge, jgen);
        provider.defaultSerializeField("mwt", struc.mwt, jgen);
        if(struc.createdBy!=null)
        	provider.defaultSerializeField("createdBy", struc.createdBy.username, jgen);
        if(struc.lastEditedBy!=null)
        	provider.defaultSerializeField("lastEditedBy", struc.lastEditedBy.username, jgen);
        if(struc.recordReference!=null){
        	ObjectMapper om = new ObjectMapper();
        	JsonNode jsn=om.valueToTree(struc.recordReference);
        	provider.defaultSerializeField
            ("references", jsn.get("references"), jgen);
        }
        
        //provider.defaultSerializeField("references", struc.getReferences(), jgen);
        provider.defaultSerializeField("access", struc.getAccess(), jgen);
        
        
       
        
        for (Value val : struc.properties) {
            if (Structure.H_LyChI_L4.equals(val.label)) {
                Keyword kw = (Keyword)val;
                provider.defaultSerializeField("hash", kw.term, jgen);
            }
        }
        //provider.defaultSerializeField("references", refs, jgen);
    }
    
    public void serialize (GinasChemicalStructure struc, JsonGenerator jgen,
                           SerializerProvider provider)
        throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        serializeValue (struc, jgen, provider);
        jgen.writeEndObject();
    }
}
