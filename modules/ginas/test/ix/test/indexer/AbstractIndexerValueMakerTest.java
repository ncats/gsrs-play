package ix.test.indexer;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ix.core.search.text.IndexValueMaker;
import ix.core.search.text.IndexValueMakerFactory;
import ix.core.search.text.IndexableValue;
import ix.core.search.text.ReflectingIndexValueMaker;
import ix.test.AbstractGinasTest;
import ix.test.server.GinasTestServer;
import play.Configuration;


/**
 * Helper abstract class for testing IndexValueMakers.
 * 
 * 
 * 
 * @author peryeata
 *
 * @param <T>
 * @param <U>
 */
@Ignore
public abstract class AbstractIndexerValueMakerTest<T,U extends IndexValueMaker<T>> extends AbstractGinasTest{

	@Rule
	public GinasTestServer ts = new GinasTestServer(()->{
		
		String addconf="include \"ginas.conf\"\n" + 
				"\n" + 
				"ix.core.indexValueMakers +={\n" + 
				"		\"class\":\"" + getEntityClass().getName() + "\",\n" + 
				"		\"indexer\":\"" +getIndexMakerClass().getName() + "\"\n" + 
				"	}";
		Config additionalConfig = ConfigFactory.parseString(addconf)
				.resolve()
				.withOnlyPath("ix.core.indexValueMakers");
		return new Configuration(additionalConfig).asMap();
	});
	
	


	public abstract Class<? extends T> getEntityClass();
	public abstract Class<? extends U> getIndexMakerClass();


	@Test
	public void testIndexMakerIsDifferentFromDefaultForEntity(){
		IndexValueMaker<? extends T> ivm =IndexValueMakerFactory.forClass(getEntityClass());
		assertNotEquals(ReflectingIndexValueMaker.class,ivm.getClass());
	}
	
	@Test
	public void testIndexMakerIsRegisteredForEntity(){
		List<Class<?>> ivm =IndexValueMakerFactory.getIndexValueMakersForClass(getEntityClass())
							.stream()
							.map(iv->iv.getClass())
							.collect(Collectors.toList());
		assertTrue("Could not find: " +getEntityClass().toString() + " in:" + ivm.toString(), ivm.contains(getIndexMakerClass()));
		assertTrue(IndexValueMakerFactory.isRegisteredFor(getEntityClass(), getIndexMakerClass()));
	}


	public void testIndexableValuesMatchCriteria(T t, Predicate<Stream<IndexableValue>> matches){
		IndexValueMaker<T> ivm =(IndexValueMaker<T>) IndexValueMakerFactory.forClass(getEntityClass());

		List<IndexableValue> ivs = new ArrayList<IndexableValue>();

		ivm.createIndexableValues(t, c->{
			ivs.add(c);
		});
		try{
			assertTrue(matches.test(ivs.stream()));
		}catch(Throwable tt){
			throw tt;
		}
	}
	
	
	
	public void testIndexableValuesHasFacet(T t, String facetName, Object facetValue ){
		testIndexableValuesMatchCriteria(t, s->{
			boolean match =s.anyMatch(iv->{
				if(iv.facet()){
					if(iv.name().equals(facetName)){
						return facetValue.equals(iv.value());
					}
				}
				return false;
			});
			if(!match){
				throw new AssertionError("Did not find facet:" + facetName + " with value: " + facetValue);
			}
			return match;
		});
	}
}
