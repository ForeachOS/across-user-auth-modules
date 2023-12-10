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

package com.foreach.across.modules.ldap;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.AcrossWebModule;

/**
 * @author Arne Vandamme
 * @since 1.0.0
 */
@AcrossDepends(
		required = { AcrossWebModule.NAME, AcrossHibernateJpaModule.NAME },
		optional = { "AdminWebModule", "EntityModule", "UserModule", "PropertiesModule" }
)
public class LdapModule extends AcrossModule
{
	public static final String NAME = "LdapModule";

	@Override
	public String getName() {
		return NAME;
	}


	@Override
	public String getDescription() {
		return "Provides a domain model and services for connecting to LDAP directories.";
	}
}
