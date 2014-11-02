package com.foreach.across.modules.entity.business;

import java.util.Collection;
import java.util.LinkedList;

public class EntityForm
{
	private Collection<FormElement> elements = new LinkedList<>();

	public void setEntity( Object instance ) {
		for ( FormElement element : elements ) {
			element.setEntity( instance );
		}
	}

	public void addElement( FormElement formElement ) {
		elements.add( formElement );
	}

	public Collection<FormElement> getElements() {
		return elements;
	}
}
