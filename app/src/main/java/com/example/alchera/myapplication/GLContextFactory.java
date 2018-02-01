package com.example.alchera.myapplication;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Created by alchera on 18. 2. 1.
 */
//OpenGL ES 2.0버전으로 렌더링ㅇ하기위한 환경 설정
//Context를 생성하고 삭제하는 일만.
public class GLContextFactory implements GLSurfaceView.EGLContextFactory {

    private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    @Override
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
        Log.w("", "OpenGL ES 2.0 context 클래스 생성자 호출");

        checkEglError("eglCreateContext 적용 전", egl);
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
        EGLContext context = egl.eglCreateContext(display, eglConfig,
                EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError("eglCreateContext 적용 후 ", egl);
        return context;
    }

    @Override
    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
        egl.eglDestroyContext(display,context);
    }
    private static void checkEglError(String propmt, EGL10 egl) {
        int error;
        while((error=egl.eglGetError())!=EGL10.EGL_SUCCESS) {
            Log.e("egl error ", String.format("%s : EGL error : 0x%x",propmt,error));
        }
    }
}
