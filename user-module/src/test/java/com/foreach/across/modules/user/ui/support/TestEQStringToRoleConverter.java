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

package com.foreach.across.modules.user.ui.support;

import com.foreach.across.modules.entity.query.EQString;
import com.foreach.across.modules.user.business.QRole;
import com.foreach.across.modules.user.business.Role;
import com.foreach.across.modules.user.repositories.RoleRepository;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
@ExtendWith(MockitoExtension.class)
public class TestEQStringToRoleConverter
{
	@Mock
	private RoleRepository roleRepository;

	private EQStringToRoleConverter converter;

	@BeforeEach
	public void reset() {
		converter = new EQStringToRoleConverter( roleRepository );
	}

	@Test
	public void lookupByName() {
		EQString value = new EQString( "admin" );
		Predicate predicate = QRole.role.name.equalsIgnoreCase( "admin" );
		when( roleRepository.findOne( predicate ) ).thenReturn( Optional.of( mock( Role.class ) ) );

		assertNotNull( converter.convert( value ) );
	}
}
