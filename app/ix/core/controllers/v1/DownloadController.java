package ix.core.controllers.v1;

import be.objectify.deadbolt.java.actions.Dynamic;
import ix.core.controllers.search.SearchFactory;
import ix.core.controllers.search.SearchRequest;
import ix.core.models.ETag;
import ix.core.search.SearchResult;
import ix.core.util.CachedSupplier;
import ix.core.util.GinasPortalGun;
import ix.core.util.RestUrlLink;
import ix.ginas.models.v1.Substance;
import ix.ncats.controllers.security.IxDynamicResourceHandler;
import ix.utils.Util;
import org.apache.http.entity.ContentType;
import play.db.ebean.Model;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DownloadController extends Controller {

    static CachedSupplier<Model.Finder<Long, ETag>> etagDb = Util.finderFor(Long.class, ETag.class);


    /*
    GET         /myDownloads   			          ix.ginas.controllers.GinasApp.downloadsView(rows: Int ?= 16, page: Int ?= 1)
GET         /myDownloads/:downloadID          ix.ginas.controllers.GinasApp.downloadView(downloadID: String)

GET         /downloads   			          ix.ginas.controllers.GinasApp.listDownloads()
GET         /downloads/:downloadID/download   ix.ginas.controllers.GinasApp.downloadExport(downloadID: String)
GET         /downloads/:downloadID            ix.ginas.controllers.GinasApp.getStatusFor(downloadID: String)
GET         /downloads/:downloadID/@cancel    ix.ginas.controllers.GinasApp.cancelExport(downloadID: String)
GET         /downloads/:downloadID/@remove    ix.ginas.controllers.GinasApp.removeExport(downloadID: String)

     */
    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result getDownloadRecordAsJson(String downloadID) {
        return GinasPortalGun.getDownloadRecordAsJson(downloadID);
    }

    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static Result deleteDownload(String downloadID) {
        return GinasPortalGun.deleteDownload(downloadID);
    }


    @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> downloadExport(String downloadID) {
        return GinasPortalGun.downloadExport(downloadID);
    }
        @Dynamic(value = IxDynamicResourceHandler.IS_USER_PRESENT, handler = ix.ncats.controllers.security.IxDeadboltHandler.class)
    public static F.Promise<Result> viewMyDownloads(int rows, int page, String q){

        return F.Promise.promise(() -> {
            return Results.ok(  GinasPortalGun.getDownloadsAsJson(rows, page, q))
                            .as(ContentType.APPLICATION_JSON.toString());
        });
    }


    public static F.Promise<Result> export(String context, String etag, String extension, boolean publicOnly){
        ETag etagObj = etagDb.get().query().where().eq("etag", etag).findUnique();
        if(etagObj ==null){
            return F.Promise.pure(GsrsApiUtil.notFound("no etag with id " + etag));
        }
        int total = etagObj.total;
        String query = etagObj.query;
        if(query!=null && query.trim().isEmpty()){
            query=null;
        }

        SearchResult result=null;
        try {
            result = SearchFactory.search(SearchRequest.Builder.class, Substance.class, query, total, 0, 10);
        } catch (IOException e) {
            GsrsApiUtil.internalServerError(e);
        }
//        System.out.println("result query = \n" + EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().valueToTree(result));
        List fullyPopulatedResult=new ArrayList(total);
        try {
            result.copyTo(fullyPopulatedResult,0, total, true);
        } catch (Exception e) {
            GsrsApiUtil.internalServerError(e);
        }
        //now that we have the whole result get the substances
        return GinasPortalGun.export(etag, extension, publicOnly? 1:0, (List< Substance >) fullyPopulatedResult);
    }
   /* public static Result getExportOptions(String context, String etagId, boolean publicOnly){
        List<OutputFormat> formats= GinasPortalGun.getAllSubstanceExportFormats()
                                    .stream()
                                    .sorted(Comparator.comparing(OutputFormat::getDisplayName))
                                    .collect(Collectors.toList());
        int publicFlag = publicOnly? 1:0;
        List<ExportOption> ret = new ArrayList<>();
        for(OutputFormat format : formats){
            ExportOption option = new ExportOption();
            option.displayname = format.getDisplayName();
            option.extension = format.getExtension();
//            option.link = GinasPortalGun.generateExportMetaDataUrlForApi(collectionId, format.getExtension(),publicFlag);
            option.link = RestUrlLink.from(routes.DownloadController.export(context, etagId, format.getExtension(), publicOnly));
            ret.add(option);
        }
        return Results.ok((JsonNode)EntityFactory.EntityMapper.FULL_ENTITY_MAPPER().valueToTree(ret))
                    .as("application/json");
    }*/

    public static class ExportOption{
        public RestUrlLink link;
        public String extension;
        public String displayname;
    }

}
