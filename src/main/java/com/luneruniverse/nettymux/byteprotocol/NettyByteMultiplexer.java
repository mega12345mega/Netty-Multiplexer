package com.luneruniverse.nettymux.byteprotocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;

/**
 * A {@link ChannelInboundHandler} that identifies the protocol by the incoming bytes and calls
 * {@link ByteProtocol#bind(ChannelHandlerContext)} once identified. If none of the protocols are matched, an
 * {@link InvalidByteProtocolException} is thrown with a corresponding {@link InvalidByteProtocolException.Type}.
 */
public class NettyByteMultiplexer extends ByteToMessageDecoder {
	
	/**
	 * Creates {@link NettyByteMultiplexer}s
	 */
	public static class Builder {
		private final List<ByteProtocol> protocols;
		private SslContext ssl;
		private boolean forceSsl;
		
		public Builder() {
			protocols = new ArrayList<>();
		}
		
		/**
		 * @param protocol A possible incoming protocol
		 * @return this
		 */
		public Builder addProtocol(ByteProtocol protocol) {
			protocols.add(protocol);
			return this;
		}
		/**
		 * @param protocols Possible incoming protocols
		 * @return this
		 */
		public Builder addProtocols(ByteProtocol... protocols) {
			for (ByteProtocol protocol : protocols)
				this.protocols.add(protocol);
			return this;
		}
		
		/**
		 * Require all incoming connections to use SSL; connections that don't request SSL will result in a
		 * {@link InvalidByteProtocolException} with the type being
		 * {@link InvalidByteProtocolException.Type#NOT_SSL_WHEN_FORCED}
		 * @param ssl The server's SSL data
		 * @return this
		 */
		public Builder forceSsl(SslContext ssl) {
			Objects.requireNonNull(ssl, "ssl");
			this.ssl = ssl;
			this.forceSsl = true;
			return this;
		}
		/**
		 * Allow incoming connections to use SSL
		 * @param ssl The server's SSL data
		 * @return this
		 */
		public Builder optionalSsl(SslContext ssl) {
			Objects.requireNonNull(ssl, "ssl");
			this.ssl = ssl;
			this.forceSsl = false;
			return this;
		}
		/**
		 * <em>This is the default behavior</em><br>
		 * <br>
		 * Don't check if incoming connections use SSL; if the first byte is 0x16, this is passed along to the protocols
		 * like any other byte rather than causing SSL handlers to be bound to the channel pipeline
		 * @return this
		 */
		public Builder noSsl() {
			this.ssl = null;
			this.forceSsl = false;
			return this;
		}
		
		/**
		 * @return A {@link NettyByteMultiplexer} with the added protocols and SSL settings
		 * @throws IllegalStateException If no protocols were added (regardless of SSL settings)
		 */
		public NettyByteMultiplexer build() throws IllegalStateException {
			if (protocols.isEmpty())
				throw new IllegalStateException("There are no protocols registered!");
			
			return new NettyByteMultiplexer(new ArrayList<>(protocols), ssl, forceSsl);
		}
	}
	
	/**
	 * @return A {@link Builder} to create a {@link NettyByteMultiplexer}
	 */
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
				String sslHandlerName = ctx.name() + "#sslHandler";
				ctx.pipeline().addAfter(ctx.name(), sslHandlerName, ssl.newHandler(ctx.alloc()));
				ctx.pipeline().addAfter(sslHandlerName, null, new ApplicationProtocolNegotiationHandler("") {
					@Override
					protected void configurePipeline(ChannelHandlerContext ctx, String selectedProtocol) throws Exception {
						if (selectedProtocol.isEmpty()) {
							ctx.pipeline().addAfter(ctx.name(), null, new NettyByteMultiplexer(protocols, null, false));
							return;
						}
						
						for (ByteProtocol protocol : protocols) {
							if (selectedProtocol.equals(protocol.getAlpnName())) {
								protocol.bind(ctx);
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
					protocol.bind(ctx);
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
