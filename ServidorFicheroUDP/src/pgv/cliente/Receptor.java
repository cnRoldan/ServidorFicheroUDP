package pgv.cliente;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Receptor {
	
	//TODO un hilo, escucha la cabecera del mensaje (num mensaje), 
	//TODO implementar clase Queue.
	
	public static void main(String[] args) {
		final Integer PUERTO = 1200;
		DatagramSocket socket = null;
		DatagramPacket mensaje;
		DatagramPacket ack;
		ByteArrayInputStream bais;
		DataInputStream dis;
		
		FileOutputStream fos=null;
		byte[] contenido = new byte[1008];

		byte[] buffFile=new byte[1000];
		byte[] bufACK= {6};
		int datosBuff;
		int numMensajeActual, ultimoGuardado=-1, totMensajes;
		try {
			fos = new FileOutputStream(new File("prueba.gif"));
			socket = new DatagramSocket(PUERTO);
			socket.setSoTimeout(10000);
			
			mensaje = new DatagramPacket(contenido, contenido.length);
			
			do {
				socket.receive(mensaje);
				//TODO, sacar el numMnesaje del paquete, y contruye el ACK (6b + numPaquete)
				ack=new DatagramPacket(bufACK, 1, mensaje.getAddress(),mensaje.getPort());
				socket.send(ack);
				
				bais = new ByteArrayInputStream(mensaje.getData());
				dis = new DataInputStream(bais);
				numMensajeActual=dis.readInt();
				//TODO aquí se envía el ACK.
				totMensajes=dis.readInt();
				System.out.println((numMensajeActual) + "/" + totMensajes);
				
				
				if(numMensajeActual!=ultimoGuardado) {
					datosBuff=dis.read(buffFile);
					ultimoGuardado=numMensajeActual;
					fos.write(buffFile, 0, datosBuff);
				}else
					System.out.println("*");
				
				
				dis.close();
				bais.close();
			} while(ultimoGuardado<totMensajes);
			System.out.println("Recepción de mensaje completada...");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			System.out.println("Se acabo el tiempo de escucha");
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(fos!=null)
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			socket.close();
		}
	}
}
