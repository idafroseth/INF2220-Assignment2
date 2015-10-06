package assignment2_idamfro;

import java.util.ArrayList;

public class Task {
	int id, staff, timeConsumption, earliestStartTime, latestStartTime, slack;
	String name;
	int stopTime; //Is really startTime + timeConsumption
	//if slack is 0, then this is a critical task
	
	ArrayList<Integer> dependencies;
	ArrayList<Task> dependentTasks = new ArrayList<Task>();
	int indegree;
	boolean visited = false;
	
	public Task(int id, String name, int timeConsumption, int manpower, ArrayList<Integer> dependencies){
		this.id = id;
		this.name = name;
		this.timeConsumption = timeConsumption;
		this.staff = manpower;
		this.dependencies = dependencies;
		setIndegree(dependencies.size());
	}	
	public void setIndegree(int numDependencies){
		this.indegree = numDependencies;
	}
	public void addDependentTask(Task task){
		dependentTasks.add(task);
	}
	public void removeDependentTask(Task task){
		dependentTasks.remove(task);
	}

}
