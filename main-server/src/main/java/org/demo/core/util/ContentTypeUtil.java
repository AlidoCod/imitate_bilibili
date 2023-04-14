package org.demo.core.util;


import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.springframework.http.MediaType;

public class ContentTypeUtil {

    /**
     * 若传入Null值则返回初始MimeType
     * @param path
     * @return
     */
    public static String getContentType(String path) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        int position = path.lastIndexOf('.');
        if (position >= 0) {
            String extension = path.substring(position);
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null)
                contentType = extensionMatch.getMimeType();
        }

        return contentType;
    }
}
