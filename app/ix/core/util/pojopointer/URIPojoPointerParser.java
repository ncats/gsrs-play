package ix.core.util.pojopointer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import ix.utils.Tuple;

public class URIPojoPointerParser {
	public static enum ELEMENT_TYPE{
		FIELD,
		LAMBDA,
		LOCATOR
	}
	public static PojoPointer fromURI(String uripath) {

		if (!uripath.startsWith("/") && uripath.length() > 0 && uripath.charAt(0) != '('
				&& uripath.charAt(0) != URIPojoPointerParser.LAMBDA_CHAR) {
			uripath = "/" + uripath;
		}

		final PojoPointer root = new IdentityPath();
		PojoPointer parent = root;

		final Supplier<String> paths = splitUriPaths(uripath, '/');

		String jp = paths.get();
		for (int i = 0; jp != null; i++) {

			final int p = jp.indexOf("(");
			final int l = jp.indexOf(URIPojoPointerParser.LAMBDA_CHAR);

			String field = jp;
			if (p >= 0 || l >= 0) {
				int to = Math.min(p, l);
				if (to < 0) {
					to = Math.max(p, l);
				}
				field = field.substring(0, to);
			}

			if (i == 0) {
				if (p < 0 && l < 0) {
					jp = paths.get();
					continue;
				} else {
					field = null;
				}
			}

			boolean raw = false;

			if (field != null) {
				if (field.length() > 0 && field.charAt(0) == URIPojoPointerParser.RAW_CHAR) {
					raw = true;
					field = field.substring(1);
				}
				System.out.println("Field is:" + field);
				final PojoPointer c = new ObjectPath(field);
				parent.tail(c);
				parent = c;
			} else {
				final IdentityPath c = new IdentityPath();
				parent.tail(c);
				parent = c;
			}

			final Supplier<Tuple<URIPojoPointerParser.ELEMENT_TYPE, String>> parser = parse("_" + jp, 0);

			for (Tuple<URIPojoPointerParser.ELEMENT_TYPE, String> tup = parser.get(); tup != null; tup = parser.get()) {
				switch (tup.k()) {
				case FIELD:
					System.out.println("Not going to parse field:" + tup.v() + " already did!");
					break;
				case LAMBDA:
					System.out.println("It's a lambda");
					String lambdaString = tup.v();

					if (lambdaString.startsWith("(")) {
						lambdaString = "map" + lambdaString;
					}

					final String key = parse("_" + lambdaString, 0).get().v();
					System.out.println("Found key:" + key);

					final PojoPointer pp = LambdaParseRegistry
											.getPojoPointerParser(key)
											.apply(lambdaString);
					parent.tail(pp);
					parent = pp;

					break;
				case LOCATOR:
					System.out.println("It's:" + root.toURIpath());

					final boolean isNumber = tup.v().chars().skip(1).allMatch(Character::isDigit);
					if (tup.v().startsWith("" + URIPojoPointerParser.ARRAY_CHAR) && isNumber) {
						final ArrayPath ap = new ArrayPath(Integer.parseInt(tup.v().substring(1)));
						parent.tail(ap);
						parent = ap;
					} else if (tup.v().contains(":")) {
						final FilterPath fp = new FilterPath(tup.v());
						parent.tail(fp);
						parent = fp;
					} else {
						final IDFilterPath fp = new IDFilterPath(tup.v());

						parent.tail(fp);
						parent = fp;
					}
					System.out.println("Now it's:" + root.toURIpath());
					break;
				default:
					break;

				}
			}

			if (raw) {
				parent.setRaw(true);
			}
			jp = paths.get();
		}
		return root;
	}

	public static final char ARRAY_CHAR = '$';
	public static final char LAMBDA_CHAR = '!';
	public static final char OBJECT_FUNCTION_CHAR = '$';
	public static final char RAW_CHAR = '$';
	public static Supplier<String> getParentheses(final String parenGroup){
		final AtomicInteger charindex=new AtomicInteger();
		return ()->{
			int pcount=0;
			final int i=parenGroup.indexOf('(',charindex.get());
			if(i>=parenGroup.length() || i<0){
				return null;
			}
			for(int j=i;j<parenGroup.length();j++){
				switch(parenGroup.charAt(j)){
				case '(':
					pcount++;
					break;
				case ')':
					pcount--;
					if(pcount==0){
						charindex.set(j+1);
						return parenGroup.substring(i+1,j);
					}
					break;
				}
			}
			charindex.set(parenGroup.length());
			return null;
		};
	}
	public static Supplier<Tuple<URIPojoPointerParser.ELEMENT_TYPE,String>> parse(final String element, final int start){
		final AtomicInteger charindex=new AtomicInteger(start);
		return ()->{
			URIPojoPointerParser.ELEMENT_TYPE tt=null;
			int pcount=0;
			final int i=charindex.get();
			if(i>=element.length() || i<0){
				return null;
			}
			for(int j=i;j<element.length();j++){
				switch(element.charAt(j)){
				case URIPojoPointerParser.LAMBDA_CHAR:
					System.out.println("Start lambda:" + pcount);
					if(tt==null){
						tt=URIPojoPointerParser.ELEMENT_TYPE.LAMBDA;
					}
					if(tt==URIPojoPointerParser.ELEMENT_TYPE.FIELD){
						charindex.set(j);
						return Tuple.of(tt,element.substring(1,j));
					}
					break;
				case '(':
					if(tt==null){
						tt=URIPojoPointerParser.ELEMENT_TYPE.LOCATOR;
					}
					if(tt==URIPojoPointerParser.ELEMENT_TYPE.FIELD){
						charindex.set(j);
						return Tuple.of(tt,element.substring(1,j));
					}
					pcount++;
					break;
				case ')': //terminator for LAMDA and LOCATOR
					pcount--;
					if(pcount==0){
						charindex.set(j+1);

						System.out.println("End" + tt);
						if(tt==URIPojoPointerParser.ELEMENT_TYPE.LOCATOR){
							return Tuple.of(tt,element.substring(i+1,j));
						}
						return Tuple.of(tt,element.substring(i+1,j+1));
					}
					break;
				default:
					if(tt==null && j==start){
						tt=URIPojoPointerParser.ELEMENT_TYPE.FIELD;
					}else if(tt==null){
						throw new IllegalStateException("Couldn't parse");
					}
				}
			}
//			System.out.println("Returning null, but was set to:" + tt + " with "+ pcount);
//			System.out.println("on:" + element);
			return null;
		};
	}

	public static Supplier<String> splitUriPaths(final String uripath, final char c1){
		final AtomicInteger charindex=new AtomicInteger();
		return ()->{
			int pcount=0;
			final int i=charindex.get();
			if(i>=uripath.length()){
				return null;
			}
			for(int j=i;j<uripath.length();j++){
				final char c=uripath.charAt(j);
				switch(c){
				case '(':
					pcount++;
					break;
				case ')':
					pcount--;
					break;
				default:
					if(c==c1){
						if(pcount==0){
							charindex.set(j+1);
							return uripath.substring(i,j);
						}
					}
					break;
				}
			}
			charindex.set(uripath.length());
			return uripath.substring(i);
		};
	}
}
