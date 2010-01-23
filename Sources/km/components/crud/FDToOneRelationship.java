package km.components.crud;

import is.us.util.USEOUtilities;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

/**
 * 
 * 
 * @author Hugi Thordarson
 */

public class FDToOneRelationship extends WOComponent {

	/**
	 * The Editingcontext to use.
	 * TODO: We should be using the original object's editing context.
	 */
	private EOEditingContext ec = session().defaultEditingContext();

	/**
	 * The object to set a value in.
	 */
	public EOEnterpriseObject object;
	
	/**
	 * The object to set a value in.
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

    public FDToOneRelationship(WOContext context) {
        super(context);
    }
 
    public Object displayString() {
    	return currentObject.valueForKeyPath( displayKey );
    }

    public NSArray<EOEnterpriseObject> objects() {
    	NSArray a = EOUtilities.objectsForEntityNamed( ec, entityName );
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