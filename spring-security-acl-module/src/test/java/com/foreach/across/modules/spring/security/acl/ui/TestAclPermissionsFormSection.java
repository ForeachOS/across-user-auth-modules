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

package com.foreach.across.modules.spring.security.acl.ui;

import com.foreach.across.modules.spring.security.acl.business.AclPermission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class TestAclPermissionsFormSection
{
	@Test
	public void defaultValues() {
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> AclPermissionsFormSection.builder().build() )
				.withMessage( "name is marked non-null but is null" );
	}

	@Test
	public void permissions() {
		AclPermissionsFormSection section = AclPermissionsFormSection
				.builder()
				.name( "my-section" )
				.permissions( AclPermission.READ, AclPermission.WRITE )
				.build();

		assertThat( section.getPermissionGroupsSupplier() ).isNotNull();
		assertThat( section.getPermissionGroupsSupplier().get() )
				.hasSize( 1 )
				.satisfies(
						group -> assertThat( group[0].getPermissionsSupplier().get() ).containsExactly( AclPermission.READ, AclPermission.WRITE )
				);
	}
}
