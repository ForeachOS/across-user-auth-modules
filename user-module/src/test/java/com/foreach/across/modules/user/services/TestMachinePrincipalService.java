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

package com.foreach.across.modules.user.services;

import com.foreach.across.modules.spring.security.infrastructure.services.SecurityPrincipalService;
import com.foreach.across.modules.user.business.InternalUserDirectory;
import com.foreach.across.modules.user.business.MachinePrincipal;
import com.foreach.across.modules.user.business.UserDirectory;
import com.foreach.across.modules.user.repositories.MachinePrincipalRepository;
import com.foreach.across.modules.user.services.support.DefaultUserDirectoryStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestMachinePrincipalService.Config.class)
public class TestMachinePrincipalService extends AbstractQueryDslPredicateExecutorTest
{
	@Autowired
	private DefaultUserDirectoryStrategy defaultUserDirectoryStrategy;

	@Autowired
	private MachinePrincipalRepository machinePrincipalRepository;

	@Autowired
	private MachinePrincipalService machinePrincipalService;

	@Autowired
	private SecurityPrincipalService securityPrincipalService;

	@Test
	public void serviceDelegatesToRepositoryForQueryDslPredicateExecutor() {
		queryDslPredicateExecutorTest( machinePrincipalService, machinePrincipalRepository );
	}

	@Test
	public void getMachineByNameShouldUseDefaultDirectory() {
		UserDirectory dir = new InternalUserDirectory();
		dir.setId( 123L );
		when( defaultUserDirectoryStrategy.getDefaultUserDirectory() ).thenReturn( dir );

		MachinePrincipal expected = new MachinePrincipal();
		expected.setName( "expected" );

		when( securityPrincipalService.getPrincipalByName( "123@@@expected" ) ).thenReturn( Optional.of( expected ) );

		assertEquals( Optional.of( expected ), machinePrincipalService.getMachinePrincipalByName( "EXPECTED" ) );
	}

	@Test
	public void userDirectoryStrategyShouldBeAppliedBeforeRepositoryCall() {
		MachinePrincipal one = new MachinePrincipal();
		one.setId( 1L );
		MachinePrincipal two = new MachinePrincipal();
		two.setId( 2L );
		assertNotEquals( one, two );

		doAnswer(
				invocationOnMock -> {
					( (MachinePrincipal) invocationOnMock.getArguments()[0] ).setId( 2L );
					return null;
				}
		).when( defaultUserDirectoryStrategy ).apply( one );

		when( machinePrincipalRepository.save( two ) ).thenReturn( two );

		machinePrincipalService.save( one );

		verify( machinePrincipalRepository ).save( two );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public SecurityPrincipalService securityPrincipalService() {
			return mock( SecurityPrincipalService.class );
		}

		@Bean
		public MachinePrincipalRepository machinePrincipalRepository() {
			return mock( MachinePrincipalRepository.class );
		}

		@Bean
		public DefaultUserDirectoryStrategy userDirectoryStrategy() {
			return mock( DefaultUserDirectoryStrategy.class );
		}

		@Bean
		public MachinePrincipalService machinePrincipalService() {
			return new MachinePrincipalServiceImpl();
		}
	}
}
