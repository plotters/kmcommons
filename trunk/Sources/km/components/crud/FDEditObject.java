package km.components.crud;

import is.us.wo.util.USHTTPUtilities;

import java.text.Format;

import km.components.KMComponent;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSTimestampFormatter;

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

	public boolean isFK() {
		String entityName = _selectedObject.entityName();
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed( entityName );

		for( EORelationship relationship : entity.relationships() ) {
			for( EOJoin j : relationship.joins() ) {
				if( j.sourceAttribute().equals( currentAttribute ) ) {
					return true;
				}
			}
		}

		return false;
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
		if( value != null ) {
			_selectedObject.takeValueForKey( value, currentAttribute.name() );
		}
	}

	public Object currentAttributeValue() {
		return _selectedObject.valueForKey( currentAttribute.name() );
	}

	public EOAttribute currentAttribute() {
		String entityName = _selectedObject.entityName();
		return EOModelGroup.defaultGroup().entityNamed( entityName ).attributeNamed( currentAttribute.name() );
	}

	public Format fieldFormatter() {
		if( attributeIsInteger() ) {
			return new NSNumberFormatter( "0" );
		}

		if( attributeIsTimestamp() ) {
			return new NSTimestampFormatter( "%d.%m.%y, %H:%M" );
		}

		if( attributeIsString() ) {
			return null;
		}

		return null;
	}

	public boolean attributeIsInteger() {
//		return INTEGER_CLASS.equals(  );
		try {
			Class clazz = Class.forName( currentAttribute().valueTypeClassName() );
			return java.lang.Number.class.isAssignableFrom( clazz );
		}
		catch( Exception e ) {
			System.out.println( e );
			return false;
		}
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