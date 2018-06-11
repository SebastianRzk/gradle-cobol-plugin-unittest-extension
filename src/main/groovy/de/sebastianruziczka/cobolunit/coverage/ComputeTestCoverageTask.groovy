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
		Set<String> computedFiles = new HashSet<>()

		if (this.testOuput == null) {
			logger.warn('No testcoverage found!')
		}else {
			for (String file : this.testOuput.testCoverageFiles()) {
				files << testCoverageResolver.resolve(file, this.testOuput.getCoverageOutput(file))
				String testFileName =  new FixedFileConverter(this.conf).fromFixedToRelative(file)
				computedFiles << testFileName.replace(this.conf.unittestPostfix + this.conf.srcFileType, this.conf.srcFileType)
			}
		}

		String sourceFileLocation = this.conf.projectFileResolver(this.conf.srcMainPath).absolutePath
		def allSourceFiles = project.fileTree(sourceFileLocation).include(this.conf.filetypePattern())
		allSourceFiles.each { File file ->
			String relativePath = file.absolutePath.replaceAll(sourceFileLocation, "").substring(1)

			if (!computedFiles.contains(relativePath)){
				logger.info('Read not covered file: ' + relativePath)
				files << new SourceFileReader(this.conf).read(relativePath)
			}
		}




		String xml = new XMLReportWriter(this.conf).writeToXML(files)
		File xmlOutput = new File(this.conf.absoluteUnitTestFrameworkPath(CobolUnit.class.getSimpleName()) + '/coverage.xml')
		xmlOutput << xml
	}
}
