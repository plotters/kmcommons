package km.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.components.ERXComponent;

/**
 * Common base class for KM components.
 * 
 * @author Hugi Þórðarson
 */

public abstract class KMComponent extends ERXComponent {

	public KMComponent( WOContext context ) {
		super( context );
	}

	public EOEditingContext ec() {
		return session().defaultEditingContext();
	}
}
