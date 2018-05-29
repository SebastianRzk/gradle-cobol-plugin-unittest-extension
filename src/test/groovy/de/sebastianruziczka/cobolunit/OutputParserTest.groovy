package de.sebastianruziczka.cobolunit;

import static org.assertj.core.api.Assertions.assertThat

import org.junit.Test

import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.buildcycle.test.TestMethod
import de.sebastianruziczka.buildcycle.test.TestMethodResult

class OutputParserTest {


	List<String> successFull(){
		return [
			" ",
			"TEST SUITE:",
			"GREETING AND FAREWELL ",
			"",
			"Hello Jens",
			"HELLO Hans",
			"     PASS:   1. IT RETURNS HELLO, WORLD! AS GREETING                                            ",
			" ",
			"  1 TEST CASES WERE EXECUTED",
			"  1 PASSED",
			"  0 FAILED",
			"================================================="
		]
	}


	@Test
	void test_parseSuccessFull() {
		OutputParser component_under_test = new OutputParser()

		TestFile result = component_under_test.parse("TESTFAIL", this.successFull())

		assertThat(result.name()).isEqualTo('TESTFAIL(GREETING AND FAREWELL)')
		assertThat(result.testMethods.size()).isEqualTo(1)

		TestMethod testMethod = result.testMethods.getAt(0)
		assertThat(testMethod.name).isEqualTo('1. IT RETURNS HELLO, WORLD! AS GREETING')
		assertThat(testMethod.result).isEqualTo(TestMethodResult.SUCCESSFUL)
		assertThat(testMethod.console).isEqualTo("Hello Jens\nHELLO Hans")
	}
}
