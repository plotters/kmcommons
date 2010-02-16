package km.components.crud;

import is.us.wo.util.USHTTPUtilities;

import java.text.Format;

import km.components.KMComponent;

import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

import er.extensions.eof.ERXGenericRecord;

/**
 * Edits a single EO.
 * 
 * @author Hugi Þórðarson
 * 
 * TODO: Add handling of data attributes.
 */

public class FDEditObject extends KMComponent {

	private static final String INTEGER_CLASS = "java.lang.Integer";
	private static final String TIMESTAMP_CLASS = "com.webobjects.foundation.NSTimestamp";
	private static final String STRING_CLASS = "java.lang.String";
	private static final String DATA_CLASS = "com.webobjects.foundation.NSData";

	private ERXGenericRecord _selectedObject;
	private WOComponent _pageToReturnTo;

	public EOAttribute currentAttribute;
	public EORelationship currentRelationship;
	public String filename;

	public FDEditObject( WOContext context ) {
		super( context );
	}

	/**
	 * Editable attributes (excluding PK attributes)
	 */
	public NSArray<EOAttribute> attributes() {

		String entityName = _selectedObject.entityName();
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed( entityName );

		NSMutableArray<EOAttribute> attributes = new NSMutableArray<EOAttribute>();

		for( EOAttribute attribute : entity.attributes() ) {
			if( entity.classProperties().containsObject( attribute ) && !attribute._isPrimaryKeyClassProperty() ) {
				attributes.addObject( attribute );
			}
		}

		return attributes;
	}

	/**
	 * Editable relationships.
	 */
	public NSArray<EORelationship> relationships() {

		String entityName = _selectedObject.entityName();
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed( entityName );

		NSMutableArray<EORelationship> relationships = new NSMutableArray<EORelationship>();

		for( EORelationship relationship : entity.relationships() ) {
			if( entity.classProperties().containsObject( relationship ) ) {
				relationships.addObject( relationship );
			}
		}

		return relationships;
	}

	public void setSelectedObject( ERXGenericRecord selectedObject ) {
		this._selectedObject = selectedObject;
	}

	public ERXGenericRecord selectedObject() {
		return _selectedObject;
	}

	public void setCurrentAttributeValue( Object value ) {
		_selectedObject.takeValueForKey( value, currentAttribute.name() );
	}

	public Object currentAttributeValue() {
		return _selectedObject.valueForKey( currentAttribute.name() );
	}

	public EOAttribute currentAttribute() {
		String entityName = _selectedObject.entityName();
		return EOModelGroup.defaultGroup().entityNamed( entityName ).attributeNamed( currentAttribute.name() );
	}

	public Format fieldFormatter() {
		if( attributeIsInteger() )
			return new NSNumberFormatter( "0" );

		if( attributeIsTimestamp() )
			return new NSTimestampFormatter( "%d.%m.%y, %H:%M" );

		if( attributeIsString() )
			return null;

		return null;
	}

	public boolean attributeIsInteger() {
		return INTEGER_CLASS.equals( currentAttribute().valueTypeClassName() );
	}

	public boolean attributeIsString() {
		return STRING_CLASS.equals( currentAttribute().valueTypeClassName() );
	}

	public boolean attributeIsTimestamp() {
		return TIMESTAMP_CLASS.equals( currentAttribute().valueTypeClassName() );
	}

	public boolean attributeIsData() {
		return DATA_CLASS.equals( currentAttribute().valueTypeClassName() );
	}

	/**
	 * Smu
	 */
	public WOActionResults saveChanges() {
		ec().saveChanges();
		return returnToPreviousPage();
	}

	/**
	 * Deletes the currently selected object.
	 */
	public WOActionResults deleteObject() {
		ec().deleteObject( selectedObject() );
		return saveChanges();
	}

	/**
	 * Returns to the previous page.
	 */
	public WOActionResults returnToPreviousPage() {
		pageToReturnTo().ensureAwakeInContext( context() );
		return pageToReturnTo();
	}

	public void setPageToReturnTo( WOComponent _pageToReturnTo ) {
		this._pageToReturnTo = _pageToReturnTo;
	}

	public WOComponent pageToReturnTo() {
		return _pageToReturnTo;
	}

	public WOActionResults download() {
		return USHTTPUtilities.responseWithDataAndMimeType( "file.bin", (NSData)currentAttributeValue(), "octet/stream", true );
	}
}