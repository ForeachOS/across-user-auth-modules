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

import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import org.junit.Test;

/**
 * @author Arne Vandamme
 */
public class TestCheckboxFormElement extends AbstractBootstrapViewElementTest
{
	@Test
	public void simple() {
		CheckboxFormElement box = new CheckboxFormElement();
		box.setControlName( "boxName" );
		box.setValue( 123 );
		box.setLabel( "label text" );

		renderAndExpect(
				box,
				"<div class='checkbox'><label>" +
						"<input type='checkbox' id='boxName' name='boxName' value='123' /> label text" +
						"</label>" +
						"<input type='hidden' name='_boxName' value='on' />" +
						"</div>"
		);
	}

	@Test
	public void checked() {
		CheckboxFormElement box = new CheckboxFormElement();
		box.setValue( true );
		box.setChecked( true );

		renderAndExpect(
				box,
				"<div class='checkbox'><label>" +
						"<input type='checkbox' value='true' checked='checked' />" +
						"</label></div>"
		);
	}

	@Test
	public void disabled() {
		CheckboxFormElement box = new CheckboxFormElement();
		box.setValue( "on" );
		box.setDisabled( true );

		renderAndExpect(
				box,
				"<div class='checkbox disabled'><label>" +
						"<input type='checkbox' value='on' disabled='disabled' />" +
						"</label></div>"
		);

		box.setDisabled( false );
		box.setReadonly( true );

		renderAndExpect(
				box,
				"<div class='checkbox'><label>" +
						"<input type='checkbox' value='on' readonly='readonly' />" +
						"</label></div>"
		);
	}

	@Test
	public void additionalLabelText() {
		CheckboxFormElement box = new CheckboxFormElement();
		box.setControlName( "boxName" );
		box.setValue( 123 );
		box.setLabel( "label text" );
		box.add( NodeViewElement.forTag( "strong" ) );

		renderAndExpect(
				box,
				"<div class='checkbox'><label>" +
						"<input type='checkbox' id='boxName' name='boxName' value='123' /> label text<strong></strong>" +
						"</label>" +
						"<input type='hidden' name='_boxName' value='on' />" +
						"</div>"
		);
	}
}
