package ix.core.controllers;

import java.io.*;
import java.security.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import play.*;
import play.db.ebean.*;
import play.data.*;
import play.mvc.*;

import ix.core.models.Payload;
import ix.core.NamedResource;
import ix.core.plugins.PayloadPlugin;

@NamedResource(name="payload",
               type=Payload.class,
               description="Resource for handling payload")
public class PayloadFactory extends EntityFactory {
    public static final Model.Finder<UUID, Payload> finder = 
        new Model.Finder(UUID.class, Payload.class);
    static PayloadPlugin payloadPlugin =
        Play.application().plugin(PayloadPlugin.class);

    public static Payload getPayload (UUID id) {
        return getEntity (id, finder);
    }
    
    public static Result count () { return count (finder); }
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

    public static Result get (UUID id, String select) {
        return get (id, select, finder);
    }

    public static Result field (UUID id, String path) {
        return field (id, path, finder);
    }

    public static Result create () {
        throw new UnsupportedOperationException
            ("create operation not supported!");
    }

    public static Result delete (UUID id) {
        throw new UnsupportedOperationException
            ("delete operation not supported!");
    }

    public static File getFile (UUID id) {
        Payload payload = getPayload (id);
        if (payload != null) {
            return payloadPlugin.getPayload(payload);
        }
        return null;
    }

    public static File getFile (Payload payload) {
        return payloadPlugin.getPayload(payload);
    }

    public static InputStream getStream (UUID id) {
        Payload payload = getPayload (id);
        if (payload != null)
            return payloadPlugin.getPayloadAsStream(payload);
        return null;
    }
    
    public static InputStream getStream (Payload payload) {
        if (payload.id != null)
            return getStream (payload.id);
        throw new IllegalArgumentException ("Invalid payload with no id!");
    }   
}
