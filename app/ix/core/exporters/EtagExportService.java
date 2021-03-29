package ix.core.exporters;

import com.fasterxml.jackson.databind.JsonNode;
import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.common.util.DelayedComputationIterables;
import gov.nih.ncats.common.yield.Yield;
import ix.core.controllers.EntityFactory;
import ix.core.models.ETag;
import ix.core.plugins.LoopbackWebRequestPlugin;
import ix.core.search.EntityFetcher;
import ix.core.util.EntityUtils;
import ix.ginas.models.v1.Substance;
import play.Logger;
import play.Play;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * An {@link ExportService} that uses a saved {@link ETag} and pulls out the original query
 * for it to download the ids of the results and then convert that into a {@link ix.core.util.EntityUtils.Key}
 * to fetch the entities.
 * @param <ID>
 * @param <T>
 */
public class EtagExportService<ID, T> implements ExportService<ETag,T>  {

    private static final Pattern removeTopPattern = Pattern.compile("(&top=\\d+)");
    private static final Pattern removeSkipPattern = Pattern.compile("(&skip=\\d+)");
    private static final Pattern removeViewPattern = Pattern.compile("(&view=\\s+)");

    private static CachedSupplier<LoopbackWebRequestPlugin> requestPluginCachedSupplier = CachedSupplier.of(()-> Play.application().plugin(LoopbackWebRequestPlugin.class));


    private final Http.Request request;

    public EtagExportService(Http.Request request){
        this.request = Objects.requireNonNull(request);
    }
    @Override
    public Supplier<Stream<T>> generateExportFrom(String context, ETag etag) {

        return ()-> Yield.<T>create( yieldRecipe-> {
                    String uriToUse = etag.uri;
                    JsonNode responseAsJson;
                    JsonNode array;
                    int tries = 0;


                    do {
                        responseAsJson = makePagedSubstanceRequest(uriToUse, 0, etag.total, context);
//			System.out.println(responseAsJson);
                        JsonNode finished = responseAsJson.get("finished");
                        if (finished != null && !finished.asBoolean()) {
//				System.out.println("not finished yet... waiting");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //don't count this try
                            tries--;
                        }

                        array = responseAsJson.get("content");
                        //text searches and substructure searches have a content
                        //but sequence searches do not... so need to hit their results url
                        //or maybe change what url the etag saves?
                        if (array == null) {
                            JsonNode results = responseAsJson.get("results");
                            if (results != null) {
                                uriToUse = results.asText();
                            }

                        }
                        tries++;
                    } while (array == null && tries < 3);
                    //if we are here, then
                    if (array == null) {
//			System.out.println("could not fetch results!!!");
                        throw new IllegalStateException("could not fetch results");
                    }
//		System.out.println("out of while loop array =\n===============\n===============\n=============" );
//		System.out.println(array);


                    Consumer<JsonNode> arrayConsumer = a -> {

                        for (JsonNode sub : a) {
                            //GSRS-1760 using key view always now

                            /*
                            JSON looks like: {
                            kind:	"ix.ginas.models.v1.ChemicalSubstance"
                            idString:	"e3b22138-b251-48a4-bf67-ada0f317da4a"
                            }
                             */

                            try {
                                EntityUtils.EntityInfo ei = EntityUtils.getEntityInfoFor(sub.get("kind").asText());
                                Object _id = ei.formatIdToNative(sub.get("idString").asText());
                                EntityUtils.Key k = EntityUtils.Key.of(ei, _id);
                                T obj = (T) k.getFetcher().call();
                                if(obj !=null){
                                    yieldRecipe.returning(obj);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    };

                    arrayConsumer.accept(array);

                })
                .stream();
    }

    private JsonNode makePagedSubstanceRequest(String uri, int skip, int top, String context){

        String cleanedUri = removeTopPattern.matcher(uri).replaceAll("");
        cleanedUri = removeSkipPattern.matcher(cleanedUri).replaceAll("");
        cleanedUri = removeViewPattern.matcher(cleanedUri).replaceAll("");
        //GSRS-1760 use Key view for fast fetching to avoid paging and record edits dropping out of pagged results
        if (cleanedUri.indexOf('?') > 0) {
            //has parameters so append
            cleanedUri += "&view=key&top=" + top + "&skip="+skip;
        } else {
            //doesn't have parameters
            cleanedUri += "?view=key&top=" + top + "&skip="+skip;
        }
//		System.out.println("cleaned uri = " + cleanedUri);
//
        WSRequestHolder requestHolder = requestPluginCachedSupplier.get().createNewLoopbackRequestFrom(cleanedUri, request, context);


        WSResponse wsResponse = requestHolder

                .get()
                .get(1, TimeUnit.HOURS);
        if(wsResponse.getStatus()< 300){
            //it worked!

            try {
                return wsResponse.asJson();
            }catch(RuntimeException e){
                Logger.error("error parsing result from url " + cleanedUri);
                Logger.error("could not parse json =\n" + e.getMessage());
                throw e;
            }
        }
        //errored out
        Logger.error("could not fetch page request : status code = "+wsResponse.getStatus() + " " + wsResponse.getBody());
        return EntityFactory.EntityMapper.INTERNAL_ENTITY_MAPPER().createObjectNode();
    }
}
