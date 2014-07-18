package edu.upc.trackingclient.entity;

public class Conductor {
	private int id;
	private String nombres;
	private String celular;
	private String dni;
	
	public Conductor(){
		super();
	}
	
	public Conductor(int id, String nombres, String celular, String dni) {
		super();
		this.id = id;
		this.nombres = nombres;
		this.celular = celular;
		this.dni = dni;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		this.nombres = nombres;
	}
	public String getCelular() {
		return celular;
	}
	public void setCelular(String celular) {
		this.celular = celular;
	}
	public String getDni() {
		return dni;
	}
	public void setDni(String dni) {
		this.dni = dni;
	}
	
	
}
