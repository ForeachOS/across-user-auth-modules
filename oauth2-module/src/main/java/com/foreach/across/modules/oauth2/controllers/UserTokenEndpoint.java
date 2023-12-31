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
package com.foreach.across.modules.oauth2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.StringUtils;

import java.util.*;

/**
 * Additional OAuth endpoint for creating a new user token without approvals, but based
 * on the permissions of the principal represented by the current token.
 *
 * @author Arne Vandamme
 */
@FrameworkEndpoint
public class UserTokenEndpoint
{
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private AuthorizationServerTokenServices authorizationServerTokenServices;

	@RequestMapping("/oauth/user_token")
	public ResponseEntity<Map<String, String>> createUserToken(
			OAuth2Authentication authentication,
			@RequestParam(value = "username") String username,
			@RequestParam(value = "scope") String scope
	) {
		HttpStatus status = HttpStatus.OK;
		Map<String, String> response = new HashMap<>();

		String forbidden = null;

		Set<String> allowedScopes = authentication.getOAuth2Request().getScope();
		Set<String> requestedScopes = new HashSet<>( Arrays.asList( StringUtils.split( scope, " " ) ) );

		for ( String requestedScope : requestedScopes ) {
			if ( !allowedScopes.contains( requestedScope ) ) {
				forbidden = "Scope \"" + requestedScope + "\" is not allowed with the current access token.";
			}
		}

		if ( forbidden == null ) {
			UserDetails userDetails = null;
			try {
				userDetails = userDetailsService.loadUserByUsername( username );
			}
			catch ( UsernameNotFoundException e ) {
				forbidden = "Requested user does not exist.";
			}

			if ( userDetails != null && !canAuthenticate( userDetails ) ) {
				forbidden = "Requested user is not allowed to authenticate.";
			}

			if ( userDetails != null && forbidden == null ) {
				// Build new request and user authentication
				OAuth2Request clientAuthentication = authentication.getOAuth2Request();

				OAuth2Request request = new OAuth2Request(
						clientAuthentication.getRequestParameters(),
						clientAuthentication.getClientId(),
						clientAuthentication.getAuthorities(),
						clientAuthentication.isApproved(),
						requestedScopes,
						clientAuthentication.getResourceIds(),
						clientAuthentication.getRedirectUri(),
						clientAuthentication.getResponseTypes(),
						clientAuthentication.getExtensions()
				);

				Authentication userAuthentication = new PreAuthenticatedAuthenticationToken( userDetails, null,
				                                                                             userDetails
						                                                                             .getAuthorities() );
				OAuth2Authentication newAuthentication = new OAuth2Authentication( request, userAuthentication );

				OAuth2AccessToken token = authorizationServerTokenServices.createAccessToken( newAuthentication );
				response.put( "access_token", token.getValue() );
				response.put( "token_type", token.getTokenType() );
				response.put( "expires_in", Integer.toString( token.getExpiresIn() ) );
				response.put( "scope", StringUtils.join( token.getScope(), " " ) );

				if ( token.getRefreshToken() != null ) {
					response.put( "refresh_token", token.getRefreshToken().getValue() );
				}

			}
		}

		if ( forbidden != null ) {
			status = HttpStatus.FORBIDDEN;
			response.put( "error", forbidden );
		}

		return new ResponseEntity<>( response, status );
	}

	private boolean canAuthenticate( UserDetails userDetails ) {
		return userDetails.isEnabled() && userDetails.isAccountNonExpired() && userDetails
				.isAccountNonLocked() && userDetails.isCredentialsNonExpired();
	}
}
