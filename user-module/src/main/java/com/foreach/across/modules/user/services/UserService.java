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

import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.business.UserDirectory;
import com.foreach.across.modules.user.business.UserProperties;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Collection;
import java.util.Optional;

public interface UserService extends QuerydslPredicateExecutor<User>
{
	Collection<User> getUsers();

	Optional<User> getUserById( long id );

	Optional<User> getUserByEmail( String email );

	Optional<User> getUserByEmail( String email, UserDirectory userDirectory );

	Optional<User> getUserByUsername( String username );

	Optional<User> getUserByUsername( String username, UserDirectory userDirectory );

	User save( User user );

	void delete( long id );

	void saveProperties( UserProperties userProperties );

	void deleteProperties( User user );

	void deleteProperties( long userId );

	UserProperties getProperties( User user );

	Collection<User> getUsersWithPropertyValue( String propertyName, Object propertyValue );

	@Override
	Optional<User> findOne( Predicate predicate );

	@Override
	Collection<User> findAll( Predicate predicate );

	@Override
	Collection<User> findAll( Predicate predicate, OrderSpecifier<?>... orderSpecifiers );

	@Override
	Iterable<User> findAll( Predicate predicate, Sort sort );

	@Override
	Page<User> findAll( Predicate predicate, Pageable pageable );

	@Override
	Iterable<User> findAll( OrderSpecifier<?>... orders );

	@Override
	long count( Predicate predicate );

	@Override
	boolean exists( Predicate predicate );
}
