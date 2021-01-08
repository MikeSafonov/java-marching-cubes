package com.github.mikesafonov;

import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.gl.CLGLContext;
import lombok.experimental.UtilityClass;

import java.nio.IntBuffer;

@UtilityClass
public class BufferHelper {

    public static CLBuffer createBuffer(CLGLContext context, int bufferSize, int sizeOfType, int flag) {
        CLBuffer b = context.createBuffer(sizeOfType * bufferSize, flag);
        b.use(Buffers.newDirectIntBuffer(bufferSize));
        return b;
    }


    public static int countBuffer(CLBuffer buffer) {
        try {
            IntBuffer intBuffer = (IntBuffer) buffer.getBuffer();
            return countBuffer(intBuffer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    public static int countBuffer(IntBuffer buffer) {
        int totalSum = 0;
        for (int i = 0; i < buffer.limit(); i++) {
            totalSum += buffer.get(i);
        }
        return totalSum;
    }

    public static PointerBuffer allocateDirectBuffer(long[] workgroup) {
        return PointerBuffer.allocateDirect(workgroup.length).put(workgroup, 0, workgroup.length).rewind();
    }
}
