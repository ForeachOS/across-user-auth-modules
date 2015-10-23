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
package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.hibernate.jpa.config.HibernateJpaConfiguration;
import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.across.modules.spring.security.SpringSecurityModuleCache;
import com.foreach.across.modules.user.business.MachinePrincipal;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Arne Vandamme
 */
public interface MachinePrincipalRepository
		extends IdBasedEntityJpaRepository<MachinePrincipal>, QueryDslPredicateExecutor<MachinePrincipal>
{
	@Caching(
			put = {
					@CachePut(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#result.id", condition = "#result != null"),
					@CachePut(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#result.principalName", condition = "#result != null")
			}
	)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER, readOnly = true)
	@Override
	MachinePrincipal findOne( Long id );

	@Caching(
			evict = {
					@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#p0.id"),
					@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#p0.principalName")
			}
	)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	<S extends MachinePrincipal> S save( S machinePrincipal );

	@Caching(
			evict = {
					@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#p0.id"),
					@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#p0.principalName")
			}
	)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	<S extends MachinePrincipal> S saveAndFlush( S machinePrincipal );

	@Caching(
			evict = {
					@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#p0.id"),
					@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#p0.principalName")
			}
	)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	void delete( MachinePrincipal machinePrincipal );

	@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, allEntries = true)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	void delete( Long id );

	@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, allEntries = true)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	void deleteAllInBatch();

	@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, allEntries = true)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	void deleteInBatch( Iterable<MachinePrincipal> entities );

	@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, allEntries = true)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	void delete( Iterable<? extends MachinePrincipal> entities );

	@CacheEvict(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, allEntries = true)
	@Transactional(value = HibernateJpaConfiguration.TRANSACTION_MANAGER)
	@Override
	void deleteAll();
}
