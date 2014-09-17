package crosstalk.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.avaje.ebean.*;
import com.avaje.ebean.event.BeanPersistListener;

import crosstalk.core.models.ETag;
import crosstalk.core.models.ETagRef;
import crosstalk.core.models.Edit;
import crosstalk.core.models.Principal;

import crosstalk.utils.Global;

public class SearchFactory extends Controller {
    public static Result search (String q, int top, int skip) {
        Global g = Global.getInstance();
        try {
            List results = g.getTextIndexer().search(q, top, skip);
            ObjectMapper mapper = new ObjectMapper ();

            return ok (mapper.valueToTree(results));
        }
        catch (IOException ex) {
            return badRequest (ex.getMessage());
        }
    }
}
