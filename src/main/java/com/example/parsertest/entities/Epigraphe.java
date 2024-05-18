package com.example.parsertest.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Epigraphe {
    private int id;
    private String name;
    private String translation;
    private List<String> imgUrl = new ArrayList<>();
    private LocalDate date;
    private String original;

    private Epigraphe(int id) {
        this.id = id;
    }

    public static Epigraphe newInstance(int id) {
        return new Epigraphe(id);
    }
}
