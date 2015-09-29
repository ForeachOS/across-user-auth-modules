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
package com.foreach.across.modules.entity.testmodules.springdata.business;

import org.hibernate.validator.constraints.Length;

import javax.persistence.Embeddable;

/**
 * @author Arne Vandamme
 */
@Embeddable
public class Address
{
	@Length(max = 100)
	private String street;

	private int zipCode;

	public String getStreet() {
		return street;
	}

	public void setStreet( String street ) {
		this.street = street;
	}

	public int getZipCode() {
		return zipCode;
	}

	public void setZipCode( int zipCode ) {
		this.zipCode = zipCode;
	}
}
