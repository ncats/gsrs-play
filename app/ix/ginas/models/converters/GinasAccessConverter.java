package ix.ginas.models.converters;

import java.util.BitSet;

import com.avaje.ebean.config.ScalarTypeConverter;

import ix.core.Converter;
import ix.core.controllers.AdminFactory;
import ix.core.models.Group;
import ix.ginas.models.GinasAccessContainer;

@Converter
public class GinasAccessConverter implements ScalarTypeConverter<GinasAccessContainer, byte[]> {

	@Override
	public GinasAccessContainer getNullValue() {
		return null;
	}

	@Override
	public byte[] unwrapValue(GinasAccessContainer arg0) {
		BitSet bs = new BitSet();
		
		try {
			for(Group g: arg0.getAccess()){
				Long lid=g.id;
				if(lid==null){
					throw new IllegalStateException(g.name + " not persisted yet?");
				}else{
					bs.set(lid.intValue());
				}
			}
			//System.out.println("serializaing access");
			return bs.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public GinasAccessContainer wrapValue(byte[] arg0) {
		BitSet bs = BitSet.valueOf(arg0);
		
		try {
			GinasAccessContainer gac=new GinasAccessContainer();
			for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
			     Group g1=AdminFactory.getGroupById((long)i);
			     if(g1!=null){
			    	 gac.add(g1);
			     }
			     if (i == Integer.MAX_VALUE) {
			         break; // or (i+1) would overflow
			     }
			}
			return gac;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getNullValue();
	}


    
}