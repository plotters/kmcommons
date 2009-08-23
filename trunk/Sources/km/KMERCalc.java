package km;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * A very simple wrapper for fetching currency exchange rates to the Central Bank of Iceland.
 * 
 * XML data is cached. You need to invoke "reloadData()" to update values.
 * 
 * The parsing implementation in this class is ... interesting ...
 * I wanted to avoid dependencies on any xml parsing libraries.
 * 
 * For information on the XML provided by the Central Bank, look here:
 * http://sedlabanki.is/?PageID=662
 * 
 * Usage example:
 * 
 * <code>
 * 	// Create a new calculator instance for June 1st 2004.
 * 	KMERCalc km = KMERCalc.create( "01.06.2004" );
 *
 *	// Print out data for all currencies.
 *	System.out.println( km.rateTable() );
 *
 *	// Convert 9 canadian dollars to GBP.
 *	System.out.println( km.calculate( 9, "CAD", "GBP" ) ); 
 *	</code>
 * 
 * @author Hugi Þórðarson
 * @version 0.1
 * 
 * TODO: Currently using floats, switch to BigDecimal sometime in the future.
 * TODO: Implement date ranges.
 * TODO: Wrap currencies into classes
 * TODO: Add support for more currencies (provided in other documents).
 */

public class KMERCalc {

	/**
	 * Cached data.
	 */
	private String _dateString;
	private String _xmlString;

	/**
	 * Our XML-file
	 */
	private static final String BASE_URL = "http://sedlabanki.is/?PageID=289";
	private static final String ENCODING = "ISO-8859-1";

	/**
	 * XML tag names (not all of them in use yet).
	 */
	private static final String ROOT_TAG = "myntir";
	private static final String CURRENCY_TAG = "mynt";
	private static final String CURRENCY_SYMBOL_TAG = "myntnafn";
	private static final String CURRENCY_NAME_TAG = "myntheiti";
	private static final String CURRENCY_MID_RATE_TAG = "midgengi";
	private static final String CURRENCY_SELLING_RATE_TAG = "solugengi";
	private static final String CURRENCY_PURCHASE_RATE_TAG = "kaupgengi";
	private static final String DATE_TAG = "dagsetning";

	/**
	 * Regex pattern to fetch all currency symbols from the xml.
	 */
	private static final Pattern PATTERN = Pattern.compile( "<myntnafn>(.*?)</myntnafn>", Pattern.DOTALL );

	/**
	 * Only this class can create instances of itself.
	 * Use create( dateString ) to get an instance.
	 */
	private KMERCalc() {}

	/**
	 * Usage example.
	 */
	public static void main( String[] argv ) {
		// Create an instance containing exchange rates as they were on june 1st, 2004. 
		KMERCalc km = KMERCalc.create( null );

		// Print out data for all currencies
		System.out.println( km.rateTable() );

		// Convert 9 canadian dollars to British Pounds
		System.out.println( km.calculate( 9, "CAD", "GBP" ) );

		// Convert 20 US Dollars to British Pounds.
		System.out.println( km.calculate( 144, "USD", "EUR" ) );
	}

	/**
	 * Resets the data.
	 */
	public void reloadData() {
		_xmlString = null;
	}

	/**
	 * Factory method to create instances. 
	 * 
	 * @param dateString takes a date of the format %d.%m.%Y. If null, current rates are used.
	 * 
	 */
	public static KMERCalc create( String dateString ) {
		KMERCalc km = new KMERCalc();
		km.setDateString( dateString );
		return km;
	}

	/**
	 * Returns a human readable table of all data in the object.
	 */
	public String rateTable() {
		StringBuffer b = new StringBuffer();
		int columnSize = 12;

		String kaupgengi = KMERUtil.padString( "Kaupgengi", columnSize );
		String solugengi = KMERUtil.padString( "Sölugengi", columnSize );
		String midgengi = KMERUtil.padString( "Miðgengi", columnSize );

		b.append( KMERUtil.padString( "", 8 ) + kaupgengi + solugengi + midgengi );
		b.append( "\n" );

		for( String symbol : availableCurrencies() ) {
			Object p = ratePurchaseForCurrency( symbol );
			Object s = rateSellingForCurrency( symbol );
			Object m = rateMidForCurrency( symbol );

			symbol = KMERUtil.padString( symbol, 8 );
			p = KMERUtil.padString( p, columnSize );
			s = KMERUtil.padString( s, columnSize );
			m = KMERUtil.padString( m, columnSize );
			b.append( symbol + p + s + m );
			b.append( "\n" );
		}

		return b.toString();
	}

	/**
	 * @return A string array containing available currency symbols.
	 */
	public String[] availableCurrencies() {
		Matcher m = PATTERN.matcher( xmlString() );

		ArrayList<String> parsedCurrencySymbols = new ArrayList<String>();

		while( m.find() ) {
			String nextSymbol = m.group( 1 );

			if( nextSymbol != null && nextSymbol.length() > 0 )
				parsedCurrencySymbols.add( nextSymbol );
		}

		return parsedCurrencySymbols.toArray( new String[parsedCurrencySymbols.size()] );
	}

	/**
	 * The date this class represents. 
	 */
	private String dateString() {
		return _dateString;
	}

	private void setDateString( String s ) {
		_dateString = s;
	}

	/**
	 * @return The URL we use to fetch data.
	 */
	private String url() {
		if( dateString() == null )
			return BASE_URL;

		return BASE_URL + "&dagur=" + dateString();
	}

	/**
	 * @return The purchase rate of the given currency
	 */
	public Float ratePurchaseForCurrency( String symbol ) {
		return KMERUtil.toFloat( valueOfTagAfterTag( CURRENCY_SYMBOL_TAG, symbol, CURRENCY_PURCHASE_RATE_TAG ) );
	}

	/**
	 * @return The selling rate of the given currency
	 */
	public Float rateSellingForCurrency( String symbol ) {
		return KMERUtil.toFloat( valueOfTagAfterTag( CURRENCY_SYMBOL_TAG, symbol, CURRENCY_SELLING_RATE_TAG ) );
	}

	/**
	 * @return The mid rate of the given currency
	 */
	public Float rateMidForCurrency( String symbol ) {
		return KMERUtil.toFloat( valueOfTagAfterTag( CURRENCY_SYMBOL_TAG, symbol, CURRENCY_MID_RATE_TAG ) );
	}

	/**
	 * Given a tag name and value, will show us what the entire tag looks like. 
	 */
	private static String entireTag( String tagName, String tagValue ) {
		return "<" + tagName + ">" + tagValue + "</" + tagName + ">";
	}

	/**
	 * Given a tag and a value, will return the value of a tag after that substring.
	 */
	private String valueOfTagAfterTag( String firstTag, String firstTagValue, String tagName ) {
		String entireBeforeTag = entireTag( firstTag, firstTagValue );
		String openingTag = "<" + tagName + ">";
		String closingTag = "</" + tagName + ">";
		int whereIndex = xmlString().indexOf( entireBeforeTag );
		int startIndex = xmlString().indexOf( openingTag, whereIndex ) + openingTag.length();
		int endIndex = xmlString().indexOf( closingTag, whereIndex );
		return xmlString().substring( startIndex, endIndex );
	}

	/**
	 * Calculates amounts in different currencies (using mid rate).
	 * 
	 * @param amount
	 * @param fromCurrencySymbol
	 * @param toCurrencySymbol
	 * 
	 * @return The value converted.
	 */
	public Float calculate( Number amount, String fromCurrencySymbol, String toCurrencySymbol ) {
		KMERCalc km = create( null );
		Float fromCurrencyRate = km.rateMidForCurrency( fromCurrencySymbol );
		Float toCurrencyRate = km.rateMidForCurrency( toCurrencySymbol );

		float ratio = fromCurrencyRate / toCurrencyRate;
		float result = amount.floatValue() * ratio;
		return result;
	}

	/**
	 * @return the XML we're working with.
	 */
	private String xmlString() {
		if( _xmlString == null ) {
			byte[] bytes = KMERUtil.downloadData( url() );

			if( bytes != null )
				try {
					_xmlString = new String( bytes, ENCODING );
				}
				catch( UnsupportedEncodingException e ) {
					e.printStackTrace();
				}
		}
		return _xmlString;
	}
}

/**
 * Utility methods.
 */

class KMERUtil {
	/**
	 * Downloads data from the given url. 
	 */
	static byte[] downloadData( String address ) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		URLConnection conn = null;
		InputStream in = null;
		try {
			URL url = new URL( address );
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while( (numRead = in.read( buffer )) != -1 ) {
				out.write( buffer, 0, numRead );
				numWritten += numRead;
			}

			return out.toByteArray();
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
		finally {
			try {
				if( in != null ) {
					in.close();
				}
				if( out != null ) {
					out.close();
				}
			}
			catch( IOException ioe ) {
				ioe.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * This method will adjust a String to a certain length. If the string is
	 * too long, it pads it with spaces on the left. If the string is too short,
	 * it will cut of the end of it.
	 * 
	 * Created to make creation of fixed length string easier.
	 * 
	 * @author Hugi Þórðarson
	 */
	static String padString( Object object, int desiredLength ) {

		if( object == null )
			object = "";

		String string = object.toString();

		StringBuffer str = new StringBuffer();

		if( string != null )
			str.append( string );

		int strLength = str.length();

		if( desiredLength > 0 && desiredLength > strLength ) {
			for( int i = 0; i <= desiredLength; i++ ) {
				if( i > strLength )
					str.append( ' ' );
			}
		}
		return str.toString();
	}

	/**
	 * Converts a string to a float, returning null if the conversion fails.  
	 */
	static Float toFloat( String s ) {
		try {
			return Float.valueOf( s );
		}
		catch( Exception e ) {
			return null;
		}
	}
}