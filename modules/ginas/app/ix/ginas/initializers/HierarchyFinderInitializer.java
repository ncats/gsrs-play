package ix.ginas.initializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ix.core.initializers.Initializer;
import ix.core.util.IOUtil;
import ix.ginas.controllers.v1.SubstanceHierarchyFinder;
import ix.ginas.models.v1.Substance;
import pl.joegreen.lambdaFromString.ClassPathExtractor;
import pl.joegreen.lambdaFromString.LambdaFactory;
import pl.joegreen.lambdaFromString.LambdaFactoryConfiguration;
import pl.joegreen.lambdaFromString.TypeReference;
import play.Application;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class HierarchyFinderInitializer implements Initializer {

    private static HierarchyFinderInitializer instance;

    private  List<SubstanceHierarchyFinder.HierarchyFinder> finders;

    public static HierarchyFinderInitializer getInstance() {
        return instance;
    }

    public List<SubstanceHierarchyFinder.HierarchyFinder> getFinders() {
        return finders;
    }

    @Override
    public void onStart(Application app) {
        HierarchyFinderInitializer.instance = this;

        List<?> list = app.configuration().getList("substance.hierarchyFinders");
        if(list == null){
            throw new IllegalStateException("substance hierarchy must be specified in the config");
        }
        ObjectMapper mapper = new ObjectMapper();
        List<SubstanceHierarchyFinder.HierarchyFinder> l = list.stream()
                                    .map(m-> mapper.convertValue(m, HierarchyFinderRecipe.class))
                                    .map(r-> {
                                        try{
                                            return r.makeFinder();
                                        }catch(Exception e){
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.toList());

        l.add(new SubstanceHierarchyFinder.G1SSConstituentFinder().renameChildType("IS G1SS CONSTITUENT OF"));
        l.add(new SubstanceHierarchyFinder.StructurallyDiverseParentFinder());

        this.finders = Collections.unmodifiableList(l);
    }



    private static BiFunction<Substance,Substance,String> toLambda(String lambdaString) throws Exception{
        LambdaFactoryConfiguration config = LambdaFactoryConfiguration.get()
                .withParentClassLoader(IOUtil.getGinasClassLoader())
                .withCompilationClassPath(ClassPathExtractor.getCurrentContextClassLoaderClassPath())
                .withImports(Substance.class)
                ;

        return LambdaFactory.get(config)
                .createLambda(lambdaString, new TypeReference<BiFunction<Substance,Substance,String>>() {
                });
    }

    public static class HierarchyFinderRecipe{
        private String renameChildTo;
        private String renameChildLambda;
        private String relationship;
        private Boolean invertible;

        public String getRenameChildTo() {
            return renameChildTo;
        }

        public void setRenameChildTo(String renameChildTo) {
            this.renameChildTo = renameChildTo;
        }

        public String getRenameChildLambda() {
            return renameChildLambda;
        }

        public void setRenameChildLambda(String renameChildLambda) {
            this.renameChildLambda = renameChildLambda;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }

        public Boolean getInvertible() {
            return invertible;
        }

        public void setInvertible(Boolean invertible) {
            this.invertible = invertible;
        }

        public SubstanceHierarchyFinder.HierarchyFinder makeFinder() throws Exception{
            SubstanceHierarchyFinder.HierarchyFinder finder =null;
            if(Boolean.TRUE.equals(invertible)){
                finder = new SubstanceHierarchyFinder.InvertibleRelationshipHierarchyFinder(relationship);
            }else{ // if not set then not invertible ?
                finder = new SubstanceHierarchyFinder.NonInvertibleRelationshipHierarchyFinder(relationship);
            }
            if(renameChildTo !=null){
                finder = finder.renameChildType(renameChildTo);
            }else if(renameChildLambda !=null){
                finder = finder.renameChildType(toLambda(renameChildLambda));
            }
            return finder;
        }
    }
}
