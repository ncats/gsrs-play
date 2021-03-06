package ix.test.database;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Rule;
import org.junit.Test;

import ix.AbstractGinasServerTest;
import ix.core.controllers.PrincipalFactory;
import ix.core.models.Curation;
import ix.core.models.Edit;
import ix.core.models.Principal;
import ix.test.server.GinasTestServer;


/**
*
* Tests to make sure that basic cascading changes are applied
* as expected in the database
*
*/
public class DataCascadeTest   extends AbstractGinasServerTest {


    @Test
    public void ensureDeletingEditDoesntDeleteUser() {
          Edit e=new Edit();
          e.save();
          
          long editorid=e.editor.id;
          Principal p1=PrincipalFactory.finder.get().byId(editorid);
          assertEquals((long)p1.id,editorid);
          e.delete();
          Principal p2=PrincipalFactory.finder.get().byId(editorid);
          assertNotNull("User should remain retrievable after an edit is deleted",p2);
          assertEquals((long)p2.id,editorid);
    }
    
    @Test
    public void ensureDeletingCurationDoesntDeleteUser() {
    	  Principal user1=PrincipalFactory.registerIfAbsent(new Principal("user1",null));
    	  user1.save();
    	  
    	  Curation e=new Curation();
          e.curator=user1;
          e.save();
          long editorid=user1.id;
          Principal p1=PrincipalFactory.finder.get().byId(editorid);
          assertEquals((long)p1.id,editorid);
          e.delete();
          Principal p2=PrincipalFactory.finder.get().byId(editorid);
          assertNotNull("User should remain retrievable after a curation is deleted",p2);
          assertEquals((long)p2.id,editorid);
    }

}
