package com.fast0n.findeat.database;

public class Record {
    private int id;
    private String record;

    public Record() {
    }

    public Record(int id, String record) {
        this.id = id;
        this.record = record;
    }

    public int getId() {
        return id;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public void setId(int id) {
        this.id = id;
    }
}