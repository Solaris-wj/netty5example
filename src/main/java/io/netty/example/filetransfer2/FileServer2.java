package io.netty.example.filetransfer2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server that accept the path of a file an echo back its content.
 */
public class FileServer2 {

	private final int port;

	public FileServer2(int port) {
		this.port = port;
	}

	public void run() throws Exception {
		// Configure the server.
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 100)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {

							ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
							ch.pipeline().addLast(
									new LineBasedFrameDecoder(8192));
							ch.pipeline().addLast(new StringDecoder());
							
							ch.pipeline().addLast(new StringEncoder());

							ch.pipeline().addLast(new FileHandler());

						}
					});

			// Start the server.
			ChannelFuture f = b.bind(port).sync();

			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down all event loops to terminate all threads.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = 8080;
		}
		new FileServer2(port).run();
	}

	private static final class FileHandler extends
			SimpleChannelInboundHandler<String> {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, String msg)
				throws Exception {
			
			final ChannelHandlerContext cctx=ctx;

			
			System.out.println(msg);
			
			File file = new File("C:/1.txt");
			
			ctx.writeAndFlush(String.valueOf(file.length()));
			
			if (file.exists()) {
				if (!file.isFile()) {
					
					System.out.println("Not a file: " + file + '\n');
					return;
				}
				//ctx.write("file");
				
				ctx.pipeline().addFirst(new ChunkedWriteHandler());
				ChannelFuture f = ctx.writeAndFlush(new ChunkedFile(file));

				f.addListener(new ChannelFutureListener(){

					public void operationComplete(ChannelFuture future)
							throws Exception {
						// TODO Auto-generated method stub
						System.out.println("send chunked file succeed");
						cctx.pipeline().removeFirst();
						//cctx.flush();
					}
					
				});

                ExecutorService executor=Executors.newCachedThreadPool();
                
                //final ChannelHandlerContext cctx=ctx;
                int num=50;
                for(int i = 0 ; i !=num;++i){
                	
                	final int x=i;
                	executor.submit(new Callable<Void>(){

						public Void call() throws Exception {
							
							cctx.writeAndFlush("["+x+"]\n");
							
							return null;
						}
                		
                	});
                }
                
				ctx.flush();
			} else {
				ctx.writeAndFlush("File not found: " + file + '\n');
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
				throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
	}
}