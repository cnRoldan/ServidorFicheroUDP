package pgv.servidor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.naming.CommunicationException;

import pgv.ackObject.ObjectACK;

public class EmisorSend extends Thread {
	private ArrayList<ObjectACK> vector;
	private DatagramSocket socket = null;
	private DatagramPacket mensaje,ack;
	private final Integer PUERTO = 1200;
	private final int INTENTOS = 3;
	private final int ESPERA_ACK = 3000;
	private byte[] buff = new byte[1000];
	private int numeroDePaquetes,paqueteInicial, paqueteFinal;
	private File imagen;
	private FileInputStream fis;
	private InetAddress red;
	private ByteArrayOutputStream baos;
	private DataOutputStream dos;
	private int datosBuff;


	
	@Override
	public void run() {
		super.run();
		try {
			imagen = new File("img.gif");
			fis = new FileInputStream(imagen);

			socket = new DatagramSocket();
			socket.setSoTimeout(ESPERA_ACK);
						
			red = InetAddress.getByName("localhost");
			numeroDePaquetes = (int) Math.ceil(imagen.length() / 1000.0);
			
			baos = new ByteArrayOutputStream(1008);
			dos = new DataOutputStream(baos);
			

			
			for (int i = 0; i < numeroDePaquetes; i++) {			
				for (int j = 0; j < 10; j++) {
					ObjectACK obj = new ObjectACK(false, i);
					vector.add(obj);
				}
				// Añadir cabecera
				dos.writeInt(i);
				dos.writeInt(numeroDePaquetes);

				datosBuff = fis.read(buff);
				dos.write(buff, 0, datosBuff);

				dos.flush();
				baos.flush();

				
				mensaje = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, red, PUERTO);
				socket.send(mensaje);
				baos.reset();
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
	}
}
