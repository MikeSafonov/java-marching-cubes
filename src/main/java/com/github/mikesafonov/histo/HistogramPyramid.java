package com.github.mikesafonov.histo;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLImage3d;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.gl.CLGLContext;


public abstract class HistogramPyramid {

    protected CLKernel constructHPLevelKernel;
    protected CLKernel classifyCubesKernel;


    public HistogramPyramid(CLProgram program) {
        constructHPLevelKernel = program.createCLKernel("constructHPLevel");
        classifyCubesKernel = program.createCLKernel("classifyCubes");
    }

    public abstract void createImages(CLGLContext context, int size);

    public abstract int perform(CLCommandQueue queue, CLImage3d rawData, float isolevel, int size);

    public void clean() {

        if (!constructHPLevelKernel.isReleased()) {
            constructHPLevelKernel.release();
        }
        constructHPLevelKernel = null;

        if (!classifyCubesKernel.isReleased()) {
            classifyCubesKernel.release();
        }
        classifyCubesKernel = null;
    }
}
