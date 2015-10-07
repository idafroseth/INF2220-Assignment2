package assignment2_idamfro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Project {
	public Project(String projectName){
		this.inputFilename = projectName;
		importTasks();
		topologicalSort(NUM_TASKS);
		printTasks();
		printProjectExecution();
	}
	
	HashMap<Integer, Task> projectTasks;
	int NUM_TASKS;
	static int loopCount = 0;
	String inputFilename;
	LinkedList<Task> queue;
	LinkedList<Task> delayedQueue = new LinkedList<Task>();
	LinkedList<Task> lastQueue = new LinkedList<Task>();
	int finishTime;
	
	
	//Multiple tasks can be executed simultanously
	//Delayed t1 when the only edge from t1 is to t3 and t3 also have a dependency to t2 which takes more time than t1
	//t1 has then positive slack because it do not have to be done before x time units after schedule
	//Tasks with slack 0 is critical tasks and someone else depend on it
	//Complexity is N tasks + E edges/task
	public void topologicalSort(int projectSize){
		//int time = startTask().stopTime;
		//findNextTask(0);
		int counter = 0;
		LinkedList<Task> queue = new LinkedList<Task>();
		for(Task task : projectTasks.values()){
			if(task.indegree == 0){
				queue.add(task);
				task.earliestStartTime = 0;
				task.stopTime = task.timeConsumption;
			}
		}
		Task workingTask = null;
		while( queue.size() != 0){
			workingTask = queue.getFirst();
			queue.remove(workingTask);
			++counter;
			for(Task task : workingTask.outEdges){
				if(--task.indegree == 0){
					queue.add(task);
					task.earliestStartTime = workingTask.stopTime;
					task.stopTime = task.earliestStartTime +task.timeConsumption;
					if(task.outEdges.size()!= 0){
						delayedQueue.add(task);
					}else{
						lastQueue.add(task);
					}
				}
	
			}
		}
		finishTime = workingTask.stopTime;
		calculateSlack();
		haveLoop(counter, projectSize);
	}
	
	private void calculateSlack(){
		while(lastQueue.size() != 0){
			Task task = lastQueue.removeFirst();
			if(task.stopTime<finishTime){
				task.slack = finishTime - task.stopTime;
				task.latestStartTime = finishTime - task.timeConsumption;
			}
		}
		while(delayedQueue.size() != 0){
			Task task = delayedQueue.removeFirst();
			int latestStartTime = 0;
			inner:
			for(Task dependent : task.outEdges){
				
				//If task is the only inEdge to another task, this task cannot be delayed
				if(dependent.inEdges.size() == 1){
					System.out.println("This task " + task.id +" cannot be delayed");
					task.slack = 0;
					task.latestStartTime = task.earliestStartTime;
					break inner;
				}
				int count = 0;
				for(Integer sameLevel : dependent.inEdges){
						if(projectTasks.get(sameLevel).stopTime<task.stopTime){
							count++;
						}
						else if(latestStartTime<projectTasks.get(sameLevel).stopTime){
							latestStartTime = projectTasks.get(sameLevel).stopTime;
							task.visited =false;
						}
				}
				//If all the in edges where this.task has an out edge is the largest stop time, this mean that this task cannot be delayed
				if(count == dependent.inEdges.size()){
					System.out.println("This task "+ task.id+ " cannot be delayed!!");
					task.slack = 0;
					task.latestStartTime = task.earliestStartTime;
					break inner;
					
				}
				else if(task.latestStartTime < latestStartTime){
					task.latestStartTime = latestStartTime;
					task.slack = task.latestStartTime -task.earliestStartTime;
				//	System.out.println("Task id " + task.id + "setting latest startTime to " + latestStartTime + " and slack to " + task.slack);
				}
			}
		}
	}
	
	//Check if there is a loop. If there is a simple loop it would be able to find the loop, 
	//if the loop is not only between two, but three or more tasks, we are not able to catch where the loop is
	//Except eliminate what tasks that potentially can be involved in the loop	
	public boolean haveLoop(int counter, int projectSize){
		if( counter != projectSize){
			System.out.println("LOOP - The project is not realisable....");
			LinkedList<Task> potTask = new LinkedList<Task>();
			
			for(Task task : projectTasks.values()){
				//to minimize the search
				if(task.indegree != 0 && task.outEdges.size() != 0){
					potTask.add(task);
				}
			}
			
			System.out.println("Nodes in a loop are: ");
			
			while(potTask.size() != 0){
				Task workTask = potTask.removeFirst();
				int startId = workTask.id;
	
				LinkedList<Task> track = new LinkedList<Task>();
				
				if(checkLoop(workTask, startId)){
					track.add(workTask);
				}
			}
			System.exit(0);
			return true;
			
		}
		return false;
	}
	/**
	 * Recursive method that will print a loop. It check if an dependentTask have a way to get to the 
	 * @param workTask
	 * @param id
	 * @return
	 */
	private boolean checkLoop(Task workTask, int id){
		boolean result= false;
		if(workTask.outEdges.size() == 0){
			return false;
		}
		for(Task dependentTask : workTask.outEdges){
			if(dependentTask.id == id){
				System.out.print(id + ", ");
				result = true;
				return true;
	
			}else{
				result = checkLoop(dependentTask, id);
				//return result;
			}
		}
		return result;
	}
	
	
	
	public void printProjectExecution(){
		//Easiest way has to be to save the items in a map with the <StartTime, ArrayList<Tasks>>
		HashMap<Integer, ArrayList<Task>> workingSet = new HashMap<Integer, ArrayList<Task>>();
		HashMap<Integer, ArrayList<Task>> stoppingSet = new HashMap<Integer, ArrayList<Task>>();
		
		ArrayList<Task> tasksAtTime = new ArrayList<Task>();
		ArrayList<Task> tasksStopAtTime = new ArrayList<Task>();
		int time =0;
	//	queue = new LinkedList<Task>();
		int counter = 0;
		while(counter<NUM_TASKS){
			tasksAtTime = new ArrayList<Task>();
			tasksStopAtTime = new ArrayList<Task>();
			for(Task ta : projectTasks.values()){
				if(time == ta.earliestStartTime){
					tasksAtTime.add(ta);	
				}
				if(time == ta.stopTime){
					tasksStopAtTime.add(ta);	
					counter++;
				}
			}
			if(tasksAtTime.size()>0){
				workingSet.put(time, tasksAtTime);
			}
			if(tasksStopAtTime.size()>0){
				stoppingSet.put(time, tasksStopAtTime);
			}
			time++;
		}
	
		time = 0;
		int manpower = 0;
		System.out.println("\n" + "Project EXECUTION");
		while(stoppingSet.size() != 0){
			if(workingSet.containsKey(time) || stoppingSet.containsKey(time)){
				System.out.println("**************************");
				System.out.println("Time: " + time);
				if(workingSet.containsKey(time)){
					for(Task taskAtTime : workingSet.get(time)){
						System.out.println("Starting: " + taskAtTime.id);
						manpower += taskAtTime.staff;
					}
				}
				
				if(stoppingSet.containsKey(time)){
					for(Task stopAtTime : stoppingSet.get(time)){
						System.out.println("Finished: " + stopAtTime.id);
						manpower -= stopAtTime.staff;
					}
				}
				System.out.println("Manpower: " + manpower);
			}
			workingSet.remove(time);
			stoppingSet.remove(time);
			time++;
		}
		System.out.println("********THE PROJECT WILL FINISH IN: " + (time-1) + "************");

	}
	
	/**
	 * Print the project tasks and its attribute
	 */
	public void printTasks(){
		for(Task task : projectTasks.values()){
			System.out.println("Task Id: " + task.id +"\n"
					+ "name of this task: " + task.name + "\n"
					+ "Time estimate: " + task.timeConsumption + "\n"
					+ "Manpower requirements: " + task.staff + "\n"
					+ "Earliest start time: " + task.earliestStartTime + "\n"
					+ "Latest start time: " + task.latestStartTime + "\n"
					+ "Slack: " + task.slack);
//			Denpendency edges ");
			System.out.println("outEdges: ");
			for(Task dependency : task.outEdges){
				System.out.print(dependency.id + ", ");
			}
			System.out.println("inEdges: ");
			for(Integer dependency : task.inEdges){
				System.out.print(dependency + ", ");
			}
			System.out.println("**********************************");
		}
	}
	
	/**
	 * Parsing the inputFile and initilaizes the project
	 * * (Complexity N)
	 */
	public void importTasks(){
		try {		
			File inputFile = new File(inputFilename);
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			NUM_TASKS = Integer.parseInt(reader.readLine());
			projectTasks = new HashMap<Integer, Task>(NUM_TASKS);
			String line = reader.readLine();
			
			while((line = reader.readLine())!= null){
				String[] words = line.split("\\s+");
				ArrayList<Integer> dependencies = new ArrayList<Integer>();
				for(int i = 4; i<words.length-1; i++){
					dependencies.add(Integer.parseInt(words[i]));
				}
				Task newTask = new Task(Integer.parseInt(words[0]), words[1], Integer.parseInt(words[2]), Integer.parseInt(words[3]), dependencies);
				projectTasks.put(Integer.parseInt(words[0]) ,newTask);	
			}
			initializeDependantTasks();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Use the task.dependencies to find which tasks are dependant of a task.
	 * This is done to simplify the topoSort
	 * (Complexity N^2)
	 */
	private void initializeDependantTasks(){
		for(Task task : projectTasks.values()){
			for(Integer taskId : task.inEdges){
				projectTasks.get(taskId).addDependentTask(task);
			}
		}
	}
	

	public static void main(String[] args){
		if(args.length != 1){
			System.out.println("Wrong number of arguments, use: java assignment2.Project <projectName>.txt");
			System.exit(0);
		}
		Project project = new Project(args[0]);
	//	Project project = new Project("");
	}
}
