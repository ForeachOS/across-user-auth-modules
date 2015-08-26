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

import com.foreach.across.modules.spring.security.SpringSecurityCache;
import com.foreach.across.modules.user.UserModuleCache;
import com.foreach.across.modules.user.UserModuleSettings;
import com.foreach.across.modules.user.business.Group;
import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.business.UserProperties;
import com.foreach.across.modules.user.dto.UserDto;
import com.foreach.across.modules.user.repositories.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;

@Service
public class UserServiceImpl implements UserService
{
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserValidator userValidator;

	@Autowired
	private UserPropertiesService userPropertiesService;

	@Autowired
	private UserModifiedNotifier userModifiedNotifier;

	private final PasswordEncoder passwordEncoder;
	private final boolean useEmailAsUsername;
	private final boolean requireEmailUnique;

	public UserServiceImpl( PasswordEncoder passwordEncoder, boolean useEmailAsUsername, boolean requireEmailUnique ) {
		Assert.notNull( passwordEncoder, "A UserService must be configured with a valid PasswordEncoder" );
		this.passwordEncoder = passwordEncoder;
		this.useEmailAsUsername = useEmailAsUsername;
		this.requireEmailUnique = requireEmailUnique;
	}

	@PostConstruct
	protected void validateServiceConfiguration() {
		if ( useEmailAsUsername && !requireEmailUnique ) {
			throw new RuntimeException(
					UserModuleSettings.REQUIRE_EMAIL_UNIQUE + " must be TRUE if " + UserModuleSettings.USE_EMAIL_AS_USERNAME + " is TRUE" );
		}
	}

	public boolean isUseEmailAsUsername() {
		return useEmailAsUsername;
	}

	public boolean isRequireEmailUnique() {
		return requireEmailUnique;
	}

	@Override
	public Collection<User> getUsers() {
		return userRepository.getAll();
	}

	@Cacheable(value = SpringSecurityCache.SECURITY_PRINCIPAL)
	@Override
	public User getUserById( long id ) {
		return userRepository.getById( id );
	}

	@Cacheable(value = UserModuleCache.USERS, key = "'email:' + #email")
	@Override
	public User getUserByEmail( String email ) {
		return userRepository.getByEmail( email );
	}

	@Cacheable(value = UserModuleCache.USERS, key = "'username:' + #username")
	@Override
	public User getUserByUsername( String username ) {
		return userRepository.getByUsername( username );
	}

	@Override
	public UserDto createUserDto( User user ) {
		return new UserDto( user );
	}

	@Override
	@Transactional
	public User save( UserDto userDto ) {
		User user;
		UserDto originalUser = null;

		if ( userDto.isNewEntity() ) {
			user = new User();

			if ( StringUtils.isBlank( userDto.getPassword() ) ) {
				throw new UserModuleException( "A new user always requires a non-blank password to be set." );
			}
		}
		else {
			long existingUserId = userDto.getId();

			if ( existingUserId == 0 ) {
				throw new UserModuleException(
						"Impossible to update a user with id 0, 0 is a special id that should never be used for persisted entities." );
			}

			user = getUserById( existingUserId );

			if ( user == null ) {
				throw new UserModuleException(
						"Attempt to update user with id " + existingUserId + " but that user does not exist" );
			}

			originalUser = new UserDto( user );
		}

		if ( useEmailAsUsername ) {
			if ( StringUtils.isBlank( userDto.getUsername() ) ) {
				userDto.setUsername( userDto.getEmail() );
			}

			// Update username to new email if it is modified and username was the old email
			if ( !userDto.isNewEntity()
					&& !StringUtils.equals( userDto.getEmail(), user.getEmail() )
					&& StringUtils.equals( userDto.getUsername(), user.getEmail() ) ) {
				userDto.setUsername( userDto.getEmail() );
			}
		}

		Errors errors = new BeanPropertyBindingResult( userDto, "user" );
		userValidator.validate( userDto, errors );

		if ( errors.hasErrors() ) {
			throw new UserValidationException(
					"Failed to validate User, [" + errors.getErrorCount() + "] validation errors: " + StringUtils.join(
							errors.getAllErrors(), System.lineSeparator() ),
					errors.getAllErrors() );
		}

		BeanUtils.copyProperties( userDto, user, "password" );

		// Only modify password if password on the dto is not blank
		if ( !StringUtils.isBlank( userDto.getPassword() ) ) {
			user.setPassword( passwordEncoder.encode( userDto.getPassword() ) );
		}

		if ( StringUtils.isBlank( user.getDisplayName() ) ) {
			user.setDisplayName( String.format( "%s %s", user.getFirstName(), user.getLastName() ).trim() );
		}

		if ( userDto.isNewEntity() ) {
			userRepository.create( user );
		}
		else {
			userRepository.update( user );
		}

		userDto.copyFrom( user );

		if ( originalUser != null ) {
			userModifiedNotifier.update( originalUser, userDto );
		}

		return user;
	}

	@Override
	@Transactional
	public void delete( long userId ) {
		User user = userRepository.getById( userId );
		deleteProperties( userId );
		userRepository.delete( user );
	}

	@Override
	public void deleteProperties( User user ) {
		deleteProperties( user.getId() );
	}

	@Override
	public void deleteProperties( long userId ) {
		userPropertiesService.deleteProperties( userId );
	}

	@Override
	public UserProperties getProperties( User user ) {
		return userPropertiesService.getProperties( user.getId() );
	}

	@Override
	public UserProperties getProperties( UserDto userDto ) {
		return userPropertiesService.getProperties( userDto.getId() );
	}

	@Override
	public void saveProperties( UserProperties userProperties ) {
		userPropertiesService.saveProperties( userProperties );
	}

	@Transactional(readOnly = true)
	@Override
	public Collection<User> getUsersWithPropertyValue( String propertyName, Object propertyValue ) {
		Collection<Long> userIds = userPropertiesService.getEntityIdsForPropertyValue( propertyName, propertyValue );

		if ( userIds.isEmpty() ) {
			return Collections.emptyList();
		}

		return userRepository.getAllForIds( userIds );
	}

	@Override
	public Collection<User> getUsersInGroup( Group group ) {
		return userRepository.getUsersInGroup( group );
	}
}
