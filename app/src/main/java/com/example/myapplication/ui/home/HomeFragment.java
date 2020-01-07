package com.example.myapplication.ui.home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    private StorageReference pdfRef;

    ProgressDialog progressDialog;



    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReference();

    private static final int REQUEST_CALL= 34;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);


        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
        }else{
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},9);
        }

        recyclerView=root.findViewById(R.id.recyclerView);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("orders").child("pending").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //actually called for indiv items at the database reference...
                final String fileName = dataSnapshot.getKey();
                final String type = dataSnapshot.child("type").getValue(String.class);
                String user = dataSnapshot.child("user").getValue(String.class);

                DatabaseReference reff = FirebaseDatabase.getInstance().getReference();
                reff.child("User").child(user).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String hostel= dataSnapshot.child("hostelName").getValue(String.class);
                        String mobileNo= dataSnapshot.child("mobileNumber").getValue(String.class);

                        ((MyAdapter) recyclerView.getAdapter()).update(fileName, type, hostel, mobileNo);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MyAdapter myAdapter= new MyAdapter(recyclerView, getActivity(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
        recyclerView.setAdapter(myAdapter);
        return root;
    }

    private void downloadInLocalFile(String name, String type) {

        pdfRef = storageRef.child(name+type);


        final File dir = new File(Environment.getExternalStorageDirectory() + "/pdf");
        final File file = new File(dir, name + type);
            if (!dir.exists()||!dir.isDirectory()) {
                dir.mkdir();
            }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final FileDownloadTask fileDownloadTask = pdfRef.getFile(file);
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Downloading file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(getContext(),"File downloaded", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressDialog.dismiss();
                Toast.makeText(getContext(),"File not downloaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                double progress = ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                progressDialog.setProgress((int) progress);
            }
        });
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        RecyclerView recyclerView;
        Context context;
        ArrayList<String> items=new ArrayList<>();
        ArrayList<String> types= new ArrayList<>();
        ArrayList<String> hostels= new ArrayList<>();
        ArrayList<String> mobileNos= new ArrayList<>();


        public void update(String name, String type, String hostel, String mobileNo){
            items.add(name);
            types.add(type);
            hostels.add(hostel);
            mobileNos.add(mobileNo);
            notifyDataSetChanged();  //refershes the recyler view automatically...
        }


        public MyAdapter(RecyclerView recyclerView, Context context, ArrayList<String> items, ArrayList<String> types,ArrayList<String> hostels, ArrayList<String> mobileNos) {
            this.recyclerView = recyclerView;
            this.context = context;
            this.items = items;
            this.types = types;
            this.hostels = hostels;
            this.mobileNos = mobileNos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {//to create a view for recycle view items
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //initialise the elements of indiv items...
            holder.nameOfFile.setText(items.get(position));
            holder.mobileNumber.setText(mobileNos.get(position));
            holder.hostelName.setText(hostels.get(position));

        }

        @Override
        public int getItemCount() {//return the no of items
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView nameOfFile, hostelName, mobileNumber;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameOfFile=itemView.findViewById(R.id.nameOfFiles);
                hostelName=itemView.findViewById(R.id.hostelName);
                mobileNumber=itemView.findViewById(R.id.mobileNo);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = recyclerView.getChildAdapterPosition(view);
                        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
                            downloadInLocalFile(items.get(position),types.get(position));
                        }else{
                            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},9);
                        }
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int position = recyclerView.getChildAdapterPosition(view);
                        Toast.makeText(getActivity(), mobileNos.get(position).trim(),Toast.LENGTH_SHORT).show();
                        if (mobileNos.get(position).trim().length()>0) {
                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                                String dial = "tel:" + mobileNos.get(position);
                                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                            } else {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                            }
                        }
                        return false;
                    }
                });
            }
        }
    }
}