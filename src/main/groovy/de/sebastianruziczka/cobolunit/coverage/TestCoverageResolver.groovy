package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile

class TestCoverageResolver {

	private CobolExtension configuration

	public TestCoverageResolver(CobolExtension configuration) {
		this.configuration = configuration
	}


	public CobolCoverageFile resolve(CobolSourceFile file, List<String> logOuput) {
		CobolCoverageFile coverageFile = new SourceFileReader(this.configuration).read(file)

		TestCoverageMerger merger = new TestCoverageMerger()
		merger.merge(coverageFile, logOuput)
		return coverageFile
	}
}
