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

package test.acl.application.extensions;

import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.spring.security.acl.business.AclAuthorities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;

import java.util.Collections;

/**
 * @author Arne Vandamme
 */
@ModuleConfiguration(SpringSecurityModule.NAME)
@EnableGlobalAuthentication
public class SecurityConfiguration
{
	@Autowired
	public void configureGlobal( AuthenticationManagerBuilder auth ) throws Exception {
		auth.inMemoryAuthentication()
		    .withUser( "admin" ).password( "{noop}admin" )
		    .authorities( "access administration", AclAuthorities.TAKE_OWNERSHIP, AclAuthorities.MODIFY_ACL ).and()
		    .withUser( "user" ).password( "{noop}user" )
		    .authorities( Collections.emptyList() );
	}
}
