package ix.idg.models;

import ix.core.models.EntityModel;
import ix.core.models.Keyword;
import ix.core.models.Publication;
import ix.core.models.Value;
import ix.core.models.XRef;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ix_idg_tinx")
public class TINX extends EntityModel {

    public String uniprotId;

    public String doid;


    public Double novelty;

    public Double importance;


    public List<Keyword> synonyms = new ArrayList<Keyword>();

    public List<Value> properties = new ArrayList<Value>();

    public List<XRef> links = new ArrayList<XRef>();

    public List<Publication> publications = new ArrayList<Publication>();

    public TINX() {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
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

    public List<Keyword> getSynonyms() {
        return synonyms;
    }

    public List<Value> getProperties() {
        return properties;
    }

    public List<XRef> getLinks() {
        return links;
    }

    public TINX(String uniprotId, String doid, Double novelty, Double importance) {
//        this.id = uniprotId + "#" + doid;
        this.uniprotId = uniprotId;
        this.doid = doid;
        this.novelty = novelty;
        this.importance = importance;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    @Override
    public String toString() {
        return "TINX{" +
                "uniprotId='" + uniprotId + '\'' +
                ", doid='" + doid + '\'' +
                '}';
    }
}
