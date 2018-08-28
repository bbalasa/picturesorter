package ca.c3stechnology.picturesorter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class SorterApp implements ActionListener {

	private static final String DOT = ".";
	private static final String RENAME_FACTOR = "_1";
	private static final String BACKSLASH = "\\";
	private static final String DF = "yyyy-MMM-dd";
	private static final String NEWLINE = "\n";
	private static final String DEFAULT_NEW_PATH= "F:\\PozeSorted\\";
	private static final String DEFAULT_OLD_PATH= "F:\\Pict";
	private static final String[] PICT_EXTENSIONS = {"jpg", "JPG","jpeg", "JPEG", "png"};
	
	private JFrame frame;
	private JFileChooser fc;
	private JLabel lblNewLabel;
	private JTextArea txtrResultsWillBe;
	private JButton btnNewButton;
	private List<String> badFeedback = new ArrayList<String>();
	private int foldersProcessed;
	private long filesProcessed;
	private long filesNoInfo;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SorterApp window = new SorterApp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SorterApp() {
		initialize();
		initFileChooser();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0,	Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		lblNewLabel = new JLabel("Selected base folder...");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		frame.getContentPane().add(lblNewLabel, gbc_lblNewLabel);
		
				btnNewButton = new JButton("Select");
				btnNewButton.addActionListener(this);
				btnNewButton.setEnabled(true);
				GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
				gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
				gbc_btnNewButton.gridx = 1;
				gbc_btnNewButton.gridy = 1;
				frame.getContentPane().add(btnNewButton, gbc_btnNewButton);

		txtrResultsWillBe = new JTextArea();
		txtrResultsWillBe.setText("");
		GridBagConstraints gbc_txtrResultsWillBe = new GridBagConstraints();
		gbc_txtrResultsWillBe.gridwidth = 3;
		gbc_txtrResultsWillBe.gridheight = 2;
		gbc_txtrResultsWillBe.fill = GridBagConstraints.BOTH;
		gbc_txtrResultsWillBe.gridx = 0;
		gbc_txtrResultsWillBe.gridy = 2;
		frame.getContentPane().add(txtrResultsWillBe, gbc_txtrResultsWillBe);
	}

	private void initFileChooser() {
		fc = new JFileChooser();
		fc.setCurrentDirectory(new java.io.File(DEFAULT_OLD_PATH));
		fc.setDialogTitle("Only directories can be selected...");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setAcceptAllFileFilterUsed(false);
	}

	public void actionPerformed(ActionEvent e) {
		int returnVal = fc.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
           
   		    txtrResultsWillBe.append("Working directory: "+file.getAbsolutePath());
   		    txtrResultsWillBe.append(NEWLINE);
   		    processInitialDir(file);
   		    System.out.println("Processed a total of "+filesProcessed+" files.");
   		    System.out.println("A total of "+filesNoInfo+" files were not moved.");
   		    System.out.println("################## FAILURES ######################");
   		    for(String s : badFeedback){
   		    	System.out.println(s);
   		    }
		}
	}
	
	private void processDirectory(File initialFolder){
		Collection<File> fileList = FileUtils.listFiles(initialFolder, PICT_EXTENSIONS, true);
		for(File f : fileList){
			//System.out.println("Processing file: "+f.getAbsolutePath());
			processFile(f);
		}
	}
	
	private void processInitialDir(File initialFile){
		List<File> fileList = new ArrayList<File>(Arrays.asList(initialFile.listFiles()));
   		Collections.sort(fileList);
		for(File f : fileList){
			if(f.isDirectory()){
				processDirectory(f);	
	    	}else{
	    		if(PictureType.isPictureType(FilenameUtils.getExtension(f.getName())))
	    			processFile(f);
	    	}
		}
	}
	
    private void processFile(File initialFile) {

    		String extension = FilenameUtils.getExtension(initialFile.getAbsolutePath());
    		String fAbsN = initialFile.getAbsolutePath();
			
				Metadata metadata = null;
				try {
					metadata = ImageMetadataReader.readMetadata(initialFile);
				} catch (ImageProcessingException | IOException e) {
					e.printStackTrace();
				}
				ExifSubIFDDirectory directory = metadata.getDirectory(ExifSubIFDDirectory.class);
				if(directory != null){
					Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
					if(date != null){
						DateFormat df = new SimpleDateFormat(DF);
						String formattedDate = df.format(date);
						String moveDirStr = DEFAULT_NEW_PATH+formattedDate+BACKSLASH;
												
						try {
							FileUtils.moveFileToDirectory(initialFile, new File(moveDirStr), true);
						} catch (FileExistsException e) {
							try {
								renameAndMove(initialFile, extension, moveDirStr);
							} catch (IOException e1) {
								badFeedback.add("Failed to rename picture "+fAbsN+" It was not moved.");
								e1.printStackTrace();
								filesNoInfo+=1;
							}
						} catch (IOException e) {
							badFeedback.add("Failed to rename picture "+fAbsN+" It was not moved.");
							e.printStackTrace();
							filesNoInfo+=1;
						}
						filesProcessed+=1;
					}else{
						badFeedback.add("Picture "+fAbsN+" does not contain date taken metadata. It was not moved.");
						filesNoInfo+=1;
					}
					
				}else {
					badFeedback.add("Picture "+fAbsN+" does not contain metadata. It was not moved.");
					filesNoInfo+=1;
				}		
			
	}

	private void renameAndMove(File initialFile, String extension, String moveDirStr) throws IOException {
		String initialFileAbsPath = initialFile.getAbsolutePath();
		String initialFileName = initialFile.getName();
		String initialFileBaseName = FilenameUtils.getBaseName(initialFileAbsPath);
		
		
		File newDir = new File(moveDirStr);
		String newFileName = initialFileName;
		String newBaseName = initialFileBaseName;
		
		boolean rename = true;
		while (rename) {
			if(FileUtils.directoryContains(newDir, new File(moveDirStr+newFileName))){
				newBaseName = newBaseName+RENAME_FACTOR;
				newFileName = newBaseName+DOT+extension;
				continue;
			}
			
			File renamedFile = new File(moveDirStr+newFileName);
			FileUtils.moveFile(initialFile, renamedFile);
			rename = false;
						
			System.out.println("Renamed file: "+initialFileName+" to "+ newFileName);
		}
	}
}
