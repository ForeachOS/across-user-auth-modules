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
package com.foreach.across.modules.entity.views.support;

/**
 * @author Arne Vandamme
 */
public class NestedValueFetcher implements ValueFetcher<Object>
{
	private final ValueFetcher parent, child;

	public NestedValueFetcher( ValueFetcher parent, ValueFetcher child ) {
		this.parent = parent;
		this.child = child;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public Object getValue( Object entity ) {
		Object topLevelValue = parent.getValue( entity );
		return topLevelValue != null ? child.getValue( topLevelValue ) : null;
	}
}
