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
package com.foreach.across.modules.bootstrapui.elements;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import static com.foreach.across.modules.bootstrapui.elements.DateTimeFormElementConfiguration.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
public class TestDateTimeFormElementConfiguration
{
	private static final Date PRINT_DATE;

	static {
		try {
			PRINT_DATE = DateUtils.parseDate( "2015-08-07 10:31:22", "yyyy-MM-dd HH:mm:ss" );
		}
		catch ( ParseException pe ) {
			throw new RuntimeException( pe );
		}
	}

	@Test
	public void newConfiguration() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();
		assertEquals( Format.DATETIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( "input[type=text]", configuration.get( "datepickerInput" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "nl-BE", localized.get( "locale" ) );

		assertEquals( "07-Aug-2015 10:31", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "7-aug-2015 10:31", localized.createDateFormat().format( PRINT_DATE ) );

		configuration.setLocalizePatterns( false );
		assertEquals( "en-GB", configuration.localize( Locale.forLanguageTag( "nl-BE" ) ).get( "locale" ) );
	}

	@Test
	public void datetimeFull() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.DATETIME_FULL
		);
		assertEquals( Format.DATETIME_FULL, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME_FULL, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( "input[type=text]", configuration.get( "datepickerInput" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_PATTERN_DATETIME,
				               FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "07 August 2015 10:31:22", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "7 augustus 2015 10:31:22", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void date() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.DATE );
		assertEquals( Format.DATE, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATE, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( "input[type=text]", configuration.get( "datepickerInput" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATE, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "07-Aug-2015", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "7-aug-2015", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void dateFull() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.DATE_FULL );
		assertEquals( Format.DATE_FULL, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATE_FULL, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( "input[type=text]", configuration.get( "datepickerInput" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATE, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "Friday, 7 August 2015", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "vrijdag 7 augustus 2015", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void time() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.TIME );
		assertEquals( Format.TIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_TIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( "input[type=text]", configuration.get( "datepickerInput" ) );
		assertEquals( FMT_EXPORT_MOMENT_TIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "10:31", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "10:31", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void customAttributes() throws ParseException {
		Date start = DateUtils.parseDate( "2015-08-07 10:31", "yyyy-MM-dd HH:mm" );
		Date end = DateUtils.parseDate( "2015-08-08 10:31", "yyyy-MM-dd HH:mm" );

		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();
		configuration.setMinDate( start );
		configuration.setMaxDate( end );
		configuration.setShowClearButton( true );

		assertEquals( Format.DATETIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( "input[type=text]", configuration.get( "datepickerInput" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		assertEquals( "2015-08-07 10:31", configuration.get( "minDate" ) );
		assertEquals( "2015-08-08 10:31", configuration.get( "maxDate" ) );
		assertEquals( true, configuration.get( "showClear" ) );
	}
}
