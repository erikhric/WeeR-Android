package sk.kebapp.weer.activities;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;

import java.util.List;
import java.util.Vector;

import es.ava.aruco.CameraParameters;
import es.ava.aruco.Marker;
import es.ava.aruco.MarkerDetector;
import sk.kebapp.weer.R;
import sk.kebapp.weer.application.ConnectionSettings;
import sk.kebapp.weer.application.DataSender;
import sk.kebapp.weer.application.HeadData;
import sk.kebapp.weer.widget.media.IjkVideoView;
import sk.kebapp.weer.widget.opengl.ViewToGLRenderer;
import sk.kebapp.weer.widget.opengl.renderer.RealStereoRenderer;
import sk.kebapp.weer.widget.opengl.renderer.SimulatedStereoRenderer;


public class StereoActivity extends VideoActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public GLSurfaceView mGLSurfaceView;
    public ViewToGLRenderer viewToGlRenderer;
    private DataSender dataSender;
    private HeadData headData;
    private IjkVideoView ijkVideoView;

    private boolean useOCV = false;

    private static final String TAG = "WEER";
    private static final float MARKER_SIZE = (float) 0.017;

    private static final boolean SHOW_MARKERID = false;


    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    if (useOCV)
                        mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        useOCV = ConnectionSettings.useOCV;

        ijkVideoView = (IjkVideoView) findViewById(R.id.videoView);
        ijkVideoView.setActivity(this);

        ///TODO: some dependency injection would be great
        headData = new HeadData(this);
        dataSender = new DataSender();
        dataSender.setData(headData);

        dataSender.execute(ConnectionSettings.ip, ConnectionSettings.port);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_camera_preview);

        if (useOCV) {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setMaxFrameSize(360, 240);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
        else {
            FrameLayout fl = (FrameLayout) findViewById(R.id.frame_layout);
            fl.removeAllViewsInLayout();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGLSurfaceView != null)
            mGLSurfaceView.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        mOpenCvCameraView.setZOrderMediaOverlay(true);
        mOpenCvCameraView.setZOrderOnTop(false);
        mGLSurfaceView.setZOrderOnTop(true);

        headData.startTracking();
        viewToGlRenderer.setHeadData(headData);
        viewToGlRenderer.setIjkVideoView(ijkVideoView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        headData.stopTracking();
        if (mGLSurfaceView != null)
            mGLSurfaceView.onPause();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        viewToGlRenderer.onStop();
    }

    public int getIjkVideoViewID() {
        return R.id.videoView;
    }

    Scalar color = new Scalar(250, 10, 10);

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Convert input to rgba
        if (!useOCV)
            return inputFrame.rgba();

        Mat rgba = inputFrame.rgba();

        //Setup required parameters for detect method
        MarkerDetector mDetector = new MarkerDetector();
        Vector<Marker> detectedMarkers = new Vector<>();
        CameraParameters camParams = new CameraParameters();
        camParams.readFromFile(Environment.getExternalStorageDirectory().toString());

        //Populate detectedMarkers
        mDetector.detect(rgba, detectedMarkers, camParams, MARKER_SIZE);

        /// TODO: either check whether only one marker is visible, or by marker ID
        if (detectedMarkers.size() != 0) {
            headData.setMarkerVisible(true);
            for (int i = 0; i < detectedMarkers.size(); i++) {
                Marker marker = detectedMarkers.get(i);
                marker.draw(rgba, color, 2, headData, true);

                if (SHOW_MARKERID) {
                    //Setup
                    int idValue = detectedMarkers.get(i).getMarkerId();
                    Vector<Point3> points = new Vector<>();
                    points.add(new Point3(0, 0, 0));
                    MatOfPoint3f pointMat = new MatOfPoint3f();
                    pointMat.fromList(points);
                    MatOfPoint2f outputPoints = new MatOfPoint2f();

                    //Project point to marker origin
                    Calib3d.projectPoints(pointMat, marker.getRvec(), marker.getTvec(), camParams.getCameraMatrix(), camParams.getDistCoeff(), outputPoints);
                    List<Point> pts = new Vector<>();
                    pts = outputPoints.toList();

                    //Draw id number
                    Core.putText(rgba, Integer.toString(idValue), pts.get(0), Core.FONT_HERSHEY_SIMPLEX, 2, color);
                    Log.d("ERIK", Integer.toString(idValue));
                }
            }
        } else {
            ///no marker found
            headData.setMarkerVisible(false);
        }

        return rgba;
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    public void initViews(boolean realStereo) {
        if (realStereo)
            viewToGlRenderer = new RealStereoRenderer(this);
        else
            viewToGlRenderer = new SimulatedStereoRenderer(this);


        mGLSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(viewToGlRenderer);

        mGLSurfaceView.setPreserveEGLContextOnPause(false);
        mGLSurfaceView.bringToFront();
    }
}
