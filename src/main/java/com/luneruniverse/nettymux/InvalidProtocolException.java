package com.luneruniverse.nettymux;

/**
 * None of the protocols matched the incoming data
 */
@SuppressWarnings("serial")
public class InvalidProtocolException extends Exception {
	
	public InvalidProtocolException() {
		
	}
	public InvalidProtocolException(String message) {
		super(message);
	}
	
}
