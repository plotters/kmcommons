package km.components.crud;

import km.components.KMComponent;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;

/**
 * @author Hugi Þórðarson
 */

public class FDEntityList extends KMComponent {

	public String currentAttributeName;
	public String currentEntityName;

	private String _selectedEntityName;

	public ERXGenericRecord currentObject;
	private ERXGenericRecord _selectedObject;

	public FDEntityList( WOContext context ) {
		super( context );
	}

	public NSArray<String> attributeNames() {
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed( selectedEntityName() );
		return entity.classPropertyNames();
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

	public Object currentValue() {
		try {
			Object o = currentObject.valueForKey( currentAttributeName );
			return o;
		}
		catch( Exception e ) {
			System.out.println( e );
			return "???";
		}
	}

	public boolean isImage() {
		return currentValue() instanceof NSData;
	}

	public WOActionResults createObject() {
		ERXGenericRecord eo = (ERXGenericRecord)EOUtilities.createAndInsertInstance( ec(), selectedEntityName() );
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

	/**
	 * @return The total number of objects in the database.
	 */
	public Integer numberOfObjects() {
		return ERXEOControlUtilities.objectCountWithQualifier( ec(), selectedEntityName(), null );
	}
}