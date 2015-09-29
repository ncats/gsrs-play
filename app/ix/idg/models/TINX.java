package ix.idg.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
    public Double diseaseNovelty;

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

    public Double getDiseaseNovelty() {
        return diseaseNovelty;
    }

    public TINX(String uniprotId, String doid, Double novelty, Double importance, Double diseaseNovelty) {
//        this.id = uniprotId + "#" + doid;
        this.uniprotId = uniprotId;
        this.doid = doid;
        this.novelty = novelty;
        this.importance = importance;
        this.diseaseNovelty = diseaseNovelty;
    }

    @Override
    public String toString() {
        return "TINX{" +
                "uniprotId='" + uniprotId + '\'' +
                ", doid='" + doid + '\'' +
                '}';
    }
}
