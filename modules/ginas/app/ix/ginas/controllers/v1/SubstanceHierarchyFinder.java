package ix.ginas.controllers.v1;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ix.core.plugins.IxCache;
import ix.core.util.CachedSupplier;
import ix.core.util.EntityUtils;
import ix.core.util.EntityUtils.EntityWrapper;
import ix.core.util.StreamUtil;
import ix.ginas.initializers.HierarchyFinderInitializer;
import ix.ginas.models.utils.RelationshipUtil;
import ix.ginas.models.v1.Relationship;
import ix.ginas.models.v1.SpecifiedSubstanceGroup1Substance;
import ix.ginas.models.v1.StructurallyDiverseSubstance;
import ix.ginas.models.v1.Substance;
import ix.ginas.models.v1.SubstanceReference;
import ix.ginas.models.v1.MixtureSubstance;
import ix.ginas.utils.GinasUtils;
import ix.ncats.controllers.App;
import ix.utils.Tuple;
import play.db.ebean.Model.Finder;
import play.twirl.api.Html;

/**
 * Finds the hierarchy of relevant {@link Substance}s, used for giving
 * context of related substances which have a hierarchical nature.
 *  
 * @author tyler
 *
 */
public class SubstanceHierarchyFinder {
	
	private static final String ROOT_TYPE = "ROOT";

	public static class NonSerializableWrapper<T>{
		private T t;
		public NonSerializableWrapper(T t){
			this.t=t;
		}
		public static <T> NonSerializableWrapper<T> of(T t){
			return new NonSerializableWrapper<T>(t);
		}
		public T fetchIt(){
			return t;
		}
	}
	
	
	private CachedSupplier<List<HierarchyFinder>> hfinders=CachedSupplier.of(()->{
		/*
		List<HierarchyFinder> finders= new ArrayList<>();
		finders.add(new NonInvertibleRelationshipHierarchyFinder(Relationship.ACTIVE_MOIETY_RELATIONSHIP_TYPE).renameChildType((p,c)->"HAS ACTIVE MOIETY:\"" + p.getName() + "\""));
		finders.add(new InvertibleRelationshipHierarchyFinder("SALT/SOLVATE->PARENT").renameChildType("IS SALT/SOLVATE OF"));
		finders.add(new InvertibleRelationshipHierarchyFinder("SUB_CONCEPT->SUBSTANCE").renameChildType("IS SUBCONCEPT OF"));
		//finders.add(new MixtureComponentFinder().renameChildType("IS MIXTURE CONSTITUENT"));
		finders.add(new G1SSConstituentFinder().renameChildType("IS G1SS CONSTITUENT OF"));
		finders.add(new StructurallyDiverseParentFinder());
		//StructurallyDiverseParentFinder
		//finders.add(new InvertibleRelationshipHierarchyFinder("SUB_CONCEPT->SUBSTANCE"));
		return finders;		

		 */
		return HierarchyFinderInitializer.getInstance().getFinders();
	});
	
	private List<Tuple<String,Substance>> getRelated(Substance s, Stream<HierarchyFinder> finders, String type){
		try{
			String key=type +s.uuid;
			Object o=IxCache.getOrElse(
					App.getTextIndexer().lastModified(),
					key, ()->
			NonSerializableWrapper.of(finders
					.map(f->f.findChildren(s))
					.flatMap(l->l.stream())
					.collect(Collectors.toList()))
					);
			
			NonSerializableWrapper<List<Tuple<String,Substance>>> wrapped= (NonSerializableWrapper<List<Tuple<String, Substance>>>) o;
			return wrapped.fetchIt();
		}catch(Exception e){
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	public List<Tuple<String,Substance>> getChildren(Substance s){
		return getRelated(s,
				hfinders.get().stream(),
				"children_");
	}
	
	public List<Tuple<String,Substance>> getParents(Substance s){
		return getRelated(s,
				hfinders.get().stream().map(h->h.reverse()),
				"parents_");
	}
	
	private TreeNode<Substance> getAllTree(Substance s, Function<Substance,List<Tuple<String,Substance>>> getter){

		TreeNode<Substance> root = new TreeNode<Substance>(ROOT_TYPE,s);
		
		Set<String> resolved = new HashSet<String>();
		
		Predicate<Substance> isResolved=(t)->{
				return !resolved.add(t.uuid.toString());
		};
		
		root.traverseBreadthFirst(l->{
			//get last
			TreeNode<Substance> t=l.get(l.size()-1);
			if(!isResolved.test(t.value)){
				List<TreeNode<Substance>> children=getter.apply(t.value).stream()
				                        .map(ts->new TreeNode<Substance>(ts.k(),ts.v()))
				                        .collect(Collectors.toList());
				t.children(children);
				return true;
			}
			return false;
		});
		return root;
	}
	
	public TreeNode<Substance> getAllChildren(Substance s){
		return getAllTree(s,s1->getChildren(s1));
	}
	
	public TreeNode<Substance> getAllParents(Substance s){
		return getAllTree(s,s1->getParents(s1));
	}
	
	public List<Substance> getBestRootParents(Substance s){
		TreeNode<Substance> tn = getAllParents(s);
		return tn.getAllLeafPaths()
		  .stream()
		  .map(t->t.get(t.size()-1))
		  .map(t->t.value)
		  .map(t->Tuple.of(t.uuid,t))
		  .map(t->t.withKEquality())
		  .distinct()
		  .map(t->t.v())
		  .collect(Collectors.toList());	
	}
	
	
	public List<TreeNode<Substance>> getHierarchies(Substance s){
		return getBestRootParents(s)
				.stream()
				.map(sp->getAllChildren(sp))
				.collect(Collectors.toList());	
	}
	
	
	
	
	
	
	public static class TreeNode<T>{
		private String type;
		private T value;
		
		private List<TreeNode<T>> children;
		
		public TreeNode(String type, T s, List<TreeNode<T>> children){
			this.value=s;
			this.children=children;
			this.type=type;
		}
		public TreeNode(String type, T s){
			this(type,s, new ArrayList<TreeNode<T>>());
		}
		
		public TreeNode<T> children(List<TreeNode<T>> nodes){
			this.children=nodes;
			return this;
		}
		
		
		public Stream<List<TreeNode<T>>> streamPaths(){
			return StreamUtil.forYieldingGenerator((c)->{
				traverseDepthFirst(l->{
					c.accept(l.stream().collect(Collectors.toList()));
					return true;
				});
			});
		}
		
		public T getValue(){
			return this.value;
			
		}
		
		public String getType(){
			return this.type;
		}
		
		public void traverseDepthFirst(Predicate<List<TreeNode<T>>> shouldContinue){
			traverseDepthFirst(shouldContinue, new ArrayList<TreeNode<T>>());			
		}
		
		private void traverseDepthFirst(Predicate<List<TreeNode<T>>> shouldContinue, List<TreeNode<T>> soFar){
			soFar.add(this);
			if(shouldContinue.test(soFar)){
				this.children.stream().forEach(c->c.traverseDepthFirst(shouldContinue, soFar));
			}
			soFar.remove(this);
		}
		
		public void traverseBreadthFirst(Predicate<List<TreeNode<T>>> shouldContinue){
			List<List<TreeNode<T>>> soFar = new ArrayList<>();
			soFar.add(Stream.of(this).collect(Collectors.toList()));
		
			while(soFar.size()>0){
				List<List<TreeNode<T>>> temp = new ArrayList<>();	
				for(List<TreeNode<T>> t: soFar){
					if(shouldContinue.test(t)){
						for(TreeNode<T> t2: t.get(t.size()-1).children){
							t.add(t2);
							temp.add(t.stream().collect(Collectors.toList()));
							t.remove(t.size()-1);
						}
					}
				}
				soFar=temp;
			}
		}
		
		public void printHierarchy(Function<TreeNode<T>, String> toString){
			System.out.println(toString);
		}
		
		public String stringHierarchy(Function<TreeNode<T>,String> toString){
			return this.streamPaths()
			    .map(l->{
			    	String space=IntStream.range(0,l.size()).mapToObj(i->"-").collect(Collectors.joining(""));
					TreeNode<T> fin=l.get(l.size()-1);
					return space + toString.apply(fin);
			    })
			    .collect(Collectors.joining("\n"));
		}
		
		@JsonIgnore
		public List<List<TreeNode<T>>> getAllLeafPaths(){
			return streamPaths()
			    .filter(l->l.get(l.size()-1).isLeaf())
			    .collect(Collectors.toList());
		}
		
		public boolean isLeaf(){
			return this.children.size()==0;
		}
		
		
		public <U> TreeNode<U> map(Function<T,U> mapper){
			TreeNode<U> tn = new TreeNode<U>(this.type, mapper.apply(this.value));
			return tn.children(this.children.stream().map(t1->t1.map(mapper)).collect(Collectors.toList()));
		}
		
	}
	
	
	public static interface HierarchyFinder{
		public List<Tuple<String,Substance>> findParents(Substance s);
		public List<Tuple<String,Substance>> findChildren(Substance s);		
		public default HierarchyFinder reverse(){
			HierarchyFinder _this=this;
			return new HierarchyFinder(){

				@Override
				public List<Tuple<String, Substance>> findParents(Substance s) {
					return _this.findChildren(s);
				}

				@Override
				public List<Tuple<String, Substance>> findChildren(
						Substance s) {
					return _this.findParents(s);
				}
				
			};
		}
		
		public default HierarchyFinder renameTypes(Function<String,String> trenamer){
			HierarchyFinder _this=this;
			return new HierarchyFinder(){

				@Override
				public List<Tuple<String, Substance>> findChildren(Substance s) {
					return _this.findChildren(s).stream().map(Tuple.kmap(trenamer)).collect(Collectors.toList());
				}

				@Override
				public List<Tuple<String, Substance>> findParents(
						Substance s) {
					return _this.findParents(s).stream().map(Tuple.kmap(trenamer)).collect(Collectors.toList());
				}
				
			};			
		}
		
		public default HierarchyFinder renameChildType(String newName){
			HierarchyFinder _this=this;
			
				return new HierarchyFinder(){

					@Override
					public List<Tuple<String, Substance>> findChildren(Substance s) {
						return _this.findChildren(s).stream().map(Tuple.kmap((k)->newName)).collect(Collectors.toList());
					}

					@Override
					public List<Tuple<String, Substance>> findParents(
							Substance s) {
						return _this.findParents(s);
					}
				};			
		}
		
		public default HierarchyFinder renameChildType(BiFunction<Substance,Substance,String> parentChildRenamer){
			HierarchyFinder _this=this;

				return new HierarchyFinder(){

					@Override
					public List<Tuple<String, Substance>> findChildren(Substance s) {
						return _this.findChildren(s).stream()
								                    .map(t->{
								                    	String newName = parentChildRenamer.apply(s, t.v());
								                    	return Tuple.of(newName,t.v());
								                    })
								                    .collect(Collectors.toList());
					}

					@Override
					public List<Tuple<String, Substance>> findParents(
							Substance s) {
						return _this.findParents(s);
					}
				};
		}

		public default HierarchyFinder renameParentType(BiFunction<Substance,Substance,String> parentChildRenamer){
			return this.reverse().renameChildType((p,c)->parentChildRenamer.apply(c, p)).reverse();
		}

		public default HierarchyFinder renameParentType(String newName){
			return this.reverse().renameChildType(newName).reverse();			
		}
		
	}
	
	/**
	 * This hierarchy finder finds invertible relationships
	 * @author tyler
	 *
	 */
	public static class InvertibleRelationshipHierarchyFinder implements HierarchyFinder{
		
		private String downwardWay;
		private String upwardWay;
		public InvertibleRelationshipHierarchyFinder(String downWardRelationship){
			this.downwardWay=downWardRelationship;
			this.upwardWay=RelationshipUtil.reverseRelationship(this.downwardWay);
		}
		@Override
		public List<Tuple<String, Substance>> findParents(Substance s) {
			return findDirectRelated(s,this.upwardWay);
		}
		@Override
		public List<Tuple<String, Substance>> findChildren(Substance s) {
			return findDirectRelated(s,this.downwardWay);
		}
	}
	
	/**
	 * This hierarchy finder finds invertible relationships
	 * @author tyler
	 *
	 */
	public static class G1SSConstituentFinder implements HierarchyFinder{
		
		public G1SSConstituentFinder(){
		}
		@Override
		public List<Tuple<String, Substance>> findParents(Substance s) {
			if(s instanceof SpecifiedSubstanceGroup1Substance){
				SpecifiedSubstanceGroup1Substance ssg=(SpecifiedSubstanceGroup1Substance)s;
				return ssg.specifiedSubstance
					.constituents
					.stream()
					.map(c->Tuple.of("G1SS:" + c.role,SubstanceFactory.getFullSubstance(c.substance)))
					.filter(rs->rs.v()!=null)
					.collect(Collectors.toList());
				 
			}
			return new ArrayList<>();
		}
		@Override
		public List<Tuple<String, Substance>> findChildren(Substance s) {
			return SubstanceFactory.finder.get()
					 .where()
					 .eq("specifiedSubstance.constituents.substance.refuuid", s.uuid.toString())
					 .findList()
					 .stream()
					 .map(sg->(SpecifiedSubstanceGroup1Substance)sg)
					 .map(sg->Tuple.of(sg.specifiedSubstance
							             .constituents
							             .stream()
							             .filter(c->s.uuid.toString().equals(c.substance.refuuid))
							             .map(c->c.role)
							             .findFirst()
							             .orElse("UNKNOWN"),
							             sg
							 ))
					 .map(Tuple.kmap(k->"HAS G1SS:" + k))
					 .map(Tuple.vmap(sub->(Substance)sub))
					 .collect(Collectors.toList());
		}
	}
	public static List<MixtureSubstance> getMixturesContaining(Substance s){
		MixtureComponentFinder finder= new MixtureComponentFinder();
		return finder.findChildren(s)
				.stream()
				.map(t->(MixtureSubstance)t.v())
				.collect(Collectors.toList());
	}

	public static List<SpecifiedSubstanceGroup1Substance> getG1SSContaining(Substance s){
		G1SSConstituentFinder finder= new G1SSConstituentFinder();
		return finder.findChildren(s)
		      .stream()
		      .map(t->(SpecifiedSubstanceGroup1Substance)t.v())
		      .collect(Collectors.toList());
	}


	public static class MixtureComponentFinder implements HierarchyFinder {
		public MixtureComponentFinder(){
		}
		@Override
		public List<Tuple<String, Substance>> findParents(Substance s) {
			if(s instanceof MixtureSubstance){
				MixtureSubstance ssg=(MixtureSubstance)s;
				return ssg.mixture
						.components
						.stream()
						.map(c->Tuple.of("Mixture Component" + c,SubstanceFactory.getFullSubstance(c.substance)))
						.filter(rs->rs.v()!=null)
						.collect(Collectors.toList());

			}
			return new ArrayList<>();
		}
		@Override
		public List<Tuple<String, Substance>> findChildren(Substance s) {
			return SubstanceFactory.finder.get()
					.where()
					.eq("mixture.components.substance.refuuid", s.uuid.toString())
					.findList()
					.stream()
					.map(ms -> (MixtureSubstance) ms)
					.map(ms -> Tuple.of("EXISTS IN MIXTURE",
							ms))
					.map(Tuple.vmap(sub -> (Substance) sub))
					.collect(Collectors.toList());
		}
	}
	/**
	 * This hierarchy finder finds parent source material
	 * @author tyler
	 *
	 */
	public static class StructurallyDiverseParentFinder implements HierarchyFinder{
		
		public StructurallyDiverseParentFinder(){
		}
		@Override
		public List<Tuple<String, Substance>> findParents(Substance s) {
			if(s instanceof StructurallyDiverseSubstance){
				StructurallyDiverseSubstance ssg=(StructurallyDiverseSubstance)s;
				return Optional.ofNullable(ssg.structurallyDiverse.parentSubstance)
					.map(c->Tuple.of("Source Parent",SubstanceFactory.getFullSubstance(c)))
					.filter(rs->rs.v()!=null)
					.map(c->Stream.of(c).collect(Collectors.toList()))
					.orElse(new ArrayList<>());
				 
			}
			return new ArrayList<>();
		}
		@Override
		public List<Tuple<String, Substance>> findChildren(Substance s) {
			return SubstanceFactory.finder.get()
					 .where()
					 .eq("structurallyDiverse.parentSubstance.refuuid", s.uuid.toString())
					 .findList()
					 .stream()
					 .map(sg->Tuple.of("Source Child",
							             sg
							 ))
					 .collect(Collectors.toList());
		}
	}
	
	public static class NonInvertibleRelationshipHierarchyFinder implements HierarchyFinder{
		
		private String fetchableDirection;
		private String reverseName;
		
		public NonInvertibleRelationshipHierarchyFinder(String fetchableDirection, String reverseName){
			this.reverseName=reverseName;
			this.fetchableDirection=fetchableDirection;
		}
		
		public NonInvertibleRelationshipHierarchyFinder(String fetchableDirection){
			this(fetchableDirection,"HAS " + fetchableDirection);
		}
		
		/**
		 * This way is assumed to be fetchable
		 */
		@Override
		public List<Tuple<String, Substance>> findParents(Substance s) {
			return findDirectRelated(s,this.fetchableDirection);
		}
		
		/**
		 * This way is assumed non-fetchable
		 */
		@Override
		public List<Tuple<String, Substance>> findChildren(Substance s) {
			
			String parentUUID=s.uuid.toString();
			
			Finder<Object,?> rfinder=EntityUtils.getEntityInfoFor(Relationship.class)
			           .getFinder();
			
			Map<String,Object> criteria = new HashMap<String,Object>();
			
			criteria.put("relatedSubstance.refuuid", parentUUID);
			criteria.put("type", this.fetchableDirection);
			
			return rfinder.where()
				   .allEq(criteria)
				   .findList()
				   .stream()
				   .map(r->(Relationship)r)
				   .map(r->Tuple.of(reverseName,r.fetchOwner()))
				   .map(t->Tuple.of(t.k() + "_" + t.v().uuid, t))
				   .map(t->t.withKEquality())
				   .distinct()
				   .map(t->t.v())
				   .collect(Collectors.toList());
		}
	}
	
	private static List<Tuple<String, Substance>> findDirectRelated(Substance s, String type){
		return s.relationships
				 .stream()
				 .filter(r->type.equals(r.type))
				 .map(r->Tuple.of(r.type,SubstanceFactory.getFullSubstance(r.relatedSubstance)))
				 .filter(rs->rs.v()!=null)
				 .collect(Collectors.toList());
	}

	
	public static Html htmlHelper(TreeNode<Substance> tn){
		return new Html("<pre>" + tn.map(s->s.asSubstanceReference()).stringHierarchy(t->t.value.getName() + "[" + t.type + "]") + "</pre>");
	}
	
	public static List<TreeNode2> makeJsonTreeForAPI(Substance sub) {

		List<TreeNode<Substance>> tnlist = (new SubstanceHierarchyFinder())
				.getHierarchies(sub);


		TreeNode2Builder builder = new TreeNode2Builder();
		for (TreeNode<Substance> n : tnlist) {
			n.traverseDepthFirst(l -> {
				TreeNode<Substance> fin = l.get(l.size() - 1);
				String text = ("[" + fin.value.getApprovalIDDisplay() + "] "
						+ fin.value.getName()
						+ (fin.type.equals(ROOT_TYPE) ? "" : " {" + fin.type + "}")).toUpperCase();

				builder.addNode(text, fin.type, l.size() - 1, fin.value.asSubstanceReference());
//				System.out.println(text + "\n  " + namer.apply(fin) + "  depth = " + l.size() );
				return true;
			});
		}
		List<TreeNode2> nodes = builder.build();
//		try {
//			System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(nodes));
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}

		return nodes;
	}
	public static List<JsNodeTree> makeJsonTree(Substance sub){

		List<TreeNode<Substance>> tnlist = (new SubstanceHierarchyFinder())
				.getHierarchies(sub);

		Function<TreeNode<Substance>, String> namer = (tn1)->{
			return tn1.value.uuid +	tn1.type;
		};

		Map<String,String> uniqueID = new HashMap<String,String>();

		AtomicInteger ai = new AtomicInteger(0);


		return
				tnlist.stream()
				.flatMap(tn->tn.streamPaths()
						.map(l->{
							TreeNode<Substance> fin=l.get(l.size()-1);

							List<String> path1=l.stream()
									.map(namer)
									.collect(Collectors.toList());

							String id=path1.stream().collect(Collectors.joining("!_!"));
							String path=path1.stream()
									.limit(l.size()-1)
									.collect(Collectors.joining("!_!"));
							id=uniqueID.computeIfAbsent(id,(k)->ai.getAndIncrement()+"");
							path=uniqueID.computeIfAbsent(path,(k)->ai.getAndIncrement()+"");
							if(l.size()==1){
								path="#";
							}

							String showID="NO " + GinasUtils.getApprovalIdGenerator().getName();
							

							String text=("[" + fin.value.getApprovalIDDisplay() + "] " 
									+ fin.value.getName()
									+ (fin.type.equals(ROOT_TYPE)?"": " {" + fin.type + "}")).toUpperCase();



							return new JsNodeTree.Builder()
									.id(id)
									.parent(path)
									.text(text)
									.value(new TreeNode<SubstanceReference>(fin.type,fin.value.asSubstanceReference()))
									.build();

						}))
				.collect(Collectors.toList());

	}
	
	public static String makeRawJsonTree(Substance s){
		return EntityWrapper.of(makeJsonTree(s)).toFullJson();
	}

	public static class TreeNode2{
		String text;
		String type;
		SubstanceReference value;
		int depth;
		boolean expandable;

		String id;
		String path;
		String parent;

		public String getParent() {
			return parent;
		}

		public void setParent(String parent) {
			this.parent = parent;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public SubstanceReference getValue() {
			return value;
		}

		public void setValue(SubstanceReference value) {
			this.value = value;
		}

		public int getDepth() {
			return depth;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

		public boolean isExpandable() {
			return expandable;
		}

		public void setExpandable(boolean expandable) {
			this.expandable = expandable;
		}
	}

	public static class TreeNode2Builder{
		LinkedList<TreeNode2> nodes = new LinkedList<>();
		AtomicInteger counter= new AtomicInteger();
		Deque<TreeNode2> nestedNodes = new ArrayDeque<>();
		TreeNode2 root= null;
		public TreeNode2Builder addNode(String text,
				String type,
				int depth,
				SubstanceReference value){
			TreeNode2 node = new TreeNode2();
			node.setText(text);
			node.setType(type);
			node.setValue(value);
			node.setDepth(depth);
			node.setId(Integer.toString(counter.getAndAdd(1)));
			TreeNode2 last = nodes.peekLast();

			if(last ==null){
				node.setParent("#");
				nestedNodes.add(node);
				root = node;
			}else {
				int lastDepth = last.getDepth();
				if(lastDepth == depth){
					//sibling same parent
					node.setParent(last.getParent());
				}else{
					//different depth as last
					if(!nestedNodes.contains(last)) {
						nestedNodes.push(last);
					}
					if (lastDepth < depth) {
						//depth increasing
						last.setExpandable(true);
						//last is the parent
						node.setParent(last.getId());
						nestedNodes.push(node);

					}else {
						//depth decreasing
						int numberOfLevelsToPop = depth - lastDepth;

						TreeNode2 sameDepth = null;
						while (!nestedNodes.isEmpty() && sameDepth == null) {
							TreeNode2 popped = nestedNodes.pop();
							if (popped.getDepth() == depth) {
								sameDepth = popped;

							}
						}
						if(sameDepth ==null) {
							//this probably can't happen but just in case
							sameDepth = root;
						}
						node.setParent(sameDepth.getParent());
					}

					}
				}


			nodes.add(node);

			return this;
		}

		public List<TreeNode2> build(){
			return new ArrayList<>(nodes);
		}
	}

}
