package hello;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.jayway.restassured.RestAssured;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import redis.clients.jedis.Response;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getHello() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Greetings from Spring Boot!")));
    }
    
    @Test
    public void testCreateAccesssToken()
    {
    	SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        String secret = "SECRET";
        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secret);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        String id= "plan_0215";
        String subject = "ACCESS_TOKEN";
        String issuer= "SUPER_ADMIN";
        
		//Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                                    .setIssuedAt(now)
                                    .setSubject(subject)
                                    .claim("user", "unique-id-of-user")
                                    .setIssuer(issuer)
                                    .signWith(signatureAlgorithm, signingKey);

        // builder.setExpiration(getNextYearDate());
//        int ttlMillis=0;
//		//if it has been specified, let's add the expiration
//        if (ttlMillis >= 0) {
//        long expMillis = nowMillis + ttlMillis;
//            Date exp = new Date(expMillis);
//            builder.setExpiration(exp);
//        }
        System.out.println("builder " + builder.compact());
    }
    
    @Test
    public void testValidateToken()
    {
    	String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJwbGFuXzAyMTUiLCJpYXQiOjE0NzgwNDMyNzksInN1YiI6IkFDQ0VTU19UT0tFTiIsInVzZXIiOiJ1bmlxdWUtaWQtb2YtdXNlciIsImlzcyI6IlNVUEVSX0FETUlOIn0.ek919bn8UqV9WWMfIZu25VBMtbavAQZEDL-LiIzVZDE";
    	String secret = "SECRET";
    	
    	Claims claims = Jwts.parser()         
    		       .setSigningKey(DatatypeConverter.parseBase64Binary(secret))
    		       .parseClaimsJws(token).getBody();
    		    System.out.println("ID: " + claims.getId());
    		    System.out.println("Subject: " + claims.getSubject());
    		    System.out.println("Issuer: " + claims.getIssuer());
    		    //System.out.println("Expiration: " + claims.getExpiration());
    		    System.out.println("User:" + claims.get("user"));
    }
    
    
    private Date getNextYearDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        return cal.getTime();
    }
    
    
/*    @Test
    public void givenResourceExists_whenRetrievingResource_thenEtagIsAlsoReturned() {

    String uriOfResource = createAsUri();
    
    Response findOneResponse = (Response) RestAssured.given().header("Accept","application/json").get(uriOfResource);
   
    }*/
    
}
