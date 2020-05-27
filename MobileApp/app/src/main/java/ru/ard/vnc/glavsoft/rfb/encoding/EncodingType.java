package ru.ard.vnc.glavsoft.rfb.encoding;

import androidx.core.view.InputDeviceCompat;
import java.util.LinkedHashSet;

public enum EncodingType {
    RAW_ENCODING(0, "Raw"),
    COPY_RECT(1, "CopyRect"),
    RRE(2, "RRE"),
    HEXTILE(5, "Hextile"),
    ZLIB(6, "ZLib"),
    TIGHT(7, "Tight"),
    ZRLE(16, "ZRLE"),
    RICH_CURSOR(-239, "RichCursor"),
    DESKTOP_SIZE(-223, "DesctopSize"),
    CURSOR_POS(-232, "CursorPos"),
    COMPRESS_LEVEL_0(InputDeviceCompat.SOURCE_ANY, "CompressionLevel0"),
    COMPRESS_LEVEL_1(-255, "CompressionLevel1"),
    COMPRESS_LEVEL_2(-254, "CompressionLevel2"),
    COMPRESS_LEVEL_3(-253, "CompressionLevel3"),
    COMPRESS_LEVEL_4(-252, "CompressionLevel4"),
    COMPRESS_LEVEL_5(-251, "CompressionLevel5"),
    COMPRESS_LEVEL_6(-250, "CompressionLevel6"),
    COMPRESS_LEVEL_7(-249, "CompressionLevel7"),
    COMPRESS_LEVEL_8(-248, "CompressionLevel8"),
    COMPRESS_LEVEL_9(-247, "CompressionLevel9"),
    JPEG_QUALITY_LEVEL_0(-32, "JpegQualityLevel0"),
    JPEG_QUALITY_LEVEL_1(-31, "JpegQualityLevel1"),
    JPEG_QUALITY_LEVEL_2(-30, "JpegQualityLevel2"),
    JPEG_QUALITY_LEVEL_3(-29, "JpegQualityLevel3"),
    JPEG_QUALITY_LEVEL_4(-28, "JpegQualityLevel4"),
    JPEG_QUALITY_LEVEL_5(-27, "JpegQualityLevel5"),
    JPEG_QUALITY_LEVEL_6(-26, "JpegQualityLevel6"),
    JPEG_QUALITY_LEVEL_7(-25, "JpegQualityLevel7"),
    JPEG_QUALITY_LEVEL_8(-24, "JpegQualityLevel8"),
    JPEG_QUALITY_LEVEL_9(-23, "JpegQualityLevel9");
    
    public static LinkedHashSet<EncodingType> compressionEncodings = new LinkedHashSet<>();
    public static LinkedHashSet<EncodingType> ordinaryEncodings = new LinkedHashSet<>();
    public static LinkedHashSet<EncodingType> pseudoEncodings = new LinkedHashSet<>();
    private int id;
    private final String name;

    static {
        ordinaryEncodings.add(TIGHT);
        ordinaryEncodings.add(HEXTILE);
        ordinaryEncodings.add(ZRLE);
        ordinaryEncodings.add(ZLIB);
        ordinaryEncodings.add(RRE);
        ordinaryEncodings.add(COPY_RECT);
        pseudoEncodings.add(RICH_CURSOR);
        pseudoEncodings.add(CURSOR_POS);
        pseudoEncodings.add(DESKTOP_SIZE);
        compressionEncodings.add(COMPRESS_LEVEL_0);
        compressionEncodings.add(JPEG_QUALITY_LEVEL_0);
    }

    private EncodingType(int id2, String name2) {
        this.id = id2;
        this.name = name2;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static EncodingType byId(int id2) {
        EncodingType[] values = values();
        for (EncodingType type : values) {
            if (type.getId() == id2) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported encoding id: " + id2);
    }
}
