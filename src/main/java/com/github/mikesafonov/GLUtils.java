package com.github.mikesafonov;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import lombok.experimental.UtilityClass;

import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;

@UtilityClass
public class GLUtils {

    private static final int SIZE_PER_TRIANGLE = 4 * 18;
    private static final int SIZE_PER_POINT = 4 * 3;


    public static GLVertexObjects initVBOTriangles(GLAutoDrawable drawable, int trianglesCount, int sizePerTriangle, int shaderProgramID) {
        GL3 gl3 = (GL3) drawable.getGL();
        return initVBOTriangles(gl3, trianglesCount, sizePerTriangle, shaderProgramID);
    }

    public static GLVertexObjects initVBOTriangles(GL3 gl, int trianglesCount, int shaderProgramID) {
        return initVBOTriangles(gl, trianglesCount, SIZE_PER_TRIANGLE, shaderProgramID);
    }

    public static GLVertexObjects initVBOTriangles(GL3 gl, int trianglesCount, int sizePerTriangle, int shaderProgramID) {
        int tempArray[] = new int[1];

        gl.glGenVertexArrays(1, IntBuffer.wrap(tempArray));
        int vertexArrayObject = tempArray[0];
        gl.glBindVertexArray(vertexArrayObject);

        // Создаем VBO
        gl.glGenBuffers(1, IntBuffer.wrap(tempArray));
        int vertexBufferObject = tempArray[0];

        gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        gl.glBufferData(GL_ARRAY_BUFFER, trianglesCount * sizePerTriangle, null,
                GL3.GL_STATIC_DRAW);

        // Initialize the attribute location of the input
        // vertices for the shader program
        int location = gl.glGetAttribLocation(shaderProgramID, "inVertex");
        gl.glVertexAttribPointer(location, 3, GL_FLOAT, false, 24, 0);
        gl.glEnableVertexAttribArray(location);
        //нормаль
        location = gl.glGetAttribLocation(shaderProgramID, "inNormal");
        gl.glVertexAttribPointer(location, 3, GL_FLOAT, false, 24, 12);
        gl.glEnableVertexAttribArray(location);


        return new GLVertexObjects(vertexBufferObject, vertexArrayObject);
    }

    public static GLVertexObjects initPointData(GL3 gl, int pointCount, int shaderProgramID) {
        return initPointData(gl, pointCount, SIZE_PER_POINT, shaderProgramID);
    }

    public static GLVertexObjects initPointData(GL3 gl, int pointCount, int pointSize, int shaderProgramID) {

        int tempArray[] = new int[1];

        // Создаем VAO для вершин треугольников
        gl.glGenVertexArrays(1, IntBuffer.wrap(tempArray));
        int pointArrayObject = tempArray[0];
        gl.glBindVertexArray(pointArrayObject);

        gl.glGenBuffers(1, IntBuffer.wrap(tempArray));
        int pointBufferObject = tempArray[0];

        gl.glBindBuffer(GL_ARRAY_BUFFER, pointBufferObject);

        gl.glBufferData(GL_ARRAY_BUFFER, pointCount * pointSize, null,
                GL3.GL_STATIC_DRAW);
//
//        // Initialize the attribute location of the input
//        // vertices for the shader program
        int location = gl.glGetAttribLocation(shaderProgramID, "position");
        gl.glVertexAttribPointer(location, 3, GL_FLOAT, false, 12, 0);
        gl.glEnableVertexAttribArray(location);

        return new GLVertexObjects(pointBufferObject, pointArrayObject);

    }




}
