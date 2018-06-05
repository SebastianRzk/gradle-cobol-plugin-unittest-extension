package de.sebastianruziczka.cobolunit.coverage;

import java.util.List;

import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile;
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod;

class TestCoverageMerger {

	public CobolCoverageFile merge(CobolCoverageFile file, List<String> coverageOuptut) {

		CobolCoverageMethod actualMethod = null;
		int actualMethodOffset = 0;
		for (String line : coverageOuptut) {

			if ("Paragraph:".equals(line.substring(29, 39))) {
				actualMethod = getMethodForLine(line, file);
				actualMethodOffset = getTraceNumberFrom(line) + 1;
				continue;
			}

			if (actualMethod == null) {
				continue;
			}
			int tracedLineNumber = getTraceNumberFrom(line);
			actualMethod.setLineCoveredWithRelativeIndex(tracedLineNumber - actualMethodOffset);
		}

		return file;
	}

	private int getTraceNumberFrom(String line) {
		return Integer.parseInt(line.substring(69, 71));
	}

	private CobolCoverageMethod getMethodForLine(String line, CobolCoverageFile file) {
		String methodName = line.substring(40, 63).trim();
		for (CobolCoverageMethod method : file.methods()) {
			if (method.name().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
}
