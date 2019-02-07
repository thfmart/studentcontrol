package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

/**
 * Created by Thiago on 4/14/2016.
 */

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseInteractor;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.helper.DatabaseUtil;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Course;
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Student;

public class StudentsTabFragment extends Fragment {
    private List<Student> myStudents = new ArrayList<Student>();
    View mView;
    Course mCourse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.tab_fragment2, container, false);
        mView = layout;

        Bundle args = getArguments();
        String courseCode = args.getString("EXTRA_COURSE_CODE");
        mCourse = DatabaseInteractor.getCourseByCode(getContext(), courseCode);

        /*FloatingActionButton fab = (FloatingActionButton) mView.findViewById(R.id.new_course_btn_new_student);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                //TODO abrir tela de cadastro de pessoa
            }
        });*/

        populateList();
        populateListView();
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateList();
        populateListView();
    }

    private void populateList() {
        myStudents = mCourse.getStudentList();
    }

    private void populateListView() {
        ArrayAdapter<Student> adapter = new MyListPeopleAdapter();
        ListView list = (ListView) mView.findViewById(R.id.itemPeopleList);
        list.setAdapter(adapter);
    }

    private class MyListPeopleAdapter extends ArrayAdapter<Student> {
        public MyListPeopleAdapter() {
            super(getActivity(), R.layout.people_view, myStudents);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater(null).inflate(R.layout.people_view, parent, false);
            }
            Student currentStudent = myStudents.get(position);
            itemView.setTag(currentStudent);

            ImageView imageView = (ImageView)itemView.findViewById(R.id.people_icon);
            Bitmap a = DatabaseUtil.stringToBitmap(currentStudent.getPhotoList().get(0).getImage());
            //Bitmap a = DatabaseUtil.stringToBitmap(currentStudent.getPhotoList().get(currentStudent.getPhotoList().size()-1).getImage());
            imageView.setImageBitmap(a);

            TextView nameText = (TextView) itemView.findViewById(R.id.people_name);
            nameText.setText(currentStudent.getName());

            TextView idText = (TextView) itemView.findViewById(R.id.people_txtID);
            idText.setText(Long.toString(currentStudent.getStudentCode()));

            return itemView;
        }
    }


}