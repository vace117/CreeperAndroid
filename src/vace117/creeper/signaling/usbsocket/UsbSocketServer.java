package vace117.creeper.signaling.usbsocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.SocketException;
import java.util.Set;

import vace117.creeper.logging.CreeperContext;
import vace117.creeper.signaling.AbstractSocketServer;
import android.app.Activity;

/**
 * This socket expects a connection from Raspberry Pi on port 4444. 
 * 
 * This connection can be achieved by using USB tethering, or adb port forwarding. 
 *
 * @author Val Blant
 */
public class UsbSocketServer extends AbstractSocketServer {
	private static final int PORT = 4444;
	
	public UsbSocketServer(Activity mainActivity) {
		super(PORT, mainActivity);
	}

	@Override
	protected ChannelInitializer<SocketChannel> createChannelInitializer() {
		return new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
                ch.pipeline().addLast(
                	new LineBasedFrameDecoder(200), // 1) Split the incoming ByteBuf at new-lines 
                    new StringDecoder(), // 2) Convert the line from ByteBuf to String 
                    new UsbMessageDecoder(CreeperContext.getInstance().controller), // 3) Parse into a UsbMessage and hand off to controller 
                    
                    new StringEncoder(), // 2) Encode the String as ByteBuf 
                    new UsbCommandEncoder() // 1) Convert UsbCommand to String 
                );
            }
        };
	}

	@Override
	protected void sayHello() throws SocketException {
    	Set<String> activeAddresses = findActiveInterfaces();
    	
    	CreeperContext.getInstance().info("Creeper waiting for USB connection from Rasberry Pi at:");
        for ( String addr : activeAddresses ) {
        	CreeperContext.getInstance().info("   http://" + addr + ":" + port);
        }
	}

}
