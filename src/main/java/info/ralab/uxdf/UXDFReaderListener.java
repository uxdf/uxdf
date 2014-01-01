package info.ralab.uxdf;

import info.ralab.uxdf.instance.EventEntity;
import info.ralab.uxdf.instance.NodeEntity;

public interface UXDFReaderListener {

    void startReadUXDF();

    void readSd(Sd sd);

    void startReadNode();

    void readNode(NodeEntity nodeEntity) throws UXDFException;

    void endReadNode();

    void startReadEvent();

    void readEvent(EventEntity eventEntity) throws UXDFException;

    void endReadEvent();

    void endReadUXDF(Throwable error);
}
