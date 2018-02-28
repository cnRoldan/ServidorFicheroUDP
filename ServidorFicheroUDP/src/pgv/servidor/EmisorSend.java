package pgv.servidor;

import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import pgv.ackObject.ObjectACK;

public class EmisorSend extends Thread {
	ArrayList<ObjectACK> vector;
	DatagramSocket socket = null;
	DatagramPacket mensaje;
	final Integer PUERTO = 1200;
	final int INTENTOS = 3;
	final int ESPERA_ACK = 3000;
	byte[] buff = new byte[1000];
	File imagen;
	FileInputStream fis;
	@Override
	public void run() {
		super.run();
		try {
			imagen = new File("img.gif");
			fis = new FileInputStream(imagen);

			socket = new DatagramSocket();
			socket.setSoTimeout(ESPERA_ACK);
			
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
}
