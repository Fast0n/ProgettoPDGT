package com.fast0n.findeat.db_recents;

public class Recent {
    private int id;
    private String record;

    public Recent() {
    }

    public Recent(int id, String record) {
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