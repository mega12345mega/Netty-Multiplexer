package com.luneruniverse.nettymux.messageprotocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

public class NettyMessageMultiplexer<I> extends MessageToMessageDecoder<I> {
	
	public static class Builder<I> {
		private final Class<I> clazz;
		private final List<MessageProtocol<I>> protocols;
		
		public Builder(Class<I> clazz) {
			this.clazz = clazz;
			this.protocols = new ArrayList<>();
		}
		
		public Builder<I> addProtocol(MessageProtocol<I> protocol) {
			protocols.add(protocol);
			return this;
		}
		@SuppressWarnings("unchecked")
		public Builder<I> addProtocols(MessageProtocol<I>... protocols) {
			for (MessageProtocol<I> protocol : protocols)
				this.protocols.add(protocol);
			return this;
		}
		
		public NettyMessageMultiplexer<I> build() {
			if (protocols.isEmpty())
				throw new IllegalStateException("There are no protocols registered!");
			
			return new NettyMessageMultiplexer<>(clazz, new ArrayList<>(protocols));
		}
	}
	
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
