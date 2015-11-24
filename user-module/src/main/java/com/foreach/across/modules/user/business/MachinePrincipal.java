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
package com.foreach.across.modules.user.business;

import com.foreach.across.modules.user.config.UserSchemaConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Represents a SecurityPrincipal identity that is like a user account, but not a physical person.
 *
 * @author Arne Vandamme
 */
@NotThreadSafe
@Entity
@DiscriminatorValue("machine")
@Table(name = UserSchemaConfiguration.TABLE_MACHINE_PRINCIPAL)
public class MachinePrincipal extends GroupedPrincipal<MachinePrincipal> implements Comparable<MachinePrincipal>
{
	@NotBlank
	@Size(max = 100)
	@Column(name = "name", unique = true)
	private String name;

	public String getName() {
		return StringUtils.lowerCase( name );
	}

	public void setName( String name ) {
		this.name = StringUtils.lowerCase( name );
		setPrincipalName( name );
	}

	@Override
	public int compareTo( MachinePrincipal o ) {
		return StringUtils.defaultString( getName() ).compareTo( StringUtils.defaultString( o.getName() ) );
	}
}
