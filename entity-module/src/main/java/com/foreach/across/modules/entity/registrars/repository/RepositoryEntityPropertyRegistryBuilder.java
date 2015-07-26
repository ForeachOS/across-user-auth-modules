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
package com.foreach.across.modules.entity.registrars.repository;

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.*;
import com.foreach.across.modules.entity.registry.properties.meta.PropertyPersistenceMetadata;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.repository.core.support.RepositoryFactoryInformation;

import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.validation.metadata.BeanDescriptor;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Creates a {@link com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry} for a
 * {@link org.springframework.data.repository.core.support.RepositoryFactoryInformation} bean.</p>
 * <p>Puts every EntityPropertyRegistry in the central registry so properties of associated entities
 * can be determined as well.</p>
 */
public class RepositoryEntityPropertyRegistryBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger( RepositoryEntityPropertyRegistryBuilder.class );

	private final BeanMetaDataManager metaDataManager = new BeanMetaDataManager(
			new ConstraintHelper(), Collections.<MetaDataProvider>emptyList()
	);

	@Autowired
	private EntityPropertyRegistries entityPropertyRegistries;

	public <T> void buildEntityPropertyRegistry( MutableEntityConfiguration<T> entityConfiguration ) {
		Class<T> entityType = entityConfiguration.getEntityType();
		RepositoryFactoryInformation<T, ?> repositoryFactoryInformation
				= entityConfiguration.getAttribute( RepositoryFactoryInformation.class );

		MutableEntityPropertyRegistry registry =
				(MutableEntityPropertyRegistry) entityPropertyRegistries.getRegistry( entityType );

		registry.setDefaultOrder( new EntityPropertyComparators.Ordered() );

		setBeanDescriptor( entityConfiguration );

		// add @Embedded
		PersistentEntity<?, ?> persistentEntity = repositoryFactoryInformation.getPersistentEntity();
		initializePersistentEntity( registry, persistentEntity );
		configureEmbeddedProperties( registry, persistentEntity );

		configureSortableProperties( registry, persistentEntity );
		configureDefaultFilter( entityType, registry );
		configureKnownDescriptors( entityType, registry );

		entityConfiguration.setPropertyRegistry( registry );
	}

	private void configureDefaultFilter( Class<?> entityType, MutableEntityPropertyRegistry registry ) {
		if ( registry.getDefaultFilter() == null ) {
			List<String> excludedProps = new LinkedList<>();
			excludedProps.add( "class" );

//			if ( Persistable.class.isAssignableFrom( entityType ) ) {
//				excludedProps.add( "new" );
//			}
//
//			if ( SettableIdBasedEntity.class.isAssignableFrom( entityType ) ) {
//				excludedProps.add( "newEntityId" );
//			}

			//registry.setDefaultFilter( EntityPropertyFilters.exclude( excludedProps ) );
		}

		registry.getMutableProperty( "class" ).setHidden( true );
	}

	private void configureKnownDescriptors( Class<?> entityType, MutableEntityPropertyRegistry registry ) {

//		if ( Persistable.class.isAssignableFrom( entityType ) ) {
//			registry.getMutableProperty( "id" ).setHidden( true );
//		}
//
//		if ( SettableIdBasedEntity.class.isAssignableFrom( entityType ) ) {
//			MutableEntityPropertyDescriptor mutable = registry.getMutableProperty( "newEntityId" );
//			mutable.setReadable( false );
//			mutable.setHidden( true );
//		}
	}

	private void initializePersistentEntity( MutableEntityPropertyRegistry registry,
	                                         PersistentEntity<?, ?> persistentEntity ) {
		for ( EntityPropertyDescriptor descriptor : registry.getRegisteredDescriptors() ) {
			PersistentProperty persistentProperty = persistentEntity.getPersistentProperty( descriptor.getName() );
			if ( persistentProperty != null ) {
				MutableEntityPropertyDescriptor mutable = registry.getMutableProperty( descriptor.getName() );
				if ( mutable != null ) {
					mutable.setAttribute( PersistentProperty.class, persistentProperty );
					// todo: remove
					if ( persistentProperty.isAnnotationPresent( Embedded.class )
							|| persistentProperty.isAnnotationPresent( EmbeddedId.class ) ) {
						mutable.setAttribute( EntityAttributes.PROPERTY_PERSISTENCE_METADATA,
						                      new PropertyPersistenceMetadata() );
					}
				}
			}
		}
	}

	private void configureEmbeddedProperties( MutableEntityPropertyRegistry registry,
	                                          PersistentEntity<?, ?> persistentEntity ) {
		LOG.trace( "Finding embedded properties for entity {}", persistentEntity.getType() );
		for ( EntityPropertyDescriptor descriptor : registry.getRegisteredDescriptors() ) {
			PersistentProperty persistentProperty = persistentEntity.getPersistentProperty( descriptor.getName() );
			if ( persistentProperty != null && ( persistentProperty.isAnnotationPresent( Embedded.class )
					|| persistentProperty.isAnnotationPresent( EmbeddedId.class ) ) ) {
				LOG.trace( "Setting persisted property {} as embedded", persistentProperty.getName() );
				MutableEntityPropertyDescriptor mutable = registry.getMutableProperty( descriptor.getName() );
				if ( mutable != null ) {
					PropertyPersistenceMetadata propertyPersistenceMetadata = mutable.getAttribute(
							EntityAttributes.PROPERTY_PERSISTENCE_METADATA, PropertyPersistenceMetadata.class );
					propertyPersistenceMetadata.setEmbedded( true );
				}
			}
		}
	}

	private void configureSortableProperties( MutableEntityPropertyRegistry registry,
	                                          PersistentEntity<?, ?> persistentEntity ) {
		LOG.trace( "Finding sortable properties for entity {}", persistentEntity.getType() );

		// A property is sortable by default if it is persisted
		for ( EntityPropertyDescriptor descriptor : registry.getRegisteredDescriptors() ) {
			PersistentProperty persistentProperty = persistentEntity.getPersistentProperty( descriptor.getName() );

			if ( persistentProperty != null && !persistentProperty.isTransient() ) {
				LOG.trace( "Setting persisted property {} as sortable", persistentProperty.getName() );

				MutableEntityPropertyDescriptor mutable = registry.getMutableProperty( descriptor.getName() );

				if ( mutable != null ) {
					mutable.setAttribute( EntityAttributes.SORTABLE_PROPERTY, persistentProperty.getName() );
				}
			}
		}
	}

	private void setBeanDescriptor( MutableEntityConfiguration<?> entityConfiguration ) {
		BeanMetaData<?> metaData = metaDataManager.getBeanMetaData( entityConfiguration.getEntityType() );
		BeanDescriptor beanDescriptor = metaData.getBeanDescriptor();

		if ( beanDescriptor != null ) {
			entityConfiguration.setAttribute( BeanDescriptor.class, beanDescriptor );
		}
	}
}
