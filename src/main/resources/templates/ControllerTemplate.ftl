package ${packageName};

<#list importsList as import>${import}</#list>
import com.blumeglobal.JsonFileReaderUtil;
import com.blumeglobal.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

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
    private ${className} ${classInstance};
    </#if>
    </#if>


  <#list methodsList as method>
   <#if method.hasRequestBody??>
   <#if method.hasRequestBody>
  <#list method.pathToJsonFiles as RequestResponseMap>
  <#assign currentIndex = RequestResponseMap?index>
    @Test
    public void ${method.methodName}Test_${currentIndex}() throws Exception {

      <#list method.arguments as argument>
       <#if argument.dataType == "ApiRequest">
        <#list RequestResponseMap?keys as Request>
        String pathToRequestJson = "${Request}";
        String pathToResponseJson = "${RequestResponseMap[Request]}";
        </#list>
        List<${method.entityType}> requestObjects = JsonFileReaderUtil.readRequestEntities(pathToRequestJson,${method.entityType}.class);
        ApiRequest<${method.entityType}> apiRequest = new ApiRequest<>();
       <#else>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
       </#if>
      </#list>

      <#if method.hasRequestBody??>
       <#if method.hasRequestBody>
        apiRequest.setRequest(requestObjects);
        apiRequest.setCorrelationId(correlationId);
       </#if>
      </#if>

        ResponseEntity<${method.returnValue}> responseEntity = ${classInstance}.${method.methodName}(<#list method.arguments as argument>${argument.name}<#sep>, </#sep></#list>);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ${method.returnValue} body = responseEntity.getBody();
        assertNotNull(body);

      <#if method.hasRequestParam??>
      <#if method.hasRequestParam>
      <#list method.arguments as argument>
      <#if argument.annotationType == "RequestParam">
        assertEquals(${argument.name}, body.get${argument.name?cap_first}());
      </#if>
      </#list>
      </#if>
      </#if>

        List<${method.returnEntityType}> actualList = body.getResults();
        assertNotNull(actualList);

    <#if method.hasPathVariable??>
      <#if method.hasPathVariable>
        for (${method.returnEntityType} dto : actualList) {
        <#list method.arguments as argument>
        <#if argument.annotationType == "PathVariable">
          assertEquals(${argument.name}, dto.get${argument.name?cap_first}());
        </#if>
        </#list>
        }
      </#if>
    </#if>

    <#assign Properties = method.assertionParameters>

    <#if method.hasRequestBody??>
      <#if method.hasRequestBody>

        ApiResponse<${method.returnEntityType}> expectedResponse = JsonFileReaderUtil.readResponseEntities(pathToResponseJson,${method.returnEntityType}.class);
        List<${method.returnEntityType}> expectedList = expectedResponse.getResults();
        assertNotNull(expectedList);

        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
          <#list Properties as property>
           //${method.entityType} expected = requestObjects.get(i);
           //${method.entityType} actual = actualList.get(i);
           //assertEquals(expected, actual);
           assertEquals(expectedList.get(i).get${property?cap_first}(),actualList.get(i).get${property?cap_first}());
          </#list>
        }
      </#if>
    </#if>


    }
  </#list>
  </#if>
   </#if>

    @Test
    public void ${method.methodName}() throws Exception {

    <#list method.arguments as argument>
      <#if argument.dataType == "ApiRequest">
        String pathToJson = "${argument.pathToJsonFile}";
        List<${method.entityType}> requestObjects = JsonFileReaderUtil.readRequestEntities(pathToJson,${method.entityType}.class);
        ApiRequest<${method.entityType}> apiRequest = new ApiRequest<>();
      <#else>
        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
      </#if>
    </#list>

      <#if method.hasRequestBody??>
        <#if method.hasRequestBody>
        apiRequest.setRequest(requestObjects);
        apiRequest.setCorrelationId(correlationId);
        </#if>
      </#if>

        ResponseEntity<${method.returnValue}> responseEntity = ${classInstance}.${method.methodName}(<#list method.arguments as argument>${argument.name}<#sep>, </#sep></#list>);
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ${method.returnValue} body = responseEntity.getBody();
        assertNotNull(body);

      <#if method.hasRequestParam??>
      <#if method.hasRequestParam>
      <#list method.arguments as argument>
      <#if argument.annotationType == "RequestParam">
        assertEquals(${argument.name}, body.get${argument.name?cap_first}());
      </#if>
      </#list>
      </#if>
      </#if>

        List<${method.returnEntityType}> actualList = body.getResults();
        assertNotNull(actualList);

      <#if method.hasPathVariable??>
       <#if method.hasPathVariable>
        for (${method.returnEntityType} dto : actualList) {
        <#list method.arguments as argument>
         <#if argument.annotationType == "PathVariable">
           assertEquals(${argument.name}, dto.get${argument.name?cap_first}());
         </#if>
        </#list>
        }
       </#if>
      </#if>

      <#assign Properties = method.assertionParameters>

      <#if method.hasRequestBody??>
      <#if method.hasRequestBody>
        assertEquals(requestObjects.size(), actualList.size());
        for (int i = 0; i < requestObjects.size(); i++) {
          <#list Properties as property>
           //${method.entityType} expected = requestObjects.get(i);
           //${method.entityType} actual = actualList.get(i);
           //assertEquals(expected, actual);
           assertEquals(requestObjects.get(i).get${property?cap_first}(),actualList.get(i).get${property?cap_first}());
          </#list>
        }
      </#if>
      </#if>

    }


//  <#list method.arguments as argument>
//    <#assign nullArgument = argument.name>
//  @Test
  //  public void ${method.methodName}_Wrong${argument.name?cap_first}() throws Exception {
//
  //  <#list method.arguments as argument>
    // <#if argument.name == nullArgument>
      //<#if argument.dataType == "ApiRequest">
//        ApiRequest<${method.entityType}> apiRequest = new ApiRequest<>();
//      <#else>
//        ${argument.dataType} ${argument.name} = "";
//      </#if>
//     <#else>
//      <#if argument.dataType == "ApiRequest">
//        String pathToJson = "${argument.pathToJsonFile}";
//        List<${method.entityType}> requestObjects = JsonFileReaderUtil.readRequestEntities(pathToJson,${method.entityType}.class);
//        ApiRequest<${method.entityType}> apiRequest = new ApiRequest<>();
//      <#else>
//        ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
//      </#if>
//     </#if>
//    </#list>
//    <#if nullArgument != "apiRequest">
//     <#if method.hasRequestBody??>
//        <#if method.hasRequestBody>
//        apiRequest.setRequest(requestObjects);
//        apiRequest.setCorrelationId(correlationId);
//        </#if>
//     </#if>
//    </#if>
//        ResponseEntity<${method.returnValue}> responseEntity = ${classInstance}.${method.methodName}(<#list method.arguments as argument>${argument.name}<#sep>, </#sep></#list>);
//        assertNotNull(responseEntity);
//        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
//        ${method.returnValue} body = responseEntity.getBody();
//        assertNotNull(body);

//    }
//  </#list>

// </#list>


}