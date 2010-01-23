package km;

import is.us.util.USStringUtilities;

import java.io.File;

import com.webobjects.foundation.*;

/**
 * * Converts between timecodes and frame numbers.
 * * Adds multiple timecodes to get one result.
 * * Timecodes within one project are always of the same type.
 * 
 * @author Ósk Gunnlaugsdóttir
 */

public class KMFrameCalc {

	private static final float FPS_FILM = 24;
	private static final float FPS_PAL = 25;
	private static final float FPS_NTSC = 29.5f; // TODO: Double check.

	private static final String COLON = ":";

	public static void main( String[] argv ) {
		//		System.out.println( framesToString( 30, FPS_PAL ) );
		//		readFile();
		NSArray<String> codes = timeCodesFromFile( "/Users/hugi/Desktop/Batch" );

		for( String tc : codes ) {
			System.out.println( tc );
			System.out.println( framesFromTimeCode( tc, FPS_PAL ) );
		}
	}

	/**
	 * 
	 */
	public static int framesFromTimeCode( String tc, float fps ) {

		NSArray<String> tcArray = NSArray.componentsSeparatedByString( tc, COLON );

		int hours = Integer.valueOf( tcArray.objectAtIndex( 0 ) );
		int minutes = Integer.valueOf( tcArray.objectAtIndex( 1 ) );
		int seconds = Integer.valueOf( tcArray.objectAtIndex( 2 ) );
		int frames = Integer.valueOf( tcArray.objectAtIndex( 3 ) );

		int hourFrames = (int)(hours * 60 * 60 * fps);
		int minuteFrames = (int)(minutes * 60 * fps);
		int secondFrames = (int)(seconds * fps);

		return hourFrames + minuteFrames + secondFrames + frames;
	}

	/**
	 * Takes a string of the format ":x:y:z" and returns the numerical value of z.
	 */
	private int lastValueBeforeColon( String tc ) {
		NSArray<String> a = NSArray.componentsSeparatedByString( tc, COLON );
		return Integer.valueOf( a.lastObject() );
	}

	/**
	 * 
	 * @param numberOfFrames
	 * @param fps
	 * @return
	 */
	public static String framesToString( float numberOfFrames, int fps ) {

		int fpm = fps * 60;
		int fph = fpm * 60;

		int seconds = (int)(numberOfFrames / fps);
		int remainingFrames = (int)(numberOfFrames % fps);

		String secondString = fixNumberFormat( seconds );
		String frameString = fixNumberFormat( remainingFrames );
		return secondString + ":" + frameString;
	}

	/**
	 * Formats the string for double digits. 
	 */
	private static String fixNumberFormat( int number ) {
		String numberString = String.valueOf( number );

		if( number < 10 ) {
			numberString = "0" + numberString;
		}

		return numberString;
	}

	/**
	 * 
	 */
	public static NSArray<String> timeCodesFromFile( String fileName ) {
		String entireFile = USStringUtilities.readStringFromFileUsingEncoding( new File( fileName ), "UTF-8" );

		NSArray<String> lines = NSArray.componentsSeparatedByString( entireFile, "\r" );

		NSMutableArray<String> timeCodes = new NSMutableArray<String>();

		for( String line : lines ) {
			NSArray<String> values = NSArray.componentsSeparatedByString( line, "\t" );

			if( values.count() > 1 ) {
				String timeCodeValue = values.objectAtIndex( 1 );

				// FIXME: Use isTimeCode();
				if( !"Duration".equals( timeCodeValue ) )
					timeCodes.addObject( timeCodeValue );
			}
		}

		return timeCodes;
	}

	/**
	 * TODO: Implement
	 *
	private static final isTimecode( String string) {
		
	}
	*/
}