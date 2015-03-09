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
package com.foreach.across.modules.metrics;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.debugweb.DebugWebModule;

/**
 * @author Marc Vanbrabant
 */
@AcrossDepends( optional = DebugWebModule.NAME )
public class MetricModule extends AcrossModule
{
	public static final String NAME = "MetricModule";
	public static final String RESOURCES = "metrics";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getResourcesKey() {
		return RESOURCES;
	}

	@Override
	public String getDescription() {
		return "Provides metrics.";
	}
}
