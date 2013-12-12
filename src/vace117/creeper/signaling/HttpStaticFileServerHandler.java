package vace117.creeper.signaling;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.activation.MimetypesFileTypeMap;

import vace117.creeper.logging.Logger;
import android.app.Activity;
import android.content.res.AssetFileDescriptor;

/**
 * Acts as a very simple file server. We use it to send the HTML page to the browser.
 * 
 * @author Val Blant
 */
public class HttpStaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private final String defaultFileName;
	private final Activity mainActivity;

	public HttpStaticFileServerHandler(String defaultFileName, Activity mainActivity) {
		super();
		this.defaultFileName = defaultFileName;
		this.mainActivity = mainActivity;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (!request.getDecoderResult().isSuccess()) {
			sendError(ctx, BAD_REQUEST);
			return;
		}

		if (request.getMethod() != GET) {
			sendError(ctx, METHOD_NOT_ALLOWED);
			return;
		}

		String uri = request.getUri();

		if (uri.contains("websocket")) { // Not for us - let WebSocket handle this
			Logger.info("Routing WebSocket request...");
			ctx.fireChannelRead(request);
		} else {
			if (uri.length() < 2) {
				uri = defaultFileName;
			} 

			Logger.info("Processing request for {}...", uri);
			AssetFileDescriptor fd = null;
			try {
				fd = readAsset(uri);
			} catch (IOException e) {
				sendError(ctx, NOT_FOUND, uri);
				Logger.error(e);
			}
		

			if ( fd == null ) {
				sendError(ctx, NOT_FOUND, uri);
			}
			else {
				Logger.info("Transmitting {}...", uri);
				
				long fileLength = fd.getLength();
				FileInputStream fileInputStream = new FileInputStream(fd.getFileDescriptor());

				HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
				setContentLength(response, fileLength);
				setContentTypeHeader(response, uri);
				if (isKeepAlive(request)) {
					response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
				}

				// Write the initial line and the header.
				ctx.write(response);

				// Write the content.
				ctx.write(
						new DefaultFileRegion(fileInputStream.getChannel(), 0, fileLength), 
						ctx.newProgressivePromise());

				// Write the end marker
				ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

				fileInputStream.close();
			}
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			sendError(ctx, INTERNAL_SERVER_ERROR);
		}
	}

	private AssetFileDescriptor readAsset(String uri) throws IOException {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e1) {
				throw new Error();
			}
		}

		// Convert file separators.
		uri = uri.replace('/', File.separatorChar);

		if (!uri.startsWith("/web")) {
			return null; // Disallow anything that is not under /web
		}
		
		uri = uri.substring(1) + ".jpg";

		return mainActivity.getAssets().openFd(uri);
	}

	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		sendError(ctx, status, null);
	}
	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String uri) {
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
				status, Unpooled.copiedBuffer("Failure: " + status.toString()
						+ "\r\n", CharsetUtil.UTF_8));
		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		
		if (uri != null) Logger.warn("{} cannot be transmitted!", uri);
	}

	/**
	 * Sets the content type header for the HTTP Response
	 * 
	 * @param file file to extract content type from
	 */
	private static void setContentTypeHeader(HttpResponse response, String uri) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		String mimeType = mimeTypesMap.getContentType(uri);
		response.headers().set(CONTENT_TYPE, mimeType);
	}

}
