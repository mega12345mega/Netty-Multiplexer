package com.luneruniverse.nettymux.messageprotocol;

import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.nettymux.ProtocolDetectionResult;
import com.luneruniverse.nettymux.byteprotocol.HttpByteProtocol;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * Detects {@link FullHttpRequest}s that are upgraded to WebSocket (you may want to precede this with a
 * {@link HttpByteProtocol} check)
 */
public class WebsocketHttpMessageProtocol implements MessageProtocol<FullHttpRequest> {
	
	private final Consumer<ChannelPipeline> bind;
	
	/**
	 * @param bind Set up the pipeline for a WebSocket (see {@link #bind(ChannelPipeline)} for details)
	 */
	public WebsocketHttpMessageProtocol(Consumer<ChannelPipeline> bind) {
		this.bind = bind;
	}
	
	@Override
	public ProtocolDetectionResult attemptDetection(List<FullHttpRequest> in) {
		HttpHeaders headers = in.get(0).headers();
		return HttpHeaderNames.UPGRADE.contentEqualsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
				"WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE)) ?
				ProtocolDetectionResult.DETECTED : ProtocolDetectionResult.REJECTED;
	}
	
	@Override
	public void bind(ChannelPipeline pipeline) {
		bind.accept(pipeline);
	}
	
}
