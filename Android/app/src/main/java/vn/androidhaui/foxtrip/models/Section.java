package vn.androidhaui.foxtrip.models;

import java.util.List;

public class Section {
    private final String title;
    private final List<Tour> tours;
    public Section(String title, List<Tour> tours) {
        this.title = title; this.tours = tours;
    }
    public String getTitle() { return title; }
    public List<Tour> getTours() { return tours; }
}

