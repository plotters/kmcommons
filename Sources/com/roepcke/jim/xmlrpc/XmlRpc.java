/** 
 * Copyright 1999 Hannes Wallnoefer
 * Copyright 2001 Jim Roepcke
 * XML-RPC base class. See http://www.xmlrpc.com/
 */

package com.roepcke.jim.xmlrpc;

import java.io.*;
import java.text.*;
import java.util.*;

import org.xml.sax.*;

import com.webobjects.foundation.*;

/**
 * This abstract base class provides basic capabilities for XML-RPC, like parsing of parameters
 * or encoding Java objects into XML-RPC format. Any XML parser with a <a href=http://www.megginson.com/SAX/>
 * SAX</a> interface can be used.<p>
 * XmlRpcServer and XmlRpcClient are the classes that actually implement an XML-RCP server and client.
 * @see XmlRpcServer
 * @see XmlRpcClient
 */

public abstract class XmlRpc extends HandlerBase {

	public static final String version = "WO (helma) XML-RPC 1.0";

	String methodName;

	// class name of SAX parser to use
	private static Class parserClass;
	private static Hashtable saxDrivers = new Hashtable();
	static {
		saxDrivers.put( "xp", "com.jclark.xml.sax.Driver" );
		saxDrivers.put( "ibm1", "com.ibm.xml.parser.SAXDriver" );
		saxDrivers.put( "ibm2", "com.ibm.xml.parsers.SAXParser" );
		saxDrivers.put( "aelfred", "com.microstar.xml.SAXDriver" );
		saxDrivers.put( "oracle1", "oracle.xml.parser.XMLParser" );
		saxDrivers.put( "oracle2", "oracle.xml.parser.v2.SAXParser" );
		saxDrivers.put( "openxml", "org.openxml.parser.XMLSAXParser" );
	}

	// the stack we're parsing our values into.
	NSMutableArray values; // Stack values;
	Value currentValue;

	// formats for parsing and generating dateTime values
	//	static final NSGregorianDateFormatter _datetime = new NSGregorianDateFormatter( "%Y%m%d %H:%M:%S", false );
	//	static final NSGregorianDateFormatter _date = new NSGregorianDateFormatter( "%Y%m%d", false );
	//	static final NSGregorianDateFormatter _time = new NSGregorianDateFormatter( "%H:%M:%S", false );

	static final SimpleDateFormat _datetime = new SimpleDateFormat( "yyyyMMdd'T'HH:mm:ss" );
	static final SimpleDateFormat _date = new SimpleDateFormat( "yyyyMMdd" );
	static final SimpleDateFormat _time = new SimpleDateFormat( "HH:mm:ss" );

	// DateFormat datetime;
	// now comes wapped into a synchronized class because dateFormat is not threadsafe
	// static Formatter dateformat = new Formatter ();

	// used to collect character data of parameter values
	StringBuffer cdata;
	boolean readCdata;

	// XML RPC parameter types used for dataMode
	static final int STRING = 0;
	static final int INTEGER = 1;
	static final int BOOLEAN = 2;
	static final int DOUBLE = 3;
	static final int DATE = 4;
	static final int BASE64 = 5;
	static final int STRUCT = 6;
	static final int ARRAY = 7;
	static final int NIL = 8;

	// Error level + message
	int errorLevel;
	String errorMsg;

	static final int NONE = 0;
	static final int RECOVERABLE = 1;
	static final int FATAL = 2;

	// use HTTP keepalive?
	static boolean keepalive = false;

	// for debugging output
	public static boolean debug = false;
	final static String types[] = { "String", "Integer", "Boolean", "Double", "Date", "Base64", "Struct", "Array", "Nil" };

	// mapping between java encoding names and "real" names used in XML prolog.
	// if you use an encoding not listed here send feedback to xmlrpc@helma.org

	static String encoding = "ISO8859_1";
	static Properties encodings = new Properties();
	static {
		encodings.put( "UTF8", "UTF-8" );
		encodings.put( "ISO8859_1", "ISO-8859-1" );
	}

	/**
	 * Set the SAX Parser to be used. The argument can either be the full class name or 
	 * a user friendly shortcut if the parser is known to this class. The parsers that can 
	 * currently be set by shortcut are listed in the main documentation page. If you are using 
	 * another parser please send me the name of the SAX driver and I'll include it in a future release. 
	 * If setDriver() is never called then the System property "sax.driver" is consulted. If that is not defined 
	 * the driver defaults to OpenXML.
	 */
	public static void setDriver( String driver ) throws ClassNotFoundException {
		String parserClassName = null;
		try {
			parserClassName = (String)saxDrivers.get( driver );
			if( parserClassName == null )
				parserClassName = driver;
			parserClass = Class.forName( parserClassName );
		}
		catch( ClassNotFoundException x ) {
			throw new ClassNotFoundException( "SAX driver not found: " + parserClassName );
		}
	}

	/**
	 * Set the SAX Parser to be used by directly passing the Class object.
	 */
	public static void setDriver( Class driver ) {
		parserClass = driver;
	}

	/**
	 * Set the encoding of the XML. This should be the name of a Java encoding
	 * contained in the encodings Hashtable.
	 */
	public static void setEncoding( String enc ) {
		encoding = enc;
	}

	public String getEncoding() {
		return encodings.getProperty( encoding, encoding );
	}

	/**
	 * Switch debugging output on/off.
	 */
	public static void setDebug( boolean val ) {
		debug = val;
	}

	/**
	 * Switch HTTP keepalive on/off.
	 */
	public static void setKeepAlive( boolean val ) {
		keepalive = val;
	}

	/**
	 * get current HTTP keepalive mode.
	 */
	public static boolean getKeepAlive() {
		return keepalive;
	}

	/** 
	 * Parse the input stream. For each root level object, method <code>objectParsed</code>
	 * is called.
	 */
	synchronized void parse( InputStream is ) throws Exception {

		// reset values (XmlRpc objects are reusable)
		errorLevel = NONE;
		errorMsg = null;
		values = new NSMutableArray(); // new Stack (); 
		if( cdata == null )
			cdata = new StringBuffer( 128 );
		else
			cdata.setLength( 0 );
		readCdata = false;
		currentValue = null;

		long now = System.currentTimeMillis();
		if( parserClass == null ) {
			// try to get the name of the SAX driver from the System properties
			setDriver( System.getProperty( "sax.driver", "org.openxml.parser.XMLSAXParser" ) );
		}

		Parser parser = null;
		try {
			parser = (Parser)parserClass.newInstance();
		}
		catch( NoSuchMethodError nsm ) {
			// This is thrown if no constructor exists for the parser class 
			// and is transformed into a regular exception.
			throw new Exception( "Can't create Parser: " + parserClass );
		}

		parser.setDocumentHandler( this );
		parser.setErrorHandler( this );

		parser.parse( new InputSource( is ) );
		if( debug )
			System.err.println( "Spent " + (System.currentTimeMillis() - now) + " millis parsing" );
	}

	/**
	 * Writes the XML representation of a supported Java object to the XML writer.
	 */
	void writeObject( Object what, XmlWriter writer ) {
		if( what instanceof XmlRpcSerializeable ) {
			what = ((XmlRpcSerializeable)what).serialize();
		}

		writer.startElement( "value" );
		if( what == null ) {
			// try sending experimental <ni/> element
			writer.emptyElement( "nil" );
		}
		else if( what instanceof String ) {
			writer.chardata( what.toString() );
		}
		else if( what instanceof Integer ) {
			writer.startElement( "int" );
			writer.write( what.toString() );
			writer.endElement( "int" );
		}
		else if( what instanceof Boolean ) {
			writer.startElement( "boolean" );
			writer.write( ((Boolean)what).booleanValue() ? "1" : "0" );
			writer.endElement( "boolean" );
		}
		else if( what instanceof Double || what instanceof Float ) {
			writer.startElement( "double" );
			writer.write( what.toString() );
			writer.endElement( "double" );
		}
		else if( what instanceof NSTimestamp ) {
			writer.startElement( "dateTime.iso8601" );
			NSTimestamp d = (NSTimestamp)what;
			try {
				Date date = new Date( d.getTime() );
				writer.write( _date.format( date ) );
				writer.write( "T" );
				writer.write( _time.format( date ) );
			}
			catch( Exception p ) {
				// System.out.println ("Exception while parsing date: "+p);
				throw new RuntimeException( p.getMessage() );
			}
			// writer.write (dateformat.format (d));
			writer.endElement( "dateTime.iso8601" );
		}
		else if( what instanceof byte[] ) {
			writer.startElement( "base64" );
			writer.write( Base64.encode( (byte[])what ) );
			writer.endElement( "base64" );
		}
		else if( what instanceof NSData ) {
			writer.startElement( "base64" );
			writer.write( Base64.encode( ((NSData)what).bytes( 0, ((NSData)what).length() ) ) );
			writer.endElement( "base64" );
		}
		else if( what instanceof NSArray ) {
			writer.startElement( "array" );
			writer.startElement( "data" );
			NSArray v = (NSArray)what;
			int l2 = v.count();
			for( int i2 = 0; i2 < l2; i2++ )
				writeObject( v.objectAtIndex( i2 ), writer );
			writer.endElement( "data" );
			writer.endElement( "array" );
		}
		else if( what instanceof NSDictionary ) {
			writeDictionary( what, writer );
		}
		else
			throw new RuntimeException( "unsupported Java type: " + what.getClass() );
		writer.endElement( "value" );
	}

	private void writeDictionary( Object what, XmlWriter writer ) {
		writer.startElement( "struct" );
		NSDictionary h = (NSDictionary)what;
		for( Enumeration e = h.keyEnumerator(); e.hasMoreElements(); ) {
			String nextkey = (String)e.nextElement();
			Object nextval = h.objectForKey( nextkey );
			writer.startElement( "member" );
			writer.startElement( "name" );
			writer.write( nextkey );
			writer.endElement( "name" );
			writeObject( nextval, writer );
			writer.endElement( "member" );
		}
		writer.endElement( "struct" );
	}

	/**
	 *  This method is called when a root level object has been parsed. 
	 */
	abstract void objectParsed( Object what );

	////////////////////////////////////////////////////////////////
	// methods called by XML parser 

	/**
	 * Method called by SAX driver.
	 */
	public void characters( char ch[], int start, int length ) throws SAXException {
		if( !readCdata )
			return;
		cdata.append( ch, start, length );
	}

	/**
	 * Method called by SAX driver.
	*/
	public void endElement( String name ) throws SAXException {

		if( debug )
			System.err.println( "endElement: " + name );

		// finalize character data, if appropriate
		if( currentValue != null && readCdata ) {
			currentValue.characterData( cdata.toString() );
			cdata.setLength( 0 );
			readCdata = false;
		}

		if( "value".equals( name ) ) {
			int depth = values.count();
			// Only handle top level objects or objects contained in arrays here.
			// For objects contained in structs, wait for </member> (see code below).
			if( depth < 2 || values.objectAtIndex( depth - 2 ).hashCode() != STRUCT ) {
				Value v = currentValue;
				values.removeLastObject();
				if( depth < 2 ) {
					// This is a top-level object
					objectParsed( v.value );
					currentValue = null;
				}
				else {
					// add object to sub-array; if current container is a struct, add later (at </member>)
					currentValue = (Value)values.lastObject();
					currentValue.endElement( v );
				}
			}
		}

		// Handle objects contained in structs.
		if( "member".equals( name ) ) {
			Value v = currentValue;
			values.removeLastObject();
			currentValue = (Value)values.lastObject();
			currentValue.endElement( v );
		}

		else if( "methodName".equals( name ) ) {
			methodName = cdata.toString();
			cdata.setLength( 0 );
			readCdata = false;
		}
	}

	/**
	 * Method called by SAX driver.
	 */
	public void startElement( String name, AttributeList atts ) throws SAXException {

		if( debug )
			System.err.println( "startElement: " + name );

		if( "value".equals( name ) ) {
			// System.err.println ("starting value");
			Value v = new Value();
			values.addObject( v );
			currentValue = v;
			// cdata object is reused
			cdata.setLength( 0 );
			readCdata = true;
		}

		else if( "methodName".equals( name ) ) {
			cdata.setLength( 0 );
			readCdata = true;
		}

		else if( "name".equals( name ) ) {
			cdata.setLength( 0 );
			readCdata = true;
		}

		else if( "string".equals( name ) ) {
			// currentValue.setType (STRING);
			cdata.setLength( 0 );
			readCdata = true;
		}
		else if( "i4".equals( name ) || "int".equals( name ) ) {
			currentValue.setType( INTEGER );
			cdata.setLength( 0 );
			readCdata = true;
		}
		else if( "boolean".equals( name ) ) {
			currentValue.setType( BOOLEAN );
			cdata.setLength( 0 );
			readCdata = true;
		}
		else if( "double".equals( name ) ) {
			currentValue.setType( DOUBLE );
			cdata.setLength( 0 );
			readCdata = true;
		}
		else if( "dateTime.iso8601".equals( name ) ) {
			currentValue.setType( DATE );
			cdata.setLength( 0 );
			readCdata = true;
		}
		else if( "base64".equals( name ) ) {
			currentValue.setType( BASE64 );
			cdata.setLength( 0 );
			readCdata = true;
		}
		else if( "struct".equals( name ) )
			currentValue.setType( STRUCT );
		else if( "array".equals( name ) )
			currentValue.setType( ARRAY );
		else if( "nil".equals( name ) )
			currentValue.setType( NIL );
	}

	public void error( SAXParseException e ) throws SAXException {
		System.err.println( "Error parsing XML: " + e );
		errorLevel = RECOVERABLE;
		errorMsg = e.toString();
	}

	public void fatalError( SAXParseException e ) throws SAXException {
		System.err.println( "Fatal error parsing XML: " + e );
		errorLevel = FATAL;
		errorMsg = e.toString();
	}

	/**
	 * This represents an XML-RPC Value while the request is being parsed.
	 */
	class Value {

		int type;
		Object value;
		// the name to use for the next member of struct values
		String nextMemberName;

		NSMutableDictionary struct;
		NSMutableArray array;

		/**
		 * Constructor.
		 */
		public Value() {
			this.type = STRING;
		}

		/**
		 * Notification that a new child element has been parsed. 
		 */
		public void endElement( Value child ) {
			if( type == ARRAY )
				array.addObject( child.value );
			else if( type == STRUCT )
				struct.setObjectForKey( child.value, nextMemberName );
		}

		/**
		 * Set the type of this value. If it's a container, create the corresponding java container. 
		 */
		public void setType( int type ) {
			// System.err.println ("setting type to "+types[type]);
			this.type = type;
			if( type == ARRAY )
				value = array = new NSMutableArray();
			if( type == STRUCT )
				value = struct = new NSMutableDictionary();
		}

		/**
		 * Set the character data for the element and interpret it according to the 
		 * element type
		 */
		public void characterData( String cdata ) {
			switch (type) {
				case INTEGER:
					value = new Integer( cdata.trim() );
					break;
				case BOOLEAN:
					value = new Boolean( "1".equals( cdata.trim() ) );
					break;
				case DOUBLE:
					value = new Double( cdata.trim() );
					break;
				case DATE:
					try {
						//String d = cdata.trim().replace( 'T', ' ' );
						value = new NSTimestamp( _datetime.parse( cdata.trim(), new ParsePosition( 0 ) ) );
						// value = dateformat.parse (cdata.trim ());
					}
					catch( Exception p ) {
						// System.err.println ("Exception while parsing date: "+p);
						throw new RuntimeException( p.getMessage() );
					}
					break;
				case BASE64:
					value = new NSData( Base64.decode( cdata.getBytes() ) );
					break;
				case STRING:
					value = cdata;
					break;
				case STRUCT:
					// this is the name to use for the next member of this struct
					nextMemberName = cdata;
					break;
			}
		}

		// This is a performance hack to get the type of a value without casting the Object. 
		// It breaks the contract of method hashCode, but it doesn't matter since
		// Value objects are never used as keys in Hashtables. 
		public int hashCode() {
			return type;
		}

		public String toString() {
			return (types[type] + " element " + value);
		}
	}

	// A quick and dirty XML writer.
	class XmlWriter {

		StringBuffer buf;
		String enc;

		public XmlWriter( StringBuffer buf ) {
			// The encoding used for XML-RPC is ISO-8859-1 for pragmatical reasons (Frontier/Win).
			this( buf, encoding );
		}

		public XmlWriter( StringBuffer buf, String enc ) {
			this.buf = buf;
			this.enc = enc;
			// get name of encoding for XML prolog
			String encName = encodings.getProperty( enc, enc );
			buf.append( "<?xml version=\"1.0\" encoding=\"" + encName + "\"?>" );
		}

		public void startElement( String elem ) {
			buf.append( "<" );
			buf.append( elem );
			buf.append( ">" );
		}

		public void endElement( String elem ) {
			buf.append( "</" );
			buf.append( elem );
			buf.append( ">" );
		}

		public void emptyElement( String elem ) {
			buf.append( "<" );
			buf.append( elem );
			buf.append( "/>" );
		}

		public void chardata( String text ) {
			int l = text.length();
			for( int i = 0; i < l; i++ ) {
				char c = text.charAt( i );
				switch (c) {
					case '<':
						buf.append( "&lt;" );
						break;
					case '&':
						buf.append( "&amp;" );
						break;
					default:
						buf.append( c );
				}
			}
		}

		public void write( char[] text ) {
			buf.append( text );
		}

		public void write( String text ) {
			buf.append( text );
		}

		public String toString() {
			return buf.toString();
		}

		public byte[] getBytes() throws UnsupportedEncodingException {
			return buf.toString().getBytes( enc );
		}

	}

}

// wraps a DateFormat because it's not threadsafe
//class Formatter {
//
//    private DateFormat f;
//
//    public Formatter () {
//	f = new SimpleDateFormat ("yyyyMMdd'T'HH:mm:ss");
//    }
//
//    public synchronized String format (Date d) {
//	return f.format (d);
//    }
//
//    public synchronized Date parse (String s) throws ParseException {
//	return f.parse (s);
//    }
//}