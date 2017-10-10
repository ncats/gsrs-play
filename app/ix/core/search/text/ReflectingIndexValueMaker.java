package ix.core.search.text;

import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.Field.Store.YES;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.facet.FacetField;

import ix.core.search.text.PathStack;
import ix.core.search.text.TextIndexer;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import play.Logger;

public class ReflectingIndexValueMaker<T> implements IndexValueMaker<T>{

	public class IndexingFieldCreator implements BiConsumer<PathStack, EntityUtils.EntityWrapper>{
		Consumer<IndexableValue> toAdd;
		private EntityWrapper firstValue=null;
		
		public IndexingFieldCreator(Consumer<IndexableValue> toAdd) {
			Objects.requireNonNull(toAdd);
			this.toAdd = toAdd;  //where to put the fields
		}
	
		public void acceptWithGeneric(PathStack path, EntityUtils.EntityWrapper<Object> ew) {
			toAdd.accept(new IndexableValueDirect(new FacetField(TextIndexer.DIM_CLASS, ew.getKind())));

				ew.getId().ifPresent(o -> {
					if (o instanceof Long) {
						toAdd.accept(new IndexableValueDirect(new LongField(ew.getInternalIdField(), (Long) o, YES)));
					} else {
						toAdd.accept(new IndexableValueDirect(new StringField(ew.getInternalIdField(), o.toString(), YES)));  //Only Special case
					}
					toAdd.accept(new IndexableValueDirect(new StringField(ew.getIdField(), o.toString(), NO)));
				}); //
	
				// primitive fields only, they should all get indexed
				ew.streamFieldsAndValues(f -> f.isPrimitive()).forEach(fi -> {
					path.pushAndPopWith(fi.k().getName(), () -> {
						toAdd.accept(IndexableValueFromIndexable.of( path.getFirst(), fi.v(), path.toPath(),
								fi.k().getIndexable()));
					});
				}); //Primitive fields
	
				ew.getDynamicFacet().ifPresent(fv -> {
					path.pushAndPopWith(fv.k(), () -> {
						toAdd.accept(new IndexableValueFromRaw(fv.k(), fv.v(), path.toPath()).dynamic().suggestable());
					});
				}); //Dynamic Facets
				
				ew.streamMethodKeywordFacets().forEach(kw -> {
					path.pushAndPopWith(kw.label, () -> {
						toAdd.accept(new IndexableValueFromRaw(kw.label, kw.getValue(), path.toPath()).dynamic().suggestable());
					});
				}); //Method keywords
	
				ew.streamMethodsAndValues(m -> m.isArrayOrCollection()).forEach(t -> {
					path.pushAndPopWith(t.k().getName(), () -> {
						t.k().forEach(t.v(), (i, o) -> {
							path.pushAndPopWith(i + "", () -> {
								toAdd.accept(IndexableValueFromIndexable.of( path.getFirst(), o,
										path.toPath(), t.k().getIndexable()));
							});
						});
					});
				});// each array / collection
	
				ew.streamMethodsAndValues(m -> !m.isArrayOrCollection()).forEach(t -> {
					path.pushAndPopWith(t.k().getName(), () -> {
						toAdd.accept(IndexableValueFromIndexable.of(path.getFirst(), t.v(), path.toPath(),
								t.k().getIndexable()));
					});
				});// each non-array
	
				ew.streamFieldsAndValues(f -> (!f.isPrimitive() && !f.isArrayOrCollection())).forEach(fi -> {
					path.pushAndPopWith(fi.k().getName(), () -> {
						if (fi.k().isEntityType()) {
							if (fi.k().isExplicitlyIndexable()) {
								toAdd.accept(IndexableValueFromIndexable.of(path.getFirst(), fi.v(),
										path.toPath(), fi.k().getIndexable()));
							}
						} else { // treat as string
							toAdd.accept(IndexableValueFromIndexable.of(path.getFirst(), fi.v(),
									path.toPath(), fi.k().getIndexable()));
						}
					}); // for each field with value
				}); // foreach non-primitive field
		}
	
		
		//Just had some generic problems, so this delegates
		// TODO: clean up
		@Override
		public void accept(PathStack t, EntityUtils.EntityWrapper u) {
			if(firstValue ==null){
				firstValue = u;
			}
			acceptWithGeneric(t, u);
		}

	}

	@Override
	public void createIndexableValues(T t, Consumer<IndexableValue> consumer) {
		EntityWrapper<T> ew=EntityWrapper.of(t);
		IndexingFieldCreator ifc= new IndexingFieldCreator(consumer);
		ew.traverse().execute(ifc);
	}

}
