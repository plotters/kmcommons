package km.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

/**
 * Look for our components.
 * 
 * @author Hugi Thordarson
 */

public class KMCommonLook extends ERXComponent {

	public KMCommonLook( WOContext context ) {
		super( context );
	}

	@Override
	protected boolean useDefaultComponentCSS() {
		return true;
	}
}