package com.example.alchera.myapplication;

import android.app.Activity;
import android.graphics.Point;
import android.opengl.GLSurfaceView;

import android.util.Log;

import com.vuforia.CameraDevice;
import com.vuforia.Renderer;
import com.vuforia.Vec2I;
import com.vuforia.VideoMode;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alchera on 18. 2. 1.
 */
// 매 프레임마다 렌더링을 담당
public class GLRenderer implements GLSurfaceView.Renderer {

    private Activity mActivity = null;
    private boolean mIsActive = false;
    public GLRenderer(Activity activity){
        mActivity=activity;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //surface가 생성될 떄 호출되는 callback
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //surface가 변결될 떄 호출되는 callback
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //그림

    }


    public void setActive(boolean active) {
        mIsActive = active;
        if(mIsActive) VideoBackgroundConfig();
    }

    public void VideoBackgroundConfig(){


            com.vuforia.CameraDevice cameraDevice = CameraDevice.getInstance();
            com.vuforia.VideoMode vm = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);

            com.vuforia.VideoBackgroundConfig config = new com.vuforia.VideoBackgroundConfig();
            config.setEnabled(true);
            config.setPosition(new Vec2I(0, 0));

            Point size = new Point();
            mActivity.getWindowManager().getDefaultDisplay().getRealSize(size);
            int xSize = 0, ySize = 0;

            int width = size.x;
            int height = size.y;

            xSize = (int) (vm.getHeight() * (height / (float) vm
                    .getWidth()));
            ySize = height;

            if (xSize < width)
            {
                xSize = height;
                ySize = (int) (width * (vm.getWidth() / (float) vm
                        .getHeight()));
            }
            config.setSize(new Vec2I(xSize, ySize));
            Log.i("screensize","vm"+vm.getHeight()+","+vm.getHeight()+"realsize"+xSize+","+ySize);
            Renderer.getInstance().setVideoBackgroundConfig(config);

    }
}