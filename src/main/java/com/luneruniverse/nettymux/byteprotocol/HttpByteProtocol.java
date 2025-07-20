package com.luneruniverse.nettymux.byteprotocol;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.luneruniverse.nettymux.ProtocolDetectionResult;
import com.luneruniverse.nettymux.messageprotocol.NettyMessageMultiplexer;
import com.luneruniverse.nettymux.messageprotocol.NormalHttpMessageProtocol;
import com.luneruniverse.nettymux.messageprotocol.WebSocketHttpMessageProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Detects HTTP/1.1 requests<br>
 * <br>
 * <strong>Warning:</strong> This doesn't check for the <code>Upgrade</code> header. Follow this with a
 * {@link NettyMessageMultiplexer} that includes the {@link NormalHttpMessageProtocol} to confirm that the request
 * is normal. Similarly, you can detect the WebSocket protocol by including the {@link WebSocketHttpMessageProtocol}.
 */
public class HttpByteProtocol implements ByteProtocol {
	
	private static final Set<String> METHODS;
	private static final int MIN_METHOD_LENGTH;
	private static final int MAX_METHOD_LENGTH;
	static {
		METHODS = new HashSet<>();
		METHODS.add("CONNECT");
		METHODS.add("DELETE");
		METHODS.add("GET");
		METHODS.add("HEAD");
		METHODS.add("OPTIONS");
		METHODS.add("PATCH");
		METHODS.add("POST");
		METHODS.add("PUT");
		METHODS.add("TRACE");
		
		MIN_METHOD_LENGTH = METHODS.stream().mapToInt(String::length).min().getAsInt();
		MAX_METHOD_LENGTH = METHODS.stream().mapToInt(String::length).max().getAsInt();
	}
	
	private final Consumer<ChannelHandlerContext> bind;
	
	/**
	 * @param bind Set up the pipeline for HTTP/1.1 (see {@link #bind(ChannelHandlerContext)} for details)
	 */
	public HttpByteProtocol(Consumer<ChannelHandlerContext> bind) {
		this.bind = bind;
	}
	
	@Override
	public String getAlpnName() {
		return "http/1.1";
	}
	
	@Override
	public ProtocolDetectionResult attemptDetection(ByteBuf in) {
		if (in.readableBytes() < MIN_METHOD_LENGTH)
			return ProtocolDetectionResult.UNKNOWN;
		
		byte[] bytes = new byte[Math.min(in.readableBytes(), MAX_METHOD_LENGTH)];
		in.getBytes(in.readerIndex(), bytes);
		String str = new String(bytes, StandardCharsets.US_ASCII);
		
		for (String method : METHODS) {
			if (str.startsWith(method))
				return ProtocolDetectionResult.DETECTED;
		}
		
		if (in.readableBytes() < MAX_METHOD_LENGTH)
			return ProtocolDetectionResult.UNKNOWN;
		return ProtocolDetectionResult.REJECTED;
	}
	
	@Override
	public void bind(ChannelHandlerContext ctx) {
		bind.accept(ctx);
	}
	
}
