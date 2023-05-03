package edu.iastate.cs527;

import jdk.jfr.*;

/**
 * @author nandhan, Created on 03/05/23
 */
public class Profiling {

    @Label("Seek Record Time")
    @Enabled(false)
    @StackTrace(false)
    public static class SeekRecordTime extends Event{}

    @Label("Atomic Get Time")
    @Enabled(false)
    @StackTrace(false)
    public static class LFGetTime extends Event {}

    @Label("Search Time")
    @Enabled(false)
    public static class SearchTime extends Event{}


}