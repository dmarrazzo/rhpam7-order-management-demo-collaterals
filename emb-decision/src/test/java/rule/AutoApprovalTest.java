package rule;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import com.example.OrderInfo;

public class AutoApprovalTest {
	private KieSession kieSession;

	private void initFromClasspath() {
		KieServices kieServices = KieServices.Factory.get();
		KieContainer kieContainer = kieServices.getKieClasspathContainer();

		kieSession = kieContainer.newKieSession();
	}

	private void initFromMaven() {
		KieServices kieServices = KieServices.Factory.get();
		ReleaseId releaseId = kieServices.newReleaseId("com.example", "order-management", "1.0-SNAPSHOT");
		KieContainer kieContainer = kieServices.newKieContainer(releaseId);
		
		kieSession = kieContainer.newKieSession();
	}

	@Before
	public void before() {
		System.setProperty("jbpm.enable.multi.con", "true");
		initFromClasspath();
	}

	@After
	public void after() {
		kieSession.dispose();
	}

	@Test
	public void test() {
		OrderInfo orderInfo = new OrderInfo(0, "Item1", "basic", "low", 500, null, null);
		FactHandle fact = kieSession.insert(orderInfo);
		kieSession.getAgenda().getAgendaGroup("approval").setFocus();
		int firedRules = kieSession.fireAllRules();
		System.out.println(firedRules + " " + orderInfo);
		
		assertTrue("at least 1 rule", firedRules>0);
		assertTrue("approved", orderInfo.getManagerApproval());

		//----------------------------------------------------------------------
		orderInfo.setPrice(501);
		kieSession.update(fact, orderInfo);
		kieSession.getAgenda().getAgendaGroup("approval").setFocus();
		firedRules = kieSession.fireAllRules();
		System.out.println(firedRules + " " + orderInfo);
		assertTrue("at least 1 rule", firedRules>0);
		assertTrue("not approved", false == orderInfo.getManagerApproval());

		//----------------------------------------------------------------------
		orderInfo.setCategory("optional");		
		orderInfo.setPrice(100);
		kieSession.update(fact, orderInfo);
		kieSession.getAgenda().getAgendaGroup("approval").setFocus();
		firedRules = kieSession.fireAllRules();
		System.out.println(firedRules + " " + orderInfo);
		assertTrue("at least 1 rule", firedRules>0);
		assertTrue("approved", orderInfo.getManagerApproval());

	}

}
