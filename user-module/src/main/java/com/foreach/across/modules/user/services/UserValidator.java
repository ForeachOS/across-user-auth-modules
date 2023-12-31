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

package com.foreach.across.modules.user.services;

import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipal;
import com.foreach.across.modules.spring.security.infrastructure.services.SecurityPrincipalService;
import com.foreach.across.modules.user.UserModuleSettings;
import com.foreach.across.modules.user.business.Group;
import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.business.UserDirectory;
import com.foreach.across.modules.user.services.support.DefaultUserDirectoryStrategy;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
public class UserValidator implements Validator
{
	@Autowired
	private UserService userService;

	@Autowired
	private UserModuleSettings settings;

	@Autowired
	private EmailValidator emailValidator;

	@Autowired
	private SecurityPrincipalService securityPrincipalService;

	@Autowired
	private DefaultUserDirectoryStrategy defaultUserDirectoryStrategy;

	@Override
	public void validate( Object target, Errors errors ) {
		if ( supports( target.getClass() ) ) {
			User userDto = (User) target;
			defaultUserDirectoryStrategy.apply( userDto );

			if ( settings.isRequireEmailUnique() ) {
				User userByEmail = userService.getUserByEmail( userDto.getEmail() ).orElse( null );
				if ( userByEmail != null && !userByEmail.getId().equals( userDto.getId() ) ) {
					errors.reject( null, "email already exists" );
				}
			}

			if ( settings.isUseEmailAsUsername() || settings.isRequireEmailUnique() ) {
				if ( StringUtils.isBlank( userDto.getEmail() ) ) {
					errors.rejectValue( "email", null, "email cannot be empty" );
				}
			}
			else {
				if ( StringUtils.isBlank( userDto.getUsername() ) ) {
					errors.rejectValue( "username", null, "username cannot be empty" );
				}
			}

			if ( StringUtils.contains( userDto.getUsername(), ' ' ) ) {
				errors.rejectValue( "username", null, "username cannot contain whitespaces" );
			}

			if ( !StringUtils.isBlank( userDto.getEmail() ) ) {
				if ( !emailValidator.isValid( userDto.getEmail(), null ) ) {
					errors.rejectValue( "email", null, "invalid email" );
				}
			}

			if ( !errors.hasFieldErrors( "email" ) ) {
				String principalName = userDto.getUsername();

				if ( settings.isUseEmailAsUsername() && StringUtils.isBlank( principalName ) ) {
					principalName = userDto.getEmail();
				}

				securityPrincipalService.getPrincipalByName( principalName )
				                        .filter( principal -> !isSamePrincipal( principal, userDto ) )
				                        .ifPresent( p -> errors.rejectValue( "username", "alreadyExists", "username is not available" ) );
			}

			if ( !errors.hasFieldErrors( "groups" ) ) {
				UserDirectory expectedUserDirectory = userDto.getUserDirectory();

				for ( Group group : userDto.getGroups() ) {
					if ( !expectedUserDirectory.equals( group.getUserDirectory() ) ) {
						errors.rejectValue( "groups", "differentGroupUserDirectory", new Object[] { group.getName() },
						                    "Groups must be from the same user directory." );
					}
				}
			}
		}
	}

	private boolean isSamePrincipal( SecurityPrincipal principal, User user ) {
		return principal instanceof User && ( (User) principal ).getId().equals( user.getId() );
	}

	@Override
	public boolean supports( Class<?> clazz ) {
		return User.class.isAssignableFrom( clazz );
	}
}
