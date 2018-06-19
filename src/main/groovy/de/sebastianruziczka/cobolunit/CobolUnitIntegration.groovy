package de.sebastianruziczka.cobolunit

import static de.sebastianruziczka.api.CobolCodeType.unit_test
import static de.sebastianruziczka.cobolunit.CobolUnitMetaKeys.ABSOLUTE_FIXED_UNITTEST_PATH

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import de.sebastianruziczka.CobolExtension
import de.sebastianruziczka.api.CobolCodeType
import de.sebastianruziczka.api.CobolIntegrationTestFrameworkProvider
import de.sebastianruziczka.api.CobolSourceFile
import de.sebastianruziczka.api.CobolTestFramework
import de.sebastianruziczka.buildcycle.test.TestFile
import de.sebastianruziczka.cobolunit.coverage.ComputeTestCoverageTask
import de.sebastianruziczka.cobolunit.coverage.OutputParserTestCoverageDecorator
import de.sebastianruziczka.cobolunit.coverage.linefix.FixedFileConverter
import de.sebastianruziczka.cobolunit.coverage.linefix.UnitTestLineFixer
import de.sebastianruziczka.cobolunit.steps.OutputParser
import de.sebastianruziczka.cobolunit.steps.TestDebugCompiler
import de.sebastianruziczka.cobolunit.steps.TestDebugExecutor
import de.sebastianruziczka.cobolunit.steps.ZUTZCPC
import de.sebastianruziczka.metainf.MetaInfPropertyResolver

@CobolIntegrationTestFrameworkProvider
class CobolUnitIntegration implements CobolTestFramework{
	Logger logger = LoggerFactory.getLogger('cobolUnit')

	private CobolExtension configuration
	private Project project
	private def defaultConf = ["ZUTZCWS", "SAMPLET"]
	private final static DEFAULT_CONF_NAME = 'DEFAULT.CONF'
	private String pluginName = null
	private OutputParserTestCoverageDecorator testCoverageProvider;
	private ZUTZCPC zutzcpc = null;

	@Override
	void configure(CobolExtension configuration, Project project) {
		this.configuration = configuration
		this.project = project

		this.project.task('computeIntegrationTestCoverage', type:ComputeTestCoverageTask){

			group: 'COBOL Development'
			description: 'Generates a testcoverage xml (cobertura-style)'

			doFirst{
				testOuput = this.testCoverageProvider
				conf = this.configuration
			}
		}
	}

	@Override
	int prepare() {
		logger.info('Setup ZUTZCPC test framework')
		this.zutzcpc = new ZUTZCPC(this.frameworkBin(), this.configuration)
		this.zutzcpc.setup()

		logger.info('Create default test.conf')
		this.createTestConf()
		return 0
	}

	private void createTestConf() {
		String path = this.defaultConfPath()
		logger.info('Using Path: ' + path)
		def defaultConfFile = new File(path)
		defaultConfFile.delete()

		defaultConfFile.withWriter { out ->
			this.defaultConf.each { out.println it }
		}
	}

	private String defaultConfPath() {
		return this.frameworkBin() + '/' + DEFAULT_CONF_NAME
	}

	@Override
	public TestFile test(CobolSourceFile file) {
		String testName = file.getRelativePath(unit_test)

		CobolUnitSourceFile unitSourceFile = new CobolUnitSourceFile(file, this.frameworkBin(), this.testBin(file) + '/' + file.getRelativePath(unit_test))

		String testBuildPath = this.testBin(file) + '/' + testName
		File buildTestModule = new File(this.getParent(testBuildPath))



		if (buildTestModule.exists()) {
			logger.info('Delete already existing integration folder')
			buildTestModule.delete()
			buildTestModule = new File(this.getParent(testBuildPath))
		}
		this.logger.info('Creating integration test directory ' + testBuildPath)
		buildTestModule.mkdirs()

		logger.info('Moving build files into integrationtestfolder')

		this.project.copy {
			from this.configuration.binMainPath
			into buildTestModule.absolutePath
		}

		this.project.copy{
			from unitSourceFile.getAbsolutePath(CobolCodeType.integration_test_ressources)
			into buildTestModule.absolutePath
		}


		logger.info('Preprocess Test: ' + testName)
		this.zutzcpc.preprocessTest(unitSourceFile, this.defaultConfPath(), CobolCodeType.unit_test)

		if(this.configuration.unittestCodeCoverage) {
			unitSourceFile.modifyTestModulePath(this.testBin(file) + '/' + new FixedFileConverter(this.configuration).fromOriginalToFixed(file.getRelativePath(unit_test)))
			file.setMeta(ABSOLUTE_FIXED_UNITTEST_PATH, this.configuration.projectFileResolver(unitSourceFile.actualTestfilePath()).absolutePath)

			new UnitTestLineFixer().fix(unitSourceFile)
		}

		logger.info('Compile Test: ' + unitSourceFile.actualTestfilePath())
		new TestDebugCompiler(this.configuration, this.frameworkBin()).compileTest(unitSourceFile)
		logger.info('Run Test: ' + unitSourceFile.actualTestfilePath())
		String result = new TestDebugExecutor(this.configuration).executeTest(unitSourceFile)

		return this.parseProcessOutput(result, unitSourceFile)
	}

	private TestFile parseProcessOutput(String processOutput, CobolUnitSourceFile file) {
		List<String> lines = Arrays.asList(processOutput.split(System.getProperty('line.separator')))
		OutputParser parser = new OutputParser(this.configuration)
		if (this.configuration.unittestCodeCoverage) {
			if (this.testCoverageProvider == null) {
				this.testCoverageProvider = new OutputParserTestCoverageDecorator(parser)
			}
			this.testCoverageProvider.parse(file, lines)
		}
		return parser.parse(file, lines)
	}

	private String frameworkBin() {
		return this.configuration.absoluteUnitTestFrameworkPath(CobolUnit.getSimpleName()) + '/integration'
	}

	private String testBin(CobolSourceFile test) {
		return this.frameworkBin() + '/' + test.baseFileName()
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
		this.testCoverageProvider = null
	}
}