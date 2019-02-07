package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition;

import org.opencv.core.Mat;

/**
 * Created by Thiago on 1/9/2016.
 */
public class Preprocessedface {
    public Mat face;
    public boolean processed;

    public Preprocessedface(){
        face = new Mat();
        processed = false;

    }
}
