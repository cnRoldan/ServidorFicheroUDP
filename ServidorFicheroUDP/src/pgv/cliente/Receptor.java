package pgv.cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import pgv.ackObject.ObjectACK;

public class Receptor extends Thread {

	/**
	 * Ventana de ACK que envía confirmaciones y tabla de Bytes para contenido que
	 * se recibe
	 */
	private static ArrayList<ObjectACK> listaACK = new ArrayList<>(10);;
	private static ArrayList<byte[]> listaPaquetes = new ArrayList<>(10);
	private static Integer ultimoPaquete;

	private static Integer puerto = 1200;
	private static DatagramSocket socket;
	private static DatagramPacket mensajeRecibido;
	private static ByteArrayInputStream bais;
	private static DataInputStream dis;
	private static Integer mensajeActual;
	private static Integer totalPaquetes;
	private static boolean recibido;

	/**
	 * Escribir los datos en el fichero.
	 */
	private static FileOutputStream fos;
	private static byte[] contenidoGuardado;

	/**
	 * Flujos y Datagram para enviar el mensaje que confirma la recepción
	 */
	private static ByteArrayOutputStream baos;
	private static DataOutputStream dos;
	private static DatagramPacket mensajeAck;

	@Override
	public void run() {
		super.run();
		// Se rellena la lista con valores por defecto
		for (int i = 0; i < 10; i++) {
			listaACK.add(new ObjectACK(false, i));
			listaPaquetes.add(null);

		}
		// Una vez rellenadas las listas, el último paquete es el tamaño total de la
		// lista;
		ultimoPaquete = listaACK.size();

		try {
			fos = new FileOutputStream(new File("prueba.gif"));
			socket = new DatagramSocket(puerto);
			contenidoGuardado = new byte[1008];

			mensajeRecibido = new DatagramPacket(contenidoGuardado, contenidoGuardado.length);

			while (true) {
				recibido = false;
				socket.receive(mensajeRecibido);

				// leemos el contenido del mensaje
				bais = new ByteArrayInputStream(mensajeRecibido.getData());
				dis = new DataInputStream(bais);

				// seteamos el numero de mensaje actual y el total, cuidado no leer 2 veces
				mensajeActual = dis.readInt();
				totalPaquetes = dis.readInt();

				System.out.println(mensajeActual + " / " + totalPaquetes);

				/**
				 * Se marcará como recibido solo si ha sido validado Y corresponde al mensaje
				 * actual.
				 */
				for (int i = 0; i < listaACK.size(); i++) {
					if (listaACK.get(i).isValidado() && listaACK.get(i).getNumPaquetes() == mensajeActual)
						recibido = true;
				}

				if (!recibido) {
					baos = new ByteArrayOutputStream(mensajeRecibido.getData().length - 8);
					dos = new DataOutputStream(baos);

					for (int i = 0; i < contenidoGuardado.length; i++) {
						dos.writeInt(dis.read());
					}
					dos.close();
					baos.close();

					// hay que poner en contenido, en la posicion de mensaje, lo que acabamos de
					// leer
					listaPaquetes.add(mensajeActual, baos.toByteArray());

					// como ya hemos recibido este mensaje hay que marcarlo en los ack
					for (int i = 0; i < listaACK.size(); i++) {
						if (listaACK.get(i).getNumPaquetes() == mensajeActual)
							;
						listaACK.get(i).setValidado(true);
					}
					/**
					 * Una vez que la posición del primer ACK es TRUE, podemos empezar a rodar la ventana
					 */

					if (listaACK.get(0).isValidado()) {
						for (int i = 0; i < listaACK.size(); i++) {
							fos.write(listaPaquetes.get(i));
							ultimoPaquete++;
							// Se elimina el que ya está escrito y validado
							listaACK.remove(i);
							// Se añade uno con los valores por defecto
							listaACK.add(i, new ObjectACK(false, ultimoPaquete));
							// Se elimina de la lista y se añade una tabla en su posición 
							listaPaquetes.remove(i);
							listaPaquetes.add(null);

						}
					}
				} else {
					dis.close();
					bais.close();
				}

				/**
				 * Se manda el ACK correspondiente con tamaño 5, porque tiene un byte de 6(ACK) y un entero (5)
				 */
				baos = new ByteArrayOutputStream(5);
				dos = new DataOutputStream(baos);

				dos.writeInt(6);
				dos.writeInt(mensajeActual - 1);
				dos.close();
				baos.close();

				mensajeAck = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length,
						mensajeRecibido.getAddress(), mensajeRecibido.getPort());
				socket.send(mensajeAck);
				System.out.println("Se ha mandado el ACK");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Receptor receptor = new Receptor();
		receptor.start();
	}
}
