package km.components.crud;

import km.components.KMComponent;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.*;

import er.extensions.eof.ERXGenericRecord;

/**
 * @author Hugi Þórðarson
 */

public class FDEntityList extends KMComponent {

	public String currentEntityName;

	private String _selectedEntityName;

	public ERXGenericRecord currentObject;
	private ERXGenericRecord _selectedObject;

	public FDEntityList( WOContext context ) {
		super( context );
	}

	/**
	 * The names of all entities in the system. 
	 */
	public NSArray<String> entityNames() {
		NSArray<EOModel> models = EOModelGroup.defaultGroup().models();

		NSMutableArray<String> entityNames = new NSMutableArray<String>();

		for( EOModel model : models ) {

			for( EOEntity entity : model.entities() ) {
				if( !entity.isAbstractEntity() ) {
					entityNames.addObject( entity.name() );
				}
			}
		}

		return entityNames;
	}

	/**
	 * All the objects for the selected entity. 
	 */
	public NSArray<ERXGenericRecord> allObjects() {
		if( selectedEntityName() != null )
			return EOUtilities.objectsForEntityNamed( ec(), selectedEntityName() );

		return NSArray.emptyArray();
	}

	/**
	 * The selected entity. 
	 */
	public WOActionResults selectEntity() {
		setSelectedEntityName( currentEntityName );
		return context().page();
	}

	/**
	 * The selected entity. 
	 */
	public WOActionResults selectObject() {
		FDEditObject nextPage = pageWithName( FDEditObject.class );
		nextPage.setSelectedObject( currentObject );
		nextPage.setPageToReturnTo( this );
		return nextPage;
	}

	public String objectName() {
		try {
			// FIXME: This is ugly.
			return (String)currentObject.valueForKey( "name" );
		}
		catch( Exception e ) {
			return "(enginn heitisreitur fannst)";
		}
	}

	public WOActionResults createObject() {
		ERXGenericRecord eo = (ERXGenericRecord) EOUtilities.createAndInsertInstance( ec(), selectedEntityName() );
		ec().saveChanges();

		FDEditObject nextPage = pageWithName( FDEditObject.class );
		nextPage.setSelectedObject( eo );
		nextPage.setPageToReturnTo( this );
		return nextPage;
	}

	public void setSelectedEntityName( String _selectedEntityName ) {
		this._selectedEntityName = _selectedEntityName;
	}

	public String selectedEntityName() {
		return _selectedEntityName;
	}

	public void setSelectedObject( ERXGenericRecord selectedObject ) {
		this._selectedObject = selectedObject;
	}

	public ERXGenericRecord selectedObject() {
		return _selectedObject;
	}
}