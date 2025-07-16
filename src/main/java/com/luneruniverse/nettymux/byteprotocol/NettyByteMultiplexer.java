package com.luneruniverse.nettymux.byteprotocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;

public class NettyByteMultiplexer extends ByteToMessageDecoder {
	
	public static class Builder {
		private final List<ByteProtocol> protocols;
		private SslContext ssl;
		private boolean forceSsl;
		
		public Builder() {
			protocols = new ArrayList<>();
		}
		
		public Builder addProtocol(ByteProtocol protocol) {
			protocols.add(protocol);
			return this;
		}
		public Builder addProtocols(ByteProtocol... protocols) {
			for (ByteProtocol protocol : protocols)
				this.protocols.add(protocol);
			return this;
		}
		
		public Builder forceSsl(SslContext ssl) {
			Objects.requireNonNull(ssl, "ssl");
			this.ssl = ssl;
			this.forceSsl = true;
			return this;
		}
		public Builder optionalSsl(SslContext ssl) {
			Objects.requireNonNull(ssl, "ssl");
			this.ssl = ssl;
			this.forceSsl = false;
			return this;
		}
		public Builder noSsl() {
			this.ssl = null;
			this.forceSsl = false;
			return this;
		}
		
		public NettyByteMultiplexer build() {
			if (protocols.isEmpty())
				throw new IllegalStateException("There are no protocols registered!");
			
			return new NettyByteMultiplexer(new ArrayList<>(protocols), ssl, forceSsl);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	private final List<ByteProtocol> protocols;
	private final SslContext ssl;
	private final boolean forceSsl;
	
	private NettyByteMultiplexer(List<ByteProtocol> protocols, SslContext ssl, boolean forceSsl) {
		this.protocols = protocols;
		this.ssl = ssl;
		this.forceSsl = forceSsl;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (ssl != null) {
			if (in.getByte(in.readerIndex()) == 0x16) {
				ctx.pipeline().addLast(ssl.newHandler(ctx.alloc()));
				ctx.pipeline().addLast(new ApplicationProtocolNegotiationHandler("") {
					@Override
					protected void configurePipeline(ChannelHandlerContext ctx, String selectedProtocol) throws Exception {
						if (selectedProtocol.isEmpty()) {
							ctx.pipeline().addLast(new NettyByteMultiplexer(protocols, null, false));
							return;
						}
						
						for (ByteProtocol protocol : protocols) {
							if (selectedProtocol.equals(protocol.getAlpnName())) {
								protocol.bind(ctx.pipeline());
								return;
							}
						}
						
						throw new InvalidByteProtocolException(InvalidByteProtocolException.Type.INVALID_ALPN_PROTOCOL);
					}
				});
				ctx.pipeline().remove(this);
				return;
			} else if (forceSsl)
				throw new InvalidByteProtocolException(InvalidByteProtocolException.Type.NOT_SSL_WHEN_FORCED);
		}
		
		for (Iterator<ByteProtocol> i = protocols.iterator(); i.hasNext();) {
			ByteProtocol protocol = i.next();
			
			switch (protocol.attemptDetection(in)) {
				case DETECTED:
					protocol.bind(ctx.pipeline());
					ctx.pipeline().remove(this);
					return;
				case UNKNOWN:
					break;
				case REJECTED:
					i.remove();
					break;
			}
		}
		
		if (protocols.isEmpty())
			throw new InvalidByteProtocolException(InvalidByteProtocolException.Type.FAILED_TO_DETECT);
	}
	
}
