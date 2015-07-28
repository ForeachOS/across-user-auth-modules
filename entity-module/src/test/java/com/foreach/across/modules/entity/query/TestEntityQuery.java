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
package com.foreach.across.modules.entity.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestEntityQuery
{
	@Test
	public void singlePropertyToString() {
		EntityQuery query = EntityQuery.and( new EntityQueryCondition( "name", EntityQueryOps.EQ, "myName" ) );

		assertEquals( "(name = 'myName')", query.toString() );
	}

	@Test
	public void nestedExpressionsToString() {
		EntityQuery query = new EntityQuery( EntityQueryOps.AND );
		query.add( new EntityQueryCondition( "name", EntityQueryOps.EQ, "someName" ) );
		query.add( new EntityQueryCondition( "city", EntityQueryOps.NEQ, 217 ) );

		EntityQuery subQuery = EntityQuery.or(
				new EntityQueryCondition( "email", EntityQueryOps.CONTAINS, "emailOne" ),
				new EntityQueryCondition( "email", EntityQueryOps.EQ, "emailTwo" )
		);

		query.add( subQuery );

		assertEquals( "(name = 'someName' and city != 217 and (email contains 'emailOne' or email = 'emailTwo'))",
		              query.toString() );
	}

	@Test
	public void simpleToAndFromJson() throws Exception {
		EntityQuery query = new EntityQuery( EntityQueryOps.AND );
		query.add( new EntityQueryCondition( "name", EntityQueryOps.EQ, "someName" ) );
		query.add( new EntityQueryCondition( "city", EntityQueryOps.NEQ, 217 ) );

		EntityQuery subQuery = EntityQuery.or(
				new EntityQueryCondition( "email", EntityQueryOps.CONTAINS, "emailOne" ),
				new EntityQueryCondition( "email", EntityQueryOps.EQ, "emailTwo" )
		);
		query.add( subQuery );

		String value = new ObjectMapper().writeValueAsString( query );

		EntityQuery q = new ObjectMapper().readValue( value, EntityQuery.class );
		assertEquals( "(name = 'someName' and city != 217 and (email contains 'emailOne' or email = 'emailTwo'))",
		              q.toString() );
	}
}
