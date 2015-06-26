package ix.ntd.controllers;

import ix.core.controllers.search.SearchFactory;
import ix.core.models.Keyword;
import ix.core.models.Structure;
import ix.core.models.Value;
import ix.core.plugins.StructureIndexerPlugin;
import ix.core.plugins.TextIndexerPlugin;
import ix.core.search.TextIndexer;
import ix.core.search.TextIndexer.Facet;
import ix.ginas.models.v1.*;
import ix.ncats.controllers.App;
import ix.utils.Util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.util.StringUtils;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.ebean.Model;
import play.mvc.Result;


import com.fasterxml.jackson.databind.ObjectMapper;

public class NTDApp extends App {
    static final TextIndexer TEXT_INDEXER = 
        Play.application().plugin(TextIndexerPlugin.class).getIndexer();


    // substance finder
    static final Model.Finder<UUID, Substance> SUBFINDER =
        new Model.Finder(UUID.class, Substance.class);
    
    // relationship finder
    static final Model.Finder<UUID, Relationship> RELFINDER =
        new Model.Finder(UUID.class, Relationship.class);

    
    public static final String[] CHEMICAL_FACETS = {
        "Status",
        "Substance Class",
        "SubstanceStereoChemistry",
        "Molecular Weight",
        "GInAS Tag"
    };
    
    public static final String[] PROTEIN_FACETS = {
        "Sequence Type",
        "Substance Class",
        "Status"
    };

    public static final String[] ALL_FACETS = {
            "Status",
            "Substance Class",
            "SubstanceStereoChemistry",
            "Molecular Weight",
            "GInAS Tag",
            "Sequence Type",
            "Material Class",
            "Material State",
            "Material Type",
            "Family",
            "Genus",
            "Species"
    };

    static <T> List<T> filter (Class<T> cls, List values, int max) {
        List<T> fv = new ArrayList<T>();
        for (Object v : values) {
            if (cls.isAssignableFrom(v.getClass())) {
                fv.add((T)v);
                if (fv.size() >= max)
                    break;
            }
        }
        return fv;
    }

    /**
     * return a field named type to get around scala's template reserved
     * keyword
     */
    public static String getType (Object obj) {
        Class cls = obj.getClass();
        String type = null;
        try {
            Field f = cls.getField("type");
            if (f != null)
                return (String)f.get(obj);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return type;
    }

    public static Result error (int code, String mesg) {
        return ok (ix.ntd.views.html.error.render(code, mesg));
    }

    public static Result _notFound (String mesg) {
        return notFound (ix.ntd.views.html.error.render(404, mesg));
    }

    public static Result _badRequest (String mesg) {
        return badRequest (ix.ntd.views.html.error.render(400, mesg));
    }

    public static Result _internalServerError (Throwable t) {
        t.printStackTrace();
        return internalServerError
            (ix.ntd.views.html.error.render
             (500, "Internal server error: "+t.getMessage()));
    }

    public static Result authenticate () {
        return ok ("You're authenticated!");
    }








    @SuppressWarnings("rawtypes")
    public static int getCount (Object obj){
        int count=0;
        try {
            for(Field l: obj.getClass().getFields()){
//                                              Logger.info(l.getName().toString());
                Class type = l.getType();
                if(type.isArray()){
                    count += Array.getLength(l.get(obj));
                }
                else if (Collection.class.isAssignableFrom(type)) {
                    count += ((Collection)l.get(obj)).size();
                                                          Logger.info("collection"+ count);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Logger.info("final count = " + count);
        return count;

    }
    public static Map<String,Map<String,String>> cvlist = new HashMap<String,Map<String,String>>();
    static {
        Map<String, String> strains = new HashMap<String,String>();
        strains.put("strain_1", "strain 1");
        strains.put("strain_2", "strain 2");
        strains.put("strain_3", "strain 3");
        strains.put("strain_4", "strain 4");
        cvlist.put("strains", strains);

    }

    //Return the CV (value + display text) for given element
    public static Map<String,String> getCVList(String domain){
        return cvlist.get(domain);


    }

}

