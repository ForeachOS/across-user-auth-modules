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
package com.foreach.across.modules.oauth2.config.security;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.oauth2.OAuth2ModuleSettings;
import com.foreach.across.modules.oauth2.services.ClientOAuth2AuthenticationSerializer;
import com.foreach.across.modules.oauth2.services.CustomTokenServices;
import com.foreach.across.modules.oauth2.services.OAuth2StatelessJdbcTokenStore;
import com.foreach.across.modules.oauth2.services.UserDetailsOAuth2AuthenticationSerializer;
import com.foreach.across.modules.spring.security.infrastructure.config.SecurityInfrastructure;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.DefaultUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.TokenStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAspectJAutoProxy
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter
{
	@Autowired
	private SecurityInfrastructure securityInfrastructure;

	@Autowired
	@Qualifier(AcrossContext.DATASOURCE)
	private DataSource dataSource;

	@Autowired
	@Qualifier("oAuth2ClientDetailsService")
	private ClientDetailsService clientDetailsService;

	@Autowired
	private OAuth2ModuleSettings oAuth2ModuleSettings;

	@Bean
	public ClientOAuth2AuthenticationSerializer clientOAuth2AuthenticationSerializer() {
		return new ClientOAuth2AuthenticationSerializer();
	}

	@Bean
	public UserDetailsOAuth2AuthenticationSerializer userOAuth2AuthenticationSerializer() {
		return new UserDetailsOAuth2AuthenticationSerializer();
	}

	@Bean
	@Primary
	public AuthorizationServerTokenServices tokenServices() {
		DefaultTokenServices tokenServices = new CustomTokenServices();
		tokenServices.setTokenStore( tokenStore() );
		tokenServices.setSupportRefreshToken( true );
		tokenServices.setClientDetailsService( clientDetailsService );
		//tokenServices.setTokenEnhancer(tokenEnchancer());

		return tokenServices;
	}

	@Bean
	@Exposed
	public TokenStore tokenStore() {
		return new OAuth2StatelessJdbcTokenStore( dataSource );
	}

	@Override
	public void configure( ClientDetailsServiceConfigurer clients ) throws Exception {
		clients.withClientDetails( clientDetailsService );
	}

	@Override
	public void configure( AuthorizationServerEndpointsConfigurer endpoints ) throws Exception {
		Map<String, String> mapping = new HashMap<>();
		if ( StringUtils.isNotBlank( oAuth2ModuleSettings.getCustomApprovalForm() ) ) {
			mapping.put( "/oauth/confirm_access", "/oauth/custom_confirm_access" );
		}
		endpoints.tokenStore( tokenStore() )
		         .tokenServices( tokenServices() )
		         .authenticationManager( securityInfrastructure.authenticationManager() )
		         .getFrameworkEndpointHandlerMapping().setMappings( mapping );

		if ( oAuth2ModuleSettings.isUseTokenStoreUserApprovalHandler() ) {
			endpoints.userApprovalHandler( tokenStoreApprovalHandler() ).requestFactory( oAuth2RequestFactory() );
		}
		else {
			endpoints.userApprovalHandler( new DefaultUserApprovalHandler() );
		}

		if ( oAuth2ModuleSettings.isUseJdbcAuthorizationCodeService() ) {
			endpoints.authorizationCodeServices( new JdbcAuthorizationCodeServices( dataSource ) );
		}
	}

	private TokenStoreUserApprovalHandler tokenStoreApprovalHandler() {
		TokenStoreUserApprovalHandler tokenStoreUserApprovalHandler = new TokenStoreUserApprovalHandler();
		tokenStoreUserApprovalHandler.setRequestFactory( oAuth2RequestFactory() );
		tokenStoreUserApprovalHandler.setTokenStore( tokenStore() );
		return tokenStoreUserApprovalHandler;
	}

	private DefaultOAuth2RequestFactory oAuth2RequestFactory() {
		return new DefaultOAuth2RequestFactory( clientDetailsService );
	}

	@Override
	public void configure( AuthorizationServerSecurityConfigurer oauthServer ) throws Exception {
		oauthServer.allowFormAuthenticationForClients();
	}
}
