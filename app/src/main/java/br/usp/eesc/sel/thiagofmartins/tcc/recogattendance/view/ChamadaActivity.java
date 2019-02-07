package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseUtil;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Lesson;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Photo;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.Labels;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.Olhos;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.PersonRecognizer;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.ProcessaImagem;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.TutorialCamera;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

//import java.io.FileNotFoundException;
//import org.opencv.contrib.FaceRecognizer;

/**
 * Created by Thiago on 1/31/2016.
 */


public class ChamadaActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 { ///TakePicture extends Activity implements CameraBridgeViewBase.CvCameraViewListener2



    private Camera mCamera;
    private TutorialCamera mOpenCvCameraView;
    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int TRAINING= 0;
    public static final int SEARCHING= 1;
    public static final int IDLE= 2;

    private static final int frontCam =1;
    private static final int backCam =2;


    private int faceState=IDLE;

//
    private MenuItem               nBackCam;
    private MenuItem               mFrontCam;
    private MenuItem               mEigen;


    private Mat mRgba;
    private Mat                    mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
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
    private ImageView Iv;

    ImageButton imCamera;

    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;


    long MAXIMG=9;

    ProcessaImagem meuProcesso = new ProcessaImagem();

    PersonRecognizer fr;
    String Path="";
    String Facefile="";
    Labels labelsFile;
    LinearLayout layout;
    LinearLayout cam;
    String resultName;
    int who;

    TextView name;
    TextView id;
    ImageView image;


    Photo photo = new Photo();
    Student student = new Student();
    Boolean canPredict;
    public List<Long> presentStudentsList = new ArrayList<Long>();

    private List<Integer> averagePerson= new ArrayList<Integer>();
    public List<Student> studentList= new ArrayList<Student>();
    Mat processada;
    Lesson lesson = new Lesson();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
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



    public ChamadaActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode==RESULT_OK) {
            Long studentCode = getIntent().getLongExtra("studentCode", 0);
            String image = getIntent().getStringExtra("face");



            if (studentCode > 0) {
                presentStudentsList.add(studentCode);

              /*  Bitmap face = DatabaseUtil.stringToBitmap(image);
                Bitmap tempA = readBitmap(0, "B");
                Size size = new Size(130, 150);
                Mat faceM = ProcessaImagem.Bitmap_to_Mat(tempA);
                int[] labels = new int[1];
                opencv_core.MatVector matVector = new opencv_core.MatVector(1);
                Imgproc.resize(faceM, faceM, size);
                opencv_core.IplImage IplFace;
                MatToIplImage(faceM,faceM.width(),faceM.height());
                IplFace=MatToIplImage(faceM,faceM.width(),faceM.height());
                matVector.put(0,IplFace);
                labels[0]= Integer.parseInt(studentCode.toString());//atencao!! escolher add ID aluno
                final opencv_core.MatVector m = matVector;
                final int[] lab = labels;
                fr.update(m,lab,DatabaseInteractor.getStudentByCode(this,studentCode).getName(),Facefile);*/
            }
        }
    }

    opencv_core.IplImage MatToIplImage(Mat m,int width,int heigth)
    {


        Bitmap bmp=Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


        Utils.matToBitmap(m, bmp);
        return BitmapToIplImage(bmp,width, heigth);

    }

    opencv_core.IplImage BitmapToIplImage(Bitmap bmp, int width, int height) {

        if ((width != -1) || (height != -1)) {
            Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, false);
            bmp = bmp2;
        }

        opencv_core.IplImage image = opencv_core.IplImage.create(bmp.getWidth(), bmp.getHeight(),
                IPL_DEPTH_8U, 4);

        bmp.copyPixelsToBuffer(image.getByteBuffer());

        opencv_core.IplImage grayImg = opencv_core.IplImage.create(image.width(), image.height(),
                IPL_DEPTH_8U, 1);

        cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

        return grayImg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_chamada);
        mOpenCvCameraView = (TutorialCamera) findViewById(R.id.tutorialCamera);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //imCamera=(ImageButton)findViewById(R.id.changeCamera);
        Button btn_accept = (Button)findViewById(R.id.acceptButton);
        Button btn_decline = (Button)findViewById(R.id.declineButton);
        Button btn_add = (Button)findViewById(R.id.addButton);
        Button btn_save = (Button)findViewById(R.id.saveChamada);
        layout = (LinearLayout)findViewById(R.id.frameLayout2);
        cam = (LinearLayout)findViewById(R.id.changeC);
        layout.setVisibility(View.INVISIBLE);
        name = (TextView)findViewById(R.id.nameView);
        id = (TextView)findViewById(R.id.idView);
        image = (ImageView)findViewById(R.id.chamadaView);
        final String courseCode = getIntent().getStringExtra("courseCode");
        final Long courseID = getIntent().getLongExtra("courseID", 0);
        Path=Environment.getExternalStorageDirectory()+"/EESC-FACE/faces/";
        Facefile=Environment.getExternalStorageDirectory()+"/EESC-FACE/faces/Face-Recognizer.xml";
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();


        lesson.setDate(dateFormat.format(date));
        lesson.setCourseId(courseID);

        fr=new PersonRecognizer(this, Path);
        if (Facefile!=null) {
            fr.load2(Facefile);
        }

        List<Student> allStudentsList = DatabaseInteractor.getAllStudents(ChamadaActivity.this);
        if (allStudentsList.size()>0){canPredict=true;}
        else{canPredict=false;}
        allStudentsList.clear();

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long studentCode = student.getStudentCode();
                presentStudentsList.add(studentCode);
                Photo dbPhoto = new Photo();
                Mat a = new Mat();
                Imgproc.resize(processada,a,new Size (30,40));
                dbPhoto.setImage(DatabaseUtil.bitmapToString(ProcessaImagem.MattoBitmap(a)));
                dbPhoto.setStudentId(student.getId());
                dbPhoto.setLessonId(lesson.getId());
                DatabaseInteractor.savePhoto(ChamadaActivity.this,dbPhoto);


                setVisibility(layout, false);
                setVisibility(cam, true);
            }
        });

        btn_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(layout,false);
                setVisibility(cam, true);
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChamadaActivity.this, SelectPeopleActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra("courseCode", courseCode);
                startActivityForResult(i, 0);

            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChamadaActivity.this, FinalActivity.class);//MediaStore.

                for(int i = 0;i<presentStudentsList.size();i++)
                {
                    student = DatabaseInteractor.getStudentByCode(ChamadaActivity.this, presentStudentsList.get(i));
                    studentList.add(student);
                    //DatabaseInteractor.addStudentToLesson(ChamadaActivity.this,student.getId(),lesson.getId());
                }

                lesson.setStudentList(studentList);
                DatabaseInteractor.saveLesson(ChamadaActivity.this, lesson);
                List<Lesson> lessonList = new ArrayList<Lesson>();
                lessonList = DatabaseInteractor.getLessonByCourseId(ChamadaActivity.this, courseID);
                Lesson lastLesson = lessonList.get(lessonList.size()-1);
                intent.putExtra("courseCode", courseCode);
                intent.putExtra("courseId",courseID);
                intent.putExtra("LessonID",lastLesson.getId());
                //// TODO: 5/14/2016 adicionar novas fotos de alunos adicionados manualmente
                startActivity(intent);
                finish();
            }
        });
        imCamera=(ImageButton)findViewById(R.id.changecam2);
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

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

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
        //mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,

        Rect[] faceArray = faces.toArray();


        if (faceArray.length>0) {

            Rect r = faceArray[0];
            Mat face = mRgba.submat(r);
            if (face.width() <= 0) {
                Point leftEye = new Point();
            }
            Mat packet = face.clone();

            Point leftEye = new Point();
            Point rightEye = new Point();
            olhos = detectBothEyes(face, eyeCascade1, eyeCascade2, olhos.leftEye, olhos.rightEye, olhos.searchedLeftEye, olhos.searchedRightEye);


            leftEye = olhos.leftEye;
            rightEye = olhos.rightEye;
            //adicionar processamento da imagem!!!!!
            if (leftEye.x >= 0 && rightEye.x >= 0)

            {
                Core.rectangle(mRgba, faceArray[0].tl(), faceArray[0].br(), FACE_RECT_COLOR, 3);

                processada = meuProcesso.gray(face);
                Imgproc.equalizeHist(processada, processada);

                //if(fr.canPredict())
                if (canPredict) {

                    int who = fr.predict(processada);

                    if (who > 0) {

                        student = DatabaseInteractor.getStudentByCode(ChamadaActivity.this, who);




                        resultName = student.getName();
                        photo = student.getPhotoList().get(0);


                        setVisibility(layout, true);
                        setVisibility(cam, false);
                        setText(name, resultName);
                        //setText(id, Integer.toString(fr.getConfidence()));
                        setText(id, Integer.toString((int)student.getStudentCode()));
                        setImage(image, DatabaseUtil.stringToBitmap(photo.getImage()));

                    }


                }


            } else {
                setVisibility(layout, false);
                setVisibility(cam, true);
            }
        }

        return mRgba;

    }

    public static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Map.Entry<T, Integer> max = null;

        for (Map.Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }

    private void setImage(final ImageView image,final Bitmap value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               image.setImageBitmap(value);
            }
        });
    }
    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    private void setVisibility(final LinearLayout layout,final Boolean flag){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (flag)
                    layout.setVisibility(View.VISIBLE);
                else
                    layout.setVisibility(View.GONE);
            }
        });
    }


    public Bitmap readBitmap(int id, String name) {
        Bitmap a = null;
        File f = Environment.getExternalStorageDirectory();
        File image = new File(f + "/EESC-Face/faces", name + "-" + Integer.toString(id) + ".jpg");
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        a = BitmapFactory.decodeFile(image.getAbsolutePath(), options);

        return a;
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

        if (searchedLeftEye!=null)///////////////////////////////////////////check
            searchedLeftEye = new Rect(leftX, topY, widthX, heightY);
        if (searchedRightEye!=null)///////////////////////////////////////////check
            searchedRightEye = new Rect(rightX, topY, widthX, heightY);

        leftEyeRect = detectLargestObject(topLeftOfFace, eyeCascade1, leftEyeRect, topLeftOfFace.cols());
        rightEyeRect = detectLargestObject(topRightOfFace, eyeCascade1, rightEyeRect, topRightOfFace.cols());

        boolean try_twice = false;
        if (try_twice) {
            if (leftEyeRect.width <= 0 && !eyeCascade2.empty()) {
                leftEyeRect = detectLargestObject(topLeftOfFace, eyeCascade2, leftEyeRect, topLeftOfFace.cols());

            }

            if (rightEyeRect.width <= 0 && !eyeCascade2.empty()) {
                rightEyeRect = detectLargestObject(topRightOfFace, eyeCascade2, rightEyeRect, topRightOfFace.cols());

            }
        }

        if (leftEyeRect.width > 0) {
            leftEyeRect.x += leftX;
            leftEyeRect.y += topY;
            leftEye = new Point(leftEyeRect.x + leftEyeRect.width/2, leftEyeRect.y + leftEyeRect.height/2);
        }
        else {
            leftEye = new Point(-1, -1);
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

        nBackCam.setChecked(false);
        mFrontCam.setChecked(false);
        if (item == nBackCam)
        {
            mOpenCvCameraView.setCamFront();
            mChooseCamera=frontCam;
        }
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

}



