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
package com.foreach.across.modules.entity.newviews.bootstrapui;

import com.foreach.across.modules.entity.newviews.EntityViewElementBuilderFactory;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.mysema.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;

import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
public abstract class ViewElementBuilderFactoryTestSupport<T extends ViewElement>
{
	@Autowired
	protected EntityConfiguration entityConfiguration;

	@Autowired
	protected EntityPropertyRegistry registry;

	@Autowired
	protected EntityRegistry entityRegistry;

	protected ViewElementBuilderContext builderContext;
	protected Map<String, EntityPropertyDescriptor> properties = new HashMap<>();

	@Before
	@SuppressWarnings("unchecked")
	public void before() {
		reset( entityConfiguration, registry );

		builderContext = mock( ViewElementBuilderContext.class );

		EntityMessageCodeResolver codeResolver = mock( EntityMessageCodeResolver.class );

		when( entityConfiguration.getEntityMessageCodeResolver() ).thenReturn( codeResolver );

		if ( properties.isEmpty() ) {
			BeanMetaDataManager manager = new BeanMetaDataManager(
					new ConstraintHelper(), Collections.<MetaDataProvider>emptyList()
			);

			BeanMetaData<?> metaData = manager.getBeanMetaData( getTestClass() );
			BeanDescriptor beanDescriptor = metaData.getBeanDescriptor();

			for ( Field field : ReflectionUtils.getFields( getTestClass() ) ) {
				String propertyName = field.getName();
				PropertyDescriptor validationDescriptor = beanDescriptor.getConstraintsForProperty( field.getName() );

				EntityPropertyDescriptor descriptor = mock( EntityPropertyDescriptor.class );
				when( descriptor.getName() ).thenReturn( propertyName );
				when( descriptor.getDisplayName() ).thenReturn( StringUtils.lowerCase( propertyName ) );
				when( descriptor.getAttribute( PropertyDescriptor.class ) ).thenReturn( validationDescriptor );
				when( descriptor.getPropertyType() ).thenReturn( (Class) field.getType() );
				TypeDescriptor typeDescriptor = new TypeDescriptor( field );
				when( descriptor.getPropertyTypeDescriptor() ).thenReturn( typeDescriptor );

				when( codeResolver.getMessageWithFallback(
						"properties." + field.getName(),
						StringUtils.lowerCase( propertyName )
				) )
						.thenReturn( "resolved: " + StringUtils.lowerCase( propertyName ) );

				properties.put( propertyName, descriptor );
			}
		}
	}

	protected abstract Class getTestClass();

	protected <V extends T> V assemble( String propertyName ) {
		return assemble( properties.get( propertyName ) );
	}

	@SuppressWarnings("unchecked")
	protected <V extends T> V assemble( EntityPropertyDescriptor descriptor ) {
		return (V) builderFactory()
				.createBuilder( descriptor, registry, entityConfiguration )
				.build( builderContext );
	}

	protected abstract EntityViewElementBuilderFactory builderFactory();
}
