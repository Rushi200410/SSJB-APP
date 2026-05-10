package com.example.ssjb.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "students")
public class Student {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    @NonNull
    public String instrument;

    public Double balance;

    public Student(@NonNull String name, @NonNull String instrument, Double balance) {
        this.name = name;
        this.instrument = instrument;
        this.balance = balance;
    }
}
