package com.github.mikesafonov;

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLProgram;
import lombok.experimental.UtilityClass;

import java.io.*;


@UtilityClass
public class CLProgramBuilder {

    private static final String SOURCE_NAME_3D = "/cl/SurfaceExtraction.cl";
    private static final String SOURCE_NAME_MORTON = "/cl/SurfaceExtraction_no_3d_write.cl";


    public static CLProgram build(CLContext context, boolean image3dSupport, CLDevice device, int size) {

        String programFile = (image3dSupport) ? SOURCE_NAME_3D : SOURCE_NAME_MORTON;

        String options = "-DSIZE=" + size + " -DTYPE_INT";

        try {
            String sourceCode = readProgramSource(CLProgramBuilder.class.getResource(programFile).getFile());
            return context.createProgram(sourceCode).build(options, device);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }


    private static String readProgramSource(String file) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return readSource(reader);
        }
    }


    private static String readProgramSource(InputStream stream) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return readSource(reader);
        }
    }


    private static String readSource(BufferedReader reader) throws IOException {
        final StringBuilder sb = new StringBuilder(2048);
        String line;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
        } finally {
            reader.close();
        }

        return sb.toString();
    }

}
