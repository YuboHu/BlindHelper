import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.CannotRealizeException;

import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;

import javax.media.control.FrameGrabbingControl;

import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import javax.sound.sampled.LineUnavailableException;
import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.ArrayUtils;


public class BlindHelper extends JFrame {
	  private static final long serialVersionUID = -7683199785998957180L;
	  private static BufferedImage imgBuffer = null;
	  private static int[][] grayValue = null;
	  private static JPanel videoPanel = null;
	  private static JImagePanel imgPanel = null;
	  private static JImagePanel grayPanel = null;
	  private static Player mediaPlayer = null;
	  
	  public BlindHelper() {
		  initialize();
	  }
	  private void initialize(){
		// Variable Initialization
		imgPanel = new JImagePanel(null, 0, 0);
		grayPanel = new JImagePanel(null, 0, 0);
		videoPanel = new JPanel(new BorderLayout());
		  
		// Set MenuBar
	    JMenuBar menubar = new JMenuBar();
	    JMenu fileMenu = new JMenu("File");
	    JMenuItem ImgMi = new JMenuItem("Load Image");
	    JMenuItem VideoMi = new JMenuItem("Load Video");
	    fileMenu.add(ImgMi);
	    fileMenu.add(VideoMi);
	    menubar.add(fileMenu);
	    setJMenuBar(menubar);
	    
	    // Set North JPanel
	    JPanel pNorth = new JPanel(new BorderLayout());
	    JButton play1frame = new JButton("play 1 frame");
	    JButton playAll = new JButton("play all");
	    JCheckBox cbHarmonic  = new JCheckBox( "Add Harmonic", false );
	    cbHarmonic.setToolTipText("..else pure sine tone");
	    pNorth.add(play1frame, BorderLayout.WEST);
	    pNorth.add(playAll, BorderLayout.CENTER);
	    pNorth.add(cbHarmonic, BorderLayout.EAST);
	    
	    JPanel pMain = new JPanel(new BorderLayout());
	    
	    // Container for video
		JPanel videoContainer  = new JPanel();
		videoContainer.setLayout(new BoxLayout(videoContainer,BoxLayout.X_AXIS));
		videoContainer.add(videoPanel);
		
		// Container for static image
		JPanel imageContainer  = new JPanel();
		imageContainer.setLayout(new BoxLayout(imageContainer,BoxLayout.X_AXIS));
		imageContainer.add(imgPanel);
		imageContainer.add(grayPanel);
	    
		// Main container has north continer
		// And has video container or image container
	    pMain.add(videoContainer, BorderLayout.CENTER);
	    pMain.add(pNorth, BorderLayout.NORTH);
	    
	    // Put main container
	    getContentPane().add(pMain);
	    setLocationByPlatform(true);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);
	    pack();
	    
	    ImgMi.addActionListener((ActionEvent event) -> {
	    	JFileChooser fileChooser = new JFileChooser();
	    	URL mediaURL = null;
	    	// show open file dialog
	    	int result = fileChooser.showOpenDialog(null);
	    	if (result == JFileChooser.APPROVE_OPTION) // user chose a file
	    	{
	    		try {
	    			// get the file as URL 
	    			mediaURL = fileChooser.getSelectedFile().toURI().toURL();
	    		} catch (MalformedURLException malformedURLException) {
	    			System.err.println("Could not create URL for the file");
	    		}
	    	}
		    try {
				loadMediaURL(mediaURL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    pMain.remove(videoContainer);
		    pMain.add(imageContainer, BorderLayout.CENTER);
	    });
    
	    VideoMi.addActionListener((ActionEvent event) -> {
	    	JFileChooser fileChooser = new JFileChooser();
	    	URL mediaURL = null;
		    // show open file dialog
		    int result = fileChooser.showOpenDialog(null);
		    if (result == JFileChooser.APPROVE_OPTION) // user chose a file
		    {
		    	try {
		            // get the file as URL 
		            mediaURL = fileChooser.getSelectedFile().toURI().toURL();
		        } catch (MalformedURLException malformedURLException) {
		            System.err.println("Could not create URL for the file");
		        }
		    }
		    try {
		    	loadVideoURL(mediaURL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    pMain.remove(imageContainer);
		    pMain.add(videoContainer, BorderLayout.CENTER);
	    });
	    
	    play1frame.addActionListener( new ActionListener() {
	        public void actionPerformed(ActionEvent ae) {
	          if(mediaPlayer!=null){
	        	    mediaPlayer.start();
			        try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        mediaPlayer.stop();
			        grabFrame();
	          }
	          try{
	            generateTone(cbHarmonic.isSelected());
	          }catch(LineUnavailableException lue){
	            System.out.println(lue);
	          }
	        }
	    });
	    
	    playAll.addActionListener( new ActionListener() {
	        public void actionPerformed(ActionEvent ae) {
	        	
	        	while(mediaPlayer!=null&&mediaPlayer.getMediaTime().getSeconds() < mediaPlayer.getDuration().getSeconds())
	        	{
	        		for(ActionListener a: play1frame.getActionListeners()) {
	        		    a.actionPerformed(new ActionEvent(play1frame,0,null));
	        		}
		        }
	        }
	        });
	  }
	  
	  public class JImagePanel extends JPanel{  
			private static final long serialVersionUID = -6751939089869082018L;
			private BufferedImage image;  
		    int x, y;  
		    public JImagePanel(BufferedImage image, int x, int y) {  
		        super();  
		        this.image = image;  
		        this.x = x;  
		        this.y = y;  
		    }  
		    public void setImage(BufferedImage image){
		    	this.image = image;
		    	repaint();
		    }
		    protected void paintComponent(Graphics g) {  
		        super.paintComponent(g);  
		        g.drawImage(image, x, y, this.getWidth(),this.getHeight(), this);  
		    }  
	  }
	
	  private void loadMediaURL(URL mediaURL) throws IOException {
		  imgBuffer = ImageIO.read(mediaURL);
		  int width = imgBuffer.getWidth()/2;
		  int height = imgBuffer.getHeight()/2;
		  
		  BufferedImage colorImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		  BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		  Graphics2D graph1 = colorImage.createGraphics();
		  graph1.drawImage(imgBuffer, 0, 0, width, height, null);
		  graph1.dispose();
		  Graphics2D graph2 = grayImage.createGraphics();
		  graph2.drawImage(imgBuffer, 0, 0, width, height, null);
		  graph2.dispose();
		  
		  //videoPanel.setImage(imgBuffer);
		  imgPanel.setImage(colorImage);
		  grayPanel.setImage(grayImage);
		  
		  grayValue = normalizeImage(imgBuffer);
		  setSize(imgBuffer.getWidth() * 2, imgBuffer.getHeight()+100);
	}
	  
		private void loadVideoURL(URL mediaURL) throws IOException {
			try {
				Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
		        mediaPlayer = Manager.createRealizedPlayer(mediaURL);
		        Component video = mediaPlayer.getVisualComponent();
		        Component controls = mediaPlayer.getControlPanelComponent();
		
		        if (video != null){
		        	videoPanel.add(video, BorderLayout.CENTER);
		        }
		
		        if (controls != null){
		        	videoPanel.add(controls, BorderLayout.SOUTH);
		        }
		
		        mediaPlayer.getGainControl().setMute(true);
		        mediaPlayer.start();
		        try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        mediaPlayer.stop();
		        grabFrame();
	    		
	            setSize(500, 500);    
			} catch (NoPlayerException noPlayerException) {
				System.err.println("No media player found");
		    } // end catch
		    catch (CannotRealizeException cannotRealizeException) {
		        System.err.println("Could not realize media player");
		    } // end catch
		    catch (IOException iOException) {
		        System.err.println("Error reading from the source");
		    }
		}
		
		private void grabFrame(){
			mediaPlayer.getControl("javax.media.control.FramePositioningControl");
            FrameGrabbingControl fg = (FrameGrabbingControl)mediaPlayer.getControl("javax.media.control.FrameGrabbingControl");

            //mediaPlayer.prefetch();

            Buffer buf = fg.grabFrame();
            VideoFormat vf = (VideoFormat) buf.getFormat();
 
            BufferToImage bufferToImage = new BufferToImage(vf);
            Image im = bufferToImage.createImage(buf);

            BufferedImage formatImg = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = formatImg.getGraphics();

            g.drawImage(im, 0, 0, 64, 64, null);
            g.dispose();
            
            imgBuffer = formatImg;
            int width = imgBuffer.getWidth()/2;
            int height = imgBuffer.getHeight()/2;
		  
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D graph1 = resizedImage.createGraphics();
            graph1.drawImage(imgBuffer, 0, 0, width, height, null);
            graph1.dispose();
            Graphics2D graph2 = grayImage.createGraphics();
            graph2.drawImage(imgBuffer, 0, 0, width, height, null);
            graph2.dispose();
		  
            grayValue = normalizeImage(imgBuffer);
            
            videoPanel.repaint();
		}
	  
		private static int[][] normalizeImage(BufferedImage imgBuffer) {
			BufferedImage resizedImage = new BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D graph = resizedImage.createGraphics();
			graph.drawImage(imgBuffer, 0, 0, 64, 64, null);
			graph.dispose();
		  	int[][] grayValue = new int [64][64];
		  	
		  	for(int i =0;i<64;i++) {
		  		for(int j = 0;j<64;j++) {
		  			int rgb = resizedImage.getRGB(i, j);
		  	        int r = (rgb >> 16) & 0xFF;
		  	        int g = (rgb >> 8) & 0xFF;
		  	        int b = (rgb & 0xFF);
		  	        grayValue[i][j] = (int) (0.299*r+0.587*g+0.114*b)/16;
		  		}
		  	}
		  	
		  	return grayValue;
		 }
	
		/** Generates a tone.
		@param hz Base frequency (neglecting harmonic) of the tone in cycles per second
		@param msecs The number of milliseconds to play the tone.
		@param volume Volume, form 0 (mute) to 100 (max).
		@param addHarmonic Whether to add an harmonic, one octave up. */
		public static void generateTone(boolean addHarmonic)
				throws LineUnavailableException {
			double secs = (double)1 / (double)64;
		    //float frequency = 44100;
			double[] tone = new double[0];
			
		    for(int x = 0;x < 64;x++) {
		    	double[][] audio = new double[64][];
		    	for(int y = 0;y < 64;y++) {
		    		double hz = 440 * Math.pow(2, (32-y)/12);
		    		int volume = grayValue[x][y] * 6;
		    		audio[y] = StdAudio.note(addHarmonic, hz, secs, volume);
		    		//StdAudio.play(audio[y]);
		    	}
				for(int i = 0; i < audio[0].length; ++i) {
					for(int j=1;j<64;j++){
						audio[0][i] += audio[j][i];
					}
					audio[0][i] /= 64 ;
		    	}
				tone = (double[])ArrayUtils.addAll(tone, audio[0]);
		    }
		    StdAudio.play(tone);
		}
		
	  public static void main(String args[]) throws Exception {
			BlindHelper b = new BlindHelper();
		    b.setVisible(true);    		
	  }
	  
}
