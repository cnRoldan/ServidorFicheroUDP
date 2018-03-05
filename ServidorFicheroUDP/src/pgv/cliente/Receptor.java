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
	private static byte[] contenido;


	/**
	 * Flujos y Datagram para enviar el mensaje que confirma la recepción
	 */
	private static ByteArrayOutputStream baos;
	private static DataOutputStream dos;
	private static DatagramPacket mensajeAck;

	@Override
	public void run() {
		super.run();
		// relleno las listas con los valores por defecto
		for (int i = 0; i < 10; i++) {
			listaACK.add(new ObjectACK(false, i));
			listaPaquetes.add(null);

		}
		// Una vez rellenadas las listas, el último paquete es el tamaño total de la lista;
		ultimoPaquete = listaACK.size();

		try {
			fos = new FileOutputStream(new File("prueba.gif"));
			socket = new DatagramSocket(puerto);
			contenido = new byte[1008];

			// recibiremos X datagramas diferentes del tamaño dicho, menos el ultimo
			// probablemente
			mensajeRecibido = new DatagramPacket(contenido, contenido.length);

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
				 * Se marcará como recibido solo si ha sido validado Y corresponde al mensaje actual.
				 */
				for (int i = 0; i < listaACK.size(); i++) {
					if (listaACK.get(i).isValidado() && listaACK.get(i).getNumPaquetes() == mensajeActual)
						recibido = true;
				}
				

				if (!recibido) {
					baos = new ByteArrayOutputStream(mensajeRecibido.getData().length - 8);
					dos = new DataOutputStream(baos);

					for (int i = 0; i < contenido.length; i++) {
						dos.writeInt(dis.read());
					}
					dos.close();
					baos.close();

					// hay que poner en contenido, en la posicion de mensaje, lo que acabamos de
					// leer
					listaPaquetes.add(mensajeActual, baos.toByteArray());

					// como ya hemos recibido este mensaje hay que marcarlo en los ack
					for (int i = 0; i < listaACK.size(); i++) {
						if (listaACK.get(i).getNumPaquetes() == mensajeActual);
							listaACK.get(i).setValidado(true);
					}
					/*
					 * si el ack de la primera posicion es true, ya podemos buscar consecutivos y
					 * escribirlos en el fichero y a la vez que escribimos podemos rodar la ventana
					 */
					
					if (listaACK.get(0).isValidado()) {
						for (int i = 0; i < listaACK.size() && listaACK.get(i).isValidado(); i++) {
							fos.write(listaPaquetes.get(i));
							ultimoPaquete++;
							// borro este que ya he escrito y validado
							listaACK.remove(i);
							// añado uno nuevo con los valores por defecto donde estaba el que acabo de
							// escribir en el fichero
							listaACK.add(i, new ObjectACK(false, ultimoPaquete));
							// tambien hay que quitarlo del contenido
							listaPaquetes.remove(i);
							listaPaquetes.add(null);

						}
					}
				} else {
					dis.close();
					bais.close();
				}

				// Mandar el ack correspondiente
				// tam 5 porque tenemos que mandar el byte de 6 y un entero 4+1
				baos = new ByteArrayOutputStream(5);
				dos = new DataOutputStream(baos);

				dos.writeInt(6);
				dos.writeInt(mensajeActual - 1);
				dos.close();
				baos.close();

				mensajeAck = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length, mensajeRecibido.getAddress(),
						mensajeRecibido.getPort());
				socket.send(mensajeAck);
				System.out.println("he mandado el ack");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}



	public static void main(String[] args) {

	}
}
