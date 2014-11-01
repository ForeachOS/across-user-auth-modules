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
package com.foreach.across.modules.user.config.modules;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.user.controllers.GroupController;
import com.foreach.across.modules.user.controllers.RoleController;
import com.foreach.across.modules.user.controllers.UserController;
import com.foreach.across.modules.user.handlers.AdminWebEventsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AcrossDepends(required = "AdminWebModule")
public class UserAdminWebConfiguration
{
	@Bean
	public RoleController roleController() {
		return new RoleController();
	}

	@Bean
	public UserController userController() {
		return new UserController();
	}

	@Bean
	public GroupController groupController() {
		return new GroupController();
	}

	@Bean
	public AdminWebEventsHandler adminWebEventsHandler() {
		return new AdminWebEventsHandler();
	}
}
