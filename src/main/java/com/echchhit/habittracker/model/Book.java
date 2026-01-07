package com.echchhit.habittracker.model;

public class Book {
    private final int id;
    private final String title;
    private final String author;
    private final int totalPages;
    private final int pagesRead;

    public Book(int id, String title, String author, int totalPages, int pagesRead) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.pagesRead = pagesRead;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getTotalPages() { return totalPages; }
    public int getPagesRead() { return pagesRead; }

    public String getProgressText() {
        return pagesRead + " pages read out of " + totalPages;
    }
}