package net.savantly.metrics.carbonProxy.rewriter;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import net.savantly.metrics.carbonProxy.KeyValuePair;

@RunWith(SpringRunner.class)
public class RewriterServiceTest {

	@Test
	public void test() {
		RewriterService rewriter = new RewriterService();
		Map<String, KeyValuePair> replacements = new HashMap<>();
		replacements.put("example", new KeyValuePair("^test(.*)", "changed$1"));
		rewriter.setReplacements(replacements);
		
		rewriter.postConstruct();
		
		String metric = "test.one.two";
		String expected = "changed.one.two";
		
		String actual = rewriter.rewrite(metric);
		Assert.assertEquals("The metric segment should be replaced", expected, actual);
	}
}
