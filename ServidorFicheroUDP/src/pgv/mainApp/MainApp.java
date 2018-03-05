package pgv.mainApp;

import java.util.ArrayList;

import pgv.ackObject.ObjectACK;
import pgv.cliente.Receptor;
import pgv.servidor.Emisor;

public class MainApp {
	//ArrayList a la que accede la clase Emisor
	public static ArrayList<ObjectACK> vector = new ArrayList<>(10);


	public static void main(String[] args) {
		//SE RELLENA LA PRIMERA VEZ
		for (int i = 0; i < vector.size(); i++) {
			vector.add(new ObjectACK(false, i));
		}
		
		Receptor receptor = new Receptor();
		receptor.start();
		
		Emisor emisor = new Emisor();
		emisor.start();
		
	}
	

}
