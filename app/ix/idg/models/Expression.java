package ix.idg.models;

import ix.core.models.Indexable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ix_idg_expression")
public class Expression extends play.db.ebean.Model {
    public static final String EXPR_QUAL = "Expression Qualitative";

    @Id public Long id;

    @Column(nullable=false)
    public String tissue;

    @Column(nullable = false)
    public String source;

    public String sourceid;

    public Double confidence;
    public Double numberValue;

    @Column(nullable = true)
    @Indexable(facet=true,name=EXPR_QUAL)
    public String qualValue;

    public String evidence;

    public Expression() {
    }

    public String getSourceid() {
        return sourceid;
    }

    public String getSource() {
        return source;
    }

    public String getTissue() {
        return tissue;
    }

    public Double getConfidence() {
        return confidence;
    }

    public Double getNumberValue() {
        return numberValue;
    }

    public String getQualValue() {
        return qualValue;
    }

    public String getEvidence() {
        return evidence;
    }

    public Expression(String tissue, Double confidence, Double numberValue, String qualValue, String evidence) {
        this.tissue = tissue;
        this.confidence = confidence;
        this.numberValue = numberValue;
        this.qualValue = qualValue;
        this.evidence = evidence;
    }


    @Override
    public String toString() {
        return "Expression{source='"+source+"' sourceId='"+sourceid+"' qualitative="+qualValue+"}";
    }
}
