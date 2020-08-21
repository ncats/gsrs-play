package ix.core.util;

import ix.core.util.ConfigHelper;
import ix.utils.Util;
import org.reflections.Reflections;
import play.api.Play;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 *An automatic concrete type resolver for Jackson that will be able to find
 * the correct concrete type for a JSON object that was serialized as
 * an abstract class or interface.
 *
 * The abstract class or interface should be annotated with
 * Jackson's JsonTypeInfo and JsonTypeIdResolver annotations
 * pointing to this class (see Usage below).  This class will then be used
 * to serialize a discriminator key and map the key to the concrete classes it finds
 * on the classpath that they refer to.
 *
 * This code is a modified version of a <a href= "https://gist.github.com/root-talis/36355f227ff5bb7a057ff7ad842d37a3">github "gist" posted by user
 * root_talis</a> as a comment to an answer of a
 * <a href ="https://stackoverflow.com/questions/30362446/deserialize-json-with-jackson-into-polymorphic-types-a-complete-example-is-giv/30386694#30386694">
 *     question on StackOverflow</a>
 * The changes made include adding a default instance annotation to preserve backward compatability,
 * adding the packages to search through for implementations configurable through the Play
 * conf file, adding support for anonymous classes as concrete implementations,
 * and general code clean up/ performance improvements.
 *
 *
 * @author root_talis
 * @author katzelda
 *
 * Automatic inheritance type resolver for Jackson.
 * @author root_talis<https://github.com/root-talis>
 *
 *
 *
 * Usage:
 *<p/>
 * First annotate the abstract class or interface with the Jackson JsonTypeInfo and IdResolver annotations
 * to tell it to use this class as the custom deserializer:
 * <p/>
 * <pre>
 * @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "discriminator")
 * @JsonTypeIdResolver(InheritanceTypeIdResolver.class)
 * abstract class Animal {  // can be abstract or interface
 *    // ...
 * }
 * </pre>
 *<p/>
 * The {@code property} value is a the new attribute added to the JSON that will be used as the discriminator
 * to find the implementation.  This value can be any string and can be changed for each abstract class
 * in the project.
 * <p/>
 * Other classes are left as is:
 * <pre>
 * // no additional annotations needed to parse json into this class
 * class Cat extends Animal {
 *    // ...
 * }
 *
 * // no additional annotations needed to parse json into this class
 * class Lion extends Cat {
 *    // ...
 * }
 *
 * // no additional annotations needed to parse json into this class
 * class Dog extends Animal {
 *    // ...
 * }
 *</pre>
 *
 *
 */

public class InheritanceTypeIdResolver implements TypeIdResolver {

    /**
     * Annotate the concrete class with this annotation
     * to be marked as the 'default' implementation to use
     * if a JSON object
     * is missing the discriminator property.
     * This is mostly used for backwards compatibility
     * where old JSON that was generated by the application
     * before the introduction to the type resolver (mostly used for tests).
     * Only 1 implementation should be annotated with this
     * annotation, if more than one are annotated
     * then it is not deterministic which class
     * is chosen as the default.
     */

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface DefaultInstance{

    }
    private JavaType baseType;
    private Map<String, JavaType> typeMap = new HashMap<>();

    private JavaType defaultType;
    @Override
    public void init(JavaType javaType) {
        baseType = javaType;

        Class<?> clazz = baseType.getRawClass();

        List<String> packages = ConfigHelper.getStringList("ix.json.typeIdResolvers", Collections.emptyList());
        System.out.println("HERE IN TYPE ID RESOLVER!!! packages = " + packages);
        Util.printExecutionStackTrace();
        Reflections reflections = new Reflections(packages.toArray(new Object[packages.size()])); // root package to scan for subclasses
        Set<Class<?>> subtypes = (Set<Class<?>>)reflections.getSubTypesOf(clazz);

        int classModifiers = clazz.getModifiers();

        if (!Modifier.isAbstract(classModifiers) && !Modifier.isInterface(classModifiers)) {
            subtypes.add(clazz);
        }


        subtypes.forEach(type -> {
            System.out.println("found subtype " + type);
            String key = computeNameFor(type);

            JavaType value = TypeFactory.defaultInstance().constructSpecializedType(baseType, type);
            if (typeMap.put(key, value) !=null) {
                throw new IllegalStateException("Type name \"" + key + "\" already exists.");
            }

            if(type.getAnnotation(DefaultInstance.class) !=null){
                defaultType = value;
            }

        });
    }

    @Override
    public String idFromValue(Object o) {
        return idFromValueAndType(o, o.getClass());
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    //katzelda 2018-11-10
    //note this implementation has several methods
    //that are not annotated with @Override but actually
    //override interface methods
    //there are also some methods also not annotated that don't implement interface methods.
    //This is because the TypeIdResolver made API incompatiable changes adding and removing
    //these methods.  There appears to be a classpath issue where some of our dependencies
    //now require a newer version of the Jackson library but sometimes we load the jar
    //from a different dependency.  This way no matter which version of the interface we load
    //it will compile. We implement both versions before and after the interface change
    //but remove the override annotation to avoid compiler issues.

    public String getDescForKnownTypeIds() {
        return "inheritance type for base " + baseType;
    }

    private String computeNameFor(Class<?> cls){
        if(cls.isAnonymousClass()){
            int offset=0;

            for(Class<?> c : cls.getEnclosingClass().getClasses()){
                offset++;
                if( c == cls){
                    break;
                }
            }
            return cls.getEnclosingClass().getSimpleName() + "$" + offset ;
        }
        return cls.getSimpleName();
    }
    @Override
    public String idFromValueAndType(Object o, Class<?> aClass) {
        String name = computeNameFor(aClass);
        JavaType javaType = typeMap.get(name);
        if(javaType ==null){
            if(defaultType ==null) {
                return null;
            }
            javaType = defaultType;
        }
        return computeNameFor(javaType.getRawClass());

    }
    public JavaType typeFromId(String s) {
        return typeFromId(null, s);
    }
    public JavaType typeFromId(DatabindContext databindContext, String s) {
        JavaType type = typeMap.get(s);

        if(type ==null) {

            throw new IllegalStateException("Cannot find class for type id \"" + s + "\" type map = " + typeMap.keySet());
        }
        return type;
    }



    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

}
