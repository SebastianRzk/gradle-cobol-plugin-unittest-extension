package de.sebastianruziczka.cobolunit.coverage

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.CobolUnit
import de.sebastianruziczka.cobolunit.coverage.report.XMLReportWriter

class ComputeTestCoverageTask extends DefaultTask{

	public OutputParserTestCoverageDecorator testOuput
	public CobolExtension conf

	@TaskAction
	public void computeTestCoverage() {
		TestCoverageResolver testCoverageResolver = new TestCoverageResolver(this.conf)
		def files = []
		for (String file : this.testOuput.testCoverageFiles()) {
			files << testCoverageResolver.resolve(file, this.testOuput.getCoverageOutput(file))
		}
		String xml = new XMLReportWriter(this.conf).writeToXML(files)
		File xmlOutput = new File(this.conf.absoluteUnitTestFrameworkPath(CobolUnit.class.getSimpleName()) + '/cov.xml')
		xmlOutput << xml
	}
}
