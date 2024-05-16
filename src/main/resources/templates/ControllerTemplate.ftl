package ${packageName};

<#list importsList as import>${import}</#list>
import com.blumeglobal.JsonFileReaderUtil;
import com.blumeglobal.AbstractIntegrationTest;
import com.blumeglobal.ValidationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import static org.junit.Assert.*;

import com.blumeglobal.core.security.token.config.JwtBean;
import com.blumeglobal.core.security.token.config.JwtPrivilegeBean;
import com.blumeglobal.core.security.token.context.JwtContext;
import com.blumeglobal.core.security.token.context.JwtContextHolder;

import java.io.File;
import java.util.*;

import org.mockito.*;

@Profile("test")
public class ${className}Test extends AbstractIntegrationTest {

    <#if isConstructorPresent??>
    <#if isConstructorPresent>
    @Autowired
    private ${className} ${classInstance};
    <#else>
    <#list fields as field>
    @Mock
    private ${field} ${field?lower_case};

    </#list>
    @InjectMocks
    @Autowired
    private ${className} ${classInstance};
    </#if>
    </#if>

    @Before
    public void setUpMongoDb() {

    }


<#list methodsList as method>
 <#if method.hasRequestBody>
  <#if method.isApiRequestPresent>
   <#if method.isApiResponsePresent>

    @Test
    public void ${method.methodName}_${method.methodNumber}Test() throws Exception {

    <#list method.arguments as argument>
    <#if argument.dataType == "ApiRequest">
        String pathToRequestJson = "${method.pathToRequestJson}";
        List<${method.requestEntityType}> requestObjects = JsonFileReaderUtil.readRequestEntities(pathToRequestJson,${method.requestEntityType}.class);
        ApiRequest<${method.requestEntityType}> apiRequest = new ApiRequest<>();
    <#else>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
    </#if>
    </#list>

        apiRequest.setRequest(requestObjects);
        apiRequest.setCorrelationId(correlationId);

      <#if pathToJwtJson??>
        JwtContext jwtContext=new JwtContext();
        String pathToJwtJson = "${method.pathToJwtJson}";
        JwtBean jwtBean = JsonFileReaderUtil.getJwtFromJsonFile(pathToJwtJson) ;
        jwtContext.setJwtBean(jwtBean);
        JwtContextHolder.setJwtContext(jwtContext);
      </#if>

        ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments?sort_by('argumentOrder') as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
        assertNotNull(actualResponse);
        ${method.returnValue} body = actualResponse.getBody();
        assertNotNull(body);

      <#if method.responseHeaders?size != 0>
      <#list method.responseHeaders?keys as responseHeaderKey>
        assertEquals(${method.responseHeaders[responseHeaderKey]}, actualResponse.get${responseHeaderKey?cap_first}());
      </#list>
      </#if>

        List<${method.responseEntityType}> actualList = body.getResults();
        assertNotNull(actualList);
    <#if method.hasPathVariable??>
    <#if method.hasPathVariable>
        for (${method.responseEntityType} dto : actualList) {
    <#list method.arguments as argument>
    <#if argument.annotationType == "PathVariable">
            assertEquals(${argument.name}, dto.get${argument.name?cap_first}());
    </#if>
    </#list>
        }
    </#if>
    </#if>
    <#assign Properties = method.assertionParameters>

    <#if method.hasRequestBody>
        String pathToResponseJson = "${method.pathToResponseJson}";
        ApiResponse<${method.responseEntityType}> expectedResponse = JsonFileReaderUtil.readResponseEntities(pathToResponseJson,${method.responseEntityType}.class);
        List<${method.responseEntityType}> expectedList = expectedResponse.getResults();
        assertNotNull(expectedList);
        assertEquals(expectedList.size(), actualList.size());

    <#if method.responseHeadersAndValidationChecks?size != 0>
    <#list method.responseHeadersAndValidationChecks?keys as headerKey>
        <#if method.responseHeadersAndValidationChecks[headerKey]?size != 0>
        <#list method.responseHeadersAndValidationChecks[headerKey] as validation>
        assertTrue(ValidationUtil.validate(body.get${headerKey?cap_first}(), <#if validation == "RANGE"> 0, 0 <#else>"${validation}" </#if>));
        </#list>
        </#if>
        assertEquals(expectedResponse.get${headerKey?cap_first}(), body.get${headerKey?cap_first}());
    </#list>
    </#if>

        for (int i = 0; i < expectedList.size(); i++) {
    <#list Properties as property>
           assertEquals(expectedList.get(i).get${property?cap_first}(),actualList.get(i).get${property?cap_first}());
    </#list>
    <#if method.responseResultHeadersAndValidationChecks?size != 0>
    <#list method.responseResultHeadersAndValidationChecks?keys as resultHeaderKey>
       <#if method.responseResultHeadersAndValidationChecks[resultHeaderKey]?size != 0>
       <#list method.responseResultHeadersAndValidationChecks[resultHeaderKey] as validation>
           assertTrue(ValidationUtil.validate(body.getResults().get(i).get${resultHeaderKey?cap_first}(), <#if validation == "RANGE"> 0, 0 <#else>"${validation}" </#if>));
       </#list>
       </#if>
           assertEquals(expectedResponse.getResults().get(i).get${resultHeaderKey?cap_first}(), body.getResults().get(i).get${resultHeaderKey?cap_first}());
    </#list>
    </#if>
        }
    </#if>

    <#if pathToJwtJson??>
       JwtContextHolder.removeJwtContext();
    </#if>
    }
   <#else>
    @Test
    public void ${method.methodName}_${method.methodNumber}Test() throws Exception {

    <#list method.arguments as argument>
    <#if argument.dataType == "ApiRequest">
        String pathToRequestJson = "${method.pathToRequestJson}";
    <#else>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
    </#if>
    </#list>

      <#if pathToJwtJson??>
        JwtContext jwtContext=new JwtContext();
        String pathToJwtJson = "${method.pathToJwtJson}";
        JwtBean jwtBean = JsonFileReaderUtil.getJwtFromJsonFile(pathToJwtJson) ;
        jwtContext.setJwtBean(jwtBean);
        JwtContextHolder.setJwtContext(jwtContext);
      </#if>

        ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments?sort_by('argumentOrder') as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
        assertNotNull(actualResponse);
        ${method.returnValue} body = actualResponse.getBody();
        assertNotNull(body);
    }
   </#if>
  <#else>
   <#if method.isApiResponsePresent>
    @Test
    public void ${method.methodName}_${method.methodNumber}Test() throws Exception {

    <#list method.arguments as argument>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
    </#list>

    <#if method.isApiResponsePresent??>
    <#if method.isApiResponsePresent>
        String pathToResponseJson = "${method.pathToResponseJson}";
    </#if>
    </#if>

      <#if pathToJwtJson??>
        JwtContext jwtContext=new JwtContext();
        String pathToJwtJson = "${method.pathToJwtJson}";
        JwtBean jwtBean = JsonFileReaderUtil.getJwtFromJsonFile(pathToJwtJson) ;
        jwtContext.setJwtBean(jwtBean);
        JwtContextHolder.setJwtContext(jwtContext);
      </#if>

        ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments?sort_by('argumentOrder') as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
        assertNotNull(actualResponse);
        ${method.returnValue} body = actualResponse.getBody();
        assertNotNull(body);

      <#if method.responseHeaders?size != 0>
      <#list method.responseHeaders?keys as responseHeaderKey>
        assertEquals(${method.responseHeaders[responseHeaderKey]}, actualResponse.get${responseHeaderKey?cap_first}());
      </#list>
      </#if>

        List<${method.responseEntityType}> actualList = body.getResults();
        assertNotNull(actualList);

    <#if method.hasPathVariable??>
    <#if method.hasPathVariable>
        for (${method.responseEntityType} dto : actualList) {
    <#list method.arguments as argument>
    <#if argument.annotationType == "PathVariable">
            assertEquals(${argument.name}, dto.get${argument.name?cap_first}());
    </#if>
    </#list>
        }
    </#if>
    </#if>

        <#assign Properties = method.assertionParameters>

        ApiResponse<${method.responseEntityType}> expectedResponse = JsonFileReaderUtil.readResponseEntities(pathToResponseJson,${method.responseEntityType}.class);
        List<${method.responseEntityType}> expectedList = expectedResponse.getResults();
        assertNotNull(expectedList);
        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
    <#list Properties as property>
           assertEquals(expectedList.get(i).get${property?cap_first}(),actualList.get(i).get${property?cap_first}());
    </#list>
    <#if method.responseResultHeadersAndValidationChecks?size != 0>
    <#list method.responseResultHeadersAndValidationChecks?keys as resultHeaderKey>
       <#if method.responseResultHeadersAndValidationChecks[resultHeaderKey]?size != 0>
       <#list method.responseResultHeadersAndValidationChecks[resultHeaderKey] as validation>
           assertTrue(ValidationUtil.validate(body.getResults().get(i).get${resultHeaderKey?cap_first}(), <#if validation == "RANGE"> 0, 0 <#else>"${validation}" </#if>));
       </#list>
       </#if>
           assertEquals(expectedResponse.getResults().get(i).get${resultHeaderKey?cap_first}(), body.getResults().get(i).get${resultHeaderKey?cap_first}());
    </#list>
    </#if>
        }

    <#if pathToJwtJson??>
       JwtContextHolder.removeJwtContext();
    </#if>
    }
   <#else>
    @Test
    public void ${method.methodName}_${method.methodNumber}Test() throws Exception {

    <#list method.arguments as argument>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value?default('')}"<#else>${argument.value?default('')}</#if>;
    </#list>

      <#if pathToJwtJson??>
        JwtContext jwtContext=new JwtContext();
        String pathToJwtJson = "${method.pathToJwtJson}";
        JwtBean jwtBean = JsonFileReaderUtil.getJwtFromJsonFile(pathToJwtJson) ;
        jwtContext.setJwtBean(jwtBean);
        JwtContextHolder.setJwtContext(jwtContext);
      </#if>

        ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments?sort_by('argumentOrder') as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
        assertNotNull(actualResponse);
        Object body = actualResponse.getBody();
        assertNotNull(body);

    <#if pathToJwtJson??>
       JwtContextHolder.removeJwtContext();
    </#if>
    }
   </#if>
  </#if>
 <#else>
  <#if method.isApiResponsePresent>

    @Test
    public void ${method.methodName}_${method.methodNumber}Test() throws Exception {

    <#list method.arguments as argument>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
    </#list>

      <#if method.pathToJwtJson??>
        JwtContext jwtContext=new JwtContext();
        String pathToJwtJson = "${method.pathToJwtJson}";
        JwtBean jwtBean = JsonFileReaderUtil.getJwtFromJsonFile(pathToJwtJson) ;
        jwtContext.setJwtBean(jwtBean);
        JwtContextHolder.setJwtContext(jwtContext);
      </#if>

        String pathToResponseJson = "${method.pathToResponseJson}";

        ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments?sort_by('argumentOrder') as argument>${argument.name}<#sep>, </#sep></#list>);
        assertNotNull(actualResponse);
        ${method.returnValue} body = actualResponse.getBody();
        assertNotNull(body);

      <#if method.responseHeaders?size != 0>
      <#list method.responseHeaders?keys as responseHeaderKey>
        assertEquals(${method.responseHeaders[responseHeaderKey]}, actualResponse.get${responseHeaderKey?cap_first}());
      </#list>
      </#if>

        List<${method.responseEntityType}> actualList = body.getResults();
        assertNotNull(actualList);

    <#if method.hasPathVariable??>
    <#if method.hasPathVariable>
        for (${method.responseEntityType} dto : actualList) {
    <#list method.arguments as argument>
    <#if argument.annotationType == "PathVariable">
            assertEquals(${argument.name}, dto.get${argument.name?cap_first}());
    </#if>
    </#list>
        }
    </#if>
    </#if>

    <#assign Properties = method.assertionParameters>
        ApiResponse<${method.responseEntityType}> expectedResponse = JsonFileReaderUtil.readResponseEntities(pathToResponseJson,${method.responseEntityType}.class);
        List<${method.responseEntityType}> expectedList = expectedResponse.getResults();
        assertNotNull(expectedList);
        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
      <#list Properties as property>
             assertEquals(expectedList.get(i).get${property?cap_first}(),actualList.get(i).get${property?cap_first}());
      </#list>
      <#if method.responseResultHeadersAndValidationChecks?size != 0>
      <#list method.responseResultHeadersAndValidationChecks?keys as resultHeaderKey>
         <#if method.responseResultHeadersAndValidationChecks[resultHeaderKey]?size != 0>
         <#list method.responseResultHeadersAndValidationChecks[resultHeaderKey] as validation>
             assertTrue(ValidationUtil.validate(body.getResults().get(i).get${resultHeaderKey?cap_first}(), <#if validation == "RANGE"> 0, 0 <#else>"${validation}" </#if>));
         </#list>
         </#if>
             assertEquals(expectedResponse.getResults().get(i).get${resultHeaderKey?cap_first}(), body.getResults().get(i).get${resultHeaderKey?cap_first}());
      </#list>
      </#if>
         }

      <#if pathToJwtJson??>
         JwtContextHolder.removeJwtContext();
      </#if>
    }
  <#else>
    @Test
    public void ${method.methodName}_${method.methodNumber}Test() throws Exception {

    <#list method.arguments as argument>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
    </#list>

      <#if pathToJwtJson??>
        JwtContext jwtContext=new JwtContext();
        String pathToJwtJson = "${method.pathToJwtJson}";
        JwtBean jwtBean = JsonFileReaderUtil.getJwtFromJsonFile(pathToJwtJson) ;
        jwtContext.setJwtBean(jwtBean);
        JwtContextHolder.setJwtContext(jwtContext);
      </#if>

        Object actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments?sort_by('argumentOrder') as argument>${argument.name}<#sep>, </#sep></#list>);
        assertNotNull(actualResponse);
        Object expectedResponse = new Object();
        assertEquals(actualResponse,expectedResponse);

      <#if pathToJwtJson??>
        JwtContextHolder.removeJwtContext();
      </#if>

    }
  </#if>
 </#if>
</#list>
}