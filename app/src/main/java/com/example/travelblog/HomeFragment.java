package com.example.travelblog;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView homeView;
    private List<TravelBlog> travelBlogs;

    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter homeAdapter;

    private FirebaseAuth firebaseAuth;

    SharedPref pref;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        pref = new SharedPref(getContext());
        if(pref.loadNightMode() == true){
            getActivity().setTheme(R.style.DarkTheme);
        }else{
            getActivity().setTheme(R.style.AppTheme);
        }

        View view = inflater.inflate(R.layout.fragment_home,container,false);

        travelBlogs = new ArrayList<>();
        homeView = view.findViewById(R.id.home_view);

        firebaseAuth = FirebaseAuth.getInstance();

        homeAdapter = new HomeAdapter(travelBlogs);
        homeView.setLayoutManager(new LinearLayoutManager(getActivity()));
        homeView.setAdapter(homeAdapter);

        if(firebaseAuth.getCurrentUser() != null){
            firebaseFirestore = FirebaseFirestore.getInstance();

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING);

            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    for (DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                            TravelBlog travelBlog = documentChange.getDocument().toObject(TravelBlog.class);
                            travelBlogs.add(travelBlog);

                            homeAdapter.notifyDataSetChanged();

                        }
                    }
                }
            });
        }
        // Inflate the layout for this fragment
        return view;
    }

}
