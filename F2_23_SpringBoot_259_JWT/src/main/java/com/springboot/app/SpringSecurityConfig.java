package com.springboot.app;

//import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;

import com.springboot.app.auth.filter.JWTAuthenticationFilter;
import com.springboot.app.auth.filter.JWTAuthorizationFilter;
import com.springboot.app.auth.handler.LoginSuccessHandler;
import com.springboot.app.models.service.JpaUserDetailsService;



//CLASE192
//1.- EL AuthenticationManagerBuilder : ES PARA REGISTRAR A LO USUARIOS
//2.- x
//3.-ENCRIPTADOR. UN PASSWORD ENCODER.
//4.-SE OBTIENE LA INSTANCIA DEL BCryptPasswordEncoder Y SE GUARDA EN EL CONTENEDOR, YA QUE ESTÁ CON BEAN.
//5.-CON EL OBJ encoder SE PUEDEN REGISTRAR LOS USUARIOS Y SE PUEDE ENCRIPTAR LAS CONTRASEÑAS.
//SE CONSTRUYEN LOS USUARIOS Y SE PASA EL METODO passwordEncoder(). POR CADA USUARIO QUE SE REGISTRE SE PASA LA CONTRASEÑA Y SE ENCRIPTA.
//6.-SE CREAN LOS USUARIOS EN MEMORIA. SE PASAN USUARIO, CLAVE, ROLES.

//CLASE193
//7.-IMPLEMENTACION DE NUEVO MÉTODO PARA LAS AUTOTIZACIONES HTTP. PARA LAS RUTAS.

//CLASE205
//SE COMENTAN RUTAS antMatchers PARA USAR ANOTACIONES EN EL CONTROLADOR DE CLIENTE. Y TAMBN EL CONTROLADOR DE LAS FACTURAS.

					    	//PARA @Secured       //PARA @PreAuthorize
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled=true)//CLASE205 : SE HABILITAN ANOTACIONES DE SEGURIDAD   // @Secured y @PreAuthorize SON LA MISMA WEA.
@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

	// 7
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// SE ASIGNAN LAS RUTAS DE ACCESO PÚBLICO.
		http.authorizeRequests().antMatchers("/", "/css/**", "/js/**", "/images/**", "/listar", "/locale", "/listar-rest").permitAll()
		
			
		
				.anyRequest().authenticated()
				
				
				
				
				
				//CLASE259 : APLICANDO SECURIDAD CON JWT - SE QUITA SEGURIDAD CON SESIONES. //SE DESHABILITA LA PROTECCIÓN CsRF. SE DESHABILITA PQ SE VA A USAR JASON WEB TOKEN, EN VEZ DEL TOKEN DE PROTECCIÓN CONTRA CSRF.
				
				.and()
					//CLASE 261 : SE REGISTRA FILTRO. EN EL PARÁMETRO SE LE PASA EL AUTHENTICATION MANAGER
					.addFilter(new JWTAuthenticationFilter(authenticationManager()))
					//CLASE269 : SE AGERGA EL JWTAuthorizationFilter.
					.addFilter(new JWTAuthorizationFilter(authenticationManager()))   
					
				//259
				.csrf().disable()
				
				//CLASE259 :CONFIGURACIÓN DE SPRING SECURITY, SE HABILITA EN EL SESSION MANAGER. // SessionCreationPolicy.STATELESS : DESHABILITA EL USO DE SESSIONES.
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	

	@Autowired // 1
	public void configurerGlobal(AuthenticationManagerBuilder builder) throws Exception {
		
		//CLASE214-------CONFIGURACIÓN Y REGISTRO DEL JpaUserDetailsService---------------------------------------------
		builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
		//---------------------------------------------------------------------------------------------------------------
		
		
		/*
		//CLASE214 -------SE COMENTA LA AUTENTICACION CON JDBC-----------------------------------------------------------
		//SE PASA COMO INSTANCIA LA CONEXIÓN dataSource, LUEGO EL CODIFICADOR DE PASSWORD, DESPUÉS SE CONFIGURA LA CONSULTA SQL (PARA LA AUTENTICACIÓN SE USA UNA CONSULTA SQL NATIVA). 																							
		builder.jdbcAuthentication()
		.dataSource(dataSource)
		.passwordEncoder(passwordEncoder)
		//"SELECCIONA USERNAME, PASSWORD ACTIVADOS DESDE LA TABLA USERS DONDE EL USERNAME ES IGUAL AL USERNAME DEL FORMULARIO DE LOGIN".
		.usersByUsernameQuery("select username, password, enabled from users where username=?")
		//SE ARMA LA CONSULTA DE LOS ROLES...
		//SELECCIONA USERNAME DE TABLE USER, AUTHORITY DE TABLE AUTHORITIES DESDE authorities UNIDO CON users u EN a.user_id=u.id DONDE EL USERNAME DEL LOGIN ES ?
		.authoritiesByUsernameQuery("select	u.username, a.authority from authorities a inner join users u on (a.user_id=u.id) where u.username=?");
		//---------------------------------------------------------------------------------------------------------------
		*/
		
		
		/*
		//CLASE209 -------SE COMENTA LA CONFIGURACION DE USER Y PASSWORD CON AUTENTICACION EN MEMORIA--------------------
		// 4
		PasswordEncoder encoder = passwordEncoder;
		// 5
		UserBuilder users = User.builder().passwordEncoder(password -> {
			return encoder.encode(password);
		});
		// 6
		builder.inMemoryAuthentication().withUser(users.username("admin").password("12345").roles("ADMIN", "USER"))
				.withUser(users.username("andres").password("12345").roles("USER"));	
		//---------------------------------------------------------------------------------------------------------------
		*/
	}

	
	//CLASE207
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	//CLASE209 --SE INYECTA PARA LA CONEXIÓN CON LA BASE DE DATOS.
	//@Autowired
	//private DataSource dataSource;
	
	//CLASE214 --SE INYECTA PARA LA CONEXIÓN CON LA BASE DE DATOS.
	@Autowired
	private JpaUserDetailsService userDetailsService;
	
}
