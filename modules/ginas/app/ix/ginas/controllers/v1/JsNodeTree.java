package ix.ginas.controllers.v1;

public class JsNodeTree {
	public String id;
	public String text;
	public String parent;
	public String icon = "fa fa-folde-o";
	public Object value;

	public static class Builder {
		private String id;
		private String text;
		private String parent;
		private String icon= "fa fa-folde-o";
		private Object value;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder text(String text) {
			this.text = text;
			return this;
		}

		public Builder parent(String parent) {
			this.parent = parent;
			return this;
		}

		public Builder icon(String icon) {
			this.icon = icon;
			return this;
		}
		
		public Builder value(Object val) {
			this.value=val;
			return this;
		}

		public JsNodeTree build() {
			return new JsNodeTree(this);
		}
	}

	private JsNodeTree(Builder builder) {
		this.id = builder.id;
		this.text = builder.text;
		this.parent = builder.parent;
		this.icon = builder.icon;
		this.value = builder.value;
	}
}