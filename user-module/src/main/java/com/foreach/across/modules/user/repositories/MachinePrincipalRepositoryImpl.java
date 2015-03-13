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
package com.foreach.across.modules.user.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepositoryImpl;
import com.foreach.across.modules.user.business.MachinePrincipal;
import com.foreach.across.modules.user.converters.FieldUtils;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Arne Vandamme
 */
@Repository
public class MachinePrincipalRepositoryImpl extends BasicRepositoryImpl<MachinePrincipal> implements MachinePrincipalRepository
{
	@Transactional(readOnly = true)
	@Override
	public MachinePrincipal getByName( String name ) {
		return (MachinePrincipal) distinct()
				.add( Restrictions.eq( "principalName", FieldUtils.lowerCase( name ) ) )
				.uniqueResult();
	}
}