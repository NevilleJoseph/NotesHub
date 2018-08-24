package com.pnpj.noteshub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class FirstSignInActivity extends AppCompatActivity {

    String userName, uid;
    Uri imageUri = null;
    String path;
    boolean isUploader = false;
    ArrayList<String> college = new ArrayList<>();

    private static final int RC_PHOTO_PICKER =  2;

    private StorageReference storageReference;
    private DatabaseReference databaseReference,collegeNameRef,branchNameRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    Spinner autoCompleteTextView;
    Spinner spinner1,spinner2;
    EditText editText2;
    ArrayAdapter adapterBranches;

    private Context mContext;
    private Activity mActivity;

    private LinearLayout mRelativeLayout;
    private Button mButton;

    private PopupWindow mPopupWindow;

    private RelativeLayout collegeNameAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userName = user.getDisplayName();
        uid = firebaseAuth.getUid();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("user_collegeId").child(uid);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("users");
        EditText editText = findViewById(R.id.firstSignIn_nameEditText);
        autoCompleteTextView = findViewById(R.id.firstSignIn_collegeNameAutoComplete);
        spinner1 = findViewById(R.id.firstSignIn_collegeBranchEditText);
        editText2 = findViewById(R.id.firstSignIn_collegeRollEditText);
        spinner2 = findViewById(R.id.semesterSpinner);
        editText.setText(userName);

        collegeNameAdd = findViewById(R.id.collegeNameAdd);

        final LinearLayout linearLayout = findViewById(R.id.collegeIdPhotoLinearLayout);
        final TextView textView = findViewById(R.id.rollNoPrompt);

        final RelativeLayout relativeLayout = findViewById(R.id.addBranchNameRelLayout);

        branchNameRef = FirebaseDatabase.getInstance().getReference().child("branches");
        final ArrayList<String> branches = new ArrayList<>();
        adapterBranches = new ArrayAdapter(FirstSignInActivity.this,android.R.layout.simple_list_item_1,branches);
        branchNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot getBranch : dataSnapshot.getChildren())
                {
                    branches.add(getBranch.getKey());
                }
                branches.add("+ Add Your Branch");
                spinner1.setAdapter(adapterBranches);
                SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
                boolean check = sharedPreferences.getBoolean(uid,false);
                if(check)
                {
                    String branch = sharedPreferences.getString(uid+"branch","");
                    int pos = adapterBranches.getPosition(branch);
                    spinner1.setSelection(pos);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final ArrayAdapter collegeAdapter = new ArrayAdapter(FirstSignInActivity.this,android.R.layout.simple_list_item_1,college);

        collegeNameRef = FirebaseDatabase.getInstance().getReference().child("colleges");
        collegeNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot getCollege : dataSnapshot.getChildren())
                {
                    college.add(getCollege.getKey());
                }
                college.add("+ Add Your College");
                autoCompleteTextView.setAdapter(collegeAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if((adapterView.getItemAtPosition(i).toString()).equals("+ Add Your Branch"))
                {
                    relativeLayout.setVisibility(View.VISIBLE);
                }
                else
                {
                    relativeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        autoCompleteTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if((adapterView.getItemAtPosition(i).toString()).equals("+ Add Your College"))
                {
                    collegeNameAdd.setVisibility(View.VISIBLE);
                }
                else
                {
                    collegeNameAdd.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final CheckBox checkBox = findViewById(R.id.uploaderCheckBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    isUploader = true;
                    linearLayout.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    Button selectPhoto = findViewById(R.id.collegeIdPhotoButton);
                    selectPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ImagePicker.create(FirstSignInActivity.this)
                                    .single()
                                    .start(RC_PHOTO_PICKER);
                        }
                    });
                }
                else
                {
                    isUploader = false;
                    linearLayout.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                }
            }
        });

        Button submitBtn = findViewById(R.id.submitButton);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDetails();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        boolean checkUser = sharedPreferences.getBoolean(uid,false);
        if(checkUser)
        {
            editText2.setText(sharedPreferences.getString(uid+"roll",""));
            String sempos = sharedPreferences.getString(uid+"sem","").substring(0,1);
            int position = Integer.parseInt(sempos);
            spinner2.setSelection(position-1);
        }
    }

    public void submitDetails()
    {
        final String rollNo, branch, semester;
        EditText editText = findViewById(R.id.firstSignIn_nameEditText);
        EditText editText5 = findViewById(R.id.firstSignIn_collegeNameAddEditText);
        String userNameTyped = editText.getText().toString();
        String branch5;
        if(!(userNameTyped.equals(userName)))
        {
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(userNameTyped).build());
        }
        Spinner spinner5 = findViewById(R.id.firstSignIn_collegeNameAutoComplete);
        if((spinner5.getSelectedItem().toString()).equals("+ Add Your College"))
        {
            branch5 = editText5.getText().toString();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("colleges");
            databaseReference.child(branch5).setValue("true");
        }
        else
        {
            branch5 = spinner5.getSelectedItem().toString();
        }
        EditText editText2 = findViewById(R.id.firstSignIn_collegeRollEditText);
        rollNo = editText2.getText().toString();
        Spinner spinner1 = findViewById(R.id.firstSignIn_collegeBranchEditText);
        EditText editText3 = findViewById(R.id.firstSignIn_branchNameAddEditText);
        if((spinner1.getSelectedItem().toString()).equals("+ Add Your Branch"))
        {
            branch = editText3.getText().toString();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("branches");
            databaseReference.child(branch).setValue("true");
        }
        else
        {
            branch = spinner1.getSelectedItem().toString();
        }
        Spinner spinner = findViewById(R.id.semesterSpinner);
        semester = spinner.getSelectedItem().toString();
        if(branch5.equals("") || rollNo.equals("") || branch.equals("") || semester.equals(""))
        {
            Toast.makeText(FirstSignInActivity.this,"Enter All The Details",Toast.LENGTH_SHORT).show();
            return;
        }
        if(isUploader)
        {
            if(imageUri == null)
            {
                Toast.makeText(FirstSignInActivity.this,"Select An Image",Toast.LENGTH_SHORT).show();
                return;
            }
            StorageReference photoRef = storageReference.child("CollegeId");
            byte[] data = ImageUtils.compressImage(path);
            photoRef.putBytes(data);
        }
        userclass userDetails = new userclass(branch5, rollNo, branch, semester, isUploader,false);
        databaseReference.child(uid).setValue(userDetails);

        SharedPreferences preferences = getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(uid,true);
        editor.putString(uid+"branch",branch);
        editor.putString(uid+"sem",semester);
        editor.putBoolean(uid+"uploader",isUploader);
        editor.putString(uid+"college",branch5);
        editor.putString(uid+"roll",rollNo);
        editor.putBoolean(uid+"isVerifiedUploader",false);
        editor.commit();

        Intent intent = new Intent(FirstSignInActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK)
        {
            ArrayList<Image> images = (ArrayList<Image>) ImagePicker.getImages(data);
            for(Image image : images)
            {
                path = image.getPath();
                imageUri = Uri.fromFile(new File(image.getPath()));
                ImageView imageView = findViewById(R.id.collegeIdImageView);
                imageView.setImageBitmap(BitmapFactory.decodeFile(image.getPath()));
                imageView.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.firstsignin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.firstSignInSignOut: {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(FirstSignInActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(FirstSignInActivity.this);
                }
                builder.setTitle("Confirm Sign Out")
                        .setMessage("Do you really want to sign out of this app?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(FirstSignInActivity.this,SplashScreenActivity.class);
                                startActivity(intent);
                                AuthUI.getInstance().signOut(FirstSignInActivity.this);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            }
            // case blocks for other MenuItems (if any)
        }
        return false;
    }
}
