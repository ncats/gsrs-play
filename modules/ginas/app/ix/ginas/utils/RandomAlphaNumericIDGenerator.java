package ix.ginas.utils;

import java.util.HashSet;
import java.util.Set;

public abstract class RandomAlphaNumericIDGenerator extends IDGenerator<String>{
	private char[] alphabet;
	private int len;
	private int idLen=9;
	private boolean check=false;
	Set<String> recentlyGenerated = new HashSet<String>();
	
	public RandomAlphaNumericIDGenerator(char[] alphabet, int len, boolean check){
		idLen=len;
		this.check=check;
		this.alphabet=alphabet;
		len=this.alphabet.length;	
	}
	
	
	@Override
	public synchronized String generateID() {
		int sum=0;		
		int totalLength=idLen;
		if(check)totalLength++;
		char[] retid= new char[totalLength];
		
		for(int i=0;i<idLen;i++){
			int k=(int) (Math.random()*len);
			retid[i]=alphabet[k];
			sum+=k;
		}
		if(check){
			int chk=sum%len;
			retid[idLen]=alphabet[chk];
		}
		
		String s=new String(retid);
		if(recentlyGenerated.contains(s)){
			s=generateID();
		}
		
		recentlyGenerated.add(s);
		while(!allowID(s)){
			s=generateID();
		}
		return s;
	}
	/**
	 * Does additional check to ensure that the id is not a duplicate,
	 * and / or does not break additional rules. If this function returns false,
	 * another random ID will be generated.
	 * @param s
	 * @return
	 */
	public abstract boolean allowID(String s);


	public int charValue(char c){
		for(int k=0;k<len;k++){
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
			int chk=sum%len;
			if(chk==charValue(v.charAt(idLen))){
				return true;
			}else{
				return false;
			}
		}
		return true;
	}

}