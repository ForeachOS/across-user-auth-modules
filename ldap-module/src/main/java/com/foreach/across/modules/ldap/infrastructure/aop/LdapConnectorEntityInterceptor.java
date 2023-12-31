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

package com.foreach.across.modules.ldap.infrastructure.aop;

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.ldap.business.LdapConnector;
import com.foreach.across.modules.ldap.business.LdapConnectorSettings;
import com.foreach.across.modules.ldap.business.LdapConnectorType;
import com.foreach.across.modules.ldap.services.properties.LdapConnectorSettingsService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Marc Vanbrabant
 * @since 1.0.0
 */
public class LdapConnectorEntityInterceptor extends EntityInterceptorAdapter<LdapConnector>
{
	@Autowired
	private LdapConnectorSettingsService ldapConnectorSettingsService;

	@Override
	public boolean handles( Class<?> entityClass ) {
		return LdapConnector.class.isAssignableFrom( entityClass );
	}

	@Override
	public void afterCreate( LdapConnector entity ) {
		registerLdapConnectorSettings( entity );
	}

	@Override
	public void afterUpdate( LdapConnector entity ) {
		registerLdapConnectorSettings( entity );
	}

	private void registerLdapConnectorSettings( LdapConnector entity ) {
		LdapConnectorSettings ldapConnectorSettings = ldapConnectorSettingsService.getProperties( entity.getId() );

		LdapConnectorType ldapConnectorType = entity.getLdapConnectorType();
		ldapConnectorSettings.putAll( ldapConnectorType.getSettings() );
		ldapConnectorSettingsService.saveProperties( ldapConnectorSettings );
	}
}
