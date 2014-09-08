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
package com.foreach.across.modules.oauth2;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.database.HasSchemaConfiguration;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.filters.BeanFilterComposite;
import com.foreach.across.core.filters.ClassBeanFilter;
import com.foreach.across.core.installers.AcrossSequencesInstaller;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.hibernate.provider.*;
import com.foreach.across.modules.oauth2.config.OAuth2EndpointsConfiguration;
import com.foreach.across.modules.oauth2.config.OAuth2RepositoriesConfiguration;
import com.foreach.across.modules.oauth2.config.OAuth2SchemaConfiguration;
import com.foreach.across.modules.oauth2.config.OAuth2ServicesConfiguration;
import com.foreach.across.modules.oauth2.config.security.AuthorizationServerSecurityConfiguration;
import com.foreach.across.modules.oauth2.config.security.CustomTokenEndpointsConfiguration;
import com.foreach.across.modules.oauth2.config.security.ResourceServerSecurityConfiguration;
import com.foreach.across.modules.oauth2.installers.OAuth2SchemaInstaller;
import com.foreach.across.modules.oauth2.installers.TokenStoreSchemaInstaller;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.user.UserModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpointHandlerMapping;

import java.util.Set;

@AcrossDepends(required = { UserModule.NAME, SpringSecurityModule.NAME })
public class OAuth2Module extends AcrossModule implements HasHibernatePackageProvider, HasSchemaConfiguration
{

	public static final String NAME = "Oauth2Module";

	private final SchemaConfiguration schemaConfiguration = new OAuth2SchemaConfiguration();

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Provides Oauth 2 security";
	}

	public OAuth2Module() {
		setExposeFilter(
				new BeanFilterComposite(
						defaultExposeFilter(),
						new ClassBeanFilter( FrameworkEndpointHandlerMapping.class )
				)
		);
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				new AcrossSequencesInstaller(),
				new OAuth2SchemaInstaller( schemaConfiguration ),
				new TokenStoreSchemaInstaller()
		};
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add(
				new AnnotatedClassConfigurer(
						OAuth2RepositoriesConfiguration.class,
						OAuth2ServicesConfiguration.class,
						OAuth2EndpointsConfiguration.class,
						AuthorizationServerSecurityConfiguration.class,
						CustomTokenEndpointsConfiguration.class,
						ResourceServerSecurityConfiguration.class
				)
		);
	}

	/**
	 * Returns the package provider associated with this implementation.
	 *
	 * @param hibernateModule AcrossHibernateModule that is requesting packages.
	 * @return HibernatePackageProvider instance.
	 */
	public HibernatePackageProvider getHibernatePackageProvider( AcrossHibernateModule hibernateModule ) {
		if ( StringUtils.equals( "AcrossHibernateModule", hibernateModule.getName() ) ) {
			return new HibernatePackageProviderComposite(
					new PackagesToScanProvider( "com.foreach.across.modules.oauth2.business" ),
					new TableAliasProvider( schemaConfiguration.getTables() ) );
		}

		return null;
	}

	@Override
	public SchemaConfiguration getSchemaConfiguration() {
		return schemaConfiguration;
	}
}
