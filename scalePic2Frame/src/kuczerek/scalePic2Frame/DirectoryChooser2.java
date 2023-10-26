package kuczerek.scalePic2Frame;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import eu.essilab.lablib.checkboxtree.CheckboxTree;

public class DirectoryChooser2 extends JFrame {
	
	//https://essi-lab.eu/projectsSites/lablib-checkboxtree/index.html
	//https://github.com/lorebiga/CheckboxTree
	//https://itblackbelt.wordpress.com/2007/09/20/swing-based-tree-layouts-with-checkboxtree
	//https://essi-lab.eu/projectsSites/lablib-checkboxtree/apidocs/index.html
	
	public DirectoryChooser2 () {
		super( "JTree - Demo" );
	}
	
	public ArrayList<Path> getChosenDirectories () {
	    
		ArrayList<Path> allFiles = new ArrayList<Path>();	   
	    return allFiles;
	}
	

    public void showExampleTree()
    {
    	/*TreeNode root = createTree();

        // Das Model wird dem Konstruktor des JTrees übergeben
        //JTree tree = new JTree( root );
        CheckboxTree tree = new CheckboxTree(root);*/
    	
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    	getList(root, new File(Specs.photoAlbumPath));
    	CheckboxTree tree = new CheckboxTree(root);
      
        this.add( new JScrollPane( tree ));
        
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        this.pack();
        this.setLocationRelativeTo( null );
        this.setVisible( true );
    }       
    
    public void getList(DefaultMutableTreeNode node, File f) {
        if(f.isDirectory()) {
            System.out.println("DIRECTORY  -  " + f.getName());
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(f);
            node.add(child);
            File fList[] = f.listFiles();
            for(int i = 0; i  < fList.length; i++)
                getList(child, fList[i]);
        }
    }
    
    private TreeNode createDemoTree(){
        /*
         * Der Baum wird folgende Form haben:
         * Wurzel
         * +- Buchstaben
         * |  +- A
         * |  +- B
         * |  +- C
         * +- Zahlen
         *    +- 1
         *    +- 2
         *    +- 3
         */
        
        // Zuerst werden alle Knoten hergestellt...
        DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Wurzel" );
        
        DefaultMutableTreeNode letters = new DefaultMutableTreeNode( "Buchstaben" );
        DefaultMutableTreeNode digits = new DefaultMutableTreeNode( "Zahlen" );
        
        DefaultMutableTreeNode letterA = new DefaultMutableTreeNode( "A" );
        DefaultMutableTreeNode letterB = new DefaultMutableTreeNode( "B" );
        DefaultMutableTreeNode letterC = new DefaultMutableTreeNode( "C" );
        
        DefaultMutableTreeNode digit1 = new DefaultMutableTreeNode( "1" );
        DefaultMutableTreeNode digit2 = new DefaultMutableTreeNode( "2" );
        DefaultMutableTreeNode digit3 = new DefaultMutableTreeNode( "3" );
        
        // ... dann werden sie verknüpft
        letters.add( letterA );
        letters.add( letterB );
        letters.add( letterC );
        
        digits.add( digit1 );
        digits.add( digit2 );
        digits.add( digit3 );
        
        root.add( letters );
        root.add( digits );
        
        return root;
     }
}
