package ix.test.authentication;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import ix.AbstractGinasServerTest;
import ix.core.models.Principal;
import ix.test.server.BrowserSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by katzelda on 3/6/17.
 */
public abstract class AbstractAddUserTest extends AbstractGinasServerTest {
    protected static final  List<UserResult> DEFAULT_USERS = Arrays.asList( new UserResult("admin", true,null),
            new UserResult("fakeuser1", true,null),
            new UserResult("fakeuser2", true,null),
            new UserResult("fakeuser3", true,null),
            new UserResult("fakeAdmin1", true,null));
    BrowserSession session;

    @Before
    public void createBrowserSession(){
        session = ts.newBrowserSession(ts.getAdmin());


    }

    @After
    public void tearDown(){
        session.close();
    }


    protected List<UserResult> queryAllCurrentUsers() throws IOException {
        return queryAllCurrentUsers(session);
    }
    protected List<UserResult> queryAllCurrentUsers(BrowserSession aSession) throws IOException {
        HtmlPage response = aSession.submit(aSession.newGetRequest("ginas/app/admin/users").get()).getPage();

        return parseResponse(response);
    }

    protected Principal createPrincipalWithoutUserProfile(String username){
        Principal p = new Principal();
        p.username = username;
        p.save();
        return p;
    }

    protected List<UserResult> parseResponse(HtmlPage responseHtml){
        /*
        <div class="col-md-9">
          <table class="table table-striped">
            <thead>
              <tr>
                <td>
                  <b>
                    User Name
                  </b>
                </td>
                <td>
                  <b>
                    Active
                  </b>
                </td>
                <td>
                  <b>
                    Email
                  </b>
                </td>
              </tr>
            </thead>
            <tbody>
              <form class="ng-pristine ng-valid">
              </form>
              <tr id="1">
                <td>
                  <a href="/ginas/app/admin/user/1" target="_self">
                     admin
                  </a>
                </td>
                <td>
                  true

         */
        List<UserResult> list = new ArrayList<>();
        for(Object a :responseHtml.getByXPath("//td/a[starts-with(@href, '/ginas/app/admin/user')]")){
            //these ar the anchor tags
            HtmlAnchor anchor = (HtmlAnchor)a;
            HtmlTableRow tr = (HtmlTableRow) anchor.getParentNode() // td
                            .getParentNode(); //tr

           // System.out.println("found tr = " + tr);

            String username = tr.getCell(0).asText();
            boolean isActive = Boolean.parseBoolean(tr.getCell(1).asText());
            String email = tr.getCell(2).asText().trim();
            if(email.isEmpty()){
                email = null;
            }

            list.add(new UserResult(username, isActive, email));
        }

        return list;
    }
}
