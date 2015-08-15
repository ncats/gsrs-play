package ix.core.models;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import play.db.ebean.*;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonProperty;
import ix.utils.Global;

@Entity
@Table(name="ix_core_procjob")
public class ProcessingJob extends Model {
    public enum Status {
        COMPLETE, RUNNING, NOT_RUN, FAILED, PENDING, STOPPED, UNKNOWN
    }
    
    @Id
    public Long id;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_procjob_key")
    public List<Keyword> keys = new ArrayList<Keyword>();

    public Status status = Status.PENDING;
    @Column(name="job_start")
    public Long start;
    @Column(name="job_stop")
    public Long stop;

    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String message;
    
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String statistics;

    @OneToOne(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public Principal owner;
    
    @OneToOne(cascade=CascadeType.ALL)
    @JsonView(BeanViews.Full.class)
    public Payload payload;
    

    public ProcessingJob () {
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_payload")
    public String getJsonPayload () {
        return payload != null
            ? Global.getRef(getClass (), id)+"/payload" : null;
    }

    @JsonView(BeanViews.Compact.class)
    @JsonProperty("_owner")
    public String getJsonOwner () {
        return owner != null
            ? Global.getRef(getClass (), id)+"/owner" : null;
    }
}
