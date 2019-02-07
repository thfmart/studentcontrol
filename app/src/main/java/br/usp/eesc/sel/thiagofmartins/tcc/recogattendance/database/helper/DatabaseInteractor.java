package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.List;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Course;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Lesson;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Photo;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Professor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view.FinalActivity;


/**
 * Created by Thiago on 4/17/2016.
 */
public class DatabaseInteractor {

    public static void saveProfessor(Context context, Professor professor){
        new DatabaseHelper(context).createProfessor(professor);
    }

    public static void saveCourse(Context context, Course course){
        new DatabaseHelper(context).createCourse(course);
    }

    public static void saveLesson(Context context, Lesson lesson){
        long lessonId = new DatabaseHelper(context).createLesson(lesson);
        for(Student student: lesson.getStudentList()){
            addStudentToLesson(context,student.getId(), lessonId);
        }
    }

    public static void saveStudent(Context context, Student student){
        new DatabaseHelper(context).createStudent(student);
    }

    public static void savePhoto(Context context, Photo photo){
        new DatabaseHelper(context).createPhoto(photo);
    }

    public static Professor getProfessorByEmail(Context context, String professorEmail){
        return new DatabaseHelper(context).getProfessorByEmail(professorEmail);
    }

    public static void addCourseToProfessor(Context context, long courseId, long professorId){
        new DatabaseHelper(context).addCourseToProfessor(professorId, courseId);
    }

    public static void addStudentToCourse(Context context, long studentId, long courseId){
        new DatabaseHelper(context).addStudentToCourse(courseId, studentId);
    }

    public static void addStudentToLesson(Context context, long studentId, long lessonId){
        new DatabaseHelper(context).addStudentToLesson(lessonId, studentId);
    }

    public static void saveBitmapAsPhoto(Context context, Bitmap bitmap, long studentId, long lessonId){
        Photo photo = new Photo();
        photo.setImage(DatabaseUtil.bitmapToString(bitmap));
        photo.setLessonId(lessonId);
        photo.setStudentId(studentId);
        savePhoto(context, photo);
    }

    public static Course getCourseByCode(Context context,String code) {
        return new DatabaseHelper(context).getCourseByCode(code);
    }

    public static List<Lesson> getLessonByCourseId (Context context, long courseId){
        return new DatabaseHelper(context).getLessonByCourseId(courseId);
    }

    public static Student getStudentByCode(Context context, long studentCode) {
        return new DatabaseHelper(context).getStudentByCode(studentCode);
    }

    public static List<Student> getAllStudents(Context context) {
        return new DatabaseHelper(context).getAllStudents();
    }

    public static List<Student> getStudentsByLessonId(Context context, long lessonId) {
        return new DatabaseHelper(context).getStudentsByLessonId(lessonId);
    }

    public static void deleteLesson(Context context, long lessonId) {
        new DatabaseHelper(context).deleteLesson(lessonId);
    }
}
