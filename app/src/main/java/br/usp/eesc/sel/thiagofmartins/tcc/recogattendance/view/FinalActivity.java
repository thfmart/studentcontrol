package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseHelper;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseUtil;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Course;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Lesson;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;

/**
 * Demonstrate how to populate a complex ListView with icon and text.
 * Icon images taken from icon pack by Everaldo Coelho (http://www.everaldo.com)
 */
public class FinalActivity extends AppCompatActivity {

    private List<Student> myStudents = new ArrayList<Student>();
    private List<Student> myAttendants = new ArrayList<Student>();
    private List<Long> myAttendantsId = new ArrayList<Long>();
    private List<Lesson> myLessons = new ArrayList<Lesson>();
    private List<Student> myLessonAttendants = new ArrayList<Student>();
    public Toolbar toolbar;
    Course mCourse;
    Lesson mLesson;
    long lessonID;
    long courseID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);
        toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Salvar Chamada");
        lessonID = getIntent().getLongExtra("LessonID",0);//// TODO: 5/15/2016 check valor minimo
        final String courseCode = getIntent().getStringExtra("courseCode");
        courseID = getIntent().getLongExtra("courseId", 0);//// TODO: 5/15/2016 check valor minimo

        mCourse = DatabaseInteractor.getCourseByCode(FinalActivity.this, courseCode);
        myLessons = mCourse.getLessonList();


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*DatabaseInteractor.deleteLesson(FinalActivity.this, lessonID);
                Intent i = new Intent(FinalActivity.this, MainTabbedActivity.class);
                startActivity(i);//do something you want*/
            }
        });

        populateStudentList();
        populateListView();
        registerClickCallback();
    }





    private void populateStudentList() {
        myStudents = mCourse.getStudentList();
        myAttendants=DatabaseInteractor.getStudentsByLessonId(FinalActivity.this,lessonID);
        Student temp;
        for (int i = 0;i<myAttendants.size();i++)
        {
            temp = myAttendants.get(i);
            myAttendantsId.add(temp.getId());
        }
    }

    private void populateListView() {
        ArrayAdapter<Student> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.peopleFinalList);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.peopleFinalList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                Student clickedStudent = myStudents.get(position);
                /*String message = "You clicked position " + position
                        + " Which is car make " + clickedStudent.getName();
                Toast.makeText(FinalActivity.this, message, Toast.LENGTH_LONG).show();*/
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Student> {
        public MyListAdapter() {
            super(FinalActivity.this, R.layout.people_view, myStudents);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.people_view, parent, false);
            }
            // Find the car to work with.
            Student currentStudent = myStudents.get(position);
            itemView.setTag(currentStudent);

            if (myAttendantsId.contains(currentStudent.getId()))
            {itemView.findViewById(R.id.people_view_background).setBackgroundColor(getResources().getColor(R.color.green));}
            else
            {itemView.findViewById(R.id.people_view_background).setBackgroundColor(getResources().getColor(R.color.red));}


            // Fill the view
            ImageView imageView = (ImageView)itemView.findViewById(R.id.people_icon);
            Bitmap a = DatabaseUtil.stringToBitmap(currentStudent.getPhotoList().get(0).getImage());
            imageView.setImageBitmap(a);

            // Make:
            TextView nameText = (TextView) itemView.findViewById(R.id.people_name);
            nameText.setText(currentStudent.getName());

            // Year:
            TextView idText = (TextView) itemView.findViewById(R.id.people_txtID);
            idText.setText("" + currentStudent.getId());

            // Condition:
            //TextView infoText = (TextView) itemView.findViewById(R.id.people_txtInfo);
            //infoText.setText("");

            return itemView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_final, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id==R.id.action_saveChamada) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            List<Lesson> lessonList = new ArrayList<Lesson>();
            lessonList = DatabaseInteractor.getLessonByCourseId(FinalActivity.this, courseID);
            Lesson lastLesson = lessonList.get(lessonList.size()-1);
            Integer i;
            Integer j;

            String students = "";
            String code = "";
            String temp;
            String codetemp;
            for (i=0;i<myAttendants.size();i++)
            {
                temp = myAttendants.get(i).getName()+"\t\t\t"+Long.toString(myAttendants.get(i).getStudentCode());
                students = students+temp+"\n";

            }


            lastLesson.getDate();
            String body = mCourse.getName()+"\n"+mCourse.getCourseCode()+"\n\n"+
                    lastLesson.getDate()+":\n\n\n"+
                    students;
           // DatabaseInteractor.getProfessorByEmail()
                    String subject = "Lista de Presença - "+mCourse.getName()+" - "+lastLesson.getDate();
            intent.putExtra(Intent.EXTRA_EMAIL, "");
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, body);


            startActivity(Intent.createChooser(intent, "Send Email"));
            finish();
        }

            return super.onOptionsItemSelected(item);


    }

    public void sendEmail()
    {



    }
}












