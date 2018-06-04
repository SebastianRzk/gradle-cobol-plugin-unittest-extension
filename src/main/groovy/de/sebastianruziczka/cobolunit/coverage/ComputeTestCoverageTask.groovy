package de.sebastianruziczka.cobolunit.coverage

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import de.sebastianruziczka.CobolExtension

class ComputeTestCoverageTask extends DefaultTask{

	public OutputParserTestCoverageDecorator testOuput
	public CobolExtension conf

	@TaskAction
	public void computeTestCoverage() {
		TestCoverageResolver testCoverageResolver = new TestCoverageResolver(this.conf)
		for (String file : this.testOuput.testCoverageFiles()) {
			testCoverageResolver.resolve(file, this.testOuput.getCoverageOutput(file))
		}
	}
}
