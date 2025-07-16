package com.luneruniverse.nettymux.byteprotocol;

import com.luneruniverse.nettymux.ProtocolDetectionResult;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPipeline;

public interface ByteProtocol {
	public String getAlpnName();
	public ProtocolDetectionResult attemptDetection(ByteBuf in);
	public void bind(ChannelPipeline pipeline);
}
