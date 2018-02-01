package com.example.alchera.myapplication;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.INIT_FLAGS;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;


public class MainActivity extends Activity {
    ArrayList<String> mDataSetStrings = new ArrayList<String>();
    DataSet mDataset;
    GLView mGLView;
    GLRenderer mRenderer;
    ObjectTracker mObjectTracker;
    Activity mActivity;
    String licenceKey = "ATsUlQH/////AAAAmeFH2PdkqEWai6m8/mPhv5Qj9nWfD5KVKltkCzFeFfqJPNmUorPKkmqsr2Pk3h/DSPskgMG7CQauDqwMYZQOuXqZ/KPw50YzL0bnV1SkCEvPXiu33GIRJxFO71xdjdRcVlLuTaAeEyhd+45U08XfSKesbqbk2EnZpLyzo8vzIE+rldrwNbJh1yeDeyrMfFd0wzOFXvuwsEMqvZgu/mhJcR9+iKWkzwOVC2ePhc4xnY7pL+F4SUAm6BLDQr5OeQmujOaBtrzqRn9J/tGzCBMt/72uHh4aqoZFOp1YJpOFptHA/LrqnQt4uwMELv+hoO7eqZhk005Z8t8FxeL89T9ChTLPwvYP9KT5a92nSASKUHGF";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitVuforiaTask initVuforiaTask = new InitVuforiaTask(this);
        initVuforiaTask.execute();
    }


    class InitVuforiaTask extends AsyncTask {
        Activity mActivity;

        public InitVuforiaTask(Activity mActivity) {
            this.mActivity = mActivity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            //Vuforia init
            int mProgressValue = -1;

            int initFlag = INIT_FLAGS.GL_20;
            Vuforia.setInitParameters(mActivity, initFlag, licenceKey);
            do {
                mProgressValue = Vuforia.init();
            }while(!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);

            if(mProgressValue == -1) {
                Log.e("hate", "init fail");
            }

            if(mProgressValue == 100) {
                Log.e("init succ", "init succ");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            InitTrackerTask initTrackerTask = new InitTrackerTask(mActivity);
            initTrackerTask.execute();
        }
    }

    class InitTrackerTask extends  AsyncTask {
        Activity mActivity;

        public InitTrackerTask(Activity mActivity) {
            this.mActivity = mActivity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            //Tracker init
            TrackerManager tManager = TrackerManager.getInstance();
            Tracker tracker = tManager.initTracker(ObjectTracker.getClassType());
            if(tracker == null) {
                Log.e("init Tracker", "fail");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            LoadTrackerData loadTrackerData = new LoadTrackerData(mActivity);
            loadTrackerData.execute();
        }
    }

    class LoadTrackerData extends  AsyncTask {
        Activity mActivity;

        public LoadTrackerData(Activity activity) {
            mActivity = activity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            //Data load
            mDataSetStrings.add("MyApplication.xml");
            TrackerManager tManager = TrackerManager.getInstance();
            mObjectTracker = (ObjectTracker) tManager.getTracker(ObjectTracker.getClassType());
            mDataset = mObjectTracker.createDataSet();
            mDataset.load(mDataSetStrings.get(0), STORAGE_TYPE.STORAGE_APPRESOURCE);
            mObjectTracker.activateDataSet(mDataset);
            Trackable trackable = mDataset.getTrackable(0);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            int depthSize = 16;
            int stencilSize = 0;
            boolean translucent = Vuforia.requiresAlpha();

            //OpenGL View
            mGLView = new GLView(mActivity);
            mGLView.init(translucent, depthSize, stencilSize);
            mRenderer = new GLRenderer(mActivity);
            mRenderer.VideoBackgroundConfig();

            mGLView.setRenderer(mRenderer);
            addContentView(mGLView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            StartCameraAndTracker startCameraAndTracker = new StartCameraAndTracker();
            startCameraAndTracker.execute();
        }
    }

    class StartCameraAndTracker extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            //Camera init

            if(!CameraDevice.getInstance().init(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT)) {
                Log.e("camera", "error1");
            }

            if(!CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT)) {
                Log.e("camera", "error2");
            }

            if(!CameraDevice.getInstance().start()) {
                Log.e("camera", "error3");

            }

            Log.e("camera", "camera succ");
            //Tracker start
            mObjectTracker.start();

            return null;
        }
    }
}