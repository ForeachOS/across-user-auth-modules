package com.foreach.across.modules.entity.registry.properties;

import java.util.Collection;

public interface EntityPropertyFilter
{
	boolean shouldInclude( EntityPropertyDescriptor descriptor );

	Collection<String> getPropertyNames();

	/**
	 * Sub interface stating that the property filter declares all included properties explicitly.
	 */
	interface Inclusive extends EntityPropertyFilter {
	}

	/**
	 * Sub interface stating that the property filter only declares properties that should be excluded.
	 */
	interface Exclusive extends EntityPropertyFilter {
	}
}
