package ix.test.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import ix.AbstractGinasClassServerTest;
import ix.core.UserFetcher;
import ix.core.models.Principal;

public class UserFetcherTest extends AbstractGinasClassServerTest{

	@Before
	public void resetUser(){
		UserFetcher.setLocalThreadUser(null);
	}
	
	
	@Test
	public void ensureExplicitlySetUserIsTheUserSelected(){
		Principal p2 = new Principal();
		p2.username="ATESTUSER";
		UserFetcher.setLocalThreadUser(p2);
		assertEquals(p2,UserFetcher.getActingUser());
		assertEquals(p2,UserFetcher.getActingUser(false));
		assertEquals(p2,UserFetcher.getActingUser(true));
	}
	
	@Test
	public void ensureNotLoggedInUserIsGuestUserByDefault(){
		//Needs server for this one only
		assertEquals(UserFetcher.getDefaultUsername(),UserFetcher.getActingUser(true).username);
	}
	
	@Test
	public void ensureNotLoggedInUserIsNullIfGuestNotAllowed(){
		assertNull(UserFetcher.getActingUser(false));
	}
	
	@Test
	public void ensureExplicitlySetUserIsNotTheUserSelectedInAnotherThread() throws InterruptedException{
		Principal p2 = new Principal();
		p2.username="ATESTUSER";
		UserFetcher.setLocalThreadUser(p2);
		CountDownLatch cdl = new CountDownLatch(1);
		new Thread(()->{
			Principal p3 = new Principal();
			p3.username="SOME_OTHER_USER";
			UserFetcher.setLocalThreadUser(p3);
			cdl.countDown();
		}).start();;
		cdl.await();
		assertEquals(p2,UserFetcher.getActingUser());
		assertEquals(p2,UserFetcher.getActingUser(false));
		assertEquals(p2,UserFetcher.getActingUser(true));
	}
	
}
