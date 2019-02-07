package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Course;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Lesson;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Photo;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Professor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;


public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "facerecog.db";

    // Table Names
    private static final String TABLE_COURSE = "courses";
    private static final String TABLE_LESSON = "lessons";
    private static final String TABLE_PHOTO = "photos";
    private static final String TABLE_PROFESSOR = "professors";
    private static final String TABLE_STUDENT = "students";

    private static final String TABLE_PROFESSOR_COURSE = "professor_courses";
    private static final String TABLE_COURSE_STUDENT = "course_students";
    private static final String TABLE_LESSON_STUDENT = "lesson_students";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    // PROFESSOR Table - column names
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PROFESSOR_ID = "professor_id";
    private static final String KEY_PROFESSOR_EMAIL = "professor_email";

    // COURSE Table - column names
    private static final String KEY_COURSE_ID = "course_id";
    private static final String KEY_COURSE_CODE = "course_code";

    // STUDENT Table - column names
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_STUDENT_CODE = "student_code";

    // LESSON Table - column names
    private static final String KEY_LESSON_ID = "lesson_id";
    private static final String KEY_DATE = "date";

    // PHOTO Table - column names
    private static final String KEY_PHOTO_IMAGE = "image";
    private static final String KEY_PHOTO_ID = "photo_id";

    // Table Create Statements
    // PROFESSOR table create statement
    private static final String CREATE_TABLE_PROFESSOR = "CREATE TABLE "
            + TABLE_PROFESSOR
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_PROFESSOR_EMAIL + " TEXT,"
            + KEY_PASSWORD + " TEXT" + ")";

    // COURSE table create statement
    private static final String CREATE_TABLE_COURSE = "CREATE TABLE "+ TABLE_COURSE
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT,"
            + KEY_COURSE_CODE + " TEXT" + ")";

    // STUDENT table create statement
    private static final String CREATE_TABLE_STUDENT = "CREATE TABLE "+ TABLE_STUDENT
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_STUDENT_CODE + " INTEGER,"
            + KEY_NAME + " TEXT" + ")";

    // LESSON table create statement
    private static final String CREATE_TABLE_LESSON = "CREATE TABLE "+ TABLE_LESSON
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_COURSE_ID + " INTEGER,"
            + KEY_DATE + " DATETIME" + ")";

    // PHOTO table create statement
    private static final String CREATE_TABLE_PHOTO = "CREATE TABLE "+ TABLE_PHOTO
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_STUDENT_ID + " INTEGER,"
            + KEY_LESSON_ID + " INTEGER,"
            + KEY_PHOTO_IMAGE + " TEXT" + ")";

    // PROFESSOR_COURSE table create statement
    private static final String CREATE_TABLE_PROFESSOR_COURSE = "CREATE TABLE " + TABLE_PROFESSOR_COURSE
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_PROFESSOR_ID + " INTEGER,"
            + KEY_COURSE_ID + " INTEGER" + ")";

    // COURSE_STUDENT table create statement
    private static final String CREATE_TABLE_COURSE_STUDENT = "CREATE TABLE " + TABLE_COURSE_STUDENT
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_COURSE_ID + " INTEGER,"
            + KEY_STUDENT_ID + " INTEGER" + ")";

    // LESSON_STUDENT table create statement
    private static final String CREATE_TABLE_LESSON_STUDENT = "CREATE TABLE " + TABLE_LESSON_STUDENT
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_LESSON_ID + " INTEGER,"
            + KEY_STUDENT_ID + " INTEGER" + ")";

    public DatabaseHelper(Context context) {
        super(context, "/mnt/sdcard/" + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_PROFESSOR);
        db.execSQL(CREATE_TABLE_COURSE);
        db.execSQL(CREATE_TABLE_STUDENT);
        db.execSQL(CREATE_TABLE_LESSON);
        db.execSQL(CREATE_TABLE_PHOTO);
        db.execSQL(CREATE_TABLE_PROFESSOR_COURSE);
        db.execSQL(CREATE_TABLE_COURSE_STUDENT);
        db.execSQL(CREATE_TABLE_LESSON_STUDENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFESSOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSON);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFESSOR_COURSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSON_STUDENT);

        // create new tables
        onCreate(db);
    }

    /*
    * Creating a PROFESSOR
    */
    public long createProfessor(Professor professor) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, professor.getName());
        values.put(KEY_PASSWORD, professor.getPassword());
        values.put(KEY_PROFESSOR_EMAIL, professor.getProfessorEmail());

        // insert row
        long professorId = db.insert(TABLE_PROFESSOR, null, values);

        return professorId;
    }

    /*
    * Creating a COURSE with Students
    */
    public long createCourse(Course course, long[]studentIds) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, course.getName());
        values.put(KEY_COURSE_CODE, course.getCourseCode());

        // insert row
        long courseId = db.insert(TABLE_COURSE, null, values);

        if(studentIds != null) {
            // assigning Students to Course
            for (long studentId : studentIds) {
                addStudentToCourse(courseId, studentId);
            }
        }

        return courseId;
    }

    /*
    * Creating a COURSE
    */
    public long createCourse(Course course) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, course.getName());
        values.put(KEY_COURSE_CODE, course.getCourseCode());

        // insert row
        long courseId = db.insert(TABLE_COURSE, null, values);
        // assigning Students to Course
        for (Student student : course.getStudentList()) {
            addStudentToCourse(courseId, student.getId());
        }


        return courseId;
    }

    /*
    * Creating a STUDENT
    */
    public long createStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, student.getName());
        values.put(KEY_STUDENT_CODE, student.getStudentCode());

        // insert row
        long studentId = db.insert(TABLE_STUDENT, null, values);

        return studentId;
    }

    /*
    * Creating a LESSON with Students
    */
    public long createLesson(Lesson lesson, long[] studentIds) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COURSE_ID, lesson.getCourseId());
        values.put(KEY_DATE, lesson.getDate());

        // insert row
        long lessonId = db.insert(TABLE_LESSON, null, values);

        // assigning Students to Lesson
        for (long studentId : studentIds) {
            addStudentToLesson(lessonId, studentId);
        }

        return lessonId;
    }

    /*
    * Creating a LESSON
    */
    public long createLesson(Lesson lesson) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COURSE_ID, lesson.getCourseId());
        values.put(KEY_DATE, lesson.getDate());

        // insert row
        long lessonId = db.insert(TABLE_LESSON, null, values);

        return lessonId;
    }

    /*
    * Creating a PHOTO
    */
    public long createPhoto(Photo photo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LESSON_ID, photo.getLessonId());
        values.put(KEY_STUDENT_ID, photo.getStudentId());
        values.put(KEY_PHOTO_IMAGE, photo.getImage());

        // insert row
         return db.insert(TABLE_PHOTO, null, values);
    }

    /*
     * Creating PROFESSOR_COURSE
     */
    public long addCourseToProfessor(long professorId, long courseId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROFESSOR_ID, professorId);
        values.put(KEY_COURSE_ID, courseId);

        return db.insert(TABLE_PROFESSOR_COURSE, null, values);

    }

    /*
     * Creating COURSE_STUDENT
     */
    public long addStudentToCourse(long courseId, long studentId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COURSE_ID, courseId);
        values.put(KEY_STUDENT_ID, studentId);

        return db.insert(TABLE_COURSE_STUDENT, null, values);

    }


    /*
     * Creating LESSON_STUDENT
     */
    public long addStudentToLesson(long lessonId, long studentId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LESSON_ID, lessonId);
        values.put(KEY_STUDENT_ID, studentId);

        return db.insert(TABLE_LESSON_STUDENT, null, values);

    }

    /*
    * get single Professor
    */
    public Professor getProfessorByEmail(String professorEmail) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_PROFESSOR
                          + " WHERE " + KEY_PROFESSOR_EMAIL + " = '" + professorEmail+"'";

        Log.d(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null && c.moveToFirst()) {

            Professor professor = new Professor();
            professor.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            professor.setProfessorEmail((c.getString(c.getColumnIndex(KEY_PROFESSOR_EMAIL))));
            professor.setName(c.getString(c.getColumnIndex(KEY_NAME)));
            professor.setPassword(c.getString(c.getColumnIndex(KEY_PASSWORD)));

            List<Course> courses = getCoursesByProfessor(professor.getId());
            professor.setCourseList(new ArrayList<Course>());
            for (Course course : courses) {
                professor.getCourseList().add(course);
            }

            return professor;
        } else {
            return null;
        }
    }

    private List<Course> getCoursesByProfessor(long professorId) {
        List<Course> courseList = new ArrayList<Course>();

        String selectQuery = "SELECT  * FROM "
                + TABLE_COURSE
                + " WHERE EXISTS ("
                + " SELECT * FROM "+ TABLE_PROFESSOR_COURSE
                + " WHERE "+TABLE_PROFESSOR_COURSE+ "." + KEY_PROFESSOR_ID+ " = '" + professorId + "'"
                + " AND "+ TABLE_COURSE+"." + KEY_ID + " = " +TABLE_PROFESSOR_COURSE +"." + KEY_COURSE_ID+")";

        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Course course = new Course();
                course.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                course.setName((c.getString(c.getColumnIndex(KEY_NAME))));
                course.setCourseCode(c.getString(c.getColumnIndex(KEY_COURSE_CODE)));

                course.setStudentList(getStudentsByCourse(course.getId()));

                courseList.add(course);
            } while (c.moveToNext());
        }

        return courseList;
    }

    private List<Student> getStudentsByCourse(long courseId) {
        List<Student> studentList = new ArrayList<Student>();

        String selectQuery = "SELECT  * FROM "
                + TABLE_STUDENT
                + " WHERE EXISTS ("
                + " SELECT * FROM "+ TABLE_COURSE_STUDENT
                + " WHERE "+TABLE_COURSE_STUDENT+ "." + KEY_COURSE_ID+ " = '" + courseId + "'"
                + " AND "+ TABLE_STUDENT+"." + KEY_ID + " = " +TABLE_COURSE_STUDENT +"." + KEY_STUDENT_ID+")";


        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

       if (c.moveToFirst()) {
            do {
                Student student = new Student();
                student.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                student.setStudentCode(c.getInt(c.getColumnIndex(KEY_STUDENT_CODE)));
                student.setName(c.getString(c.getColumnIndex(KEY_NAME)));

                student.setPhotoList(getPhotosByStudentId(student.getId()));

                studentList.add(student);
            } while (c.moveToNext());
        }

        return studentList;

    }

    private List<Photo> getPhotosByStudentId(long studentId) {
        List<Photo> photoList = new ArrayList<Photo>();
        String selectQuery = "SELECT  * FROM "
                + TABLE_PHOTO
                + " WHERE " + KEY_STUDENT_ID + "="+ studentId;


        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

       if (c.moveToFirst()) {
            do {
                Photo photo = new Photo();
                photo.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                photo.setImage(c.getString(c.getColumnIndex(KEY_PHOTO_IMAGE)));
                photo.setStudentId(c.getInt(c.getColumnIndex(KEY_STUDENT_ID)));
                photo.setLessonId(c.getInt(c.getColumnIndex(KEY_LESSON_ID)));
                photoList.add(photo);
            } while (c.moveToNext());
        }

        return photoList;
    }

    private Photo getPhotosByStudentIdAndLessonId(long studentId, long lessonId) {

        String selectQuery = "SELECT  * FROM "
                + TABLE_PHOTO
                + " WHERE " + KEY_STUDENT_ID + "= '"+ studentId + "'"
                + " AND " + KEY_LESSON_ID + "= '" + lessonId+ "'";


        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            Photo photo = new Photo();
            photo.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            photo.setImage(c.getString(c.getColumnIndex(KEY_PHOTO_IMAGE)));
            photo.setStudentId(c.getInt(c.getColumnIndex(KEY_STUDENT_ID)));
            photo.setLessonId(c.getInt(c.getColumnIndex(KEY_LESSON_ID)));
            return photo;
        }
        return null;
    }

    public Course getCourseByCode(String code) {
        Course course = new Course();
        String selectQuery = "SELECT  * FROM "
                + TABLE_COURSE
                + " WHERE " + KEY_COURSE_CODE + "= '"+ code +"'";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            course.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            course.setCourseCode(c.getString(c.getColumnIndex(KEY_COURSE_CODE)));
            course.setName(c.getString(c.getColumnIndex(KEY_NAME)));
            course.setLessonList(getLessonByCourseId(course.getId()));
            course.setStudentList(getStudentsByCourse(course.getId()));

        }

        return course;
    }

    public List<Lesson> getLessonByCourseId(long courseId) {
        List<Lesson> lessonList = new ArrayList<Lesson>();
        String selectQuery = "SELECT  * FROM "
                + TABLE_LESSON
                + " WHERE " + KEY_COURSE_ID + "= '"+ courseId+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Lesson lesson = new Lesson();
                lesson.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                lesson.setDate(c.getString(c.getColumnIndex(KEY_DATE)));
                lesson.setCourseId(c.getInt(c.getColumnIndex(KEY_COURSE_ID)));
                lesson.setStudentList(getStudentsByLessonId(lesson.getId()));
                lessonList.add(lesson);
            } while (c.moveToNext());
        }

        return lessonList;
    }

    public List<Student> getStudentsByLessonId(long id) {
        List<Student> studentList = new ArrayList<Student>();

        String selectQuery = "SELECT  * FROM "
                + TABLE_STUDENT
                + " WHERE EXISTS ("
                + " SELECT * FROM "+ TABLE_LESSON_STUDENT
                + " WHERE "+TABLE_LESSON_STUDENT+ "." + KEY_LESSON_ID+ " = '" + id + "'"
                + " AND "+ TABLE_STUDENT+"." + KEY_ID + " = " +TABLE_LESSON_STUDENT +"." + KEY_STUDENT_ID+")";


        Log.d(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Student student = new Student();
                student.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                student.setStudentCode(c.getInt(c.getColumnIndex(KEY_STUDENT_CODE)));
                student.setName(c.getString(c.getColumnIndex(KEY_NAME)));

                student.setPhotoList(getPhotosByStudentId(student.getId()));

                studentList.add(student);
            } while (c.moveToNext());
        }

        return studentList;

    }

    public Student getStudentByCode(long studentCode) {
        Student student = new Student();
        String selectQuery = "SELECT  * FROM "
                + TABLE_STUDENT
                + " WHERE " + KEY_STUDENT_CODE + "= '"+ studentCode +"'";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            student.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            student.setStudentCode(c.getLong(c.getColumnIndex(KEY_STUDENT_CODE)));
            student.setName(c.getString(c.getColumnIndex(KEY_NAME)));
            student.setPhotoList(getPhotosByStudentId(student.getId()));
        }

        return student;
    }

    public List<Student> getAllStudents() {
        List<Student> studentList = new ArrayList<Student>();

        String selectQuery = "SELECT  * FROM "
                + TABLE_STUDENT;


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do{
                Student student = new Student();
                student.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                student.setStudentCode(c.getLong(c.getColumnIndex(KEY_STUDENT_CODE)));
                student.setName(c.getString(c.getColumnIndex(KEY_NAME)));
                student.setPhotoList(getPhotosByStudentId(student.getId()));
                studentList.add(student);
            } while (c.moveToNext());
        }

        return studentList;
    }

    public void deleteLesson(long lessonId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_LESSON, KEY_ID + "=" + lessonId, null);
        db.delete(TABLE_PHOTO, KEY_LESSON_ID +"="+ lessonId, null);

    }
}
