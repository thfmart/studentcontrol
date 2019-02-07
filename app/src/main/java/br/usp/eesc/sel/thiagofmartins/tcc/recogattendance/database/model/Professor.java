package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model;

import java.util.List;

/**
 * Created by Paula on 4/9/2016.
 */
public class Professor {
    private long id;
    private String professorEmail;
    private String name;
    private String password;
    private List<Course> courseList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getProfessorEmail() {
        return professorEmail;
    }

    public void setProfessorEmail(String professorEmail) {
        this.professorEmail = professorEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<Course> courseList) {
        this.courseList = courseList;
    }
}
