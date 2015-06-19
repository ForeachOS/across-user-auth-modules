package com.foreach.across.modules.entity.registry.support;

import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AttributeSupport implements ReadableAttributes, WritableAttributes
{
	private final Map<Object, Object> attributes = new HashMap<>();

	public <Y> void addAttribute( Class<Y> attributeType, Y attributeValue ) {
		Assert.notNull( attributeType );

		attributes.put( attributeType, attributeValue );
	}

	public void addAttribute( String attributeName, Object attributeValue ) {
		Assert.notNull( attributeName );

		attributes.put( attributeName, attributeValue );
	}

	public void addAllAttributes( Map<Object, Object> attributes ) {
		for ( Map.Entry<Object, Object> attribute : attributes.entrySet() ) {
			Assert.notNull( attribute.getKey() );

			if ( attribute.getKey() instanceof String || attribute.getKey() instanceof Class ) {
				this.attributes.put( attribute.getKey(), attribute.getValue() );
			}
			else {
				throw new RuntimeException(
						"Only attributes with a non-null key of type String or Class can be added" );
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <Y> Y removeAttribute( Class<Y> attributeType ) {
		return (Y) attributes.remove( attributeType );
	}

	@SuppressWarnings("unchecked")
	public <Y> Y removeAttribute( String attributeName ) {
		return (Y) attributes.remove( attributeName );
	}

	@SuppressWarnings("unchecked")
	public <Y, V extends Y> V getAttribute( Class<Y> attributeType ) {
		return (V) attributes.get( attributeType );
	}

	@SuppressWarnings("unchecked")
	public <Y> Y getAttribute( String attributeName ) {
		return (Y) attributes.get( attributeName );
	}

	@SuppressWarnings("unchecked")
	public <Y> Y getAttribute( String attributeName, Class<Y> attributeType ) {
		return (Y) attributes.get( attributeName );
	}

	public boolean hasAttribute( Class<?> attributeType ) {
		return attributes.containsKey( attributeType );
	}

	public boolean hasAttribute( String attributeName ) {
		return attributes.containsKey( attributeName );
	}

	public Map<Object, Object> getAttributes() {
		return Collections.unmodifiableMap( attributes );
	}
}
