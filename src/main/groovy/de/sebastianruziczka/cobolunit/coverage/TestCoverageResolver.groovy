package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile

class TestCoverageResolver {

	private CobolExtension configuration

	public TestCoverageResolver(CobolExtension configuration) {
		this.configuration = configuration
	}


	public CobolCoverageFile resolve(List<CobolCoverageFile> files, List<String> logOuput) {
		TestCoverageMerger merger = new TestCoverageMerger()
		merger.merge(files, logOuput)
		return files
	}
}
