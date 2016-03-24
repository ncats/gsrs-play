package ix.core.models;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import ix.core.util.TimeUtil;
import play.db.ebean.Model;
import javax.persistence.*;

@Entity
@Table(name="ix_core_session")
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
}
