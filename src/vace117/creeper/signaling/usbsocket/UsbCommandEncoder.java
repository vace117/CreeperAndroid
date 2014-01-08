package vace117.creeper.signaling.usbsocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import vace117.creeper.controller.command.CreeperCommand;

/**
 * Converts a <code>CreeperCommand</code> to a String in preparation for further encoding by Netty's <code>StringEncoder</code>
 *
 * @author Val Blant
 */
public class UsbCommandEncoder extends MessageToMessageEncoder<CreeperCommand> {

	@Override
	protected void encode(ChannelHandlerContext ctx, CreeperCommand msg, List<Object> out) throws Exception {
		out.add(msg.toString() + "\n");
	}


}
