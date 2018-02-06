package com.example.alchera.myapplication;

import android.app.Activity;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.opengl.Matrix;
import android.util.Log;
import android.view.View;

import com.vuforia.COORDINATE_SYSTEM_TYPE;
import com.vuforia.CameraDevice;
import com.vuforia.Device;
import com.vuforia.GLTextureData;
import com.vuforia.GLTextureUnit;
import com.vuforia.Matrix34F;
import com.vuforia.Matrix44F;
import com.vuforia.Mesh;
import com.vuforia.ObjectTarget;
import com.vuforia.Renderer;
import com.vuforia.RenderingPrimitives;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.TrackerManager;
import com.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.vuforia.VIEW;
import com.vuforia.Vec2I;
import com.vuforia.Vec4I;
import com.vuforia.VideoMode;
import com.vuforia.ViewList;
import com.vuforia.Vuforia;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by alchera on 18. 2. 1.
 */
// 매 프레임마다 렌더링을 담당
public class GLRenderer implements GLSurfaceView.Renderer {

    private final String TAG = "GLRenderer";

    private Activity mActivity = null;
    private boolean mIsActive = false;
    private Renderer mRenderer = null;
    RenderingPrimitives mRenderingPrimitives = null;
    GLTextureUnit mVideoBackgroundTex = null;
    int mProgram;
    int mVertexPosition;
    int mVertexTexCoord;
    int mProjectionMatrix;
    int mTexSampler2D;
    int currentView = VIEW.VIEW_SINGULAR;
    private static float OBJECT_SCALE_FLOAT = 0.003f;
    private Cube mCube;
    public GLRenderer(Activity activity){
        //init Rendering
        mActivity=activity;
        mRenderer = Renderer.getInstance();

        Device device = Device.getInstance();
        device.setViewerActive(false); // Indicates if the app will be using a viewer, stereo mode and initializes the rendering primitives
        device.setMode(Device.MODE.MODE_AR); // Select if we will be in AR or VR mode
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Vuforia.onSurfaceCreated();

        mVideoBackgroundTex = new GLTextureUnit();

        //init Rendering

        //shader 생성
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VideoBackgroundShader.VB_VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, VideoBackgroundShader.VB_FRAGMENT_SHADER);

        //program 객체를 생성한다
        mProgram = GLES20.glCreateProgram();

        checkGLerror("glCreateProgram");


        //vertex shader를 program 객체에 추가
        GLES20.glAttachShader(mProgram, vertexShader);

        checkGLerror("glAttachShader");


        //fragment shader를 program 객체에 추가
        GLES20.glAttachShader(mProgram, fragmentShader);

        checkGLerror("glAttachShader2");


        //program 객체를 OpenGL에 연결한다. program에 추가된 shader들이 Opengl에 연결된다
        GLES20.glLinkProgram(mProgram);

        checkGLerror("glLinkProgram");


        /* 비디오 백그라운드를 위한 렌더링 설정 */
        //랜더링 상태(Render State)의 일부분으로 program을 추가한다.
        GLES20.glUseProgram(mProgram);

        checkGLerror("glUseProgram");


        //프로그램으로 부터 Vertex Shader에서 texSampler2D에 대한 핸들러를 가져옴
        /*
                    "attribute vec4 vertexPosition;\n" +
                    "attribute vec2 vertexTexCoord;\n" +
                    "uniform mat4 projectionMatrix;\n" +
                    "varying vec2 texCoord;\n" +

                    "varying vec2 textCoord\n" +
                    "uniform sampler2D texSampler2D;\n" +
         */
        mVertexPosition = GLES20.glGetAttribLocation(mProgram, "vertexPosition");
        checkGLerror("glGetAttribLocation 120");

        mVertexTexCoord = GLES20.glGetAttribLocation(mProgram, "vertexTexCoord");
        checkGLerror("glGetAttribLocation 123");

        mProjectionMatrix = GLES20.glGetUniformLocation(mProgram, "projectionMatrix");
        checkGLerror("glGetUniformLocation 126");

        mTexSampler2D = GLES20.glGetUniformLocation(mProgram, "texSampler2D");
        checkGLerror("glGetUniformLocation 129");


        GLES20.glUseProgram(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Vuforia.onSurfaceChanged(width, height);
        VideoBackgroundConfig();

        mRenderingPrimitives = Device.getInstance().getRenderingPrimitives();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BITS);

        State state;
        state = TrackerManager.getInstance().getStateUpdater().updateState();

        mRenderer.begin(state);

        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera

        checkGLerror("136Line");



        renderFrame(state);

        mRenderer.end();



    }
    public void renderVideoBackground()
    {
        int videoTextureUnit = 0;
        mVideoBackgroundTex.setTextureUnit(videoTextureUnit);

        if (!mRenderer.updateVideoBackgroundTexture(mVideoBackgroundTex)) {
            Log.e(TAG, "Unable to update video background texture");
            return;
        }

        float[] vbProjectionMatrix = Tool.convert2GLMatrix(
                mRenderingPrimitives.getVideoBackgroundProjectionMatrix(currentView, COORDINATE_SYSTEM_TYPE.COORDINATE_SYSTEM_CAMERA)).getData();

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

        checkGLerror("150Line");

        Mesh vbMesh = mRenderingPrimitives.getVideoBackgroundMesh(currentView);

        checkGLerror("152Line");


        // Load the shader and upload the vertex/texcoord/index data
        GLES20.glUseProgram(mProgram);
        GLES20.glVertexAttribPointer(mVertexPosition, 3, GLES20.GL_FLOAT, false, 0, vbMesh.getPositions().asFloatBuffer());
        GLES20.glVertexAttribPointer(mVertexTexCoord, 2, GLES20.GL_FLOAT, false, 0, vbMesh.getUVs().asFloatBuffer());

        checkGLerror("157Line");


        GLES20.glUniform1i(mTexSampler2D, videoTextureUnit);

        checkGLerror("159Line");


        // Render the video background with the custom shader
        // First, we enable the vertex arrays
        GLES20.glEnableVertexAttribArray(mVertexPosition);
        GLES20.glEnableVertexAttribArray(mVertexTexCoord);

        checkGLerror("164Line");


        // Pass the projection matrix to OpenGL
        GLES20.glUniformMatrix4fv(mProjectionMatrix, 1, false, vbProjectionMatrix, 0);

        checkGLerror("167Line");


        // Then, we issue the render call
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, vbMesh.getNumTriangles() * 3, GLES20.GL_UNSIGNED_SHORT,
                vbMesh.getTriangles().asShortBuffer());

        checkGLerror("171Line");


        // Finally, we disable the vertex arrays
        GLES20.glDisableVertexAttribArray(mVertexPosition);
        GLES20.glDisableVertexAttribArray(mVertexTexCoord);

        checkGLerror("finish");

    }

    public void renderFrame(State state)
    {
        int viewID = 0;
        Vec4I viewport = mRenderingPrimitives.getViewport(viewID);
        // Set viewport for current view
        GLES20.glViewport(viewport.getData()[0], viewport.getData()[1], viewport.getData()[2], viewport.getData()[3]);
        // Set scissor
        GLES20.glScissor(viewport.getData()[0], viewport.getData()[1], viewport.getData()[2], viewport.getData()[3]);

        Matrix34F projMatrix = mRenderingPrimitives.getProjectionMatrix(viewID, COORDINATE_SYSTEM_TYPE.COORDINATE_SYSTEM_CAMERA,
                state.getCameraCalibration());

        // Create GL matrix setting up the near and far planes
        float rawProjectionMatrixGL[] = Tool.convertPerspectiveProjection2GLMatrix(
                projMatrix,//3x4 row-major matrix for perspective projection data
                0.01f,//가장 가까운
                5f)//가장 먼
                .getData();//returns a 4x4 col-major OpenGL perspective projection matrix from a 3x4 matrix

        // Apply the appropriate eye adjustment to the raw projection matrix, and assign to the global variable
        float eyeAdjustmentGL[] = Tool.convert2GLMatrix(mRenderingPrimitives
                .getEyeDisplayAdjustmentMatrix(viewID)).getData();
        //3x4 row-major matrix for pose data
            /*getEyeDisplayAdjustmentMatrix(int viewID)Unable to update video background texture");
            : return a matrix needed to correct for the different position of display relative to the eye
            returned matrix is to be applied to the tracker pose matrix during rendering
            */

        float projectionMatrix[] = new float[16];

        renderVideoBackground();

        //rawProjectionMatrixGL과 eyeAdjustmentGL 곱하여 projectionMatrix에 저장
        // Apply the adjustment to the projection matrix
        Matrix.multiplyMM(projectionMatrix, 0, rawProjectionMatrixGL, 0, eyeAdjustmentGL, 0);

        Log.v(TAG,"Matrix setting");


        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        try{
            mCube = new Cube();
        }
        catch(Exception e){

            e.printStackTrace(System.err);
            System.exit(1);
        }


//        if(state.getNumTrackableResults()>0) {
//            TrackableResult trackableResult = state.getTrackableResult(0);
//            Trackable trackable = trackableResult.getTrackable();
//
//            ObjectTarget imageTarget = (ObjectTarget) trackable;
//            float[] imageSize = imageTarget.getSize().getData();
//
//            if(trackableResult == null)
//            {
//                Log.e(TAG,"no target");
//                return;
//            }
//            Log.e(TAG,"yes target");
//            renderAugmentation(trackableResult, projectionMatrix, imageSize);
//        }
//        else
//        {
//            Log.e(TAG,""+state.getNumTrackableResults());
//        }
        for(int tIdx = 0; tIdx < state.getNumTrackableResults();tIdx++){
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

            float[] modelViewProjection = new float[16];

            Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,0.0f);

            Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                    OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);

            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);
            Log.e(TAG,"target found ");
            try{
                mCube.draw(modelViewProjection);
                checkGLerror("drawing cube");
            }
            catch (Exception e){
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    // @TODO Bad...
//    private void renderAugmentation(TrackableResult trackableResult, float[] projectionMatrix, float[] imagesize)
//    {
//        Log.v(TAG,"Rendering start ");
//
//
//        int textureIndex = 1;
//        OBJECT_SCALE_FLOAT = imagesize[0];
//        // deal with the modelview and projection matrices
//        float[] modelViewProjection = new float[16];
//        float[] modelViewProjectionScaled = new float[16];
//
//
//        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,0.0f);
//
//
//        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT/3,
//                OBJECT_SCALE_FLOAT/3, OBJECT_SCALE_FLOAT/3);
//
//        Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);
//        Log.v(TAG,"Rendering end ");
//
//        try{
//            mCube.draw(modelViewProjection);
//        }
//        catch (Exception e){
//            e.printStackTrace(System.err);
//            System.exit(1);
//        }
//
//    }

    public void checkGLerror(String op) {
        for (int error = GLES20.glGetError(); error != 0; error = GLES20
                .glGetError())
            Log.e(
                    TAG,
                    "After operation " + op + " glError 0x"
                            + Integer.toHexString(error));

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
            Renderer.getInstance().setVideoBackgroundConfig(config);
            Log.d("GLRenderer", "width : " + vm.getWidth() + " , height:"  + vm.getHeight());
    }

    public int loadShader(int type, String shaderCode) {
        //다음 2가지 타입 중 하나로 shader 객체를 생성한다
        int shader = GLES20.glCreateShader(type);

        checkGLerror("273");

        //shader 객체에 shader code를 로드합니다
        GLES20.glShaderSource(shader, shaderCode);

        checkGLerror("278");

        //shader 객체를 컴파일합니다.
        GLES20.glCompileShader(shader);

        checkGLerror("283");

        return shader;
    }
}