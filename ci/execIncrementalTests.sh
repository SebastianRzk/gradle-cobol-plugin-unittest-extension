set -e

echo ">>>>>>> creating test-repository"
cd endToEndTest
cd gradle-cobol-plugin
git pull
cd ..
cd gradle-cobol-plugin-example
git pull
cd ..
echo "<<<<<<<"

echo ">>>>>>> create local repo for this jar"
rm -rvf ./repo
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
