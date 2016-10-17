package ix.test.authentication;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ix.AbstractGinasClassServerTest;
import ix.test.server.RestSession;
import ix.test.util.RandomTextMaker;
import ix.test.util.TestUtil;
import ix.utils.Util;

@RunWith(Parameterized.class)
public class TrustHeaderAuthenticatorTest extends AbstractGinasClassServerTest{
	
	public static final Supplier<UserInfo> userSupplier = RandomTextMaker
							.getStringBasedThingSupplier(0, (s)->{
								return new UserInfo(s.get(),s.get() +"@"+s.get() +".com", s.get());
							});

	private static class UserInfo{
		private String username;
		private String email;
		private String password;
		public String getUsername() {
			return username;
		}
		public String getEmail() {
			return email;
		}
		public String getPassword() {
			return password;
		}
		
		public UserInfo(String username, String email, String password) {
			super();
			this.username = username;
			this.email = email;
			this.password = password;
		}
		
	}
	
	private static final String HEADER_NAME = "USERNAME_HEADER";
	private static final String HEADER_EMAIL = "EMAIL_HEADER";


	public static class TrustHeaderAuthenticatorTestConfig{
		String name;
		public boolean trustHeaders;
		public boolean allownonauthenticated;
		public boolean autoregister;
		public boolean autoregisteractive;
		
		
		private int expectedStatusNoHeader;
		private int expectedStatusWithHeaderNotCreated;
		private int expectedStatusWithHeaderAndCreated;

		
		TrustHeaderAuthenticatorTestConfig(String name){
			this.name=name;
		}
		public TrustHeaderAuthenticatorTestConfig trust(boolean trust){
			this.trustHeaders=trust;
			return this;
		}
		public TrustHeaderAuthenticatorTestConfig allownonauth(boolean allow){
			this.allownonauthenticated=allow;
			return this;
		}
		public TrustHeaderAuthenticatorTestConfig autoregister(boolean autoregister){
			this.autoregister=autoregister;
			return this;
		}
		public TrustHeaderAuthenticatorTestConfig autoregisteractive(boolean autoregisteractive){
			this.autoregisteractive=autoregisteractive;
			return this;
		}
		public TrustHeaderAuthenticatorTestConfig expectNoHeader(int status){
			this.setExpectedStatusNoHeader(status);
			return this;
		}
		public TrustHeaderAuthenticatorTestConfig expectWithHeaderUnregisteredUser(int status){
			this.setExpectedStatusWithHeaderNotCreated(status);
			return this;
		}
		public TrustHeaderAuthenticatorTestConfig expectWithHeaderRegisteredActiveUser(int status){
			this.setExpectedStatusWithHeaderAndCreated(status);
			return this;
		}
		
		public String getName(){
			if(name!=null){
				return name;
			}
			return "trust:" + trustHeaders + " & allowNonAuth:" + allownonauthenticated
					+" & autoregister:" + autoregister + " & autoregisteractive:" + autoregisteractive;
		}
		
		
		public Map<String, Object> getServerConfigMap(){
			return Util.MapBuilder
			.putNew("ix.authentication.trustheader", (Object) trustHeaders)
			.put("ix.authentication.usernameheader", HEADER_NAME)
			.put("ix.authentication.useremailheader", HEADER_EMAIL)
			.put("ix.authentication.allownonauthenticated", allownonauthenticated)
			.put("ix.authentication.autoregister", autoregister)
			.put("ix.authentication.autoregisteractive", this.autoregisteractive)
			.build();
		}
		int getExpectedStatusWithHeaderAndCreated() {
			return expectedStatusWithHeaderAndCreated;
		}
		void setExpectedStatusWithHeaderAndCreated(int expectedStatusWithHeaderAndCreated) {
			this.expectedStatusWithHeaderAndCreated = expectedStatusWithHeaderAndCreated;
		}
		int getExpectedStatusWithHeaderNotCreated() {
			return expectedStatusWithHeaderNotCreated;
		}
		void setExpectedStatusWithHeaderNotCreated(int expectedStatusWithHeaderNotCreated) {
			this.expectedStatusWithHeaderNotCreated = expectedStatusWithHeaderNotCreated;
		}
		int getExpectedStatusNoHeader() {
			return expectedStatusNoHeader;
		}
		void setExpectedStatusNoHeader(int expectedStatusNoHeader) {
			this.expectedStatusNoHeader = expectedStatusNoHeader;
		}
		
	}
	
	public static TrustHeaderAuthenticatorTestConfig maketest(String name){
		return new TrustHeaderAuthenticatorTestConfig(name);
	}
	public static TrustHeaderAuthenticatorTestConfig maketest(){
		return new TrustHeaderAuthenticatorTestConfig(null);
	}
	
	
	TrustHeaderAuthenticatorTestConfig config;
	
	
	public TrustHeaderAuthenticatorTest(String name, TrustHeaderAuthenticatorTestConfig config) {
		ts.modifyConfig(config.getServerConfigMap());
		ts.modifyConfig("ix.ginas.init.loadCV",false);
		ts.restart();
		this.config=config;
	}

	@Parameters(name = "{0}")
	static public Collection<Object[]> generateTestCases() {
		List<TrustHeaderAuthenticatorTestConfig> toRun = new ArrayList<>();
		
		//When we allow non-auth, but no means of logging in,
		//get 404 response for WHOAMI query
		TestUtil.allPermutations(2).stream().forEach(bs->{
		toRun.add(maketest().trust(false) 
							.allownonauth(true)
							.autoregister(bs.get(0))
							.autoregisteractive(bs.get(1))
							.expectNoHeader(404)
							.expectWithHeaderRegisteredActiveUser(404)
							.expectWithHeaderUnregisteredUser(404)
				);
		});
		//No auth should be possible with trust header set to false
		TestUtil.allPermutations(2).stream().forEach(bs->{
			toRun.add(maketest().trust(false)
					.allownonauth(false)
					.autoregister(bs.get(0))
					.autoregisteractive(bs.get(1))
					.expectNoHeader(401)
					.expectWithHeaderRegisteredActiveUser(401)
					.expectWithHeaderUnregisteredUser(401)
					);
		});
		
		toRun.add(maketest().trust(true)
				.allownonauth(false)
				.autoregister(true)
				.autoregisteractive(true)
				.expectNoHeader(401)
				.expectWithHeaderRegisteredActiveUser(200)
				.expectWithHeaderUnregisteredUser(200)
				);
		toRun.add(maketest().trust(true)
				.allownonauth(false)
				.autoregister(true)
				.autoregisteractive(false)
				.expectNoHeader(401)
				.expectWithHeaderRegisteredActiveUser(200)
				.expectWithHeaderUnregisteredUser(401)
				);
		
		TestUtil.allPermutations(1).stream().forEach(bs->{
		toRun.add(maketest().trust(true)
				.allownonauth(false)
				.autoregister(false)
				.autoregisteractive(bs.get(0))
				.expectNoHeader(401)
				.expectWithHeaderRegisteredActiveUser(200)
				.expectWithHeaderUnregisteredUser(401)
				);
		});
		toRun.add(maketest().trust(true)
				.allownonauth(true)
				.autoregister(true)
				.autoregisteractive(true)
				.expectNoHeader(404)
				.expectWithHeaderRegisteredActiveUser(200)
				.expectWithHeaderUnregisteredUser(200)
				);
		
		
		TestUtil.allPermutations(1).stream().forEach(bs->{
		toRun.add(maketest().trust(true)
				.allownonauth(true)
				.autoregister(false)
				.autoregisteractive(bs.get(0))
				.expectNoHeader(404)
				.expectWithHeaderRegisteredActiveUser(200)
				.expectWithHeaderUnregisteredUser(404)
				);
		});

		toRun.add(maketest().trust(true)
				.allownonauth(true)
				.autoregister(true)
				.autoregisteractive(false)
				.expectNoHeader(404)
				.expectWithHeaderRegisteredActiveUser(200)
				.expectWithHeaderUnregisteredUser(404)
				);
		
		return toRun.stream().map(f->new Object[]{f.getName(),f}).collect(Collectors.toList());
	}

	@Test
	public void testNoLoggedInUser() {
		try (RestSession rs = ts.notLoggedInRestSession()) {
			rs.clearAdditionalHeaders();
			assertEquals(config.getExpectedStatusNoHeader(), rs.whoAmI().getStatus());
		}
	}
	@Test
	public void testWithHeaderAndActiveUser() {
		
		UserInfo ui=userSupplier.get();
		ts.createAdmin(ui.getUsername(), ui.getPassword());
		
		try (RestSession rs = ts.notLoggedInRestSession()) {
			rs.setAdditionalHeader(HEADER_NAME, ui.getUsername());
			rs.setAdditionalHeader(HEADER_EMAIL, ui.getEmail());
			
			assertEquals(config.getExpectedStatusWithHeaderAndCreated(), rs.whoAmI().getStatus());
		}
	}
	@Test
	public void testWithHeaderForNonExistentUser() {
		
		UserInfo ui=userSupplier.get();
		try (RestSession rs = ts.notLoggedInRestSession()) {
			rs.setAdditionalHeader(HEADER_NAME, ui.getUsername());
			rs.setAdditionalHeader(HEADER_EMAIL, ui.getEmail());
			
			assertEquals(config.getExpectedStatusWithHeaderNotCreated(), rs.whoAmI().getStatus());
		}
	}

}
