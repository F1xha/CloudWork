package cl.duoc.diagnostico.security;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean @Profile("!test") JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
  @Value("${app.security.audience}") String audience){
  NimbusJwtDecoder decoder=JwtDecoders.fromIssuerLocation(issuer);
  decoder.setJwtValidator(validators(issuer,audience));
  return decoder;
 }
 public static OAuth2TokenValidator<Jwt> validators(String issuer,String audience){
  return new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(issuer),new AudienceValidator(audience));
 }
 @Bean SecurityFilterChain filter(HttpSecurity http,JwtDecoder decoder,JwtAuthConverter converter,CorsConfigurationSource cors) throws Exception{
  return http.cors(c->c.configurationSource(cors)).csrf(c->c.disable())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a
    .requestMatchers(HttpMethod.GET,"/api/public/**").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/admin/**").access(AuthorizationManagers.allOf(AuthorityAuthorizationManager.hasRole("ADMIN"),AuthorityAuthorizationManager.hasAuthority("SCOPE_recurso.read")))
    .requestMatchers("/api/admin/**").access(AuthorizationManagers.allOf(AuthorityAuthorizationManager.hasRole("ADMIN"),AuthorityAuthorizationManager.hasAuthority("SCOPE_recurso.write")))
    .requestMatchers(HttpMethod.GET,"/api/incidencias","/api/incidencias/**","/api/me").hasAuthority("SCOPE_recurso.read")
    .requestMatchers(HttpMethod.POST,"/api/incidencias").hasAuthority("SCOPE_recurso.write")
    .requestMatchers(HttpMethod.PUT,"/api/incidencias/*").hasAuthority("SCOPE_recurso.write")
    .requestMatchers(HttpMethod.DELETE,"/api/incidencias/*").access(AuthorizationManagers.allOf(AuthorityAuthorizationManager.hasRole("ADMIN"),AuthorityAuthorizationManager.hasAuthority("SCOPE_recurso.write")))
    .anyRequest().denyAll())
   .oauth2ResourceServer(o->o.jwt(j->j.decoder(decoder).jwtAuthenticationConverter(converter)))
   .build();
 }
 @Bean CorsConfigurationSource cors(@Value("${app.cors.allowed-origins}") String origins){
  CorsConfiguration c=new CorsConfiguration();
  c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).toList());
  c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
  c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
  c.setExposedHeaders(List.of("Location","WWW-Authenticate"));
  var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
 }
}
