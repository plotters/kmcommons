package km.components.crud;

import km.components.KMComponent;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.NSArray;

/**
 * @author Hugi Thordarson
 */

public class FDToOneRelationship extends KMComponent {

	/**
	 * The object to set a value in.
	 */
	public EOEnterpriseObject object;

	/**
	 * 
	 */
	public EOEnterpriseObject currentObject;

	/**
	 * Keypath to show.
	 */
	public String displayKey;

	/**
	 * The keypath to set in the object.
	 */
	public String keypath;

	/**
	 * The Entity to fetch objects for.
	 */
	public String entityName;

	public FDToOneRelationship( WOContext context ) {
		super( context );
	}

	public Object displayString() {
		return currentObject.valueForKeyPath( displayKey );
	}

	public NSArray<EOEnterpriseObject> objects() {
		NSArray a = EOUtilities.objectsForEntityNamed( ec(), entityName );
		a = EOSortOrdering.sortedArrayUsingKeyOrderArray( a, new NSArray( new EOSortOrdering( displayKey, EOSortOrdering.CompareAscending ) ) );
		return a;
	}

	public EOEnterpriseObject selectedObject() {
		return (EOEnterpriseObject)object.valueForKeyPath( keypath );
	}

	public void setSelectedObject( EOEnterpriseObject eo ) {
		object.addObjectToBothSidesOfRelationshipWithKey( eo, keypath );
	}
}