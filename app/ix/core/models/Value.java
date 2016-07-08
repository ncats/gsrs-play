package ix.core.models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

@Entity
@Table(name="ix_core_value")
@Inheritance
@DiscriminatorValue("VAL")
public class Value extends LongBaseModel implements Serializable{
    @Id
    public Long id;
    public String label;
    
    public Value () {}
    public Value (String label) {
        this.label = label;
    }

    @JsonIgnore
    public Object getValue () {
        throw new UnsupportedOperationException
            ("getValue is not defined for class "+getClass().getName());
    }
}
