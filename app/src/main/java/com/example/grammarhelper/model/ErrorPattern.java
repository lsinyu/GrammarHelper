package com.example.grammarhelper.model;

public class ErrorPattern {
    public String errorSubtype;
    public int count;

    public ErrorPattern(String errorSubtype, int count) {
        this.errorSubtype = errorSubtype;
        this.count = count;
    }
}
