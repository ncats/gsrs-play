package ix.ginas.models.v1;

import java.util.Objects;

/**
 *
 * @author mitch
 */
public class QualifiedAtom{
	public QualifiedAtom(String symbol, int massIndication) {
		this.symbol = symbol;
		this.massIndication = massIndication;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 29 * hash + Objects.hashCode(this.symbol);
		hash = 29 * hash + this.massIndication;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final QualifiedAtom other = (QualifiedAtom) obj;
		if (this.massIndication != other.massIndication) {
			return false;
		}
		if (!Objects.equals(this.symbol, other.symbol)) {
			return false;
		}
		return true;
	}

	private String symbol;
	private int massIndication;

	public String getSymbol() {
		return symbol;
	}


	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public int getMassIndication() {
		return massIndication;
	}

	public void setMassIndication(int massIndication) {
		this.massIndication = massIndication;
	}
	
	
}
