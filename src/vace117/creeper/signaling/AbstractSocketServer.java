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

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import vace117.creeper.logging.CreeperContext;
import android.app.Activity;

/**
 * Starts up a Netty server on the specified port and provides some convenience methods to implementors.
 * 
 * The pipeline is put together by the implementors.
 *
 * @author Val Blant
 */
public abstract class AbstractSocketServer {

    protected final int port;
    protected final Activity mainActivity;
    
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();


    public AbstractSocketServer(int port, Activity mainActivity) {
        this.port = port;
        this.mainActivity = mainActivity;
        
    }
    
    /**
     * Initialize the channel pipeline. This defines what this socket server actually does.
     */
    protected abstract ChannelInitializer<SocketChannel> createChannelInitializer();
    
    /**
     * Allows the implementor to announce it's availability to the world.
     */
    protected abstract void sayHello() throws SocketException;
    
    public void run() throws Exception {
        try {
            final ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(createChannelInitializer());
            
            // Launch the socket listening thread
            final Channel ch = sb.bind(port).sync().channel();
            CreeperContext.getInstance().info("Socket server started on port {}", port);
            
            // Print connect info
            sayHello();

            // Shutdown handler 
            ChannelFuture closeFuture = ch.closeFuture().sync();
            closeFuture.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					shutdown();
				}
			});
        }
        catch (Exception e) {
        	CreeperContext.getInstance().error("Couldn't start Web Server!", e);
        }
        finally {
        	shutdown();
        }
    }
    
    /**
     * Enumerates all interfaces and collects all IP addresses that are up and have IPv4 addresses on them
     * 
     * @throws SocketException
     */
    protected Set<String> findActiveInterfaces() throws SocketException {
        Set<String> activeAddresses = new HashSet<String>();
        Enumeration<NetworkInterface> allInterfaces = NetworkInterface.getNetworkInterfaces();
        while ( allInterfaces.hasMoreElements() ) {
        	NetworkInterface nif = allInterfaces.nextElement();
        	if ( nif.isUp() && !nif.isVirtual() && !nif.isLoopback() ) {
            	for ( InterfaceAddress addr : nif.getInterfaceAddresses() ) {
            		String address = addr.getAddress().getHostAddress();
            		if ( !address.contains(":") ) {
            			activeAddresses.add(address);
            		}
            	}
        	}
        }
        
        return activeAddresses;
    }
    
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
