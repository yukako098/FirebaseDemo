package com.example.yuka.lab7;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText mEditText;
    private Spinner mSpinner;
    private ListView mListView;
    private ArrayList<Artist> mArtist;
    private ArrayAdapter<Artist> mAdapter;

    private DatabaseReference ref_artists;
    private DatabaseReference ref_tracks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = findViewById(R.id.artistET);
        mSpinner = findViewById(R.id.genres);
        mListView = findViewById(R.id.artistListView);
        mArtist = new ArrayList<>();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance(); // Connect to database
        ref_artists = database.getReference("artists"); // inside the databse, find reference of "message"
        ref_tracks = database.getReference("tracks");

        // When user long press listview, to show popup screen.
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // 1. Which artist is clicked?
                Artist artist = mArtist.get(position);

                // 2. show the dialog.
                showUpdateDialog(artist.getId(), artist.getName(), artist.getGenre());
                return false;
            }
        });


        // When user click listview, show artist_tracks_dialog.xml
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = mArtist.get(position);
                Intent intent = new Intent(MainActivity.this, TracksActivity.class);
                intent.putExtra("artist_id", artist.getId());
                intent.putExtra("artist_name", artist.getName());
                startActivity(intent);

            }
        });


    }


    private int getIndexForGenre(String genre){

        switch (genre){
            case "Pop":
                return 0;
            case "Rock":
                return 1;

            case "Hip-Hop":
                return 2;

            case "Classics":
                return 3;

            case "Samba":
                return 4;

            case "Reggae":
                return 5;

            case "K-Pop":
                return 6;

            case "EDM":
                return 7;

            default:
                return 0;
        }
    }

    private void showUpdateDialog(final String id, String name, String genre){
        // 1. build the dialog with the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_artist_dialog, null);
        builder.setView(dialogView);

        final EditText editText = dialogView.findViewById(R.id.dialog_et);
        editText.setText(name);
        final Spinner spinner = dialogView.findViewById(R.id.dialog_spinner);
        spinner.setSelection(getIndexForGenre(genre));

        builder.setTitle("Update " + name);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // 2. set click listnner for update and delete buttons
        Button update_btn = dialogView.findViewById(R.id.dialog_update_btn);
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Artist edited_artist = new Artist(
                        id,
                        editText.getText().toString().trim(),
                        spinner.getSelectedItem().toString());

                if (TextUtils.isEmpty(editText.getText().toString().trim())){
                    return;
                }

                ref_artists.child(id).setValue(edited_artist, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Toast.makeText(MainActivity.this, "Successfully updated the artist", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.dismiss();

            }
        });

        Button delete_btn = dialogView.findViewById(R.id.dialog_delete_btn);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref_artists.child(id).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){
                            Toast.makeText(MainActivity.this, "Successfully remove the artist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ref_tracks.child(id).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){
                            Toast.makeText(MainActivity.this, "Successfully remove the artist", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alertDialog.dismiss();

            }
            // TODO remove tracks that belong to the artist.

                //alertDialog.dismiss();



        });



    }


    @Override // whenever starts app, get call this method *database should be updated.
    protected void onStart() {
        super.onStart();
        ref_artists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // when database changed.
                mArtist.clear();
                for (DataSnapshot artistSnapshiot : dataSnapshot.getChildren()){
                    Artist artist = artistSnapshiot.getValue(Artist.class); // { id : ..., name : ..., genre : ...,}
                    mArtist.add(artist);
                }


                mAdapter = new ArrayAdapter<Artist>(MainActivity.this, android.R.layout.simple_list_item_1, mArtist);
                mListView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //when there's some error
                if (databaseError != null){
                    Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show(); // new class already created, so you have to mention "MainActivity.this"
                }
            }
        });
    }

    public void addArtist(View view) {
//        String artist_id = String.valueOf(id);
        String artist_name = mEditText.getText().toString().trim(); // trim is for getting rid of spaces.
        String artist_genre = mSpinner.getSelectedItem().toString();

        if (!TextUtils.isEmpty(artist_name)){


            // 1. generate an unique id.
            String id = ref_artists.push().getKey(); // give you id as a string

            // 2. create an Artist Object using the id.
            Artist artist = new Artist(id, artist_name, artist_genre);

            // 3. add the artist as a child of "ref_artists"
            ref_artists.child(id).setValue(artist);

            mArtist.add(artist);
            mAdapter.notifyDataSetChanged();
            mEditText.setText("");
        } else {
            Toast.makeText(this, "Please enter the Artist Name", Toast.LENGTH_SHORT).show();
        }



    }
}
