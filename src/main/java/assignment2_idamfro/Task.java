package assignment2_idamfro;

import java.util.ArrayList;

public class Task {
	int id, staff, timeConsumption;
	String name;
	int earliestStartTime, latestStartTime;
	int stopTime;
	//if slack is 0, then this is a critical task
	int slack;
	ArrayList<Integer> dependencies;
	ArrayList<Task> dependentTasks = new ArrayList<Task>();
	int indegree;
	int topoNumber;
	
	public Task(int id, String name, int timeConsumption, int manpower, ArrayList<Integer> dependencies){
		this.id = id;
		this.name = name;
		this.timeConsumption = timeConsumption;
		this.staff = manpower;
		this.dependencies = dependencies;
		setIndegree(dependencies.size());
		
		this.earliestStartTime = 1000;
	}	
	public void setIndegree(int numDependencies){
		this.indegree = numDependencies;
	}
	public void addDependentTask(Task task){
		dependentTasks.add(task);
	}

}
