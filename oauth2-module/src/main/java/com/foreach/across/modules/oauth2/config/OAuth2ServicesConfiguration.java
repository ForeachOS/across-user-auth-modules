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
package com.foreach.across.modules.oauth2.config;

import com.foreach.across.modules.oauth2.business.OAuth2Client;
import com.foreach.across.modules.oauth2.services.ClientDetailsServiceImpl;
import com.foreach.across.modules.oauth2.services.OAuth2Service;
import com.foreach.across.modules.oauth2.services.OAuth2ServiceImpl;
import com.foreach.across.modules.user.services.support.ExpressionBasedSecurityPrincipalLabelResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetailsService;

@Configuration
public class OAuth2ServicesConfiguration
{
	@Bean
	public OAuth2Service oAuth2Service() {
		return new OAuth2ServiceImpl();
	}

	@Bean(name = "oAuth2ClientDetailsService")
	public ClientDetailsService clientDetailsService() {
		return new ClientDetailsServiceImpl();
	}

	@Bean
	public ExpressionBasedSecurityPrincipalLabelResolver oAuth2ClientLabelResolver() {
		return new ExpressionBasedSecurityPrincipalLabelResolver( OAuth2Client.class, "clientId" );
	}
}
