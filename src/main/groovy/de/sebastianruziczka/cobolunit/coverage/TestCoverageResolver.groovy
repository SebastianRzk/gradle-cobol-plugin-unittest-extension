package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile

class TestCoverageResolver {

	private CobolExtension configuration

	public TestCoverageResolver(CobolExtension configuration) {
		this.configuration = configuration
	}


	public List<CobolCoverageFile> resolve(List<CobolCoverageFile> files, String logOuput) {
		TestCoverageMerger merger = new TestCoverageMerger()
		merger.merge(files, Arrays.asList(logOuput.split('\n')))
		return files
	}
}
