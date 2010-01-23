package km.components;

import com.webobjects.appserver.WOContext;

/**
 * Look for our components.
 * 
 * @author Hugi Thordarson
 */

public class KMCommonLook extends KMComponent {

	public KMCommonLook( WOContext context ) {
		super( context );
	}

	@Override
	protected boolean useDefaultComponentCSS() {
		return true;
	}
}