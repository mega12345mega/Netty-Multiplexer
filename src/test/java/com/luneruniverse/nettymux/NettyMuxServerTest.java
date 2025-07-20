package com.luneruniverse.nettymux;

import com.luneruniverse.nettymux.byteprotocol.HttpByteProtocol;
import com.luneruniverse.nettymux.byteprotocol.MagicByteProtocol;
import com.luneruniverse.nettymux.byteprotocol.NettyByteMultiplexer;
import com.luneruniverse.nettymux.messageprotocol.NettyMessageMultiplexer;
import com.luneruniverse.nettymux.messageprotocol.NormalHttpMessageProtocol;
import com.luneruniverse.nettymux.messageprotocol.WebSocketHttpMessageProtocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.pkitesting.CertificateBuilder;
import io.netty.pkitesting.X509Bundle;
import io.netty.util.ResourceLeakDetector;

public class NettyMuxServerTest {
	
	public static void main(String[] args) throws Exception {
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
		new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
				System.gc();
			}
		}, "Resource Leak Trigger").start();
		
		X509Bundle cert = new CertificateBuilder()
				.subject("CN=localhost")
				.setIsCertificateAuthority(true)
				.buildSelfSigned();
		SslContext ssl = SslContextBuilder.forServer(cert.toTempCertChainPem(), cert.toTempPrivateKeyPem())
				.applicationProtocolConfig(new ApplicationProtocolConfig(
						ApplicationProtocolConfig.Protocol.ALPN,
						ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
						ApplicationProtocolConfig.SelectedListenerFailureBehavior.FATAL_ALERT,
						"http/1.1", "magic"))
				.build();
		
		EventLoopGroup group = new MultiThreadIoEventLoopGroup(3, NioIoHandler.newFactory());
		try {
			ServerBootstrap server = new ServerBootstrap()
					.group(group)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel channel) throws Exception {
							channel.pipeline().addLast(NettyByteMultiplexer.builder()
									.addProtocol(new MagicByteProtocol("magic", "magic".getBytes(), true, pipeline -> {
										System.out.println("bind magic");
									}))
									.addProtocol(new HttpByteProtocol(pipeline -> {
										pipeline.addLast(new HttpServerCodec(), new HttpObjectAggregator(65536));
										pipeline.addLast(NettyMessageMultiplexer.builder(FullHttpRequest.class)
												.addProtocol(new NormalHttpMessageProtocol(pipeline2 -> {
													System.out.println("bind normal http");
												}))
												.addProtocol(new WebSocketHttpMessageProtocol(pipeline2 -> {
													System.out.println("bind websocket");
												}))
												.build());
									}))
									.optionalSsl(ssl)
									.build());
						}
					});
			ChannelFuture future = server.bind(1000).sync();
			future.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
	
}
