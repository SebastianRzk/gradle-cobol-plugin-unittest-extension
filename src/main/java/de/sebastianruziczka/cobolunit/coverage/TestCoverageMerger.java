package de.sebastianruziczka.cobolunit.coverage;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile;
import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageMethod;

class TestCoverageMerger {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCoverageMerger.class.getName());
	private CobolTraceMode traceMode = null;

	public CobolCoverageFile merge(CobolCoverageFile file, List<String> coverageOuptut) {

		CobolCoverageMethod actualMethod = null;
		int offsetDifference = -1;
		for (int i = 0; i < coverageOuptut.size() - 1; i++) {
			String line = coverageOuptut.get(i);

			LOGGER.debug("-------------------------_");
			LOGGER.debug(line + ">>>" + coverageOuptut.get(i + 1));

			int tracedLineNumber = getTraceNumberFrom(line, coverageOuptut.get(i + 1));

			if (offsetDifference == -1 && this.traceMode.isParagraph(line)) {
				actualMethod = getMethodForName(this.traceMode.parseParagraphName(line), file);
				if (actualMethod != null) {
					offsetDifference = tracedLineNumber - actualMethod.startLine() + 1;
					if (this.traceMode == CobolTraceMode.gnucobol1) {// Fix if first statement is not in first line
						offsetDifference = offsetDifference - actualMethod.firstStatement();
					}
				}
				continue;
			}

			LOGGER.debug(tracedLineNumber + "");
			LOGGER.debug("Offset " + offsetDifference);
			LOGGER.debug("traced: " + (tracedLineNumber - offsetDifference));

			if (offsetDifference == -1 || this.traceMode.isParagraph(line)) {
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

	private int getTraceNumberFrom(String line, String followingLine) {
		if (this.traceMode == null) {
			this.traceMode = CobolTraceMode.getTraceModeFor(line);
			LOGGER.info("INIT Tracemode: " + this.traceMode);
		}
		return this.traceMode.getLineNumberFor(line, followingLine);
	}

	private CobolCoverageMethod getMethodForName(String methodName, CobolCoverageFile file) {
		for (CobolCoverageMethod method : file.methods()) {
			if (method.name().equals(methodName)) {
				return method;
			}
		}
		return null;
	}
}
