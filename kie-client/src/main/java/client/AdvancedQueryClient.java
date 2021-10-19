package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.OrderInfo;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.definition.TaskField;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.util.QueryFilterSpecBuilder;
import org.kie.server.api.util.TaskQueryFilterSpecBuilder;
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
	private static final String DEPLOYMENTID = "order-management_1.1-SNAPSHOT";
	private static String PROCESS_ID = "OrderManagement";

	private static final String QUERY_NAME_ALL_PROCESS_INSTANCES_WITH_VARIABLES = "getAllProcessInstancesWithVariables";

	public static void main(String[] args) {
		AdvancedQueryClient queryClient = new AdvancedQueryClient();

		long start = System.currentTimeMillis();

		queryClient.getAllTasksAdvanced();
		// queryClient.rawAdvancedQuery();

		// queryClient.registerQuery2();

		long end = System.currentTimeMillis();
		System.out.println("elapsed time: " + (end - start));
	}

	private void getAllTasksAdvanced() {
		try {
			KieServicesClient client = getClient();

			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);

			List<?> list = Arrays.asList(DEPLOYMENTID);
			TaskQueryFilterSpec filterSpec = new TaskQueryFilterSpecBuilder().in(TaskField.STATUS, Arrays.asList("Ready"))
			                                                                 .in(TaskField.DEPLOYMENTID, list)
			                                                                 .get();

			List<TaskInstance> tasks = queryClient.findHumanTasksWithFilters("jbpmHumanTasks", filterSpec, 0, 20);

			System.out.println(tasks);
			System.out.println("List len: " + tasks.size());
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getAllProcesses() {
		try {
			KieServicesClient client = getClient();

			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);

			List<List> rawList = queryClient.query("f49bc778-4883-4295-b391-6454fe64e42b",
			        QueryServicesClient.QUERY_MAP_RAW, 0, 10, List.class);

			System.out.println(rawList);
			System.out.println("List len: " + rawList.size());
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			                              + "ON (v.variableId = x.variableId  AND v.id = x.maxvilid ) "
			                              + "INNER JOIN VariableInstanceLog v "
			                              + "ON (v.processInstanceId = pil.processInstanceId)");
			queryDefinition.setSource("java:jboss/datasources/ExampleDS");
			queryDefinition.setTarget("CUSTOM");
			queryClient.replaceQuery(queryDefinition);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerQuery2() {
		try {
			KieServicesClient client = getClient();

			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);

			QueryDefinition queryDefinition = new QueryDefinition();
			queryDefinition.setName(QUERY_NAME_ALL_PROCESS_INSTANCES_WITH_VARIABLES);
			queryDefinition.setExpression(
			        "SELECT DISTINCT pil.PROCESSINSTANCEID, pil.PROCESSID, pil.START_DATE, pil.END_DATE, pil.STATUS, pil.PARENTPROCESSINSTANCEID, pil.OUTCOME, pil.DURATION, pil.USER_IDENTITY, pil.PROCESSVERSION,pil.PROCESSNAME, pil.CORRELATIONKEY, pil.EXTERNALID, pil.PROCESSINSTANCEDESCRIPTION, pil.SLA_DUE_DATE, pil.SLACOMPLIANCE, oi.item, oi.category, oi.urgency, oi.price, oi.managerApproval, oi.rejectionReason oi.targetPrice"
			                              + "FROM PROCESSINSTANCELOG pil"
			                              + "INNER JOIN MAPPEDVARIABLE mv ON pil.PROCESSINSTANCEID = mv.PROCESSINSTANCEID"
			                              + "INNER JOIN ORDERINFO oi ON oi.ID = mv.VARIABLEID");
			queryDefinition.setSource("${org.kie.server.persistence.ds}");
			queryDefinition.setTarget("CUSTOM");
			queryClient.replaceQuery(queryDefinition);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void rawAdvancedQuery() {
		try {
			KieServicesClient client = getClient();

			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);

			List<List> rawList = queryClient.query("fe375037-8da7-43c3-a0d9-28051c39fb9c",
			        QueryServicesClient.QUERY_MAP_RAW, 0, 10, List.class);

			System.out.println(rawList);
			System.out.println("List len: " + rawList.size());
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private KieServicesClient getClient() {
		KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(URL, user, password);

		// Marshalling
		config.setMarshallingFormat(MarshallingFormat.JSON);
		Set<Class<?>> extraClasses = new HashSet<Class<?>>();
		extraClasses.add(Date.class);
		extraClasses.add(OrderInfo.class);
		config.addExtraClasses(extraClasses);
		Map<String, String> headers = null;
		config.setHeaders(headers);
		KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

		return client;
	}
}