package ix.ginas.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;
import ix.core.models.Value;

@Entity
@Table(name="ix_ginas_citation")
public class Citation extends GinasModel {
    public String citation;
    public String docType;
    public Date documentDate;
    
    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_ginas_citation_tag")
    public List<Value> tags = new ArrayList<Value>();

    public Citation () {}
    public Citation (String name) {
        citation = name;
    }
}
