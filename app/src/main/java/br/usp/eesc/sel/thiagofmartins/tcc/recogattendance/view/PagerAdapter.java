package br.usp.eesc.sel.thiagofmartins.tcc.recogattendance.view;

/**
 * Created by Thiago on 4/14/2016.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    String mCourseCode;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, String courseCode) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.mCourseCode = courseCode;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                LessonsTabFragment tab1 = new LessonsTabFragment();
                Bundle args = new Bundle();
                args.putString("EXTRA_COURSE_CODE", mCourseCode);
                tab1.setArguments(args);
                return tab1;
            case 1:
                StudentsTabFragment tab2 = new StudentsTabFragment();
                Bundle argsTab2 = new Bundle();
                argsTab2.putString("EXTRA_COURSE_CODE", mCourseCode);
                tab2.setArguments(argsTab2);
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}