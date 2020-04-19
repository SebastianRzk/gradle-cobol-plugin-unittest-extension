package de.sebastianruziczka.cobolunit.coverage

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.cobolunit.CobolUnit
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.cobolunit.coverage.report.XMLReportWriter
import de.sebastianruziczka.cobolunit.coverage.sourcefilereader.SourceFileReader

class ComputeTestCoverageTask extends DefaultTask{

	public Map<CobolUnitSourceFile, List<String>> testOuput
	public String coveragePrefix = ""

	@Input
	public String getCoveragePrefix() {
		return this.coveragePrefix
	}
	
	@Input
	public Map<CobolUnitSourceFile, List<String>> getTestOutput(){
		return this.testOuput
	}
	
	@TaskAction
	public void computeTestCoverage() {
		final CobolExtension conf = getProject().extensions.findByType(CobolExtension.class)
		
		def files = []
		Set<String> computedFiles = new HashSet<>()

		String sourceFileLocation = conf.projectFileResolver(conf.srcMainPath).absolutePath
		def allSourceFiles = project.fileTree(sourceFileLocation).include(conf.filetypePattern())
		allSourceFiles.each { File file ->
			String relativePath = file.absolutePath.replaceAll(sourceFileLocation, "").substring(1)
			files << new SourceFileReader(conf).read(new CobolUnitSourceFile(new CobolSourceFile(conf, relativePath), null, null, null))
		}

		if (getTestOutput() == null) {
			logger.warn('No testcoverage found!')
		}else {
			for (CobolUnitSourceFile file : getTestOutput().keySet()) {
				new TestCoverageMerger().merge(files, Arrays.asList(getTestOutput().get(file).split('\n')))
			}
		}

		String xml = new XMLReportWriter(conf).writeToXML(files)
		File xmlOutput = new File(conf.absoluteUnitTestFrameworkPath(CobolUnit.class.getSimpleName()) + '/' + getCoveragePrefix() + 'coverage.xml')
		xmlOutput << xml
	}
}
