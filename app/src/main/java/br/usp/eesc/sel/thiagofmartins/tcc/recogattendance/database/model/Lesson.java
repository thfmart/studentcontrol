package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model;

import java.util.List;

/**
 * Created by Paula on 4/9/2016.
 */
public class Lesson {
    private long id;
    private String date;
    private long courseId;

    private List<Student> studentList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCourseId() {
        return courseId;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }
}
