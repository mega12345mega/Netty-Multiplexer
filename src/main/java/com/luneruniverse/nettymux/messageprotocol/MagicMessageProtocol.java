package com.luneruniverse.nettymux.messageprotocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.luneruniverse.nettymux.ProtocolDetectionResult;

import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;

/**
 * Detects protocols that start by sending a specific message sequence
 * @param <I> The type of messages that should be handled
 */
public class MagicMessageProtocol<I> implements MessageProtocol<I> {
	
	private final List<I> magic;
	private final boolean removeMagic;
	private final Consumer<ChannelPipeline> bind;
	
	/**
	 * @param magic The message sequence that starts this protocol
	 * @param removeMagic If the magic message sequence should be removed before data is passed to the protocol handlers
	 * @param bind Set up the pipeline for this protocol (see {@link #bind(ChannelPipeline)} for details)
	 */
	public MagicMessageProtocol(List<I> magic, boolean removeMagic, Consumer<ChannelPipeline> bind) {
		this.magic = new ArrayList<>(magic);
		this.removeMagic = removeMagic;
		this.bind = bind;
	}
	
	@Override
	public ProtocolDetectionResult attemptDetection(List<I> in) {
		if (in.size() < magic.size())
			return ProtocolDetectionResult.UNKNOWN;
		
		List<I> inMagic = in.subList(0, magic.size());
		if (!magic.equals(inMagic))
			return ProtocolDetectionResult.REJECTED;
		
		if (removeMagic) {
			inMagic.forEach(ReferenceCountUtil::release);
			in.clear();
		}
		
		return ProtocolDetectionResult.DETECTED;
	}
	
	@Override
	public void bind(ChannelPipeline pipeline) {
		bind.accept(pipeline);
	}
	
}
