package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thiago on 12/16/2015.
 */
public class ProcessaImagem
{
    public static final double DESIRED_LEFT_EYE_X = 0.16;     // Controls how much of the face is visible after preprocessing.
    public static final double DESIRED_LEFT_EYE_Y = 0.14;
    public static final double FACE_ELLIPSE_CY = 0.40;
    public static final double FACE_ELLIPSE_W = 0.50;         // Should be atleast 0.5
    public static final double FACE_ELLIPSE_H = 0.80;


    //const = public static final;
    //vector<Mat> preprocessedFaces; = List<mat> preprocessedFaces; i
    public static Mat Bitmap_to_Mat(Bitmap image) {

        //List<Mat> matList = new ArrayList<>();
        //matList.add(Mat a); = processfaces.push_back(faces);
        //matList.
        Mat m = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1);


        Utils.bitmapToMat(image, m);

        return m;
    }


    public Mat BitmaptoMat(Bitmap image) {

        //List<Mat> matList = new ArrayList<>();
        //matList.add(Mat a); = processfaces.push_back(faces);
        //matList.
        Mat m = new Mat (image.getWidth(), image.getHeight(), CvType.CV_8UC1,new Scalar(4));

        Utils.bitmapToMat(image, m);

        return m;
    }



    public static Bitmap MattoBitmap (Mat m){

        Bitmap resultBitmap = Bitmap.createBitmap(m.cols(),  m.rows(),Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(m, resultBitmap);

        return resultBitmap;
    }

    public Mat gray(Mat face)
    {
    Mat gray = new Mat();
    if (face.channels() == 3) {
        Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGR2GRAY);
    }
    else if (face.channels() == 4) {
        Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGRA2GRAY);
    }
    else {
        // Access the input image directly, since it is already grayscale.
        gray = face;
    }
    Size size = new Size(130,150);
        Imgproc.resize(gray,gray,size);
    return gray;

    }

    public Mat equalize(Mat face,Point leftEye,Point rightEye,int desiredFaceWidth, boolean doLeftAndRightSeparately)
    {

        Mat gray = new Mat();
        if (face.channels() == 3) {
            Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGR2GRAY);
        }
        else if (face.channels() == 4) {
            Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGRA2GRAY);
        }
        else {
            // Access the input image directly, since it is already grayscale.
            gray = face;
        }





        // Make the face image the same size as the training images.

        // Since we found both eyes, lets rotate & scale & translate the face so that the 2 eyes
        // line up perfectly with ideal eye positions. This makes sure that eyes will be horizontal,
        // and not too far left or right of the face, etc.

        // Get the center between the 2 eyes.
        int desiredFaceHeight=desiredFaceWidth;

        Point eyesCenter = new Point( ((float)(leftEye.x + rightEye.x) * 0.5f), ((float)(leftEye.y + rightEye.y) * 0.5f ));

        // Get the angle between the 2 eyes.
        double dy = (rightEye.y - leftEye.y);
        double dx = (rightEye.x - leftEye.x);
        double len = Math.sqrt(dx*dx + dy*dy);
        double angle = Math.atan2(dy, dx) * 180.0/Math.PI; // Convert from radians to degrees.

        // Hand measurements shown that the left eye center should ideally be at roughly (0.19, 0.14) of a scaled face image.
        final double DESIRED_RIGHT_EYE_X = (1.0f - DESIRED_LEFT_EYE_X);
        // Get the amount we need to scale the image to be the desired fixed size we want.
        double desiredLen = (DESIRED_RIGHT_EYE_X - DESIRED_LEFT_EYE_X) * desiredFaceWidth;
        double scale = desiredLen / len;
        // Get the transformation matrix for rotating and scaling the face to the desired angle & size.
        Mat rot_mat = Imgproc.getRotationMatrix2D(eyesCenter, angle, scale);
        // Shift the center of the eyes to be the desired center between the eyes.
        //byte buff[] = new byte[rot_mat.total()*rot_mat.channels()
        //checkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk itttttttttttttttttttttttttttttttttttttttt

        rot_mat.put(0,2,(int)(rot_mat.get(0,2)[0])+desiredFaceWidth*0.5f-eyesCenter.x);//rot_mat.at<double>(0, 2) += desiredFaceWidth * 0.5f - eyesCenter.x;
        rot_mat.put(1,2,(int)(rot_mat.get(1,2)[0])+desiredFaceHeight*DESIRED_LEFT_EYE_Y - eyesCenter.y);// rot_mat.at<double>(1, 2) += desiredFaceHeight * DESIRED_LEFT_EYE_Y - eyesCenter.y;








        // Rotate and scale and translate the image to the desired angle & size & position!
        // Note that we use 'w' for the height instead of 'h', because the input face has 1:1 aspect ratio.
        Bitmap grayb = MattoBitmap(gray);
        Bitmap f = MattoBitmap(face);

        Mat warped = new Mat(desiredFaceHeight, desiredFaceWidth, CvType.CV_8U, new Scalar(128)); // Clear the output image to a default grey.
        Bitmap ba = MattoBitmap(warped);
        Imgproc.warpAffine(gray, warped, rot_mat, warped.size());
        //imshow("warped", warped);
        Bitmap b = MattoBitmap(warped);
        // Give the image a standard brightness and contrast, in case it was too dark or had low contrast.
        if (!doLeftAndRightSeparately) {
            // Do it on the whole face.
            Imgproc.equalizeHist(warped, warped);

        }
        else {
            // Do it seperately for the left and right sides of the face.
            equalizeLeftAndRightHalves(warped);

        }
        //imshow("equalized", warped);

        // Use the "Bilateral Filter" to reduce pixel noise by smoothing the image, but keeping the sharp edges in the face.
        Mat filtered = new Mat(warped.size(), CvType.CV_8U);
        Imgproc.bilateralFilter(warped, filtered, 0, 20.0, 2.0);
        //imshow("filtered", filtered);

        // Filter out the corners of the face, since we mainly just care about the middle parts.
        // Draw a filled ellipse in the middle of the face-sized image.
        Mat mask =new  Mat(warped.size(), CvType.CV_8U, new Scalar(0)); // Start with an empty mask.
        Point faceCenter = new Point( desiredFaceWidth/2, Math.round(desiredFaceHeight * FACE_ELLIPSE_CY) );
        Size size = new Size( Math.round(desiredFaceWidth * FACE_ELLIPSE_W), Math.round(desiredFaceHeight * FACE_ELLIPSE_H) );
        Core.ellipse(mask, faceCenter, size, 0, 0, 360, new Scalar(255), Core.FILLED);////Check!!!!!!!!!!!!!!!!!!!!!!!!!!
        Bitmap c = MattoBitmap(mask);

        //imshow("mask", mask);

        // Use the mask, to remove outside pixels.
        Mat dstImg = new Mat(warped.size(), CvType.CV_8U, new Scalar(128)); // Clear the output image to a default gray.
            /*
            namedWindow("filtered");
            imshow("filtered", filtered);
            namedWindow("dstImg");
            imshow("dstImg", dstImg);
            namedWindow("mask");
            imshow("mask", mask);
            */
        // Apply the elliptical mask on the face.
        filtered.copyTo(dstImg, mask);  // Copies non-masked pixels from filtered to dstImg.
        //imshow("dstImg", dstImg);

        return dstImg;


    }






    public Preprocessedface getPreprocessedFace(Mat srcImg,
                                                int desiredFaceWidth,
                                                CascadeClassifier faceCascade,
                                                CascadeClassifier eyeCascade1,
                                                CascadeClassifier eyeCascade2,
                                                boolean doLeftAndRightSeparately,
                                                Rect storeFaceRect,
                                                Point storeLeftEye,
                                                Point storeRightEye,
                                                Rect searchedLeftEye,
                                                Rect searchedRightEye)
    {

        // Use square faces.
        int desiredFaceHeight = desiredFaceWidth;
        // Mark the detected face region and eye search regions as invalid, in case they aren't detected.
        if (storeFaceRect!= null)
            storeFaceRect.width = -1;
        if (storeLeftEye!=null)
            storeLeftEye.x = -1;
        if (storeRightEye!=null)
            storeRightEye.x= -1;
        if (searchedLeftEye!=null)
            searchedLeftEye.width = -1;
        if (searchedRightEye!=null)
            searchedRightEye.width = -1;

        // Find the largest face.
        Rect faceRect = new Rect();


        int scaledWidth = 150;
        faceRect = detectLargestObject(srcImg, faceCascade, faceRect,scaledWidth);

        // Check if a face was detected.
        if (faceRect.width > 0) {

            // Give the face rect to the caller if desired.
            if (storeFaceRect !=null)///////////////////////////////////////////check
                storeFaceRect = faceRect;


            Mat faceImg = new Mat (srcImg,(faceRect));    // Get the detected face image.
            Bitmap a = MattoBitmap(faceImg);
            // If the input image is not grayscale, then convert the BGR or BGRA color image to grayscale.
            Mat gray = new Mat();
            if (faceImg.channels() == 3) {
                Imgproc.cvtColor(faceImg, gray, Imgproc.COLOR_BGR2GRAY);
            }
            else if (faceImg.channels() == 4) {
                Imgproc.cvtColor(faceImg, gray, Imgproc.COLOR_BGRA2GRAY);
            }
            else {
                // Access the input image directly, since it is already grayscale.
                gray = faceImg;
            }

            // Search for the 2 eyes at the full resolution, since eye detection needs max resolution possible!
            Point leftEye = new Point();
            Point rightEye = new Point();
            Olhos olhos = new Olhos();
            olhos = detectBothEyes(gray, eyeCascade1, eyeCascade2, olhos.leftEye, olhos.rightEye, olhos.searchedLeftEye, olhos.searchedRightEye);
            searchedLeftEye = olhos.searchedLeftEye;
            searchedRightEye = olhos.searchedRightEye;
            leftEye = olhos.leftEye;
            rightEye=olhos.rightEye;

            // Give the eye results to the caller if desired.
            if (storeLeftEye!=null)///////////////////////////////////////////check
                storeLeftEye = leftEye;
            if (storeRightEye!=null)///////////////////////////////////////////check
                storeRightEye = rightEye;

            // Check if both eyes were detected.
            if (leftEye.x >= 0 && rightEye.x >= 0) {

                // Make the face image the same size as the training images.

                // Since we found both eyes, lets rotate & scale & translate the face so that the 2 eyes
                // line up perfectly with ideal eye positions. This makes sure that eyes will be horizontal,
                // and not too far left or right of the face, etc.

                // Get the center between the 2 eyes.


                Point eyesCenter = new Point( ((float)(leftEye.x + rightEye.x) * 0.5f), ((float)(leftEye.y + rightEye.y) * 0.5f ));

                // Get the angle between the 2 eyes.
                double dy = (rightEye.y - leftEye.y);
                double dx = (rightEye.x - leftEye.x);
                double len = Math.sqrt(dx*dx + dy*dy);
                double angle = Math.atan2(dy, dx) * 180.0/Math.PI; // Convert from radians to degrees.

                // Hand measurements shown that the left eye center should ideally be at roughly (0.19, 0.14) of a scaled face image.
                final double DESIRED_RIGHT_EYE_X = (1.0f - DESIRED_LEFT_EYE_X);
                // Get the amount we need to scale the image to be the desired fixed size we want.
                double desiredLen = (DESIRED_RIGHT_EYE_X - DESIRED_LEFT_EYE_X) * desiredFaceWidth;
                double scale = desiredLen / len;
                // Get the transformation matrix for rotating and scaling the face to the desired angle & size.
                Mat rot_mat = Imgproc.getRotationMatrix2D(eyesCenter, angle, scale);
                // Shift the center of the eyes to be the desired center between the eyes.
                //byte buff[] = new byte[rot_mat.total()*rot_mat.channels()
                //checkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk itttttttttttttttttttttttttttttttttttttttt
                rot_mat.put(0,2,(int)(rot_mat.get(0,2)[0])+desiredFaceWidth*0.5f-eyesCenter.x);//rot_mat.at<double>(0, 2) += desiredFaceWidth * 0.5f - eyesCenter.x;
                rot_mat.put(1,2,(int)(rot_mat.get(1,2)[0])*desiredFaceHeight*DESIRED_LEFT_EYE_Y - eyesCenter.y);// rot_mat.at<double>(1, 2) += desiredFaceHeight * DESIRED_LEFT_EYE_Y - eyesCenter.y;








                // Rotate and scale and translate the image to the desired angle & size & position!
                // Note that we use 'w' for the height instead of 'h', because the input face has 1:1 aspect ratio.


                Mat warped = new Mat(desiredFaceHeight, desiredFaceWidth, CvType.CV_8U, new Scalar(128)); // Clear the output image to a default grey.
                Imgproc.warpAffine(gray, warped, rot_mat, warped.size());
                //imshow("warped", warped);
                Bitmap b = MattoBitmap(warped);
                // Give the image a standard brightness and contrast, in case it was too dark or had low contrast.
                if (!doLeftAndRightSeparately) {
                    // Do it on the whole face.
                    Imgproc.equalizeHist(warped, warped);

                }
                else {
                    // Do it seperately for the left and right sides of the face.
                    equalizeLeftAndRightHalves(warped);
                }
                //imshow("equalized", warped);

                // Use the "Bilateral Filter" to reduce pixel noise by smoothing the image, but keeping the sharp edges in the face.
                Mat filtered = new Mat(warped.size(), CvType.CV_8U);
                Imgproc.bilateralFilter(warped, filtered, 0, 20.0, 2.0);
                //imshow("filtered", filtered);

                // Filter out the corners of the face, since we mainly just care about the middle parts.
                // Draw a filled ellipse in the middle of the face-sized image.
                Mat mask =new  Mat(warped.size(), CvType.CV_8U, new Scalar(0)); // Start with an empty mask.
                Point faceCenter = new Point( desiredFaceWidth/2, Math.round(desiredFaceHeight * FACE_ELLIPSE_CY) );
                Size size = new Size( Math.round(desiredFaceWidth * FACE_ELLIPSE_W), Math.round(desiredFaceHeight * FACE_ELLIPSE_H) );
                Core.ellipse(mask, faceCenter, size, 0, 0, 360, new Scalar(255), Core.FILLED);////Check!!!!!!!!!!!!!!!!!!!!!!!!!!
                Bitmap c = MattoBitmap(mask);

                //imshow("mask", mask);

                // Use the mask, to remove outside pixels.
                Mat dstImg = new Mat(warped.size(), CvType.CV_8U, new Scalar(128)); // Clear the output image to a default gray.
            /*
            namedWindow("filtered");
            imshow("filtered", filtered);
            namedWindow("dstImg");
            imshow("dstImg", dstImg);
            namedWindow("mask");
            imshow("mask", mask);
            */
                // Apply the elliptical mask on the face.
                filtered.copyTo(dstImg, mask);  // Copies non-masked pixels from filtered to dstImg.
                //imshow("dstImg", dstImg);

                Preprocessedface preprocessedface = new Preprocessedface();
                preprocessedface.face = dstImg;
                preprocessedface.processed = true;
                return preprocessedface;


            }
        /*
        else {
            // Since no eyes were found, just do a generic image resize.
            resize(gray, tmpImg, Size(w,h));
        }
        */
        }

        Preprocessedface preprocessedface = new Preprocessedface();
        preprocessedface.face = new Mat();
        preprocessedface.processed = false;
        return preprocessedface;

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
        Bitmap a = MattoBitmap(equalizedImg);
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

    public Olhos detectBothEyes(final Mat face, CascadeClassifier eyeCascade1, CascadeClassifier eyeCascade2, Point leftEye, Point rightEye, Rect searchedLeftEye, Rect searchedRightEye)
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
        if (leftEyeRect.width <= 0 && !eyeCascade2.empty()) {
           leftEyeRect= detectLargestObject(topLeftOfFace, eyeCascade2, leftEyeRect, topLeftOfFace.cols());
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

    public void  equalizeLeftAndRightHalves(Mat faceImg)
    {
        // It is common that there is stronger light from one half of the face than the other. In that case,
        // if you simply did histogram equalization on the whole face then it would make one half dark and
        // one half bright. So we will do histogram equalization separately on each face half, so they will
        // both look similar on average. But this would cause a sharp edge in the middle of the face, because
        // the left half and right half would be suddenly different. So we also histogram equalize the whole
        // image, and in the middle part we blend the 3 images together for a smooth brightness transition.

        int w = faceImg.cols();
        int h = faceImg.rows();

        // 1) First, equalize the whole face.
        Mat wholeFace = new Mat();
        Imgproc.equalizeHist(faceImg, wholeFace);

        // 2) Equalize the left half and the right half of the face separately.
        int midX = w/2;
        Mat leftSide = new Mat(faceImg,(new Rect(0,0, midX,h)));
        Mat rightSide = new Mat (faceImg,(new Rect(midX,0, w-midX,h)));
        Imgproc.equalizeHist(leftSide, leftSide);
        Imgproc.equalizeHist(rightSide, rightSide);

        // 3) Combine the left half and right half and whole face together, so that it has a smooth transition.
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                int v;
                if (x < w/4) {          // Left 25%: just use the left face.
                    v = (int)(leftSide.get(y,x)[0]);//v = leftSide.at<uchar>(y,x);

                }
                else if (x < w*2/4) {   // Mid-left 25%: blend the left face & whole face.
                    int lv = (int)(leftSide.get(y,x)[0]);//int lv = leftSide.at<uchar>(y,x);
                    int wv = (int)(wholeFace.get(y,x)[0]);//int wv = wholeFace.at<uchar>(y,x);


                    // Blend more of the whole face as it moves further right along the face.
                    float f = (x - w*1/4) / (float)(w*0.25f);
                    v = Math.round((1.0f - f) * lv + (f) * wv);
                }
                else if (x < w*3/4) {   // Mid-right 25%: blend the right face & whole face.
                    int rv = (int)(rightSide.get(y,x-midX)[0]);//int rv = rightSide.at<uchar>(y,x-midX);
                    int wv = (int)(wholeFace.get(y,x)[0]);//int wv = wholeFace.at<uchar>(y,x);
                    // Blend more of the right-side face as it moves further right along the face.
                    float f = (x - w*2/4) / (float)(w*0.25f);
                    v = Math.round((1.0f - f) * wv + (f) * rv);
                }
                else {                  // Right 25%: just use the right face.
                    v = (int)(rightSide.get(y,x-midX)[0]);//v = rightSide.at<uchar>(y,x-midX);

                }
                faceImg.put(y,x,v);//checkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                //faceImg.at<uchar>(y,x) = v;
            }// end x loop
        }//end y loop
    }


    public void detectManyObjects(final Mat img, CascadeClassifier cascade, MatOfRect objects, int scaledWidth)
    {
        // Search for many objects in the one image.
        int flags = Objdetect.CASCADE_SCALE_IMAGE;

        // Smallest object size.
        Size minFeatureSize = new Size(20, 20);
        // How detailed should the search be. Must be larger than 1.0.
        float searchScaleFactor = 1.1f;
        // How much the detections should be filtered out. This should depend on how bad false detections are to your system.
        // minNeighbors=2 means lots of good+bad detections, and minNeighbors=6 means only good detections are given but some are missed.
        int minNeighbors = 4;
        //MatOfRect objects = new MatOfRect();
        // Perform Object or Face Detection, looking for many objects in the one image.
        detectObjectsCustom(img, cascade, objects, scaledWidth, flags, minFeatureSize, searchScaleFactor, minNeighbors);
    }
}
