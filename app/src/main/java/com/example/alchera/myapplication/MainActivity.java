package com.example.alchera.myapplication;

import android.app.Activity;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.Device;
import com.vuforia.INIT_FLAGS;
import com.vuforia.ObjectTracker;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

import java.util.ArrayList;


public class MainActivity extends Activity {
    public static final String LOGTAG = "MainActivity";
    String licenceKey = "ATsUlQH/////AAAAmeFH2PdkqEWai6m8/mPhv5Qj9nWfD5KVKltkCzFeFfqJPNmUorPKkmqsr2Pk3h/DSPskgMG7CQauDqwMYZQOuXqZ/KPw50YzL0bnV1SkCEvPXiu33GIRJxFO71xdjdRcVlLuTaAeEyhd+45U08XfSKesbqbk2EnZpLyzo8vzIE+rldrwNbJh1yeDeyrMfFd0wzOFXvuwsEMqvZgu/mhJcR9+iKWkzwOVC2ePhc4xnY7pL+F4SUAm6BLDQr5OeQmujOaBtrzqRn9J/tGzCBMt/72uHh4aqoZFOp1YJpOFptHA/LrqnQt4uwMELv+hoO7eqZhk005Z8t8FxeL89T9ChTLPwvYP9KT5a92nSASKUHGF";

    private boolean mIsVuforiaInit = false;

    ObjectTracker mObjectTracker;

    ArrayList<String> mDataSetStrings = new ArrayList<String>();
    DataSet mDataset;

    GLView mGLView;
    GLRenderer mRenderer;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mIsVuforiaInit) {
            Log.d(LOGTAG, "Vuforia is already initialized");
        } else {
            InitVuforiaTask initVuforiaTask = new InitVuforiaTask(this);
            initVuforiaTask.execute();
        }
    }

    /*
    TODO : android life-cycle에 맞춰서 Vuforia, OpenGL, Camera을 작동시켜야 한다.
    Vuforia -> Tracker -> OpenGL -> Camera 순으로 초기화했으니
    Camera _> OpenGL -> Tracker -> Vuforia deinit하는게 맞을 것 같다.
     */
    @Override
    protected void onPause() {
        super.onPause();

        CameraDevice.getInstance().stop();
        CameraDevice.getInstance().deinit();

        mGLView.onPause();

        mObjectTracker.stop();

        Vuforia.onPause();
    }

    /*
    TODO : onResume(), onDestory() 구현
    @Override
    protected void onResume() {
        super.onResume();

        Vuforia.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Vuforia.deinit();
    }
    */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Device.getInstance().setConfigurationChanged();
    }

    class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean> {
        Activity mActivity;

        public InitVuforiaTask(Activity mActivity) {
            this.mActivity = mActivity;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //Vuforia init
            int mProgressValue = -1;

            int initFlag = INIT_FLAGS.GL_20;
            Vuforia.setInitParameters(mActivity, initFlag, licenceKey);
            do {
                mProgressValue = Vuforia.init();
            }while(!isCancelled() && mProgressValue >= 0 && mProgressValue < 100);

            return (mProgressValue > 0);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(!result) {
                Log.d(LOGTAG, "failed to initialize Vuforia");
            }

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
           // mDataSetStrings.add("MyApplication.xml");
            mDataSetStrings.add("StonesAndChips.xml");
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