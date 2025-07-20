package com.luneruniverse.nettymux.messageprotocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * A {@link ChannelInboundHandler} that identifies the protocol by the incoming messages and calls
 * {@link MessageProtocol#bind(ChannelHandlerContext)} once identified. If none of the protocols are matched, an
 * {@link InvalidMessageProtocolException} is thrown.
 * @param <I> The type of messages that should be handled
 */
public class NettyMessageMultiplexer<I> extends MessageToMessageDecoder<I> {
	
	/**
	 * Creates {@link NettyMessageMultiplexer}s
	 * @param <I> The type of messages that should be handled
	 */
	public static class Builder<I> {
		private final Class<I> clazz;
		private final List<MessageProtocol<I>> protocols;
		
		public Builder(Class<I> clazz) {
			this.clazz = clazz;
			this.protocols = new ArrayList<>();
		}
		
		/**
		 * @param protocol A possible incoming protocol
		 * @return this
		 */
		public Builder<I> addProtocol(MessageProtocol<I> protocol) {
			protocols.add(protocol);
			return this;
		}
		/**
		 * @param protocols Possible incoming protocols
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public Builder<I> addProtocols(MessageProtocol<I>... protocols) {
			for (MessageProtocol<I> protocol : protocols)
				this.protocols.add(protocol);
			return this;
		}
		
		/**
		 * @return A {@link NettyMessageMultiplexer} with the added protocols
		 * @throws IllegalStateException If no protocols were added
		 */
		public NettyMessageMultiplexer<I> build() throws IllegalStateException {
			if (protocols.isEmpty())
				throw new IllegalStateException("There are no protocols registered!");
			
			return new NettyMessageMultiplexer<>(clazz, new ArrayList<>(protocols));
		}
	}
	
	/**
	 * @param <I> The type of messages that should be handled
	 * @param clazz The type of messages that should be handled
	 * @return A {@link Builder} to create a {@link NettyMessageMultiplexer}
	 */
	public static <I> Builder<I> builder(Class<I> clazz) {
		return new Builder<>(clazz);
	}
	
	private final List<MessageProtocol<I>> protocols;
	private final List<I> in;
	
	private NettyMessageMultiplexer(Class<I> clazz, List<MessageProtocol<I>> protocols) {
		super(clazz);
		this.protocols = protocols;
		this.in = new ArrayList<>();
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception {
		ReferenceCountUtil.retain(msg);
		in.add(msg);
		
		for (Iterator<MessageProtocol<I>> i = protocols.iterator(); i.hasNext();) {
			MessageProtocol<I> protocol = i.next();
			
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
			throw new InvalidMessageProtocolException();
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		if (!in.isEmpty()) {
			for (I msg : in)
				ctx.fireChannelRead(msg);
			ctx.fireChannelReadComplete();
			in.clear();
		}
	}
	
}
