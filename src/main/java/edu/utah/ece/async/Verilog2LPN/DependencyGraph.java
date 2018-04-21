package edu.utah.ece.async.Verilog2LPN;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class DependencyGraph<T> {
	private HashMap<T, HashSet<T>> adjacencyList;
	
	public DependencyGraph() {
		adjacencyList = new HashMap<>();
	}
	
	public boolean addDependency(T dependent, T dependee) {
		if(causesCycle(dependent, dependee)) {
			return false;
		}
		
		adjacencyList.get(dependent).add(dependee);
		return true;
	}
	
	private boolean causesCycle(T dependent, T dependee) {
		HashSet<T> visited = new HashSet<>();
		
		LinkedList<T> toVisit = new LinkedList<T>(adjacencyList.get(dependee));
		
		while(!toVisit.isEmpty()) {
			if(visited.contains(dependent)) {
				return false;
			}
			
			T current = toVisit.remove();
			visited.add(current);
			toVisit.addAll(adjacencyList.get(current));
		}
		
		return true;
	}
}
