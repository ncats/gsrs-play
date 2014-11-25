package ix.ncats.controllers.reach;

import java.io.*;
import java.util.*;

import play.*;
import play.data.*;
import play.mvc.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import ix.ncats.models.Project;

public class ProjectForm extends Controller {
    static public Form<Project> form = Form.form(Project.class);

    public static Result index () {
        return ok (ix.ncats.views.html.project.render(form));
    }

    public static Result newProject () {
        Form<Project> filled = form.bindFromRequest();
        if (filled.hasErrors()) {
            return badRequest (ix.ncats.views.html.project.render
                               (filled));
        }
        Project project = filled.get();
        project.save();
        Logger.debug("Project "+project.id+": "+project.title+" created!"); 
        
        return redirect (routes.ProjectForm.index());
    }
}
