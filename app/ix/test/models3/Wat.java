package ix.test.models3;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.Table;

import ix.core.models.BaseModel;
import ix.core.util.EntityUtils.EntityWrapper;

@Entity
@Table(name = "ix_some_table2")
public class Wat extends BaseModel
{
    
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    

    @Id
    public String id;
    
    public String t;
    
    
    //@Override
    public String fetchGlobalId() {
        // TODO Auto-generated method stub
        return this.getClass().getName() + ":" + id;
    }
    
    
    @PostPersist
    public void test(){
        System.out.println("Saved:" + EntityWrapper.of(this).getKey());
    }
}
