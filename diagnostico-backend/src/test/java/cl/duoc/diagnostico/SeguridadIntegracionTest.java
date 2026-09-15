package cl.duoc.diagnostico;
import cl.duoc.diagnostico.security.SecurityConfig;
import cl.duoc.diagnostico.repository.*;
import cl.duoc.diagnostico.model.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.*;
import java.security.*;
import java.security.interfaces.*;
import java.time.Instant;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
@Import(SeguridadIntegracionTest.LocalJwt.class)
class SeguridadIntegracionTest {
 static final String ISSUER="https://login.microsoftonline.com/test-tenant/v2.0";
 static final String AUD="test-api";
 static final KeyPair KEYS=keys();
 static KeyPair keys(){try{var g=KeyPairGenerator.getInstance("RSA");g.initialize(2048);return g.generateKeyPair();}catch(Exception e){throw new IllegalStateException(e);}}
 @TestConfiguration static class LocalJwt {
  @Bean JwtDecoder decoder(){var d=NimbusJwtDecoder.withPublicKey((RSAPublicKey)KEYS.getPublic()).build();d.setJwtValidator(SecurityConfig.validators(ISSUER,AUD));return d;}
 }
 @Autowired MockMvc mvc;
 @Autowired IncidenciaRepository incidencias;
 @Autowired CategoriaRepository categorias;
 long categoriaId;
 @BeforeEach void preparar(){incidencias.deleteAll();categorias.deleteAll();categoriaId=categorias.save(new Categoria("Hardware")).getId();}
 String token(String role,String scopes,String issuer,String aud,long seconds,KeyPair keys)throws Exception{
  var claims=new JWTClaimsSet.Builder().issuer(issuer).audience(aud).subject("usuario-test")
   .issueTime(Date.from(Instant.now().minusSeconds(30))).expirationTime(Date.from(Instant.now().plusSeconds(seconds)))
   .claim("preferred_username","test@example.invalid").claim("roles",List.of(role)).claim("scp",scopes).build();
  var jwt=new SignedJWT(new JWSHeader(JWSAlgorithm.RS256),claims);jwt.sign(new RSASSASigner((RSAPrivateKey)keys.getPrivate()));return jwt.serialize();
 }
 String token(String role,String scopes)throws Exception{return token(role,scopes,ISSUER,AUD,300,KEYS);}
 String payload(){return "{\"titulo\":\"Impresora sin conexión\",\"descripcion\":\"Revisar red\",\"categoriaId\":"+categoriaId+",\"prioridad\":\"MEDIA\"}";}
 @Test void publico200()throws Exception{mvc.perform(get("/api/public/catalogo")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Hardware"));}
 @Test void protegidoSinToken401()throws Exception{mvc.perform(get("/api/incidencias")).andExpect(status().isUnauthorized());}
 @Test void tokenMalformado401()throws Exception{mvc.perform(get("/api/incidencias").header("Authorization","Bearer invalido")).andExpect(status().isUnauthorized());}
 @Test void expirado401()throws Exception{mvc.perform(get("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.read",ISSUER,AUD,-120,KEYS))).andExpect(status().isUnauthorized());}
 @Test void firmaIncorrecta401()throws Exception{mvc.perform(get("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.read",ISSUER,AUD,300,keys()))).andExpect(status().isUnauthorized());}
 @Test void issuerIncorrecto401()throws Exception{mvc.perform(get("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.read","https://otro.invalid",AUD,300,KEYS))).andExpect(status().isUnauthorized());}
 @Test void audienciaIncorrecta401()throws Exception{mvc.perform(get("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.read",ISSUER,"otra-api",300,KEYS))).andExpect(status().isUnauthorized());}
 @Test void userAdmin403()throws Exception{mvc.perform(get("/api/admin/reporte").header("Authorization","Bearer "+token("User","recurso.read"))).andExpect(status().isForbidden());}
 @Test void admin200()throws Exception{mvc.perform(get("/api/admin/reporte").header("Authorization","Bearer "+token("Admin","recurso.read"))).andExpect(status().isOk());}
 @Test void adminSinScope403()throws Exception{mvc.perform(get("/api/admin/reporte").header("Authorization","Bearer "+token("Admin","recurso.write"))).andExpect(status().isForbidden());}
 @Test void lecturaNoPermiteEscribir403()throws Exception{mvc.perform(post("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.read")).contentType("application/json").content(payload())).andExpect(status().isForbidden());}
 @Test void creaRelacion201YConsulta200()throws Exception{
  mvc.perform(post("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.read recurso.write")).contentType("application/json").content(payload())).andExpect(status().isCreated()).andExpect(jsonPath("$.categoriaId").value(categoriaId)).andExpect(jsonPath("$.estado").value("ABIERTA"));
  assertThat(incidencias.count()).isEqualTo(1);
  mvc.perform(get("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.read"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].categoria").value("Hardware"));
 }
 @Test void cuerpoInvalido400()throws Exception{mvc.perform(post("/api/incidencias").header("Authorization","Bearer "+token("User","recurso.write")).contentType("application/json").content("{}" )).andExpect(status().isBadRequest());}
 @Test void userNoElimina403()throws Exception{mvc.perform(delete("/api/incidencias/123").header("Authorization","Bearer "+token("User","recurso.write"))).andExpect(status().isForbidden());}
 @Test void corsPreflight()throws Exception{mvc.perform(options("/api/incidencias").header("Origin","http://localhost:4200").header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","authorization,content-type")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:4200"));}
 @Test void meClaimsSinTokenCompleto()throws Exception{mvc.perform(get("/api/me").header("Authorization","Bearer "+token("User","recurso.read"))).andExpect(status().isOk()).andExpect(jsonPath("$.roles[0]").value("User")).andExpect(jsonPath("$.scp").value("recurso.read")).andExpect(jsonPath("$.access_token").doesNotExist());}
}
