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

package com.foreach.across.samples.user.application.config;

import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.user.business.BasicSecurityPrincipal;
import com.foreach.across.modules.user.business.User;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven Gentens
 * @since 3.1.0
 */
@Configuration
public class UserDirectoryConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		// Show user directory information
		entities.assignableTo( BasicSecurityPrincipal.class )
		        .properties(
				        props -> props.property( "userDirectory" )
				                      .order( 1 )
				                      .readable( true )
				                      .writable( true )

		        )
		        .createFormView( fvb -> fvb.showProperties( "userDirectory", "." ) );

		entities.withType( User.class )
		        .listView(
				        lvb -> lvb.showProperties( "userDirectory", "." )
				                  .entityQueryFilter( eqf -> eqf.showProperties( "userDirectory" ) )
		        );
	}
}
