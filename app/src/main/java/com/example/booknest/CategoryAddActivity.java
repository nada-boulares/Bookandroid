package com.example.booknest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.booknest.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {
//view binding
    private ActivityCategoryAddBinding binding;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //progress bar
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //init firebase auth
        firebaseAuth=FirebaseAuth.getInstance();
        // Retrieve and initially hide the ProgressBar
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.GONE);
        // Set click listener for the back button to navigate to the previous screen
        binding.backBtn.setOnClickListener(v -> onBackPressed());
        //hadle click,begin upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    private String category="";
    private void validateData(){
        /*validate data*/
        //get data
        category=binding.categoryEt.getText().toString().trim();
        //validate if not empty
        if (TextUtils.isEmpty(category)){
            Toast.makeText(this,"Please enter category...!",Toast.LENGTH_SHORT).show();
        }else {
            addCategoryFirebase();
        }
    }
    private void addCategoryFirebase(){
        //show progress
        progressBar.setVisibility(View.VISIBLE);
        //get timespam
        long timespam = System.currentTimeMillis();
        //set up info to add in firebase db
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("id",""+timespam);
        hashMap.put("category",""+category);
        hashMap.put("timestamp",timespam);
        hashMap.put("uid",""+firebaseAuth.getUid());
        //add to firebase
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timespam)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //category add success
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryAddActivity.this,"Category added successfully...",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //category add failed
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryAddActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}