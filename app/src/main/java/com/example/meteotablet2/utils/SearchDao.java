package com.example.meteotablet2.utils;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.meteotablet2.entities.SearchedData;

import java.util.List;

@Dao
public interface SearchDao {

    @Insert
    void insert(SearchedData searchedData);

    @Delete
    void delete(SearchedData searchedData);

    @Update
    void update(SearchedData searchedData);

    @Query("DELETE FROM 'searcheddata'")
    void deleteAll();

    @Query("SELECT * FROM 'searcheddata'")
    List<SearchedData> getAll();

    @Query("SELECT * FROM 'searcheddata' WHERE id IN (:id)")
    SearchedData getDataById(int id);

}
