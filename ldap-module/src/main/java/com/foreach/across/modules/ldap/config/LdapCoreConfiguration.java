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

package com.foreach.across.modules.ldap.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import com.foreach.across.modules.ldap.repositories.LdapConnectorRepository;
import com.foreach.across.modules.ldap.services.LdapSynchronizationService;
import com.foreach.across.modules.ldap.services.LdapSynchronizationServiceImpl;
import com.foreach.across.modules.ldap.tasks.LdapSynchronizationTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Marc Vanbrabant
 * @since 1.0.0
 */
@Configuration
@EnableScheduling
@EnableAcrossJpaRepositories(basePackageClasses = LdapConnectorRepository.class)
public class LdapCoreConfiguration
{
	@Bean
	@AcrossDepends(required = "UserModule")
	public LdapSynchronizationTask ldapSynchronizationTask() {
		return new LdapSynchronizationTask();
	}

	@Bean
	@AcrossDepends(required = "UserModule")
	public LdapSynchronizationService ldapSynchronizationService() {
		return new LdapSynchronizationServiceImpl();
	}
}
