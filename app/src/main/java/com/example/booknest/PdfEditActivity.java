package com.example.booknest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.example.booknest.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
//view binding
    private ActivityPdfAddBinding binding;
   //book id get from intent started from AdpterPdfAdmin
    private String bookId;
    //progress dialog
    private ProgressDialog progressDialog;


    private ArrayList<String> categoryTitleArraylist, CategoryIdArraylist;
    private static final String TAG = "BOOK_Edit_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bookId = getIntent().getStringExtra("bookId");
    //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCateories();
        loadBookInfo();


        //handle click pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });

    //handle click go to previous screen
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //handle click begin upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();

            }
        });
    }

    private void loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info");
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book info
                        selectedCategoryId = ""+snapshot.child("CategoryId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        // set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);
                        Log.d(TAG, "onDataChange: Loading Book Category Info ");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("category").getValue();
                                        //set to  category text view
                                        binding.categoryTv.setText(category);

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private String title="", description ="";
    private void validateData(){
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description ...", Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Click Category", Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }


    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf:Starting updating pdf info to db... ");
        //show progress
        progressDialog.setMessage("Updating book info ...");
        progressDialog.show();

        //setup data to update to do
        HashMap<String, Object> hashMap = new HashMap<>();
        HashMap.put("title", ""+title);
        HashMap.put("description", ""+description);
        HashMap.put("categoryId", ""+selectedCategoryId);
    //start updating
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "onSuccess:  Book updated....");
                Toast.makeText(PdfEditActivity.this, "Book info updated...", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure:failed to update due to "+e.getMessage());
                        Toast.makeText(PdfEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })

    }

    private String selectedCategoryId="", getSelectedCategoryTitle=""

    private void categoryDialog(){
        //make string arrat from arraylist of string
        String[] categoriesArray = new String[categoryTitleArraylist.size()];
        for(int i=0; i<categoryTitleArraylist.size(); i++){
            categoriesArray[i] = categoryTitleArraylist.get(i);
        }
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    selectedCategoryId= CategoryIdArraylist.get(which);
                    getSelectedCategoryTitle= categoryTitleArraylist.get(which);
                    //set to textview
                        binding.categoryTv.setText(getSelectedCategoryTitle);
                    }
                })
                .show();


    }
    private void loadCateories() {
        Log.d(TAG, "loadCateories: loading categories...");
        CategoryIdArraylist = new ArrayList<>();
        categoryTitleArraylist = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                CategoryIdArraylist.clear();
                categoryTitleArraylist.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    CategoryIdArraylist.add(id);
                    categoryTitleArraylist.add(category);
                    Log.d(TAG, "onDataChange: ID: "+id);
                    Log.d(TAG, "onDataChange: Category:"+category );
                            
                    
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}