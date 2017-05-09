package ix.core.factories;

import java.util.Set;

import ix.core.initializers.Initializer;
import ix.core.util.CachedSupplier;
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
			.map(m->(String)m.get("class"))
			.map(n->CachedSupplier.ofThrowing(()->Class.forName(n).newInstance()))
			.filter(p->!p.getThrown().isPresent())
			.map(o->(Initializer)o.get())
			.forEach(rf->{
				this.register(Initializer.class, rf, false);
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
