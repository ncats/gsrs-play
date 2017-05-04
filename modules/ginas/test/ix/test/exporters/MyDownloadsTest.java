package ix.test.exporters;

import ix.AbstractGinasServerTest;
import ix.ginas.exporters.ExportMetaData;
import ix.test.server.BrowserSession;
import ix.test.server.MyDownloadsAPI;
import ix.test.server.RestSession;
import ix.test.server.SubstanceLoader;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.List;
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
             BufferedReader reader = new BufferedReader(new InputStreamReader(browserSession.newSubstanceSearcher().all().export("csv")))
        ) {
            List<String> originalExport = reader.lines().collect(Collectors.toList());

            assertEquals(91, originalExport.size()); // 90 records + 1 line of header

            MyDownloadsAPI api =ts.newRestSession(browserSession.getUser()).newDownloadAPI();

            List<ExportMetaData> metaData = api.getAllDownloads();
            assertEquals(1, metaData.size());

            ExportMetaData actual = metaData.get(0);

            assertEquals("csv", actual.extension);
            assertEquals(90, actual.getNumRecords());
            try(BufferedReader reader2 = new BufferedReader( new InputStreamReader(browserSession.get(actual.getDownloadUrl()).getBodyAsStream()))){
                List<String> reDownloadedExport = reader2.lines().collect(Collectors.toList());

                assertEquals(originalExport, reDownloadedExport);
            }
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
