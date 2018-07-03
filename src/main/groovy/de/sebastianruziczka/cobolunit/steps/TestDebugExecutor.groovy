package de.sebastianruziczka.cobolunit.steps

import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXECFILE_PATH
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.BUILD_TEST_EXEC_LOG_PATH

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.cobolunit.CobolUnitSourceFile
import de.sebastianruziczka.process.ProcessWrapper

class TestDebugExecutor {

	private CobolExtension configuration

	public TestDebugExecutor(CobolExtension configuration) {
		this.configuration = configuration
	}

	public String executeTest(CobolUnitSourceFile file) {
		file.setMeta(BUILD_TEST_EXEC_LOG_PATH, file.getMeta(BUILD_TEST_EXECFILE_PATH) + '_TESTEXEC.LOG')
		File executableDir = new File(file.getMeta(BUILD_TEST_EXECFILE_PATH)).getParentFile()
		ProcessWrapper processWrapper = new ProcessWrapper(
				[
					'cobcrun',
					'-M',
					new File(file.getMeta(BUILD_TEST_EXECFILE_PATH)).getName(),
					file.baseFileName()
				],
				executableDir,
				'Execute Unittest '+ file.getMeta(BUILD_TEST_EXECFILE_PATH),
				file.getMeta(BUILD_TEST_EXEC_LOG_PATH))

		processWrapper.setEnvironmentVariable('COB_EXIT_WAIT', 'off')
		if (this.configuration.unittestCodeCoverage) {
			processWrapper.setEnvironmentVariable('COB_TRACE_FILE', file.testCoverageFilePath())
			processWrapper.setEnvironmentVariable('COB_SET_TRACE', 'Y')
			processWrapper.setEnvironmentVariable('COBC_INTERACTIVE', 'N')

			for (String key : this.configuration.additionalRuntimeEnvironmentVariables.keySet()) {
				processWrapper.setEnvironmentVariable(key, this.configuration.additionalRuntimeEnvironmentVariables.get(key))
			}
		}

		processWrapper.exec(true)
		return processWrapper.processOutput()
	}
}
