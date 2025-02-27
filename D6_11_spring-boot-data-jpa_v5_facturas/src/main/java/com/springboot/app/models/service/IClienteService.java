package com.springboot.app.models.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.springboot.app.models.entity.Cliente;
import com.springboot.app.models.entity.Factura;
import com.springboot.app.models.entity.Producto;

public interface IClienteService {

	public List<Cliente>findAll();
	public void save(Cliente cliente);
	public Cliente findOne(Long id);
	public void delete(Long id);
	
	public Page<Cliente>findAll(Pageable pageable);
	
	public  List<Producto>findByNombre(String term);
	
	public void saveFactura(Factura factura);
	
	//METODO PARA BUSCAR PRODUCTO POR EL ID.
	public Producto findProductoById(Long id);
		
	public Factura findFacturaById(Long id); 
}
