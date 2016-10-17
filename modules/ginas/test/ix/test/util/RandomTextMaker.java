package ix.test.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

import ix.core.util.CachedSupplier;

/**
 * Simple class to make a deterministic
 * stream of strings which approximately mimic 
 * standard English characters in frequency 
 * 
 * @author peryeata
 *
 */
public class RandomTextMaker {

	// Random text to get some usernames
	private static String generator = "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" 
			+ "PAAA=";
	private static CachedSupplier<String[]> snips = CachedSupplier.of(()->{
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(generator));
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new GZIPInputStream(bais)))) {
				return buffer
						.lines()
						.map(s -> s.split("[^A-Za-z]"))
						.flatMap(st -> Arrays.stream(st))
						.map(String::toUpperCase)
						.distinct()
						.toArray(i -> new String[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(c->c+"").toArray(i -> new String[i]);
	});

	private static String encode(int i, String[] alph) {
		int j = i;
		String ret = "";
		while (j > 0) {
			ret = (alph[j % alph.length]) + ret;
			j /= alph.length;
		}
		if (ret.length() == 0) {
			return "" + alph[0];
		}
		return ret;
	}

	
	public static Supplier<String> getNewStringSupplier(int seed){
		return new StringMaker(seed);
	}
	
	public static <T> Supplier<T> getStringBasedThingSupplier(int seed, Function<Supplier<String>, T> maker){
		StringMaker sm = new StringMaker(seed);
		return ()->{
			return maker.apply(sm);
		};
	}
	
	private static class StringMaker implements Supplier<String>{
		private static AtomicInteger ai = new AtomicInteger();
		private int[] map= new int[snips.get().length];
		StringMaker(int seed){
			List<Integer> ilist= IntStream.range(0, map.length).mapToObj(i->i).collect(Collectors.toList());
			Collections.shuffle(ilist, new Random(seed));
			map = ilist.stream().mapToInt(i->i).toArray();
		}
		
		@Override
		public String get() {
			int i = ai.incrementAndGet();
			return encode(map[i], snips.get());
		}
		
	}
	
}
