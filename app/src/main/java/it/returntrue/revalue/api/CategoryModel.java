/*
 * Copyright (C) 2016 Alessandro Riperi
*/

package it.returntrue.revalue.api;

/**
 * Represents a category from data model
 * */
public class CategoryModel {
    public int Id;
    public String Name;

    @Override
    public String toString() {
        return Name;
    }
}