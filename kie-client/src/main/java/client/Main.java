package client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.OrderInfo;
import com.example.SupplierInfo;

import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.admin.OrgEntities;
import org.kie.server.api.model.definition.TaskField;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.util.TaskQueryFilterSpecBuilder;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.client.admin.UserTaskAdminServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class Main {

	final static Logger log = LoggerFactory.getLogger(Main.class);

	private static final String URL = "http://localhost:8080/kie-server/services/rest/server";
	private static final String user = System.getProperty("username", "donato");
	private static final String password = System.getProperty("password", "donato");
	private static final String CONTAINER = "order-management_1.1-SNAPSHOT";
	private static String PROCESS_ID = "OrderManagement";

	private static final String QUERY_NAME_ALL_PROCESS_INSTANCES_WITH_VARIABLES = "getAllProcessInstancesWithVariables";

	public static void main(String[] args) {
		Main clientApp = new Main();

		long start = System.currentTimeMillis();

		// clientApp.registerQuery();
		// clientApp.advancedQuery();
		// clientApp.launchProcess();
		// clientApp.findTasksAssignedAsPotentialOwner();
		// clientApp.startTask(12L);
		// clientApp.getTaskInputContentByTaskId(3L);
		// clientApp.completeTask(12L);
		// clientApp.evaluateDecision();
		// clientApp.updateTask();
		// clientApp.getContainers();
		clientApp.taskListQuery();

		long end = System.currentTimeMillis();
		System.out.println("elapsed time: " + (end - start));
	}

	private void taskList() {
		try {
			KieServicesClient client = getClient();
			UserTaskServicesClient userTaskServicesClient = client.getServicesClient(UserTaskServicesClient.class);
			List<TaskSummary> list = userTaskServicesClient.findTasksByStatusByProcessInstanceId(1L, null, 0, 10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void taskListQuery() {
		try {
			KieServicesClient client = getClient();


			String queryName = "jbpmHumanTasks";
			String mapper = "UserTasks";
			long processInstanceId = 1L;
			
			QueryServicesClient queryClient = client.getServicesClient(QueryServicesClient.class);
			TaskQueryFilterSpec filterSpec = new TaskQueryFilterSpecBuilder()
					.equalsTo(TaskField.PROCESSINSTANCEID, processInstanceId).get();
			List<TaskInstance> result = queryClient.query(queryName, mapper, filterSpec, 0, 10, TaskInstance.class);

			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void evaluateDecision() {
		KieServicesClient client = getClient();
		RuleServicesClient ruleServicesClient = client.getServicesClient(RuleServicesClient.class);

		KieCommands cmdFactory = KieServices.Factory.get().getCommands();

		List<Command<?>> commands = new ArrayList<>();

		OrderInfo orderInfo = new OrderInfo(0,"Item1", "basic", "low", 500, null,"",null,500,null);

		commands.add(cmdFactory.newInsert(orderInfo));
		commands.add(cmdFactory.newAgendaGroupSetFocus("approval"));
		commands.add(cmdFactory.newFireAllRules());

		// The following command retrieves all objects from Working Memory
		commands.add(cmdFactory.newGetObjects("objects"));

		commands.add(cmdFactory.newDispose());

		BatchExecutionCommand batchExecution = cmdFactory.newBatchExecution(commands);
		ServiceResponse<ExecutionResults> serviceResponse = ruleServicesClient.executeCommandsWithResults(CONTAINER,
				batchExecution);

		serviceResponse.getResult().getIdentifiers().forEach(i -> {
			System.out.println(serviceResponse.getResult().getValue(i));
		});

	}
	
	private void getContainers() {

		KieServicesClient client = getClient();
		
		ServiceResponse<KieContainerResourceList> listContainers = client.listContainers();
		List<KieContainerResource> containers = listContainers.getResult().getContainers();
		containers.forEach(c -> {
			System.out.format("id: %s\n", c.getContainerId());
		});
	}

	private void updateTask() {
		KieServicesClient client = getClient();
		UserTaskAdminServicesClient adminServicesClient = client.getServicesClient(UserTaskAdminServicesClient.class);
		OrgEntities orgEntities = new OrgEntities();
		List<String> groups = new ArrayList<>();
		groups.add("user");
		orgEntities.setGroups(groups);
		adminServicesClient.addPotentialOwners("SimpleCase_1.0", 2L, false, orgEntities);
	}

	private void completeTask(Long taskId) {
		try {

			KieServicesClient client = getClient();
			UserTaskServicesClient userTaskServicesClient = client.getServicesClient(UserTaskServicesClient.class);

			// ---------------------------
			Map<String, Object> params = new HashMap<>();
			OrderInfo orderInfo = new OrderInfo();
			orderInfo.setOrderId(7L);
			orderInfo.setItem("Monitor Dell U2412M");
			orderInfo.setUrgency("low");
			orderInfo.setCategory("basic");

			params.put("orderInfo", orderInfo);

			SupplierInfo supplierInfo = new SupplierInfo();
			supplierInfo.setUser("donato");

			params.put("supplierInfo", supplierInfo);

			// ---------------------------
			userTaskServicesClient.completeTask(CONTAINER, taskId, null, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getTaskInputContentByTaskId(Long taskId) {
		try {

			KieServicesClient client = getClient();
			UserTaskServicesClient userTaskServicesClient = client.getServicesClient(UserTaskServicesClient.class);
			Map<String, Object> input = userTaskServicesClient.getTaskInputContentByTaskId(CONTAINER, taskId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findTasksAssignedAsPotentialOwner() {
		try {

			KieServicesClient client = getClient();
			UserTaskServicesClient userTaskServicesClient = client.getServicesClient(UserTaskServicesClient.class);
			userTaskServicesClient.findTasksAssignedAsPotentialOwner(user, 0, 20);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void findTasksOwned() {
		try {

			KieServicesClient client = getClient();
			UserTaskServicesClient userTaskServicesClient = client.getServicesClient(UserTaskServicesClient.class);
			userTaskServicesClient.findTasksOwned(null, 0, 20);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void startTask(Long taskId) {
		try {

			KieServicesClient client = getClient();
			UserTaskServicesClient userTaskServicesClient = client.getServicesClient(UserTaskServicesClient.class);
			userTaskServicesClient.startTask(CONTAINER, taskId, user);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void launchProcess() {
		try {

			KieServicesClient client = getClient();

			ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);
			Map<String, Object> variables = new HashMap<>();

			// ---------------------------
			OrderInfo orderInfo = new OrderInfo();
			orderInfo.setItem("Monitor Dell U2412M");
			orderInfo.setUrgency("Low");

			variables.put("orderInfo", orderInfo);

			// ---------------------------
			processClient.startProcess(CONTAINER, PROCESS_ID, variables);

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
