package ix.qhts.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Transactional;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.controllers.EntityFactory;
import ix.core.controllers.StructureFactory;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.NamedResource;
import ix.qhts.models.Sample;

@NamedResource(name="samples", type=Sample.class,
     description="This resource is for handling of (qHTS) assay samples.")
public class SampleFactory extends EntityFactory {
    public static final Model.Finder<Long, Sample> finder = 
        new Model.Finder(Long.class, Sample.class);

    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }
    
    public static Result get (Long id, String select) {
        return get (id, select, finder);
    }

    public static List<Sample> filter (int top, int skip) {
        return filter (top, skip, null);
    }

    public static List<Sample> filter (int top, int skip, String filter) {
        return filter (new FetchOptions (top, skip, filter), finder);
    }

    public static List<Sample> filter (FetchOptions options) {
        return filter (options, finder);
    }

    public static List<Sample> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
    }

    public static Integer getCount () {
        try {
            return getCount (finder);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Sample.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Sample.class, finder);
    }
    
    public static Sample getEntity (long id) {
        return getEntity (id, finder);
    }

    public static Sample resolve (Collection<Keyword> keywords) {
        return resolve (keywords.toArray(new Keyword[0]));
    }
    
    public static Sample resolve (Keyword... keywords) {
        for (Keyword kw : keywords) {
            if (Structure.H_LyChI_L4.equals(kw.label)
                || Structure.H_InChI_Key.equals(kw.label)
                || Sample.S_UNII.equals(kw.label)
                || Sample.S_SID.equals(kw.label)
                || Sample.S_CID.equals(kw.label)
                || Sample.S_DSSTOX.equals(kw.label)
                || Sample.S_CASRN.equals(kw.label)
                || Sample.S_NCGC.equals(kw.label)
                || Sample.S_TOX21.equals(kw.label)) {
                List<Sample> samples =
                    finder.where(Expr.and(Expr.eq("synonyms.label", kw.label),
                                          Expr.eq("synonyms.term", kw.term)))
                    .findList();
                if (!samples.isEmpty()) {
                    if (samples.size() > 1) {
                        Logger.warn("SampleFactory.resolve: label="+kw.label
                                    +" term="+kw.term+" resolves to "
                                    +samples.size()+" samples!");
                    }
                    return samples.iterator().next();
                }
            }
        }
        return null;
    }

    public static Sample registerIfAbsent (Sample sample) {
        Sample s = resolve (sample.synonyms);
        if (s == null) {
            sample.save();
            s = sample;
        }
        return s;
    }
}
