package de.sebastianruziczka.cobolunit.coverage

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile
import de.sebastianruziczka.cobolunit.coverage.sourcefilereader.SourceFileReader

class TestCoverageResolver {

	private CobolExtension configuration

	public TestCoverageResolver(CobolExtension configuration) {
		this.configuration = configuration
	}


	public CobolCoverageFile resolve(CobolUnitSourceFile file, List<String> logOuput) {
		CobolCoverageFile coverageFile = new SourceFileReader(this.configuration).read(file)

		TestCoverageMerger merger = new TestCoverageMerger()
		merger.merge(coverageFile, logOuput)
		return coverageFile
	}
}
