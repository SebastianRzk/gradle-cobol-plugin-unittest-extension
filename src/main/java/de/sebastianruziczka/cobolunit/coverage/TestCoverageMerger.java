package de.sebastianruziczka.cobolunit.coverage;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.sebastianruziczka.cobolunit.coverage.model.CobolCoverageFile;

class TestCoverageMerger {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCoverageMerger.class.getName());
	private CobolTraceMode traceMode = null;

	public List<CobolCoverageFile> merge(List<CobolCoverageFile> files, List<String> coverageOuptut) {
		CobolCoverageFile initialFile = null;
		for (int i = 0; i < coverageOuptut.size() - 1; i++) {
			LOGGER.debug("-------------------------_");
			String line = coverageOuptut.get(i);
			String nextLine = coverageOuptut.get(i + 1);
			LOGGER.debug(line + ">>>" + nextLine);

			if (initialFile == null) {
				Optional<CobolCoverageFile> file = this.resolveFileForLine(files, line);
				if (file.isPresent()) {
					file.get().feedLineOfInitialFile(line, nextLine);
					initialFile = file.get();
				}
				continue;
			}
			Optional<CobolCoverageFile> newFile = this.resolveFileForLine(files, line);

			if (newFile.isPresent()) {
				if (newFile.get() == initialFile) {
					newFile.get().feedLineOfInitialFile(line, nextLine);
					continue;
				}
				newFile.get().feedLineOfCalledFile(line, nextLine);
			}
		}
		return files;
	}

	private Optional<CobolCoverageFile> resolveFileForLine(List<CobolCoverageFile> files, String line) {
		if (this.traceMode == null) {
			this.traceMode = CobolTraceMode.getTraceModeFor(line);
		}
		String programmID = this.traceMode.parseProgrammID(line);
		LOGGER.debug("Resolved Programm ID: " + programmID);
		for (CobolCoverageFile file : files) {
			if (file.simpleName().equals(programmID)) {
				return Optional.of(file);
			}
		}
		LOGGER.debug("No file with id found");
		LOGGER.debug("FoundFiles: ");
		LOGGER.debug(files.toString());
		return Optional.empty();
	}

}
