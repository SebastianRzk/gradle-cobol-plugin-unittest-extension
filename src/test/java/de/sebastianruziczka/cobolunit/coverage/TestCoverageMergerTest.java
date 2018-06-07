package de.sebastianruziczka.cobolunit.coverage;

import static de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus.not_passed;
import static de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus.passed;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile;
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageLine;
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod;
import de.sebastianruziczka.cobolunit.coverage.model.CoverageStatus;

public class TestCoverageMergerTest {
	private String simpleLogOuput() {
		return "Program-Id: Main             Statement: MOVE                   Line: 28\n" + //
				"Program-Id: Main             Statement: PERFORM                Line: 29\n" + //
				"Program-Id: Main             Paragraph: 2000-COMPUTE-GREETING  Line: 90\n" + //
				"Program-Id: Main             Statement: STRING                 Line: 91\n" + //
				"Program-Id: Main             Statement: IF                     Line: 95\n";
	}

	private String complexLogOuput() {
		return "Program-Id: Main             Statement: MOVE                   Line: 01\n" + //
				"Program-Id: Main             Paragraph: 1000-COMPUTE-GREETING  Line: 1000\n" + //
				"Program-Id: Main             Statement: STRING                 Line: 1001\n" + //
				"Program-Id: Main             Paragraph: 2000-COMPUTE-GREETING  Line: 1101\n" + //
				"Program-Id: Main             Statement: STRING                 Line: 1103\n" + //
				"Program-Id: Main             Statement: IF                     Line: 1104\n";
	}

	@Test
	public void testParse_shouldDetectParagraph() {
		CobolCoverageFile file = new CobolCoverageFile("My Cool File");
		CobolCoverageMethod method = new CobolCoverageMethod("2000-COMPUTE-GREETING", 1);
		method.setEnd(6);
		file.addMethod(method);

		TestCoverageMerger component_under_test = new TestCoverageMerger();

		component_under_test.merge(file, Arrays.asList(this.simpleLogOuput().split("\n")));

		System.out.println(method.methodStatus().toString());

		assertLines(method, 1, 2, 3, 4, 5, 6);
		assertStatus(method, passed, not_passed, not_passed, not_passed, passed, not_passed);
	}

	@Test
	public void testParse_shouldSwitchParagraphs() {
		CobolCoverageFile file = new CobolCoverageFile("My Cool File");
		CobolCoverageMethod method1000 = new CobolCoverageMethod("1000-COMPUTE-GREETING", 100);
		method1000.setEnd(104);
		file.addMethod(method1000);

		CobolCoverageMethod method2000 = new CobolCoverageMethod("2000-COMPUTE-GREETING", 200);
		method2000.setEnd(204);
		file.addMethod(method2000);

		TestCoverageMerger component_under_test = new TestCoverageMerger();

		component_under_test.merge(file, Arrays.asList(this.complexLogOuput().split("\n")));

		System.out.println(method1000.methodStatus().toString());

		assertLines(method1000, 100, 101, 102, 103, 104);
		assertStatus(method1000, passed, not_passed, not_passed, not_passed, not_passed);

		assertLines(method2000, 200, 201, 202, 203, 204);
		assertStatus(method2000, not_passed, passed, passed, not_passed, not_passed);
	}

	@Test
	public void testParse_shouldJu() {
		CobolCoverageFile file = new CobolCoverageFile("My Cool File");
		CobolCoverageMethod method1000 = new CobolCoverageMethod("1000-COMPUTE-GREETING", 100);
		method1000.setEnd(104);
		file.addMethod(method1000);

		CobolCoverageMethod method2000 = new CobolCoverageMethod("2000-COMPUTE-GREETING", 200);
		method2000.setEnd(204);
		file.addMethod(method2000);

		TestCoverageMerger component_under_test = new TestCoverageMerger();

		component_under_test.merge(file, Arrays.asList(this.complexLogOuput().split("\n")));

		System.out.println(method1000.methodStatus().toString());

		assertLines(method1000, 100, 101, 102, 103, 104);
		assertStatus(method1000, passed, not_passed, not_passed, not_passed, not_passed);

		assertLines(method2000, 200, 201, 202, 203, 204);
		assertStatus(method2000, not_passed, passed, passed, not_passed, not_passed);
	}

	private void assertLines(CobolCoverageMethod method, int... lines) {
		List<CobolCoverageLine> methodStatus = method.methodStatus();
		for (int i = 0; i < lines.length; i++) {
			assertThat(methodStatus.get(i).lineNumber()).isEqualTo(lines[i]);
		}
	}

	private void assertStatus(CobolCoverageMethod method, CoverageStatus... lines) {
		assertThat(method.methodStatus()).extracting(CobolCoverageLine::status).containsExactlyInAnyOrder(lines);
	}
}