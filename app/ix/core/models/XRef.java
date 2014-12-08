package ix.core.models;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import javax.persistence.*;

import play.Logger;
import play.db.ebean.Model;
import ix.utils.Global;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="ix_core_xref")
public class XRef extends Model {
    @JsonIgnore
    @Id
    public Long id;

    /**
     * not id of the XRef instance but id of the instance for which this
     * XRef is pointing to
     */
    public Long refid; 

    @Column(length=512,nullable=false)
    public String kind;

    @JsonIgnore
    @Column(length=512)
    @Transient
    public String _table; // internal database table name

    @JsonIgnore
    @Transient
    @Indexable(indexed=false)
    public Object _instance; // instance of the object 

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_xref_property")
    public List<Value> properties = new ArrayList<Value>();

    public XRef () {
    }

    public XRef (String namespace, String kind, Long id) {
        if (id == null)
            throw new IllegalArgumentException
                ("Can't create XRef with no id");
        if (namespace == null)
            throw new IllegalArgumentException
                ("Namespace parameter can't be null");
        this.kind = kind;
        this.refid = id;
    }

    public XRef (Object instance) {
        Class cls = instance.getClass();
        if (null == cls.getAnnotation(Entity.class))
            throw new IllegalArgumentException
                ("Can't create XRef for non-Entity instance");
        try {
            /*
            Method m = cls.getMethod("getId");
            if (m == null)
                throw new IllegalArgumentException
                    ("Entity does not have getId method!");

            Class c = m.getReturnType();
            if (!Long.class.isAssignableFrom(c))
                throw new IllegalArgumentException
                    ("Entity's getId must return a Long!");
            */

            for (Field f : cls.getFields()) {
                if (null != f.getAnnotation(Id.class)) {
                    Object id = f.get(instance);
                    if (id != null && id instanceof Long) {
                        this.refid = (Long)id;
                    }
                    break;
                }
            }

            kind = cls.getName();
            if (refid == null)
                throw new IllegalArgumentException
                    (cls.getName()+": Can't create XRef for Entity "
                     +"with no Id defined!");
        }
        catch (Exception ex) {
            throw new IllegalArgumentException (ex);
        }

        Table tab = (Table)cls.getAnnotation(Table.class);
        if (tab != null) {
            _table = tab.name();
        }
        this._instance = instance;
    }

    public Object deRef () {
        return deRef (false);
    }

    public Object deRef (boolean force) {
        if (_instance == null || force) {
            try {
                Model.Finder finder = new Model.Finder
                    (Long.class, Class.forName(kind));
                _instance = finder.byId(refid);
            }
            catch (Exception ex) {
                Logger.trace("Can't retrieve XRef "+kind+":"+refid, ex);
            }
        }
        return _instance;
    }

    public String getHRef () {
        return Global.getRef(kind, refid);
    }
}
