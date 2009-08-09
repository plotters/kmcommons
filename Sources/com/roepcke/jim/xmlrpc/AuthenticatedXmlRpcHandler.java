/*
 * Copyright 2000 Hannes Wallnoefer
 * Copyright 2001 Jim Roepcke
 */

package com.roepcke.jim.xmlrpc;

import com.webobjects.foundation.NSArray;

/**
 * An XML-RPC handler that also handles user authentication.
 */

public interface AuthenticatedXmlRpcHandler {

	/**
	 * Return the result, or throw an Exception if something went wrong.
	 */
	public Object execute( String method, NSArray params, String user, String password ) throws Exception;

}