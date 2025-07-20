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
        .addProtocol(new MagicByteProtocol("magic", "magic".getBytes(), true, ctx -> {
            System.out.println("bind magic");
        }))
        .addProtocol(new HttpByteProtocol(ctx -> {
            System.out.println("http ...");
            ctx.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(65536));
            ctx.pipeline().addLast(NettyMessageMultiplexer.builder(FullHttpRequest.class)
                    .addProtocol(new NormalHttpMessageProtocol(ctx2 -> {
                        System.out.println("bind normal http");
                    }))
                    .addProtocol(new WebSocketHttpMessageProtocol(ctx2 -> {
                        System.out.println("bind websocket");
                    }))
                    .build());
        }))
        .optionalSsl(ssl)
        .build());
```