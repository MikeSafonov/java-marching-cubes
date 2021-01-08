package com.github.mikesafonov;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;


public class ShaderProgram {

    private int shaderProgramID;

    private String fragmentShaderSourceFile = "/shaders/fragmentshader.frag";
    private String vertexShaderSourceFile = "/shaders/vertexshader.vert";


    public ShaderProgram() {
    }

    public ShaderProgram(GL3 gl) {
        initShaders(gl, vertexShaderSourceFile, fragmentShaderSourceFile);
    }

    public ShaderProgram(GL3 gl, String vertexShaderSourceFile, String fragmentShaderSourceFile) {
        initShaders(gl, vertexShaderSourceFile, fragmentShaderSourceFile);
    }



    /**
     * Initialize the shaders and the shader program
     *
     */
    public void initShaders(GLAutoDrawable drawable) {
        initShaders(drawable, vertexShaderSourceFile, fragmentShaderSourceFile);
    }


    public void initShaders(GL3 gl, String vertexShaderSourceFile, String fragmentShaderSourceFile) {
        int vertexShaderID = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
        gl.glShaderSource(vertexShaderID, 1,
                new String[]{loadShader(vertexShaderSourceFile)}, null);
        gl.glCompileShader(vertexShaderID);

        int fragmentShaderID = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        gl.glShaderSource(fragmentShaderID, 1,
                new String[]{loadShader(fragmentShaderSourceFile)}, null);
        gl.glCompileShader(fragmentShaderID);

        shaderProgramID = gl.glCreateProgram();
        gl.glAttachShader(shaderProgramID, vertexShaderID);
        gl.glAttachShader(shaderProgramID, fragmentShaderID);
        gl.glLinkProgram(shaderProgramID);
    }


    public void initShaders(GLAutoDrawable drawable, String vertexShaderSourceFile, String fragmentShaderSourceFile) {
        GL3 gl = drawable.getGL().getGL3();
        initShaders(gl, vertexShaderSourceFile, fragmentShaderSourceFile);
    }


    public int getShaderProgramID() {
        return shaderProgramID;
    }


    private String loadShader(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader brf = null;
        try {
            brf = new BufferedReader(new FileReader(ShaderProgram.class.getResource(filename).getFile()));
            stringBuilder = new StringBuilder();
            String line = null;
            while ((line = brf.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();

    }


    private void printShaderInfoLog(GLAutoDrawable d, int obj) {
        GL3 gl = d.getGL().getGL3(); // get the OpenGL 3 graphics context
        IntBuffer infoLogLengthBuf = IntBuffer.allocate(1);
        gl.glGetShaderiv(obj, GL3.GL_INFO_LOG_LENGTH, infoLogLengthBuf);
        int infoLogLength = infoLogLengthBuf.get(0);
        if (infoLogLength > 0) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(infoLogLength);
            gl.glGetShaderInfoLog(obj, infoLogLength, infoLogLengthBuf, byteBuffer);
            for (byte b : byteBuffer.array()) {
                System.err.print((char) b);
            }
        }
    }

    private void printProgramInfoLog(GLAutoDrawable d, int obj) {
        GL3 gl = d.getGL().getGL3(); // get the OpenGL 3 graphics context
        IntBuffer infoLogLengthBuf = IntBuffer.allocate(1);
        gl.glGetProgramiv(obj, GL3.GL_INFO_LOG_LENGTH, infoLogLengthBuf);
        int infoLogLength = infoLogLengthBuf.get(0);
        if (infoLogLength > 0) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(infoLogLength);
            gl.glGetProgramInfoLog(obj, infoLogLength, infoLogLengthBuf, byteBuffer);
            for (byte b : byteBuffer.array()) {
                System.err.print((char) b);
            }
        }
    }

    }
