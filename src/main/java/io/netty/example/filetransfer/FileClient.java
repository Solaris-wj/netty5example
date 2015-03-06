package io.netty.example.filetransfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedFile;

public class FileClient {

	public void connect(int port, String host) throws Exception {
		// 网络事件处理线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 配置客户端启动类
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)// 设置封包
															// 使用一次大数据的写操作，而不是多次小数据的写操作
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
//							ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
							
							ch.pipeline().addLast(
								new LineBasedFrameDecoder(8192));
							ch.pipeline().addLast(new StringDecoder());
							ch.pipeline().addLast(new StringEncoder());
//
//							
							ch.pipeline().addLast(new MyHandler());
							

						}
					});
			// 连接服务器 同步等待成功
			ChannelFuture f = b.connect(host, port).sync();

			f.channel().closeFuture().sync();

		} finally {
			// 释放线程组资源
			group.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 8080;
		// ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

		FileClient cl = new FileClient();
		cl.connect(port, "127.0.0.1");

	}

}

class MyHandler extends ChannelHandlerAdapter{

	@Override
	public void channelActive(ChannelHandlerContext ctx){
		System.out.println("channelActived");
		ctx.writeAndFlush("C:/1.txt\n");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
		System.out.println("Message received");
		
		System.out.println(msg.getClass());
		//System.out.println(msg instanceof FileRegion);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}