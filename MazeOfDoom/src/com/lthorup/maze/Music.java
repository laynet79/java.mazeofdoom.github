package com.lthorup.maze;

import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public class Music implements Runnable {

	private String soundFileName;
	private float volume;
	private boolean loop;
	
	public Music(String fileName, float volume, boolean loop) {
		soundFileName = fileName;
		this.volume = volume;
		this.loop = loop;
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void run() {
		try {
			do
			{
				URL url = this.getClass().getClassLoader().getResource(soundFileName);
				AudioInputStream stream = AudioSystem.getAudioInputStream(url);
				AudioFormat format = stream.getFormat();
				if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED){
					format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
							format.getSampleRate(),
							format.getSampleSizeInBits() * 2,
							format.getChannels(),
							format.getFrameSize() * 2,
							format.getFrameRate(),
							true);
					stream = AudioSystem.getAudioInputStream(format, stream);
				}
				
				SourceDataLine.Info info = new DataLine.Info(
						SourceDataLine.class,
						stream.getFormat(),
						(int)(stream.getFrameLength() * format.getFrameSize()));
				
				SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(stream.getFormat());
				line.start();
				
				// set volume
				FloatControl volumeControl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
				volumeControl.setValue((float)(Math.log(volume/100.0f) / Math.log(10.0f) * 20.0f));
				
				long lastUpdate = System.currentTimeMillis();
				double sinceLastUpdate = (System.currentTimeMillis() - lastUpdate) / 1000.0d;
				
				double seconds = 2.0; // time to wait before playing
				while (sinceLastUpdate < seconds) {
					sinceLastUpdate = (System.currentTimeMillis() - lastUpdate) / 1000.0d;
				}
				
				int numRead = 0;
				byte[] buf = new byte[line.getBufferSize()];
				while ((numRead = stream.read(buf, 0, buf.length)) >= 0) {
					int offset = 0;
					while (offset < numRead) {
						offset += line.write(buf,  offset,  numRead - offset);
					}
				}
				line.drain();
				line.stop();
			} while (loop);			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
