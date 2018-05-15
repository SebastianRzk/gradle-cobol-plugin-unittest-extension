set -e

echo ">>>>>>> creating test-repository"
mkdir endToEndTest
cd endToEndTest
git clone https://github.com/RosesTheN00b/gradle-cobol-plugin-example
git clone https://github.com/RosesTheN00b/gradle-cobol-plugin
cd gradle-cobol-plugin
git checkout dev
cd ..
cd ..
echo "<<<<<<<"

echo ">>>>>>> create local repo for this jar"
gradle publish
mv ../repo endToEndTest/repo
echo "<<<<<<<"

echo ">>>>>>> prepare test-repositories"
gradle generateVersions switchOffline
echo "<<<<<<<"

echo ">>>>>>> create local repo for gradle plugin jar"
cd endToEndTest/gradle-cobol-plugin
gradle publish
cd ..
cd ..
echo "<<<<<<<"

echo ">>>>>>> exec test"
cd endToEndTest/gradle-cobol-plugin-example
gradle checkCobol
echo "<<<<<<<"
