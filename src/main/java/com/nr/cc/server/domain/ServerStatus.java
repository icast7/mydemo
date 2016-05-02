package com.nr.cc.server.domain;
/**
 * This class represents the server status to be printed to the console
 * Created by icastillejos on 4/20/16.
 */
public class ServerStatus {
    int uniqueNumbersSinceLastReport;
    int duplicateNumbersSinceLastReport;
    int totalUniqueNumbers;

    public ServerStatus(){
        super();
    }

    public int getUniqueNumbersSinceLastReport() {
        return uniqueNumbersSinceLastReport;
    }

    public void incrementUniqueNumbersSinceLastReport() {
        this.uniqueNumbersSinceLastReport++;
    }

    public int getDuplicateNumbersSinceLastReport() {
        return duplicateNumbersSinceLastReport;
    }

    public void incrementDuplicateNumbersSinceLastReport() {
        this.duplicateNumbersSinceLastReport++;
    }

    public int getTotalUniqueNumbers() {
        return totalUniqueNumbers;
    }

    public void incrementTotalUniqueNumbers() {
        this.totalUniqueNumbers++;
    }

    public void clearSinceLastReportCounters(){
        this.uniqueNumbersSinceLastReport = 0;
        this.duplicateNumbersSinceLastReport = 0;
    }
}
