/*    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


//Create interactive java gui to display CG cluster data
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//import io libraries individually
//to avoid conflicting with swing filechooser
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.*;
import java.lang.Math.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.filechooser.*;

//for java 1.6
import javax.swing.GroupLayout.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.*;
import org.jfree.data.xy.*;
import org.jfree.data.general.*;
import org.jfree.data.statistics.*;
import org.jfree.data.function.*;
import org.jfree.ui.*;

public class CGgui {

/*debug switch*/
boolean DEBUG = true;

//initialize swing frame
private JFrame f = new JFrame("Cluster Analyzer");
private JPanel fCenter = new JPanel();
private JPanel fEast = new JPanel();

//initialize menu
private JMenuBar mb = new JMenuBar();
private JMenu mnuFile = new JMenu("File");
private JMenuItem mnuItemOpen = new JMenuItem("Load Sequence");
private JMenuItem mnuItemOpenDataFile = new JMenuItem("Import Saved Data");
private JMenuItem mnuItemSaveDataFile = new JMenuItem("Export Data");
private JMenuItem mnuSaveCharts = new JMenu("Save Chart");
private JMenuItem mnuItemSaveChrt = new JMenuItem("Save Histogram");
private JMenuItem mnuItemSaveGrayChrt = new JMenuItem("Save Grayscale Histogram");
private JMenuItem mnuItemSaveMinChrt = new JMenuItem("Save Minima Chart");
private JMenuItem mnuItemSaveGrayMinChrt = new JMenuItem("Save Grayscale Minima Chart");
private JMenuItem mnuItemSaveClusterChrt = new JMenuItem("Save Optimization Chart");
private JMenuItem mnuItemSaveGrayClusterChrt = new JMenuItem("Save Grayscale Optimization Chart");
private JMenuItem mnuItemClearData = new JMenuItem("Clear Data");
private JMenuItem mnuItemSaveData = new JMenuItem("Save Cluster Locations");
private JMenuItem mnuItemQuit = new JMenuItem("Quit");
private JMenu mnuEdit = new JMenu("Edit");
private JMenuItem mnuItemChartProps = new JMenuItem("Histogram Properties");
private JMenuItem mnuItemMinimaProps = new JMenuItem("Minima Chart Properties");
private JMenuItem mnuItemClusterProps = new JMenuItem("Optimization Chart Properties");
private JCheckBoxMenuItem mnuItemFindMin = new JCheckBoxMenuItem("Find Minimum", true);
private JCheckBoxMenuItem mnuItemSetAxes = new JCheckBoxMenuItem("Set Domain");
private JCheckBoxMenuItem mnuItemShowGrid = new JCheckBoxMenuItem("Show Gridlines", true);
//private JMenu mnuHelp = new JMenu("Help");
//private JMenuItem mnuItemMan = new JMenuItem("Manual");
//private JMenuItem mnuItemAbout = new JMenuItem("About");

//initialize progress bar
private JProgressBar jprogressbar = new JProgressBar();

//initialize text area
private JPanel textPanel = new JPanel();
private JTextArea SeqText = new JTextArea("Welcome to Cluster Analyzer\nBuild Date Dec. 28th 2009\n\n",10,60);
private JScrollPane jScrollPane = new JScrollPane(SeqText);

//initialize CG Settings Panel
private int MinCG = 15;
private int MaxCG = 60;
private int CurrCG = 30;
private int CGstep = 5;
private JPanel CGPanel = new JPanel();
private JPanel CGsubPanel1 = new JPanel();
private JPanel CGsubPanel2 = new JPanel();
private JPanel CGsubPanel3 = new JPanel();
private JLabel minCGsLabel = new JLabel("Minimum    ");
private JLabel maxCGsLabel = new JLabel("Maximum    ");
private JLabel CGstepLabel = new JLabel("Step Size    ");
private JLabel CGsetCurrLabel = new JLabel("Graphed");
private JLabel searchPatternLabel = new JLabel("Search pattern");
private JCheckBox caseCheckBox = new JCheckBox("Case Sensitve", false);
private JCheckBox smoothCheckBox = new JCheckBox("Smoothing Factor", true);
private JTextField minCGsText = new JTextField(Integer.toString(MinCG),17);
private JTextField maxCGsText = new JTextField(Integer.toString(MaxCG),17);
private JTextField CGstepText = new JTextField(Integer.toString(CGstep),17);
private JTextField smoothText = new JTextField(null,17);
private JTextField searchPatternText = new JTextField("CG", 17);
private JTextField CGcurrText = new JTextField(Integer.toString(CurrCG),5);
private JButton CGApplyButton = new JButton("Apply");
private JButton CGResetButton = new JButton("Reset");
private JButton CGsetCurrButton = new JButton("Go");

//initialize minimum panel
private JPanel findminPanel = new JPanel();
private JButton minGrabButton = new JButton("Grab");
private JButton maxGrabButton = new JButton("Grab");
private JButton ClearButton = new JButton("Clear");
private JButton CalcButton = new JButton("Find");
private JButton CalcAllButton = new JButton("Find All");
private JButton CancelButton = new JButton("Cancel");
private JLabel minLabel = new JLabel("Lower Bound");
private JLabel maxLabel = new JLabel("Upper Bound");
private JTextField minText = new JTextField(null,5);
private JTextField maxText = new JTextField(null,5);

//axes tool
private JPanel setaxesPanel = new JPanel();
private JLabel mindomainLabel = new JLabel("Minimum");
private JLabel maxdomainLabel = new JLabel("Maximum");
private JTextField mindomainText = new JTextField(null,5);
private JTextField maxdomainText = new JTextField(null,5);
private JButton SetDomainButton = new JButton("Set Domain");
private JButton ResetDomainButton = new JButton("Reset Domain");

//initialize chart area
private JTabbedPane ChartArea = new JTabbedPane();
private HistogramDataset histogramdataset = new HistogramDataset();
private CategoryTableXYDataset minimadataset = new CategoryTableXYDataset();
private CategoryTableXYDataset clusterdataset = new CategoryTableXYDataset();
private ChartPanel chartpanel, minchartpanel, clusterchartpanel;
private JFreeChart chart, minchart, clusterchart;

//initialize axes info, data to be graphed
private double MIN = 0;
private double MAX = 0;
private double CurrMin;
private int BINS = 100000;
private  Comparable key = "";
private double[] Lengths;
private Integer[] StartLen;
private Integer[] ChromNumPos;
private String[] ChromNum;
private Function2D regLine;

//data to be processed
private File[] g_files;
private CGdataset cgdataset = new CGdataset();
private int TotSeries = 0;
private int TotDataLength = 0;
private int prevLength = 0;
private int NumNull = 0;
private IntervalMarker intervalmarker;

public class CGdataset
{
	//initialize data
	public String header;
	public Integer[] CGdata;
	public CGdataset Next;

	public CGdataset()
    {
		header = null;
		CGdata = null;
		Next = null;
	}
}

//constructor
public CGgui()
{
	//main window frame
	f.setResizable(false);
	f.getContentPane().setLayout(new BorderLayout());
	f.getContentPane().add(fCenter, BorderLayout.CENTER);
	f.getContentPane().add(fEast, BorderLayout.EAST);

	//menu
	mnuFile.add(mnuItemOpen);
    mnuFile.add(mnuItemSaveDataFile);
	mnuFile.add(mnuItemOpenDataFile);
    mnuFile.add(mnuSaveCharts);
	mnuSaveCharts.add(mnuItemSaveChrt);
  	mnuSaveCharts.add(mnuItemSaveMinChrt);
	mnuSaveCharts.add(mnuItemSaveClusterChrt);
    mnuSaveCharts.add(mnuItemSaveGrayChrt);
  	mnuSaveCharts.add(mnuItemSaveGrayMinChrt);
	mnuSaveCharts.add(mnuItemSaveGrayClusterChrt);
	mnuFile.add(mnuItemSaveData);
	mnuFile.add(mnuItemClearData);
	mnuFile.add(mnuItemQuit);
	mnuEdit.add(mnuItemChartProps);
	mnuEdit.add(mnuItemMinimaProps);
	mnuEdit.add(mnuItemClusterProps);
	mnuEdit.add(mnuItemFindMin);
	mnuEdit.add(mnuItemSetAxes);
    mnuEdit.add(mnuItemShowGrid);
//	mnuHelp.add(mnuItemMan);
//	mnuHelp.add(mnuItemAbout);
	mb.add(mnuFile);
	mb.add(mnuEdit);
//	mb.add(mnuHelp);
	f.setJMenuBar(mb);

	//progressbar
	jprogressbar.setVisible(false);
	jprogressbar.setBorderPainted(false);
	f.getContentPane().add(jprogressbar, BorderLayout.SOUTH);

	//chart area
    //histogram
	ChartArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	chart = ChartFactory.createHistogram("Fragment Length Histogram",
		"Fragment Length (l)", "Frequency f(l)", histogramdataset,
		PlotOrientation.VERTICAL, true, true, false);
        chart.addSubtitle(0,new TextTitle("n = " + CurrCG));
	XYPlot xyplot = (XYPlot)chart.getPlot();
	xyplot.setForegroundAlpha(0.85F);
    xyplot.setDomainCrosshairVisible(false);
	chartpanel = new ChartPanel(chart);
	ChartArea.add("Fragment Length",chartpanel);

    //minima
	minchart = ChartFactory.createScatterPlot("Local Minima",
		"Fragment Length (l)", "Number of Matches per Fragment", minimadataset,
		PlotOrientation.VERTICAL, true, true, false);
	XYPlot minxyplot = (XYPlot)minchart.getPlot();
	minxyplot.setForegroundAlpha(0.85F);
	minchartpanel = new ChartPanel(minchart);
	ChartArea.add("Minima",minchartpanel);

    //optimization
	clusterchart = ChartFactory.createScatterPlot("Average Cluster Size vs. Maximum Fragment Length",
		"Number (n) of Matches per Fragment", "Number of Overlapping Fragments per Cluster/Max Fragment Length", clusterdataset,
		PlotOrientation.VERTICAL, true, true, false);
	minxyplot.setForegroundAlpha(0.85F);
	clusterchartpanel = new ChartPanel(clusterchart);
	ChartArea.add("Optimization", clusterchartpanel);

	//text area
    SeqText.setLineWrap(true);
    SeqText.setWrapStyleWord(true);
    SeqText.setEditable(false);
	jScrollPane.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Debug"),
                                BorderFactory.createEmptyBorder(5,5,5,5)),
                jScrollPane.getBorder()));
	textPanel.add(jScrollPane);
	//this next line is a hack to fix the ScrollPane size when the chart is scrolled
	jScrollPane.setPreferredSize(jScrollPane.getPreferredSize());
    //auto scroll to added text
    SeqText.setAutoscrolls(true);
    jScrollPane.setAutoscrolls(true);

	//center panel layout
	GroupLayout centerlayout = new GroupLayout(fCenter);
 	fCenter.setLayout(centerlayout);
	////Create a sequential and a parallel groups
	SequentialGroup h1 = centerlayout.createSequentialGroup();
	SequentialGroup v1 = centerlayout.createSequentialGroup();
	////grouping
	h1.addGroup(centerlayout.createParallelGroup().addComponent(ChartArea).
            addComponent(textPanel));
	centerlayout.setHorizontalGroup(h1);
	////more grouping
	v1.addGroup(centerlayout.createParallelGroup().addComponent(ChartArea));
	v1.addGroup(centerlayout.createParallelGroup().addComponent(textPanel));
	centerlayout.setVerticalGroup(v1);

	//CG Panel (search settings panel)
	CGPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Search Settings"),
               	BorderFactory.createEmptyBorder(5,5,5,5)),
                CGPanel.getBorder()));
    //set subpanel1 layout
	GroupLayout CGsublayout1 = new GroupLayout(CGsubPanel1);
 	CGsubPanel1.setLayout(CGsublayout1);
	////Create a sequential and a parallel groups
	SequentialGroup h4 = CGsublayout1.createSequentialGroup();
	SequentialGroup v4 = CGsublayout1.createSequentialGroup();
    ////grouping
	h4.addGroup(CGsublayout1.createParallelGroup().
		addComponent(searchPatternLabel).addComponent(minCGsLabel).
        addComponent(maxCGsLabel).addComponent(CGstepLabel).
        addComponent(smoothCheckBox).addComponent(CGApplyButton).
        addComponent(CGsetCurrLabel));
	h4.addGroup(CGsublayout1.createParallelGroup().
		addComponent(searchPatternText).addComponent(minCGsText).
        addComponent(maxCGsText).addComponent(CGstepText).
        addComponent(smoothText).addComponent(CGResetButton).
        addComponent(CGcurrText));
	CGsublayout1.setHorizontalGroup(h4);
	////more grouping
	v4.addGroup(CGsublayout1.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(searchPatternLabel).addComponent(searchPatternText));
    v4.addContainerGap(5,5);
	v4.addGroup(CGsublayout1.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(minCGsLabel).addComponent(minCGsText));
    v4.addContainerGap(5,5);
	v4.addGroup(CGsublayout1.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(maxCGsLabel).addComponent(maxCGsText));
    v4.addContainerGap(5,5);
	v4.addGroup(CGsublayout1.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(CGstepLabel).addComponent(CGstepText));
    v4.addContainerGap(5,5);
	v4.addGroup(CGsublayout1.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(smoothCheckBox).addComponent(smoothText));
    v4.addContainerGap(5,5);
    v4.addGroup(CGsublayout1.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(CGApplyButton).addComponent(CGResetButton));
	v4.addContainerGap(15,15);
	v4.addGroup(CGsublayout1.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(CGsetCurrLabel).addComponent(CGcurrText));
	CGsublayout1.setVerticalGroup(v4);

    //set subpanel2 layout
    GroupLayout CGsublayout2 = new GroupLayout(CGsubPanel2);
 	CGsubPanel2.setLayout(CGsublayout2);
	////Create a sequential and a parallel groups
	SequentialGroup hSettingsSub2 = CGsublayout2.createSequentialGroup();
	SequentialGroup vSettingsSub2 = CGsublayout2.createSequentialGroup();

    ////horizontal grouping
	hSettingsSub2.addGroup(CGsublayout2.createParallelGroup().
		addComponent(CGsetCurrLabel));
    hSettingsSub2.addContainerGap(25,25);
	hSettingsSub2.addGroup(CGsublayout2.createParallelGroup().
        addComponent(CGcurrText));
    hSettingsSub2.addContainerGap(25,25);
	hSettingsSub2.addGroup(CGsublayout2.createParallelGroup().
            addComponent(CGsetCurrButton));
	CGsublayout2.setHorizontalGroup(hSettingsSub2);
	////vertical grouping
	vSettingsSub2.addGroup(CGsublayout2.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(CGsetCurrLabel).addComponent(CGcurrText).
        addComponent(CGsetCurrButton));
	CGsublayout2.setVerticalGroup(vSettingsSub2);

	//set overall layout (subpanel3)
	GroupLayout CGsublayout3 = new GroupLayout(CGsubPanel3);
 	CGsubPanel3.setLayout(CGsublayout3);
	////Create a sequential and a parallel groups
	SequentialGroup h5 = CGsublayout3.createSequentialGroup();
	SequentialGroup v5 = CGsublayout3.createSequentialGroup();
	////grouping
	h5.addGroup(CGsublayout3.createParallelGroup().
		addComponent(caseCheckBox).addComponent(CGsubPanel1).
        addComponent(CGsubPanel2));
	CGsublayout3.setHorizontalGroup(h5);
	////more grouping
	v5.addGroup(CGsublayout3.createParallelGroup().addComponent(caseCheckBox));
    v5.addContainerGap(5,5);
	v5.addGroup(CGsublayout3.createParallelGroup().addComponent(CGsubPanel1));
    v5.addContainerGap(5,5);
	v5.addGroup(CGsublayout3.createParallelGroup().addComponent(CGsubPanel2));
    CGsublayout3.setVerticalGroup(v5);
	//add to resizable container
	CGPanel.add(CGsubPanel3);
	//button and slider listeners
	CGApplyButton.addActionListener(new ListenCGApplyButton());
	CGResetButton.addActionListener(new ListenCGResetButton());
	CGsetCurrButton.addActionListener(new ListenCGsetCurrButton());
    CGcurrText.addActionListener(new ListenCGsetCurrButton());

	//minimum area
	findminPanel.setVisible(true);
	findminPanel.setBorder(
           	BorderFactory.createCompoundBorder(
               	BorderFactory.createCompoundBorder(
               	               BorderFactory.createTitledBorder("Find Minimum"),
               	               BorderFactory.createEmptyBorder(5,5,5,5)),
               	findminPanel.getBorder()));
	GroupLayout minlayout = new GroupLayout(findminPanel);
 	findminPanel.setLayout(minlayout);

    ////Create a sequential and a parallel groups
	SequentialGroup h2 = minlayout.createSequentialGroup();
	SequentialGroup v2 = minlayout.createSequentialGroup();

    ////group find minimum and find all buttons
    SequentialGroup hFindMinButtons = minlayout.createSequentialGroup();
    SequentialGroup vFindMinButtons = minlayout.createSequentialGroup();
    hFindMinButtons.addGroup(minlayout.createParallelGroup().
            addComponent(CalcButton));
    hFindMinButtons.addGroup(minlayout.createParallelGroup().
            addComponent(CalcAllButton));
    vFindMinButtons.addGroup(minlayout.createParallelGroup(
            GroupLayout.Alignment.BASELINE).
            addComponent(CalcButton).addComponent(CalcAllButton));
	////general grouping
	h2.addGroup(minlayout.createParallelGroup().
		addComponent(minLabel).addComponent(maxLabel).
		addComponent(ClearButton));
    h2.addContainerGap(5,5);
	h2.addGroup(minlayout.createParallelGroup().
		addComponent(minText).addComponent(maxText).
        addGroup(hFindMinButtons));
    h2.addContainerGap(5,5);
    h2.addGroup(minlayout.createParallelGroup().
		addComponent(minGrabButton).addComponent(maxGrabButton).
		addComponent(CancelButton));
	minlayout.setHorizontalGroup(h2);
	////more grouping
	v2.addGroup(minlayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
		addComponent(minLabel).addComponent(minText).
		addComponent(minGrabButton));
    v2.addContainerGap(5,5);
	v2.addGroup(minlayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
		addComponent(maxLabel).addComponent(maxText).
		addComponent(maxGrabButton));
    v2.addContainerGap(5,5);
    v2.addContainerGap(5,5);
    v2.addGroup(minlayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
		addComponent(ClearButton).addGroup(vFindMinButtons).
		addComponent(CancelButton));
	minlayout.setVerticalGroup(v2);
	//add button listeners
	ClearButton.addActionListener(new ListenClearButton());
	CalcButton.addActionListener(new ListenCalcButton());
	CalcAllButton.addActionListener(new ListenCalcAllButton());
	CancelButton.addActionListener(new ListenCancelButton());
	minGrabButton.addActionListener(new ListenminGrabButton());
	maxGrabButton.addActionListener(new ListenmaxGrabButton());

	//axes panel
	mnuItemSetAxes.setState(false);
	setaxesPanel.setVisible(false);
	setaxesPanel.setBorder(
		BorderFactory.createCompoundBorder(
		BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Set Domain"),
               	                BorderFactory.createEmptyBorder(5,5,5,5)),
               	setaxesPanel.getBorder()));
	GroupLayout setaxeslayout = new GroupLayout(setaxesPanel);
 	setaxesPanel.setLayout(setaxeslayout);
	////Create a sequential and a parallel groups
	SequentialGroup h6 = setaxeslayout.createSequentialGroup();
	SequentialGroup v6 = setaxeslayout.createSequentialGroup();
	////grouping
	h6.addGroup(setaxeslayout.createParallelGroup().
		addComponent(mindomainLabel).addComponent(maxdomainLabel).
		addComponent(SetDomainButton));
    h6.addContainerGap(5,5);
	h6.addGroup(setaxeslayout.createParallelGroup().
		addComponent(mindomainText).addComponent(maxdomainText).
		addComponent(ResetDomainButton));
	setaxeslayout.setHorizontalGroup(h6);
	////more grouping
	v6.addGroup(setaxeslayout.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(mindomainLabel).addComponent(mindomainText));
    v6.addContainerGap(5,5);
    v6.addGroup(setaxeslayout.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(maxdomainLabel).addComponent(maxdomainText));
    v6.addContainerGap(5,5);
    v6.addGroup(setaxeslayout.createParallelGroup(
        GroupLayout.Alignment.BASELINE).
		addComponent(SetDomainButton).addComponent(ResetDomainButton));
	setaxeslayout.setVerticalGroup(v6);
	//add button listeners
	SetDomainButton.addActionListener(new ListenSetDomainButton());
	ResetDomainButton.addActionListener(new ListenResetDomainButton());

	//east panel layout
	GroupLayout eastlayout = new GroupLayout(fEast);
 	fEast.setLayout(eastlayout);
	////Create a sequential and a parallel groups
	SequentialGroup h3 = eastlayout.createSequentialGroup();
	SequentialGroup v3 = eastlayout.createSequentialGroup();
	////grouping
	h3.addGroup(eastlayout.createParallelGroup()
		.addComponent(CGPanel).addComponent(findminPanel)
		.addComponent(setaxesPanel));
	eastlayout.setHorizontalGroup(h3);
	////more grouping
	v3.addGroup(eastlayout.createParallelGroup().addComponent(CGPanel));
	v3.addGroup(eastlayout.createParallelGroup().addComponent(findminPanel));
	v3.addGroup(eastlayout.createParallelGroup().addComponent(setaxesPanel));
	eastlayout.setVerticalGroup(v3);

	//listen for exit signals
	f.addWindowListener(new ListenCloseWdw());
	mnuItemQuit.addActionListener(new ListenMenuQuit());

	//listen for exit signals
	f.addWindowListener(new ListenCloseWdw());
	mnuItemClearData.addActionListener(new ListenClearData());

	//listen for open signal
	mnuItemOpen.addActionListener(new ListenMenuOpen());

	//listen for import signal
	mnuItemOpenDataFile.addActionListener(new ListenMenuOpenDataFile());

	//listen for import signal
	mnuItemSaveDataFile.addActionListener(new ListenMenuSaveDataFile());

	//listen for save histogram chart signal
	mnuItemSaveChrt.addActionListener(new ListenMenuSaveChrt());

	//listen for save chart signal
	mnuItemSaveMinChrt.addActionListener(new ListenMenuSaveMinChrt());

	//listen for save cluster chart signal
	mnuItemSaveClusterChrt.addActionListener(new ListenMenuSaveClusterChrt());

    //listen for save histogram chart in grayscale signal;
    mnuItemSaveGrayChrt.addActionListener(new ListenMenuSaveGrayChrt());

	//listen for save chart signal
	mnuItemSaveGrayMinChrt.addActionListener(new ListenMenuSaveGrayMinChrt());

	//listen for save cluster chart signal
	mnuItemSaveGrayClusterChrt.addActionListener(
            new ListenMenuSaveGrayClusterChrt());

	//listen for edit histogram properties signal
	mnuItemChartProps.addActionListener(new ListenMenuChartProps());

	//listen for edit minima chart properties signal
	mnuItemMinimaProps.addActionListener(new ListenMenuMinimaProps());

	//listen for edit optimization chart properties signal
	mnuItemClusterProps.addActionListener(new ListenMenuClusterProps());

	//listen for save data signal
	mnuItemSaveData.addActionListener(new ListenMenuSaveData());

	//listen for find minimum signal
	mnuItemFindMin.addActionListener(new ListenMenuFindMin());

	//listen for find minimum signal
	mnuItemSetAxes.addActionListener(new ListenMenuSetAxes());

    //listen for show gridlines
    mnuItemShowGrid.addActionListener(new ListenMenuShowGrid());

	//other menu items

	//garbage collect
	System.gc();
}

public class ListenCGApplyButton implements ActionListener, Runnable{
	public void actionPerformed(ActionEvent e){
		ListenCGApplyButton t = new ListenCGApplyButton();
		new Thread(t).start();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			jprogressbar.updateUI();
		}
		});
	}
	public void run() {
		//set up progress bar
		jprogressbar.setVisible(true);
                jprogressbar.setIndeterminate(true);
                jprogressbar.setStringPainted(true);
                jprogressbar.setString("Applying changes...");
                PrintText("Applying changes...\n");
                f.pack();

                try {
			//set new boundaries
			MinCG = Integer.parseInt(minCGsText.getText());
			MaxCG = Integer.parseInt(maxCGsText.getText());
			CGstep = Integer.parseInt(CGstepText.getText());

			//make sure current is not too high
			if (CurrCG>MaxCG)
				CurrCG = MaxCG;

			//debug lines
			if (DEBUG) {
				PrintText("Searching for fragments between " + MinCG);
				PrintText(" and " + MaxCG + " long\n");
				PrintText("Display: " + CurrCG + " CGs \n");
			}

                        updateData();
		}
		catch (NumberFormatException err) {
			//print error
			if (DEBUG) PrintText("Error: " + err + "\n");

			//force reset
			minCGsText.setText(Integer.toString(MinCG));
			maxCGsText.setText(Integer.toString(MaxCG));
			CGstepText.setText(Integer.toString(CGstep));
			return;
		}
        }
}

public class ListenCGResetButton implements ActionListener{
	public void actionPerformed(ActionEvent e){
		//set text
		minCGsText.setText(Integer.toString(MinCG));
		maxCGsText.setText(Integer.toString(MaxCG));
		CGstepText.setText(Integer.toString(CGstep));
	}
}

public class ListenCGsetCurrButton implements ActionListener,Runnable
{
	public void actionPerformed(ActionEvent e)
    {
		ListenCGsetCurrButton t = new ListenCGsetCurrButton();
		new Thread(t).start();
		javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                jprogressbar.updateUI();
            }
		});
	}
	public void run()
    {
        try
        {
            int newCG = Integer.parseInt(CGcurrText.getText());
            //check for no change or negative value
            if ((CurrCG != newCG) && (newCG > 0))
            {
                CurrCG = Integer.parseInt(CGcurrText.getText());
            }
            else return;

            //passed checks so update
            updateData();
        }
        catch (NumberFormatException err)
        {
            if (DEBUG) PrintText("Error: " + err + "\n");
        }
        catch (IndexOutOfBoundsException err)
        {
            if (DEBUG) PrintText("Error: " + err + "\n");
        }
    }
}

private void updateData(){
    //set up progress bar
    jprogressbar.setVisible(true);
    jprogressbar.setIndeterminate(true);
    jprogressbar.setStringPainted(true);
    jprogressbar.setString("Updating data...");
    PrintText("Updating data...\n");
    f.pack();

    //recalculate data
    //rebuild arrays
    Lengths = null;
    StartLen = null;
    prevLength = 0;
    NumNull = 0;
    MIN = 0;
    MAX = 0;

    //grab last min for adjusting bounds
    double prevMin = CurrMin;

    //get lengths
    for (int j = 0; j < TotSeries; j++) {
	cgdataset = cgdataset.Next;

        //find number of series for which no lengths will be found
        if (cgdataset.CGdata != null) {
            if ((CurrCG >= cgdataset.CGdata.length) || (cgdataset.CGdata == null)) {
                NumNull++;
            }
        }
        else NumNull++;

        getLengths();
        ChromNumPos[j] = prevLength;
    }

    //graph
    replaceData(key,Lengths);

    //label chart
    chart.clearSubtitles();
    chart.addSubtitle(0,new TextTitle("n = " + CurrCG));

    //adjust min and max bounds
    adjustBounds(prevMin);

    //report new domain
    mindomainText.setText(Double.toString(MIN));
    maxdomainText.setText(Double.toString(MAX));

    //get rid of progress bar
    jprogressbar.setVisible(false);

    //garbage collect
    System.gc();
}

public void adjustBounds(double prevMin) {
	//recalculate the min finding bounds
	if ((mnuItemFindMin.getState() == true) && (regLine != null)) {
		//keep boundary range, but recenter. Default radius: 100
		double lowbound,highbound;
		try {
			lowbound = prevMin - Double.parseDouble(minText.getText());
			highbound = Double.parseDouble(maxText.getText()) - prevMin;
		}
		catch (NumberFormatException err) {
                        lowbound = 100;
			highbound = 100;
		}
		double center = regLine.getValue(CurrCG);
		if (DEBUG) {
			PrintText("With center " + center + "\n");
		}
		minText.setText(Double.toString(center - lowbound));
		maxText.setText(Double.toString(center + highbound));
		intervalmarker.setStartValue(center - lowbound);
		intervalmarker.setEndValue(center + highbound);
		PrintText("Bounds: " + (center - lowbound) + ":" + (center + highbound) + "\n");
	}
}

public class ListenCalcAllButton implements ActionListener, Runnable {
    	public void actionPerformed(ActionEvent e){
		ListenCalcAllButton t = new ListenCalcAllButton();
		new Thread(t).start();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
			jprogressbar.updateUI();
		}
		});
	}
	public void run() {
                for (CurrCG = MinCG; CurrCG <= MaxCG; CurrCG += CGstep) {
        	        //update data (automatically adjusts bounds)
                        CGcurrText.setText(""+CurrCG);
                        updateData();

                        //find min
			FindMin();
                }

		//garbage collect
		System.gc();
	}
}

public class ListenCalcButton implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
        FindMin();

		//garbage collect
		System.gc();
	}
}

public void FindMin ()
{
	try
    {
		//get the data
		XYPlot xyplot = (XYPlot)chart.getPlot();
		XYDataset xydataset = xyplot.getDataset();

		//search through and find lowest minimum
		double MinXVal = -1;
		double MinYVal = -1;
		double lowbound = Double.parseDouble(minText.getText());
		double highbound = Double.parseDouble(maxText.getText());
		if (DEBUG) PrintText("Minimum of " + xydataset.getItemCount(0) + " datapoints\n");
		for (int i = 0; i < xydataset.getItemCount(0); i++)
        {
			double XVal = xydataset.getXValue(0,i);
			if ((XVal > lowbound) && (XVal < highbound))
            {
				double YValCurr = xydataset.getYValue(0,i);
				if ((YValCurr < MinYVal) || (MinYVal == -1))
                {
					MinYVal = YValCurr;
					MinXVal = xydataset.getXValue(0,i);
				}
			}
		}

                //print out, save, and mark off min
		if (DEBUG) PrintText("Minimum located at (" + MinXVal + "," + MinYVal + ")\n");
		addMinima(MinXVal);
                CurrMin = MinXVal;
		xyplot.setDomainCrosshairValue(MinXVal);

		double AveLength = AveCGLength(MinXVal);
		if (DEBUG) PrintText("Average Number of CGs in Cluster: " + AveLength + "\n");
		addClusterSize(AveLength);
	}
	catch (NumberFormatException err)
    {
		if (DEBUG) PrintText("Error: " + err + "\n");
	}
	catch (IndexOutOfBoundsException err)
    {
		if (DEBUG) PrintText("Error: " + err + "\n");
	}
}

public class ListenCancelButton implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		findminPanel.setVisible(false);
		f.pack();
		mnuItemFindMin.setState(false);
	}
}

public class ListenClearButton implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		//remove marker
		XYPlot xyplot = (XYPlot)chart.getPlot();
		xyplot.removeDomainMarker(intervalmarker, Layer.BACKGROUND);
		intervalmarker = null;

		//clear text
		minText.setText(null);
		maxText.setText(null);
	}
}

public class ListenmaxGrabButton implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		XYPlot xyplot = (XYPlot)chart.getPlot();
		double X = xyplot.getDomainCrosshairValue();
		maxText.setText(Double.toString(X));

		if (intervalmarker == null)
        {
			intervalmarker = new IntervalMarker(X, X);
			intervalmarker.setPaint(Color.LIGHT_GRAY);
			xyplot.addDomainMarker(intervalmarker, Layer.BACKGROUND);
		}
		intervalmarker.setEndValue(X);
	}
}

public class ListenminGrabButton implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		XYPlot xyplot = (XYPlot)chart.getPlot();
		double X = xyplot.getDomainCrosshairValue();
		minText.setText(Double.toString(X));

		if (intervalmarker == null)
        {
			intervalmarker = new IntervalMarker(X, X);
			intervalmarker.setPaint(Color.LIGHT_GRAY);
			xyplot.addDomainMarker(intervalmarker, Layer.BACKGROUND);

		}
		intervalmarker.setStartValue(X);
	}
}

public class ListenSetDomainButton implements ActionListener{
	public void actionPerformed(ActionEvent e){
		try
        {
			MIN = Double.parseDouble(mindomainText.getText());
			MAX = Double.parseDouble(maxdomainText.getText());
            if (MIN < MAX)
                replaceData(key, Lengths);
            else if (DEBUG) PrintText("Error: Min !< Max\n");
		}
		catch (NumberFormatException err) {
			if (DEBUG) PrintText("Error: " + err + "\n");
		}
		catch (IndexOutOfBoundsException err) {
			if (DEBUG) PrintText("Error: " + err + "\n");
		}
	}
}

public class ListenResetDomainButton implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		//reset text
		mindomainText.setText(Double.toString(MIN));
		maxdomainText.setText(Double.toString(MAX));
	}
}

public class ListenMenuFindMin implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		if (mnuItemFindMin.getState() == true)
        {
			if (DEBUG) PrintText("Opening minimum finder\n");
			XYPlot xyplot = (XYPlot)chart.getPlot();
			xyplot.setDomainCrosshairVisible(true);
			xyplot.setDomainCrosshairLockedOnData(false);
			findminPanel.setVisible(true);
			ChartArea.add("Minima",minchartpanel);
			ChartArea.add("Optimization", clusterchartpanel);
			f.pack();
		}
		else
        {
			if (DEBUG) PrintText("Closing minimum finder\n");
			findminPanel.setVisible(false);
			ChartArea.remove(minchartpanel);
			ChartArea.remove(clusterchartpanel);
			XYPlot xyplot = (XYPlot)chart.getPlot();
                        xyplot.setDomainCrosshairVisible(false);
			f.pack();
		}
		//garbage collect
		System.gc();
	}
}

public class ListenMenuSetAxes implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		if (mnuItemSetAxes.getState() == true)
        {
			if (DEBUG) PrintText("Opening domain settings\n");
                        mindomainText.setText(Double.toString(MIN));
                        maxdomainText.setText(Double.toString(MAX));
			setaxesPanel.setVisible(true);
			f.pack();
		}
		else
        {
			if (DEBUG) PrintText("Closing domain settings\n");
			setaxesPanel.setVisible(false);
			f.pack();
		}
		//garbage collect
		System.gc();
	}
}

//toggle gridlines on and off
public class ListenMenuShowGrid implements ActionListener
{
    public void actionPerformed(ActionEvent e)
    {
        //get plot
        XYPlot xyplot = (XYPlot)chart.getPlot();

        //find menu item state and act accordingly
        if(mnuItemShowGrid.getState() == true)
        {
            if (DEBUG) PrintText("Turning gridlines on\n");
            xyplot.setDomainGridlinesVisible(true);
            xyplot.setRangeGridlinesVisible(true);
        }
        else
        {
            if (DEBUG) PrintText("Turning gridlines off\n");
            xyplot.setDomainGridlinesVisible(false);
            xyplot.setRangeGridlinesVisible(false);
        }
    }
}

//save cluster locations
public class ListenMenuSaveData implements ActionListener,Runnable
{
	public void actionPerformed(ActionEvent e)
    {
		ListenMenuSaveData t = new ListenMenuSaveData();
		new Thread(t).start();
		javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                jprogressbar.updateUI();
            }
		});
	}
	public void run()
    {
        String wd = System.getProperty("user.dir");
		JFileChooser fc;
		int rc;

		//save CGdata
		fc = new JFileChooser(wd);
        fc.setDialogType(fc.SAVE_DIALOG);
		rc = fc.showDialog(null, "Save CG Clusters");
		if (rc == JFileChooser.APPROVE_OPTION)
		{
            //set up progress bar
            jprogressbar.setVisible(true);
            jprogressbar.setIndeterminate(true);
            jprogressbar.setStringPainted(true);
            f.pack();

            jprogressbar.setString("Saving CG cluster locations...");

            if (DEBUG) PrintText("Saving CG Clusters for N = " + CurrCG + "\n");
			File file = fc.getSelectedFile();

            //locate the minimum on the chart
            if (minimadataset.getItemCount() != 0)
            {
                boolean hasMin = false;
                for (int i = 0; i < minimadataset.getItemCount(); i++)
                {
                    if (minimadataset.getYValue(0,i) == CurrCG)
                    {
                        SaveCGCluster(minimadataset.getXValue(0,i),file);
                        hasMin = true;
                        break;
                    }
                }
                if (!hasMin) PrintText("No minimum recorded for the current number of CGs\n");
            }
                        else if (DEBUG) PrintText("No minima data for computation\n");
		}
		else if(DEBUG) PrintText("Cancel Save\n");

                //get rid of progress bar
		jprogressbar.setVisible(false);

		//garbage collect
		System.gc();
		return;
	}
}

public class ListenMenuChartProps implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		chartpanel.doEditChartProperties();
	}
}

public class ListenMenuMinimaProps implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		minchartpanel.doEditChartProperties();
	}
}

public class ListenMenuClusterProps implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		clusterchartpanel.doEditChartProperties();
	}
}

private class FileExtFilter extends FileFilter
{
    //filter for *.fa files
    public boolean accept(File f)
    {
        if (f.isDirectory())
        {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null)
        {
            if (extension.equals("fa")) return true;
            else return false;
        }
        return false;
    }

    //Filter name/description
    public String getDescription()
    {
        return "Fasta Files";
    }
}

//find the extension of a given file
private String getExtension(File f)
{
    if(f != null)
    {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if(i>0 && i<filename.length()-1)
        {
            return filename.substring(i+1).toLowerCase();
        }
    }
    return null;
}

//get the chromosome number from a given header
//assumes header is of the form <chr##_*
private String getChromNumber(String header)
{
        if(f != null)
        {
                int i = header.indexOf('r');
                int j = header.lastIndexOf('_');

                if((i>0 && i<header.length()-1) && (j>i && j<=header.length()-1))
                {
                        return header.substring(i+1,j);
                }
                else if(i>0 && i<header.length()-1)
                {
                        return header.substring(i+1);
                }
        }
        if (DEBUG) PrintText("Could not find chromosome number\n");
        return null;
}

public class ListenMenuOpen implements ActionListener, Runnable
{
    public void actionPerformed(ActionEvent e)
    {
        ListenMenuOpen t = new ListenMenuOpen();
        new Thread(t).start();
        javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    jprogressbar.updateUI();
                }
            });
    }
    public void run()
    {
        String wd = System.getProperty("user.dir");
	JFileChooser fc = new JFileChooser(wd);

	//allow for multiple files
	fc.setMultiSelectionEnabled(true);

        //set filter
        fc.addChoosableFileFilter(new FileExtFilter());
        fc.setAcceptAllFileFilterUsed(false);

	int rc = fc.showDialog(null, "Open");
	if (rc == JFileChooser.APPROVE_OPTION)
	{
            g_files = fc.getSelectedFiles();
            loadFiles(g_files);
	}
	else if(DEBUG) PrintText("Cancel Open\n");

	//garbage collect
	System.gc();
	return;
    }
}

public void loadFiles(File[] files)
{
    //clear previous graph
    clearData();

    //initialize data
    Lengths = null;
    StartLen = null;
    TotDataLength = 0;
    prevLength = 0;
    NumNull = 0;
    CGdataset firstcgdataset = cgdataset;

    //set up files to be loaded
    String[] FileNames = new String[files.length];
    ChromNum = new String[files.length];
    ChromNumPos = new Integer[files.length];
    TotSeries = files.length;

    //set up progress bar
    jprogressbar.setVisible(true);
    jprogressbar.setIndeterminate(true);
    jprogressbar.setStringPainted(true);
    f.pack();

    //get data
    for (int i = 0; i < TotSeries; i++)
    {
        FileNames[i] = files[i].getAbsolutePath();
        if (DEBUG) PrintText("Open: " + FileNames[i] + "\n");

        //set up data
        jprogressbar.setString("Loading " + FileNames[i]);
        PrintText("Loading " + FileNames[i]+"\n");

        cgdataset.CGdata = LoadSeq(FileNames[i]);
        jprogressbar.setString("Completed loading " + FileNames[i]);
        PrintText("Completed loading " + FileNames[i]+"\n");

        //set up total data length and number of series for which no lengths will be found
        if (cgdataset.CGdata != null)
        {
            TotDataLength += cgdataset.CGdata.length;
            if ((CurrCG >= cgdataset.CGdata.length) || (cgdataset.CGdata == null))
            {
                NumNull++;
            }
        }
        else NumNull++;

        //get sizes and labels
        key = key + " " + cgdataset.header;
        ChromNum[i] = getChromNumber(cgdataset.header);
        if (DEBUG) PrintText(cgdataset.header + " included\n");

        //boundary cases
        if (i < (files.length - 1))
        {
            cgdataset.Next = new CGdataset();
            cgdataset.Next.Next = firstcgdataset;
            cgdataset = cgdataset.Next;
        }
        if (files.length == 1) cgdataset.Next = cgdataset;
    }

    //get lengths
    jprogressbar.setString("Calculating fragment lengths...");
    PrintText("Calculating fragment lengths...\n");
    for (int j = 0; j < TotSeries; j++)
    {
        cgdataset = cgdataset.Next;
        getLengths();
        ChromNumPos[j] = prevLength;
    }

    //graph
    jprogressbar.setString("Generating graph...");
    PrintText("Generating graph...\n");
    addData(key,Lengths);

    mindomainText.setText(Double.toString(MIN));
    maxdomainText.setText(Double.toString(MAX));

    //get rid of progress bar
    jprogressbar.setVisible(false);
}

//import from saved data
public class ListenMenuOpenDataFile implements ActionListener,Runnable{
	public void actionPerformed(ActionEvent e)
    {
		ListenMenuOpenDataFile t = new ListenMenuOpenDataFile();
		new Thread(t).start();
		javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
		public void run()
        {
			jprogressbar.updateUI();
		}
		});
	}
	public void run()
    {
		String wd = System.getProperty("user.dir");
		JFileChooser fc = new JFileChooser(wd);

		//do not allow for multiple files
		fc.setMultiSelectionEnabled(false);

		int rc = fc.showDialog(null, "Import");
		if (rc == JFileChooser.APPROVE_OPTION)
		{
			//set up progress bar
			jprogressbar.setVisible(true);
            jprogressbar.setIndeterminate(true);
            jprogressbar.setStringPainted(true);
			f.pack();

			//set up files to be loaded
			File file = fc.getSelectedFile();

            jprogressbar.setString("Importing saved data...");
            PrintText("Importing saved data...\n");
			openCGdata(file,"\n");

			//get lengths
            for (int j = 0; j < TotSeries; j++)
            {
				cgdataset = cgdataset.Next;

				//find number of series for which no lengths will be found
				if ((CurrCG >= cgdataset.CGdata.length) || (cgdataset.CGdata == null))
                {
					NumNull++;
				}

				getLengths();
                ChromNumPos[j] = prevLength;
			}

			//graph
            addData(key,Lengths);

		}
		else if(DEBUG) PrintText("Cancel Open\n");

        //get rid of progress bar
		jprogressbar.setVisible(false);

		//garbage collect
		System.gc();
		return;
	}
}

//export data
public class ListenMenuSaveDataFile implements ActionListener,Runnable
{
    public void actionPerformed(ActionEvent e)
    {
        ListenMenuSaveDataFile t = new ListenMenuSaveDataFile();
        new Thread(t).start();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                jprogressbar.updateUI();
            }
        });
    }
    public void run()
    {
        String wd = System.getProperty("user.dir");
        JFileChooser fc;
    	int rc;

        //save CGdata
        fc = new JFileChooser(wd);
        rc = fc.showDialog(null, "Export CG positions");
        if (rc == JFileChooser.APPROVE_OPTION)
        {
            //set up progress bar
            jprogressbar.setVisible(true);
            jprogressbar.setIndeterminate(true);
            jprogressbar.setStringPainted(true);
            f.pack();

            jprogressbar.setString("Exporting CG fragment locations...");
            PrintText("Exporting CG fragment locations...\n");

            File file = fc.getSelectedFile();
            saveCGdata(file,"\n");

            //get rid of progress bar
            jprogressbar.setVisible(false);
        }
        else if(DEBUG) PrintText("Cancel Save\n");

     	//garbage collect
        System.gc();
        return;
    }
}

//This class save the histogram plot in grayscal
public class ListenMenuSaveGrayChrt implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
        SaveGrayChrt();
    }
}

//This class saves the histogram plot
public class ListenMenuSaveChrt implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
        SaveChrt();
	}
}

public class ListenMenuSaveGrayMinChrt implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
        SaveGrayMinChrt();
    }
}

public class ListenMenuSaveMinChrt implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
            SaveMinChrt();
	}
}

public class ListenMenuSaveGrayClusterChrt implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
        //save
        SaveGrayClusterChrt();
    }
}

public class ListenMenuSaveClusterChrt implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
            SaveClusterChrt();
	}
}

public class ListenMenuQuit implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		System.gc();
		System.exit(0);
	}
}

public class ListenClearData implements ActionListener
{
	public void actionPerformed(ActionEvent e)
    {
		clearData();
                if (DEBUG) PrintText("Data Cleared\n");
	}
}

public class ListenCloseWdw extends WindowAdapter
{
	public void windowClosing(WindowEvent e)
    {
		System.gc();
		System.exit(0);
	}
}

public void loadText(String FileName)
{
    InputStream in = getClass().getResourceAsStream(FileName);
    try
    {
        SeqText.read(new InputStreamReader(in), null);
    }
    catch (IOException e)
    {
        e.printStackTrace();
    }
	System.gc();
}

public void PrintText(String text) {
	if (text != null) {
            SeqText.append(text);
            SeqText.setCaretPosition(SeqText.getDocument().getLength());
        }
}

//Displays the main frame of the program
public void show(){
	//show window frame
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.pack(); //Adjusts panel to components for display
	f.setVisible(true);
}

private void SaveChrt() {
        try {
            //remove crosshairs
            XYPlot xyplot = (XYPlot)chart.getPlot();
            xyplot.setDomainCrosshairVisible(false);

            //remove marker
            if (intervalmarker != null) {
                xyplot.removeDomainMarker(intervalmarker, Layer.BACKGROUND);
                intervalmarker = null;
            }

            //save
            chartpanel.doSaveAs();
        }
	catch (IOException err) {
            if(DEBUG) PrintText("Error: " + err);
	}

        //garbage collect
	System.gc();
}

private void SaveClusterChrt() {
    if (mnuItemFindMin.getState() == false) {
        PrintText("Error: No optimization chart present\n");
        return;
    }
    try {
        clusterchartpanel.doSaveAs();
    }
    catch (IOException err) {
        if(DEBUG) PrintText("Error: " + err);
    }
}

private void SaveMinChrt() {
    if (mnuItemFindMin.getState() == false) {
        PrintText("Error: No minima chart present\n");
	return;
    }

    try {
	minchartpanel.doSaveAs();
    }
    catch (IOException err) {
	if(DEBUG) PrintText("Error: " + err);
    }
}

private void SaveGrayChrt() {
    //get chart and renderer
    XYPlot xyplot = (XYPlot)chart.getPlot();
    XYItemRenderer renderer = xyplot.getRenderer();

    //set gray
    Paint defaultPaint = renderer.getSeriesPaint(0);
    renderer.setSeriesPaint(0,Color.GRAY);
    xyplot.setRenderer(0,renderer);

    //save
    SaveChrt();

    //restore color
    renderer.setSeriesPaint(0,defaultPaint);
    xyplot.setRenderer(0,renderer);
}

private void SaveGrayClusterChrt() {
    //get chart and renderer
    XYPlot xyplot = (XYPlot)clusterchart.getPlot();
    XYItemRenderer renderer = xyplot.getRenderer();

    //set gray
    Paint defaultPaint = renderer.getSeriesPaint(0);
    renderer.setSeriesPaint(0,Color.GRAY);
    xyplot.setRenderer(0,renderer);

    //save
    SaveClusterChrt();

    //restore color
    renderer.setSeriesPaint(0,defaultPaint);
    xyplot.setRenderer(0,renderer);
}

private void SaveGrayMinChrt() {
    //get chart and renderer
    XYPlot xyplot = (XYPlot)minchart.getPlot();
    XYItemRenderer renderer0 = xyplot.getRenderer(0);
    XYItemRenderer renderer1 = xyplot.getRenderer(1);

    //set gray
    Paint defaultPaint0 = renderer0.getSeriesPaint(0);
    Paint defaultPaint1 = renderer1.getSeriesPaint(0);
    renderer0.setSeriesPaint(0,Color.LIGHT_GRAY);
    renderer1.setSeriesPaint(0,Color.DARK_GRAY);
    xyplot.setRenderer(0,renderer0);
    xyplot.setRenderer(1,renderer1);

    //save
    SaveMinChrt();

    //restore color
    renderer0.setSeriesPaint(0,defaultPaint0);
    renderer1.setSeriesPaint(0,defaultPaint1);
    xyplot.setRenderer(0,renderer0);
    xyplot.setRenderer(1,renderer1);
}

//clears all the data
public void clearData()
{
	//global variables reset
	MIN = 0;
	MAX = 0;
	BINS = 100000;
	key = "";
	TotSeries = 0;
	TotDataLength = 0;
	prevLength = 0;
	regLine = null;
        Lengths = null;
        StartLen = null;
        ChromNum = null;
        ChromNumPos = null;

	//clear min settings
	minText.setText("");
	maxText.setText("");
	smoothText.setText("");

	//new data sets
	histogramdataset = new HistogramDataset();
	minimadataset = new CategoryTableXYDataset();
	clusterdataset = new CategoryTableXYDataset();

	//chart area reset
	XYPlot xyplot = (XYPlot)chart.getPlot();
	xyplot.setDataset(histogramdataset);
	chartpanel.restoreAutoBounds();

	XYPlot minxyplot = (XYPlot)minchart.getPlot();
	minxyplot.setDataset(0, minimadataset);
	minxyplot.setDataset(1, new DefaultXYDataset());
	minchart.clearSubtitles();
	minchartpanel.restoreAutoBounds();

	XYPlot clusterxyplot = (XYPlot)clusterchart.getPlot();
	clusterxyplot.setDataset(clusterdataset);
	clusterchartpanel.restoreAutoBounds();

	//remove marker
	if (intervalmarker != null) {
		xyplot = (XYPlot)chart.getPlot();
		xyplot.removeDomainMarker(intervalmarker, Layer.BACKGROUND);
		intervalmarker = null;
	}

        //remove crosshair
        xyplot.setDomainCrosshairVisible(false);

	//clear text
	minText.setText(null);
	maxText.setText(null);
	smoothText.setText(null);

	f.pack();
	System.gc();
}

//replaces the data currently in the chart with new data
//Variables in: series, data
//Variables out: histogramdataset
public void replaceData(Comparable series, double [] data) {
	//clear memory first
	histogramdataset = new HistogramDataset();

	//add data
	addData(series, data);
}

public void addData(Comparable series, double [] data)
{
    if (data != null)
	histogramdataset.addSeries(series, data, BINS, MIN, MAX);

    //add data to plot
    XYPlot xyplot = (XYPlot)chart.getPlot();

	//get smoothing parameter, rounding up to nearest int
    if (smoothText.getText().length() == 0)
    {
        int smoothParam = (int)(2*(BINS/(MAX-MIN+1)) + 1);
        smoothText.setText(Integer.toString(smoothParam));
        MovingAverage(smoothParam);
    }
    else if (smoothCheckBox.isSelected())
    {
        //smooth data
        int smoothParam = Integer.parseInt(smoothText.getText());
        if (DEBUG) PrintText("Smoothing parameter:" + smoothParam + "\n");
        MovingAverage(smoothParam);
        return;
    }
    else
    {
        //in the event no smoothing will occur, update data
        xyplot.setDataset(histogramdataset);
    }

    //turn on crosshairs
	xyplot.setDomainCrosshairVisible(true);
	xyplot.setDomainCrosshairLockedOnData(false);

    f.pack();
    System.gc();
}

public void addMinima(double value)
{
	//updata dataset and graph
	if (value != -1)
    {
        //remove previous minima for this number of CGs
        for (int i = 0; i < minimadataset.getItemCount(); i++)
        {
            if (minimadataset.getYValue(0,i) == CurrCG)
            {
                minimadataset.remove(minimadataset.getXValue(0,i), (String) histogramdataset.getSeriesKey(0));
                break;
            }
        }
        //add new minima
		minimadataset.add(value, (double) CurrCG, (String) histogramdataset.getSeriesKey(0));
	}
	else return;

	//get plot
	XYPlot xyplot = minchart.getXYPlot();
	minchart.clearSubtitles();

	//set render options
 	XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
 	XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
    Shape shape = xyplot.getRenderer().getSeriesShape(0);
	renderer1.setSeriesLinesVisible(0, false);
	renderer1.setSeriesPaint(0,Color.RED);
    renderer1.setSeriesShape(0, shape);
	if (minimadataset.getItemCount() > 1)
    {
		renderer2.setLinesVisible(true);
		renderer2.setShapesVisible(false);
		renderer2.setBaseSeriesVisibleInLegend(false);
		renderer2.setSeriesPaint(0,Color.BLUE);
	}

	//get regression
	LineFunction2D localregline = null;
	XYDataset regseries;
	if (minimadataset.getItemCount() > 1)
    {
		//create the regression
		double[] reg = Regression.getOLSRegression(minimadataset,0);
		//looking for x values so put x in terms of y
		//y = a + bx 	 for local
		//x = y/b - a/b  for global
		if (DEBUG) PrintText("Regression: y = " + reg[0] + " + " + reg[1] + "x\n");

		//global (in terms of y)
		regLine = new LineFunction2D(-reg[0]/reg[1], 1/reg[1]);

		//local (in terms of x)
		localregline = new LineFunction2D(reg[0], reg[1]);
		regseries = DatasetUtilities.sampleFunction2D(localregline,  minimadataset.getDomainLowerBound(false),
        minimadataset.getDomainUpperBound(false), 2, "Linear Regression");

		//plot line
		xyplot.setDataset(1,regseries);
		xyplot.setRenderer(1,renderer2);
		minchart.addSubtitle(0, new TextTitle("Regression: y = " + reg[0] + " + " + reg[1] + "x"));
	}

    xyplot.setDataset(0,minimadataset);
	xyplot.setRenderer(0,renderer1);

	f.pack();
	System.gc();
}

public void addClusterSize (double value)
{
	//updata dataset and graph
	if (value != -1)
    {
		clusterdataset.add((double) CurrCG, value, (String) histogramdataset.getSeriesKey(0));
	}
	clusterchartpanel.restoreAutoBounds();
	XYPlot xyplot = clusterchart.getXYPlot();
	xyplot.setDataset(clusterdataset);
}

//as of now seperator must be '\n', so the input seperator is tossed
private void openCGdata (File file, String seperator)
{
	TotDataLength = 0;
	String FileName = file.getAbsolutePath();
	try
    {
		//Create data stream from buffered input
		FileInputStream fis = new FileInputStream(FileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		DataInputStream dis = new DataInputStream(bis);

		//clear previous graph
		clearData();

		//initialize data
		Lengths = null;
		StartLen = null;
		TotDataLength = 0;
		prevLength = 0;
		NumNull = 0;

		cgdataset = new CGdataset();
		CGdataset firstcgdataset = cgdataset;

		TotSeries = Integer.parseInt(dis.readLine());
		key = "";
        ChromNum = new String[TotSeries];
        ChromNumPos = new Integer[TotSeries];
		for (int j = 0; j < TotSeries; j++)
        {
			//get header
			cgdataset.header = dis.readLine();
			key = key + cgdataset.header;
            ChromNum[j] = getChromNumber(cgdataset.header);
//			PrintText(cgdataset.header + "\n");

			//initialize CGdata
			int CGdatalength = Integer.parseInt(dis.readLine());
			TotDataLength += CGdatalength;
			cgdataset.CGdata = new Integer[CGdatalength];
			for (int i = 0; i < cgdataset.CGdata.length; i++)
            {
				cgdataset.CGdata[i] = Integer.parseInt(dis.readLine());
			}

			//boundary cases
			if (j < (TotSeries - 1))
            {
				cgdataset.Next = new CGdataset();
				cgdataset.Next.Next = firstcgdataset;
				cgdataset = cgdataset.Next;
			}
			if (TotSeries == 1) cgdataset.Next = cgdataset;
		}
		if (DEBUG) PrintText("Opened CG locations: " + FileName + "\n");
		dis.close();
		bis.close();
		fis.close();
	}
	catch (IOException err)
    {
		if (DEBUG) PrintText("Error: " + err + "\n");
	}
	//garbage collect
	System.gc();
}

public void saveCGdata (File file, String seperator)
{
	String FileName = file.getAbsolutePath();
	try
    {
		//Start permanent file
		FileOutputStream fos = new FileOutputStream(FileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		PrintWriter pw = new PrintWriter(bos);

		pw.print(Integer.toString(TotSeries) + seperator);
		TotDataLength = 0;
		for (int j = 0; j < TotSeries; j++)
        {
			cgdataset = cgdataset.Next;
			TotDataLength += cgdataset.CGdata.length;
			//series header
			pw.print(cgdataset.header + "\n" + Integer.toString(cgdataset.CGdata.length) + seperator);
			if (cgdataset.CGdata != null) for (int i = 0; i < cgdataset.CGdata.length; i++)
            {
				pw.print(Integer.toString(cgdataset.CGdata[i]) + seperator);
			}
		}
		if (DEBUG) PrintText("Saved CG locations: " + FileName + "\n");
		pw.close();
		bos.close();
		fos.close();
	}
	catch (IOException err)
    {
		if (DEBUG) PrintText("Error: " + err + "\n");
	}
	//garbage collect
	System.gc();
}

public void saveLengths (File file, String seperator)
{
	String FileName = file.getAbsolutePath();
	try
    {
		//Start permanent file
		FileOutputStream fos = new FileOutputStream(FileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		PrintWriter pw = new PrintWriter(bos);

		//file header
		pw.print(Integer.toString(Lengths.length) + seperator);

		if (Lengths != null) for (int i = 0; i < Lengths.length; i++)
        {
				pw.print(Double.toString(Lengths[i]) + seperator);
		}
		if (DEBUG) PrintText("Saved fragment lengths: " + FileName + "\n");
		pw.close();
		bos.close();
		fos.close();
	}
	catch (IOException err)
    {
		if (DEBUG) PrintText("Error: " + err + "\n");
	}
	//garbage collect
	System.gc();
}

public void saveStartLen (File file, String seperator)
{
	String FileName = file.getAbsolutePath();
	try
    {
		//Start permanent file
		FileOutputStream fos = new FileOutputStream(FileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		PrintWriter pw = new PrintWriter(bos);

		//file header
		pw.print(Integer.toString(StartLen.length) + seperator);

		if (Lengths != null) for (int i = 0; i < StartLen.length; i++)
        {
				pw.print(Integer.toString(StartLen[i]) + seperator);
		}
		if (DEBUG) PrintText("Saved fragment start positions: " + FileName + "\n");
		pw.close();
		bos.close();
		fos.close();
	}
	catch (IOException err)
    {
		if (DEBUG) PrintText("Error: " + err + "\n");
	}
	//garbage collect
	System.gc();
}

//returns the average number of CGs in a cluster
//divided by the maximum fragment length
//Variables in: Lengths, StartLen, MinXVal (minimum)
//Variable out: returns double
public double AveCGLength(double minimum)
{
	double NumInClusters = 0;
	int NumClusters = 0;

	//go through CGLength, stopping at those shorter than the current min
	for (int j = 0; j < Lengths.length; j++)
    {
		if (Lengths[j] < minimum)
        {
			NumClusters++;
			//check for overlaps, determine cluster length
			if (j < Lengths.length - 1)
            {
				int i = 0;
				while ((StartLen[j] + Lengths[j] >= StartLen[j+i+1]))
                {
					if (Lengths[j+i+1] < minimum)
                    {
						NumInClusters++;
					}
					i++;
					if (j+i+1 >= Lengths.length - 1) break;
				}
				j += i;
			}
		}
	}

	//find ave
	return (NumInClusters/NumClusters)/minimum;
}

public void SaveCGCluster(double minimum, File file)
{

    String FileName = file.getAbsolutePath();
    String ext = getExtension(file);
    if (ext == null)
    {
        FileName = FileName + ".bed";
    }
    try
    {
        //Start permanent file
        FileOutputStream fos = new FileOutputStream(FileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        PrintWriter pw = new PrintWriter(bos);

    	int NumClusters = 0;

//        pw.print("Start Position \t End Position\n");

        //go through CGLength, stopping at those shorter than the current min
        for (int j = 0; j < Lengths.length; j++)
        {
            if (Lengths[j] < minimum)
            {
                NumClusters++;

                //record chromosome number
                for (int k = 0; k < ChromNum.length; k++)
                {
                    if (ChromNumPos[k] >= j)
                    {
                        pw.print(ChromNum[k] + "\t");
                        break;
                    }
                }

                //record cluster start
                pw.print(StartLen[j] + "\t");

                //check for overlaps, determine cluster length
                if (j < Lengths.length - 1)
                {
                    int i = 0;
                    while ((StartLen[j] + Lengths[j] >= StartLen[j+i+1]))
                    {
                        i++;
                        if (j+i+1 >= Lengths.length - 1) break;
                    }
                    j += i;
                }
                //record cluster end
                pw.print(StartLen[j] + "\n");
            }
        }
        if (DEBUG) PrintText("Total number of clusters found: " + NumClusters + "\n");

        pw.close();
        bos.close();
        fos.close();
    }
    catch (IOException err)
    {
        if (DEBUG) PrintText("Error: " + err + "\n");
    }

    //garbage collect
    System.gc();
}

//produces fragment length data
//Variables in:  cgdataset, CurrCG, MaxCG, MinCG, CGstep
//Variables out: MIN, MAX, Lengths, StartLen
//Global helper variables used: prevLength, NumNull
public void getLengths()
{
	//don't calculate if there is no data
	if (cgdataset.CGdata == null)
    {
		return;
	}

	//don't calculate if there are not enough pts
	if (CurrCG >= cgdataset.CGdata.length)
    {
		return;
	}

	//initialize Lengths data (this has extra spaces for mult files, when one has data points but not enough)
	if (Lengths == null)
    {
		Lengths = new double[TotDataLength - ((TotSeries-NumNull)*(CurrCG))];
		StartLen = new Integer[TotDataLength - ((TotSeries-NumNull)*(CurrCG))];
		prevLength = 0;
	}

	//find distances and MIN,MAX
	for (int i = 0; i < (cgdataset.CGdata.length - CurrCG); i++)
    {
		Lengths[i+prevLength]  = cgdataset.CGdata[i+CurrCG-1]-cgdataset.CGdata[i];
		StartLen[i+prevLength]  = cgdataset.CGdata[i];
		if ((Lengths[i+prevLength] < MIN) || (MIN == 0))
        {
			MIN = Lengths[i+prevLength];
			//manual debug line
			//PrintText("Minimum: " + MIN + "\n" + "at Pass (" + i + "," + (CurrCG) + ")\n");
		}
		if (((Lengths[i+prevLength] > MAX) &&
                     (Lengths[i+prevLength] < 2*MAX)) || (MAX == 0))
        {
			MAX = Lengths[i+prevLength];
		}
	}

	prevLength = prevLength + cgdataset.CGdata.length - CurrCG; // - MinCG%CGstep;
	//Manual Debug lines
	//PrintText("Current Position: " + (CurrCG-MinCG)/CGstep + " of " + (MaxCG-MinCG)/CGstep + "\n");
	//PrintText("Length to be added: " + (cgdataset.CGdata.length - CurrCG) + "\n");
	//PrintText("Total length: " + length.length + "\n");

	if (DEBUG) PrintText("Domain: " + MIN + ":" + MAX + "\n");
	System.gc();
}

//reads in chromosome file and collects CG locations
// Variables in:  FileName
// Variables out: cgdataset
public Integer[] LoadSeq(String FileName)
{
	//Manual Debug Line
	//files too large to display
	//CGinterface.loadText("test.txt");

	//create temporary list
	ArrayList<Integer> CGList = new ArrayList<Integer>();

	try
    {
		//Create data stream from buffered input
		FileInputStream fis = new FileInputStream(FileName);
		BufferedReader d = new BufferedReader(new InputStreamReader(fis));

		// toss header
		cgdataset.header = d.readLine();

		//set up sequence buffer
		File file = new File(FileName);
		long fileLength = file.length();

		if (fileLength > Integer.MAX_VALUE)
		{
			System.out.println("file too large");
			System.exit(-1);
		}

		char[] b = new char[(int)fileLength + 1];
		//int numRead = d.read(b, 0, b.length);

		// Get a Channel for the source file
		FileChannel fc = fis.getChannel();

		// Get a CharBuffer from the source file
		ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
		Charset cs = Charset.forName("8859_1");
		CharsetDecoder cd = cs.newDecoder();
		CharBuffer cb = cd.decode(bb);

		//debug code
		if (DEBUG)
		{
			PrintText(FileName + " found\n");
			//System.out.printf("C:\t%d\nc:\t%d\n", (int) 'C', (int) 'c');
			//System.out.printf("G:\t%d\ng:\t%d\n", (int) 'G', (int) 'g');
			PrintText("Header: " + cgdataset.header + "\n");
		}


		// Remove whitespace
		Pattern pw = Pattern.compile("[\r\n]");
		Matcher mw = pw.matcher(cb);
		String DNA = mw.replaceAll("");

                //PrintText("\n|"+DNA+"|\n");
                
		d.close();

		//search for all CGs
                Pattern p;

                if (caseCheckBox.isSelected() == true)
                {
                    p = Pattern.compile(searchPatternText.getText());
                }
                else
                {
                    p = Pattern.compile(searchPatternText.getText(), Pattern.CASE_INSENSITIVE);
                }

		Matcher m = p.matcher(DNA);

                while (m.find())
		{
                    CGList.add(m.end() - cgdataset.header.length() - 1);
		}

	}
	catch (IOException e)
        {
		/*debug code*/
		if (DEBUG)
        {
			PrintText(FileName + " not found\n");
		}
		return null;
	}

	//turn linked list into standard array for easy use
	Integer[] CGArray = new Integer[CGList.size()];
	CGList.toArray(CGArray);

	/*debug code*/
	PrintText("done scanning\n");
	if (DEBUG)
    {
		PrintText(CGArray.length + " fragments found\n");
		PrintText("First 10 Locations:\n");
		for (int i = 0; ((i < CGArray.length) && (i < 10)); i++)
        {
			PrintText(CGArray[i] + "\n");
		}
	}
	System.gc();
	return CGArray;
}

//calculates a moving average of window size M on the Lengths array
//Variables in: Lengths, chart
//Variables out: N/A it changes the chart display only
public void MovingAverage(int M) {
        //get chart
        XYPlot xyplot = (XYPlot)chart.getPlot();

	int N = histogramdataset.getItemCount(0);
	double[] NewX = new double[N-M];
	double[] NewY = new double[N-M];
	XYSeries xyseries = new XYSeries(histogramdataset.getSeriesKey(0));
	for (int i = 0; i < N - M; i++) {
		NewY[i] = 0;
		NewX[i] = histogramdataset.getXValue(0,i);
		for (int j = 0; j < M; j++) {
			NewY[i] += histogramdataset.getYValue(0,i+j);
		}
		NewY[i] /= M;
		xyseries.add(NewX[i],NewY[i]);
	}

        //update chart
	XYDataset xydataset = new XYSeriesCollection(xyseries);
	xyplot.setDataset(xydataset);
	System.gc();
}

public static void main(String [ ] args)
{
	//initialize gui
	CGgui CGinterface = new CGgui();
	CGinterface.show();

	/*Code for Debugging purposes*/

	//garbage collect
	Runtime r = Runtime.getRuntime();
	r.gc();

	//grab file
//	CGinterface.CGdata = CGinterface.LoadSeq("chr21_random.fa");

	//process data
	//find distances of 30
//	double[] length = CGinterface.getLengths(30);

	//graph data
//	CGinterface.addData(CGinterface.header,length,100000,CGinterface.MIN,CGinterface.MAX);
//	CGinterface.MovingAverage(150);
//	CGinterface.Bezier(.01);
}}
