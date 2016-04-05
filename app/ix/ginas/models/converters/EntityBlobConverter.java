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

import ix.core.Converter;
import ix.core.controllers.EntityFactory;
import ix.core.controllers.EntityFactory.EntityMapper;

public abstract class EntityBlobConverter<K> extends ScalarTypeBase<K> {
	
	
	public EntityBlobConverter(Class<K> c){
		super(c,false, Types.BLOB);
	}

	public abstract byte[] convertToBytes(K value) throws IOException;
	public abstract K convertFromBytes(byte[] bytes) throws IOException;
	
	@Override
	public void bind(DataBind b, K value) throws SQLException {
		if (value == null) {
			b.setNull(jdbcType);
		} else {
			try{
				b.setBytes(convertToBytes(value));
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

	@Override
	public K parseDateTime(long arg0) {
		throw new TextException("Not supported, really");
	}

	@Override
	public K read(DataReader reader) throws SQLException {
		// TODO Auto-generated method stub
		byte[] b=reader.getBlobBytes();
		try{
			return convertFromBytes(b);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException(e);
		}
		
	}

	@Override
	public Object readData(DataInput dataInput) throws IOException {
		if (!dataInput.readBoolean()) {
		      return null;
		} else {
		      int len = dataInput.readInt();
		      byte[] buf = new byte[len];
		      dataInput.readFully(buf, 0, buf.length);
		      return buf;
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
		      byte[] bytes = convertToBytes(toBeanType(v));
		      dataOutput.writeInt(bytes.length);
		      dataOutput.write(bytes);
		    }
	}
	
	    


    
}