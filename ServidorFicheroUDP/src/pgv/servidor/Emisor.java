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

import javax.naming.CommunicationException;

public class Emisor extends Thread {
	final int INTENTOS = 3;
	final int ESPERA_ACK = 3000;
	File imagen;
	DatagramSocket socket = null;
	DatagramPacket mensaje;
	DatagramPacket ack;
	final Integer PUERTO = 1200;
	Integer numeroDePaquetes;
	InetAddress red;
	ByteArrayOutputStream baos;
	DataOutputStream dos;

	FileInputStream fis;
	byte[] buff = new byte[1000];
	// byte[] bufACK= new ;
	int intentos;
	boolean enviado;
	int datosBuff;
	
	//TODO dos hilos de EMisor, uno recibe ACK, otro envía paquete
	//TODO vector de diez posiciones, cada OBJ ACK, tendrá dos atributos, VALIDADO, NumPaquetes
	//TODO LOs dos hilos comparten el vector, una zona de exclusividad hará que no metan mano al mismo tiempo.
	
	
	@Override
	public void run() {
		// TODO enviar mediante un hilo
		super.run();
		try {
			imagen = new File("img.gif");
			fis = new FileInputStream(imagen);

			socket = new DatagramSocket();
			socket.setSoTimeout(ESPERA_ACK);
			ack = new DatagramPacket(new byte[1], 1);

			red = InetAddress.getByName("localhost");
			numeroDePaquetes = (int) Math.ceil(imagen.length() / 1000.0);
			baos = new ByteArrayOutputStream(1008);
			dos = new DataOutputStream(baos);

			for (int i = 1; i <= numeroDePaquetes; i++) {

				// Añadir cabecera
				dos.writeInt(i);
				dos.writeInt(numeroDePaquetes);

				datosBuff = fis.read(buff);
				dos.write(buff, 0, datosBuff);

				dos.flush();
				baos.flush();

				mensaje = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, red, PUERTO);
				intentos = 0;
				enviado = false;
				do {
					socket.send(mensaje);
					intentos++;
					try {
						socket.receive(ack);
						if (ack.getData()[0] == 6)
							enviado = true;
					} catch (SocketTimeoutException e) {
						if (intentos == INTENTOS)
							throw new CommunicationException("No se reciben los mensajes enviados");
					}
				} while (!enviado);
				baos.reset();
			}

			dos.close();
			baos.close();
			System.out.println("Transmisión completada...");

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

	public static void main(String[] args) {
		Emisor emisor = new Emisor();
		emisor.start();
	}
}
