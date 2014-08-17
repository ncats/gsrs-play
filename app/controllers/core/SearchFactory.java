package controllers.core;

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

import models.core.ETag;
import models.core.ETagId;
import models.core.Edit;
import models.core.Principal;

import utils.Global;

public class SearchFactory extends Controller {
    public static Result search (int top, int skip, String q) {
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
