package com.luneruniverse.nettymux.byteprotocol;

import java.util.function.Consumer;

import com.luneruniverse.nettymux.ProtocolDetectionResult;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;

public class MagicByteProtocol implements ByteProtocol {
	
	private final String alpnName;
	private final byte[] magic;
	private final boolean removeMagic;
	private final Consumer<ChannelPipeline> bind;
	
	public MagicByteProtocol(String alpnName, byte[] magic, boolean removeMagic, Consumer<ChannelPipeline> bind) {
		this.alpnName = alpnName;
		this.magic = magic;
		this.removeMagic = removeMagic;
		this.bind = bind;
	}
	
	@Override
	public String getAlpnName() {
		return alpnName;
	}
	
	@Override
	public ProtocolDetectionResult attemptDetection(ByteBuf in) {
		if (in.readableBytes() < magic.length)
			return ProtocolDetectionResult.UNKNOWN;
		
		byte[] bytes = new byte[magic.length];
		in.getBytes(in.readerIndex(), bytes);
		for (int i = 0; i < magic.length; i++) {
			if (bytes[i] != magic[i])
				return ProtocolDetectionResult.REJECTED;
		}
		
		if (removeMagic)
			in.skipBytes(magic.length);
		
		return ProtocolDetectionResult.DETECTED;
	}
	
	@Override
	public void bind(ChannelPipeline pipeline) {
		bind.accept(pipeline);
	}
	
}
