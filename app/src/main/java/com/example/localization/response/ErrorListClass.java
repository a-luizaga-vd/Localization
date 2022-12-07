package com.example.localization.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ErrorListClass {

    List<ErrorPojoClass> listaErrores;
    @Override
    public String toString() {
        return "Software [employees=" + listaErrores + "]";
    }

}
