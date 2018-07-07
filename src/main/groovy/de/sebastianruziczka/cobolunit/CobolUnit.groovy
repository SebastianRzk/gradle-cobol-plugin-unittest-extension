package de.sebastianruziczka.cobolunit


import static de.sebastianruziczka.api.CobolCodeType.unit_test
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.ABSOLUTE_FIXED_UNITTEST_PATH

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolCodeType
import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.api.CobolTestFramework
import de.sebastianruziczka.api.CobolUnitFrameworkProvider
import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.cobolunit.coverage.ComputeTestCoverageTask
import de.sebastianruziczka.cobolunit.coverage.linefix.FixedFileConverter
import de.sebastianruziczka.cobolunit.coverage.linefix.UnitTestLineFixer
import de.sebastianruziczka.cobolunit.steps.OutputParser
import de.sebastianruziczka.cobolunit.steps.TestExectuableCompiler
import de.sebastianruziczka.cobolunit.steps.TestExectuableExecutor
import de.sebastianruziczka.cobolunit.steps.ZUTZCPC
import de.sebastianruziczka.metainf.MetaInfPropertyResolver

@CobolUnitFrameworkProvider
class CobolUnit implements CobolTestFramework{
	Logger logger = LoggerFactory.getLogger('cobolUnit')

	private CobolExtension configuration
	private Project project
	private def defaultConf = ["ZUTZCWS", "SAMPLET"]
	private final static DEFAULT_CONF_NAME = 'DEFAULT.CONF'
	private String pluginName = null
	private Map<CobolUnitSourceFile, List<String>> coverageOutput = new HashMap()
	private ZUTZCPC zutzcpc = null;

	@Override
	void configure(CobolExtension configuration, Project project) {
		this.configuration = configuration
		this.project = project

		ZUTZCPC zutzcpcInstance =  new ZUTZCPC(this.frameworkBase(), this.configuration)
		this.zutzcpc = zutzcpcInstance


		/**
		 * Maybe the precompiler task is already defined (by the integrationtest->configure)
		 */
		if(! project.tasks.findByName('compileZUTZCPC')){
			this.project.task('compileZUTZCPC', type:CompileZUTZCPC){
				project = project
				group = 'COBOL UNIT'
				description = 'Compiles the cobol unittest precompiler (ZUTZCPC)'
				zutzcpc = zutzcpcInstance
			}
		}
		this.project.tasks.testUnit.dependsOn << this.project.tasks.compileZUTZCPC

		this.project.task('computeTestCoverage', type:ComputeTestCoverageTask){
			group: 'COBOL Development'
			description: 'Generates a testcoverage xml (cobertura-style)'

			doFirst{
				testOuput = this.coverageOutput
				conf = this.configuration
			}
		}
	}

	@Override
	int prepare() {
		logger.info('Create default test.conf')
		this.createTestConf()
		return 0
	}

	private void createTestConf() {
		String path = this.defaultConfPath()
		logger.info('Using Path: '+path)
		def defaultConfFile = new File(path)
		defaultConfFile.delete()

		defaultConfFile.withWriter { out ->
			this.defaultConf.each { out.println it }
		}
	}

	private String defaultConfPath() {
		return this.frameworkBin() + DEFAULT_CONF_NAME
	}

	@Override
	public TestFile test(CobolSourceFile file) {
		String testName = file.getRelativePath(unit_test)

		TestCoverageIs coverage = TestCoverageIs.Disabled
		if (this.configuration.unittestCodeCoverage) {
			coverage = TestCoverageIs.Enabled
		}

		CobolUnitSourceFile unitSourceFile = new CobolUnitSourceFile(file,//
				this.frameworkBin(),//
				this.frameworkBin() + '/' + file.getRelativePath(unit_test),//
				coverage
				)

		String testBuildPath = this.frameworkBin() + '/' + testName
		File buildTestModule = new File(this.getParent(testBuildPath))
		if (!buildTestModule.exists()) {
			this.logger.info('Creating test directory ' + testBuildPath)
			buildTestModule.mkdirs()
		}

		logger.info('Preprocess Test: ' + testName)
		this.zutzcpc.preprocessTest(unitSourceFile, this.defaultConfPath(), CobolCodeType.unit_test)

		if(coverage == TestCoverageIs.Enabled) {
			unitSourceFile.modifyTestModulePath(this.frameworkBin() + '/' + new FixedFileConverter(this.configuration).fromOriginalToFixed(file.getRelativePath(unit_test)))
			file.setMeta(ABSOLUTE_FIXED_UNITTEST_PATH, this.configuration.projectFileResolver(unitSourceFile.actualTestfilePath()).absolutePath)

			new UnitTestLineFixer().fix(unitSourceFile)
		}

		logger.info('Compile Test: ' + unitSourceFile.actualTestfilePath())
		new TestExectuableCompiler(this.configuration, this.frameworkBin()).compileTest(unitSourceFile)
		logger.info('Run Test: ' + unitSourceFile.actualTestfilePath())
		String result = new TestExectuableExecutor(this.configuration).executeTest(unitSourceFile)

		return this.parseProcessOutput(result, unitSourceFile)
	}

	private TestFile parseProcessOutput(String processOutput, CobolUnitSourceFile file) {
		List<String> lines = Arrays.asList(processOutput.split(System.getProperty('line.separator')))
		OutputParser parser = new OutputParser(this.configuration)

		if (file.testCoverage() == TestCoverageIs.Enabled) {
			this.coverageOutput.put(file, new File(file.testCoverageFilePath()).text)
		}
		return parser.parse(file, lines)
	}

	private String frameworkBin() {
		return this.configuration.absoluteUnitTestFrameworkPath(this.getClass().getSimpleName())
	}

	private String frameworkBase() {
		return this.frameworkBin()
	}

	private String getParent(String path) {
		File file = new File(path)
		return file.getParent()
	}

	private String versionNumber() {
		MetaInfPropertyResolver resolver = new MetaInfPropertyResolver('gradle-cobol-plugin-unittest-extension')
		return resolver.get('Implementation-Version').orElse('No version found!') + ' (' + resolver.get('Build-Date').orElse('No date found') + ')'
	}


	@Override
	public String toString() {
		if (this.pluginName == null) {
			this.pluginName =  this.getClass().getSimpleName() + ' version: ' + versionNumber()
		}
		return this.pluginName
	}

	@Override
	public void clean() {
		this.coverageOutput = new HashMap()
	}
}