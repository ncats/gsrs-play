package ix.test.util.process;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class DifferentPortExecutorService extends ThreadPoolExecutor {


    private final BlockingQueue<Integer> freePorts;

    public DifferentPortExecutorService( int beginPort, int endPort, int maxConcurrency){
        super(maxConcurrency, maxConcurrency, 20L, TimeUnit.MINUTES, new LinkedBlockingDeque<>());
        freePorts = new ArrayBlockingQueue<Integer>(endPort-beginPort +1);
        for(int i= beginPort; i<= endPort; i++){
            freePorts.add(Integer.valueOf(i));
        }
    }

    public void add(ProcessBuilder builder){
        final Integer usedPort[] = new Integer[1];
        ProcessJob job = new ProcessJob(builder, b -> {
                        List<String> command = b.command();
                        try {
                            usedPort[0] = freePorts.take();
                File tmpDir = new File("tmp_"+usedPort[0]);
                tmpDir.mkdirs();

                            command.add("-Dix.ginas.testserver.defaultPort=" + usedPort[0]);
                            command.add("-Dix.home=multiThreadRun_" + usedPort[0]);
                command.add("-Djava.io.tmpdir=" + tmpDir.getAbsolutePath());


                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    },
                ()-> {
                    try{
                            freePorts.put(usedPort[0]);
                        }catch(Exception e) {
                        e.printStackTrace();
                    }}
                );
        this.submit(job);
    }


}
