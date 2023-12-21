JARNAME="GABot-1.0-all.jar"

cd ~
git clone https://github.com/timoxa0/GABot
cd GABot
chmod +x ./gradlew
./gradlew shadowJar
cp ~/GABot/build/libs/$JARNAME ~/GABot.jar
cd ~

echo "java -jar GABot.jar" > start.sh
echo "screen -dmS GABot java -jar GABot.jar" > startscreen.sh

chmod +x start{,screen}.sh

./start.sh