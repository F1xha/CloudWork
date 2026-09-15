package cl.duoc.diagnostico.security;
import java.util.*;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
@Component
public class JwtAuthConverter implements Converter<Jwt,AbstractAuthenticationToken>{
 public AbstractAuthenticationToken convert(Jwt jwt){
  Set<GrantedAuthority> authorities=new HashSet<>();
  List<String> roles=jwt.getClaimAsStringList("roles");
  if(roles!=null) roles.forEach(r->authorities.add(new SimpleGrantedAuthority("ROLE_"+r.toUpperCase(Locale.ROOT))));
  String scp=jwt.getClaimAsString("scp");
  if(scp!=null && !scp.isBlank()) Arrays.stream(scp.trim().split("\\s+")).forEach(s->authorities.add(new SimpleGrantedAuthority("SCOPE_"+s)));
  String name=jwt.getClaimAsString("preferred_username");
  return new JwtAuthenticationToken(jwt,authorities,name==null?jwt.getSubject():name);
 }
}
