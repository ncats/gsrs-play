package ix.core.chem;

public class ElementInfo {
	public String symbol;
	private int count;
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	private String isotopicForm = "";
	

	public ElementInfo(String s, int c) {
		this.count = c;
		this.symbol = s;
	}
	
	public ElementInfo(String s, int c, String isotopicForm) {
		this.count = c;
		this.symbol = s;
		this.isotopicForm=isotopicForm;
	}
}
