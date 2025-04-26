package com.example.dronedroid.ui.dashboard;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    public void setAngle(float pitch, float roll, float yaw){
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }
    float pitch = 0.0f;
    float roll = 0.0f;
    float yaw = 0.0f;

    private static final int HEADER_LENGTH = 80;
    private static final int TRIANGLE_LENGTH = 50;
    FloatBuffer normsBuffer ,verticesBuffer;
    private int triangleCount;

    public void loadSTL(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] header = new byte[HEADER_LENGTH];
        bufferedInputStream.read(header);

        byte[] triangleCountArray = new byte[4];
        bufferedInputStream.read(triangleCountArray);
        triangleCount = ByteBuffer.wrap(triangleCountArray).order(ByteOrder.LITTLE_ENDIAN).getInt();

        float[] vertices = new float[triangleCount * 9];
        float[] norms = new float[triangleCount * 3];
        byte[] triangle = new byte[TRIANGLE_LENGTH];

        for (int i = 0; i < triangleCount; i++) {
            bufferedInputStream.read(triangle);
            ByteBuffer triangleBuffer = ByteBuffer.wrap(triangle).order(ByteOrder.LITTLE_ENDIAN);

            norms[(i * 3)] = triangleBuffer.getFloat();
            norms[(i * 3) + 1] = triangleBuffer.getFloat();
            norms[(i * 3) + 2] = triangleBuffer.getFloat();

            for (int j = 0; j < 3; j++) {
                vertices[(i * 9) + (j * 3)] = triangleBuffer.getFloat();
                vertices[(i * 9) + (j * 3) + 1] = triangleBuffer.getFloat();
                vertices[(i * 9) + (j * 3) + 2] = triangleBuffer.getFloat();
            }
        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(norms.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        normsBuffer = vbb.asFloatBuffer();
        normsBuffer.put(norms);
        normsBuffer.position(0);

        vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        verticesBuffer = vbb.asFloatBuffer();
        verticesBuffer.put(vertices);
        verticesBuffer.position(0);
    }
    public void draw(GL10 gl){
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normsBuffer);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, verticesBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, triangleCount * 3);
    }
    public class MyCube {
        private final FloatBuffer mVertexBuffer;

        public MyCube(){
            float vertices[] = {
                    // 前
                    -0.5f, -0.5f, 0.5f,
                    0.5f, -0.5f, 0.5f,
                    -0.5f, 0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    // 後
                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
                    // 左
                    -0.5f, -0.5f, 0.5f,
                    -0.5f, -0.5f, -0.5f,
                    -0.5f, 0.5f, 0.5f,
                    -0.5f, 0.5f, -0.5f,
                    // 右
                    0.5f, -0.5f, 0.5f,
                    0.5f, -0.5f, -0.5f,
                    0.5f, 0.5f, 0.5f,
                    0.5f, 0.5f, -0.5f,
                    // 上
                    -0.5f, 0.5f, 0.5f,
                    0.5f, 0.5f, 0.5f,
                    -0.5f, 0.5f, -0.5f,
                    0.5f, 0.5f, -0.5f,
                    // 底
                    -0.5f, -0.5f, 0.5f,
                    0.5f, -0.5f, 0.5f,
                    -0.5f, -0.5f, -0.5f,
                    0.5f, -0.5f, -0.5f
            };

            ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            mVertexBuffer = vbb.asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);
        }

        public void draw(GL10 gl){
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

            // Front
            gl.glNormal3f(0, 0, 1.0f);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

            // Back
            gl.glNormal3f(0, 0, -1.0f);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);

            // Left
            gl.glNormal3f(-1.0f, 0, 0);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);

            // Right
            gl.glNormal3f(1.0f, 0, 0);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);

            // Top
            gl.glNormal3f(0, 1.0f, 0);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);

            // Right
            gl.glNormal3f(0, -1.0f, 0);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);
        }
    }
    MyCube cube = new MyCube();
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Initialization code for OpenGL
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);

        gl.glLightf(GL10.GL_LIGHT0, GL10.GL_AMBIENT, 0.2f);

        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);

        gl.glMaterialf(GL10.GL_LIGHT0, GL10.GL_AMBIENT, 0.1f);
        gl.glMaterialf(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, 20.6f);
        gl.glMaterialf(GL10.GL_LIGHT0, GL10.GL_SPECULAR, 10.0f);
        gl.glMaterialf(GL10.GL_LIGHT0, GL10.GL_SHININESS, 10.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Handle surface size changes
        gl.glViewport(0,0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 90f, (float)width/height, 1f, 800f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Rendering code for each frame
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glTranslatef(0.0f, 00.0f, -200f);
        gl.glRotatef(-90, 1f, 0f, 0f);
        //gl.glRotatef(-180, 0, 0, 1f);

        gl.glRotatef(pitch, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(-roll, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(yaw, 0.0f, 0.0f, 1.0f);

        float[] lightpos = {0.0f, 0.0f, 120.0f, 1.0f};
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, FloatBuffer.wrap(lightpos));

        //cube.draw(gl);
        this.draw(gl);

    }
}
