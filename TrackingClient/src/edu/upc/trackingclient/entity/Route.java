package edu.upc.trackingclient.entity;

public class Route {
	private String latlong;
	private String cliente;
	private String estado;
	private Integer ruta;
	private Integer conductor;
		
	
	
	public Route(String latlong, String cliente, String estado, Integer ruta,
			Integer conductor) {
		super();
		this.latlong = latlong;
		this.cliente = cliente;
		this.estado = estado;
		this.ruta = ruta;
		this.conductor = conductor;
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
	public Integer getRuta() {
		return ruta;
	}
	public void setRuta(Integer ruta) {
		this.ruta = ruta;
	}
	public Integer getConductor() {
		return conductor;
	}
	public void setConductor(Integer conductor) {
		this.conductor = conductor;
	}
	
	
}
