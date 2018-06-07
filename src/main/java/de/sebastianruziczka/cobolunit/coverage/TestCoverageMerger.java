package de.sebastianruziczka.cobolunit.coverage;

import java.util.List;

import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile;
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod;

class TestCoverageMerger {

	public CobolCoverageFile merge(CobolCoverageFile file, List<String> coverageOuptut) {

		CobolCoverageMethod actualMethod = null;
		int offsetDifference = -1;
		for (String line : coverageOuptut) {
			int tracedLineNumber = getTraceNumberFrom(line);

			if (offsetDifference == -1 && "Paragraph:".equals(line.substring(29, 39))) {
				actualMethod = getMethodForName(line, file);
				if (actualMethod != null) {
					offsetDifference = tracedLineNumber - actualMethod.startLine() + 1;
				}
				continue;
			}

			if (offsetDifference == -1 || "Paragraph:".equals(line.substring(29, 39))) {
				continue;
			}
			actualMethod = getMethodForLineNumber(tracedLineNumber - offsetDifference, file);

			if (actualMethod == null) {
				continue;
			}
			actualMethod
					.setLineCoveredWithRelativeIndex(tracedLineNumber - offsetDifference - actualMethod.startLine());
		}

		return file;

	}

	private CobolCoverageMethod getMethodForLineNumber(int lineNumber, CobolCoverageFile file) {
		for (CobolCoverageMethod method : file.methods()) {
			if (lineNumber >= method.startLine() && lineNumber <= method.endLine()) {
				return method;
			}
		}
		return null;
	}

	private int getTraceNumberFrom(String line) {
		if (line.length() < 70) {
			return -1;
		}
		return Integer.parseInt(line.substring(69).trim());
	}

	private CobolCoverageMethod getMethodForName(String line, CobolCoverageFile file) {
		String methodName = line.substring(40, 63).trim();
		for (CobolCoverageMethod method : file.methods()) {
			if (method.name().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
}
