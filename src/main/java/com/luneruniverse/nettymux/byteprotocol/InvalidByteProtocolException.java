package com.luneruniverse.nettymux.byteprotocol;

import com.luneruniverse.nettymux.InvalidProtocolException;

@SuppressWarnings("serial")
public class InvalidByteProtocolException extends InvalidProtocolException {
	
	public enum Type {
		FAILED_TO_DETECT,
		NOT_SSL_WHEN_FORCED,
		INVALID_ALPN_PROTOCOL
	}
	
	private final Type type;
	
	public InvalidByteProtocolException(Type type) {
		super(type.toString());
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
}
