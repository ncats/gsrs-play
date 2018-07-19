package ix.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import ix.core.validator.ValidationResponseBuilder;
import ix.ginas.models.v1.Substance;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * class that uses Reflection to be able to access ix.ginas classes
 * since we can't import them because this module is built before the ginas module
 * and we can't depend on it.
 */
public class GinasPortalGun {
    private static Class<?> GINAS_APP_CLASS;
    private static Class<?> JSON_SUBSTANCE_FACTORY_CLASS;
    private static Class<?> ValidationResponseBuilderClass, AcceptAllStrategy;
    static {
        try {
            GINAS_APP_CLASS = Class.forName("ix.ginas.controllers.GinasApp");
            JSON_SUBSTANCE_FACTORY_CLASS = Class.forName("ix.ginas.utils.JsonSubstanceFactory");
            ValidationResponseBuilderClass = Class.forName("ix.ginas.utils.validation.ValidationUtils$GinasValidationResponseBuilder");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
}