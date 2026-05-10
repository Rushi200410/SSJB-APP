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

    public String middleName;
    public String surname;
    public String phoneNumber;

    @NonNull
    public String instrument;

    public String address;
    public String joiningDateIso;
    public String knowledgeLevel;
    public String lastUpdatedIso;
    public Double balance;

    public Student(
            @NonNull String name,
            String middleName,
            String surname,
            String phoneNumber,
            @NonNull String instrument,
            String address,
            String joiningDateIso,
            String knowledgeLevel,
            String lastUpdatedIso,
            Double balance
    ) {
        this.name = name;
        this.middleName = middleName;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.instrument = instrument;
        this.address = address;
        this.joiningDateIso = joiningDateIso;
        this.knowledgeLevel = knowledgeLevel;
        this.lastUpdatedIso = lastUpdatedIso;
        this.balance = balance;
    }
}
