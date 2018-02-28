package pgv.ackObject;

public class ObjectACK {
	private boolean validado;
	private int numPaquete;
	
	public ObjectACK(boolean validado, int numPaquete) {
		this.validado = validado;
		this.numPaquete = numPaquete;
	}
	public boolean isValidado() {
		return validado;
	}
	public void setValidado(boolean validado) {
		this.validado = validado;
	}
	public int getNumPaquetes() {
		return numPaquete;
	}
	public void setNumPaquetes(int numPaquetes) {
		this.numPaquete = numPaquetes;
	}
}
