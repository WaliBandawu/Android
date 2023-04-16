package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toolbar;


import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION ="com.example.notepad.NOTE_POSITION";
    public static final int POSITION_NOT_SET = -1;

    private NoteInfo mNote;
    private Boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mtextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;
    private NoteActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
//       Toolbar toolbar = findViewById(R.id.toolbar);
//       setSupportActionBar(toolbar);
        ViewModelProvider viewModelProvider= new ViewModelProvider( getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mViewModel =viewModelProvider.get(NoteActivityViewModel.class);
        if (mViewModel.mIsNewlyCreated &&  savedInstanceState !=null)
            mViewModel.restoreState(savedInstanceState);

        mViewModel.mIsNewlyCreated =false;

        mSpinnerCourses = findViewById(R.id.spinner_courses);
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adopterCourses =
                new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,courses);
            adopterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerCourses.setAdapter(adopterCourses);
            readDisplayStateValue();
            saveOriginalNoteValue();

        mtextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if(mIsNewNote){
            createNewNote();
        }
        displayNote(mSpinnerCourses, mtextNoteTitle, mTextNoteText);

    }

    private void saveOriginalNoteValue() {
        if(mIsNewNote){
            return;

        }
        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mViewModel.mOriginalNoteTitle = mNote.getTitle();
        mViewModel.mOriginalNoteText = mNote.getText();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState!=null)
            mViewModel.saveState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling){
            if(mIsNewNote){
            DataManager.getInstance().removeNote(mNotePosition);

        }else {
                storePreviousNoteValue();
            }
        }else {
                saveNote();
            }

    }

    private void storePreviousNoteValue() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mOriginalNoteTitle);
        mNote.setText(mViewModel.mOriginalNoteText);

    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
         mNotePosition = dm.createNewNote();
        mNote=dm.getNotes().get(mNotePosition);
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mtextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses= DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote);
        spinnerCourses.setSelection(courseIndex);
        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void readDisplayStateValue() {
        Intent intent =getIntent();
        int position= intent.getIntExtra(NOTE_POSITION,POSITION_NOT_SET);
        mIsNewNote = position ==POSITION_NOT_SET;
        if(!mIsNewNote)
            mNote=DataManager.getInstance().getNotes().get(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_email) {
            sendEmail();
            return true;
        }else if(id==R.id.action_cancel){

            mIsCancelling=true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        CourseInfo courses = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mtextNoteTitle.getText().toString();
        String text = "Check out what I learned from Pluralsight course\""+
                courses.getTitle() +"\"\n" + mTextNoteText.getText();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,text);
        startActivity(intent);



    }
}