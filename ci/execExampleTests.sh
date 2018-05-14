set -e

echo ">>>>>>> creating test-repository"
mkdir endToEndTest
cd endToEndTest
git clone https://github.com/RosesTheN00b/gradle-cobol-plugin-example
git clone https://github.com/RosesTheN00b/gradle-cobol-plugin
cd gradle-cobol-plugin
git checkout dev
cd ..
echo "<<<<<<<"

echo ">>>>>>> create local repo for this jar"
cd ..
gradle publish
mv ../repo endToEndTest/repo
echo "<<<<<<<"

echo ">>>>>>> create local repo for gradle plugin jar"
cd endToEndTest/gradle-cobol-plugin
gradle publish
cd ..
cd ..
echo "<<<<<<<"


echo ">>>>>>> prepare test-repository"
gradle generateVersions switchOffline
echo "<<<<<<<"

echo ">>>>>>> exec test"
cd endToEndTest/gradle-cobol-plugin-example
gradle checkCobol
echo "<<<<<<<"
