package com.luneruniverse.nettymux.messageprotocol;

import java.util.List;

import com.luneruniverse.nettymux.ProtocolDetectionResult;

import io.netty.channel.ChannelPipeline;

public interface MessageProtocol<I> {
	public ProtocolDetectionResult attemptDetection(List<I> in);
	public void bind(ChannelPipeline pipeline);
}
