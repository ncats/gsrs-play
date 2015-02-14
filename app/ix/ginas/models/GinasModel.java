package ix.ginas.models;

import java.util.UUID;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import play.db.ebean.Model;
import ix.core.models.IxModel;

@MappedSuperclass
public class GinasModel extends IxModel {
    @Id
    public UUID uuid;

    public GinasModel () {
    }
}
