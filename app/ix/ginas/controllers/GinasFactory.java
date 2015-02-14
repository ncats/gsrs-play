package ix.ginas.controllers;

import java.io.*;
import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import ix.ginas.models.*;

public class GinasFactory extends Controller {
    public static Result index () {
        return ok ("Welcome to GInAS!");
    }
}
