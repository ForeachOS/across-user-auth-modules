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

import com.foreach.across.modules.user.business.MachinePrincipal;
import com.foreach.across.modules.user.business.UserDirectory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Arne Vandamme
 */
public interface MachinePrincipalService extends QuerydslPredicateExecutor<MachinePrincipal>
{
	/**
	 * @return all registered machine principals
	 */
	Collection<MachinePrincipal> getMachinePrincipals();

	Optional<MachinePrincipal> getMachinePrincipalById( long id );

	/**
	 * Get the machine with a given name in the default directory.
	 *
	 * @param name of the machine
	 * @return instance or {@code null} if not found
	 */
	Optional<MachinePrincipal> getMachinePrincipalByName( String name );

	/**
	 * Get the group with a given name in the specified directory.
	 *
	 * @param name          of the machine
	 * @param userDirectory the machine should belong to
	 * @return instance or {@code null} if not found
	 */
	Optional<MachinePrincipal> getMachinePrincipalByName( String name, UserDirectory userDirectory );

	MachinePrincipal save( MachinePrincipal machinePrincipalDto );

	@Override
	Optional<MachinePrincipal> findOne( Predicate predicate );

	@Override
	Collection<MachinePrincipal> findAll( Predicate predicate );

	@Override
	Collection<MachinePrincipal> findAll( Predicate predicate, OrderSpecifier<?>... orderSpecifiers );

	@Override
	Page<MachinePrincipal> findAll( Predicate predicate, Pageable pageable );

	@Override
	long count( Predicate predicate );
}
