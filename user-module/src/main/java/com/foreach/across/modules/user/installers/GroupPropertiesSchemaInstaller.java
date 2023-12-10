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
package com.foreach.across.modules.user.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.modules.properties.installers.EntityPropertiesInstaller;
import com.foreach.across.modules.user.config.UserSchemaConfiguration;
import lombok.RequiredArgsConstructor;

/**
 * @author Arne Vandamme
 */
@Installer(description = "Installs the group properties table", version = 1)
public class GroupPropertiesSchemaInstaller extends EntityPropertiesInstaller
{
	@Override
	protected String getTableName() {
		return UserSchemaConfiguration.TABLE_GROUP_PROPERTIES;
	}

	@Override
	protected String getKeyColumnName() {
		return UserSchemaConfiguration.COLUMN_GROUP_ID;
	}
}
