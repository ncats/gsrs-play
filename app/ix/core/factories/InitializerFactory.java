package ix.core.factories;

import java.util.Set;

import ix.core.initializers.Initializer;
import ix.core.util.CachedSupplier;
import ix.core.util.IOUtil;
import ix.utils.Tuple;
import play.Application;

public class InitializerFactory extends InternalMapEntityResourceFactory<Initializer>{
	private static InitializerFactory _instance;
	
	
	
	
	public InitializerFactory(Application app) {
		super(app);
		_instance=this;
	}

	@Override
	public void initialize(Application app) {
		this.getStandardResourceStream(app, "ix.core.initializers")

			.map(m->Tuple.of((String)m.get("class"),m))
			.map(Tuple.kmap(n->CachedSupplier.ofThrowing(()-> IOUtil.getGinasClassLoader().loadClass(n).newInstance())))
			.filter(p->!p.k().getThrown().isPresent())
			.map(Tuple.kmap(o->(Initializer)o.get()))
			.map(t->t.k().initializeWith(t.v()))
			.forEach(iniz->{
			    
				this.register(Initializer.class, (Initializer)iniz, false);
			});
	}
	
	public Set<Initializer> getInitializers(){
		return this.getRegisteredResourcesFor(Initializer.class);
	}
	
	public static InitializerFactory getInstance(Application app){
		if(_instance!=null){
			return _instance;
		}
		return new InitializerFactory(app);
	}

}
