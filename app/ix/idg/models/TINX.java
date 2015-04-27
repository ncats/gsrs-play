package ix.idg.models;

import ix.core.models.EntityModel;
import ix.core.models.Keyword;
import ix.core.models.Publication;
import ix.core.models.Value;
import ix.core.models.XRef;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ix_idg_tinx")
public class TINX extends play.db.ebean.Model {
    @Id public Long id;

    @Column(nullable=false)
    public String uniprotId;
    @Column(nullable=false)
    public String doid;
    public Double novelty;
    public Double importance;

    public TINX() {
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public String getDoid() {
        return doid;
    }

    public Double getNovelty() {
        return novelty;
    }

    public Double getImportance() {
        return importance;
    }

    public TINX(String uniprotId, String doid, Double novelty, Double importance) {
//        this.id = uniprotId + "#" + doid;
        this.uniprotId = uniprotId;
        this.doid = doid;
        this.novelty = novelty;
        this.importance = importance;
    }

    @Override
    public String toString() {
        return "TINX{" +
                "uniprotId='" + uniprotId + '\'' +
                ", doid='" + doid + '\'' +
                '}';
    }
}
