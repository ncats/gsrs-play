package ix.ginas.models.v1;

import ix.ginas.models.utils.JSONEntity;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import ix.core.models.Indexable;
import ix.core.models.Keyword;
import ix.core.models.Principal;
import ix.ginas.models.*;


@JSONEntity(title = "Note", isFinal = true)
@Entity
@Table(name="ix_ginas_note")
public class Note extends CommonDataElementOfCollection {

    @JSONEntity(title = "Note")
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String note;

    public Note () {}
    public Note (String note) {
        this.note = note;
    }
	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();

		return temp;
	}

    @Override
    public String toString() {
        return "Note{" +
                "note='" + note + '\'' +
                '}';
    }
}
