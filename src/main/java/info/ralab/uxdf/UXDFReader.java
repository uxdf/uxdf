package info.ralab.uxdf;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.parser.Feature;
import info.ralab.uxdf.instance.EventEntity;
import info.ralab.uxdf.instance.NodeEntity;

import java.io.*;

/**
 * 基于输入流读取UXDF对象。继承自{@link JSONReader}。
 *
 * @see JSONReader
 */
public class UXDFReader extends JSONReader {

    /**
     * 构造UXDF读取对象。
     *
     * <p>
     * 通过输入流{@link InputStream}加载UXDF对象。<br />
     * 使用默认字符编码{@link UXDF#CHARSET}。<br />
     * 使用默认JSON解析功能:<br />
     * <ol>
     * <li>属性有序存储{@link Feature#OrderedField}。</li>
     * <li>采用ISO8601日期格式{@link Feature#AllowISO8601DateFormat}。</li>
     * </ol>
     * </p>
     *
     * @param inputStream 输入流。
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFReader(final InputStream inputStream) throws UnsupportedEncodingException {
        super(
                new InputStreamReader(inputStream, UXDF.CHARSET),
                Feature.OrderedField,
                Feature.AllowISO8601DateFormat,
                Feature.DisableCircularReferenceDetect
        );
    }


    /**
     * 构造UXDF读取对象。
     *
     * <p>
     * 通过输入流{@link InputStream}加载UXDF对象。<br />
     * 使用默认字符编码{@link UXDF#CHARSET}。<br />
     * 使用自定义JSON解析功能。
     * </p>
     *
     * @param inputStream 输入流。
     * @param features    JSON解析功能。
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFReader(final InputStream inputStream, Feature... features) throws UnsupportedEncodingException {
        super(
                new InputStreamReader(inputStream, UXDF.CHARSET),
                features
        );
    }

    /**
     * 构造UXDF读取对象。
     *
     * <p>
     * 通过输入流{@link InputStream}加载UXDF对象。<br />
     * 使用自定义字符编码。<br />
     * 使用默认JSON解析功能:<br />
     * <ol>
     * <li>属性有序存储{@link Feature#OrderedField}。</li>
     * <li>采用ISO8601日期格式{@link Feature#AllowISO8601DateFormat}。</li>
     * </ol>
     * </p>
     *
     * @param inputStream 输入流。
     * @param charset     自定义字符编码。
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFReader(final InputStream inputStream, final String charset) throws UnsupportedEncodingException {
        super(
                new InputStreamReader(inputStream, charset),
                Feature.OrderedField,
                Feature.AllowISO8601DateFormat,
                Feature.DisableCircularReferenceDetect
        );
    }

    /**
     * 构造UXDF读取对象。
     *
     * <p>
     * 通过输入流{@link InputStream}加载UXDF对象。<br />
     * 使用自定义字符编码。<br />
     * 使用自定义JSON解析功能。
     * </p>
     *
     * @param inputStream 输入流。
     * @param charset     自定义字符编码。
     * @param features    自定义JSON解析功能。
     * @throws UnsupportedEncodingException 使用了系统不支持的字符编码时
     */
    public UXDFReader(final InputStream inputStream, final String charset, Feature... features) throws UnsupportedEncodingException {
        super(new InputStreamReader(inputStream, charset), features);
    }

    /**
     * 一次性读取UXDF信息。
     * <p>
     * 读取的UXDF信息必须包含sd和data部分，并且顺序总是sd在前，data在后。<br />
     * <pre><code>
     * {
     *     "sd": {},
     *     "data": {}
     * }
     * </code></pre>
     * </p>
     * <p>
     * 读取UXDF时，会优先基于UXDF文件中的sd部分校验。
     * 如果在UXDF内容中sd内未找到，则会查找{@link UXDFLoader}中的sd定义内容。<br />
     * 读取data时会基于{@link SdData}校验数据合法性。
     * </p>
     * <p>
     * 当超大数据时，有可能引起内存溢出异常。
     * 超大数据建议使用{@link UXDFReader#readUXDF(info.ralab.uxdf.UXDFReaderListener)}。<br />
     * </p>
     *
     * @return 读取到的UXDF对象
     */
    public UXDF readUXDF() {
        // TODO 按照sd，data的顺序进行读取
        // TODO 支持根据UXDF内容中sd进行校验
        return this.readObject(UXDF.class);
    }

    /**
     * 流式读取UXDF信息。
     * <p>
     * 读取的UXDF信息必须包含sd和data部分，并且顺序总是sd在前，data在后。<br />
     * <pre><code>
     * {
     *     "sd": {},
     *     "data": {}
     * }
     * </code></pre>
     * </p>
     * <p>
     * 读取UXDF时，会优先基于UXDF文件中的sd部分校验。
     * 如果在UXDF内容中sd内未找到，则会查找{@link UXDFLoader}中的sd定义内容。
     * </p>
     * <p>
     * 流式读会依次读取sd，和data中每一个Node和Event。<br />
     * 在以下切点调用{@link UXDFReaderListener}中各种方法进行通知：<br />
     * <ul>
     * <li>开始读取UXDF{@link UXDFReaderListener#startReadUXDF()}</li>
     * <li>读取到sd{@link UXDFReaderListener#readSd(info.ralab.uxdf.Sd)}</li>
     * <li>开始读取node{@link UXDFReaderListener#startReadNode()}</li>
     * <li>读取到node{@link UXDFReaderListener#readNode(info.ralab.uxdf.instance.NodeEntity)}</li>
     * <li>结束读取node{@link UXDFReaderListener#endReadNode()}</li>
     * <li>开始读取event{@link UXDFReaderListener#startReadEvent()}</li>
     * <li>读取到event{@link UXDFReaderListener#readEvent(info.ralab.uxdf.instance.EventEntity)}</li>
     * <li>结束读取event{@link UXDFReaderListener#endReadEvent()}</li>
     * <li>结束读取UXDF{@link UXDFReaderListener#endReadUXDF(java.lang.Throwable)}</li>
     * </ul>
     * </p>
     *
     * @param uxdfReaderListener UXDF流式读取监听器
     */
    public void readUXDF(final UXDFReaderListener uxdfReaderListener) {
        assert uxdfReaderListener != null;

        // TODO UXDFReaderListener应该使用可变参数形式，允许传入多个。

        // 读取过程中产生的错误
        Throwable readError = null;

        try {
            uxdfReaderListener.startReadUXDF();
            this.startObject();
            String sdKey = this.readString();
            if (!UXDF.KEY_SD.equals(sdKey)) {
                throw new UXDFException("missing a part of sd");
            }
            uxdfReaderListener.readSd(this.readObject(Sd.class));
            String dataKey = this.readString();
            if (!UXDF.KEY_DATA.equals(dataKey)) {
                throw new UXDFException("missing a part of data");
            }

            this.startObject();
            while (this.hasNext()) {
                String entityKey = this.readString();
                if (SdData.KEY_NODE.equals(entityKey)) {
                    this.startArray();
                    uxdfReaderListener.startReadNode();
                    while (this.hasNext()) {
                        // NodeEntity
                        uxdfReaderListener.readNode(this.readObject(NodeEntity.class));
                    }
                    uxdfReaderListener.endReadNode();
                    this.endArray();
                } else if (SdData.KEY_EVENT.equals(entityKey)) {
                    this.startObject();
                    while (this.hasNext()) {
                        String eventName = this.readString();
                        if (eventName == null || eventName.isEmpty()) {
                            continue;
                        }
                        this.startArray();
                        uxdfReaderListener.startReadEvent();
                        while (this.hasNext()) {
                            // EventEntity
                            uxdfReaderListener.readEvent(this.readObject(EventEntity.class));
                        }
                        uxdfReaderListener.endReadEvent();
                        this.endArray();
                    }
                    this.endObject();
                }
            }
            this.endObject();
            this.endObject();
        } catch (Exception e) {
            readError = e;
        } finally {
            uxdfReaderListener.endReadUXDF(readError);
        }
    }
}
