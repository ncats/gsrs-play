package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ix.core.models.Indexable;
import ix.core.models.IxModel;
import ix.core.models.Keyword;
import ix.ginas.models.serialization.KeywordListDeserializer;
import ix.ginas.models.serialization.KeywordListSerializer;


@Entity
@Table(name = "ix_ginas_controlled_vocab")
@Inheritance
@DiscriminatorValue("CTLV")
public class ControlledVocabulary extends IxModel {

    private static final long serialVersionUID = 5455592961232451608L;

    //@JsonIgnore
    public Long getId() {
        return this.id;
    }

    ;

    @Column(unique = true)
    @Indexable(name = "Domain", facet = true)
    public String domain;


    public void setTerms(List<VocabularyTerm> terms) {
        this.terms = terms;
    }

    private String vocabularyTermType = VocabularyTerm.class.getName();

    @ManyToMany(cascade = CascadeType.ALL)
    @JsonSerialize(using = KeywordListSerializer.class)
    @JsonDeserialize(using = KeywordListDeserializer.class)
    @Indexable(name = "Field")
    public List<Keyword> fields = new ArrayList<Keyword>();

    public boolean editable = true;

    public boolean filterable = false;


    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    //@JoinTable(name="ix_ginas_cv_terms")
    public List<VocabularyTerm> terms;

    public VocabularyTerm getTermWithValue(String val) {
        for (VocabularyTerm vt : this.terms) {
            //System.out.println("Looking at value:" + vt.display);
            if (vt.value.equals(val)) {

                return vt;
            }
        }
        return null;
    }

    public List<? extends VocabularyTerm> getTerms() {
        return terms;
    }

    public <T extends VocabularyTerm> void addTerms(T term) {
        this.terms.add(term);
    }

    public void setVocabularyTermType(String vocabularyTermType) {
        this.vocabularyTermType = vocabularyTermType;
    }

    public String getVocabularyTermType() {
        return vocabularyTermType;
    }


}
