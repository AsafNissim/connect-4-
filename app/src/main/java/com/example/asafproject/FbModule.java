package com.example.asafproject;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FbModule {

    private final DatabaseReference playRef;

    public FbModule() {
        playRef = FirebaseDatabase.getInstance().getReference("play");
    }

    // מעדכן את העמודה האחרונה שנלחצה
    public void setLastCol(int col) {
        playRef.child("col").setValue(col);

    }
}
