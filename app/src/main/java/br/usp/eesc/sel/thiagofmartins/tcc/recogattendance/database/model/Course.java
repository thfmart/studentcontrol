package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model;

import java.util.List;

/**
 * Created by Paula on 4/9/2016.
 */
public class Course {
    private long id;
    private String name;
    private String courseCode;
    private List<Student> studentList;
    private List<Lesson> lessonList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public List<Lesson> getLessonList() {
        return lessonList;
    }

    public void setLessonList(List<Lesson> lessonList) {
        this.lessonList = lessonList;
    }
}
