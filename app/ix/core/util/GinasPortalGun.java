package ix.core.util;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;


import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ix.core.exporters.OutputFormat;
import ix.core.models.UserProfile;
import ix.core.validator.ValidationResponseBuilder;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import play.data.DynamicForm;
import play.libs.F;
import play.mvc.Result;

/**
 * class that uses Reflection to be able to access ix.ginas classes
 * since we can't import them because this module is built before the ginas module
 * and we can't depend on it.
 */
public class GinasPortalGun {
    private static Class<?> GINAS_APP_CLASS;
    private static Class<?> JSON_SUBSTANCE_FACTORY_CLASS;
    private static Class<?> ValidationResponseBuilderClass, AcceptAllStrategy;
    private static Class<?> SUBSTANCE_FACTORY_CLASS;

    private static Class<?> ADMINISTRATION_CLASS;
    private static Class<?> GINAS_LOAD_CLASS;
    private static Class<?> APP_CLASS;
    static {
        try {
            GINAS_APP_CLASS = Class.forName("ix.ginas.controllers.GinasApp");
            GINAS_LOAD_CLASS = Class.forName("ix.ginas.controllers.GinasLoad");
            JSON_SUBSTANCE_FACTORY_CLASS = Class.forName("ix.ginas.utils.JsonSubstanceFactory");
            ValidationResponseBuilderClass = Class.forName("ix.ginas.utils.validation.ValidationUtils$GinasValidationResponseBuilder");
            SUBSTANCE_FACTORY_CLASS = Class.forName("ix.ginas.controllers.v1.SubstanceFactory");
            ADMINISTRATION_CLASS = Class.forName("ix.ncats.controllers.crud.Administration");
            APP_CLASS = Class.forName("ix.ncats.controllers.App");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static Result downloadFile(String path){
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "downloadFile", MethodType.methodType(Result.class, String.class));
            return (Result) handle.invoke(path);

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static JsonNode getDirListAsJsonNode(){
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "getLogFileListAsJsonNode", MethodType.methodType(JsonNode.class));
            return (JsonNode) handle.invoke();

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static JsonNode getLogListAsJsonNode(){
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "getLogFileListAsJsonNode", MethodType.methodType(JsonNode.class, String.class));
            return (JsonNode) handle.invoke("logs");

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static Result deleteDownload(String downloadId){
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "deleteDownload", MethodType.methodType(Result.class, String.class));
            return (Result) handle.invoke(downloadId);

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static Result getDownloadRecordAsJson(String downloadId) {
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "getDownloadRecordAsJson", MethodType.methodType(Result.class, String.class));
            return (Result) handle.invoke(downloadId);

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static F.Promise<Result> downloadExport(String downloadID) {
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "downloadExport", MethodType.methodType(F.Promise.class, String.class));
            return (F.Promise<Result>) handle.invoke(downloadID);

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static JsonNode getDownloadsAsJson(int rows,int page, String key) {

        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "getDownloadsAsJson", MethodType.methodType(JsonNode.class,
                                                                                                                        int.class, int.class, String.class));
            return (JsonNode) handle.invoke(rows, page, key);

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static void addUser(DynamicForm form){
        //static void addUser(DynamicForm requestData)
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(ADMINISTRATION_CLASS, "addUser", MethodType.methodType(void.class, DynamicForm.class));
             handle.invoke(form);
             return;
        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static void updateUser(UserProfile profileToUpdate, DynamicForm form){
        //static void updateUser(UserProfile profile, DynamicForm requestData)
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(ADMINISTRATION_CLASS, "updateUser", MethodType.methodType(void.class, UserProfile.class, DynamicForm.class));
            handle.invoke(profileToUpdate,form);
            return;
        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static List<UserProfile> getAllUserProfiles(){
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(ADMINISTRATION_CLASS, "principalsList", MethodType.methodType(List.class));
            return (List<UserProfile>) handle.invoke();
        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static Substance createSubstanceFromJson(JsonNode tree){
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(JSON_SUBSTANCE_FACTORY_CLASS, "makeSubstance", MethodType.methodType(Substance.class, JsonNode.class));
            return (Substance) handle.invoke(tree);
        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static <T> ValidationResponseBuilder<T> createValidationResponseBuilderWithAcceptAllStrategy(T newobj){
        MethodType methodType = MethodType.methodType(void.class, Object.class);
       try{
        MethodHandle constructor = MethodHandles.publicLookup().findConstructor(ValidationResponseBuilderClass, methodType);
        return (ValidationResponseBuilder<T>) constructor.invoke(newobj);
       }catch(Throwable e){
           e.printStackTrace();
           throw new IllegalStateException(e);
       }
    }

    /**
     * This is just a delegating method for the SubstanceFactory method which is not visible
     * from inside of this model. This is by no means ideal.
     * @param sr
     * @return
     */
    public static Optional<SubstanceReference> getUpdatedSubstanceReference(SubstanceReference sr){
    	 MethodType methodType = MethodType.methodType(Optional.class, SubstanceReference.class);
    	 try{
             MethodHandle handle = MethodHandles.lookup().findStatic(SUBSTANCE_FACTORY_CLASS, "getUpdatedVersionOfSubstanceReference", methodType);
             return (Optional<SubstanceReference>) handle.invoke(sr);
         }catch(Throwable e){
             e.printStackTrace();
             throw new IllegalStateException(e);
         }
    }

    public static Result monitorProcessApi(String processID) {
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_LOAD_CLASS, "monitorProcessApi", MethodType.methodType(Result.class, String.class));
            return (Result) handle.invoke(processID);

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static Result loadJsonViaAPI() {
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_LOAD_CLASS, "loadJsonViaAPI", MethodType.methodType(Result.class));
            return (Result) handle.invoke();

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static String getBestKeyForCurrentRequest(){
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(APP_CLASS, "getBestKeyForCurrentRequest", MethodType.methodType(String.class));
            return (String) handle.invoke();

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
    public static ObjectNode generateExportMetaDataUrlForApi(String collectionID, String extension, int publicOnly) {
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "generateExportMetaDataUrlForApi", MethodType.methodType(ObjectNode.class, String.class, String.class, int.class));

            return (ObjectNode) handle.invoke(collectionID, extension, publicOnly);

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static F.Promise<Result> export(String collectionID, String extension, int publicOnly, List<Substance> dataToExport) {
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "export", MethodType.methodType(F.Promise.class, String.class, String.class, int.class, Supplier.class));

            return (F.Promise<Result>) handle.invoke(collectionID, extension, publicOnly, new Supplier<Stream<Substance>>() {
                @Override
                public Stream<Substance> get() {
                    return dataToExport.stream();
                }
            });

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
        public static Set<OutputFormat> getAllSubstanceExportFormats() {
        try{
            MethodHandle handle = MethodHandles.lookup().findStatic(GINAS_APP_CLASS, "getAllSubstanceExportFormats", MethodType.methodType(Set.class));
            return (Set<OutputFormat>) handle.invoke();

        }catch(Throwable e){
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}