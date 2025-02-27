package com.springboot.app.controllers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.app.models.entity.Cliente;
import com.springboot.app.models.service.IClienteService;
import com.springboot.app.util.paginator.PageRender;


//1.- @RequestParam: PARA OBTENER LA URL DE LA PÁGINA ACTUAL. SE USA EN ESTE CASO PARA USAR EL PAGINADOR.
	// int page: ES EL NOMBRE DEL PARÁMETRO.
//2.- SE CREA OBJ PAGEABLE Y SE DEFINEN 5 ELEMENTOS POR PÁGINA.
//3.- SE HACE LA INVOCACIÓN AL SERVICE, MÉTODO FINDALL() Y SE LE PASA EL pageRequest CON LA CANTIDAD DE CLIENTES A MOSTRAR POR PAGINA. POR LO TANTO "clientes" SE CONBIERTE EN UNA LISTA PAGINADA.

@Controller
@SessionAttributes("cliente")
public class ClienteController {
	
	//1
	@RequestMapping(value="/listar", method=RequestMethod.GET)
	public String listar(@RequestParam(name="page", defaultValue="0") int page, Model model) {
		
		//2
		Pageable pageRequest = PageRequest.of(page, 5);
		//3
		Page<Cliente>clientes = clienteService.findAll(pageRequest);
		
		PageRender<Cliente>pageRender = new PageRender("/listar", clientes);
		
		model.addAttribute("titulo", "listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		
		return "listar";
	}
	
	@RequestMapping(value="/form")
	public String crear(Map<String,Object>model) {
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", "formulario de cliente");
		return "form";
	}
	
	
	@RequestMapping(value="/form",method=RequestMethod.POST)
	public String guerdar(@Valid Cliente cliente, BindingResult result, Model model, @RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {
		if(result.hasErrors()) {
			model.addAttribute("titulo","Formulario de cliente");
			return "form";
		}
		
		
		if(!foto.isEmpty()) {
			//PARA REEMPLAZAR LA FOTO DEL CLIENTE
			if(cliente.getId() != null && cliente.getId()>0 && cliente.getFoto() != null && cliente.getFoto().length()>0) {
				
				Path rootPath = Paths.get("uploads").resolve(cliente.getFoto()).toAbsolutePath();
				File archivo = rootPath.toFile();
				if(archivo.exists() && archivo.canRead()) {
					archivo.delete();
				}
			}
			
			
			
			
			//NOMBRE ÚNICO PARA CADA FOTO.
			String uniqueFilename = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
			Path rootPath = Paths.get("uploads").resolve(uniqueFilename);
			Path rootAbsolutePath = rootPath.toAbsolutePath();
			log.info("rootPath: " + rootPath);
			log.info("rootAbsolutePath: " + rootAbsolutePath);
			try {
				
				Files.copy(foto.getInputStream(), rootAbsolutePath);
				
				flash.addFlashAttribute("info","Ha subido correctamente la foto: " + uniqueFilename);
				//SE PASA EL NOMBRE DE LA FOTO AL CLEINTE
				cliente.setFoto(uniqueFilename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
		}
		
		
		String mensajeFlash = (cliente.getId() != null)? "Cliente editado con éxito." : "Cliente agregado con éxito.";
		
		clienteService.save(cliente);
		status.setComplete();
		flash.addFlashAttribute("success",mensajeFlash);
		return "redirect:listar";
	}
	
	
	@RequestMapping(value="/form/{id}") 
	public String editar(@PathVariable Long id, Map<String,Object>model, RedirectAttributes flash) {
		Cliente cliente = null;
		
		if(id>0) {
			cliente = clienteService.findOne(id);
			if(cliente == null) {
				flash.addFlashAttribute("error","El id de cliente no existe en la bbdd!");
				return "redirect:/listar";
			}
		}else {
			flash.addFlashAttribute("error","El id de cliente no puede ser 0!");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Editar cliente");
		return "form";
	}
	
	@RequestMapping(value="/eliminar/{id}")
	public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
		if(id > 0) {
			
			Cliente cliente = clienteService.findOne(id);
			
			clienteService.delete(id);
			flash.addFlashAttribute("success","Cliente eliminado con éxito.");
			
			
			Path rootPath = Paths.get("uploads").resolve(cliente.getFoto()).toAbsolutePath();
			File archivo = rootPath.toFile();
			if(archivo.exists() && archivo.canRead()) {
				if(archivo.delete()) {
					flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con exito!");
				}
			}
		}
		
		return "redirect:/listar";
	}
	
	
	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value="id")Long id, Map<String,Object>model, RedirectAttributes flash) {
		//Cliente cliente = clienteService.findOne(id);
		//CLASE186
		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if(cliente==null) {
			flash.addFlashAttribute("error","El cliente no existe en la BBDD");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Detalle cliente: " + cliente.getNombre());
		return "ver";
	}
	
	
	@GetMapping("/uploads/{filename:.+}")  //ESTA EXPRESIÓN  PERMITE QUE SPRING NO TRUNQUE LA EXTENSIÓN DEL ARCHIVO.
	public ResponseEntity<Resource>verFoto(@PathVariable String filename){
		Path pathFoto = Paths.get("uploads").resolve(filename).toAbsolutePath(); // CONCATENA PARA FORMAR EL PATH COMPLETO DESDE C: , JUNTANDO EL NOMBRE DEL FOLDER EN LA RAIZ + EL NOMBRE DEL ARCHIVO.
		log.info("pathFoto: " + pathFoto);
		Resource recurso = null;
		try {
			recurso = new UrlResource(pathFoto.toUri());
			if(!recurso.exists() || !recurso.isReadable()) {
				throw new RuntimeException("Error, no se puede cargar la imagen: " + pathFoto.toString());
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"").body(recurso);
	}
	
	
	@Autowired
	private IClienteService clienteService; 
	
	private final Logger log = LoggerFactory.getLogger(getClass());

}
