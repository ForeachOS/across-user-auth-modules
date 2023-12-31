/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.modules.oauth2.services;

import com.foreach.across.modules.oauth2.business.OAuth2Client;
import com.foreach.across.modules.spring.security.AuthenticationUtils;
import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipalId;
import com.foreach.across.modules.spring.security.infrastructure.services.SecurityPrincipalService;
import com.foreach.across.modules.user.business.Permission;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.business.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.SerializationUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestOAuth2JdbcTokenStore.Config.class)
@DirtiesContext
public class TestOAuth2JdbcTokenStore
{
	@MockBean
	private DataSource dataSource;
	@MockBean
	private UserDetailsService userDetailsService;
	@MockBean
	private ClientDetailsService clientDetailsService;
	@MockBean
	private SecurityPrincipalService securityPrincipalService;
	@Autowired
	private OAuth2StatelessJdbcTokenStore oAuth2StatelessJdbcTokenStore;
	@Autowired
	private List<OAuth2AuthenticationSerializer> serializers;
	@Autowired
	private DummyOAuth2AuthenticationSerializer dummyOAuth2AuthenticationSerializer;

	@BeforeEach
	public void resetMocks() {
		for ( OAuth2AuthenticationSerializer serializer : serializers ) {
			reset( serializer );
		}
		reset( dataSource, userDetailsService, clientDetailsService, oAuth2StatelessJdbcTokenStore );
	}

	@Test
	public void testClientSerialization() {
		OAuth2Request request = new OAuth2Request(
				Collections.singletonMap( "keyParam", "keyValue" ),
				"testClientId",
				Collections.singleton( mock( GrantedAuthority.class ) ),
				true,
				Collections.singleton( "fullScope" ),
				Collections.singleton( "resourceId" ),
				"redirectUrl",
				Collections.singleton( "responseTypes" ),
				Collections.<String, Serializable>singletonMap( "extensionKey", new ArrayList() )
		);

		OAuth2Client clientDetails = new OAuth2Client();

		Set<Role> roles = new HashSet<>();
		Role thirdRole = new Role( "role three" );
		thirdRole.addPermission( new Permission( "authority two" ) );

		roles.add( new Role( "role one" ) );
		roles.add( new Role( "role two" ) );
		roles.add( thirdRole );
		clientDetails.setRoles( roles );

		clientDetails.setId( 516L );
		Set<String> authorityTypes = new HashSet<>();
		authorityTypes.add( "authority one" );
		authorityTypes.add( "authority two" );
		clientDetails.setAuthorizedGrantTypes( authorityTypes );

		when( clientDetailsService.loadClientByClientId( "testClientId" ) ).thenReturn( clientDetails );

		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication( request, null );
		byte[] bytes = oAuth2StatelessJdbcTokenStore.serializeAuthentication( oAuth2Authentication );

		assertNotNull( bytes );
		assertTrue( bytes.length > 10, "should find some bytes in this object" );

		Object o = SerializationUtils.deserialize( bytes );
		assertNotNull( o );
		assertTrue( o instanceof AuthenticationSerializerObject,
		            "Serializer should be of type ClientOAuth2AuthenticationSerializer" );

		OAuth2Authentication storedAuthentication = oAuth2StatelessJdbcTokenStore.deserializeAuthentication( bytes );

		assertNotNull( storedAuthentication );
		assertEquals( 4, storedAuthentication.getAuthorities().size() );

		verify( oAuth2StatelessJdbcTokenStore ).serializeAuthentication( eq( oAuth2Authentication ) );
		verify( userDetailsService, never() ).loadUserByUsername( anyString() );
		verify( clientDetailsService ).loadClientByClientId( "testClientId" );

		assertEquals( Collections.singletonMap( "keyParam", "keyValue" ),
		              storedAuthentication.getOAuth2Request().getRequestParameters() );
		assertEquals( Collections.singleton( "fullScope" ), storedAuthentication.getOAuth2Request().getScope() );
		assertEquals( true, storedAuthentication.getOAuth2Request().isApproved() );
		assertEquals( Collections.singleton( "resourceId" ), storedAuthentication.getOAuth2Request().getResourceIds() );
		assertEquals( "redirectUrl", storedAuthentication.getOAuth2Request().getRedirectUri() );
		assertEquals( Collections.singleton( "responseTypes" ),
		              storedAuthentication.getOAuth2Request().getResponseTypes() );
		assertEquals( Collections.<String, Serializable>singletonMap( "extensionKey", new ArrayList() ),
		              storedAuthentication.getOAuth2Request().getExtensions() );

		Collection<? extends GrantedAuthority> storedAuthorities =
				storedAuthentication.getOAuth2Request().getAuthorities();

		assertEquals( 4, storedAuthorities.size() );
		assertTrue( AuthenticationUtils.hasAuthority( storedAuthorities, "ROLE_role three" ) );
		assertTrue( AuthenticationUtils.hasAuthority( storedAuthorities, "ROLE_role one" ) );
		assertTrue( AuthenticationUtils.hasAuthority( storedAuthorities, "ROLE_role two" ) );
		assertTrue( AuthenticationUtils.hasAuthority( storedAuthorities, "authority two" ) );
	}

	@Test
	public void testUserSerialization() {
		OAuth2Client client = new OAuth2Client();
		client.setRoles( Sets.newSet( new Role( "adminClient" ), new Role( "manager client" ) ) );

		OAuth2Request request = new OAuth2Request( Collections.singletonMap( "userkeyParam", "userkeyValue" ),
		                                           "testClientId", client.getAuthorities(), true,
		                                           Collections.singleton( "fullScopeUser" ),
		                                           Collections.singleton( "resourceIdUser" ), "redirectUrlUser",
		                                           Collections.singleton(
				                                           "responseTypesForUser" ),
		                                           Collections.<String, Serializable>singletonMap(
				                                           "extensionKeysForUser", new ArrayList() )
		);

		User user = new User();
		user.setUsername( "testusername" );

		Set<Role> roles = new HashSet<>();
		Role userRole = new Role( "role three" );

		Set<Permission> permissions = new HashSet<>();
		permissions.add( new Permission( "permission 1" ) );
		permissions.add( new Permission( "permission 2" ) );
		userRole.setPermissions( permissions );

		roles.add( userRole );
		user.setRoles( roles );

		user.setId( 777L );

		when( clientDetailsService.loadClientByClientId( "testClientId" ) ).thenReturn( client );
		when( securityPrincipalService.getPrincipalById( SecurityPrincipalId.of( "testusername" ) ) ).thenReturn( Optional.of( user ) );

		Authentication userAuthentication = new UsernamePasswordAuthenticationToken( SecurityPrincipalId.of( user.getUsername() ), user.getPassword() );
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication( request, userAuthentication );
		byte[] bytes = oAuth2StatelessJdbcTokenStore.serializeAuthentication( oAuth2Authentication );

		assertNotNull( bytes );
		assertTrue( bytes.length > 10, "should find some bytes in this object" );

		Object o = SerializationUtils.deserialize( bytes );
		assertNotNull( o );
		assertTrue( o instanceof AuthenticationSerializerObject,
		            "Serializer should be of type UserDetailsOAuth2AuthenticationSerializer" );

		OAuth2Authentication storedAuthentication = oAuth2StatelessJdbcTokenStore.deserializeAuthentication( bytes );

		assertNotNull( storedAuthentication );

		verify( oAuth2StatelessJdbcTokenStore ).serializeAuthentication( eq( oAuth2Authentication ) );
		verify( securityPrincipalService ).getPrincipalById( SecurityPrincipalId.of( "testusername" ) );
		verify( clientDetailsService ).loadClientByClientId( "testClientId" );

		assertEquals( Collections.singletonMap( "userkeyParam", "userkeyValue" ),
		              storedAuthentication.getOAuth2Request().getRequestParameters() );
		assertEquals( Collections.singleton( "fullScopeUser" ), storedAuthentication.getOAuth2Request().getScope() );
		assertEquals( true, storedAuthentication.getOAuth2Request().isApproved() );
		assertEquals( Collections.singleton( "resourceIdUser" ),
		              storedAuthentication.getOAuth2Request().getResourceIds() );
		assertEquals( "redirectUrlUser", storedAuthentication.getOAuth2Request().getRedirectUri() );
		assertEquals( Collections.singleton( "responseTypesForUser" ),
		              storedAuthentication.getOAuth2Request().getResponseTypes() );
		assertEquals( Collections.<String, Serializable>singletonMap( "extensionKeysForUser", new ArrayList() ),
		              storedAuthentication.getOAuth2Request().getExtensions() );

		Collection<GrantedAuthority> storedAuthorities = storedAuthentication.getAuthorities();
		assertEquals( 3, storedAuthorities.size() );
		assertTrue( AuthenticationUtils.hasAuthority( storedAuthorities, "ROLE_role three" ) );
		assertTrue( AuthenticationUtils.hasAuthority( storedAuthorities, "permission 1" ) );
		assertTrue( AuthenticationUtils.hasAuthority( storedAuthorities, "permission 2" ) );

		Collection<? extends GrantedAuthority> storedOauthRequestAuthorities =
				storedAuthentication.getOAuth2Request().getAuthorities();
		assertEquals( 2, storedOauthRequestAuthorities.size() );
		assertTrue( AuthenticationUtils.hasAuthority( storedOauthRequestAuthorities, "ROLE_manager client" ) );
		assertTrue( AuthenticationUtils.hasAuthority( storedOauthRequestAuthorities, "ROLE_adminClient" ) );
	}

	@Test
	public void testSerializationWithNullPricipallFallsBackToNormalSerialization() {
		OAuth2Request request = new OAuth2Request( Collections.<String, String>emptyMap(),
		                                           null, Collections.<GrantedAuthority>emptyList(), true,
		                                           Collections.singleton( "fullScope" ),
		                                           Collections.singleton( "resourceId" ), "",
		                                           Collections.<String>emptySet(),
		                                           Collections.<String, Serializable>emptyMap() );

		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication( request, null );
		byte[] bytes = oAuth2StatelessJdbcTokenStore.serializeAuthentication( oAuth2Authentication );
		assertNotNull( bytes );
		assertTrue( bytes.length > 10, "should find some bytes in this object" );

		OAuth2Authentication storedAuthentication = oAuth2StatelessJdbcTokenStore.deserializeAuthentication( bytes );
		assertNotNull( storedAuthentication );
		assertEquals( null, storedAuthentication.getPrincipal() );
		assertEquals( "fullScope", storedAuthentication.getOAuth2Request().getScope().iterator().next() );
	}

	@Test
	public void testSerializationWithAnyOtherPricipallFallsBackToNormalSerialization() {
		OAuth2Request request = new OAuth2Request( Collections.<String, String>emptyMap(),
		                                           null, Collections.<GrantedAuthority>emptyList(), true,
		                                           Collections.singleton( "fullScope" ),
		                                           Collections.singleton( "resourceId" ), "",
		                                           Collections.<String>emptySet(),
		                                           Collections.<String, Serializable>emptyMap() );

		String[] principal = new String[] { "weird", "principal", "object" };
		Authentication userAuthentication = new PreAuthenticatedAuthenticationToken( principal, null );
		OAuth2Authentication oAuth2Authentication = new OAuth2Authentication( request, userAuthentication );
		byte[] bytes = oAuth2StatelessJdbcTokenStore.serializeAuthentication( oAuth2Authentication );
		assertNotNull( bytes );
		assertTrue( bytes.length > 10, "should find some bytes in this object" );

		OAuth2Authentication storedAuthentication = oAuth2StatelessJdbcTokenStore.deserializeAuthentication( bytes );
		assertNotNull( storedAuthentication );
		String[] storedPrincipal = (String[]) storedAuthentication.getPrincipal();
		assertEquals( 3, storedPrincipal.length );
		assertArrayEquals( principal, storedPrincipal );
		assertEquals( "fullScope", storedAuthentication.getOAuth2Request().getScope().iterator().next() );

		assertEquals( 3, serializers.size() );
		for ( OAuth2AuthenticationSerializer serializer : serializers ) {
			verify( serializer, never() ).serialize( any( OAuth2Authentication.class ) );
		}
	}

	@Test
	public void testInvalidSerializedObject() {
		assertThrows( OAuth2AuthenticationSerializer.SerializationException.class, () -> {
			byte[] unknownObject = SerializationUtils.serialize( new ArrayList<>() );
			OAuth2Authentication invalidByteArray = oAuth2StatelessJdbcTokenStore.deserializeAuthentication(
					unknownObject );
			assertNull( invalidByteArray );
		} );
	}

	@Test
	public void testCustomSerializerThrowsErrorWhenDeserializingUnknownObject() {
		assertThrows( OAuth2AuthenticationSerializer.SerializationException.class, () -> {
			AuthenticationSerializerObject<ArrayList<Object>> authenticationSerializerObject =
					new AuthenticationSerializerObject<>( DummyOAuth2AuthenticationSerializer.class.getCanonicalName(),
					                                      new ArrayList<>(),
					                                      mock( OAuth2Request.class ) );
			byte[] serializedDummyObject = SerializationUtils.serialize( authenticationSerializerObject );
			OAuth2Authentication invalidByteArray = oAuth2StatelessJdbcTokenStore.deserializeAuthentication(
					serializedDummyObject );
			assertNull( invalidByteArray );
		} );
	}

	@Test
	public void testCustomSerializerThrowsErrorWhenSerializingUnknownObject() {
		assertThrows( OAuth2AuthenticationSerializer.SerializationException.class, () -> {
			OAuth2Request request = new OAuth2Request( Collections.<String, String>emptyMap(),
			                                           null, Collections.<GrantedAuthority>emptyList(), true,
			                                           Collections.singleton( "fullScope" ),
			                                           Collections.singleton( "resourceId" ), "",
			                                           Collections.<String>emptySet(),
			                                           Collections.<String, Serializable>emptyMap() );

			Authentication userAuthentication = mock( Authentication.class );
			OAuth2Authentication oAuth2Authentication = new OAuth2Authentication( request, userAuthentication );
			byte[] bytes = dummyOAuth2AuthenticationSerializer.serialize( oAuth2Authentication );

			assertNull( bytes );
		} );

	}

	@Configuration
	static class Config
	{
		@Bean
		public OAuth2StatelessJdbcTokenStore oAuth2StatelessJdbcTokenStore( DataSource dataSource ) {
			return spy( new OAuth2StatelessJdbcTokenStore( dataSource ) );
		}

		@Bean
		public ClientOAuth2AuthenticationSerializer clientOAuth2AuthenticationSerializer() {
			return spy( new ClientOAuth2AuthenticationSerializer() );
		}

		@Bean
		public SecurityPrincipalIdOAuth2AuthenticationSerializer userOAuth2AuthenticationSerializer() {
			return spy( new SecurityPrincipalIdOAuth2AuthenticationSerializer() );
		}

		@Bean
		public DummyOAuth2AuthenticationSerializer dummyOAuth2AuthenticationSerializer() {
			return spy( new DummyOAuth2AuthenticationSerializer() );
		}
	}

	private static class DummyOAuth2AuthenticationSerializer extends OAuth2AuthenticationSerializer
	{

		@Override
		protected byte[] serializePrincipal( Object object, OAuth2Request oAuth2Request ) {
			return null;
		}

		@Override
		public OAuth2Authentication deserialize( AuthenticationSerializerObject serializerObject ) {
			return null;
		}

		@Override
		public boolean canSerialize( OAuth2Authentication authentication ) {
			return false;
		}
	}
}
