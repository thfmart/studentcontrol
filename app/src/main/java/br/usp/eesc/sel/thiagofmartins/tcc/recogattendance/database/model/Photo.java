package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model;

/**
 * Created by Paula on 4/9/2016.
 */
public class Photo {
    private long id;
    private String image;
    private long lessonId;
    private long studentId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getLessonId() {
        return lessonId;
    }

    public void setLessonId(long lessonId) {
        this.lessonId = lessonId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }
}
