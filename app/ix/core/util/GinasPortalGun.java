package ix.core.util;

import com.fasterxml.jackson.databind.JsonNode;
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

    static {
        try {
            GINAS_APP_CLASS = Class.forName("ix.ginas.controllers.GinasApp");
            JSON_SUBSTANCE_FACTORY_CLASS = Class.forName("ix.ginas.utils.JsonSubstanceFactory");
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
}