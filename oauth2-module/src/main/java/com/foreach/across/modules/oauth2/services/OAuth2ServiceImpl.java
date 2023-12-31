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
package com.foreach.across.modules.oauth2.services;

import com.foreach.across.modules.oauth2.OAuth2ModuleCache;
import com.foreach.across.modules.oauth2.business.OAuth2Client;
import com.foreach.across.modules.oauth2.business.OAuth2Scope;
import com.foreach.across.modules.oauth2.repositories.OAuth2ClientRepository;
import com.foreach.across.modules.oauth2.repositories.OAuth2ScopeRepository;
import com.foreach.across.modules.spring.security.SpringSecurityModuleCache;
import com.foreach.across.modules.user.services.support.DefaultUserDirectoryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class OAuth2ServiceImpl implements OAuth2Service
{
	@Autowired
	private OAuth2ScopeRepository oAuth2ScopeRepository;

	@Autowired
	private OAuth2ClientRepository oAuth2ClientRepository;

	@Autowired
	private DefaultUserDirectoryStrategy defaultUserDirectoryStrategy;

	@Override
	public Collection<OAuth2Client> getOAuth2Clients() {
		return oAuth2ClientRepository.findAll();
	}

	@Override
	public Collection<OAuth2Scope> getOAuth2Scopes() {
		return oAuth2ScopeRepository.findAll();
	}

	@Override
	public OAuth2Scope saveScope( OAuth2Scope oAuth2Scope ) {
		return oAuth2ScopeRepository.save( oAuth2Scope );
	}

	@Override
	public Optional<OAuth2Scope> getScopeById( long id ) {
		return oAuth2ScopeRepository.findById( id );
	}

	@Override
	public OAuth2Client saveClient( OAuth2Client oAuth2Client ) {
		defaultUserDirectoryStrategy.apply( oAuth2Client );
		return oAuth2ClientRepository.save( oAuth2Client );
	}

	@Cacheable(value = OAuth2ModuleCache.CLIENTS, unless = SpringSecurityModuleCache.UNLESS_NULLS_ONLY)
	@Override
	public Optional<OAuth2Client> getClientById( String clientId ) {
		return oAuth2ClientRepository.findByClientId( clientId );
	}
}
