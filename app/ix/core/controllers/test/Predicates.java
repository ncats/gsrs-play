package ix.core.controllers.test;

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

import ix.core.models.*;

public class Predicates extends Controller {
    static String[] predicates = new String[]{
        "ParentOf",
        "ChildOf",
        "MemberOf",
        "IsA",
        "RelatedTo",
        "NeighborOf",
        "InstanceOf",
        "InhibitorOf",
        "MetaboliteOf"
    };
    static  Random rand = new Random ();

    static Model.Finder<Long, XRef> xrefDb = 
        new Model.Finder(Long.class, XRef.class);
    public static Result xrefs (int top, int skip, String filter) {
        List<XRef> xrefs = xrefDb.where(filter)
            .orderBy("id asc")
            .setFirstRow(skip)
            .setMaxRows(top)
            .findList();
        ObjectMapper mapper = new ObjectMapper ();
        return ok (mapper.valueToTree(xrefs));
    }

    public static Result index (int size) {
        List<Value> values = new ArrayList<Value>();
        randomProperties (size/2, values);
        for (Value v : values) {
            v.save();
            //Logger.debug("..."+v.id+" "+v.label);
        }
        Logger.debug("Generating random properties..."+values.size());

        int np = rand.nextInt(size);
        for (int i = 0; i < np; ++i) {
            Predicate p = new Predicate 
                (predicates[rand.nextInt(predicates.length)]);
            int k = rand.nextInt(values.size());
            Value v = values.get(k);
            p.subject = new XRef (v);

            BitSet bs = new BitSet (values.size());
            bs.set(k);
            int op = rand.nextInt(10);
            for (int j = 0; j < op; ++j) {
                int m = rand.nextInt(values.size());
                if (!bs.get(m)) {
                    v = values.get(m);
                    XRef x = new XRef (v);
                    p.objects.add(x);
                    bs.set(m);
                }
            }

            if (!p.objects.isEmpty()) {
                p.subject.save();
                for (XRef x : p.objects)
                    x.save();
                p.save();

                try {
                    ObjectWriter writer = new ObjectMapper().writer
                        (new DefaultPrettyPrinter ());
                    Logger.debug(writer.writeValueAsString(p));
                }
                catch (Exception ex) {
                    Logger.trace("Can't generate json", ex);
                }
                Logger.debug("Predicate "+p.id+" "+p.predicate
                             +" subject={"+p.subject.id+","+p.subject.refid
                             +","+p.subject.kind+","+p.subject._instance+"}"
                             +" objects="
                             +p.objects.size());
            }
        }
        return redirect (ix.core.controllers.routes
                         .RouteFactory.page("predicates", 10, 0, null, null));
    }

    static char[] alpha = { 
        'a','b','c','d','e','f','g','h','i','j','k','l','m',
        'n','p','q','r','s','t','u','v','w','x','y','z'
    };
    static String randStr () {
        int len = rand.nextInt(20);
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < len; ++i) {
            sb.append(alpha[rand.nextInt(alpha.length)]);
        }
        return sb.toString();
    }

    static XRef createXRef (Object obj) {
        XRef ref = new XRef (obj);
        randomProperties (ref.properties);
        return ref;
    }

    static void randomProperties (List<Value> props) {
        randomProperties (1, props);
    }

    static void randomProperties (int iters, List<Value> props) {
        for (int i = 0; i < iters; ++i) {
            if (rand.nextDouble() < .5)
                props.add(new VInt ("VInt", (long)rand.nextInt()));
            if (rand.nextInt(2) == 0)
                props.add(new VStr ("VStr", randStr ()));
            if (rand.nextInt(2) == 1)
                props.add(new VNum ("VNum", rand.nextDouble()));
            if (rand.nextInt(2) == 0) {
                int lval = rand.nextInt(1000);
                int rval = lval + rand.nextInt(Math.max(1, 1000-lval));
                VRange range = new VRange ("VRange", 
                                           (double)lval, (double)rval);
                range.average = (double)(lval+rand.nextInt
                                         (Math.max(1, rval-lval)));
                props.add(range);
            }
            if (rand.nextInt(2) == 1) {
                byte[] b = new byte[rand.nextInt(1024)];
                rand.nextBytes(b);
                VBin bin = new VBin ("VBin", b);
                props.add(bin);
            }
            if (rand.nextInt(2) == 1) {
                int[] ia = new int[rand.nextInt(512)];
                for (int j = 0; j < ia.length; ++j)
                    ia[j] = rand.nextInt();
                //int[] ia = new int[]{1,2,3,4,5,6,7,9,9,8,7,6,5,4,3,2,1,0};
                props.add(new VIntArray ("VIntArray", ia));
            }
        }
    }
}
