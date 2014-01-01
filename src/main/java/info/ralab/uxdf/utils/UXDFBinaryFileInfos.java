package info.ralab.uxdf.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static info.ralab.uxdf.utils.UXDFBinaryFileInfo.*;

/**
 * UXDF二进制文件辅助类
 */
@Slf4j
public class UXDFBinaryFileInfos {

    public static UXDFBinaryFileInfo make(final InputStream inputStream) {
        try {
            // 读取文件信息头
            byte[] infoBytes = new byte[HEAD_LENGTH];
            int readLength = inputStream.read(infoBytes);
            // 非法数据
            if (readLength != HEAD_LENGTH) {
                log.error("二进制头信息长度[{}]错误。", readLength);
                return null;
            }
            JSONObject fileInfo = JSONObject.parseObject(new String(infoBytes));

            return new UXDFBinaryFileInfo() {

                @Override
                public String getName() {
                    return fileInfo.getString(PROP_NAME);
                }

                @Override
                public String getContentType() {
                    return fileInfo.getString(PROP_CONTENT_TYPE);
                }

                @Override
                public long getLength() {
                    return fileInfo.getLong(PROP_LENGTH);
                }

                @Override
                public InputStream getInputStream() {
                    return inputStream;
                }

                @Override
                public boolean isFile() {
                    return true;
                }
            };

        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    public static UXDFBinaryFileInfo make(final byte[] value) {
        return make(new ByteArrayInputStream(value));
    }

    public static InputStream convertToInputStream(final UXDFBinaryFileInfo fileInfo) throws IOException {

        return fileInfo.getInputStream();
    }
}
