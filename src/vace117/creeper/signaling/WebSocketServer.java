/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package vace117.creeper.signaling;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import vace117.creeper.logging.Logger;
import vace117.creeper.webrtc.PeerConnectionManager;
import android.app.Activity;

/**
 * A WebSocket Server that responds to requests at:
 *
 * <pre>
 * http://localhost:8080/websocket
 * </pre>
 *
 */
public class WebSocketServer {
    private final int port;
    private final Activity mainActivity;
    
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();


    public WebSocketServer(int port, Activity mainActivity) {
        this.port = port;
        this.mainActivity = mainActivity;
    }
    
    public void run() throws Exception {
        try {
            final ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(final SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                        new HttpRequestDecoder(),
                        new HttpObjectAggregator(65536),
                        new HttpResponseEncoder(),
                        new HttpStaticFileServerHandler("/web/creeper.html", mainActivity),
                        new WebSocketConnectionObserver("/websocket"),
                        new WebSocketMessageHandler());
                }
            });

            final Channel ch = sb.bind(port).sync().channel();
            Logger.info("Web server started on port {}", port);

            ChannelFuture closeFuture = ch.closeFuture().sync();
            closeFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					shutdown();
				}
			});
        }
        catch (Exception e) {
        	Logger.error("Couldn't start Web Server!", e);
        }
        finally {
        	shutdown();
        }
    }
    
    public void shutdown() {
    	shutdownPeerConnectionManager();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private final void shutdownPeerConnectionManager() {
    	Logger.info("Shutting Down...");
    	
    	if ( PeerConnectionManager.isPeerConnectionManagerAvailable() ) {
    		PeerConnectionManager.getInstance().shutDown();
    	}
    }


}
