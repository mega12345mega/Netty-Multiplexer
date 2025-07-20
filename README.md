# Netty Multiplexer
[![Release](https://jitpack.io/v/mega12345mega/Netty-Multiplexer.svg)](https://jitpack.io/#mega12345mega/Netty-Multiplexer)

Combine HTTP, WS, SSL/TLS, and custom protocols into one port

[Javadoc](https://jitpack.io/com/github/mega12345mega/Netty-Multiplexer/latest/javadoc/)

# Usage

Use `NettyByteMultiplexer` and `NettyMessageMultiplexer` to detect what protocol should be used.

## Example

See `NettyMuxServerTest` for a complete example:
```
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
```