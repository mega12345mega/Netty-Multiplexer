package com.luneruniverse.nettymux.messageprotocol;

import java.util.List;

import com.luneruniverse.nettymux.ProtocolDetectionResult;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * Detects the protocol based on incoming messages
 * @param <I> The type of messages that should be handled
 */
public interface MessageProtocol<I> {
	/**
	 * Check if the currently received messages are enough to determine whether or not this protocol is in use.<br>
	 * <br>
	 * <strong>Warning:</strong> Only remove messages from <code>in</code> if the protocol is detected AND
	 * you want to remove some messages before they get to the protocol's handler! When removing messages, make sure
	 * to call {@link ReferenceCountUtil#release(Object)}!
	 * @param in The currently received messages
	 * @return If this protocol is in use, cannot be in use, or this isn't known
	 */
	public ProtocolDetectionResult attemptDetection(List<I> in);
	/**
	 * Set up the pipeline to handle this protocol (this is only called if {@link #attemptDetection(List)} returns
	 * {@link ProtocolDetectionResult#DETECTED}). You do not need to remove multiplexing-related handlers; this will
	 * be done automatically after this method returns.
	 * @param ctx The context of the {@link NettyMessageMultiplexer}
	 */
	public void bind(ChannelHandlerContext ctx);
}
