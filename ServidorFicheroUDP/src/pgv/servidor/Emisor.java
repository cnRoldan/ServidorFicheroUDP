package pgv.servidor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import pgv.ackObject.ObjectACK;

public class Emisor extends Thread {
	public static ArrayList<ObjectACK> vector = new ArrayList<>(10);
	private DatagramSocket socket = null;
	private DatagramPacket mensaje;
	private int numeroDePaquetes;
	public int numeroPaqueteEnviado = 0;
	private final Integer PUERTO = 1200;
	private final int ESPERA_ACK = 3000;

	public class EmisorReceptor extends Thread {
		private ByteArrayInputStream bais;
		private DataInputStream dis;

		@Override
		public void run() {
			super.run();
			try {
				socket = new DatagramSocket(PUERTO);
				mensaje = new DatagramPacket(new byte[5], 5);
				socket.setSoTimeout(ESPERA_ACK);
				while (true) {
					socket.receive(mensaje);
					// leemos el contenido del mensaje
					bais = new ByteArrayInputStream(mensaje.getData());
					dis = new DataInputStream(bais);

					if (dis.readInt() == 6) {
						// Si el entero es igual a 6, se valida.
						vector.get(dis.readInt()).setValidado(true);

						// Recorro el vector y solo borro si el validado corresponde a la primera
						// posicion, ya que el Arraylist se va rodando.

						// Semaphore.acquire();
						int i = 0;
						while (vector.get(i).isValidado() && i == 0 && i < vector.size()) {
							vector.remove(0);
							vector.add(new ObjectACK(false, numeroDePaquetes));
							i++;
						}
						// Semaphore.release();
					}
				}

			} catch (Exception e) {
			}
		}
	}

	public class EmisorEnviador extends Thread {
		private File imagen;
		private FileInputStream fis;
		private InetAddress red;
		private ByteArrayOutputStream baos;
		private DataOutputStream dos;
		private int datosBuff;
		private byte[] buff = new byte[1000];

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
					// Añadir cabecera
					dos.writeInt(numeroPaqueteEnviado);
					dos.writeInt(numeroDePaquetes);

					datosBuff = fis.read(buff);
					dos.write(buff, 0, datosBuff);

					dos.flush();
					baos.flush();

					// Solo enviará el mensaje si NO está validado en el vector.
					// TODO en este punto el hilo EmisorReceptor deberá despertar al hilo.
					for (int j = 0; j < vector.size(); j++) {
						if (!vector.get(j).isValidado()) {
							mensaje = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, red, PUERTO);
							socket.send(mensaje);
							System.out.println("Paquete " + numeroPaqueteEnviado + " enviado.");
							numeroPaqueteEnviado++;
						}
					}
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

	@Override
	public void run() {
		super.run();
		// TODO Enviar primeramente los 10 primeros paquetes, antes de inicializar los
		// hilos.
		for (int i = 0; i < vector.size(); i++) {
			vector.add(new ObjectACK(false, i));
		}

		EmisorReceptor emisorQueRecibe = new EmisorReceptor();
		emisorQueRecibe.start();

		EmisorEnviador emisorQueEnvia = new EmisorEnviador();
		emisorQueEnvia.start();
	}

	public static void main(String[] args) {
		Emisor emisor = new Emisor();
		emisor.start();
	}
}
