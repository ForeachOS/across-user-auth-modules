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

package com.foreach.across.modules.it.ldap;

import com.foreach.across.modules.ldap.LdapModule;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@AcrossWebAppConfiguration
public class ITBootstrapWithoutAdditionalModules
{
	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void repositoriesShouldBeExposed() {
		assertTrue( applicationContext.containsBean( "com.foreach.across.modules.ldap.repositories.LdapConnectorRepository" ) );
	}

	@Configuration
	@AcrossTestConfiguration(modules = LdapModule.NAME)
	protected static class Config
	{
	}
}
