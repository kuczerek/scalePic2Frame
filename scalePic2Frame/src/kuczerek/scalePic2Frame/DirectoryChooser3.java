package kuczerek.scalePic2Frame;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

import javax.swing.tree.DefaultMutableTreeNode;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;


public class DirectoryChooser3 extends JFrame implements ActionListener{
	
	private JCheckBoxTree cbt;
 	
	public DirectoryChooser3 () {
		super( "JTree - Demo" );
	}
	
    public void showExampleTree()
    {
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    	getList(root, new File(Specs.photoAlbumPath));
    	
        setSize(500, 500);
        this.getContentPane().setLayout(new BorderLayout());
        cbt = new JCheckBoxTree(root);
        this.add( new JScrollPane( cbt ), BorderLayout.CENTER);
        
        cbt.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
            public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {
                /*System.out.println("event");
                TreePath[] paths = cbt.getCheckedPaths();
                for (TreePath tp : paths) {
                    for (Object pathPart : tp.getPath()) {
                        System.out.print(pathPart + ",");
                    }                   
                    System.out.println();
                }*/
            }           
        });   
        
	    JButton okButton = new JButton("OK");
	    okButton.addActionListener(this);
	    this.add(okButton, BorderLayout.PAGE_END);
        
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        //this.pack();
        this.setLocationRelativeTo( null );
        this.setVisible( true );
    }       
    
    public void getList(DefaultMutableTreeNode node, File f) {
	    System.out.println("DIRECTORY  -  " + f.getName());
	    DefaultMutableTreeNode child = new DefaultMutableTreeNode(f);
	    node.add(child);
	    File fList[] = f.listFiles(File::isDirectory);
	    for(int i = 0; i  < fList.length; i++)
	        getList(child, fList[i]);
    }
    
	public ArrayList<Path> getChosenDirectories () {
	    
		ArrayList<Path> allFiles = new ArrayList<Path>();	   
	    return allFiles;
	}
    
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("OK")) {
			TreePath[] paths = cbt.getCheckedPaths();
            for (TreePath tp : paths) {
                for (Object pathPart : tp.getPath()) {
                    System.out.println(pathPart);
                }                   
                System.out.println();
            }
          //this.dispose();
		}
	}
    
}
