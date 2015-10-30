package assignment2_idamfro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Project {
	public Project(String projectName){
		this.inputFilename = projectName;
		System.out.println("\n******** PROJECT " + inputFilename + " **********");
		importTasks();
		topologicalSort(NUM_TASKS);
		for(Task task:projectTasks.values()){
				printTasks(task);
		}
		printProjectExecution();
	}
	
	HashMap<Integer, Task> projectTasks;
	int NUM_TASKS;
	static int loopCount = 0;
	String inputFilename;
	LinkedList<Task> queue;
	LinkedList<Task> delayedQueue = new LinkedList<Task>();
	LinkedList<Task> lastQueue = new LinkedList<Task>();
	HashMap<Integer, ArrayList<Task>> tasksAtStartTime = new HashMap<Integer, ArrayList<Task>>();
	HashMap<Integer, ArrayList<Task>> tasksAtStopTime = new HashMap<Integer, ArrayList<Task>>();
	Set<Integer> loop = new HashSet<Integer>();
	int finishTime;
	
	
	//Multiple tasks can be executed simultanously
	//Delayed t1 when the only edge from t1 is to t3 and t3 also have a dependency to t2 which takes more time than t1
	//t1 has then positive slack because it do not have to be done before x time units after schedule
	//Tasks with slack 0 is critical tasks and someone else depend on it
	/**
	 * Performing a topological sort of the project execution. The complexity of this method is N-tasks
	 * @param projectSize
	 */
	public void topologicalSort(int projectSize){
		int counter = 0;
		LinkedList<Task> queue = new LinkedList<Task>();
		for(Task task : projectTasks.values()){
			if(task.indegree == 0){
				queue.add(task);
				task.earliestStartTime = 0;
				task.stopTime = task.timeConsumption;
				delayedQueue.add(task);
				addToStartQueue(task.earliestStartTime,task);
				addToStopQueue(task.stopTime,task);
			}
		}
		Task workingTask = null;
		while( queue.size() != 0){
			workingTask = queue.removeFirst();
			++counter;
			
			for(Task task : workingTask.outEdges){
				if(task.earliestStartTime<workingTask.stopTime){
					task.earliestStartTime = workingTask.stopTime;
				}
				if(--task.indegree == 0){
					queue.add(task);
					int time = task.earliestStartTime;
					int stopTime = time +task.timeConsumption;
					task.earliestStartTime =  time;
					task.stopTime = stopTime;
					
					addToStartQueue(time,task);
					addToStopQueue(stopTime,task);
					
					
					//Add them to the slack queues
					if(task.outEdges.size()!= 0){
						delayedQueue.add(task);
					}else{
						lastQueue.add(task);
					}
				}
	
			}
		}
		finishTime = workingTask.stopTime;
		
		haveLoop(counter, projectSize);
		calculateSlack();
	}
	
	/**
	 * Private method to add a task to the start queue
	 * @param stopTime
	 * @param task
	 */
	private void addToStartQueue(int time, Task task){
		if(tasksAtStartTime.containsKey(time)){
			ArrayList<Task> updated = tasksAtStartTime.get(time);
			updated.add(task);
			tasksAtStartTime.replace(time, updated);
		}else{
			ArrayList<Task> newList = new ArrayList<Task>();
			newList.add(task);
			tasksAtStartTime.put(time, newList);
		}
	}
	
	/**
	 * Private method to add a task to the stop queue
	 * @param stopTime
	 * @param task
	 */
	private void addToStopQueue(int stopTime, Task task){
		if(tasksAtStopTime.containsKey(stopTime)){
			ArrayList<Task> updated = tasksAtStopTime.get(stopTime);
			updated.add(task);
			tasksAtStopTime.replace(task.stopTime, updated);
		}else{
			ArrayList<Task> newList = new ArrayList<Task>();
			newList.add(task);
			tasksAtStopTime.put(stopTime, newList);
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
			for(Integer id : loop){
				System.out.print(id + ", ");
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
				loop.add(id);
				return true;
	
			}else{
				result = checkLoop(dependentTask, id);
			}
		}
		return result;
	}
	
	//Complexity is N tasks + E edges/task
		/**
		 * Method to calculate the slack of the task. Have to go thorugh every task and check if there is some slack or not and also its edges.
		 */
		private void calculateSlack(){
			while(lastQueue.size() != 0){
				Task task = lastQueue.removeFirst();
				if(task.stopTime<finishTime){
					task.slack = finishTime - task.stopTime;
					task.latestStartTime = finishTime - task.timeConsumption;
				}else if(task.stopTime == finishTime){
					task.latestStartTime = task.earliestStartTime;
					task.slack = 0;
				}
			}
			while(delayedQueue.size() != 0){
				Task task = delayedQueue.removeFirst();
				int latestStartTime = 0;
				inner:
				for(Task dependent : task.outEdges){
					
					//If task is the only inEdge to another task, this task cannot be delayed
					if(dependent.inEdges.size() == 1){
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
							}
					}
					//If all the in edges where this.task has an out edge is the largest stop time, this mean that this task cannot be delayed
					if(count == dependent.inEdges.size()){
						task.slack = 0;
						task.latestStartTime = task.earliestStartTime;
						break inner;
						
					}
					else if(task.latestStartTime < latestStartTime){
						task.latestStartTime = latestStartTime;
						task.slack = task.latestStartTime -task.earliestStartTime;
					}
				}
			}
		}
	//Complexity of this method is N-tasks
	/**
	 * Print the project execution by going through the execution queue. 
	 */
	public void printProjectExecution(){
		int time =0;
		int manpower = 0;
		System.out.print("\n" + "Project EXECUTION \n"+
		"------------------------");
		while(tasksAtStopTime.size() != 0){
			if(tasksAtStartTime.containsKey(time) || tasksAtStopTime.containsKey(time)){
			//	System.out.println();
				System.out.print("\n" + "Time: " + time);
				if(tasksAtStartTime.containsKey(time)){
					for(Task taskAtTime : tasksAtStartTime.get(time)){
						System.out.print("\n	Starting: " + taskAtTime.id);
						manpower += taskAtTime.staff;
					}
				}
				
				if(tasksAtStopTime.containsKey(time)){
					for(Task stopAtTime : tasksAtStopTime.get(time)){
						System.out.print("\n	Finished: " + stopAtTime.id);
						manpower -= stopAtTime.staff;
					}
				}
				System.out.print("\n	Current staff: " + manpower);
			}
			tasksAtStartTime.remove(time);
			tasksAtStopTime.remove(time);
			time++;
		}
		System.out.println("\n***  Shortest possible project execution is: " + (time-1) + "  ***\n");

	}
	
	/**
	 * Print the project tasks and its attribute
	 */
	public void printTasks(Task task){
//		for(Task task : projectTasks.values()){
			System.out.print("Task Id: " + task.id +"\n"
					+ "Task name: " + task.name + "\n"
					+ "Time estimate: " + task.timeConsumption + "\n"
					+ "Manpower requirements: " + task.staff + "\n"
					+ "Earliest start time: " + task.earliestStartTime + "\n"
					+ "Latest start time: " + task.latestStartTime + "\n"
					+ "Slack: " + task.slack);
//			Denpendency edges ");
			System.out.print("outEdges: ");
			for(Task dependency : task.outEdges){
				System.out.print(dependency.id + ", ");
			}
			System.out.print("\n" + "inEdges: ");
			for(Integer dependency : task.inEdges){
				System.out.print(dependency + ", ");
			}
			System.out.println("\n"+"---------------------------- \n");
//		}
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
			System.out.println("NUM-TASKS" +NUM_TASKS);
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
		args = new String[1];
				
		args[0] = "input.txt";
		if(args.length != 1){
			System.out.println("Wrong number of arguments, use: java assignment2.Project <projectName>.txt");
			System.exit(0);
		}
		
		Project project = new Project(args[0]);
	//	Project project = new Project("");
	}
}
