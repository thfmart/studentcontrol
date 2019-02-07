package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseUtil;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Course;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.recognition.ProcessaImagem;

/**
 * Demonstrate how to populate a complex ListView with icon and text.
 * Icon images taken from icon pack by Everaldo Coelho (http://www.everaldo.com)
 */
public class SelectPeopleActivity extends AppCompatActivity {

    private List<Student> myStudents = new ArrayList<Student>();
    public Toolbar toolbar;
    Course mCourse;
    Student clickedStudent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_people);
        toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Selecionar Aluno");

        //intent.putExtra("obj", addr);
        final String courseCode = getIntent().getStringExtra("courseCode");
        mCourse = DatabaseInteractor.getCourseByCode(SelectPeopleActivity.this, courseCode);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });



        populateStudentList();
        populateListView();
        registerClickCallback();
    }


    public Bitmap readBitmap(int i, String flag) {
        Bitmap a = null;
        File f = Environment.getExternalStorageDirectory();
        File image = new File(f+"/EESC-Face/temp",Integer.toString(i)+flag+".jpg");
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        a = BitmapFactory.decodeFile(image.getAbsolutePath(), options);
        boolean deleted =image.delete();


        return a;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap temp = readBitmap(0,"A");

        Intent intent = new Intent();

        intent.putExtra("face", DatabaseUtil.bitmapToString(temp));//// TODO: 5/14/2016 passar foto por string e não como bitmap


        intent.putExtra("studentCode",clickedStudent.getStudentCode());
        setResult(RESULT_OK, intent);
        finish();

    }



    private void populateStudentList() {
        myStudents = mCourse.getStudentList();
        myStudents.clear();
        myStudents.addAll(DatabaseInteractor.getAllStudents(SelectPeopleActivity.this));
    }

    private void populateListView() {
        ArrayAdapter<Student> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.peopleSelectList);
        list.setAdapter(adapter);
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.peopleSelectList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                clickedStudent = myStudents.get(position);
                Intent i = new Intent(SelectPeopleActivity.this, TakePictureActivity.class);//MediaStore.ACTION_IMAGE_CAPTURE);
                i.putExtra("flag_qdt_foto", 1);
                startActivityForResult(i, 1);
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Student> {
        public MyListAdapter() {
            super(SelectPeopleActivity.this, R.layout.people_view, myStudents);
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

            ImageView imageView = (ImageView)itemView.findViewById(R.id.people_icon);
            Bitmap a = DatabaseUtil.stringToBitmap(currentStudent.getPhotoList().get(0).getImage());//TODO: PEGAR ULTIMA FOTO ADICIONADA
            imageView.setImageBitmap(a);

            TextView nameText = (TextView) itemView.findViewById(R.id.people_name);
            nameText.setText(currentStudent.getName());

            // Year:
            TextView idText = (TextView) itemView.findViewById(R.id.people_txtID);
            idText.setText("" + currentStudent.getId());

            return itemView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_people, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}












