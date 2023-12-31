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

package com.foreach.across.modules.ldap.business;

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.ldap.repositories.HibernateLdapConnectorType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * Represents a connection to an Ldap service
 *
 * @author Marc Vanbrabant
 * @since 1.0.0
 */
@NotThreadSafe
@Entity
@Table(name = "ldap_connector")
@Data
@EqualsAndHashCode(callSuper = true)
public class LdapConnector extends SettableIdAuditableEntity<LdapConnector>
{

	@Id
	@GeneratedValue(generator = "seq_ldap_connector_id")
	@GenericGenerator(
			name = "seq_ldap_connector_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_ldap_connector_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@NotBlank
	@Column(name = "name")
	@Size(max = 255)
	private String name;

	@NotBlank
	@Column(name = "hostname")
	@Size(max = 255)
	private String hostName;

	@Column(name = "port")
	@NotNull
	@Min(0)
	@Max(65535)
	private Integer port = 389;

	@Column(name = "connector_type")
	@NotNull
	@Type(type = HibernateLdapConnectorType.CLASS_NAME)
	private LdapConnectorType ldapConnectorType;

	@Column(name = "read_timeout")
	@Min(0)
	private Integer readTimeout;

	@Column(name = "search_timeout")
	@Min(0)
	private Integer searchTimeout = 0;

	@Column(name = "connection_timeout")
	@Min(0)
	private Integer connectionTimeout;

	@Column(name = "username")
	@Size(max = 300)
	private String username;

	@Column(name = "conn_pwd")
	@Size(max = 100)
	private String password;

	@Column(name = "base_dn")
	@Size(max = 255)
	private String baseDn;

	@Column(name = "additional_user_dn")
	@Size(max = 255)
	private String additionalUserDn;

	@Column(name = "additional_group_dn")
	@Size(max = 255)
	private String additionalGroupDn;

	@Column(name = "authenticate_as")
	@Size(max = 255)
	private String synchronizationPrincipalName;
}
