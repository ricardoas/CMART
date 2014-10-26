package com.cmart.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
 
import javax.imageio.ImageIO;
 
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.mediatool.IMediaListener;

public class VideoThumbnails {

	 public static final double SECONDS_BETWEEN_FRAMES = 10;
 
	    //private static final String inputFilename = "/Users/jingwang/apache-tomcat-7.0.14/webapps/video_9.flv";
	    //private static final String outputFilePrefix = "/Users/jingwang/apache-tomcat-7.0.14/webapps/image_9";
	     
	    private static final String folder = "/Users/jingwang/apache-tomcat-7.0.14/webapps/videos/";
	    private static String inputFilename = null;
	    private static String outputFilePrefix = null;
	    // The video stream index, used to ensure we display frames from one and
	    // only one video stream from the media container.
	    private static int mVideoStreamIndex = -1;
	     
	    // Time of last frame write
	    private static long mLastPtsWrite = Global.NO_PTS;
     
	    public static final long MICRO_SECONDS_BETWEEN_FRAMES =
	        (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);
	    
	    private static int framecount=0;
	 
	    
	    public static void makeVideoThumbnail(String inputFile, String outputFile){
	    	framecount = 0;
	    	inputFilename = folder+inputFile;
	    	outputFilePrefix = folder+outputFile;
	    	
	    	System.out.println("in VideoThumbnails.makeVideoTHumbnail(), inputFilename= "+inputFilename);
	    	System.out.println("in VideoThumbnails.makeVideoTHumbnail(), outputFIlePrefic= "+outputFilePrefix);
	    	System.out.println("in VideoTHumbnails.makeVideoThumbnail(), framecount= "+framecount);
	        
	    	IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);
	   	 
	        // stipulate that we want BufferedImages created in BGR 24bit color space
	        mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
	         
	        mediaReader.addListener(new ImageSnapListener());
	 
	        // read out the contents of the media file and
	        // dispatch events to the attached listener
	        while (mediaReader.readPacket() == null){
	        	//System.out.println("in VideoThumbnails.makeVideoThumbnail()  the while loop  framecount= "+framecount);
	        	if (framecount == 3) return;
	        }
	    }
	    
	    
	    private static class ImageSnapListener extends MediaListenerAdapter {
	 
	        public void onVideoPicture(IVideoPictureEvent event) {
	 
	            if (event.getStreamIndex() != mVideoStreamIndex) {
	                // if the selected video stream id is not yet set, go ahead an
	                // select this lucky video stream
	                if (mVideoStreamIndex == -1)
	                    mVideoStreamIndex = event.getStreamIndex();
	                // no need to show frames from this video stream
	                else
	                    return;
	            }
	 
	            // if uninitialized, back date mLastPtsWrite to get the very first frame
	            if (mLastPtsWrite == Global.NO_PTS)
	                mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;
	 
	            // if it's time to write the next frame
	            if (event.getTimeStamp() - mLastPtsWrite >=
	                    MICRO_SECONDS_BETWEEN_FRAMES) {
	                
	            	framecount++;
	            	System.out.println("in VideoThumbnails.onVideoPicture()  framecount is= "+framecount);
	            	if(framecount == 3){
	            	
	            		String outputFilename = dumpImageToFile(event.getImage());
	 
	            		// indicate file written
	            		//double seconds = ((double) event.getTimeStamp()) Global.DEFAULT_PTS_PER_SECOND;
	            		System.out.printf("in VideoThumbnails.onVideoPicture()  write the count: "+framecount+"file to folder");
	            	}
	                // update last write time
                mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
	            }
	 
	        }
	         
	        private String dumpImageToFile(BufferedImage image) {
	            try {
	                String outputFilename = outputFilePrefix +".gif";
	                ImageIO.write(image, "png", new File(outputFilename));
	                return outputFilename;
	            }
	            catch (IOException e) {
	            	e.printStackTrace();
	            	return null;
	            }
	        }
	    }
 
	}
