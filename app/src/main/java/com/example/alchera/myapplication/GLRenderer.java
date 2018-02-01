package com.example.alchera.myapplication;

import android.app.Activity;
import android.graphics.Point;
import android.opengl.GLSurfaceView;

import android.util.Log;

import com.vuforia.CameraDevice;
import com.vuforia.Device;
import com.vuforia.Renderer;
import com.vuforia.Vec2I;
import com.vuforia.VideoMode;
import com.vuforia.Vuforia;

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
        Device device = Device.getInstance();
        device.setViewerActive(false); // Indicates if the app will be using a viewer, stereo mode and initializes the rendering primitives
        device.setMode(Device.MODE.MODE_AR); // Select if we will be in AR or VR mode
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Vuforia.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Vuforia.onSurfaceChanged(width, height);
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
            /*
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
            */
            config.setSize(new Vec2I(500, 500));
            Renderer.getInstance().setVideoBackgroundConfig(config);
    }
}