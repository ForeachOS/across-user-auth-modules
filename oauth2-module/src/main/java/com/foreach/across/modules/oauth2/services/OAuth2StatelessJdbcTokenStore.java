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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.util.SerializationUtils;

import javax.sql.DataSource;
import java.util.List;

public class OAuth2StatelessJdbcTokenStore extends JdbcTokenStore
{
	@Autowired
	private List<OAuth2AuthenticationSerializer> serializers;

	public OAuth2StatelessJdbcTokenStore( DataSource dataSource ) {
		super( dataSource );
	}

	@Override
	protected byte[] serializeAuthentication( OAuth2Authentication authentication ) {
		for ( OAuth2AuthenticationSerializer serializer : serializers ) {
			if ( serializer.canSerialize( authentication ) ) {
				return serializer.serialize( authentication );
			}
		}
		return super.serializeAuthentication( authentication );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected OAuth2Authentication deserializeAuthentication( byte[] authentication ) {
		Object object = SerializationUtils.deserialize( authentication );
		if ( object instanceof AuthenticationSerializerObject ) {
			AuthenticationSerializerObject oAuth2AuthenticationSerializerObject =
					(AuthenticationSerializerObject) object;
			for ( OAuth2AuthenticationSerializer serializer : serializers ) {
				if ( serializer.canDeserialize( oAuth2AuthenticationSerializerObject ) ) {
					return serializer.deserialize( oAuth2AuthenticationSerializerObject );
				}
			}
			throw new OAuth2AuthenticationSerializer.SerializationException( object );
		}
		else if ( object instanceof OAuth2Authentication ) {
			return (OAuth2Authentication) object;
		}
		else {
			throw new OAuth2AuthenticationSerializer.SerializationException( object );
		}
	}
}
