package com.luneruniverse.nettymux.messageprotocol;

import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.nettymux.ProtocolDetectionResult;
import com.luneruniverse.nettymux.byteprotocol.HttpByteProtocol;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;

/**
 * Detects {@link FullHttpRequest}s that aren't upgraded (you may want to precede this with a {@link HttpByteProtocol} check)
 */
public class NormalHttpMessageProtocol implements MessageProtocol<FullHttpRequest> {
	
	private final Consumer<ChannelPipeline> bind;
	
	/**
	 * @param bind Set up the pipeline for un-upgraded HTTP (see {@link #bind(ChannelPipeline)} for details)
	 */
	public NormalHttpMessageProtocol(Consumer<ChannelPipeline> bind) {
		this.bind = bind;
	}
	
	@Override
	public ProtocolDetectionResult attemptDetection(List<FullHttpRequest> in) {
		return in.get(0).headers().get(HttpHeaderNames.UPGRADE) == null ?
				ProtocolDetectionResult.DETECTED : ProtocolDetectionResult.REJECTED;
	}
	
	@Override
	public void bind(ChannelPipeline pipeline) {
		bind.accept(pipeline);
	}
	
}
