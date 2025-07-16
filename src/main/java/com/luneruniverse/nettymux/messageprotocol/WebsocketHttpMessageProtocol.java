package com.luneruniverse.nettymux.messageprotocol;

import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.nettymux.ProtocolDetectionResult;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

public class WebsocketHttpMessageProtocol implements MessageProtocol<FullHttpRequest> {
	
	private final Consumer<ChannelPipeline> bind;
	
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
