package com.example.yuka.lab7;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TracksActivity extends AppCompatActivity {

    private DatabaseReference ref_tracks;
    private TextView artistTv;
    private Artist mArtist;
    private EditText mEditText;
    private SeekBar seekBar;
    private ListView mListView;
    private ArrayList<Track> mArray;
    private ArrayAdapter<Track> mAdapter;
    private String artist_id;
    private String artist_name;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_tracks_dialog);
        Intent intent = getIntent();
        artist_id = intent.getStringExtra("artist_id");
        artist_name = intent.getStringExtra("artist_name");

        artistTv = findViewById(R.id.track_artist_name);
        mEditText = findViewById(R.id.track_et);
        seekBar = findViewById(R.id.track_seekBar);
        mListView = findViewById(R.id.tracks_listView);
        mArray = new ArrayList<>();


        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // Connect to database
        ref_tracks = database.getReference("tracks"); // inside the databse, find reference of "message" * In this case, "Hello, World!"

        artistTv.setText(artist_name);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // When user long press listview, to show popup screen.
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 1. Which artist is clicked?
                Track track = mArray.get(position);

                // 2. show the dialog.
                showUpdateDialog(track.getId(), track.getTrackName());
                return false;
            }
        });


    }


    private void showUpdateDialog(final String id, String name){
        // 1. build the dialog with the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_tracks_dialog, null);
        builder.setView(dialogView);

        final EditText editText = dialogView.findViewById(R.id.dialog_et);
        editText.setText(name);
//        final SeekBar seekBar = dialogView.findViewById(R.id.dialog_seekBar);
//        spinner.setSelection(getIndexForGenre(genre));

        builder.setTitle("Update " + name);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // 2. set click listnner for update and delete buttons
        Button update_btn = dialogView.findViewById(R.id.dialog_update_btn);
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // String id, String trackName, String rating, String artist_id
                Track edited_artist = new Track(
                        id,
                        editText.getText().toString().trim(),
                        String.valueOf(seekBar.getProgress()),
                        artist_id);

                if (TextUtils.isEmpty(editText.getText().toString().trim())){
                    return;
                }

                ref_tracks.child(artist_id).child(id).setValue(edited_artist, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(TracksActivity.this, "Successfully updated the track", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.dismiss();

            }
        });

        Button delete_btn = dialogView.findViewById(R.id.dialog_delete_btn);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref_tracks.child(artist_id).child(id).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){
                            Toast.makeText(TracksActivity.this, "Successfully remove the track", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.dismiss();

            }
        });



    }

    @Override // whenever starts app, get call this method *database should be updated.
    protected void onStart() {
        super.onStart();
        ref_tracks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // when database changed.
                mArray.clear();
                // dataSnapshot(tracks) - artist_id - getChildren
                for (DataSnapshot trackSnapshot : dataSnapshot.child(artist_id).getChildren()){
                    Track track = trackSnapshot.getValue(Track.class); // { id : ..., name : ..., genre : ...,}
                    mArray.add(track);
                }


                mAdapter = new ArrayAdapter<Track>(TracksActivity.this, android.R.layout.simple_list_item_1, mArray);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //when there's some error
                if (databaseError != null){
                    Toast.makeText(TracksActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show(); // new class already created, so you have to mention "MainActivity.this"
                }
            }
        });
    }


    public void addTracks(View view) {
        String track_name = mEditText.getText().toString().trim(); // trim is for getting rid of spaces.
        String track_rating = String.valueOf(seekBar.getProgress());


        if (!TextUtils.isEmpty(track_name)){

            // 1. generate an unique id.
            String id = ref_tracks.push().getKey(); // give you id as a string


            // 2. create an Track Object using the id.
            Track track = new Track(id, track_name, track_rating, artist_id);

            // 3. add the artist as a child of "ref_tracks"
            ref_tracks.child(artist_id).child(id).setValue(track);
            // ref_tracks.child(artist_id).child(/* track id */)

            mArray.add(track);
            mAdapter.notifyDataSetChanged();
            mEditText.setText("");

        } else {
            Toast.makeText(this, "Please enter the Artist Name", Toast.LENGTH_SHORT).show();
        }

    }

}
