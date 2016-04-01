package ix.ginas.models;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Keyword;
import ix.core.models.LongBaseModel;

@Entity
@Table(name = "ix_ginas_reference_cit")
public class GinasReferenceContainer extends LongBaseModel{
	
	@ManyToMany(cascade = CascadeType.ALL)
    @JsonSerialize(using=ReferenceSetSerializer.class)    
	@JsonDeserialize(using=ReferenceSetDeserializer.class)
    public Set<Keyword> references = new LinkedHashSet<Keyword>();
    public String entityType;

	
	public GinasReferenceContainer(){
		
	}
	
	public GinasReferenceContainer(Object o){
		this.entityType=o.getClass().getName();
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Keyword> getReferences() {
        return references;
    }

    public void setReferences(Set<Keyword> references) {
        this.references = new LinkedHashSet<Keyword>(references);
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
