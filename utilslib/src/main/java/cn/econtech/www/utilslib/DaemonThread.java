package cn.econtech.www.utilslib;


public class DaemonThread extends Thread {
	public interface Process {
		void process() ;
	}
	private volatile boolean threadSuspended = false;
	private volatile boolean threadStopped = false;
	private Process process = null;
	public DaemonThread(Process process) {
		this.process = process;
	}
	
	@Override
	public void run() {
		
		// Keep listening to the InputStream until an exception occurs.
	        while (!threadStopped) {
	        	process.process();
	        	try {
	        		if (threadSuspended) {
	        			synchronized (this) {
	        				while (threadSuspended && !threadStopped) {
	        					wait();
		        			}
		        		}
		        	}
		        }
		        catch (InterruptedException e) {
		        	//ignore interruption
		        }
	        }
	}
	
	public synchronized void startThread() {
		threadSuspended = false;
		notify();
	}

	public synchronized void pauseThread() {
		threadSuspended = true;
		notify();
	}

	public synchronized void stopThread() {
		threadStopped = true;
		notify();
	}
	
}
