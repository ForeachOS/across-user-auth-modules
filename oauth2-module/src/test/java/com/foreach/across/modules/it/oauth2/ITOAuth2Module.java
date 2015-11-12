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
package com.foreach.across.modules.it.oauth2;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.oauth2.OAuth2Module;
import com.foreach.across.modules.oauth2.OAuth2ModuleSettings;
import com.foreach.across.modules.oauth2.business.OAuth2Client;
import com.foreach.across.modules.oauth2.services.OAuth2Service;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.spring.security.infrastructure.services.SecurityPrincipalService;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.test.AcrossTestWebContext;
import org.junit.Test;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.*;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpointHandlerMapping;

import java.util.Set;

import static org.junit.Assert.*;

public class ITOAuth2Module
{
	@Test
	public void verifyEndpointsDetected() {
		BaseConfiguration configuration = new BaseConfiguration()
		{
			@Override
			protected void configureModule( OAuth2Module module ) {
			}
		};

		try (AcrossTestWebContext ctx = new AcrossTestWebContext( configuration )) {
			FrameworkEndpointHandlerMapping handlerMapping
					= ctx.beanRegistry().getBeanOfType( FrameworkEndpointHandlerMapping.class );

			Set<String> endpoints = handlerMapping.getPaths();

			assertTrue( endpoints.contains( "/oauth/invalidate" ) );
			assertTrue( endpoints.contains( "/oauth/user_token" ) );
		}
	}

	@Test
	public void verifyNoCaching() {
		BaseConfiguration configuration = new BaseConfiguration()
		{
			@Override
			protected void configureModule( OAuth2Module module ) {
			}
		};

		try (AcrossTestWebContext ctx = new AcrossTestWebContext( configuration )) {
			OAuth2Service oauth2Service
					= ctx.beanRegistry().getBeanOfType( OAuth2Service.class );
			SecurityPrincipalService securityPrincipalService
					= ctx.beanRegistry().getBeanOfType( SecurityPrincipalService.class );

			OAuth2Client oAuth2Client = new OAuth2Client();
			oAuth2Client.setClientId( "someclient" );
			oAuth2Client.setClientSecret( "fred" );
			oAuth2Client.setSecretRequired( true );

			oauth2Service.save( oAuth2Client );

			OAuth2Client existing = oauth2Service.getClientById( "someclient" );
			assertNotNull( existing );

			assertEquals( oAuth2Client, existing );

			OAuth2Client fetched = oauth2Service.getClientById( "someclient" );
			assertNotNull( fetched );
			assertEquals( oAuth2Client, fetched );
			assertNotSame( existing, fetched );

			fetched = securityPrincipalService.getPrincipalByName( oAuth2Client.getPrincipalName() );
			assertNotNull( fetched );
			assertEquals( oAuth2Client, fetched );
			assertNotSame( existing, fetched );
		}
	}

	@Test
	public void defaultApprovalHandler() {
		BaseConfiguration configuration = new BaseConfiguration()
		{
			@Override
			protected void configureModule( OAuth2Module module ) {
				module.setProperty( OAuth2ModuleSettings.APPROVAL_HANDLER,
				                    OAuth2ModuleSettings.ApprovalHandler.DEFAULT );
			}
		};

		try (AcrossTestWebContext ctx = new AcrossTestWebContext( configuration )) {
			UserApprovalHandler approvalHandler
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, UserApprovalHandler.class );

			assertNotNull( approvalHandler );
			assertTrue( approvalHandler instanceof DefaultUserApprovalHandler );

			ApplicationContext applicationContext
					= ctx.contextInfo().getModuleInfo( OAuth2Module.NAME ).getApplicationContext();

			assertTrue( applicationContext.containsBeanDefinition( "approvalStore" ) );
			assertFalse( ( (SingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory() )
					             .containsSingleton( "approvalStore" ) );
		}
	}

	@Test
	public void tokenStoreApprovalHandler() {
		BaseConfiguration configuration = new BaseConfiguration()
		{
			@Override
			protected void configureModule( OAuth2Module module ) {
				module.setProperty( OAuth2ModuleSettings.APPROVAL_HANDLER,
				                    OAuth2ModuleSettings.ApprovalHandler.TOKEN_STORE );
			}
		};

		try (AcrossTestWebContext ctx = new AcrossTestWebContext( configuration )) {
			UserApprovalHandler approvalHandler
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, UserApprovalHandler.class );

			assertNotNull( approvalHandler );
			assertTrue( approvalHandler instanceof TokenStoreUserApprovalHandler );

			ApplicationContext applicationContext
					= ctx.contextInfo().getModuleInfo( OAuth2Module.NAME ).getApplicationContext();

			assertTrue( applicationContext.containsBeanDefinition( "approvalStore" ) );
			assertFalse( ( (SingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory() )
					             .containsSingleton( "approvalStore" ) );
		}
	}

	@Test
	public void jdbcApprovalStore() {
		BaseConfiguration configuration = new BaseConfiguration()
		{
			@Override
			protected void configureModule( OAuth2Module module ) {
				module.setProperty( OAuth2ModuleSettings.APPROVAL_HANDLER,
				                    OAuth2ModuleSettings.ApprovalHandler.APPROVAL_STORE );
				module.setProperty( OAuth2ModuleSettings.APPROVAL_STORE, OAuth2ModuleSettings.ApprovalStore.JDBC );
			}
		};

		try (AcrossTestWebContext ctx = new AcrossTestWebContext( configuration )) {
			UserApprovalHandler approvalHandler
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, UserApprovalHandler.class );

			assertNotNull( approvalHandler );
			assertTrue( approvalHandler instanceof ApprovalStoreUserApprovalHandler );

			ApprovalStore approvalStore
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, ApprovalStore.class );

			assertNotNull( approvalStore );
			assertTrue( approvalStore instanceof JdbcApprovalStore );

			ApplicationContext applicationContext
					= ctx.contextInfo().getModuleInfo( OAuth2Module.NAME ).getApplicationContext();
			assertTrue( ( (SingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory() )
					            .containsSingleton( "approvalStore" ) );
		}
	}

	@Test
	public void memoryApprovalStore() {
		BaseConfiguration configuration = new BaseConfiguration()
		{
			@Override
			protected void configureModule( OAuth2Module module ) {
				module.setProperty( OAuth2ModuleSettings.APPROVAL_HANDLER,
				                    OAuth2ModuleSettings.ApprovalHandler.APPROVAL_STORE );
				module.setProperty( OAuth2ModuleSettings.APPROVAL_STORE, OAuth2ModuleSettings.ApprovalStore.IN_MEMORY );
			}
		};

		try (AcrossTestWebContext ctx = new AcrossTestWebContext( configuration )) {
			UserApprovalHandler approvalHandler
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, UserApprovalHandler.class );

			assertNotNull( approvalHandler );
			assertTrue( approvalHandler instanceof ApprovalStoreUserApprovalHandler );

			ApprovalStore approvalStore
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, ApprovalStore.class );

			assertNotNull( approvalStore );
			assertTrue( approvalStore instanceof InMemoryApprovalStore );

			ApplicationContext applicationContext
					= ctx.contextInfo().getModuleInfo( OAuth2Module.NAME ).getApplicationContext();
			assertTrue( ( (SingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory() )
					            .containsSingleton( "approvalStore" ) );
		}
	}

	@Test
	public void tokenApprovalStore() {
		BaseConfiguration configuration = new BaseConfiguration()
		{
			@Override
			protected void configureModule( OAuth2Module module ) {
				module.setProperty( OAuth2ModuleSettings.APPROVAL_HANDLER,
				                    OAuth2ModuleSettings.ApprovalHandler.APPROVAL_STORE );
				module.setProperty( OAuth2ModuleSettings.APPROVAL_STORE, OAuth2ModuleSettings.ApprovalStore.TOKEN );
			}
		};

		try (AcrossTestWebContext ctx = new AcrossTestWebContext( configuration )) {
			UserApprovalHandler approvalHandler
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, UserApprovalHandler.class );

			assertNotNull( approvalHandler );
			assertTrue( approvalHandler instanceof ApprovalStoreUserApprovalHandler );

			ApprovalStore approvalStore
					= ctx.beanRegistry().getBeanOfTypeFromModule( OAuth2Module.NAME, ApprovalStore.class );

			assertNotNull( approvalStore );
			assertTrue( approvalStore instanceof TokenApprovalStore );

			ApplicationContext applicationContext
					= ctx.contextInfo().getModuleInfo( OAuth2Module.NAME ).getApplicationContext();
			assertTrue( ( (SingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory() )
					            .containsSingleton( "approvalStore" ) );
		}
	}

	static abstract class BaseConfiguration implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( acrossHibernateModule() );
			context.addModule( userModule() );
			context.addModule( oauth2Module() );
			context.addModule( propertiesModule() );
			context.addModule( springSecurityModule() );
		}

		private PropertiesModule propertiesModule() {
			return new PropertiesModule();
		}

		private AcrossHibernateModule acrossHibernateModule() {
			return new AcrossHibernateModule();
		}

		private UserModule userModule() {
			return new UserModule();
		}

		private OAuth2Module oauth2Module() {
			OAuth2Module oAuth2Module = new OAuth2Module();
			configureModule( oAuth2Module );
			return oAuth2Module;
		}

		private SpringSecurityModule springSecurityModule() {
			SpringSecurityModule springSecurityModule = new SpringSecurityModule();
			springSecurityModule.addApplicationContextConfigurer( DummyResourceServerConfigurerAdapter.class );
			return springSecurityModule;
		}

		protected abstract void configureModule( OAuth2Module module );
	}

	@Configuration
	static class DummyResourceServerConfigurerAdapter extends ResourceServerConfigurerAdapter
	{
		@Override
		public void configure( ResourceServerSecurityConfigurer resources ) throws Exception {
			resources.resourceId( "dummyResourceId" );
		}

		@Override
		public void configure( HttpSecurity http ) throws Exception {
			http.requestMatchers().antMatchers( "/users/**", "/user/**", "/oauth/user_token" );
			http.authorizeRequests().anyRequest().authenticated();
		}
	}
}
