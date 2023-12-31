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

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.actions.EntityConfigurationAllowableActionsBuilder;
import com.foreach.across.modules.entity.actions.FixedEntityAllowableActionsBuilder;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.spring.security.acl.SpringSecurityAclModuleIcons;
import com.foreach.across.modules.spring.security.acl.business.AclAuthorities;
import com.foreach.across.modules.spring.security.acl.business.AclSecurityEntity;
import com.foreach.across.modules.spring.security.acl.services.AclSecurityEntityService;
import com.foreach.across.modules.spring.security.acl.ui.AclPermissionsForm;
import com.foreach.across.modules.spring.security.acl.validators.AclSecurityEntityValidator;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.spring.security.actions.AuthorityMatchingAllowableActions;
import com.foreach.across.modules.spring.security.authority.AuthorityMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Vandamme
 */
@Configuration
@ConditionalOnAcrossModule({ EntityModule.NAME, AdminWebModule.NAME, BootstrapUiModule.NAME })
@ComponentScan(basePackageClasses = AclPermissionsForm.class)
public class EntityUiModuleConfiguration implements EntityConfigurer
{
	@Bean
	public AclSecurityEntityValidator aclSecurityEntityValidator( AclSecurityEntityService aclSecurityEntityService ) {
		return new AclSecurityEntityValidator( aclSecurityEntityService );
	}

	@PostConstruct
	void registerIcons() {
		SpringSecurityAclModuleIcons.registerIconSet();
	}

	@Override
	public void configure( EntitiesConfigurationBuilder configuration ) {
		configuration.withType( AclSecurityEntity.class )
		             .allowableActionsBuilder( actionsBuilderForAuthority( AclAuthorities.TAKE_OWNERSHIP ) )
		             .listView( lvb -> lvb.showProperties( "name", "parent.name", "lastModified" ) );
	}

	private EntityConfigurationAllowableActionsBuilder actionsBuilderForAuthority( String authority ) {
		Map<AllowableAction, AuthorityMatcher> actionAuthorityMatcherMap = new HashMap<>();
		actionAuthorityMatcherMap.put( AllowableAction.READ, AuthorityMatcher.allOf( authority ) );
		actionAuthorityMatcherMap.put( AllowableAction.UPDATE, AuthorityMatcher.allOf( authority ) );
		actionAuthorityMatcherMap.put( AllowableAction.DELETE, AuthorityMatcher.allOf( authority ) );
		actionAuthorityMatcherMap.put( AllowableAction.CREATE, AuthorityMatcher.allOf( authority ) );

		return new FixedEntityAllowableActionsBuilder(
				AuthorityMatchingAllowableActions.forSecurityContext( actionAuthorityMatcherMap )
		);
	}
}
