package ix.test.modelsb;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.Table;

import ix.core.models.BaseModel;
import ix.core.models.Indexable;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.ginas.models.v1.Substance.SubstanceClass;

@Entity
@Table(name = "ix_some_table2")
public class Wat extends BaseModel{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    

    @Id
    public String id;
    
    public String t;
    public int type;
    
    
    @Indexable(facet=true)
    public String getSubstanceClass(){
        return SubstanceClass.values()[type].name();
    }
    
    
    @Override
    public String fetchGlobalId() {
        return this.getClass().getName() + ":" + id;
    }
    
}
