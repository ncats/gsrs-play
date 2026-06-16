package ix.core.utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public abstract class RandomAlphaNumericIDGenerator<T> extends AbstractNoDependencyIDGenerator<T, String> {
	private char[] alphabet;
	private int idLen;
	private boolean check;
	Set<String> recentlyGenerated = new HashSet<>();

	private final Random random;
	public RandomAlphaNumericIDGenerator(char[] alphabet, int len, boolean check){
		this(new Random(), alphabet, len, check);
	}
	public RandomAlphaNumericIDGenerator(Random random, char[] alphabet, int len, boolean check){
		idLen=len;
		this.check=check;
		this.alphabet=alphabet;
		this.random = Objects.requireNonNull(random);
	}

	public boolean addCheckDigit() {
		return check;
	}

	@Override
	public synchronized String generateID() {
		String s = generateRandomID();
		String decoratedId = decorateRandomID(s);
		if(recentlyGenerated.add(decoratedId)){
			if(allowID(s, decoratedId)){
				return decoratedId;
			}
		}
		//if we get here something was bad
		return generateID();
	}

	protected String decorateRandomID(String randomId){
		return randomId;
	}

	protected String generateRandomID() {
		int sum=0;
		int totalLength=idLen;

		if(check)totalLength++;
		char[] retid= new char[totalLength];

		for(int i=0;i<idLen;i++){
			int k=random.nextInt(alphabet.length);
			retid[i]=alphabet[k];
			sum+=k;
		}
		if(check){
			int chk=sum%alphabet.length;
			retid[idLen]=alphabet[chk];
		}

		return new String(retid);
	}

	/**
	 * Does additional check to ensure that the id is not a duplicate,
	 * and / or does not break additional rules. If this function returns false,
	 * another random ID will be generated.
	 * @param randomPart
	 * @param fullId
	 * @return
	 */
	public abstract boolean allowID(String randomPart, String fullId);


	public int charValue(char c){
		for(int k=0;k<alphabet.length;k++){
			if(c==alphabet[k]){
				return k;
			}
		}
		return -1;
	}
	public int getLength(){
		if(check){
			return idLen + 1;
		}else{
			return idLen;
		}
	}
	
	public boolean valid(String v){
		if(v.length()!=getLength()){
			return false;
		}
		
		int sum=0;	
		for(int i=0;i<idLen;i++){
			char c=v.charAt(i);
			int t=charValue(c);
			if(t==-1)return false;
			sum+=t;
		}
		if(check){
			int chk=sum%alphabet.length;
			if(chk==charValue(v.charAt(idLen))){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}
}