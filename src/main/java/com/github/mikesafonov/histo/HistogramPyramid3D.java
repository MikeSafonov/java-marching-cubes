package com.github.mikesafonov.histo;

import com.github.mikesafonov.BufferHelper;
import com.github.mikesafonov.MathHelper;
import com.jogamp.common.nio.PointerBuffer;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLContext;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;


public class HistogramPyramid3D extends HistogramPyramid {

    private List<CLImage3d> images;


    public HistogramPyramid3D(CLProgram program) {
        super(program);

        images = new ArrayList<>();
    }

    @Override
    public void createImages(CLGLContext context, int size) {

        int bufferSize = size;
        // Make the two first buffers use INT8
        images.add(context.createImage3d(bufferSize, bufferSize, bufferSize,
                new CLImageFormat(CLImageFormat.ChannelOrder.RGBA, CLImageFormat.ChannelType.UNSIGNED_INT8),
                CLMemory.Mem.READ_WRITE));
        bufferSize /= 2;
        images.add(context.createImage3d(bufferSize, bufferSize, bufferSize,
                new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.UNSIGNED_INT8),
                CLMemory.Mem.READ_WRITE));
        bufferSize /= 2;
        // And the third, fourth and fifth INT16
        images.add(context.createImage3d(bufferSize, bufferSize, bufferSize,
                new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.UNSIGNED_INT16),
                CLMemory.Mem.READ_WRITE));
        bufferSize /= 2;
        images.add(context.createImage3d(bufferSize, bufferSize, bufferSize,
                new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.UNSIGNED_INT16),
                CLMemory.Mem.READ_WRITE));
        bufferSize /= 2;
        images.add(context.createImage3d(bufferSize, bufferSize, bufferSize,
                new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.UNSIGNED_INT16),
                CLMemory.Mem.READ_WRITE));
        bufferSize /= 2;
        // The rest will use INT32
        for (int i = 5; i < (MathHelper.log2(size)); i++) {
            if (bufferSize == 1)
                bufferSize = 2; // Image cant be 1x1x1
            images.add(context.createImage3d(bufferSize, bufferSize, bufferSize,
                    new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.UNSIGNED_INT32),
                    CLMemory.Mem.READ_WRITE));
            bufferSize /= 2;
        }


    }

    @Override
    public int perform(CLCommandQueue queue, CLImage3d rawData, float isolevel, int size) {
        histogramPyramidConstruction(queue, rawData, isolevel, size);

        CLImage3d lastImage = images.get(images.size() - 1);
        queue.putReadImage(lastImage, 0, 0,
                0, 0, 0, size, size, size, true);
        queue.finish();
        IntBuffer intBuffer = (IntBuffer) lastImage.getBuffer();

        int totalSum = BufferHelper.countBuffer(intBuffer);
        return totalSum;
    }


    private void histogramPyramidConstruction(CLCommandQueue queue, CLImage3d rawData, float isolevel, int size) {
        updateScalarField(queue, rawData, isolevel, size);

        // Run base to first level
        constructHPLevelKernel.setArg(0, images.get(0));
        constructHPLevelKernel.setArg(1, images.get(1));
        int previous = size / 2;
        queue.putNDRangeKernel(
                constructHPLevelKernel,
                1, null, PointerBuffer.allocateDirect(1).put(previous * previous * previous), null
        );


        // Run level 2 to top level
        for (int i = 1; i < MathHelper.log2(size) - 1; i++) {
            constructHPLevelKernel.setArg(0, images.get(i));
            constructHPLevelKernel.setArg(1, images.get(i + 1));
            previous /= 2;
            queue.putNDRangeKernel(
                    constructHPLevelKernel,
                    1, null, PointerBuffer.allocateDirect(1).put(previous * previous * previous), null
            );
        }
    }


    private void updateScalarField(CLCommandQueue queue, CLImage3d rawData, float isolevel, int size) {
        classifyCubesKernel.setArg(0, images.get(0));
        classifyCubesKernel.setArg(1, rawData);
        classifyCubesKernel.setArg(2, isolevel);
        queue.putNDRangeKernel(classifyCubesKernel, 1, null,
                PointerBuffer.allocateDirect(1).put(size * size * size).rewind(), null);
    }


    public List<CLImage3d> getImages() {
        return images;
    }

    @Override
    public void clean() {
        super.clean();

        for (var image3d : images) {
            if (!image3d.isReleased()) {
                image3d.release();
            }
        }

        images.clear();
    }
}
