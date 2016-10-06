package ix.test.plugin;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import ix.test.server.GinasTestServer;

public class ConsoleFilterPluginTest {
	@Rule
    public GinasTestServer ts = new GinasTestServer(9001);
	
	@Test
	public void testConsoleLocalThreadFilterOnlyPrintsLocalThreadOutput() throws Exception {
		final String TO_OUTPUT = "I See stderr";
		Runnable r=()->{
			System.err.println(TO_OUTPUT);
		};
		
		ix.core.plugins.ConsoleFilterPlugin.runWithSwallowedStdErr(r,s->{
			System.out.println("Swallowed:" + s);
			assertEquals(TO_OUTPUT,s);
		});
		r.run();
	}
	
	@Test
	public void testConsoleClassFilterSwallowsStackTracePrint() throws Exception {
		Set<String> linesExpected = new HashSet<String>();
		Set<String> linesFound = new HashSet<String>();
		
		Runnable r=()->{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream psForAnalysis = new PrintStream(baos);
			List<PrintStream> pstreams = new ArrayList<PrintStream>();
			pstreams.add(psForAnalysis);
			pstreams.add(System.err);
			
			for(PrintStream ps:pstreams){
				new Throwable().printStackTrace(ps);
			}
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())))) {
				buffer.lines().forEach(l->linesExpected.add(l));
		    }catch(Exception e){
		    	
		    }
		};
		
		ix.core.plugins.ConsoleFilterPlugin.runWithSwallowedStdErrFor(r,".*ConsoleFilterPluginTest.*",s->{
			linesFound.add(s.toString());			
		});
		linesExpected.stream().filter(l->!linesFound.contains(l)).forEach(l->{
			System.err.println("Missing:" + l);
		});
		assertEquals(linesExpected,linesFound);
	}

	
	
}
