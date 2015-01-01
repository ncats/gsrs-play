package ix.idg.models;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.DiscriminatorValue;

import ix.core.models.Keyword;

@Entity
@DiscriminatorValue("DIS")
public class Disease extends EntityModel {
    public enum Source {
	DiseaseOntology,
	OMIM,
	UniProt,
	ICD9,
	ICD10,
	ICD11,
	MeSH,
	UMLS
    }

    public Disease () {}
    public Disease (String name) {
	this.name = name;
    }
}
