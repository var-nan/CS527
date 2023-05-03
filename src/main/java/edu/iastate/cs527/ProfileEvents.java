package edu.iastate.cs527;

import jdk.jfr.*;

/**
 *
 * Measures Execution times for defined Events. Runs with Java Flight Recorder.
 *
 * Flags to start recording.
 * "-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:StartFlightRecording"
 *
 * @author nandhan, Created on 03/05/23
 */
public class ProfileEvents {

    /*
    NOTE: Enable below classes to profile with JFR.
     */

    @Label("Seek Record Time")
    @Enabled(false)
    @StackTrace(false)
    public static class SeekRecordTime extends Event{}

    @Label("Atomic Get Time")
    @Enabled(false)
    @StackTrace(false)
    public static class LFGetTime extends Event {}

    @Label("Lock Search Time")
    @Enabled(false)
    @StackTrace(false)
    public static class LockSearchTime extends Event{}

    @Label("Search Time")
    @Enabled(false)
    public static class SearchTime extends Event{}

}