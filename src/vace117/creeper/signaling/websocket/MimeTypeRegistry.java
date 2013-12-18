package vace117.creeper.signaling.websocket;

import java.util.HashMap;
import java.util.Map;

/**
 * We need to write correct mime types in the HTTP response for different file extensions, so this registry provides 
 * that mapping.
 * 
 * @author Val Blant
 */
public class MimeTypeRegistry {
	private static Map<String, String> fileTypeMap = new HashMap<String, String>();
	static {
		fileTypeMap.put("html", "text/html");
		fileTypeMap.put("css", "text/css");
		fileTypeMap.put("js", "application/javascript");
	}

	public static String lookup(String ext) {
		return fileTypeMap.get(ext);
	}
}
