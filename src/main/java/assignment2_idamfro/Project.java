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
		initializeDependantTasks();	
		topologicalSort();
		printTasks();
		printProjectExecution();
	}
	
	HashMap<Integer, Task> projectTasks;
	int NUM_TASKS;
	String inputFilename;
	
	
	
	//Multiple tasks can be executed simultanously
	//Delayed t1 when the only edge from t1 is to t3 and t3 also have a dependency to t2 which takes more time than t1
	//t1 has then positive slack because it do not have to be done before x time units after schedule
	//Tasks with slack 0 is critical tasks and someone else depend on it
	public void topologicalSort(){
		//int time = startTask().stopTime;
		//findNextTask(0);
		int counter = 0;
		LinkedList<Task> queue = new LinkedList<Task>();
		for(Task task : projectTasks.values()){
			if(task.indegree == 0){
				queue.add(task);
				task.earliestStartTime = 0;
				task.stopTime = task.timeConsumption;
				System.out.println(task.id + " is added as start task" );
			}
		}
		while( queue.size() != 0){
			Task workingTask = queue.getFirst();
			queue.remove(workingTask);
			++counter;
			for(Task task : workingTask.dependentTasks){
				if(--task.indegree == 0){
					queue.add(task);
					task.earliestStartTime = workingTask.stopTime;
					task.stopTime = task.earliestStartTime +task.timeConsumption;
				}
			}
		}
		if( counter != NUM_TASKS){
			System.out.println("THERE IS A LOOP!");
			System.exit(0);
		//	throw new CycleFoundException();
		}
		System.out.println(counter + " and number of tasks are: " + NUM_TASKS);
	}

	public void printProjectExecution(){
		/**
		 * Project Execution
		 *  Time: 0 Starting: x
		 *  		Starting: x
		 * 	   Current staff: x
		 * 
		 * Print every task
		 *  - Identity number 
		 *  - Name
		 *  - Time needed to finish the task
		 *  - ManpowerRequired
		 *  - Earliest starting time
		 *  - Latest starting time
		 *  - slack
		 */
		
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
		System.out.println("********THE PROJECT WILL FINISH IN: " + time +" *********");

	}
	public void printTasks(){
		for(Task task : projectTasks.values()){
			System.out.println("Task Id: " + task.id +"\n"
					+ "name of this task: " + task.name + "\n"
					+ "Time estimate: " + task.timeConsumption + "\n"
					+ "Manpower requirements: " + task.staff + "\n"
					+ "Earliest start time: " + task.earliestStartTime + "\n"
					+ "Latest start time: " + task.latestStartTime + "\n"
					+ "Slack: " + task.slack +"\n"
					+ "Sequence numb: " + task.topoNumber);
//			Denpendency edges ");
			System.out.println("dependentTasks: ");
			for(Task dependency : task.dependentTasks){
				System.out.print(dependency.id + ", ");
			}
			System.out.println("depencies: ");
			for(Integer dependency : task.dependencies){
				System.out.print(dependency + ", ");
			}
			System.out.println("**********************************");
		}
	}
	
	/**
	 * Parsing the inputFile and initilaizes the project
	 */
	public void importTasks(){
		try {		
			File inputFile = new File("input.txt");
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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void initializeDependantTasks(){
		for(Task task : projectTasks.values()){
			for(Integer taskId : task.dependencies){
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
	}
}
