package com.github.mikesafonov;

public class GLVertexObjects {

    private int vbo;
    private int vao;

    public GLVertexObjects(int vbo, int vao) {
        this.vbo = vbo;
        this.vao = vao;
    }

    public int getVbo() {
        return vbo;
    }

    public int getVao() {
        return vao;
    }
}
