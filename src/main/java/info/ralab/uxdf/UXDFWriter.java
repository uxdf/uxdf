package info.ralab.uxdf;

import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.*;

/**
 * UXDF数据写出对象。将UXDF写出到流。<br />
 * 继承自{@link JSONWriter}
 */
public class UXDFWriter extends JSONWriter {

    /**
     * 构造UXDF写出对象。<br />
     * <p>
     * 设置输出流{@link OutputStream}构造。<br />
     * 使用默认字符编码{@link UXDF#CHARSET}。<br />
     * 使用默认JSON序列化功能:<br />
     * <ol>
     * <li>采用ISO8601日期格式{@link SerializerFeature#UseISO8601DateFormat}。</li>
     * <li>禁用引用{@link SerializerFeature#DisableCircularReferenceDetect}。</li>
     * </ol>
     * </p>
     * </p>
     *
     * @param outputStream 输出流
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFWriter(final OutputStream outputStream) throws UnsupportedEncodingException {
        this(outputStream, UXDF.CHARSET);
    }

    /**
     * 构造UXDF写出对象。<br />
     * <p>
     * 设置输出流{@link OutputStream}构造。<br />
     * 使用自定义字符编码。<br />
     * 使用默认JSON序列化功能:<br />
     * <ol>
     * <li>采用ISO8601日期格式{@link SerializerFeature#UseISO8601DateFormat}。</li>
     * <li>禁用引用{@link SerializerFeature#DisableCircularReferenceDetect}。</li>
     * </ol>
     * </p>
     * </p>
     *
     * @param outputStream 输出流
     * @param charset      指定字符编码
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFWriter(final OutputStream outputStream, final String charset) throws UnsupportedEncodingException {
        this(
                outputStream,
                charset,
                SerializerFeature.UseISO8601DateFormat,
                SerializerFeature.DisableCircularReferenceDetect
        );
    }

    /**
     * 构造UXDF写出对象。<br />
     * <p>
     * 设置输出流{@link OutputStream}构造。<br />
     * 使用默认字符编码{@link UXDF#CHARSET}。<br />
     * 使用指定JSON序列化功能。
     * </p>
     * </p>
     *
     * @param outputStream 输出流
     * @param features     JSON序列化功能
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFWriter(final OutputStream outputStream, final SerializerFeature... features) throws UnsupportedEncodingException {
        this(outputStream, UXDF.CHARSET, features);
    }

    /**
     * 构造UXDF写出对象。<br />
     * <p>
     * 设置输出流{@link OutputStream}构造。<br />
     * 使用自定义字符编码。<br />
     * 使用指定JSON序列化功能。
     * </p>
     * </p>
     *
     * @param outputStream 输出流
     * @param charset      指定字符编码
     * @param features     JSON序列化功能
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFWriter(
            final OutputStream outputStream, final String charset, final SerializerFeature... features
    ) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(outputStream, charset));
        for (int i = 0; features != null && i < features.length; i++) {
            SerializerFeature feature = features[i];
            this.config(feature, true);
        }
    }

    /**
     * 写出UXDF数据，数据按照先sd后data的顺序写出。输出格式未经美化<br />
     * <pre><code>
     * {
     *     "sd": {},
     *     "data": {
     *         "node": [],
     *         "event": []
     *     }
     * }
     * </code></pre>
     *
     * @param uxdf UXDF数据
     */
    public void writeUXDF(final UXDF uxdf) {
        this.startObject();

        this.writeKey(UXDF.KEY_SD);
        this.writeValue(uxdf.getSd());

        this.writeKey(UXDF.KEY_DATA);
        this.startObject();

        this.writeKey(SdData.KEY_NODE);
        this.startArray();
        uxdf.getData().getUnmodifiableNode().forEach(this::writeObject);
        this.endArray();

        this.writeKey(SdData.KEY_EVENT);
        this.startObject();
        uxdf.getData().getUnmodifiableEvent().forEach((eventName, eventEntities) -> {
            this.writeKey(eventName);
            this.startArray();
            eventEntities.forEach(this::writeObject);
            this.endArray();
        });
        this.endObject();

        this.endObject();

        this.endObject();
    }
}
