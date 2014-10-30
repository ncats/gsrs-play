package ix.core.models;

import java.lang.reflect.Method;
import javax.persistence.*;

import play.Logger;
import play.db.ebean.Model;
import ix.utils.Global;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="ix_core_xref")
public class XRef extends Model {
    @Id
    public Long id;

    @Column(length=512,nullable=false)
    public String type;

    @Column(length=512)
    @Transient
    public String table;
    public Long refId;

    @JsonIgnore
    @Transient
    public Object instance;

    public XRef () {}
    public XRef (String type, Long id) {
        this.type = type;
        refId = id;
    }

    public XRef (Object instance) {
        Class cls = instance.getClass();
        if (null == cls.getAnnotation(Entity.class))
            throw new IllegalArgumentException
                ("Can't create XRef for non-Entity instance");
        try {
            Method m = cls.getMethod("getId");
            if (m == null)
                throw new IllegalArgumentException
                    ("Entity type does not have getId method!");

            Class c = m.getReturnType();
            if (!Long.class.isAssignableFrom(c))
                throw new IllegalArgumentException
                    ("Entity's getId must return a Long!");
            type = cls.getName();
            refId = (Long)m.invoke(instance);
            if (refId == null)
                throw new IllegalArgumentException
                    ("Can't create XRef for Entity with no Id defined!");

        }
        catch (Exception ex) {
            throw new IllegalArgumentException (ex);
        }

        Table tab = (Table)cls.getAnnotation(Table.class);
        if (tab != null) {
            table = tab.name();
        }
        this.instance = instance;
    }

    public Object deRef () {
        return deRef (false);
    }

    public Object deRef (boolean force) {
        if (instance == null || force) {
            try {
                Model.Finder finder = new Model.Finder
                    (Long.class, Class.forName(type));
                instance = finder.byId(refId);
            }
            catch (Exception ex) {
                Logger.trace("Can't retrieve XRef "+type+":"+refId, ex);
            }
        }
        return instance;
    }

    public String getHref () {
        String resource = Global.getResource(type);
        if (resource != null) {
            return resource+"("+refId+")";
        }
        return null;
    }
}
