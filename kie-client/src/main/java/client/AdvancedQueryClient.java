package client;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.OrderInfo;
import com.example.SupplierInfo;

import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.QueryServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AdvancedQueryClient
 */
public class AdvancedQueryClient {
	final static Logger log = LoggerFactory.getLogger(Main.class);

	private static final String URL = "http://localhost:8080/kie-server/services/rest/server";
	private static final String user = System.getProperty("username", "donato");
	private static final String password = System.getProperty("password", "donato");
	private static final String CONTAINER = "order-management_1.1-SNAPSHOT";
	private static String PROCESS_ID = "OrderManagement";

	private static final String QUERY_NAME_ALL_PROCESS_INSTANCES_WITH_VARIABLES = "getAllProcessInstancesWithVariables";

    public static void main(String[] args) {
        AdvancedQueryClient queryClient = new AdvancedQueryClient();

		long start = System.currentTimeMillis();

		queryClient.registerQuery();
		// queryClient.advancedQuery();

		long end = System.currentTimeMillis();
		System.out.println("elapsed time: " + (end - start));
    }

	public void registerQuery() {
		try {
			KieServicesClient client = getClient();

			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);

			QueryDefinition queryDefinition = new QueryDefinition();
			queryDefinition.setName(QUERY_NAME_ALL_PROCESS_INSTANCES_WITH_VARIABLES);
			queryDefinition.setExpression("select pil.*, v.variableId, v.value " + "from ProcessInstanceLog pil "
					+ "INNER JOIN (select vil.processInstanceId ,vil.variableId, MAX(vil.ID) maxvilid  FROM VariableInstanceLog vil "
					+ "GROUP BY vil.processInstanceId, vil.variableId ORDER BY vil.processInstanceId)  x "
					+ "ON (v.variableId = x.variableId  AND v.id = x.maxvilid ) " + "INNER JOIN VariableInstanceLog v "
					+ "ON (v.processInstanceId = pil.processInstanceId)");
			queryDefinition.setSource("java:jboss/datasources/ExampleDS");
			queryDefinition.setTarget("CUSTOM");
			queryClient.replaceQuery(queryDefinition);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void advancedQuery() {
		try {
			KieServicesClient client = getClient();

			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);

			QueryFilterSpec spec = new QueryFilterSpecBuilder().equalsTo("variableId", "istruttore")
					.equalsTo("value", "istruttore7").equalsTo("status", 1).get();

			List<ProcessInstance> listProcessInstance = queryClient.query(
					QUERY_NAME_ALL_PROCESS_INSTANCES_WITH_VARIABLES, "ProcessInstances", spec, 0, 30,
					ProcessInstance.class);

			for (ProcessInstance processInstance : listProcessInstance) {
				System.out.println(">>>" + processInstance.getId());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private KieServicesClient getClient() {
		KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(URL, user, password);

		// Configuration for JMS
		//		KieServicesConfiguration config = KieServicesFactory.newJMSConfiguration(connectionFactory, requestQueue, responseQueue, username, password)
		
		// Marshalling
		config.setMarshallingFormat(MarshallingFormat.JSON);
		Set<Class<?>> extraClasses = new HashSet<Class<?>>();
		extraClasses.add(OrderInfo.class);
		extraClasses.add(SupplierInfo.class);
		config.addExtraClasses(extraClasses);
		Map<String, String> headers = null;
		config.setHeaders(headers);
		KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

		return client;
	}
}