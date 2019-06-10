package client;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.OrderInfo;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;

/**
 * DMNTest
 */
public class DMNTest {
	private static final String URL = "http://localhost:8080/kie-server/services/rest/server";
	private static final String user = System.getProperty("username", "donato");
	private static final String password = System.getProperty("password", "donato");
	private static final String CONTAINER = "order-management-dmn";

	public static void main(String[] args) {
		DMNTest dmnTest = new DMNTest();

		long start = System.currentTimeMillis();

		dmnTest.evaluateDMN();

		long end = System.currentTimeMillis();
		System.out.println("elapsed time: " + (end - start));
	}

	private void evaluateDMN() {
		KieServicesClient client = getClient();
		DMNServicesClient dmnClient = client.getServicesClient(DMNServicesClient.class);

		String namespace = "https://github.com/kiegroup/drools/kie-dmn/_669C7599-69B9-491F-8D2F-47AD5B813116";
		String modelName = "order-approval";

		DMNContext dmnContext = dmnClient.newContext();

		OrderInfo orderInfo = new OrderInfo(0, "item", "basic", "low", 1000, false, null, null, 1000, null);

		dmnContext.set("Order Information", orderInfo);

		ServiceResponse<DMNResult> result = dmnClient.evaluateAll(CONTAINER, namespace, modelName, dmnContext);
		System.out.println(result);
		
	}

	private KieServicesClient getClient() {
		KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(URL, user, password);

		// Configuration for JMS
		//		KieServicesConfiguration config = KieServicesFactory.newJMSConfiguration(connectionFactory, requestQueue, responseQueue, username, password)
		
		Map<String, String> headers = null;
		config.setHeaders(headers);
		Set<Class<?>> classes = new HashSet<>();
		classes.add(OrderInfo.class);
		config.addExtraClasses(classes);

		config.setMarshallingFormat(MarshallingFormat.JSON);
		KieServicesClient client = KieServicesFactory.newKieServicesClient(config);

		return client;
	}
}
