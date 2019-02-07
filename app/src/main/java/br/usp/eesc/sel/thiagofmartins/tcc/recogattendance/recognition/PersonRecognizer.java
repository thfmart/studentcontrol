package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.javacv.cpp.opencv_contrib;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.MatVector;
import com.googlecode.javacv.cpp.opencv_imgproc;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

public class PersonRecognizer {


    opencv_contrib.FaceRecognizer faceRecognizer;
    String Path;

    static  final int WIDTH= 130;
    static  final int HEIGHT= 150;
    private int confidence;
    private Context context;


    public PersonRecognizer(Context context, String path)
    {
        this.context=context;
        faceRecognizer =  com.googlecode.javacv.cpp.opencv_contrib.createLBPHFaceRecognizer(2,8,7,7,50);//200
        Path=path;
    }

    public void update(MatVector matVector, int[] labels, String description,String Facefile){
        int size = DatabaseInteractor.getAllStudents(context).size();

        if (size>1)
        {
            faceRecognizer.update(matVector, labels);
        }
        else {
            faceRecognizer.train(matVector, labels);
        }
        faceRecognizer.save(Facefile);

    }


    public int predict(Mat m) {

        int n[] = new int[1];
        double p[] = new double[1];
        opencv_core.IplImage ipl = MatToIplImage(m,WIDTH, HEIGHT);
//		IplImage ipl = MatToIplImage(m,-1, -1);

        faceRecognizer.predict(ipl, n, p);
        if (n[0]!=-1)
        {confidence = (int)p[0];}
        else
        {confidence=-1;}

        if (n[0] != -1)
        {return n[0];}
        else
        {return -2;}
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
        opencv_core.IplImage image = opencv_core.IplImage.create(bmp.getWidth(), bmp.getHeight(), IPL_DEPTH_8U, 4);
        bmp.copyPixelsToBuffer(image.getByteBuffer());

        opencv_core.IplImage grayImg = opencv_core.IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(image, grayImg, opencv_imgproc.CV_BGR2GRAY);

        return grayImg;
    }

    public void load2(String path)
    {
        try {
            faceRecognizer.load(path);

        } catch (Exception e) {
            Log.e("error", e.getCause() + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getConfidence (){
        return confidence;
    }


}
