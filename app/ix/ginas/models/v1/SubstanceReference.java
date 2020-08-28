package ix.ginas.models.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ix.core.util.GinasPortalGun;
import ix.ginas.models.GinasAccessReferenceControlled;
import ix.ginas.models.GinasCommonSubData;
import ix.ginas.models.utils.JSONEntity;
import ix.utils.Util;

@Entity
@Table(name="ix_ginas_substanceref")
@JSONEntity(name = "substanceReference", isFinal = true)
public class SubstanceReference extends GinasCommonSubData {

    public static SubstanceReference newReferenceFor(Substance s){
        SubstanceReference ref = new SubstanceReference();
        ref.refuuid = s.getUuid().toString();
        ref.refPname = s.getName();
        ref.approvalID = s.approvalID;
        ref.substanceClass = Substance.SubstanceClass.reference.toString();

        return ref;
    }
    @JSONEntity(title = "Substance Name")
    @Column(length=1024)
    public String refPname;
    
    @JSONEntity(isRequired = true)
    @Column(length=128)
    public String refuuid;
    
    @JSONEntity(values = "JSONConstants.ENUM_REFERENCE")
    public String substanceClass;
    
    @Column(length=32)
    public String approvalID;

    public SubstanceReference () {}
    
    public String getLinkingID(){
        if(approvalID!=null){
                return approvalID;
        }
        if(refuuid!=null){
                return refuuid;
        }
        return refPname;
    }
    
    /**
     * Create a new copy of this reference without the
     * same UUID so the database will treat it as a new object.
     * @return
     */
    public SubstanceReference copyWithNullUUID(){
        SubstanceReference ref = new SubstanceReference();
        ref.refuuid = this.refuuid;
        ref.refPname =this.refPname;
        ref.approvalID = this.approvalID;
        ref.substanceClass = this.substanceClass;
        //this should be null anyway but make it explicit.
        ref.uuid = null;
        return ref;
    }

    @JsonProperty("_refPname")
    public String getHtmlRefPname(){
        return Util.getStringConverter().toHTML(refPname);
    }

    public String getName(){
    	if(refPname!=null)
    		return refPname;
    	String rep= getLinkingID();
    	if(rep==null){
    		return "NO_NAME";
    	}
    	return rep;
    }

    @Override
    public String toString() {
        return "SubstanceReference{" +
                "refPname='" + refPname + '\'' +
                ", refuuid='" + refuuid + '\'' +
                ", substanceClass='" + substanceClass + '\'' +
                ", approvalID='" + approvalID + '\'' +
                '}';
    }

    /**
     * <p>
     * This is called after the substanceReference is loaded from the database. Here,
     * it specifically attempts to fetch any more recent version of the substance and uses
     * the information associated (approvalID, uuid, pt) with that rather than the information
     * stored on the subref itself. This is a little bit of a hack, as it would be better to
     * have this all handled automatically by the database in a very strong linked fashion.
     * The reason we can't do that right now is that there are times when the substance
     * reference is either to a substance which has not been imported yet, or may not ever
     * be imported.
     *
     * </p>
     *
     * There are 3 main use cases that stop us from changing the database setup directly:
     *
     * <ol>
     * <li>
     *    Bulk loading, where a relationship is referenced before the substance is. We need to be
     *    able to make the proper relationships more "solid" once the corresponding substance is
     *    imported. There are other ways to deal with this, like pulling the relationships outside
     *    of the object, or storing them in some temporary table until the loading is complete.
     * </li>
     * <li>
     *    Individual cherry-picked loading of substances. This is the same issue as above, but
     *    it's not coming from a bulk load but a selected one-at-a-time load. It may be possible
     *    that a related substance is never actually imported, or is imported much later.
     * </li>
     * <li>
     *    In very rare cases, this object may be used for storing a relationship to an entity which
     *    does not yet exist in any database, but will serve as a placeholder for whenever it does
     *    (if it ever does get made). This is such a rare occurrence, and it's not actually supported
     *    by the current forms. We may not want to consider this a real scenario.
     * </li>
     * </ol>
     */
    @PostLoad
    public void postLoad(){
    	//Use of the "portal gun" is not ideal
    	Optional<SubstanceReference> osr=GinasPortalGun.getUpdatedSubstanceReference(this);
    	if(osr.isPresent()){
    		SubstanceReference newRef = osr.get();
    		this.approvalID=newRef.approvalID;
    		this.refuuid=newRef.refuuid;
    		this.refPname=newRef.refPname;
    	}


    }

    /**
     * Tests if the referenced record is the same for the given {@link SubstanceReference}
     * as for this one. This just compares the refuuid field.
     * @param sr
     * @return
     */
    public boolean isEquivalentTo(SubstanceReference sr){
    	if(sr==null) return false;
    	return Objects.equals(sr.refuuid, this.refuuid);
    }


    /**
     * Tests if the supplied {@link Substance} is being referenced
     * by this substance reference. This is done by first instantiating
     * a {@link SubstanceReference} from the {@link Substance} and then
     * using {@link #isEquivalentTo(SubstanceReference)} for testing.
     *
     * @param s
     * @return
     */
    public boolean isReferencingSubstance(Substance s){
    	return this.isEquivalentTo(s.asSubstanceReference());
    }


	@Override
	@JsonIgnore
	public List<GinasAccessReferenceControlled> getAllChildrenCapableOfHavingReferences() {
		List<GinasAccessReferenceControlled> temp = new ArrayList<GinasAccessReferenceControlled>();
		return temp;
	}
}
