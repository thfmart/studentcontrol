package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Course;

public class CourseListActivity extends AppCompatActivity {

    private List<Course> myCourses = new ArrayList<Course>();
    public Toolbar toolbar;
    String professorEmail;
    ArrayAdapter<Course> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Matérias");

        professorEmail = getIntent().getStringExtra("EXTRA_PROFESSOR_EMAIL");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_course);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CourseListActivity.this, NewCourseActivity.class);
                i.putExtra("EXTRA_PROFESSOR_EMAIL", professorEmail);
                startActivity(i);
            }
        });

        populateCourseList();
        populateListView();
        registerClickCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateCourseList();
        populateListView();
    }

    private void populateCourseList() {
        myCourses = DatabaseInteractor.getProfessorByEmail(this, professorEmail).getCourseList();
    }

    private void populateListView() {
        if(myCourses!=null && !myCourses.isEmpty()) {
            adapter = new MyListAdapter();
            ListView list = (ListView) findViewById(R.id.itemCourseList);
            list.setAdapter(adapter);
        }
    }

    private void registerClickCallback() {
        ListView list = (ListView) findViewById(R.id.itemCourseList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                Course clickedCourse = myCourses.get(position);

                Intent intent = new Intent(CourseListActivity.this, MainTabbedActivity.class);
                intent.putExtra("EXTRA_COURSE_CODE", clickedCourse.getCourseCode());
                startActivity(intent);
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Course> {
        public MyListAdapter() {
            super(CourseListActivity.this, R.layout.item_view, myCourses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            Course current = myCourses.get(position);
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
                itemView.setTag(current);
            }

            itemView.findViewById(R.id.item_view_background).setBackgroundColor(getResources().getColor(R.color.white));

            TextView nameText = (TextView) itemView.findViewById(R.id.item_name);
            nameText.setText(current.getName());

            TextView idText = (TextView) itemView.findViewById(R.id.item_txtInfo);
            idText.setText("" + current.getCourseCode());

            return itemView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}












