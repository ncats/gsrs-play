package ix.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.core.util.TimeUtil;

public class TimeProfiler{
		private static final int CHUNK_SIZE=10;
	
		private Counter numberCount = new Counter();
		private Counter timeCount = new Counter();
		private Map<String,Long> startTimes = new HashMap<String,Long>();
		
		private Map<String,List<Double>> lastTimes = new HashMap<String,List<Double>>();
		
		private Counter lastChunkCount = new Counter();
		private Counter lastChunkTimeCount = new Counter();
		
		private int chunkSize=10;
		
		//private static final int CHUNK_SIZE=10;
    	public static class Counter{
        	private Map<String,Long> count= new LinkedHashMap<String,Long>();
        	public void addTo(String key, long c){
        		Long o=count.get(key);
        		if(o==null){
        			o=0l;
        		}
        		o=o+c;
        		count.put(key,o);
        	}
        	public void increment(String key){
        		addTo(key,1);
        	}
        	public long get(String key){
        		return count.get(key);
        	}
        	public Set<String> getKeys(){
        		return count.keySet();
        	}
        	public void reset(String key){
        		count.remove(key);
        	}
        }
    	
    	
    	public TimeProfiler(int bins){
    		chunkSize=bins;
    	}
    	
    	public boolean containsKey(Object okey){
    		String key = okey.toString();
    		return timeCount.count.containsKey(key);    		
    	}
    	
    	public void addTime(Object okey){
    		String key=okey.toString();
    		numberCount.increment(key);
    		lastChunkCount.increment(key);
    		startTimes.put(key, ix.core.util.TimeUtil.getCurrentTime(TimeUnit.NANOSECONDS));
    	}
    	
    	public void stopTime(Object okey){
    		String key = okey.toString();
    		if(!numberCount.count.containsKey(key))return;
    		long total=numberCount.get(key);
    		long chunktotal=lastChunkCount.get(key);
    		long time=ix.core.util.TimeUtil.getCurrentTime(TimeUnit.NANOSECONDS)-startTimes.get(key);
    		
    		timeCount.addTo(key, time);
    		lastChunkTimeCount.addTo(key, time);
    		
    		if(chunktotal>=total/chunkSize){
    			double avgTime=lastChunkTimeCount.get(key)/(0.0+chunktotal);
    			List<Double> ltimes=lastTimes.get(key);
	    		if(ltimes==null){
	    			ltimes= new ArrayList<Double>();
	    			lastTimes.put(key,ltimes);
	    		}
	    		
	    		ltimes.add(avgTime);
	    		if(ltimes.size()>chunkSize)
	    			normalizeTimes(ltimes, chunkSize);
	    		lastChunkCount.reset(key);
	    		lastChunkTimeCount.reset(key);
    		}
    		startTimes.remove(key);
    	}
    	
    	public double getAverageTime(Object okey, TimeUnit tu){
    		String s=okey.toString();
    		if(containsKey(s)){
    			long nanos=(timeCount.get(s));
        		double spec=(tu.convert(nanos, TimeUnit.NANOSECONDS)+0.0);
        		
    			return (spec/numberCount.get(s));
    		}
    		return -1;
    	}
    	public double getLastAverageTime(Object okey, TimeUnit tu){
    		String s=okey.toString();

    		if(!containsKey(s)){
    			return -1;
    		}
    		try{
    			long nanos=(lastChunkTimeCount.get(s));
        		double spec=(tu.convert(nanos, TimeUnit.NANOSECONDS)+0.0);
        		
				return (spec/(0.0+lastChunkCount.get(s)));
			}catch(Exception e){
				List<Double> ds=lastTimes.get(s);
				long nanos=ds.get(ds.size()-1).longValue();
				double spec=(tu.convert(nanos, TimeUnit.NANOSECONDS)+0.0);
				return spec;
			}
    	}
    	
    	private static void normalizeTimes(List<Double> times, int samples){
    		double[] mytimes=new double[samples];
    		for(int i=0;i<samples;i++){
    			mytimes[i]=interp(times,(i+0)/((double)samples));
    		}
    		times.clear();
    		for(int i=0;i<samples;i++){
    			times.add(mytimes[i]);
    		}
    	}
    	private static double interp(List<Double> times, double t){
    		double approx=t*(times.size());
    		
    		int low = Math.max(0,(int)Math.floor(approx));
    		int high = Math.min((int)Math.ceil(approx), times.size()-1);
    		
    		double additional = approx-low;
    		double d1=times.get(low);
    		double d2=times.get(high);
    		return additional*d2 + (1-additional)*d1;
    		
    	}
    	
    	public void printResult(String s){

			double average=getAverageTime(s,TimeUnit.MILLISECONDS);
			double lastAverage = getLastAverageTime(s,TimeUnit.MILLISECONDS);
			
			if(timeCount.count.containsKey(s)){
				StringBuilder sb = new StringBuilder();
				List<Double> times=new ArrayList<Double>(lastTimes.get(s));
				if(times.size()<this.chunkSize){
					normalizeTimes(times, this.chunkSize);
				}
				int i=0;
    			for(double avg:times){
    				if(i>0){
    					sb.append("," + avg/(1E6+0.0));
    				}
    				i++;
    			}
    			System.out.println(
    					s + "\t" + 
    					numberCount.get(s) + "\t" + 
    					(timeCount.get(s)/(1E6+0.0)) + "\t" +
    					average + sb.toString() + "\t" + lastAverage);
			}else{
				//System.out.println("NO key:" + s + " avg:" + average);
			}
    	}
    	
    	public void printResults(){
    		System.out.println("===========");
    		for(String s:numberCount.getKeys()){
    			printResult(s);
    		}
    		System.out.println("==========");
    	}
    	
    	private static TimeProfiler _instance;
    	
    	public static TimeProfiler getInstance(){
    		if(_instance==null){
    			_instance=new TimeProfiler(CHUNK_SIZE);
    		}
    		return _instance;
    	}
    	
    	public static void addGlobalTime(Object okey){
    		getInstance().addTime(okey);
    	}
    	public static void stopGlobalTime(Object okey){
    		getInstance().stopTime(okey);
    	}
    	
    	
    }