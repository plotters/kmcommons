package km.components.crud;

import km.components.KMComponent;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Inspects a to-many relationship, allowing editing, addition and removal of objects.
 * 
 * @author Hugi Þórðarson
 */

public class FDToManyRelationship extends KMComponent {

	private static final String SELECTED_OBJECT = "selectedObject";
	public String entityName;
	public String displayKey;
	public String keypath;
	public String inspectorPageName;
	public EOEnterpriseObject object;
	public boolean allowDuplicates = false;

	public EOEnterpriseObject currentObjectInPopUp;
	public EOEnterpriseObject selectedObjectInPopUp;

	public EOEnterpriseObject currentObject;
	public int tableIndex;

	public FDToManyRelationship( WOContext context ) {
		super( context );
	}

	public NSArray<EOEnterpriseObject> allObjects() {
		EOSortOrdering s = new EOSortOrdering( displayKey, EOSortOrdering.CompareAscending );
		NSMutableArray<EOEnterpriseObject> a = EOUtilities.objectsForEntityNamed( ec(), entityName ).mutableClone();

		if( !allowDuplicates ) {
			a.removeObjectsInArray( selectedObjects() );
		}

		return EOSortOrdering.sortedArrayUsingKeyOrderArray( a, new NSArray<EOSortOrdering>( s ) );
	}

	public Object popUpDisplayString() {
		return currentObjectInPopUp.valueForKeyPath( displayKey );
	}

	public Object displayString() {
		return currentObject.valueForKeyPath( displayKey );
	}

	public NSArray<EOEnterpriseObject> selectedObjects() {
		EOSortOrdering s = new EOSortOrdering( displayKey, EOSortOrdering.CompareAscending );
		NSArray<EOEnterpriseObject> a = (NSArray<EOEnterpriseObject>)object.valueForKeyPath( keypath );
		return EOSortOrdering.sortedArrayUsingKeyOrderArray( a, new NSArray<EOSortOrdering>( s ) );
	}

	public WOActionResults removeObject() {
		object.removeObjectFromBothSidesOfRelationshipWithKey( currentObject, keypath );
		ec().saveChanges();
		return context().page();
	}

	public WOActionResults addObject() {
		object.addObjectToBothSidesOfRelationshipWithKey( selectedObjectInPopUp, keypath );
		ec().saveChanges();
		return context().page();
	}

	/**
	 * @return True, if there's no inspector page set.
	 */
	public boolean isDisabled() {
		return inspectorPageName == null;
	}

	/**
	 * Selects the current object in the list.
	 */
	public WOActionResults selectObject() {
		WOComponent nextPage = pageWithName( inspectorPageName );
		nextPage.takeValueForKey( currentObject, SELECTED_OBJECT );
		return nextPage;
	}
}