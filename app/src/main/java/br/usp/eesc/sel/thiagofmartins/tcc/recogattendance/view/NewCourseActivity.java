package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseUtil;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Course;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;

public class NewCourseActivity extends AppCompatActivity {

    private List<Student> allStudentsList = new ArrayList<Student>();
    public Toolbar toolbar;
    private CheckBox chkIos;
    private TextInputLayout inputLayoutName;
    private List<Student> selectedStudentsList = new ArrayList<Student>();
    String professorEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_course);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("Nova Disciplina");

        professorEmail = getIntent().getStringExtra("EXTRA_PROFESSOR_EMAIL");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        //chkIos = (CheckBox)findViewById(R.id.checkBox);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.new_course_btn_new_student);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(NewCourseActivity.this, CadastroActivity.class);
                startActivity(i);
            }
        });

        populateStudentList();
        populateListView();
        registerClickCallback();

    }

    private void populateStudentList() {
        //TODO pegar todos os alunos do banco
        allStudentsList.clear();
        allStudentsList.addAll(DatabaseInteractor.getAllStudents(NewCourseActivity.this));
        /*for(int i=1;i<10;i++){
            Photo photo = new Photo();
            photo.setImage(DatabaseUtil.bitmapToString(BitmapFactory.decodeResource(getResources(), R.drawable.a6)));
            List<Photo> list = new ArrayList<>();
            list.add(photo);
            Student student = new Student();
            student.setName("Barack Obama");
            student.setStudentCode(123+i);
            student.setPhotoList(list);
            allStudentsList.add(student);
        }*/
    }

    private void populateListView() {
        if(allStudentsList != null && !allStudentsList.isEmpty()){
            ArrayAdapter<Student> adapter = new MyListAdapter();
            ListView list = (ListView) findViewById(R.id.people_checkListView2);
            list.setAdapter(adapter);
        }
    }

    private void registerClickCallback() {


        /*chkIos = (CheckBox) findViewById(R.id.checkBox);
        chkIos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/



        ListView list = (ListView) findViewById(R.id.people_checkListView2);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {
                chkIos = (CheckBox) viewClicked.findViewById(R.id.checkBox);
                Student clickedStudent = allStudentsList.get(position);
                if (chkIos.isChecked()) {
                    chkIos.setChecked(false);
                    selectedStudentsList.remove(clickedStudent);
                } else {
                    chkIos.setChecked(true);
                    selectedStudentsList.add(clickedStudent);
                }
            }
        });
    }


    private class MyListAdapter extends ArrayAdapter<Student> {
        public MyListAdapter() {
            super(NewCourseActivity.this, R.layout.people_check_view, allStudentsList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.people_check_view, parent, false);
            }

            Student currentStudent = allStudentsList.get(position);
            itemView.setTag(currentStudent);

            itemView.findViewById(R.id.people_check_view_background).setBackgroundColor(getResources().getColor(R.color.white));

            ImageView imageView = (ImageView)itemView.findViewById(R.id.people_icon);
            Bitmap a = DatabaseUtil.stringToBitmap(currentStudent.getPhotoList().get(0).getImage());
            imageView.setImageBitmap(a);

            TextView nameText = (TextView) itemView.findViewById(R.id.people_name);
            nameText.setText(currentStudent.getName());

            TextView idText = (TextView) itemView.findViewById(R.id.people_txtID);
            idText.setText("" + currentStudent.getStudentCode());

            return itemView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.done_newcourse) {
            if(saveCourse()) {
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Boolean saveCourse(){

        AutoCompleteTextView textDisciplina = (AutoCompleteTextView) findViewById(R.id.Course);
        EditText textCodigo = (EditText) findViewById(R.id.sigla);

        if(textCodigo.length()>1 && textDisciplina.length()>1){

            Course course = new Course();
            course.setName(textDisciplina.getText().toString());
            course.setCourseCode(textCodigo.getText().toString());
            course.setStudentList(selectedStudentsList);
            DatabaseInteractor.saveCourse(this, course);
            DatabaseInteractor.addCourseToProfessor(this, DatabaseInteractor.getCourseByCode(this, textCodigo.getText().toString()).getId(), DatabaseInteractor.getProfessorByEmail(this, professorEmail).getId());
            return true;
        } else {
            Toast.makeText(this, "Os campos Nome e Sigla devem ser preenchidos", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        populateStudentList();
        populateListView();
    }
}
