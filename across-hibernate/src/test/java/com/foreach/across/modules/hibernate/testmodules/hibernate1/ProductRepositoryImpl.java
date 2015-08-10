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
package com.foreach.across.modules.hibernate.testmodules.hibernate1;

import com.foreach.across.modules.hibernate.services.HibernateSessionHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductRepositoryImpl implements ProductRepository
{
	@Autowired
	private HibernateSessionHolder sessionFactory;

	@Transactional(readOnly = true)
	public Product getProductWithId( int id ) {
		return (Product) sessionFactory.getCurrentSession().byId( Product.class ).load( id );
	}

	@Transactional
	public void save( Product product ) {
		sessionFactory.getCurrentSession().saveOrUpdate( product );
	}
}
