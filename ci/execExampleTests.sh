set -e

echo ">>>>>>> creating test-repository"
mkdir endToEndTest
cd endToEndTest
git clone https://github.com/RosesTheN00b/gradle-cobol-plugin-example
git clone https://github.com/RosesTheN00b/gradle-cobol-plugin
cd ..
echo "<<<<<<<"

echo ">>>>>>> create local repo for this jar"
gradle publish -PgenerateLatest
mv ../repo endToEndTest/repo
echo "<<<<<<<"

echo ">>>>>>> prepare test-repositories"
gradle generateVersions switchOffline
echo "<<<<<<<"

echo ">>>>>>> create local repo for gradle plugin jar"
cd endToEndTest/gradle-cobol-plugin
gradle publish -PgenerateLatest
cd ..
cd ..
echo "<<<<<<<"

echo ">>>>>>> install gnu cobol"
cd endToEndTest/gradle-cobol-plugin-example/
sh ci/install_requirements.sh
cd ..
cd ..
echo "<<<<<<<"


echo ">>>>>>> exec test"
cd endToEndTest/gradle-cobol-plugin-example/project-cobol-unit-test
gradle check
gradle testUnit computeTestCoverage  --refresh-dependencies --debug 
cat build/CobolUnit/coverage.xml
cd ..
cd ..
cd ..
echo "<<<<<<<"


echo ">>>>>>> exec test"
cd endToEndTest/gradle-cobol-plugin-example/project-cobol-integration-test
gradle check
gradle testIntegration computeIntegrationTestCoverage  --refresh-dependencies --debug
cat build/CobolUnit/coverage.xml
echo "<<<<<<<"

