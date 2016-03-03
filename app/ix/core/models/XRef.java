package ix.core.models;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import javax.persistence.*;

import play.Logger;
import play.db.ebean.Model;

import ix.core.controllers.EntityFactory;
import ix.utils.Global;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name="ix_core_xref")
public class XRef extends IxModel {
    /**
     * not id of the XRef instance but id of the instance for which this
     * XRef is pointing to
     */
    @Column(nullable=false,length=40)
    public String refid; 
    @Column(length=255,nullable=false)
    public String kind;
    public boolean deprecated;
    
    @JsonIgnore
    @Transient
    @Indexable(indexed=false)
    public Object _instance; // instance of the object 

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="ix_core_xref_property")
    public List<Value> properties = new ArrayList<Value>();

    public XRef () {
    }

    public XRef (String kind, Long id) {
        this (kind, id.toString());
    }
    
    public XRef (String kind, UUID id) {
        this (kind, id.toString());
    }
    
    public XRef (String kind, String id) {
        if (id == null)
            throw new IllegalArgumentException
                ("Can't create XRef with no id");
        this.kind = kind;
        this.refid = id;
    }

    public XRef (Object instance) {
        Class cls = instance.getClass();
        if (null == cls.getAnnotation(Entity.class))
            throw new IllegalArgumentException
                ("Can't create XRef for non-Entity instance");
        try {
        	Object id = EntityFactory.getId(instance);
            if (id != null) {
                    this.refid = id.toString();
            } else {
                    throw new IllegalArgumentException
                       (cls.getName()+": Can't create XRef with null id!");
            }
            kind = cls.getName();
        }
        catch (Exception ex) {
            throw new IllegalArgumentException (ex);
        }

        this._instance = instance;
    }

    public Object deRef () {
        return deRef (false);
    }

    public Object deRef (boolean force) {
        if (_instance == null || force) {
            try {
                Class cls = Class.forName(kind);
                Field fid = EntityFactory.getIdFieldForClass(cls);
                if (fid != null) {
                    Class type = fid.getType();
                    Model.Finder finder = new Model.Finder(type, cls);
                    if (Long.class.isAssignableFrom(type))
                        _instance = finder.byId(Long.parseLong(refid));
                    else if (UUID.class.isAssignableFrom(type))
                        _instance = finder.byId(UUID.fromString(refid));
                    else
                        _instance = finder.byId(refid);
                }
                else {
                    throw new RuntimeException
                        ("Class "+kind+" doesn't have any fields "
                         +"annotated with @Id!");
                }
            }
            catch (Exception ex) {
                Logger.trace("Can't retrieve XRef "+kind+":"+refid, ex);
            }
        }
        return _instance;
    }

    public Value addIfAbsent (Value value) {
        if (value != null) {
            if (value.id != null) {
                for (Value p : properties) {
                    if (value.id.equals(p.id))
                        return p;
                }
            }
            properties.add(value);
        }
        
        return value;
    }
    
    public String getHRef () {
        return Global.getRef(kind, refid);
    }

    public boolean referenceOf (Object instance) {
        try {
            Class cls = Class.forName(kind);
            Class type = instance.getClass();
            if (cls.isAssignableFrom(type) || type.isAssignableFrom(cls)) {
                Object id=EntityFactory.getId(instance);
            	if (id != null) {
                    return refid.equals(id.toString());
                }
                else {
                    Logger.error
                        ("Class "+type.getName()+" has no @Id annotation, or no Id found!");
                }
            }
        }
        catch (Exception ex) {
            Logger.trace("Can't retrieve class "+kind, ex);
        }
        return false;
    }
}
