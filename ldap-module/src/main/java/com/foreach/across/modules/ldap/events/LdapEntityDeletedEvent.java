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

package com.foreach.across.modules.ldap.events;

import com.foreach.across.core.events.AcrossEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

@Getter
@AllArgsConstructor
public class LdapEntityDeletedEvent<T> implements AcrossEvent, ResolvableTypeProvider
{
	private T entity;

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics( LdapEntityDeletedEvent.class, ResolvableType.forInstance( entity ) );
	}
}
