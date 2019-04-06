package com.ticsell.Fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.ticsell.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    View mView;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    public HomeFragment() {
        // Required empty public constructor
    }

    String location;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_home, container, false);
        //code

        MaterialSpinner spinner = (MaterialSpinner)mView. findViewById(R.id.spinner);
        spinner.setItems("chennai", "trichy", "coimbatore");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                location=item;
            }
        });



        return mView;
    }

}
