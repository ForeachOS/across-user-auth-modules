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

import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.across.modules.spring.security.SpringSecurityModuleCache;
import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipal;
import com.foreach.across.modules.spring.security.infrastructure.services.SecurityPrincipalRetrievalStrategy;
import com.foreach.across.modules.user.business.BasicSecurityPrincipal;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * @author Arne Vandamme
 */
public interface SecurityPrincipalRepository
		extends IdBasedEntityJpaRepository<BasicSecurityPrincipal>, SecurityPrincipalRetrievalStrategy
{
	@Caching(
			put = {
					@CachePut(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#result.id", condition = "#result != null"),
					@CachePut(value = SpringSecurityModuleCache.SECURITY_PRINCIPAL, key = "#result.principalName", condition = "#result != null")
			}
	)
	@Query("select p from BasicSecurityPrincipal p where p.principalName = ?1")
	@Override
	Optional<SecurityPrincipal> getPrincipalByName( String principalName );
}
