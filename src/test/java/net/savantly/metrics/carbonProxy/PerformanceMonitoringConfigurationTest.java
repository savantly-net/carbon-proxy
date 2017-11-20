package net.savantly.metrics.carbonProxy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(methodMode=MethodMode.AFTER_METHOD)
public class PerformanceMonitoringConfigurationTest {
	
	@Autowired
	PerformanceMonitoringConfiguration performance;

	@Test
	public void test() {
		performance.sendStatistics();
	}
}
