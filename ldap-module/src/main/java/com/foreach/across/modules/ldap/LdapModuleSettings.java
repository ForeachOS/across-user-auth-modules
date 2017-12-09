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

package com.foreach.across.modules.ldap;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Marc Vanbrabant
 * @since 1.0.0
 */
@ConfigurationProperties("ldapModule")
@Data
@SuppressWarnings("unused")
public class LdapModuleSettings
{
	public static final String DISABLE_SYNCHRONIZATION_TASK = "ldapModule.disableSynchronizationTask";
	public static final String SYNCHRONIZATION_TASK_INTERVAL_IN_SECONDS =
			"ldapModule.synchronizationTaskIntervalInSeconds";

	private boolean disableSynchronizationTask;
	private long synchronizationTaskIntervalInSeconds = 300;
}
