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
package com.foreach.across.modules.user.extensions;

import com.foreach.across.modules.logging.method.MethodLoggerAdapter;
import com.foreach.across.modules.user.UserModule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Arne Vandamme
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SuppressWarnings("unused")
public class MethodLoggingConfiguration
{
	@Bean
	public MethodExecutionLogger methodExecutionLogger() {
		return new MethodExecutionLogger();
	}

	@Aspect
	class MethodExecutionLogger extends MethodLoggerAdapter
	{
		public MethodExecutionLogger() {
			super( UserModule.NAME );
		}

		@Around("serviceMethod() || repositoryMethod()")
		@Override
		protected Object proceedAndLogExecutionTime( ProceedingJoinPoint point ) throws Throwable {
			return super.proceedAndLogExecutionTime( point );
		}

		@Pointcut("execution(* com.foreach.across.modules.user.services..*.*(..))")
		public void serviceMethod() {
		}

		@Pointcut("execution(* com.foreach.across.modules.user.repositories..*.*(..))")
		public void repositoryMethod() {
		}
	}
}
