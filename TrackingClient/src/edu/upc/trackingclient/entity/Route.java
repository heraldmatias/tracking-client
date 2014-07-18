package edu.upc.trackingclient.entity;

public class Route {
	private String latlong;
	private String cliente;
	private String estado;
		
	public Route(String latlong, String cliente, String estado) {
		super();
		this.latlong = latlong;
		this.cliente = cliente;
		this.estado = estado;
	}
	
	public String getLatlong() {
		return latlong;
	}
	public void setLatlong(String latlong) {
		this.latlong = latlong;
	}
	public String getCliente() {
		return cliente;
	}
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	
}
