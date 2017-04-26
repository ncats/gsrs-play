package ix.ginas.controllers;

import ix.ginas.utils.reindex.ProcessListener;

import play.Logger;

class SubstanceReIndexListener implements ProcessListener {

    /**
     * Log of index messages so they are saved to file for later examination
     * in case of failure. Previously logs only written to browser.
     */
    private static final Logger.ALogger LOG = Logger.of("index-rebuild");

    private long startTime;
    private StringBuilder message = new StringBuilder();

    private int totalIndexed = 0;

    private String recordsToIndex = "?";

    private long lastUpdateTime;

    private boolean currentlyRunning = false;

    private int currentRecordsIndexed = 0;

    private int recordsIndexedLastUpdate = 0;

    @Override
    public void newProcess() {
        lastUpdateTime = startTime = System.currentTimeMillis();
        message = new StringBuilder(10_000);
        totalIndexed = 0;
        recordsToIndex = "?";
        currentlyRunning = true;
        currentRecordsIndexed = 0;
        recordsIndexedLastUpdate = 0;
    }

    public StringBuilder getMessage() {
        return message;
    }

    public boolean isCurrentlyRunning() {
        return currentlyRunning;
    }

    @Override
    public void doneProcess() {
        if (!currentlyRunning) {
            return;
        }
        updateMessage();
        currentlyRunning = false;

        StringBuilder doneMessage = new StringBuilder(100);

        if (currentRecordsIndexed >= totalIndexed) {
            doneMessage.append("\n\nCompleted Substance reindexing.");
        } else {
            doneMessage.append("\n\nError : did not finish indexing all records, only re-indexed ")
                    .append(currentRecordsIndexed);
        }
        doneMessage.append("\nTotal Time:").append((System.currentTimeMillis() - startTime)).append("ms");
        message.append(doneMessage);
        LOG.info(doneMessage.toString());
    }

    @Override
    public void recordProcessed(Object o) {
        currentRecordsIndexed++;
        if (currentRecordsIndexed % 50 == 0) {
            updateMessage();
        }
    }

    @Override
    public void totalRecordsToProcess(int total) {
        recordsToIndex = Integer.toString(total);
    }

    private void updateMessage() {
        int numProcessedThisTime = currentRecordsIndexed - recordsIndexedLastUpdate;
        if (numProcessedThisTime < 1) {
            return;
        }
        long currentTime = System.currentTimeMillis();

        long totalTimeSerializing = currentTime - startTime;

        String toAppend = "\n" + numProcessedThisTime + " more records Processed: " + currentRecordsIndexed + " of "
                + recordsToIndex + " in " + ((currentTime - lastUpdateTime)) + "ms (" + totalTimeSerializing
                + "ms serializing)";
        Logger.debug("REINDEXING:" + toAppend);
        LOG.info(toAppend);

        message.append(toAppend);

        lastUpdateTime = currentTime;

        recordsIndexedLastUpdate = currentRecordsIndexed;
    }

    @Override
    public void countSkipped(int numSkipped) {
        totalIndexed -= numSkipped;
    }

    @Override
    public void error(Throwable t) {

        t.printStackTrace();

        LOG.error("error reindexing", t);
    }
}