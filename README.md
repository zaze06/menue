# Menue
A simple little menu/app system meant to be used whit a SSD1306 OLED display and 4+ buttons as inputs.

## How to use
- Connection setup
  - to be added
- Code
  - Compile
    1. Clone this repository
    2. open the file `src/main/java/me/alien/spotify/Spotify.java` line [797](https://github.com/zaze06/menue/blob/master/src/main/java/me/alien/spotify/Spotify.java#L797) and [798](https://github.com/zaze06/menue/blob/master/src/main/java/me/alien/spotify/Spotify.java#L797) and replace the tokens with the id's or empty string
       - if replaced by empty string the program `spotify` won't work
    3. to main directory and run the following commands
       - This part requires gradle
       1. `./gradlew shadowJar` or on Windows `./gradlew.bat shadowJar` in the head directory
    4. now you have a `menue-1.0-all.jar` located in `build/libs`
  - run
    1. put the `menue-1.0-all.jar` file on your raspberry pi and make sure that `pigpio` is installed on the pi
    2. start `menue-1.0-all.jar` whit java 17(or above should be supported)