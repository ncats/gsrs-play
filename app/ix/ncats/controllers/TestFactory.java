package ix.ncats.controllers;

import java.util.*;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.ncats.models.*;
import ix.core.models.*;
import ix.core.controllers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestFactory extends Controller {
    public static Result publication () {
        Publication pub = new Publication ("Publication 1");
        pub.authors.add(new PubAuthor (0, new Author ("foo", "bar")));
        ix.ncats.models.NIHAuthor a = 
            new ix.ncats.models.NIHAuthor ("foo2", "bar2");
        a.title = "master of the universe";
        pub.authors.add(new PubAuthor (1, a));
        ix.ncats.models.Employee e = 
            new ix.ncats.models.Employee ("foo3", "bar3");
        e.role = ix.ncats.models.Employee.Role.Biology;
        pub.authors.add(new PubAuthor (2, e));
        pub.save();

        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(pub));
    }
}
