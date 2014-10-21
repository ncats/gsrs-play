package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.Figure;
import com.fasterxml.jackson.databind.JsonNode;

public class FigureFactory extends EntityFactory {
    public static final Model.Finder<Long, Figure> finder = 
        new Model.Finder(Long.class, Figure.class);

    public static List<Figure> all () { return all (finder); }
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String expand,
                               String filter) {
        return page (top, skip, expand, filter, finder);
    }

    public static List<Figure> filter (JsonNode json, int top, int skip) {
        return filter (json, top, skip, finder);
    }

    public static Result get (Long id, String select) {
        String format = request().getQueryString("format");
        if (format != null && format.equalsIgnoreCase("image")) {
            Figure fig = finder.byId(id);
            if (fig != null) {
                response().setContentType(fig.mimeType);
                return ok (fig.data);
            }
        }
        return get (id, select, finder);
    }

    public static Result field (Long id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        return create (Figure.class, finder);
    }

    public static Result delete (Long id) {
        return delete (id, finder);
    }

    public static Result update (Long id, String field) {
        return update (id, field, Figure.class, finder);
    }
}
