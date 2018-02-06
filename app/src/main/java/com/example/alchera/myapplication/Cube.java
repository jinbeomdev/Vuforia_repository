package com.example.alchera.myapplication;
/**
 * Created by alchera on 18. 2. 5.
 */

import android.opengl.GLES20;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cube {
    public FloatBuffer vertexBuffer;
    public FloatBuffer colorBuffer;
    public ShortBuffer drawListBuffer;
    private FloatBuffer normalBuffer;


    static final int COORDS_PER_VERTEX = 3;
    static final int COLORS_PER_VERTEX = 4;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int colorStride = COLORS_PER_VERTEX *4;

    public static final String vertexShaderCode = " \n" + "\n"
            + "attribute vec4 vPosition; \n"
            + "attribute vec4 aColor; \n"
            + "varying vec4 vColor; \n"
            + "uniform mat4 uMVPMatrix; \n" + "\n"
            + "void main() \n" + "{ \n"
            + "   vColor = aColor;"
            + "   gl_Position = uMVPMatrix * vPosition; \n"

            + "} \n";

    public static final String fragmentShaderCode = " \n" + "\n"
            + "precision mediump float; \n" + " \n"
            + "varying vec4 vColor; \n" + " \n"
            + "void main() \n"
            + "{ \n" + "   gl_FragColor = vColor; \n"
            + "} \n";


    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private final int mProgram2;

    static float[] vertices = {
            -1.00f, -1.00f, 1.00f, // front
            1.00f, -1.00f, 1.00f,
            1.00f, 1.00f, 1.00f,
            -1.00f, 1.00f, 1.00f,

            -1.00f, -1.00f, -1.00f, // back
            1.00f, -1.00f, -1.00f,
            1.00f, 1.00f, -1.00f,
            -1.00f, 1.00f, -1.00f,

            -1.00f, -1.00f, -1.00f, // left
            -1.00f, -1.00f, 1.00f,
            -1.00f, 1.00f, 1.00f,
            -1.00f, 1.00f, -1.00f,

            1.00f, -1.00f, -1.00f, // right
            1.00f, -1.00f, 1.00f,
            1.00f, 1.00f, 1.00f,
            1.00f, 1.00f, -1.00f,

            -1.00f, 1.00f, 1.00f, // top
            1.00f, 1.00f, 1.00f,
            1.00f, 1.00f, -1.00f,
            -1.00f, 1.00f, -1.00f,

            -1.00f, -1.00f, 1.00f, // bottom
            1.00f, -1.00f, 1.00f,
            1.00f, -1.00f, -1.00f,
            -1.00f, -1.00f, -1.00f
    };

    //정점 배열의 정점 인덱스를 이용하여 각 면마다 2개의 삼각형(ccw)를 구성
    static short[] indices={
            0, 1, 2, 0, 2, 3, // front
            4, 6, 5, 4, 7, 6, //back
            8, 9, 10, 8, 10, 11, //left
            12, 14, 13, 12, 15, 14, //right
            16, 17, 18, 16, 18, 19, //top
            20, 22, 21, 20, 23, 22//bottom
    };

    static float[] colors = {
            0.0f,  1.0f,  0.0f,  1.0f,
            0.0f,  1.0f,  0.0f,  1.0f,
            1.0f,  0.5f,  0.0f,  1.0f,
            1.0f,  0.5f,  0.0f,  1.0f,
            1.0f,  0.0f,  0.0f,  1.0f,
            1.0f,  0.0f,  0.0f,  1.0f,
            1.0f,  0.0f,  0.0f,  1.0f,
            1.0f,  0.0f,  0.0f,  1.0f
    };

    static float[] normals =
    {

    };

    public Cube() throws Exception {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();//convert byte to float
        vertexBuffer.put(vertices);//transfer the data into buffer
        vertexBuffer.position(0);//rewind

        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length*2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
        cbb.order(ByteOrder.nativeOrder());
        colorBuffer = cbb.asFloatBuffer();//convert byte to float
        colorBuffer.put(colors);//transfer the data into buffer
        colorBuffer.position(0);//rewind

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        if(GLES20.glIsShader(vertexShader)==false)
            throw new Exception("Vtx shader Failed");

        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        if(GLES20.glIsShader(fragmentShader) == false)
            throw new Exception("Frag shader Failed");

        //create empty openGL ES program
        mProgram2 = GLES20.glCreateProgram();
        if(GLES20.glIsProgram(mProgram2) == false)
            throw new Exception("Program Creation Failed");

        //add the vertex shader
        GLES20.glAttachShader(mProgram2, vertexShader);
        //add fragment shader
        GLES20.glAttachShader(mProgram2, fragmentShader);
        //link
        GLES20.glLinkProgram(mProgram2);
    }
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void draw(float[] mvpMatrix)  throws Exception{

        if(mvpMatrix == null || mvpMatrix.length != 16)
            throw new Exception("Wrong Matrix");


        GLES20.glUseProgram(mProgram2);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram2, "vPosition");
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mColorHandle = GLES20.glGetAttribLocation(mProgram2, "aColor");
        GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                4, colorBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram2, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle,1,false,mvpMatrix,0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}