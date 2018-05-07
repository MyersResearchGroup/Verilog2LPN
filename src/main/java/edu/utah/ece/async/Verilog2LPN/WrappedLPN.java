package edu.utah.ece.async.Verilog2LPN;

import edu.utah.ece.async.lema.verification.lpn.LPN;

import java.util.HashSet;

public class WrappedLPN {
    public LPN lpn;
    public HashSet<String> last;

    public WrappedLPN(String initial) {
        this.lpn = new LPN();
        this.last = new HashSet<>();

        this.last.add(initial);
        this.lpn.addPlace(initial, true);
    }
}
