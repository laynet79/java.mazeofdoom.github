package com.lthorup.maze;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
   
public enum SoundEffect {
	//LEVEL1("music/level1.wav"),
	//LEVEL2("music/level2.wav"),
	//LEVEL3("music/level3.wav"),
	DOOR_OPEN("sound/dooropen.wav"),
	DOOR_CLOSE("sound/doorclose.wav"),
	PUNCH("sound/punch.wav"),
	PISTOL("sound/pistol.wav"),
	SHOTGUN("sound/shotgun.wav"),
	ROCKET("sound/rocket.wav"),
	EXPLODE("sound/explode.wav"),
	FLAME("sound/flame.wav"),
	WALK_INTO_WALL("sound/intowall.wav"),
	PAIN("sound/pain.wav"),
	DEATH("sound/death.wav"),
	GROWL("sound/growl.wav");

   private Clip clip;
   
   SoundEffect(String soundFileName) {
      try {
         URL url = this.getClass().getClassLoader().getResource(soundFileName);
         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url);
         clip = AudioSystem.getClip();
         clip.open(audioInputStream);
      } catch (UnsupportedAudioFileException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (LineUnavailableException e) {
         e.printStackTrace();
      }
   }
   
   public void play() {
       if (clip.isRunning())
       	clip.stop();
       clip.setFramePosition(0);
       clip.start();
   }
   
   public void playContinuous() {
	   if (clip.isRunning())
		   clip.stop();
	   clip.setFramePosition(0);
	   clip.loop(Clip.LOOP_CONTINUOUSLY);
	   clip.start();
   }
   
   public void playIfNotRunning() {
	   if (! clip.isRunning()) {
	        clip.setFramePosition(0);
	        clip.start();
	   }
   }
      
   public void stop() {
	   if (clip.isRunning())
		   clip.stop();
   }
   
   // Optional static method to pre-load all the sound files.
   static void init() {
      values(); // calls the constructor for all the elements
   }
   /*
   import java.applet.Applet;
   import java.applet.AudioClip;
   import java.io.File;
    
   class Calc {
       public static void main(String[] args) throws Exception {
           AudioClip clip = Applet.newAudioClip(new File("test.wav").toURI().toURL());
           clip.loop();
           // Ctrl-C to exit...
       }
   }
   */
}
