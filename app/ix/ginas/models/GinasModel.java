package ix.ginas.models;

import java.util.UUID;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import ix.core.models.IxModel;

@MappedSuperclass
public class GinasModel extends IxModel {
    @JsonIgnore
    @Id
    public Long id;

    @Column(nullable=false,length=40)
    public String uuid;

    public GinasModel () {
    }

    @PrePersist
    public void persisted () {
	if (uuid == null) {
	    uuid = UUID.randomUUID().toString();
	}
    }
}
