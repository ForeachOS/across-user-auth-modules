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

import com.foreach.across.core.annotations.RefreshableCollection;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

/**
 * @author Andy Somers
 */
public class CustomJdbcAuthorizationCodeServices implements AuthorizationCodeServices
{
	@RefreshableCollection(includeModuleInternals = true)
	private Collection<OAuth2AuthenticationSerializer> serializers;

	private static final String DEFAULT_SELECT_STATEMENT = "select code, authentication from oauth_code where code = ?";
	private static final String DEFAULT_INSERT_STATEMENT =
			"insert into oauth_code (code, authentication, created) values (?, ?, ?)";
	private static final String DEFAULT_DELETE_STATEMENT = "delete from oauth_code where code = ?";

	private String selectAuthenticationSql = DEFAULT_SELECT_STATEMENT;
	private String insertAuthenticationSql = DEFAULT_INSERT_STATEMENT;
	private String deleteAuthenticationSql = DEFAULT_DELETE_STATEMENT;

	private final JdbcTemplate jdbcTemplate;

	public CustomJdbcAuthorizationCodeServices( DataSource dataSource ) {
		this.jdbcTemplate = new JdbcTemplate( dataSource );
	}

	public String createAuthorizationCode( OAuth2Authentication authentication ) {
		String code = UUID.randomUUID().toString();
		jdbcTemplate.update( insertAuthenticationSql,
		                     new Object[] { code, serializeAuthentication( authentication ),
		                                    new Date() }, new int[] {
						Types.VARCHAR, Types.LONGVARBINARY, Types.TIMESTAMP } );
		return code;
	}

	public OAuth2Authentication consumeAuthorizationCode( String code ) throws InvalidGrantException {
		OAuth2Authentication auth;

		try {
			auth = jdbcTemplate.queryForObject( selectAuthenticationSql,
			                                    new RowMapper<OAuth2Authentication>()
			                                    {
				                                    public OAuth2Authentication mapRow( ResultSet rs,
				                                                                        int rowNum )
						                                    throws SQLException {
					                                    byte[] authenticationByteStream = rs.getBytes(
							                                    "authentication" );
					                                    return deserializeAuthentication(
							                                    authenticationByteStream );
				                                    }
			                                    }, code );
		}
		catch ( EmptyResultDataAccessException e ) {
			auth = null;
		}

		if ( auth != null ) {
			jdbcTemplate.update( deleteAuthenticationSql, code );
		}
		else {
			throw new InvalidGrantException( "Invalid authorization code: " + code );
		}
		return auth;
	}

	private byte[] serializeAuthentication( OAuth2Authentication authentication ) {
		for ( OAuth2AuthenticationSerializer serializer : serializers ) {
			if ( serializer.canSerialize( authentication ) ) {
				return serializer.serialize( authentication );
			}
		}
		return SerializationUtils.serialize( authentication );
	}

	@SuppressWarnings("unchecked")
	private OAuth2Authentication deserializeAuthentication( byte[] authentication ) {
		Object object = org.springframework.util.SerializationUtils.deserialize( authentication );
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

	public void setSelectAuthenticationSql( String selectAuthenticationSql ) {
		this.selectAuthenticationSql = selectAuthenticationSql;
	}

	public void setInsertAuthenticationSql( String insertAuthenticationSql ) {
		this.insertAuthenticationSql = insertAuthenticationSql;
	}

	public void setDeleteAuthenticationSql( String deleteAuthenticationSql ) {
		this.deleteAuthenticationSql = deleteAuthenticationSql;
	}
}
