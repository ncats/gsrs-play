package models.core;

import play.db.ebean.*;
import javax.persistence.*;

@Entity
@Table(name="ct_processing_status")
public class ProcessingStatus extends Model {
    public enum Status {
        COMPLETE, RUNNING, FAILED
    }

    @Id
    public Long id;

    /**
     * job status
     */
    public Status status;

    /**
     * detailed status message
     */
    @Column(length=4000)
    public String message;

    /**
     * payload content (if any)
     */
    @OneToOne(cascade=CascadeType.ALL)
    public Payload payload; 

    public ProcessingStatus () {}
}
