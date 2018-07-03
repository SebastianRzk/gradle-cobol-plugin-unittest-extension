package de.sebastianruziczka.cobolunit.coverage

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.cobolunit.CobolUnit
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.cobolunit.coverage.report.XMLReportWriter
import de.sebastianruziczka.cobolunit.coverage.sourcefilereader.SourceFileReader

class ComputeTestCoverageTask extends DefaultTask{

	public Map<CobolUnitSourceFile, List<String>> testOuput
	public CobolExtension conf
	public String coveragePrefix = ""

	@TaskAction
	public void computeTestCoverage() {
		def files = []
		Set<String> computedFiles = new HashSet<>()

		String sourceFileLocation = this.conf.projectFileResolver(this.conf.srcMainPath).absolutePath
		def allSourceFiles = project.fileTree(sourceFileLocation).include(this.conf.filetypePattern())
		allSourceFiles.each { File file ->
			String relativePath = file.absolutePath.replaceAll(sourceFileLocation, "").substring(1)
			files << new SourceFileReader(this.conf).read(new CobolUnitSourceFile(new CobolSourceFile(this.conf, relativePath), null, null, null))
		}

		if (this.testOuput == null) {
			logger.warn('No testcoverage found!')
		}else {
			for (CobolUnitSourceFile file : this.testOuput.keySet()) {
				new TestCoverageMerger().merge(files, Arrays.asList(this.testOuput.get(file).split('\n')))
			}
		}

		String xml = new XMLReportWriter(this.conf).writeToXML(files)
		File xmlOutput = new File(this.conf.absoluteUnitTestFrameworkPath(CobolUnit.class.getSimpleName()) + '/' + this.coveragePrefix + 'coverage.xml')
		xmlOutput << xml
	}
}
