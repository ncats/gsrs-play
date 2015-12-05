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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "ix_ginas_reference_cit")
public class GinasReference {
	@Id
	Long id;
	
	@ManyToMany(cascade = CascadeType.PERSIST)
    @JsonSerialize(using=KeywordListSerializer.class)    
    public Set<Keyword> references = new LinkedHashSet<Keyword>();
	public String entityType;

	
	public GinasReference(){
		
	}
	public GinasReference(Object o){
		this.entityType=o.getClass().getName();
	}
}
