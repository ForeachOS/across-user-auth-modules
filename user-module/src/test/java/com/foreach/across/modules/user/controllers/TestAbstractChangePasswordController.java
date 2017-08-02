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

package com.foreach.across.modules.user.controllers;

import com.foreach.across.modules.user.business.User;
import com.foreach.across.modules.user.events.UserPasswordChangeAllowedEvent;
import com.foreach.across.modules.user.services.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import static com.foreach.across.modules.user.controllers.AbstractChangePasswordController.ERROR_FEEDBACK_MODEL_ATTRIBUTE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Sander Van Loock
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAbstractChangePasswordController
{
	private AbstractChangePasswordController controller;
	@Mock
	private UserService userService;

	@Mock
	private JavaMailSender javaMailSender;
	private ModelMap model;

	@Before
	public void setUp() throws Exception {
		controller = spy( AbstractChangePasswordController.class );
		controller.setJavaMailSender( javaMailSender );
		controller.setUserService( userService );
		model = new ModelMap();

		controller.setConfiguration( ChangePasswordControllerProperties.builder().build() );
	}

	@Test(expected = IllegalArgumentException.class)
	public void validateRequiredProperties() throws Exception {
		controller.setConfiguration( null );
		controller.validateRequiredProperties();
	}

	@Test
	public void defaultChangePasswordTemplateShouldBeReturned() throws Exception {
		String actualTemplate = controller.renderChangePasswordForm( "test@sander.be", model );

		assertEquals( false, model.get( "useEmailLookup" ) );
		assertEquals( ChangePasswordControllerProperties.DEFAULT_CHANGE_PASSWORD_TEMPLATE, actualTemplate );
	}

	@Test
	public void customChangePasswordTemplateShouldBeReturned() throws Exception {
		String expectedForm = "my/custom/form";
		ChangePasswordControllerProperties customConfig =
				ChangePasswordControllerProperties.builder()
				                                  .changePasswordForm( expectedForm )
				                                  .useEmailLookup( true )
				                                  .build();
		controller.setConfiguration( customConfig );
		String actual = controller.renderChangePasswordForm( "test@sander.be", model );

		assertEquals( true, model.get( "useEmailLookup" ) );
		assertEquals( expectedForm, actual );
	}

	@Test
	public void nullEmailShouldNotTriggerChange() throws Exception {
		String actual = controller.requestPasswordChange( null, model );

		assertEquals( "UserModule.web.changePassword.errorFeedback.invalidValue", model.get( ERROR_FEEDBACK_MODEL_ATTRIBUTE ) );
		assertEquals( false, model.get( "useEmailLookup" ) );
		assertEquals( ChangePasswordControllerProperties.DEFAULT_CHANGE_PASSWORD_TEMPLATE, actual );
		verifyNoChangeHappend();
	}

	@Test
	public void emptyEmailShouldNotTriggerChange() throws Exception {
		String actual = controller.requestPasswordChange( "", model );

		assertEquals( "UserModule.web.changePassword.errorFeedback.invalidValue", model.get( ERROR_FEEDBACK_MODEL_ATTRIBUTE ) );
		assertEquals( false, model.get( "useEmailLookup" ) );
		assertEquals( ChangePasswordControllerProperties.DEFAULT_CHANGE_PASSWORD_TEMPLATE, actual );
		verifyNoChangeHappend();
	}

	@Test
	public void unknownUserShouldNotTriggerChange() throws Exception {
		BindingResult bindingResult = mock( BindingResult.class );
		String email = "email";
		when( userService.getUserByUsername( email ) ).thenReturn( null );
		controller.requestPasswordChange( email, model );

		verify( userService, times( 1 ) ).getUserByUsername( "email" );

		verifyNoChangeHappend();

	}

	@Test
	public void invalidUserShouldNotTriggerChange() throws Exception {
		when( controller.retrieveUser( "sander@localhost" ) ).thenReturn( null );

		String actual = controller.requestPasswordChange( "sander@localhost", model );
		assertEquals( "UserModule.web.changePassword.errorFeedback.userNotFound", model.get( ERROR_FEEDBACK_MODEL_ATTRIBUTE ) );
		assertEquals( false, model.get( "useEmailLookup" ) );
		assertEquals( ChangePasswordControllerProperties.DEFAULT_CHANGE_PASSWORD_TEMPLATE, actual );
		verify( userService, times( 1 ) ).getUserByUsername( "sander@localhost" );
		verifyNoChangeHappend();
	}

	@Test
	public void retrieveUserByEmail() throws Exception {
		controller.setConfiguration( ChangePasswordControllerProperties.builder()
		                                                               .useEmailLookup( true )
		                                                               .build() );
		when( userService.getUserByEmail( "sander@localhost" ) ).thenReturn( new User() );
		User user = controller.retrieveUser( "sander@localhost" );

		verify( userService, times( 1 ) ).getUserByEmail( "sander@localhost" );

	}

	@Test
	public void retrieveUserByUsername() throws Exception {
		when( userService.getUserByUsername( "sander@localhost" ) ).thenReturn( new User() );
		User user = controller.retrieveUser( "sander@localhost" );

		verify( userService, times( 1 ) ).getUserByUsername( "sander@localhost" );

	}

	@Test
	public void userCannotChangePasswordShouldNotTriggerChange() throws Exception {
		User user = new User();
		when( controller.retrieveUser( "sander@localhost" ) ).thenReturn( user );
		UserPasswordChangeAllowedEvent allowedEvent = new UserPasswordChangeAllowedEvent( "qsdmlkfk", user, null );
		allowedEvent.setPasswordChangeAllowed( false );
		when( controller.validateUserCanChangePassword( user ) ).thenReturn( allowedEvent );

		String actual = controller.requestPasswordChange( "sander@localhost", model );
		assertEquals( "UserModule.web.changePassword.errorFeedback.userNotAllowedToChangePassword", model.get( ERROR_FEEDBACK_MODEL_ATTRIBUTE ) );
		assertEquals( false, model.get( "useEmailLookup" ) );
		assertEquals( ChangePasswordControllerProperties.DEFAULT_CHANGE_PASSWORD_TEMPLATE, actual );
		verify( userService, times( 1 ) ).getUserByUsername( "sander@localhost" );
		verifyNoChangeHappend();
	}

	private void verifyNoChangeHappend() {
		verifyNoMoreInteractions( userService, javaMailSender );
	}

	private class ChangePasswordController extends AbstractChangePasswordController
	{

	}
}