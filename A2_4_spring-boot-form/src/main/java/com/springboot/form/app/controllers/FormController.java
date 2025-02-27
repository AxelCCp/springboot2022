package com.springboot.form.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.form.app.models.domain.Usuario;

@Controller
public class FormController {
	
	
	@GetMapping("/form")
	public String form(Model model) {
		model.addAttribute("titulo","Formulario usuarios");
		return "form";
	}
	
	@PostMapping("/form")
	public String procesar(Model model, @RequestParam(name="username") String username, 
										@RequestParam(name="password") String password, 
										@RequestParam(name="email") String email) {
		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(password);
		usuario.setEmail(email);
		model.addAttribute("titulo","Resultado Formulario");
		model.addAttribute("usuario",usuario);
		
		return "resultado";
	}
}
			