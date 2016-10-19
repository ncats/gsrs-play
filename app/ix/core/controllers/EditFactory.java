package ix.core.controllers;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import ix.core.NamedResource;
import ix.core.models.Edit;
import ix.core.util.Java8Util;
import play.Logger;
import play.db.ebean.Model;
import play.mvc.Result;

@NamedResource(name="edits",
type=Edit.class,
description="Edit history of changes to entities")
public class EditFactory extends EntityFactory {
    public static final Model.Finder<UUID, Edit> finder = 
        new Model.Finder(UUID.class, Edit.class);

    public static Result get (String uuid) {
        try {
            UUID id = UUID.fromString(uuid);
            Edit edit = finder.byId(id);
            if (edit != null) {
                ObjectMapper mapper = getEntityMapper ();
                return Java8Util.ok (mapper.valueToTree(edit));
            }

            return notFound ("Bad request: "+request().uri());
        }
        catch (Exception ex) {
            Logger.trace("Can't retrieve edit "+uuid, ex);
        }
        
        return internalServerError
            ("Unable to fullfil request: "+request().uri());
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

	public static Result field(String uuid, String field) {

		UUID id = UUID.fromString(uuid);

		Edit edit = finder.byId(id);
		if (edit != null) {
			return field(edit, field);
		}
		throw new IllegalArgumentException("Bad request: " + request().uri());
	}
    
    public static Result page (int top, int skip, String filter) {
        return page (top, skip, filter, finder);
    }

}
