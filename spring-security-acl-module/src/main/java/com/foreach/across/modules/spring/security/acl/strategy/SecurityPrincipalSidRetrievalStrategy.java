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
package com.foreach.across.modules.spring.security.acl.strategy;

import com.foreach.across.modules.spring.security.acl.business.SecurityPrincipalSid;
import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipal;
import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipalHierarchy;
import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipalId;
import com.foreach.across.modules.spring.security.infrastructure.services.SecurityPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * General implementation of {@link org.springframework.security.acls.model.SidRetrievalStrategy}
 * supporting {@link com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipal} and
 * {@link com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipalHierarchy} implementations.
 * <p/>
 * All parent sids (eg. user groups the user principal belongs to) will be added right after the principal
 * sid but before any authorities.
 *
 * @author Arne Vandamme
 */
@RequiredArgsConstructor
public class SecurityPrincipalSidRetrievalStrategy implements SidRetrievalStrategy
{
	private final SecurityPrincipalService securityPrincipalService;

	@Override
	public List<Sid> getSids( Authentication authentication ) {
		Object principal = authentication.getPrincipal();

		Collection<SecurityPrincipal> parents = ( principal instanceof SecurityPrincipalHierarchy )
				? ( (SecurityPrincipalHierarchy) principal ).getParentPrincipals() : new ArrayList<>();

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		List<Sid> sids = new ArrayList<>( authorities.size() + 1 + parents.size() );

		if ( principal instanceof SecurityPrincipal ) {
			sids.add( SecurityPrincipalSid.of( (SecurityPrincipal) principal ) );
		}
		else if ( principal instanceof SecurityPrincipalId ) {
			securityPrincipalService.getPrincipalById( (SecurityPrincipalId) principal )
			                        .ifPresent( securityPrincipal -> {
				                        if ( securityPrincipal instanceof SecurityPrincipalHierarchy ) {
					                        parents.addAll( ( (SecurityPrincipalHierarchy) securityPrincipal ).getParentPrincipals() );
				                        }
			                        } );

			sids.add( SecurityPrincipalSid.of( (SecurityPrincipalId) principal ) );
		}
		else {
			sids.add( new PrincipalSid( authentication ) );
		}

		for ( SecurityPrincipal parent : parents ) {
			sids.add( SecurityPrincipalSid.of( parent ) );
		}

		for ( GrantedAuthority authority : authorities ) {
			sids.add( new GrantedAuthoritySid( authority ) );
		}

		return sids;
	}
}
