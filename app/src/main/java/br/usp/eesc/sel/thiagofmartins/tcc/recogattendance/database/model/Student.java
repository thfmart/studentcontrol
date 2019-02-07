package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model;

import java.util.List;

/**
 * Created by Paula on 4/9/2016.
 */
public class Student {
    private long id;
    private long studentCode;
    private String name;
    private List<Photo> photoList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(long studentCode) {
        this.studentCode = studentCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Photo> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<Photo> photoList) {
        this.photoList = photoList;
    }
}
