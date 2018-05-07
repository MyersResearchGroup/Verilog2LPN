package edu.utah.ece.async.Verilog2LPN;

import edu.utah.ece.async.lema.verification.lpn.LPN;

import java.util.HashSet;

public class WrappedLPN {
    private int nextPlace;
    private int nextTransition;
    private String currentStart;

    public LPN lpn;
    public HashSet<String> last;

    public WrappedLPN(LPN lpn) {
        this.nextPlace = 0;
        this.nextTransition = 0;

        this.currentStart = null;
        this.lpn = lpn;
        this.last = new HashSet<>();
    }

    public String nextPlaceName() {
        String nextPlaceName = "P" + Integer.toString(nextPlace);
        nextPlace++;

        return nextPlaceName;
    }

    public String nextTransitionName() {
        String nextTransitionName = "T" + Integer.toString(nextTransition);
        nextTransition++;

        return nextTransitionName;
    }

    public void closeNet() {
        String transitionName = nextTransitionName();

        this.lpn.addTransition(transitionName);

        for (String place : this.last) {
            this.lpn.addMovement(place, transitionName);
        }

        this.lpn.addMovement(transitionName, currentStart);
    }

    public void createNewNet() {
        String placeName = nextPlaceName();
        this.currentStart = placeName;
        this.last.clear();
        this.last.add(placeName);
        this.lpn.addPlace(placeName, true);
    }
}
