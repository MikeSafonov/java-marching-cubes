package com.github.mikesafonov.histo;

import com.github.mikesafonov.BufferHelper;
import com.github.mikesafonov.MathHelper;
import com.github.mikesafonov.Properties;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLContext;

import java.util.ArrayList;
import java.util.List;

import static com.jogamp.opencl.llb.CLMemObjBinding.CL_MEM_READ_WRITE;

public class HistogramPyramidBuffer extends HistogramPyramid {


    private CLKernel constructHPLevelCharCharKernel;
    private CLKernel constructHPLevelCharShortKernel;
    private CLKernel constructHPLevelShortShortKernel;
    private CLKernel constructHPLevelShortIntKernel;
    private List<CLBuffer> buffers;
    private CLBuffer cubeIndexesBuffer;


    public HistogramPyramidBuffer(CLProgram program) {
        super(program);

        constructHPLevelCharCharKernel = program.createCLKernel("constructHPLevelCharChar");
        constructHPLevelCharShortKernel = program.createCLKernel("constructHPLevelCharShort");
        constructHPLevelShortShortKernel = program.createCLKernel("constructHPLevelShortShort");
        constructHPLevelShortIntKernel = program.createCLKernel("constructHPLevelShortInt");

        buffers = new ArrayList<>();
    }

    @Override
    public void createImages(CLGLContext context, int size) {
        int bufferSize = size * size * size;
        int totalBufferSize = bufferSize;
        buffers.add(BufferHelper.createBuffer(context, bufferSize, Properties.BYTE_SIZE, CL_MEM_READ_WRITE));

        bufferSize /= 8;
        totalBufferSize += bufferSize;
        buffers.add(BufferHelper.createBuffer(context, bufferSize, Properties.BYTE_SIZE, CL_MEM_READ_WRITE));

        bufferSize /= 8;
        totalBufferSize += 2 * bufferSize;
        buffers.add(BufferHelper.createBuffer(context, bufferSize, Properties.SHORT_SIZE, CL_MEM_READ_WRITE));

        bufferSize /= 8;
        totalBufferSize += 2 * bufferSize;
        buffers.add(BufferHelper.createBuffer(context, bufferSize, Properties.SHORT_SIZE, CL_MEM_READ_WRITE));

        bufferSize /= 8;
        totalBufferSize += 2 * bufferSize;
        buffers.add(BufferHelper.createBuffer(context, bufferSize, Properties.SHORT_SIZE, CL_MEM_READ_WRITE));

        for (int i = 5; i < (MathHelper.log2(size)); i++) {
            bufferSize /= 8;
            totalBufferSize += 4 * bufferSize;
            buffers.add(BufferHelper.createBuffer(context, bufferSize, Properties.INT_SIZE, CL_MEM_READ_WRITE));
        }

        System.out.println("allocating " + totalBufferSize / 1048576 + " mb");

        cubeIndexesBuffer = context.createBuffer(size * size * size, CL_MEM_READ_WRITE);
    }

    @Override
    public int perform(CLCommandQueue queue, CLImage3d rawData, float isolevel, int size) {

        histogramPyramidConstruction(queue, rawData, isolevel, size);

        CLBuffer buffer = buffers.get(buffers.size() - 1);
        queue.putReadBuffer(buffer, true);
        queue.finish();
        int totalSum = BufferHelper.countBuffer(buffer);

        return totalSum;
    }


    private void histogramPyramidConstruction(CLCommandQueue queue, CLImage3d rawData, float isolevel, int size) {
        updateScalarField(queue, rawData, isolevel, size);

        // Run base to first level
        constructHPLevelCharCharKernel.setArg(0, buffers.get(0));
        constructHPLevelCharCharKernel.setArg(1, buffers.get(1));

        int previous = size / 2;
        long[] wg = new long[]{previous, previous, previous};
        queue.putNDRangeKernel(
                constructHPLevelCharCharKernel,
                3, null, BufferHelper.allocateDirectBuffer(wg), null
        );

        previous /= 2;
        wg = new long[]{previous, previous, previous};
        constructHPLevelCharShortKernel.setArg(0, buffers.get(1));
        constructHPLevelCharShortKernel.setArg(1, buffers.get(2));
        queue.putNDRangeKernel(
                constructHPLevelCharShortKernel,
                3, null, BufferHelper.allocateDirectBuffer(wg), null
        );

        previous /= 2;
        wg = new long[]{previous, previous, previous};
        constructHPLevelShortShortKernel.setArg(0, buffers.get(2));
        constructHPLevelShortShortKernel.setArg(1, buffers.get(3));
        queue.putNDRangeKernel(
                constructHPLevelShortShortKernel,
                3, null, BufferHelper.allocateDirectBuffer(wg), null
        );
        previous /= 2;
        wg = new long[]{previous, previous, previous};
        constructHPLevelShortShortKernel.setArg(0, buffers.get(3));
        constructHPLevelShortShortKernel.setArg(1, buffers.get(4));
        queue.putNDRangeKernel(
                constructHPLevelShortShortKernel,
                3, null, BufferHelper.allocateDirectBuffer(wg), null
        );

        previous /= 2;
        wg = new long[]{previous, previous, previous};
        constructHPLevelShortIntKernel.setArg(0, buffers.get(4));
        constructHPLevelShortIntKernel.setArg(1, buffers.get(5));
        queue.putNDRangeKernel(
                constructHPLevelShortIntKernel,
                3, null, BufferHelper.allocateDirectBuffer(wg), null
        );


        // Run level 2 to top level
        for (int i = 5; i < MathHelper.log2(size) - 1; i++) {
            constructHPLevelKernel.setArg(0, buffers.get(i));
            constructHPLevelKernel.setArg(1, buffers.get(i + 1));
            previous /= 2;
            wg = new long[]{previous, previous, previous};
            queue.putNDRangeKernel(
                    constructHPLevelKernel,
                    3, null, BufferHelper.allocateDirectBuffer(wg), null
            );
        }
    }


    private void updateScalarField(CLCommandQueue queue, CLImage3d rawData, float isolevel, int size) {

        classifyCubesKernel.setArg(0, buffers.get(0));
        classifyCubesKernel.setArg(1, cubeIndexesBuffer);
        classifyCubesKernel.setArg(2, rawData);
        classifyCubesKernel.setArg(3, isolevel);
        long[] workGroup = new long[]{size, size, size};


        queue.putNDRangeKernel(
                classifyCubesKernel, 3,
                null,
                BufferHelper.allocateDirectBuffer(workGroup),
                null
        );

    }

    public List<CLBuffer> getBuffers() {
        return buffers;
    }

    public CLBuffer getCubeIndexesBuffer() {
        return cubeIndexesBuffer;
    }

    @Override
    public void clean() {
        super.clean();

        for(CLBuffer buffer :  buffers){
            if(!buffer.isReleased()) {
                buffer.release();
            }
        }
        buffers.clear();
        if(!cubeIndexesBuffer.isReleased()) {
            cubeIndexesBuffer.release();
        }
        cubeIndexesBuffer = null;

        if(!constructHPLevelCharCharKernel.isReleased()) {
            constructHPLevelCharCharKernel.release();
        }
        constructHPLevelCharCharKernel = null;

        if(!constructHPLevelCharShortKernel.isReleased()) {
            constructHPLevelCharShortKernel.release();
        }
        constructHPLevelCharShortKernel =null;

        if(!constructHPLevelShortShortKernel.isReleased()) {
            constructHPLevelShortShortKernel.release();
        }
        constructHPLevelShortShortKernel = null;

        if(!constructHPLevelShortIntKernel.isReleased()) {
            constructHPLevelShortIntKernel.release();
        }
        constructHPLevelShortIntKernel = null;
    }

}
