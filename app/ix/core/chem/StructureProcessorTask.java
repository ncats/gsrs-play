package ix.core.chem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.nih.ncats.molwitch.Chemical;
import ix.core.models.Structure;

public class StructureProcessorTask {
	public boolean isStandardize() {
		return standardize;
	}

	public void setStandardize(boolean standardize) {
		this.standardize = standardize;
	}

	public boolean isQuery() {
		return isQuery;
	}

	public void setQuery(boolean isQuery) {
		this.isQuery = isQuery;
	}

	public Chemical getMol() {
		return mol;
	}
	


	public Chemical getChemical(){
		return mol;
	}

	public void setMol(Chemical mol) {
		this.mol = mol;
	}

	public Structure getStructure() {
		return structure;
	}

	public void setStructure(Structure structure) {
		this.structure = structure;
	}

	public Collection<Structure> getComponents() {
		return components;
	}

	public void setComponents(List<Structure> components) {
		this.components = components;
	}
	
	
	public Structure instrument(){
		StructureProcessor.instrument(this);
		return this.structure;
	}

	private boolean standardize;
	private boolean isQuery;
	private Chemical mol;
	private Structure structure;
	private Collection<Structure> components;

	public static class Builder {
		private boolean standardize=true;
		private boolean query=false;
		
		
		private Chemical mol=null;
		private Structure structure = new Structure();
		private Collection<Structure> components = new ArrayList<>();

		public Builder standardize(boolean standardize) {
			this.standardize = standardize;
			return this;
		}

		public Builder query(boolean isQuery) {
			this.query = isQuery;
			return this;
		}

		public Builder mol(Chemical mol) {
			this.mol = mol;
			return this;
		}
		

		
		public Builder mol(String mol) throws Exception{

			this.mol = Chemical.parse(ChemCleaner.getCleanMolfile(mol));
			
			return this;
		}

		public Builder structure(Structure structure) {
			this.structure = structure;
			return this;
		}

		public Builder components(Collection<Structure> components) {
			this.components = components;
			return this;
		}

		public StructureProcessorTask build() {
			return new StructureProcessorTask(this);
		}
	}

	private StructureProcessorTask(Builder builder) {
		this.standardize = builder.standardize;
		this.isQuery = builder.query;
		this.mol = builder.mol;
		this.structure = builder.structure;
		this.components = builder.components;
	}
}