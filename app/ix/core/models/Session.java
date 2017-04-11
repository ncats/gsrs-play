package ix.core.models;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ix.core.History;
import ix.core.util.TimeUtil;

@Entity
@Table(name="ix_core_session")
@Indexable(indexed=false)
@History(store=false)
public class Session extends BaseModel {
    @Id public UUID id;
    @OneToOne(cascade=CascadeType.ALL)
    public UserProfile profile;
    
    public final long created = TimeUtil.getCurrentTimeMillis();
    public long accessed = TimeUtil.getCurrentTimeMillis();
    public String location;
    public boolean expired;
        
    public Session () {}
    public Session (UserProfile profile) {
        this.profile = profile;
    }
	@Override
	public String fetchGlobalId() {
		if(this.id==null)return null;
		return this.id.toString();
	}
}
