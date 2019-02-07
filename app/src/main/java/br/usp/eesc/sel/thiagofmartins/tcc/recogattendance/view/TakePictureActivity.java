package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.contrib.Contrib;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseHelper;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.Olhos;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.ProcessaImagem;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.TutorialCamera;

//import java.io.FileNotFoundException;
//import org.opencv.contrib.FaceRecognizer;


/**
 * Created by Thiago on 1/31/2016.
 */



public class TakePictureActivity extends Activity implements CvCameraViewListener2{ ///TakePicture extends Activity implements CameraBridgeViewBase.CvCameraViewListener2



    /*
        private static final String    TAG = "OCVSample::Activity";
        private File         mCascadeFile;
        private CascadeClassifier      mJavaDetector;
        private TutorialCamera mOpenCvCameraView;

        public static final int        JAVA_DETECTOR       = 0;
        public static final int        NATIVE_DETECTOR     = 1;

        private int                    mDetectorType       = JAVA_DETECTOR;
        private String[]               mDetectorName;

        private static final int frontCam =1;
        private static final int backCam =2;
        private int mChooseCamera = backCam;
        ImageButton imCamera;

        private Mat                    mRgba;
        private Mat                    mGray;
    */
    private Camera mCamera;
    private TutorialCamera mOpenCvCameraView;
    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int TRAINING= 0;
    public static final int SEARCHING= 1;
    public static final int IDLE= 2;

    private static final int frontCam =1;
    private static final int backCam =2;


    private int faceState=IDLE;
//    private int countTrain=0;

    //    private MenuItem               mItemFace50;
//    private MenuItem               mItemFace40;
//    private MenuItem               mItemFace30;
//    private MenuItem               mItemFace20;
//    private MenuItem               mItemType;
//
    private MenuItem               nBackCam;
    private MenuItem               mFrontCam;
    private MenuItem               mEigen;


    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    Olhos olhos = new Olhos();

    private File                   eyeCascade1File;
    private File                   eyeCascade2File;
    public CascadeClassifier      eyeCascade1;
    public CascadeClassifier      eyeCascade2;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely=999;

    String mPath="";


    private int mChooseCamera = backCam;

    EditText text;
    TextView textresult;
    private  ImageView Iv;
    Bitmap mBitmap;
    static Handler mHandler;


    ToggleButton toggleButtonGrabar,toggleButtonTrain,buttonSearch;
    Button buttonCatalog;
    ImageView ivGreen,ivYellow,ivRed;
    ImageButton imCamera;
    ImageButton buttonClick;
    boolean takePhoto = false;

    TextView textState;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


    long MAXIMG=9;
    public int resultCode = 2;

    ArrayList<Mat> alimgs = new ArrayList<Mat>();
    Bitmap test;



    int[] labels = new int[(int)MAXIMG];
    int countImages=0;
    long[]addr = new long[]{0,1,2,3,4,5,6,7,8};
    ProcessaImagem meuProcesso = new ProcessaImagem();





    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //   System.loadLibrary("detection_based_tracker");


                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);

                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        //mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    try {
                        // load cascade file from application resources
                        InputStream isa = getResources().openRawResource(R.raw.haarcascade_mcs_lefteye);
                        File cascadeDira = getDir("cascade", Context.MODE_PRIVATE);
                        eyeCascade1File = new File(cascadeDira, "haarcascade_mcs_lefteye.xml");
                        FileOutputStream osa = new FileOutputStream(eyeCascade1File);

                        byte[] buffera = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = isa.read(buffera)) != -1) {
                            osa.write(buffera, 0, bytesRead);
                        }
                        isa.close();
                        osa.close();

                        eyeCascade1 = new CascadeClassifier(eyeCascade1File.getAbsolutePath());
                        if (eyeCascade1.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            eyeCascade1 = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " +eyeCascade1File.getAbsolutePath());

                        //mJavaDetector = new CascadeClassifier(faceCascadeFile.getAbsolutePath());

                        cascadeDira.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }


                    try {
                        // load cascade file from application resources
                        InputStream isb = getResources().openRawResource(R.raw.haarcascade_mcs_righteye);
                        File cascadeDirb = getDir("cascade", Context.MODE_PRIVATE);
                        eyeCascade2File = new File(cascadeDirb, "haarcascade_mcs_righteye.xml");
                        FileOutputStream osb = new FileOutputStream(eyeCascade2File);

                        byte[] bufferb = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = isb.read(bufferb)) != -1) {
                            osb.write(bufferb, 0, bytesRead);
                        }
                        isb.close();
                        osb.close();

                        eyeCascade2 = new CascadeClassifier(eyeCascade2File.getAbsolutePath());
                        if (eyeCascade2.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            eyeCascade2 = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " +eyeCascade2File.getAbsolutePath());

                        //mJavaDetector = new CascadeClassifier(faceCascadeFile.getAbsolutePath());

                        cascadeDirb.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }


                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    public TakePictureActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }





    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mPath=Environment.getExternalStorageDirectory()+"/faceapp/faces/";
        setContentView(R.layout.activity_take_picture);



        //

        Iv=(ImageView)findViewById(R.id.takeView);

        /*mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj=="img") {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(test);
                    Iv.setImageBitmap(test);

                }
                else
                {

                }

            }
        };*/

        //
        buttonClick = (ImageButton) findViewById(R.id.imageButton);

        mOpenCvCameraView = (TutorialCamera) findViewById(R.id.tutorialCamera);

        mOpenCvCameraView.setCvCameraViewListener(this);

        int max = getIntent().getIntExtra("flag_qdt_foto",0);

        if (max==1) {
            MAXIMG = 1;
            resultCode = 1;
        }
        else if (max==2)
        {
            MAXIMG = 9;
            resultCode = 2;
        }

        else
        {
            resultCode=1;
        }


        buttonClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto=true;
            }

        });

        imCamera=(ImageButton)findViewById(R.id.changeCamera);

        imCamera.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (mChooseCamera == frontCam) {
                    mChooseCamera = backCam;
                    mOpenCvCameraView.setCamBack();
                } else {
                    mChooseCamera = frontCam;
                    mOpenCvCameraView.setCamFront();

                }
            }
        });




    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }


    public void onDestroy() {
        super.onDestroy();

        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Size size = mRgba.size();
        //Imgproc.resize(mRgba, mRgba, new Size(), 0.5, 0.5, Imgproc.INTER_AREA);
//960,1280
        mGray = inputFrame.gray();
        //mGray = Imgproc.resize(mGray,mGray,new Size(480,640),2,2,0);
        //Imgproc.equalizeHist(mGray, mGray);





        //if (getResources().getConfiguration().orientation==0);

        //Mat equalizedImg = new Mat();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        /************************************************Detect face - LBP cascade***********************************/

        //Size maxFeatureSize = new Size();// IMPORTANT second option: new Size();
        //Size minFeatureSize = new Size(20, 20);
        MatOfRect faces = new MatOfRect();
        mJavaDetector.detectMultiScale(mGray, faces, 1.1f, 2, Objdetect.CASCADE_FIND_BIGGEST_OBJECT,new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());


        Rect [] faceArray = faces.toArray();


        if (faceArray.length>0) {

            Rect r = faceArray[0];
            Mat face = mRgba.submat(r);
            if (face.width()<=0)
            {
                Point leftEye = new Point();
            }
            Mat packet = face.clone();

            Point leftEye = new Point();
            Point rightEye = new Point();
            olhos = detectBothEyes(face,eyeCascade1,eyeCascade2,olhos.leftEye, olhos.rightEye, olhos.searchedLeftEye, olhos.searchedRightEye);

            /***********Send intent back*****************/

            /*Intent intent = new Intent();
            long addr = face.getNativeObjAddr();
            intent.putExtra("obj",addr);
            setResult(2,intent);
            finish();*/

            /******************************************/

            /*************************************************************************************************************************/
            leftEye = olhos.leftEye;
            rightEye=olhos.rightEye;
            //leftEye.x = 1;
            //rightEye.x=1;
            //
            // Check if both eyes were detected.
            if (leftEye.x >= 0 && rightEye.x >= 0)
            //if(true)
            {
                //Core.putText(mRgba, "Face detectada", new Point(20, 20), 1, 5, new Scalar(255, 255, 255), 1, 8, false);
                //for (int i = 0; i < faceArray.length; i++)
                Core.rectangle(mRgba, faceArray[0].tl(), faceArray[0].br(), FACE_RECT_COLOR, 3);
                //Core.rectangle(mRgba, olhos.searchedLeftEye.tl(), olhos.searchedLeftEye.br(), new Scalar (255,255,255), 3);
                //Core.rectangle(mRgba, olhos.searchedRightEye.tl(), olhos.searchedRightEye.br(), new Scalar (255,255,255), 3);





                //
                if (takePhoto)
                {

                    /*Message msg = new Message();
                    String textTochange = "img";
                    msg.obj = textTochange;
                    mHandler.sendMessage(msg);
                    */
                    //
                    //Mat processada = meuProcesso.equalize(packet, leftEye, rightEye, 200, true);

                    Mat processada = meuProcesso.gray(packet);
                    Imgproc.equalizeHist(processada,processada);




                    if (countImages < MAXIMG &&(processada.width()>0))
                    {

                        //rightX[countImages]=rightEye.x;
                        // rightY[countImages]=rightEye.y;
                        // leftX[countImages]=leftEye.x;
                        //leftY[countImages]=leftEye.y;
                        //addr[countImages] = processada.getNativeObjAddr();//mudar de volta!!!
                        Bitmap bmp = ProcessaImagem.MattoBitmap(processada);
                        SaveImage(bmp,Integer.toString(countImages)+"A.jpg");

                        Bitmap final_Image = ProcessaImagem.MattoBitmap(packet);
                        SaveImage(final_Image,Integer.toString(countImages)+"B.jpg");//ATENÇÃOOOOO!!!!
                        //SaveImage(bmp,Integer.toString(countImages)+"B.jpg");




                    }
                    else
                    {
                        Intent intent = new Intent();
                        //long addr = face.getNativeObjAddr();
                        //intent.putExtra("obj", addr);
                        //intent.putExtra("9or1", resultCode);
                        //intent.putExtra("leftX",leftX);
                        //intent.putExtra("rightX", rightX);
                        //intent.putExtra("leftY",leftY);
                        //intent.putExtra("rightY", rightY);
                        setResult(RESULT_OK, intent);
                        //setResult(2,intent);
                        finish();
                    }
                    countImages++;
                }
            }


        }


        //Imgproc.resize(mRgba, mRgba, new Size(), 2, 2, Imgproc.INTER_LINEAR);
        return mRgba;


    }

    private void SaveImage(Bitmap finalBitmap, String fname) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/EESC-Face/temp/");
        myDir.mkdirs();
        Random generator = new Random();
        //int n = 10000;
        //n = generator.nextInt(n);
        //String fname = n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Rect detectLargestObject( Mat img, CascadeClassifier cascade, Rect faceRect, int scaledWidth)
    {
        // Only search for just 1 object (the biggest in the image).
        int flags = Objdetect.CASCADE_FIND_BIGGEST_OBJECT; // | CASCADE_DO_ROUGH_SEARCH;

        // Smallest object size.

        Size minFeatureSize =  new Size(20, 20);
        // How detailed should the search be. Must be larger than 1.0.
        float searchScaleFactor = 1.3f;
        // How much the detections should be filtered out. This should depend on how bad false detections are to your system.
        // minNeighbors=2 means lots of good+bad detections, and minNeighbors=6 means only good detections are given but some are missed.
        int minNeighbors = 2;

        // Perform Object or Face Detection, looking for just 1 object (the biggest in the image).

        List<Rect> objectsa = new ArrayList<>();
        //List<Mat> matList = new ArrayList<>();
        MatOfRect objects = new MatOfRect();


        detectObjectsCustom(img, cascade, objects, scaledWidth, flags, minFeatureSize, searchScaleFactor, minNeighbors);

        if (objects.toArray().length>0) {
            // Return the only detected object.
            faceRect = objects.toArray()[0];
        }
        else {
            // Return an invalid rect.
            faceRect = new Rect(-1,-1,-1,-1);
        }
        return faceRect;
    }

    public void detectObjectsCustom(final Mat img, CascadeClassifier cascade, MatOfRect objects, int scaledWidth, int flags, Size minFeatureSize, float searchScaleFactor, int minNeighbors)
    {
        // If the input image is not grayscale, then convert the BGR or BGRA color image to grayscale.
        //List<Mat> matList = new ArrayList<>();
        Mat gray = new Mat();
        if (img.channels() == 3) {
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);

        }
        else if (img.channels() == 4) {
            Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY);
        }
        else {
            // Access the input image directly, since it is already grayscale.
            gray = img;
        }

        // Possibly shrink the image, to run much faster.
        Mat inputImg = new Mat();


        float scale = img.cols() / (float)scaledWidth;
        if (img.cols() > scaledWidth) {
            // Shrink the image while keeping the same aspect ratio.

            int scaledHeight = Math.round(img.rows() / scale);

            Imgproc.resize(gray, inputImg, new Size(scaledWidth, scaledHeight));
        }
        else {
            // Access the input image directly, since it is already small.
            inputImg = gray;
        }

        // Standardize the brightness and contrast to improve dark images.
        Mat equalizedImg = new Mat();
        Imgproc.equalizeHist(inputImg, equalizedImg);
        Bitmap a = ProcessaImagem.MattoBitmap(equalizedImg);
        Size maxFeatureSize = new Size (equalizedImg.width(),equalizedImg.height());


        cascade.detectMultiScale(equalizedImg, objects, searchScaleFactor, minNeighbors, flags, minFeatureSize, maxFeatureSize);
        // Detect objects in the small grayscale image.
        //cascade.detectMultiScale(equalizedImg, objects, searchScaleFactor, minNeighbors, flags, minFeatureSize);
        //cascade.detectMultiScale(equalizedImg, objects, searchScaleFactor, minNeighbors, flags, minFeatureSize, maxFeatureSize);

        // Enlarge the results if the image was temporarily shrunk before detection.
        if (img.cols() > scaledWidth) {
            for (int i = 0; i < objects.toArray().length; i++ ) {
                int x = objects.toArray()[i].x;
                objects.toArray()[i].x =
                        objects.toArray()[i].x = Math.round(objects.toArray()[i].x * scale);
                objects.toArray()[i].y = Math.round(objects.toArray()[i].y * scale);
                objects.toArray()[i].width = Math.round(objects.toArray()[i].width * scale);
                objects.toArray()[i].height = Math.round(objects.toArray()[i].height * scale);
            }
        }

        // Make sure the object is completely within the image, in case it was on a border.
        for (int i = 0; i < objects.toArray().length; i++ ) {
            if (objects.toArray()[i].x < 0)
                objects.toArray()[i].x = 0;
            if (objects.toArray()[i].y < 0)
                objects.toArray()[i].y = 0;
            if (objects.toArray()[i].x + objects.toArray()[i].width > img.cols())
                objects.toArray()[i].x = img.cols() - objects.toArray()[i].width;
            if (objects.toArray()[i].y + objects.toArray()[i].height > img.rows())
                objects.toArray()[i].y = img.rows() - objects.toArray()[i].height;
        }

        // Return with the detected face rectangles stored in "objects".
    }

    public Olhos detectBothEyes(Mat face, CascadeClassifier eyeCascade1, CascadeClassifier eyeCascade2, Point leftEye, Point rightEye, Rect searchedLeftEye, Rect searchedRightEye)
    {
        // Skip the borders of the face, since it is usually just hair and ears, that we don't care about.
/*
    // For "2splits.xml": Finds both eyes in roughly 60% of detected faces, also detects closed eyes.
    const float EYE_SX = 0.12f;
    const float EYE_SY = 0.17f;
    const float EYE_SW = 0.37f;
    const float EYE_SH = 0.36f;
*/
/*
    // For mcs.xml: Finds both eyes in roughly 80% of detected faces, also detects closed eyes.
    const float EYE_SX = 0.10f;
    const float EYE_SY = 0.19f;
    const float EYE_SW = 0.40f;
    const float EYE_SH = 0.36f;
*/

        // For default eye.xml or eyeglasses.xml: Finds both eyes in roughly 40% of detected faces, but does not detect closed eyes.
/*        const float EYE_SX = 0.16f;
        const float EYE_SY = 0.26f;
        const float EYE_SW = 0.30f;
        const float EYE_SH = 0.28f;*/
        final float EYE_SX = 0.16f;
        final float EYE_SY = 0.26f;
        final float EYE_SW = 0.30f;
        final float EYE_SH = 0.28f;

        int leftX = Math.round(face.cols() * EYE_SX);
        int topY = Math.round(face.rows() * EYE_SY);
        int widthX = Math.round(face.cols() * EYE_SW);
        int heightY = Math.round(face.rows() * EYE_SH);
        int rightX = (int)Math.round(face.cols() * (1.0 - EYE_SX - EYE_SW));  // Start of right-eye corner



        Mat topLeftOfFace = new Mat (face,(new Rect(leftX, topY, widthX, heightY)));
        Mat topRightOfFace = new Mat (face,(new Rect(rightX, topY, widthX, heightY)));
        Rect leftEyeRect = new Rect();
        Rect rightEyeRect = new Rect();

        // Return the search windows to the caller, if desired.
        if (searchedLeftEye!=null)///////////////////////////////////////////check
            searchedLeftEye = new Rect(leftX, topY, widthX, heightY);
        if (searchedRightEye!=null)///////////////////////////////////////////check
            searchedRightEye = new Rect(rightX, topY, widthX, heightY);

        // Search the left region, then the right region using the 1st eye detector.
        leftEyeRect = detectLargestObject(topLeftOfFace, eyeCascade1, leftEyeRect, topLeftOfFace.cols());
        rightEyeRect = detectLargestObject(topRightOfFace, eyeCascade1, rightEyeRect, topRightOfFace.cols());

        // If the eye was not detected, try a different cascade classifier.
        boolean try_twice = false;
        if (try_twice) {
            if (leftEyeRect.width <= 0 && !eyeCascade2.empty()) {
                leftEyeRect = detectLargestObject(topLeftOfFace, eyeCascade2, leftEyeRect, topLeftOfFace.cols());
                //if (leftEyeRect.width > 0)
                //    cout << "2nd eye detector LEFT SUCCESS" << endl;
                //else
                //    cout << "2nd eye detector LEFT failed" << endl;
            }
            //else
            //    cout << "1st eye detector LEFT SUCCESS" << endl;

            // If the eye was not detected, try a different cascade classifier.
            if (rightEyeRect.width <= 0 && !eyeCascade2.empty()) {
                rightEyeRect = detectLargestObject(topRightOfFace, eyeCascade2, rightEyeRect, topRightOfFace.cols());
                //if (rightEyeRect.width > 0)
                //    cout << "2nd eye detector RIGHT SUCCESS" << endl;
                //else
                //    cout << "2nd eye detector RIGHT failed" << endl;
            }
        }
        //else
        //    cout << "1st eye detector RIGHT SUCCESS" << endl;

        if (leftEyeRect.width > 0) {   // Check if the eye was detected.
            leftEyeRect.x += leftX;    // Adjust the left-eye rectangle because the face border was removed.
            leftEyeRect.y += topY;
            leftEye = new Point(leftEyeRect.x + leftEyeRect.width/2, leftEyeRect.y + leftEyeRect.height/2);
        }
        else {
            leftEye = new Point(-1, -1);    // Return an invalid point
        }

        if (rightEyeRect.width > 0) { // Check if the eye was detected.
            rightEyeRect.x += rightX; // Adjust the right-eye rectangle, since it starts on the right side of the image.
            rightEyeRect.y += topY;  // Adjust the right-eye rectangle because the face border was removed.
            rightEye = new Point(rightEyeRect.x + rightEyeRect.width/2, rightEyeRect.y + rightEyeRect.height/2);
        }
        else {
            rightEye = new Point(-1, -1);    // Return an invalid point
        }
        Olhos olhos = new Olhos();
        olhos.leftEye = leftEye;
        olhos.rightEye = rightEye;
        olhos.searchedRightEye = searchedRightEye;
        olhos.searchedLeftEye = searchedLeftEye;
        return olhos;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        if (mOpenCvCameraView.numberCameras()>1)
        {
            nBackCam = menu.add("front");
            mFrontCam = menu.add("back");
//        mEigen = menu.add("EigenFaces");
//        mLBPH.setChecked(true);
        }
        else
        {imCamera.setVisibility(View.INVISIBLE);

        }
        //mOpenCvCameraView.setAutofocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
//        if (item == mItemFace50)
//            setMinFaceSize(0.5f);
//        else if (item == mItemFace40)
//            setMinFaceSize(0.4f);
//        else if (item == mItemFace30)
//            setMinFaceSize(0.3f);
//        else if (item == mItemFace20)
//            setMinFaceSize(0.2f);
//        else if (item == mItemType) {
//            mDetectorType = (mDetectorType + 1) % mDetectorName.length;
//            item.setTitle(mDetectorName[mDetectorType]);
//            setDetectorType(mDetectorType);
//
//        }
        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        //  mEigen.setChecked(false);
        if (item == nBackCam)
        {
            mOpenCvCameraView.setCamFront();
            mChooseCamera=frontCam;
        }
        //fr.changeRecognizer(0);
        else if (item==mFrontCam)
        {
            mChooseCamera=backCam;
            mOpenCvCameraView.setCamBack();

        }

        item.setChecked(true);

        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
//        if (mDetectorType != type) {
//            mDetectorType = type;
//
//            if (type == NATIVE_DETECTOR) {
//                Log.i(TAG, "Detection Based Tracker enabled");
//                mNativeDetector.start();
//            } else {
//                Log.i(TAG, "Cascade detector enabled");
//                mNativeDetector.stop();
//            }
//        }
    }




}
