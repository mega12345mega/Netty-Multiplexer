package com.luneruniverse.nettymux.byteprotocol;

import com.luneruniverse.nettymux.InvalidProtocolException;

/**
 * None of the protocols matched the incoming byte data
 * @see Type
 */
@SuppressWarnings("serial")
public class InvalidByteProtocolException extends InvalidProtocolException {
	
	/**
	 * How the incoming data was invalid
	 * @see #FAILED_TO_DETECT
	 * @see #NOT_SSL_WHEN_FORCED
	 * @see #INVALID_ALPN_PROTOCOL
	 */
	public enum Type {
		/**
		 * Either:
		 * <ul>
		 *   <li>SSL was not requested, SSL is not forced by the server, and the incoming byte data didn't match a protocol</li>
		 *   <li>SSL was requested, SSL is allowed by the server, ALPN negotiation failed,
		 *   and the following byte data didn't match a protocol</li>
		 * </ul>
		 */
		FAILED_TO_DETECT,
		/**
		 * SSL was not requested, but SSL is forced by the server
		 */
		NOT_SSL_WHEN_FORCED,
		/**
		 * SSL was requested, SSL is allowed by the server, ALPN negotiation succeeded, and the resulting protocol name
		 * from ALPN negotiation didn't match any protocol's {@link ByteProtocol#getAlpnName()}
		 */
		INVALID_ALPN_PROTOCOL
	}
	
	private final Type type;
	
	public InvalidByteProtocolException(Type type) {
		super(type.toString());
		this.type = type;
	}
	
	/**
	 * @return How the incoming data was invalid
	 * @see Type
	 */
	public Type getType() {
		return type;
	}
	
}
