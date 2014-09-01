package com.harbinpointech.carcenter.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.harbinpointech.carcenter.R;
import com.harbinpointech.carcenter.data.WebHelper;
import com.harbinpointech.carcenter.util.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleInfoActivity extends ActionBarActivity {
    public static final String EXTRA_CARNAME = "EXTRA_CARNAME";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                RadioButton vehicle_info_base_info_radio = (RadioButton) findViewById(R.id.vehicle_info_base_info_radio);
                vehicle_info_base_info_radio.setChecked(i == 0);

                RadioButton vehicle_info_plugins_info_radio = (RadioButton) findViewById(R.id.vehicle_info_plugins_info_radio);
                vehicle_info_plugins_info_radio.setChecked(i != 0);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        RadioButton vehicle_info_base_info_radio = (RadioButton) findViewById(R.id.vehicle_info_base_info_radio);
        vehicle_info_base_info_radio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mViewPager.setCurrentItem(0, true);
                } else {
                    mViewPager.setCurrentItem(1, true);
                }
            }
        });


        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText("未查询到任何信息");
            new AsyncTask<Void, Integer, Integer>() {
                JSONObject mContent = null;

                @Override
                protected Integer doInBackground(Void... params) {
                    JSONObject[] param = new JSONObject[1];
                    try {
                        int result = 0;
                        if (getArguments().getInt(ARG_SECTION_NUMBER) == 0) {
                            result = WebHelper.getCarBaseInfos(param, getActivity().getIntent().getStringExtra(EXTRA_CARNAME), false);
                        } else {
                            result = WebHelper.getCarPluginInfos(param, getActivity().getIntent().getStringExtra(EXTRA_CARNAME));
                        }
                        if (result == 0) {
                            mContent = param[0];
                            return result;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return -1;
                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Integer result) {
                    super.onPostExecute(result);
                    if (getArguments().getInt(ARG_SECTION_NUMBER) == 0) {
                        List<Pair<String, String>> items = new ArrayList<Pair<String, String>>();
                        try {
                            if (mContent != null) {
                                mContent = mContent.getJSONObject("d");
                                items.add(new Pair<String, String>("车辆型号：", mContent.getString("CarModel")));
                                items.add(new Pair<String, String>("车牌号：", mContent.getString("CarName")));
                                items.add(new Pair<String, String>("车辆管理员：", mContent.getString("Manager")));
                                items.add(new Pair<String, String>("车管员电话：", mContent.getString("ManagerPhone")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ArrayAdapter<Pair<String, String>> adapter = new ArrayAdapter<Pair<String, String>>(getActivity(), R.layout.vehicle_basic_info_item, items) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                if (convertView == null) {
                                    convertView = getLayoutInflater(null).inflate(R.layout.vehicle_basic_info_item, parent, false);
                                }
                                TextView tvName = (TextView) convertView.findViewById(R.id.vehicle_info_item_name);
                                TextView tvValue = (TextView) convertView.findViewById(R.id.vehicle_info_item_value);
                                Pair<String, String> p = getItem(position);
                                tvName.setText(p.first);
                                tvValue.setText(p.second);
                                return convertView;
                            }
                        };
                        setListAdapter(adapter);
                    } else {
                        List<Pair<String, String>> items = new ArrayList<Pair<String, String>>();
                        try {
                            if (mContent != null) {
                                mContent = mContent.getJSONObject("d");
                                items.add(new Pair<String, String>("车辆型号：", mContent.getString("CarModel")));
                                items.add(new Pair<String, String>("车牌号：", mContent.getString("CarName")));
                                items.add(new Pair<String, String>("车辆管理员：", mContent.getString("Manager")));
                                items.add(new Pair<String, String>("车管员电话：", mContent.getString("ManagerPhone")));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ArrayAdapter<Pair<String, String>> adapter = new ArrayAdapter<Pair<String, String>>(getActivity(), R.layout.vehicle_basic_info_item, items) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                if (convertView == null) {
                                    convertView = getLayoutInflater(null).inflate(R.layout.vehicle_basic_info_item, parent, false);
                                }
                                TextView tvName = (TextView) convertView.findViewById(R.id.vehicle_info_item_name);
                                TextView tvValue = (TextView) convertView.findViewById(R.id.vehicle_info_item_value);
                                Pair<String, String> p = getItem(position);
                                tvName.setText(p.first);
                                tvValue.setText(p.second);
                                return convertView;
                            }
                        };
                        setListAdapter(adapter);
                    }

                }
            }.execute();
        }
    }

}
