package ix.ginas.models;

import ix.core.models.Keyword;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "ix_ginas_reference_cit")
public class GinasReferenceContainer {
	@Id
	private Long id;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
    @JsonSerialize(using=ReferenceListSerializer.class)    
	@JsonDeserialize(using=ReferenceListDeserializer.class)
    private Set<Keyword> references = new LinkedHashSet<Keyword>();
	private String entityType;

	
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
        this.references = references;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}
