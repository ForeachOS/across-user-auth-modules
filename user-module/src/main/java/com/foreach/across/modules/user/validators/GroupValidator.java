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

package com.foreach.across.modules.user.validators;

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.user.business.Group;
import com.foreach.across.modules.user.business.QGroup;
import com.foreach.across.modules.user.services.GroupService;
import com.foreach.across.modules.user.services.support.DefaultUserDirectoryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

/**
 * @author Arne Vandamme
 */
public class GroupValidator extends EntityValidatorSupport<Group>
{
	private final GroupService groupService;
	private final DefaultUserDirectoryStrategy defaultUserDirectoryStrategy;

	@Autowired
	public GroupValidator( GroupService groupService, DefaultUserDirectoryStrategy defaultUserDirectoryStrategy ) {
		this.groupService = groupService;
		this.defaultUserDirectoryStrategy = defaultUserDirectoryStrategy;
	}

	@Override
	public boolean supports( Class<?> clazz ) {
		return Group.class.isAssignableFrom( clazz );
	}

	@Override
	protected void preValidation( Group entity, Errors errors ) {
		defaultUserDirectoryStrategy.apply( entity );
	}

	@Override
	protected void postValidation( Group entity, Errors errors ) {
		if ( !errors.hasFieldErrors( "name" ) ) {
			Group other = groupService.findGroup( QGroup.group.name.equalsIgnoreCase( entity.getName() ) );

			if ( other != null && !other.equals( entity ) ) {
				errors.rejectValue( "name", "alreadyExists" );
			}
		}
	}
}
