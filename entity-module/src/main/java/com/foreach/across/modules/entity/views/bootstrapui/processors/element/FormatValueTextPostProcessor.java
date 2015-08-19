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
package com.foreach.across.modules.entity.views.bootstrapui.processors.element;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.web.ui.elements.ConfigurableTextViewElement;

import java.lang.reflect.Array;
import java.text.Format;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Uses a fixed {@link java.text.Format} instance to convert the object to text.
 *
 * @author Arne Vandamme
 */
public class FormatValueTextPostProcessor<T extends ConfigurableTextViewElement> extends AbstractValueTextPostProcessor<T>
{
	private final Format format;

	public FormatValueTextPostProcessor( EntityPropertyDescriptor propertyDescriptor,
	                                     Format format ) {
		super( propertyDescriptor );
		this.format = format;
	}

	@Override
	protected String print( Object value, Locale locale ) {
		if ( format instanceof MessageFormat ) {
			// MessageFormat expects an arry
			if ( !Array.class.isInstance( value ) ) {
				return format.format( new Object[] { value } );
			}
		}
		return format.format( value );
	}
}

