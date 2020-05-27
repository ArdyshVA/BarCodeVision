package ru.ard.vnc.glavsoft.rfb.encoding.decoder;

import ru.ard.vnc.glavsoft.rfb.encoding.EncodingType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DecodersContainer {
    private static Map<EncodingType, Class<? extends Decoder>> knownDecoders = new HashMap();
    private final Map<EncodingType, Decoder> decoders = new HashMap();

    static {
        knownDecoders.put(EncodingType.TIGHT, TightDecoder.class);
        knownDecoders.put(EncodingType.HEXTILE, HextileDecoder.class);
        knownDecoders.put(EncodingType.ZRLE, ZRLEDecoder.class);
        knownDecoders.put(EncodingType.ZLIB, ZlibDecoder.class);
        knownDecoders.put(EncodingType.RRE, RREDecoder.class);
        knownDecoders.put(EncodingType.COPY_RECT, CopyRectDecoder.class);
    }

    public DecodersContainer() {
        addMandatoryDecoders();
    }

    private void addMandatoryDecoders() {
        this.decoders.put(EncodingType.RAW_ENCODING, RawDecoder.getInstance());
    }

    public void initDecodersWhenNeeded(Collection<EncodingType> encodings) {
        for (EncodingType enc : encodings) {
            if (EncodingType.ordinaryEncodings.contains(enc) && !this.decoders.containsKey(enc)) {
                try {
                    this.decoders.put(enc, knownDecoders.get(enc).newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public Decoder getDecoderByType(EncodingType type) {
        return this.decoders.get(type);
    }

    public void resetDecoders() {
        for (Decoder decoder : this.decoders.values()) {
            if (decoder != null) {
                decoder.reset();
            }
        }
    }
}
