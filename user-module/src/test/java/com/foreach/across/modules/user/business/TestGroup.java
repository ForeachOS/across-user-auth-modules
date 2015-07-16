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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Arne Vandamme
 */
public class TestGroup
{
	@Test
	public void groupDto() {
		Group group = new Group();
		group.setId( 123L );
		group.setName( "group" );
		group.setRoles( Arrays.asList( new Role( "one" ), new Role( "two" ) ) );

		Group dto = group.toDto();
		assertEquals( group, dto );
		assertEquals( group.getName(), dto.getName() );
		assertEquals( group.getRoles(), dto.getRoles() );
		assertNotSame( group.getRoles(), dto.getRoles() );
	}
}
