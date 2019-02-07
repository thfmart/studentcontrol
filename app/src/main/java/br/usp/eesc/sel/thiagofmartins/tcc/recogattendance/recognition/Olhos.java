package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition;

import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * Created by Thiago on 1/5/2016.
 */
public class Olhos {
    public Point leftEye;
    public Point rightEye;
    public Rect searchedLeftEye;
    public Rect searchedRightEye;

    public Olhos(){
        leftEye = new Point();
        rightEye = new Point();
        searchedLeftEye = new Rect();
        searchedRightEye = new Rect();

    }

}

