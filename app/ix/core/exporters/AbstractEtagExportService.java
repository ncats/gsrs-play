package ix.core.exporters;

import com.fasterxml.jackson.databind.JsonNode;
import gov.nih.ncats.common.util.CachedSupplier;
import gov.nih.ncats.common.util.DelayedComputationIterables;
import gov.nih.ncats.common.yield.Yield;
import ix.core.controllers.EntityFactory;
import ix.core.models.ETag;
import ix.core.plugins.LoopbackWebRequestPlugin;
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

public abstract class AbstractEtagExportService<ID, T> implements ExportService<ETag,T>  {

    private static final Pattern removeTopPattern = Pattern.compile("(&top=\\d+)");
    private static final Pattern removeSkipPattern = Pattern.compile("(&skip=\\d+)");

    private static CachedSupplier<LoopbackWebRequestPlugin> requestPluginCachedSupplier = CachedSupplier.of(()-> Play.application().plugin(LoopbackWebRequestPlugin.class));

    private Function<JsonNode, ID> idExtractor;
    private Function<ID, T> fetcherById;
    protected int pageSize;
    private final Http.Request request;

    public AbstractEtagExportService(Http.Request request, int fetchPageSize, Function<JsonNode, ID> idExtractor, Function<ID, T> fetcherById){
        if(fetchPageSize < 1){
            throw new IllegalArgumentException("fetch page size must be >=1");
        }
        this.pageSize = fetchPageSize;
        this.idExtractor = Objects.requireNonNull(idExtractor);
        this.fetcherById = Objects.requireNonNull(fetcherById);
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
                        responseAsJson = makePagedSubstanceRequest(uriToUse, 0, pageSize, context);
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
                            ID id = idExtractor.apply(sub);
                            if(id !=null){
                                T obj = fetcherById.apply(id);
                                if(obj !=null){
                                    yieldRecipe.returning(obj);
                                }
                            }
                        }
                    };

                    arrayConsumer.accept(array);
                    for (int i = pageSize; i < etag.total; i += pageSize) {
                        responseAsJson = makePagedSubstanceRequest(uriToUse, i, pageSize, context);

                        array = responseAsJson.get("content");
                        arrayConsumer.accept(array);
                    }

                })
                .stream();
    }

    private JsonNode makePagedSubstanceRequest(String uri, int skip, int top, String context){

        String cleanedUri = removeTopPattern.matcher(uri).replaceAll("");
        cleanedUri = removeSkipPattern.matcher(cleanedUri).replaceAll("");
        if (cleanedUri.indexOf('?') > 0) {
            //has parameters so append
            cleanedUri += "&top=" + top + "&skip="+skip;
        } else {
            //doesn't have parameters
            cleanedUri += "?top=" + top + "&skip="+skip;
        }
//		System.out.println("cleaned uri = " + cleanedUri);
//
        WSRequestHolder requestHolder = requestPluginCachedSupplier.get().createNewLoopbackRequestFrom(cleanedUri, request, context);
//		for(Map.Entry<String, String[]> entry : requestHeaders.entrySet()){
//			String key = entry.getKey();
//			for(String value : entry.getValue()){
//				System.out.println("header "+ key +" = \""+value+"\"");
////				requestHolder.setHeader(key, value);
//			}
//		}
//        String[] token = requestHeaders.get("auth-token");
//        if(token !=null && token.length > 0){
//            requestHolder.setHeader("auth-token", token[0]);
//        }

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
