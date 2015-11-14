//Prototype implementation of Car Control
//Mandatory assignment
//Course 02158 Concurrent Programming, DTU, Fall 2015

//Hans Henrik Løvengreen    Oct 6,  2015


import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

class Gate {

	Semaphore g = new Semaphore(0);
	Semaphore e = new Semaphore(1);
	boolean isopen = false;

	public void pass() throws InterruptedException {
		g.P(); 
		g.V();
	}

	public void open() {
		try { e.P(); } catch (InterruptedException e) {}
		if (!isopen) { g.V();  isopen = true; }
		e.V();
	}

	public void close() {
		try { e.P(); } catch (InterruptedException e) {}
		if (isopen) { 
			try { g.P(); } catch (InterruptedException e) {}
			isopen = false;
		}
		e.V();
	}

}

class Car extends Thread {

	int basespeed = 100;             // Rather: degree of slowness
	int variation =  50;             // Percentage of base speed

	CarDisplayI cd;                  // GUI part
	public Pos[] PosArray; 		     // Array of positions

	int no;                          // Car number
	Pos startpos;                    // Startpositon (provided by GUI)
	Pos barpos;                      // Barrierpositon (provided by GUI)
	Color col;                       // Car  color
	Gate mygate;                     // Gate at startposition


	int speed;                       // Current car speed
	Pos curpos;                      // Current position 
	Pos newpos;                      // New position to go to
	Semaphore[][] semaphores;        // Arrays of semaphores to keep track of 'legal' positions

	public Car(int no, CarDisplayI cd, Gate g, Semaphore[][] semaphores) {

		this.no = no;
		this.cd = cd;
		this.semaphores = semaphores;
		mygate = g;
		startpos = cd.getStartPos(no);
		barpos = cd.getBarrierPos(no);  // For later use

		col = chooseColor();

		// do not change the special settings for car no. 0
		if (no==0) {
			basespeed = 0;  
			variation = 0; 
			setPriority(Thread.MAX_PRIORITY); 
		}
	}

	public synchronized void setSpeed(int speed) { 
		if (no != 0 && speed >= 0) {
			basespeed = speed;
		}
		else
			cd.println("Illegal speed settings");
	}

	public synchronized void setVariation(int var) { 
		if (no != 0 && 0 <= var && var <= 100) {
			variation = var;
		}
		else
			cd.println("Illegal variation settings");
	}

	synchronized int chooseSpeed() { 
		double factor = (1.0D+(Math.random()-0.5D)*2*variation/100);
		return (int)Math.round(factor*basespeed);
	}

	private int speed() {
		// Slow down if requested
		final int slowfactor = 3;  
		return speed * (cd.isSlow(curpos)? slowfactor : 1);
	}

	Color chooseColor() { 
		return Color.blue; // You can get any color, as longs as it's blue 
	}

	Pos nextPos(Pos pos) {
		// Get my track from display
		return cd.nextPos(no,pos);
	}

	boolean atGate(Pos pos) {
		return pos.equals(startpos);
	}

	class Alley{

		public void enter(int no){}

		public void leave(int no){}
	}

	public void run() {
		try {

			speed = chooseSpeed();
			curpos = startpos;
			cd.mark(curpos,col,no);

			while (true) { 
				sleep(speed());

				if (atGate(curpos)) { 
					mygate.pass(); 
					speed = chooseSpeed();
				}

				newpos = nextPos(curpos);

				try {
					semaphores[newpos.col][newpos.row].P();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//  Move to new position
				if(!newpos.equals(curpos)){
					cd.clear(curpos);
					cd.mark(curpos,newpos,col,no);
					sleep(speed());
					cd.clear(curpos,newpos);
					cd.mark(newpos,col,no);
				}
				semaphores[curpos.col][curpos.row].V();
				curpos = newpos;
			}

		} catch (Exception e) {
			cd.println("Exception in Car no. " + no);
			System.err.println("Exception in Car no. " + no + ":" + e);
			e.printStackTrace();
		}
	}

}

public class CarControl_step1a implements CarControlI{

	CarDisplayI cd;           // Reference to GUI
	Car[]  car;               // Cars
	Gate[] gate;              // Gates
	Semaphore[][] semaphores = new Semaphore[12][11]; // Array of semaphores to keep track of cars

	public CarControl_step1a(CarDisplayI cd) {

		for(Semaphore[] row : semaphores){
			for(int i = 0; i < row.length; i++){
				Semaphore f = new Semaphore(1);
				row[i] = f;
			}
		}

		this.cd = cd;
		car  = new  Car[9];
		gate = new Gate[9];

		for (int no = 0; no < 9; no++) {
			gate[no] = new Gate();
			car[no] = new Car(no,cd,gate[no],semaphores);
			car[no].start();
		} 
	}

	public void startCar(int no) {
		gate[no].open();
	}

	public void stopCar(int no) {
		gate[no].close();
	}

	public void barrierOn() { 
		cd.println("Barrier On not implemented in this version");
	}

	public void barrierOff() { 
		cd.println("Barrier Off not implemented in this version");
	}

	public void barrierShutDown() { 
		cd.println("Barrier shut down not implemented in this version");
		// This sleep is for illustrating how blocking affects the GUI
		// Remove when shutdown is implemented.
		try { Thread.sleep(3000); } catch (InterruptedException e) { }
		// Recommendation: 
		//   If not implemented call barrier.off() instead to make graphics consistent
	}

	public void setLimit(int k) { 
		cd.println("Setting of bridge limit not implemented in this version");
	}

	public void removeCar(int no) { 
		cd.println("Remove Car not implemented in this version");
	}

	public void restoreCar(int no) { 
		cd.println("Restore Car not implemented in this version");
	}

	/* Speed settings for testing purposes */

	public void setSpeed(int no, int speed) { 
		car[no].setSpeed(speed);
	}

	public void setVariation(int no, int var) { 
		car[no].setVariation(var);
	}

}






