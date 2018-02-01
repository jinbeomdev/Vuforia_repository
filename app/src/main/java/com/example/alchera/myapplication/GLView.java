package com.example.alchera.myapplication;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

/**
 * Created by alchera on 18. 2. 1.
 */

public class GLView extends GLSurfaceView {
    public static final boolean DEBUG = true;

    public GLView(Context context) {
        super(context);
    }

    //ConfigChooser, ContextFactory, Renderer 생성히여 GLSurfaceView에 적용
    public void init(boolean translucent, int depth, int stencil) {

            /*GLSurfaceView는 기본적으로 RGB_565의 알파값이 없는 surface생성
            만약 알파값이 존재하면 surface의 포맷을 PixelFormat.TRANSLUCENT로 설정
            SurfaceFlinger에 의해 32bit 알파값이 적용된 surface로 해석
            */
        if(translucent){
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }

        //Context Factory를 2.0버전으로 렌더링하기위한 환경 설정
        setEGLContextFactory(new GLContextFactory());

        //EGLConfig를 이 surface와 포맷을 확실하게 맞춰줌
        setEGLConfigChooser(translucent?
                new GLConfigChooser(8, 8, 8, 8, depth, stencil) :
                new GLConfigChooser(5, 6, 5, 0, depth, stencil));

    }

    @Override
    public void onPause() {
        super.onPause();;
    }
    @Override
    public void onResume() {
        super.onResume();
    }

}