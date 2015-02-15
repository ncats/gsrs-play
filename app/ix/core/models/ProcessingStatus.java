package ix.core.models;

import java.util.Date;
import play.db.ebean.*;
import javax.persistence.*;

@Entity
@Table(name="ix_core_processingstatus")
public class ProcessingStatus extends Model {
    public enum Status {
        COMPLETE, RUNNING, FAILED, PENDING, STOPPED
    }

    @Id
    public Long id;
    public final Date created = new Date ();
    public Date modified;

    /**
     * job status
     */
    public Status status;

    /**
     * detailed status message
     */
    @Lob
    @Basic(fetch=FetchType.EAGER)
    public String message;

    /**
     * payload content (if any)
     */
    @OneToOne(cascade=CascadeType.ALL)
    public Payload payload; 

    public ProcessingStatus () {}

    @PrePersist
    @PreUpdate
    public void modified () {
        this.modified = new Date ();
    }
}
