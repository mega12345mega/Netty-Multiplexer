package com.luneruniverse.nettymux.byteprotocol;

import com.luneruniverse.nettymux.ProtocolDetectionResult;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * Detects the protocol based on incoming bytes (in a {@link ByteBuf})
 */
public interface ByteProtocol {
	/**
	 * Get the name that can be included in ALPN negotiation (only used when a connection is over SSL).
	 * This doesn't automatically add the name to the provided {@link SslContext}.
	 * Use {@link SslContextBuilder#applicationProtocolConfig(ApplicationProtocolConfig)}
	 * if you want to enable ALPN and make sure you include the ALPN name for all possible {@link ByteProtocol}s.
	 * @return The ALPN name of this protocol
	 */
	public String getAlpnName();
	/**
	 * Check if the currently received bytes are enough to determine whether or not this protocol is in use.<br>
	 * <br>
	 * <strong>Warning:</strong> Only move the reader index of <code>in</code> if the protocol is detected AND
	 * you want to remove some bytes before they get to the protocol's handler!
	 * @param in The currently received bytes
	 * @return If this protocol is in use, cannot be in use, or this isn't known
	 */
	public ProtocolDetectionResult attemptDetection(ByteBuf in);
	/**
	 * Set up the pipeline to handle this protocol (this is only called if {@link #attemptDetection(ByteBuf)} returns
	 * {@link ProtocolDetectionResult#DETECTED}). Note that SSL support will already be added to the pipeline if it
	 * is in use. You do not need to remove multiplexing-related handlers; this will be done automatically after
	 * this method returns.
	 * @param ctx The context of the last handler in the pipeline involved in multiplexing (will be the
	 * {@link NettyByteMultiplexer} if SSL is not in use)
	 */
	public void bind(ChannelHandlerContext ctx);
}
