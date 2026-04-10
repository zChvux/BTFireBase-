package com.example.bai2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public int categoryId;
    public String name;
    public String description;

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}