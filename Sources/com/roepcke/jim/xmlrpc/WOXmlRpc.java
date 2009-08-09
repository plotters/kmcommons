/* WOXmlRpc.java created by jroepcke on Sun 11-Mar-2001 */
/* Copyright 2001 Jim Roepcke */

package com.roepcke.jim.xmlrpc;

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSData;

public class WOXmlRpc extends WODirectAction {

	static XmlRpcServer xmlrpc = new XmlRpcServer();

	public WOXmlRpc( WORequest aRequest ) {
		super( aRequest );
	}

	public WOActionResults RPC2Action() {
		WORequest request = request();
		WOResponse response = new WOResponse();
		NSData resultData;
		// response.setHeader("Server: WO (Helma) XML-RPC 1.0", "Server");
		if( "POST".equals( request.method() ) ) {
			byte[] result = xmlrpc.execute( new java.io.ByteArrayInputStream( request.content().bytes( 0, request.content().length() ) ) );
			// response.disableClientCaching();
			resultData = new NSData( result );
			response.setContent( resultData );
			response.setHeader( "text/xml", "content-type" );
			response.setHeader( Integer.toString( resultData.length() ), "content-length" );
		}
		else {
			response.setStatus( 400 );
			resultData = new NSData( ("Method " + request.method() + " not implemented (try POST)").getBytes() );
			response.setContent( resultData );
			response.setHeader( Integer.toString( resultData.length() ), "content-length" );
		}
		return response;
	}

	/**
	 * Register a handler object with this name. Methods of this objects will be
	 * callable over XML-RPC as "name.method".
	 */
	// from WebServer.java
	public static void addHandler( String name, Object target ) {
		xmlrpc.addHandler( name, target );
	}

	/**
	 * Remove a handler object that was previously registered with this server.
	 */
	// from WebServer.java
	public static void removeHandler( String name ) {
		xmlrpc.removeHandler( name );
	}

}
