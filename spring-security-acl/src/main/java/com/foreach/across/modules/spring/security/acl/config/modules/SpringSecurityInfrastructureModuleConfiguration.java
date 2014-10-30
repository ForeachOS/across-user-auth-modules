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
package com.foreach.across.modules.spring.security.acl.config.modules;

import com.foreach.across.modules.spring.security.acl.infrastructure.CurrentAclSecurityPrincipalProxy;
import com.foreach.across.modules.spring.security.acl.infrastructure.CurrentAclSecurityPrincipalProxyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Injected in the SpringSecurityInfrastructureModule before bootstrapping so the
 * right {@link com.foreach.across.modules.spring.security.acl.infrastructure.CurrentAclSecurityPrincipalProxyImpl}
 * would be used.
 *
 * @author Arne Vandamme
 */
@Configuration
public class SpringSecurityInfrastructureModuleConfiguration
{
	@Bean
	public CurrentAclSecurityPrincipalProxy currentSecurityPrincipalProxy() {
		return new CurrentAclSecurityPrincipalProxyImpl();
	}
}
