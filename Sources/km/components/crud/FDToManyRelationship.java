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
	public String fieldName;
	public String inspectorPageName;
	public EOEnterpriseObject record;

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
		a.removeObjectsInArray( selectedObjects() );
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
		NSArray<EOEnterpriseObject> a = (NSArray<EOEnterpriseObject>)record.valueForKeyPath( fieldName );
		return EOSortOrdering.sortedArrayUsingKeyOrderArray( a, new NSArray<EOSortOrdering>( s ) );
	}

	public WOActionResults removeObject() {
		record.removeObjectFromBothSidesOfRelationshipWithKey( currentObject, fieldName );
		ec().saveChanges();
		return context().page();
	}

	public WOActionResults addObject() {
		record.addObjectToBothSidesOfRelationshipWithKey( selectedObjectInPopUp, fieldName );
		ec().saveChanges();
		return context().page();
	}

	public String bgColor() {
		return (tableIndex % 2 == 0) ? "#cccccc" : null;
	}

	public boolean isDisabled() {
		return inspectorPageName == null;
	}

	public WOActionResults selectObject() {
		WOComponent nextPage = pageWithName( inspectorPageName );
		nextPage.takeValueForKey( currentObject, SELECTED_OBJECT );
		return nextPage;
	}
}