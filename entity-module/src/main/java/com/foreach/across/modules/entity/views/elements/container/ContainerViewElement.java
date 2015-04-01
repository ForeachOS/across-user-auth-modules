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
package com.foreach.across.modules.entity.views.elements.container;

import com.foreach.across.modules.entity.views.elements.CommonViewElements;
import com.foreach.across.modules.entity.views.elements.ViewElement;
import com.foreach.across.modules.entity.views.elements.ViewElements;

/**
 * Simplest extension of {@link com.foreach.across.modules.entity.views.elements.ViewElements} that also
 * implements {@link com.foreach.across.modules.entity.views.elements.ViewElement}.  A container is a named
 * collection of elements that usually does not add additional styling.
 *
 * @author Arne Vandamme
 */
public class ContainerViewElement extends ViewElements implements ViewElement
{
	public static final String TYPE = CommonViewElements.CONTAINER;

	private String elementType = TYPE;
	private String name, label = "", customTemplate;

	public ContainerViewElement() {
	}

	public ContainerViewElement( String name ) {
		this.name = name;
	}

	@Override
	public String getElementType() {
		return elementType;
	}

	public void setElementType( String elementType ) {
		this.elementType = elementType;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	@Override
	public String getCustomTemplate() {
		return customTemplate;
	}

	public void setCustomTemplate( String customTemplate ) {
		this.customTemplate = customTemplate;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void setLabel( String label ) {
		this.label = label;
	}

	@Override
	public boolean isField() {
		return false;
	}

	@Override
	public Object value( Object entity ) {
		return null;
	}

	@Override
	public String print( Object entity ) {
		return null;
	}
}
