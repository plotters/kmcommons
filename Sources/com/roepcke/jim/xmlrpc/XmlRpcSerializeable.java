/**
 * 
 */
package com.roepcke.jim.xmlrpc;

/**
 * Interface to make any object serializable over XmlRpc
 * 
 * @author Atli Páll Hafsteinsson
 *
 */
public interface XmlRpcSerializeable {

	/**
	 * @return an object of any of the types supported by XmlRpc:<br />
	 * <code>Integer</code>, <code>Double</code>, <code>Float</code>, <code>Boolean</code>, 
	 * <code>NSTimestamp</code>, <code>NSData</code>, byte array, <code>NSArray</code> 
	 * or <code>NSDictionary</code>
	 */
	public Object serialize();
}
