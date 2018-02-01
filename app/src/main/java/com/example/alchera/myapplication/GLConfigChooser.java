package com.example.alchera.myapplication;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by alchera on 18. 2. 1.
 */

public class GLConfigChooser implements GLSurfaceView.EGLConfigChooser {
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    private int[] mValue = new int[1];

    /* EGL config specification is used to specify 2.0 rendering
    * we use a minimum size of 4 bits for r/g/b, but will
    * perform actual matching in chooseConfig() below.*/
    private static int EGL_OPENGL_ES2_BIT = 4;
    private static int[] s_configAttribs2 =
            {
                    EGL10.EGL_RED_SIZE,4,
                    EGL10.EGL_GREEN_SIZE,4,
                    EGL10.EGL_BLUE_SIZE,4,
                    EGL10.EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_NONE
            };
    //생성자로부터 rgba와 깊이버퍼, 스텐실의 비트크기를 정함
    public GLConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
        mRedSize=r;
        mGreenSize=g;
        mBlueSize=b;
        mAlphaSize=a;
        mDepthSize=depth;
        mStencilSize=stencil;
    }


    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

        //최소사양과 일치하는 EGL의 환경설정의 갯수를 구함
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, s_configAttribs2, null, 0,num_config);

        int numConfigs = num_config[0];

        if(numConfigs <=0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        //할당 후 최소사양과 일치한 EGL 환경값의 배열을 읽음
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, s_configAttribs2,configs,numConfigs,num_config);

        return chooseConfig(egl,display,configs);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {

        for(EGLConfig config : configs){
            int d = findConfigAttrib(egl, display, config,
                    EGL10.EGL_DEPTH_SIZE,0);
            int s = findConfigAttrib(egl, display, config,
                    EGL10.EGL_STENCIL_SIZE,0);

            //적어도 mDepthSize 와 mStencilSize의 비트는 필요하기에 예외처리를 함
            if(d < mDepthSize || s < mStencilSize)
                continue;

            //각각의 rgba와 완벽하게 일치하는 설정값 구함
            int r = findConfigAttrib(egl, display, config,
                    EGL10.EGL_RED_SIZE,0);
            int g = findConfigAttrib(egl, display, config,
                    EGL10.EGL_GREEN_SIZE,0);
            int b = findConfigAttrib(egl, display, config,
                    EGL10.EGL_BLUE_SIZE,0);
            int a = findConfigAttrib(egl, display, config,
                    EGL10.EGL_ALPHA_SIZE,0);

            if(r == mRedSize && g == mGreenSize && b == mBlueSize && a==mAlphaSize)
                return config;
        }

        return null;
    }
    private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                 EGLConfig config, int attribute, int defaultValue){

        if(egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

}
