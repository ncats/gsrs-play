package ix.test.exporters;

import ix.AbstractGinasServerTest;
import ix.core.util.RunOnly;
import ix.ginas.exporters.ExportMetaData;
import ix.ginas.exporters.ExportProcessFactory;
import ix.test.server.BrowserSession;
import ix.test.server.MyDownloadsAPI;
import ix.test.server.RestSession;
import ix.test.server.SubstanceLoader;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by katzelda on 5/2/17.
 */
public class MyDownloadsTest extends AbstractGinasServerTest{

    @Test
    public void noExportsShouldMeanEmptyDownloads() throws IOException{
        try(RestSession restSession = ts.newRestSession(ts.getFakeUser1())){

            MyDownloadsAPI api = restSession.newDownloadAPI();

            assertTrue(api.getAllDownloads().isEmpty());
        }
    }


    @Test
    public void createdExportIsSavedInMyDownloads() throws IOException {
        try (BrowserSession browserSession = loadRep90();
             BufferedReader reader = new BufferedReader(new InputStreamReader(browserSession.newSubstanceSearcher().all().newExportRequest("csv").setPublicOnly(false).getInputStream()))
        ) {
            List<String> originalExport = reader.lines().collect(Collectors.toList());

            assertEquals(91, originalExport.size()); // 90 records + 1 line of header

            MyDownloadsAPI api =ts.newRestSession(browserSession.getUser()).newDownloadAPI();

            List<ExportMetaData> metaData = api.getAllDownloads();
            assertEquals(1, metaData.size());

            ExportMetaData actual = metaData.get(0);

            assertEquals("csv", actual.extension);
            assertEquals(90, actual.getNumRecords());
            File userRoot = ts.getUserExportDir(browserSession.getUser());
            try(BufferedReader reader2 = new BufferedReader( new FileReader(new File(userRoot, actual.filename)))){
                List<String> reDownloadedExport = reader2.lines().collect(Collectors.toList());

                assertEquals(originalExport, reDownloadedExport);
            }
        }
    }

    private List<String> exportTo(BrowserSession session, String format) throws IOException{
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(session.newSubstanceSearcher().all().newExportRequest("csv").setPublicOnly(false).getInputStream(true)))){
            return reader.lines().collect(Collectors.toList());
        }
    }


    @Test
    public void willReusePreviousDownloadIfAskedTo() throws IOException{
        try (BrowserSession browserSession = loadRep90();
             RestSession restSession = ts.newRestSession(browserSession.getUser());

        ){
            List<String> lines = exportTo(browserSession, "csv");
            MyDownloadsAPI dlApi = restSession.newDownloadAPI();

            File exportDir = dlApi.getExportDir();
            File[] files = exportDir.listFiles( (dir, name)-> name.endsWith(".csv"));
            assertEquals(1, files.length);

            File actualCsv = files[0];
            long lastModifiedDate = actualCsv.lastModified();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(browserSession.newSubstanceSearcher().all().newExportRequest("csv").getInputStream(false)))){
                List<String> redownloadLines = reader.lines().collect(Collectors.toList());

                assertEquals(lines, redownloadLines);
            }

            //check again we still only have 1 csv
            File[] files2 = exportDir.listFiles( (dir, name)-> name.endsWith(".csv"));
            assertEquals(1, files2.length);
            assertEquals(lastModifiedDate, files2[0].lastModified());

        }


    }

    @Test
    @RunOnly
    public void searchingSameThingAgainWillMakeNewFile() throws IOException{
        try (BrowserSession browserSession = loadRep90();
             RestSession restSession = ts.newRestSession(browserSession.getUser());

        ){
            List<String> lines = exportTo(browserSession, "csv");
            MyDownloadsAPI dlApi = restSession.newDownloadAPI();

            File exportDir = dlApi.getExportDir();
            File[] files = exportDir.listFiles( (dir, name)-> name.endsWith(".csv"));
            assertEquals(1, files.length);

            File actualCsv = files[0];
            long lastModifiedDate = actualCsv.lastModified();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(browserSession.newSubstanceSearcher().all()
                            .newExportRequest("csv").setPublicOnly(false).getInputStream(true)))){
                List<String> redownloadLines = reader.lines().collect(Collectors.toList());
                //redownloaded lines might be in different order
                assertEquals(lines, redownloadLines);
            }

            //check again we still only have 1 csv
            File[] files2 = exportDir.listFiles( (dir, name)-> name.endsWith(".csv"));

            assertEquals(2, files2.length);
            if(files2[0].lastModified() == lastModifiedDate){
                assertTrue(files2[1].lastModified() > lastModifiedDate);
            }else{
                assertEquals(lastModifiedDate, files2[1].lastModified());
            }
//            assertEquals(lastModifiedDate, files2[0].lastModified());

        }


    }

    @Test
    public void cantSeeOtherUsersDownloadsInMyDownloads() throws IOException{
        createdExportIsSavedInMyDownloads();
        try(RestSession restSession = ts.newRestSession(ts.getFakeUser1())){

            MyDownloadsAPI api = restSession.newDownloadAPI();

            assertTrue(api.getAllDownloads().isEmpty());
        }

    }

    private BrowserSession loadRep90() throws IOException{
        BrowserSession browserSession = ts.newBrowserSession(ts.getAdmin());
        SubstanceLoader loader = new SubstanceLoader(browserSession);
            File f = new File("test/testdumps/rep90.ginas");
            loader.loadJson(f);
        return browserSession;
    }
}
