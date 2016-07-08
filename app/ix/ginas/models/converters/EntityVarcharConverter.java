package ix.ginas.models.converters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebean.text.TextException;
import com.avaje.ebeaninternal.server.type.DataBind;
import com.avaje.ebeaninternal.server.type.DataReader;
import com.avaje.ebeaninternal.server.type.ScalarTypeBase;
import com.fasterxml.jackson.core.JsonGenerator;

import ix.core.Converter;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;

public abstract class EntityVarcharConverter<K> extends ScalarTypeBase<K> {
	
	
	public EntityVarcharConverter(Class<K> c){
		super(c,false, Types.VARCHAR);
	}

	public abstract String convertToString(K value) throws IOException;
	public abstract K convertFromString(String bytes) throws IOException;
	
	@Override
	public void bind(DataBind b, K value) throws SQLException {
		if (value == null) {
			b.setNull(jdbcType);
		} else {
			try{
				b.setString(convertToString(value));
			}catch(Exception e){
				e.printStackTrace();
				throw new SQLException(e);
			}
		}
	}

	@Override
	public String formatValue(K arg0) {
		throw new TextException("Not supported");
	}

	@Override
	public boolean isDateTimeCapable() {
		return false;
	}

	@Override
	public K parse(String arg0) {
		throw new TextException("Not supported");
	}

	//apparently removed from newest ebean
	public K parseDateTime(long arg0) {
		throw new TextException("Not supported, really");
	}
	public K convertFromMillis(long arg0) {
		return parseDateTime(arg0);
	}

	@Override
	public K read(DataReader reader) throws SQLException {
		// TODO Auto-generated method stub
		String b=reader.getString();
		try{
			return convertFromString(b);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException(e);
		}
		
	}

	
	// I'm not sure what's going on here ...
	// This used to return an "object", but now returns "K"
	// to be compliant with new ebean interface (which remains elusive)
	
	@Override
	public K readData(DataInput dataInput) throws IOException {
		if (!dataInput.readBoolean()) {
		      return null;
		} else {
		      int len = dataInput.readInt();
		      byte[] buf = new byte[len];
		      dataInput.readFully(buf, 0, buf.length);
		      return convertFromString(new String(buf, "UTF-8"));
	    }
	}

	@Override
	public K toBeanType(Object value) {
		return (K)value;
	}

	@Override
	public Object toJdbcType(Object arg0) {
		return arg0;
	}

	@Override
	public void writeData(DataOutput dataOutput, Object v) throws IOException {
		if (v == null) {
		      dataOutput.writeBoolean(false);
		    } else {
		      dataOutput.writeBoolean(true);
		      String val = convertToString(toBeanType(v));
		      dataOutput.writeUTF(val);
		    }
	}
	
	public void jsonWrite(JsonGenerator gen, String s, K kv) {
		     throw new IllegalStateException("jsonWrite Not implemented");
	}
	
	public K jsonRead(com.fasterxml.jackson.core.JsonParser jp,com.fasterxml.jackson.core.JsonToken jt){
		throw new IllegalStateException("jsonRead Not implemented");
	}
	
	    
	


    
}