package assignment2_idamfro;

import java.util.ArrayList;

public class Task {
	int id, staff, timeConsumption, earliestStartTime;
	int latestStartTime =0;
	int slack = -1;
	String name;
	int stopTime; //Is really startTime + timeConsumption
	//if slack is 0, then this is a critical task
	
	ArrayList<Integer> inEdges;
	ArrayList<Task> outEdges = new ArrayList<Task>();
	int indegree;
	boolean visited = false;
	
	public Task(int id, String name, int timeConsumption, int manpower, ArrayList<Integer> dependencies){
		this.id = id;
		this.name = name;
		this.timeConsumption = timeConsumption;
		this.staff = manpower;
		this.inEdges = dependencies;
		setIndegree(dependencies.size());
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
