package com.github.mikesafonov;

import com.github.mikesafonov.histo.HistogramPyramid3D;
import com.github.mikesafonov.histo.HistogramPyramidBuffer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLBuffer;
import com.jogamp.opencl.gl.CLGLContext;
import lombok.RequiredArgsConstructor;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

@RequiredArgsConstructor
public class MarchingCubes {
    private final int isolevel;


    public FloatBuffer run(CLGLContext context, CLDevice device,
                           boolean use3dTexture, int size, short[] data,
                           int shaderVboId) {
        var queue = device.createCommandQueue();


        var program = CLProgramBuilder.build(context, use3dTexture, device, size);

        ShortBuffer shortBuffer = ShortBuffer.wrap(data);
        var rawData =
                context.createImage3d(shortBuffer, size, size, size, 0, 0,
                        new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.SIGNED_INT16),
                        CLMemory.Mem.READ_ONLY, CLMemory.Mem.COPY_BUFFER);

        var histogramPyramid = (use3dTexture)
                ? new HistogramPyramid3D(program)
                : new HistogramPyramidBuffer(program);

        var traverseHPKernel = program.createCLKernel("traverseHP");

        histogramPyramid.createImages(context, size);
        int trianglesCount = histogramPyramid.perform(queue, rawData, isolevel, size);

        // Make OpenCL buffer from OpenGL buffer
        int i = 0;
        if (use3dTexture) {
            List<CLImage3d> images = ((HistogramPyramid3D) histogramPyramid).getImages();
            for (i = 0; i < images.size(); i++) {
                traverseHPKernel.setArg(i, images.get(i));
            }
        } else {

            HistogramPyramidBuffer histogramPyramidBuffer = (HistogramPyramidBuffer) histogramPyramid;
            List<CLBuffer> buffers = histogramPyramidBuffer.getBuffers();
            traverseHPKernel.setArg(0, rawData);
            traverseHPKernel.setArg(1, histogramPyramidBuffer.getCubeIndexesBuffer());
            for (i = 0; i < buffers.size(); i++) {
                traverseHPKernel.setArg(i + 2, buffers.get(i));
            }
            i += 2;
        }

        int glBufferSize = trianglesCount * 18 * Properties.FLOAT_SIZE;
        CLGLBuffer VBOBuffer = context.createFromGLBuffer(shaderVboId, glBufferSize, CLMemory.Mem.WRITE_ONLY);
        traverseHPKernel.setArg(i++, VBOBuffer);
        traverseHPKernel.setArg(i++, (float) isolevel);
        traverseHPKernel.setArg(i++, trianglesCount);


        queue.putAcquireGLObject(VBOBuffer);

        // Increase the global_work_size so that it is divideable by 64
        int global_work_size = trianglesCount + 64 - (trianglesCount - 64 * (trianglesCount / 64));
        // Run a NDRange kernel over this buffer which traverses back to the base level
        queue.putNDRangeKernel(traverseHPKernel, 1,
                null, PointerBuffer.allocateDirect(1).put(global_work_size).rewind(), PointerBuffer.allocateDirect(1).put(64).rewind());


        VBOBuffer.use(Buffers.newDirectFloatBuffer(18 * trianglesCount));
        queue.putReadBuffer(VBOBuffer, true);

        queue.putReleaseGLObject(VBOBuffer);

        queue.flush();

        histogramPyramid.clean();

        return (FloatBuffer) VBOBuffer.getBuffer();
    }
}
