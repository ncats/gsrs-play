package ix.ginas.models.test;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.models.BaseModel;
import ix.core.models.DynamicFacet;
import ix.core.models.Indexable;
import ix.ginas.models.CommonDataElementOfCollection;
import ix.ginas.models.utils.JSONConstants;
import ix.ginas.models.utils.JSONEntity;

@JSONEntity (title = "Code", isFinal = true)
//@Entity
//@Table(name="TEST_SUB3")
public class TestModel extends BaseModel
{
	
    
	@Id
    @Column(name="uuid")
    @Indexable(name="UUID_TEST", suggest=true)
    public String uuid;

    
	@Column(name="approval_id")
    public String approvalId;
    	
	
	//@Override
	public String fetchGlobalId() {
		return uuid.toString();
	}
    
    
}
