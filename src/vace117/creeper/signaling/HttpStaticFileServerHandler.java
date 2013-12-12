package vace117.creeper.signaling;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
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
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.activation.MimetypesFileTypeMap;

import vace117.creeper.logging.Logger;

/**
 * Acts as a very simple file server. We use it to send the HTML page to the browser.
 * 
 * @author Val Blant
 */
public class HttpStaticFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private String defaultFileName;

	public HttpStaticFileServerHandler(String defaultFileName) {
		super();
		this.defaultFileName = defaultFileName;
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
			URL fileURL = sanitizeUri(uri);
			if (fileURL == null) {
				sendError(ctx, NOT_FOUND, uri);
				return;
			}

			File file = new File(fileURL.getFile());
			if (file.isHidden() || !file.exists()) {
				sendError(ctx, NOT_FOUND, uri);
				return;
			}

			if (!file.isFile()) {
				sendError(ctx, FORBIDDEN, uri);
				return;
			}
			
			Logger.info("Transmitting {}...", uri);

			RandomAccessFile raf;
			try {
				raf = new RandomAccessFile(file, "r");
			} catch (FileNotFoundException fnfe) {
				sendError(ctx, NOT_FOUND, uri);
				return;
			}
			long fileLength = raf.length();

			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
			setContentLength(response, fileLength);
			setContentTypeHeader(response, file);
			if (isKeepAlive(request)) {
				response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}

			// Write the initial line and the header.
			ctx.write(response);

			// Write the content.
			ctx.write(
					new DefaultFileRegion(raf.getChannel(), 0, fileLength), 
					ctx.newProgressivePromise());

			// Write the end marker
			ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)	throws Exception {
		cause.printStackTrace();
		if (ctx.channel().isActive()) {
			sendError(ctx, INTERNAL_SERVER_ERROR);
		}
	}

	private URL sanitizeUri(String uri) {
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

		return this.getClass().getResource(uri);
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
	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(CONTENT_TYPE,
				mimeTypesMap.getContentType(file.getPath()));
	}

}
