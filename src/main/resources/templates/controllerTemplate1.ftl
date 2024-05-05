package ${packageName};

<#list importsList as import>${import}</#list>
import com.blumeglobal.JsonFileReaderUtil;
import com.blumeglobal.AbstractIntegrationTest;
import com.blumeglobal.ValidationUtil;
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
    @Autowired
    private ${className} ${classInstance};
    </#if>
    </#if>


<#list methodsList as method>
   <#if method.hasRequestBody>
      <#if method.isApiRequestPresent>
         <#if method.isApiResponsePresent>

            @Test
            public void ${method.methodName}Test() throws Exception {

            <#list method.arguments as argument>
            <#if argument.dataType == "ApiRequest">
                String pathToRequestJson = ${method.pathToRequestJson};
                List<${method.requestEntityType}> requestObjects = JsonFileReaderUtil.readRequestEntities(pathToRequestJson,${method.requestEntityType}.class);
                ApiRequest<${method.requestEntityType}> apiRequest = new ApiRequest<>();
            <#else>
                ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
            </#if>
            </#list>

                apiRequest.setRequest(requestObjects);
                apiRequest.setCorrelationId(correlationId);

                ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
                assertNotNull(actualResponse);
                ${method.returnValue} body = actualResponse.getBody();
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
                String pathToResponseJson = ${method.pathToResponseJson};
                ApiResponse<${method.responseEntityType}> expectedResponse = JsonFileReaderUtil.readResponseEntities(pathToResponseJson,${method.responseEntityType}.class);
                List<${method.responseEntityType}> expectedList = expectedResponse.getResults();
                assertNotNull(expectedList);
                assertEquals(expectedList.size(), actualList.size());

            <#if method.headersAndValidationChecks?size != 0>
            <#list method.headersAndValidationChecks?keys as headerKey>
                <#if method.headersAndValidationChecks[headerKey]?size != 0>
                <#list method.headersAndValidationChecks[headerKey] as validation>
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
            <#if method.resultHeadersAndValidationChecks?size != 0>
            <#list method.resultHeadersAndValidationChecks?keys as resultHeaderKey>
               <#if method.resultHeadersAndValidationChecks[resultHeaderKey]?size != 0>
               <#list method.resultHeadersAndValidationChecks[resultHeaderKey] as validation>
                   assertTrue(ValidationUtil.validate(body.getResults().get(i).get${resultHeaderKey?cap_first}(), <#if validation == "RANGE"> 0, 0 <#else>"${validation}" </#if>));
               </#list>
               </#if>
                   assertEquals(expectedResponse.getResults().get(i).get${resultHeaderKey?cap_first}(), body.getResults().get(i).get${resultHeaderKey?cap_first}());
            </#list>
            </#if>
                }
            </#if>
            }
         <#else>
            @Test
            public void ${method.methodName}Test() throws Exception {

            <#list method.arguments as argument>
            <#if argument.dataType == "ApiRequest">
                String pathToRequestJson = ${method.pathToRequestJson};
            <#else>
                ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
            </#if>
            </#list>

                ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
                assertNotNull(actualResponse);
                ${method.returnValue} body = actualResponse.getBody();
                assertNotNull(body);
            }
         </#if>
      <#else>
         <#if method.isApiResponsePresent>
            @Test
            public void ${method.methodName}Test() throws Exception {

            <#list method.arguments as argument>
                ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
            </#list>

            <#if method.isApiResponsePresent??>
            <#if method.isApiResponsePresent>
                String pathToResponseJson = "${method.pathToResponseJson}";
            </#if>
            </#if>

                ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
                assertNotNull(actualResponse);
                ${method.returnValue} body = actualResponse.getBody();
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
                }

            }
         <#else>
            @Test
            public void ${method.methodName}Test() throws Exception {

            <#list method.arguments as argument>
                ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value?default('')}"<#else>${argument.value?default('')}</#if>;
            </#list>

                ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments as argument><#if argument.isApiRequest>apiRequest<#else>${argument.name}</#if><#sep>, </#sep></#list>);
                assertNotNull(actualResponse);
                Object body = actualResponse.getBody();
                assertNotNull(body);
            }
         </#if>
      </#if>
   <#else>
      <#if method.isApiResponsePresent>
            @Test
            public void ${method.methodName}Test() throws Exception {

            <#list method.arguments as argument>
                ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
            </#list>

                String pathToResponseJson = "${method.pathToResponseJson}";

                ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments as argument>${argument.name}<#sep>, </#sep></#list>);
                assertNotNull(actualResponse);
                ${method.returnValue} body = actualResponse.getBody();
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
                }

                ResponseEntity<${method.returnValue}> actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments as argument>${argument.name}<#sep>, </#sep></#list>);
                assertNotNull(actualResponse);
                Object body = actualResponse.getBody();
                assertNotNull(body);

            }
      <#else>
            @Test
            public void ${method.methodName}Test() throws Exception {

            <#list method.arguments as argument>
                ${argument.dataType} ${argument.name} = <#if argument.dataType == "String">"${argument.value}"<#else>${argument.value}</#if>;
            </#list>

            Object actualResponse = ${classInstance}.${method.methodName}(<#list method.arguments as argument>${argument.name}<#sep>, </#sep></#list>);
            assertNotNull(actualResponse);
            Object expectedResponse = new Object();
            assertEquals(actualResponse,expectedResponse);

            }
      </#if>
   </#if>
</#list>
}