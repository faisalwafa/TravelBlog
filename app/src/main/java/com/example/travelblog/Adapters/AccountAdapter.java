package com.example.travelblog.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.travelblog.R;
import com.example.travelblog.TravelBlog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    public List<TravelBlog> travelBlogs;
    public Context context;

    private FirebaseFirestore firebaseFirestore;

    private ImageView menuMore;

    public AccountAdapter(List<TravelBlog> travelBlogs){
        this.travelBlogs = travelBlogs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mypost_item,parent,false);

        context = parent.getContext();

        firebaseFirestore = FirebaseFirestore.getInstance();


        menuMore = view.findViewById(R.id.menu_more);

        return new AccountAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        String desc_data = travelBlogs.get(position).getDesc();
        holder.setDescText(desc_data);

        String image_url = travelBlogs.get(position).getImage_url();
        System.out.println(image_url);
        holder.setPostImage(image_url);

        String user_id = travelBlogs.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("name");
                    System.out.println(userName);
                    String userImage = task.getResult().getString("image");

                    holder.setUserData(userName, userImage);
                }else {

                }
            }
        });

        long millisecond = travelBlogs.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy",new Date(millisecond)).toString();
        holder.setDate(dateString);

        menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context,menuMore);
                popupMenu.getMenuInflater().inflate(R.menu.menu_more,popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.delete_post:
                                DocumentReference documentReference = firebaseFirestore.collection("Posts").document(travelBlogs.get(position).getPost_id());
                                documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            travelBlogs.remove(position);
                                            notifyDataSetChanged();
                                            Toast.makeText(context, "Posts deleted", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(context, "Error : "+ error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return travelBlogs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView descView;
        private ImageView postImageView;
        private TextView postDate;

        private TextView postUserName;
        private CircleImageView postUserImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDescText(String text){
            descView = mView.findViewById(R.id.account_mydesc);
            descView.setText(text);
        }

        public void setPostImage(String downloadUri) {
            postImageView = mView.findViewById(R.id.account_myimage);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_launcher_background);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).into(postImageView);
        }

        public void setDate(String date) {
            postDate = mView.findViewById(R.id.account_mydate);
            postDate.setText(date);
        }

        public void setUserData(String name, String image){
            postUserName = mView.findViewById(R.id.account_myname);
            postUserImage = mView.findViewById(R.id.account_myphoto);

            postUserName.setText(name);

            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.ic_account_circle_black_24dp);

            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(postUserImage);
        }
    }
}
