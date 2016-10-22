import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.bean.playerbean.MediaPlayer;
import javax.media.control.FrameGrabbingControl;
import javax.media.control.FramePositioningControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Image;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang.ArrayUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
Audio tone generator, using the Java sampled sound API.
@author andrew Thompson
@version 2007/12/6
*/
public class BlindHelper extends JFrame {

  private static final long serialVersionUID = -7683199785998957180L;
  private static BufferedImage imgBuffer = null;
  private static int[][] grayValue = null;
  private static JPanel videoPanel = null;
  private static JImagePanel imgPanel = null;
  private static JImagePanel grayPanel = null;
  
  public BlindHelper() {
    super("Blind Helper");
    // Use current OS look and feel.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Internal Look And Feel Setting Error.");
            System.err.println(e);
        }

    JMenuBar menubar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem ImgMi = new JMenuItem("Load Image");
    JMenuItem VideoMi = new JMenuItem("Load Video");
    
    ImgMi.addActionListener((ActionEvent event) -> {
    	JFileChooser fileChooser = new JFileChooser();
    	URL mediaURL = null;
	    // show open file dialog
	    int result = fileChooser.showOpenDialog(null);
	    if (result == JFileChooser.APPROVE_OPTION) // user chose a file
	    {
	    	try {
	            // get the file as URL 
	            mediaURL = fileChooser.getSelectedFile().toURL();
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
	            mediaURL = fileChooser.getSelectedFile().toURL();
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
    });
    
    fileMenu.add(ImgMi);
    fileMenu.add(VideoMi);
    
    menubar.add(fileMenu);
    
    setJMenuBar(menubar);
    
        
    JPanel pMain=new JPanel(new BorderLayout());

    videoPanel = new JPanel(new BorderLayout());  
    //pMain.add(videoPanel, BorderLayout.CENTER);
    
    JButton play1frame = new JButton("play 1 frame");
    JButton playAll = new JButton("play all");


    final JCheckBox cbHarmonic  = new JCheckBox( "Add Harmonic", false );
    cbHarmonic.setToolTipText("..else pure sine tone");

    play1frame.addActionListener( new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          try{
            generateTone(cbHarmonic.isSelected());
          }catch(LineUnavailableException lue){
            System.out.println(lue);
          }
        }
      } );

    JPanel pNorth = new JPanel(new BorderLayout());
    pNorth.add(play1frame,BorderLayout.WEST);
    pNorth.add( playAll, BorderLayout.CENTER );
    pNorth.add( cbHarmonic, BorderLayout.EAST );

    



	imgPanel = new JImagePanel(null, 0, 0);
	grayPanel = new JImagePanel(null, 0, 0); 
	videoPanel.setMinimumSize(new Dimension(300, 300)); 
	imgPanel.setMinimumSize(new Dimension(200, 200)); 
	grayPanel.setMinimumSize(new Dimension(200, 200)); 
    //panel1.set[Preferred/Maximum/Minimum]Size()
	JPanel listContainer  = new JPanel();
	listContainer.setLayout(new BoxLayout(listContainer,BoxLayout.X_AXIS));
	listContainer.add(videoPanel);
	JPanel container  = new JPanel();
	container.setLayout(new BoxLayout(container,BoxLayout.Y_AXIS));
	container.add(imgPanel);
	container.add(grayPanel);
	listContainer.add(container);
    
    //pMain.add(imgPanel, BorderLayout.CENTER);
    pMain.add(listContainer, BorderLayout.CENTER);
    
    pMain.add(pNorth, BorderLayout.NORTH);
    pMain.setBorder( new EmptyBorder(5,3,5,3) );
    //setBounds(0, 0, 500, 500);

    getContentPane().add(pMain);
    pack();
    //this.setMinimumSize(imgBuffer.getWidth()+50, imgBuffer.getHeight()+150);
    setLocationByPlatform(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

//    String address = "/image/tone32x32.png";
//    URL url = getClass().getResource(address);
//
//    if (url!=null) {
//      Image icon = Toolkit.getDefaultToolkit().getImage(url);
//      setIconImage(icon);
//    }
  }

  private void loadMediaURL(URL mediaURL) throws IOException {
	
	  imgBuffer = ImageIO.read(mediaURL);
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
	  
	  //videoPanel.setImage(imgBuffer);
	  imgPanel.setImage(resizedImage);
	  grayPanel.setImage(grayImage);
	  
	  grayValue = normalizeImage(imgBuffer);
	  setSize(imgBuffer.getWidth() * 2, imgBuffer.getHeight()+100);
}
  
  private void loadVideoURL(URL mediaURL) throws IOException {
	    try {
	        Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, true);
	        Player mediaPlayer = Manager.createRealizedPlayer(mediaURL);
	        Component video = mediaPlayer.getVisualComponent();
	        Component controls = mediaPlayer.getControlPanelComponent();
	
	        if (video != null)
	
	        	videoPanel.add(video, BorderLayout.CENTER);
	
	        if (controls != null)
	        	videoPanel.add(controls, BorderLayout.SOUTH);
	
	        mediaPlayer.start();


    		mediaPlayer.getControl("javax.media.control.FramePositioningControl");
            FrameGrabbingControl fg =(FrameGrabbingControl)mediaPlayer.getControl("javax.media.control.FrameGrabbingControl");

            //mediaPlayer.prefetch();

            Buffer buf = fg.grabFrame();
            VideoFormat vf = (VideoFormat) buf.getFormat();
 
            BufferToImage bufferToImage = new BufferToImage(vf);
            Image im = bufferToImage.createImage(buf);

            BufferedImage formatImg = new BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR);
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
		  
		  //videoPanel.setImage(imgBuffer);
		  imgPanel.setImage(resizedImage);
		  grayPanel.setImage(grayImage);
		  
		  grayValue = normalizeImage(imgBuffer);
		  setSize(imgBuffer.getWidth() * 2, imgBuffer.getHeight()+100);
            
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
    for(int x =0;x<64;x++){
    	double[][] audio = new double[64][];
    	for(int y=0;y<64;y++)
    	{
    		int hz = 20 + y*60;
    		int volume = grayValue[x][y] * 6;
    		audio[y] = StdAudio.note(hz, secs, volume);
    		//StdAudio.play(audio[y]);

    	}
		for( int i=0; i<audio[0].length; ++ i ){
			for(int j=1;j<64;j++)
			{
				audio[0][i] += audio[j][i];
			}
			audio[0][i] /= 64 ;

    	}
		tone = (double[])ArrayUtils.addAll(tone, audio[0]);
    }
    
    StdAudio.play(tone);
  }
  
  private static int[][] normalizeImage(BufferedImage imgBuffer)
  {
  	BufferedImage resizedImage = new BufferedImage(64, 64, BufferedImage.TYPE_3BYTE_BGR);
  	Graphics2D graph = resizedImage.createGraphics();
  	graph.drawImage(imgBuffer, 0, 0, 64, 64, null);
  	graph.dispose();
  	int[][] grayValue = new int [64][64];
  	for(int i =0;i<64;i++)
  	{
  		for(int j = 0;j<64;j++)
  		{
  			int rgb = resizedImage.getRGB(i, j);
  	        int r = (rgb >> 16) & 0xFF;
  	        int g = (rgb >> 8) & 0xFF;
  	        int b = (rgb & 0xFF);
  	        grayValue[i][j] = (int) (0.299*r+0.587*g+0.114*b)/16;
  		}
  	}
  	return grayValue;
  }
  
	
	public static void main(String args[]) throws Exception {
		BlindHelper b = new BlindHelper();
	    b.setVisible(true);
	    		

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
	    @Override  
	    protected void paintComponent(Graphics g) {  
	        super.paintComponent(g);  
	        g.drawImage(image, x, y, this.getWidth(),this.getHeight(), this);  
	    }  
	}
}