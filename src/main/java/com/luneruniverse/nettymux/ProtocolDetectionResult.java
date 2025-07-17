package com.luneruniverse.nettymux;

import java.util.List;

import com.luneruniverse.nettymux.byteprotocol.ByteProtocol;
import com.luneruniverse.nettymux.messageprotocol.MessageProtocol;

import io.netty.buffer.ByteBuf;

/**
 * Returned from {@link ByteProtocol#attemptDetection(ByteBuf)} and
 * {@link MessageProtocol#attemptDetection(List)} to signal if the currently received data is enough to
 * determine whether or not a protocol is in use
 */
public enum ProtocolDetectionResult {
	/**
	 * This protocol is in use; stop trying to identify the protocol and bind this one
	 */
	DETECTED,
	/**
	 * There is not enough data to determine if this protocol is in use<br>
	 * <br>
	 * <strong>Warning:</strong> Ensure that either {@link #DETECTED} or {@link #REJECTED} will eventually be returned;
	 * otherwise, the connection will stay open forever with all of the data that has been received being held in memory!
	 */
	UNKNOWN,
	/**
	 * This protocol is not in use; stop checking for this protocol<br>
	 * If there are no possible protocols remaining, a subclass of {@link InvalidProtocolException} will be thrown.
	 */
	REJECTED
}
