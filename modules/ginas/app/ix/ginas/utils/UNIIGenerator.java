package ix.ginas.utils;


public class UNIIGenerator extends UniiLikeGenerator{


	public UNIIGenerator() {
		super(9, true);
	}

	@Override
	public String getName() {
		return "UNII";
	}

	@Override
	protected String getRandomPartOf(String id) {
		return id;
	}
}