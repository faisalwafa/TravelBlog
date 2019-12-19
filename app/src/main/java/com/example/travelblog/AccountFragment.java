package com.example.travelblog;


import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private RecyclerView accountView;
    private List<TravelBlog> travelBlogs;

    private FirebaseFirestore firebaseFirestore;
    private AccountAdapter accountAdapter;

    private Uri mainImageURI = null;
    private String user_id;
    private FirebaseAuth firebaseAuth;

    private TextView userName;
    private CircleImageView userImage;

    SharedPref pref;


    public AccountFragment() {
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

        View view = inflater.inflate(R.layout.fragment_account,container,false);

        travelBlogs = new ArrayList<>();
        accountView = view.findViewById(R.id.account_view);

        userName = view.findViewById(R.id.account_username);
        userImage = view.findViewById(R.id.account_photo);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageURI = Uri.parse(image);

                        userName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.ic_launcher_background);

                        Glide.with(AccountFragment.this).setDefaultRequestOptions(placeholderRequest).load(image).into(userImage);
                    }else {
                        Toast.makeText(getActivity(), "Data doesn't exists", Toast.LENGTH_SHORT).show();

                    }
                }else {
                    String error = task.getException().getMessage();
                    Toast.makeText(getActivity(), "Firestore Retrive Error"+error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        accountAdapter = new AccountAdapter(travelBlogs);
        accountView.setLayoutManager(new LinearLayoutManager(getActivity()));
        accountView.setAdapter(accountAdapter);

        if(firebaseAuth.getCurrentUser() != null){
            firebaseFirestore = FirebaseFirestore.getInstance();

            Query firstQuery = firebaseFirestore.collection("Posts").whereEqualTo("user_id",user_id).orderBy("timestamp",Query.Direction.DESCENDING);

            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                TravelBlog travelBlog = documentChange.getDocument().toObject(TravelBlog.class);
                                travelBlogs.add(travelBlog);

                                accountAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                }
            });
        }

        // Inflate the layout for this fragment
        return view;
    }

}
