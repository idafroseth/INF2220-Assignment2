package assignment2_idamfro;

import java.util.ArrayList;

public class Task {
	int id, staff, timeConsumption, earliestStartTime;
	int latestStartTime =-1;
	int slack = -1;
	String name;
	int stopTime = 0; //Is really startTime + timeConsumption
	//if slack is 0, then this is a critical task
	
	ArrayList<Integer> inEdges;
	ArrayList<Task> outEdges = new ArrayList<Task>();
	int indegree;
	
	public Task(int id, String name, int timeConsumption, int manpower, ArrayList<Integer> dependentOf){
		this.id = id;
		this.name = name;
		this.timeConsumption = timeConsumption;
		this.staff = manpower;
		this.inEdges = dependentOf;
		setIndegree(dependentOf.size());
	}	
	public void setIndegree(int numDependencies){
		this.indegree = numDependencies;
	}
	public void addDependentTask(Task task){
		outEdges.add(task);
	}
	public void removeDependentTask(Task task){
		outEdges.remove(task);
	}

}
