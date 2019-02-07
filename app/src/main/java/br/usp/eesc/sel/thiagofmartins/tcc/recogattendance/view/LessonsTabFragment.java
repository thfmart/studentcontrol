package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
import br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.database.model.Lesson;

public class LessonsTabFragment extends Fragment {
    private List<Lesson> myLessons = new ArrayList<Lesson>();
    View mView;
    Course course;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.tab_fragment1, container, false);
        mView = layout;

        Bundle args = getArguments();
        final String courseCode = args.getString("EXTRA_COURSE_CODE");
        course = DatabaseInteractor.getCourseByCode(getContext(), courseCode);

        FloatingActionButton fab = (FloatingActionButton) mView.findViewById(R.id.add_lesson);
        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChamadaActivity.class);

                intent.putExtra("courseID",course.getId());
                intent.putExtra("courseCode", courseCode);
                startActivity(intent);
            }
        });
        populateLessonsList();
        populateListLessonView();
        registerLessonClickCallback();


        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateLessonsList();
        populateListLessonView();
    }

    private void populateLessonsList() {
        myLessons.clear();
        myLessons = DatabaseInteractor.getLessonByCourseId(getActivity(), course.getId());
    }

    private void populateListLessonView() {
        ArrayAdapter<Lesson> adapter = new MyListLessonAdapter();
        ListView list = (ListView) mView.findViewById(R.id.itemLessonList);
        list.setAdapter(adapter);
    }

    private void registerLessonClickCallback() {
        ListView list = (ListView) mView.findViewById(R.id.itemLessonList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {

                Lesson clickedLesson = myLessons.get(position);
                //TODO abrir tela de chamada
            }
        });
    }

    private class MyListLessonAdapter extends ArrayAdapter<Lesson> {
        public MyListLessonAdapter() {
            super(getActivity(), R.layout.item_view, myLessons);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater(null).inflate(R.layout.item_view, parent, false);
            }

            Lesson currentLesson = myLessons.get(position);

            TextView nameText = (TextView) itemView.findViewById(R.id.item_name);
            nameText.setText(currentLesson.getDate());

            TextView infoText = (TextView) itemView.findViewById(R.id.item_txtInfo);
            infoText.setText(course.getName());

            return itemView;
        }
    }

}