package ix.test;

import static org.junit.Assert.*;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import ix.AbstractGinasTest;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;
import ix.core.models.BeanViews;

public class JSONViewTest  extends AbstractGinasTest {

	public static class TestJsonView{
		 	@Lob
		    @Basic(fetch=FetchType.EAGER)
		    public String normal="normal1";
		    
		    @Lob
		    @Basic(fetch=FetchType.EAGER)
		    
		    @JsonView(BeanViews.Internal.class)
		    public String internal="internal1";
		    
		    @Lob
		    @Basic(fetch=FetchType.EAGER)
		    @JsonView(BeanViews.Full.class)
		    public String full="full1";
	}
	
	@Test
	public void TryTest(){
		EntityMapper em1 = EntityFactory.EntityMapper.FULL_ENTITY_MAPPER();
		EntityMapper em2 = EntityFactory.EntityMapper.INTERNAL_ENTITY_MAPPER();
		TestJsonView tt=new TestJsonView();
		JsonNode jsn1 =em1.valueToTree(tt);
		JsonNode jsn2 =em2.valueToTree(tt);
		assertEquals(null,jsn1.get("internal"));
		assertEquals("internal1",jsn2.get("internal").asText());
	}
	
}
