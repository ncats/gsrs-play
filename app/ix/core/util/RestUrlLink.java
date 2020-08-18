package ix.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import ix.utils.Global;
import play.api.mvc.Call;

/**
 * Poorman's JSON friendly link to RESTful services.
 * @author katzelda
 */
public class RestUrlLink {
    public String url;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String type;

    public static RestUrlLink from(Call playCall){
        RestUrlLink link = new RestUrlLink();
        link.url = Global.getHost() + playCall.url();
        link.type = playCall.method();
        return link;
    }
}
