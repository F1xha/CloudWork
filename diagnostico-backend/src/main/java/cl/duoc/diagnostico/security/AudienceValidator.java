package cl.duoc.diagnostico.security;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
public class AudienceValidator implements OAuth2TokenValidator<Jwt>{
 private final String audience;
 public AudienceValidator(String audience){this.audience=audience;}
 public OAuth2TokenValidatorResult validate(Jwt jwt){
  return jwt.getAudience()!=null && jwt.getAudience().contains(audience)
   ? OAuth2TokenValidatorResult.success()
   : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Audiencia incorrecta",null));
 }
}
