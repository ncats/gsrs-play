package ix.ginas.models.v1;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ix.ginas.models.CodeSystemVocabularyTermListDeserializer;
import ix.ginas.models.FragmentVocabularyTermListDeserializer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Inheritance
@DiscriminatorValue("FRCV")
public class FragmentControlledVocabulary extends ControlledVocabulary{

@Override
@JsonDeserialize(using = FragmentVocabularyTermListDeserializer.class)
public void setTerms(List<VocabularyTerm> terms) {
	this.terms = new ArrayList<VocabularyTerm>(terms);
}

}
