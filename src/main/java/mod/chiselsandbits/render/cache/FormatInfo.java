package mod.chiselsandbits.render.cache;

import com.mojang.blaze3d.vertex.VertexFormat;

public class FormatInfo {

    final int totalSize;
    final int faceSize;

    final int[] offsets;
    final int[] indexLengths;
    final int[] finalLengths;

    // 似乎是对VertexFormat的一种封装
    public FormatInfo(VertexFormat format) {
        int total = 0;
        indexLengths = new int[format.getElements().size()];
        finalLengths = new int[format.getElements().size()];
        offsets = new int[format.getElements().size()];

        for (int x = 0; x < indexLengths.length; ++x) {
            finalLengths[x] = format.getElements().get(x).getElementCount();
            indexLengths[x] = finalLengths[x];

            switch (format.getElements().get(x).getUsage()) {
                case GENERIC, PADDING -> indexLengths[x] = 0;
                case COLOR -> indexLengths[x] = 4;
                case NORMAL, POSITION -> indexLengths[x] = 3;
                case UV -> indexLengths[x] = 2;
            }

            offsets[x] = total;
            total += indexLengths[x];
        }

        this.totalSize = total;
        this.faceSize = total * 4;
    }

    public int[] pack(
            float[][][] unpackedData) {
        int[] out = new int[this.faceSize];

        int offset = 0;
        for (int f = 0; f < 4; ++f) {
            float[][] run2 = unpackedData[f];
            for (int x = 0; x < indexLengths.length; ++x) {
                float[] run = run2[x];
                for (int z = 0; z < indexLengths[x]; z++) {
                    if (run.length > z)
                        out[offset++] = Float.floatToRawIntBits(run[z]);
                    else
                        out[offset++] = 0;
                }
            }
        }

        return out;
    }

    public float[] unpack(
            int[] raw,
            int vertex,
            int index) {
        int size = indexLengths[index];
        float[] out = new float[size];
        int start = vertex * this.totalSize + offsets[index];

        for (int x = 0; x < size; x++) {
            out[x] = Float.intBitsToFloat(raw[start + x]);
        }

        return out;
    }

}
