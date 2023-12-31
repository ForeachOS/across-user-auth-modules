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
import com.foreach.across.modules.oauth2.OAuth2ModuleSettings;
import com.foreach.across.modules.oauth2.services.*;
import com.foreach.across.modules.spring.security.infrastructure.config.SecurityInfrastructure;
import com.foreach.common.concurrent.locks.distributed.DistributedLockRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.*;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
//@EnableAspectJAutoProxy
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter implements BeanFactoryAware
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
	private CacheManager cacheManager;

	@Autowired
	private OAuth2ModuleSettings oAuth2ModuleSettings;

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory( BeanFactory beanFactory ) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Bean
	public ClientOAuth2AuthenticationSerializer clientOAuth2AuthenticationSerializer() {
		return new ClientOAuth2AuthenticationSerializer();
	}

	@Bean
	public SecurityPrincipalIdOAuth2AuthenticationSerializer userOAuth2AuthenticationSerializer() {
		return new SecurityPrincipalIdOAuth2AuthenticationSerializer();
	}

	@Bean
	@Lazy
	IsolatedLockHandler isolatedLockHandler() {
		return new IsolatedLockHandler();
	}

	/**
	 * Overrides the default baen in {@link org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration}
	 */
	@Bean(name = { "defaultAuthorizationServerTokenServices", "tokenServices" })
	@Primary
	public AuthorizationServerTokenServices defaultAuthorizationServerTokenServices() {
		CachingAndLockingTokenServices tokenServices = new CachingAndLockingTokenServices( cacheManager );
		tokenServices.setTokenStore( tokenStore() );
		tokenServices.setSupportRefreshToken( true );
		tokenServices.setClientDetailsService( clientDetailsService );

		if ( oAuth2ModuleSettings.getUseLockingForTokenCreation() ) {
			// delayed get of DistributedLockRepository, don't force across to create it if not to be used
			tokenServices.setIsolatedLockHandler( isolatedLockHandler() );
			tokenServices.setObjectLockRepository( beanFactory.getBean( DistributedLockRepository.class ) );
		}
		//tokenServices.setTokenEnhancer(tokenEnchancer());

		return tokenServices;
	}

	@Bean
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

		if ( StringUtils.isNotBlank( oAuth2ModuleSettings.getApproval().getFormEndpoint() ) ) {
			mapping.put( "/oauth/confirm_access", "/oauth/confirm_access_external" );
		}

		endpoints.tokenStore( tokenStore() )
		         .tokenServices( beanFactory.getBean( AuthorizationServerTokenServices.class ) )
		         .requestFactory( oAuth2RequestFactory() )
		         .authenticationManager( securityInfrastructure.authenticationManager() )
		         .userApprovalHandler( userApprovalHandler() )
		         .getFrameworkEndpointHandlerMapping().setMappings( mapping );

		if ( oAuth2ModuleSettings.getUseJdbcAuthorizationCodeServices() ) {
			endpoints.authorizationCodeServices( customJdbcAuthorizationCodeServices() );
		}
	}

	@Bean
	@Lazy
	public UserApprovalHandler userApprovalHandler() {
		switch ( oAuth2ModuleSettings.getApproval().getHandler() ) {
			case TOKEN_STORE:
				return tokenStoreApprovalHandler();
			case APPROVAL_STORE:
				return approvalStoreUserApprovalHandler();
			default:
				return new DefaultUserApprovalHandler();
		}
	}

	private TokenStoreUserApprovalHandler tokenStoreApprovalHandler() {
		TokenStoreUserApprovalHandler tokenStoreUserApprovalHandler = new TokenStoreUserApprovalHandler();
		tokenStoreUserApprovalHandler.setRequestFactory( oAuth2RequestFactory() );
		tokenStoreUserApprovalHandler.setTokenStore( tokenStore() );
		tokenStoreUserApprovalHandler.setClientDetailsService( clientDetailsService );
		return tokenStoreUserApprovalHandler;
	}

	private ApprovalStoreUserApprovalHandler approvalStoreUserApprovalHandler() {
		ApprovalStoreUserApprovalHandler approvalStoreUserApprovalHandler = new ApprovalStoreUserApprovalHandler();
		approvalStoreUserApprovalHandler.setClientDetailsService( clientDetailsService );
		approvalStoreUserApprovalHandler.setRequestFactory( oAuth2RequestFactory() );
		approvalStoreUserApprovalHandler.setApprovalStore( approvalStore() );
		return approvalStoreUserApprovalHandler;
	}

	@Bean
	@Lazy
	public ApprovalStore approvalStore() {
		switch ( oAuth2ModuleSettings.getApproval().getStore() ) {
			case JDBC:
				return new JdbcApprovalStore( dataSource );
			case TOKEN:
				TokenApprovalStore tokenApprovalStore = new TokenApprovalStore();
				tokenApprovalStore.setTokenStore( tokenStore() );
				return tokenApprovalStore;
			default:
				return new InMemoryApprovalStore();
		}
	}

	@Bean
	public AuthorizationCodeServices customJdbcAuthorizationCodeServices() {
		return new CustomJdbcAuthorizationCodeServices( dataSource );
	}

	@Bean
	DefaultOAuth2RequestFactory oAuth2RequestFactory() {
		return new DefaultOAuth2RequestFactory( clientDetailsService );
	}

	@Override
	public void configure( AuthorizationServerSecurityConfigurer oauthServer ) throws Exception {
		oauthServer.allowFormAuthenticationForClients();
	}
}
