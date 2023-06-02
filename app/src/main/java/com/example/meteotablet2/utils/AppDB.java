package com.example.meteotablet2.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.meteotablet2.entities.SearchedData;

@Database(entities = {SearchedData.class}, version = 1)
public abstract class AppDB extends RoomDatabase {

    public abstract SearchDao searchDao();


}
