import java.io.*;
import java.util.*;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


class FaoStatTsElement {

  String areaCode;
  String area;
  int 	 year;
  int 	 value;

  @Override
  public String toString() {
  	return "area = " + area + " areaCode = " + areaCode + " year = " + year + " value = " + value;
  }
}
class SpamGridElement {

  int    year;
  String allocKey;
  String iso3;
  String country;
  String admin1;
  String admin2;
  double value;

  String rowCol;
  int row;
  int col;
  double x;
  double y;
  double percentage;

  public SpamGridElement() {
  }
  public SpamGridElement(int year) {
    this.year = year;
  }

  @Override
  public String toString() {
  	return "country = " + country + " iso3 = " + iso3 + " admin1 = " + admin1 + " admin2 = " + admin2 + " year = " + year + " value = " + value
  		+ " allocKey = " + allocKey + " rowCol = " + rowCol
  		+ " row = " + row + " col= " + col + " x= " + x + " y = " + y + " percentage = " + percentage;
  }
}


public class FileGenerator {

  public static final int    CELL5M_COLROW_COLUMN_ID 	=  0;
  public static final int    CELL5M_X_COLUMN_ID 		=  1;
  public static final int    CELL5M_Y_COLUMN_ID 		=  2;
  public static final int    CELL5M_ALLOC_KEY_COLUMN_ID =  3;


  public static final int    SPAM_ALLOC_KEY_COLUMN_ID =  1;
  public static final int    SPAM_ISO3_COLUMN_ID 	  =  2;
  public static final int    SPAM_VALUE_COLUMN_ID 	  =  4;
  public static final int    SPAM_ADM1_COLUMN_ID 	  =  6;
  public static final int    SPAM_ADM2_COLUMN_ID 	  =  7;
  public static final int    SPAM_COUNTRY_COLUMN_ID   =  9;

  public static final int    FAO_AREA_CODE_COLUMN_ID =  2;
  public static final int    FAO_AREA_COLUMN_ID      =  3;
  public static final int    FAO_YEAR_COLUMN_ID      =  9;
  public static final int 	 FAO_VALUE_COLUMN_ID     = 11;
  public static final String FAO_COLUMN_SEPARATOR    = ",";



  // ------------------------------------------------------------------------------------------------------------
  // store downscaled results to csv file
  // ------------------------------------------------------------------------------------------------------------
  public static void storeDownscaledSgesToFile (ArrayList<SpamGridElement> sges, String outFileID) throws Exception {

     	PrintWriter out = new PrintWriter (new BufferedWriter (new OutputStreamWriter (new FileOutputStream (outFileID))), true);
     	out.println ("country,allocKey,iso3,admin1,admin2,year,value,rowCol,row,col,x,y,percentage");

		for (SpamGridElement sge : sges)
		  	out.println (sge.country
		  	    + "," + sge.allocKey
		  		+ "," + sge.iso3
		  		+ "," + sge.admin1
		  		+ "," + sge.admin2
		  		+ "," + sge.year
		  		+ "," + sge.value
		  		+ "," + sge.rowCol
		  		+ "," + sge.row
		  		+ "," + sge.col
		  		+ "," + sge.x
		  		+ "," + sge.y
		  		+ "," + sge.percentage);

		System.out.println ("downscaled results stored to file " + outFileID);
		out.close();
  }

  // ------------------------------------------------------------------------------------------------------------
  // compute the relative value for each grid cell
  // ------------------------------------------------------------------------------------------------------------
  public static void prepareDownscalingFactors (ArrayList<SpamGridElement> sges)  {

	double totalSum = 0.0;
	for (SpamGridElement sge : sges)
		totalSum += sge.value;

	System.out.println ("totalSum = " + totalSum);

	for (SpamGridElement sge : sges)
	  sge.percentage = sge.value / totalSum;

	double percCheck = 0.0;
	for (SpamGridElement sge : sges)
	  percCheck += sge.percentage;

	System.out.println ("percCheck = " + percCheck);

  }

  // ------------------------------------------------------------------------------------------------------------
  // downscale a single FAO time series step to grid level, check for errors
  // ------------------------------------------------------------------------------------------------------------
  public static ArrayList<SpamGridElement> getDownscaledSges (FaoStatTsElement fste, ArrayList<SpamGridElement> referenceSges)  {

	ArrayList<SpamGridElement> results = new ArrayList<SpamGridElement>();

	for (int i=0; i<referenceSges.size(); i++) {
		SpamGridElement sge = referenceSges.get(i);
		sge.year = fste.year;
		sge.value = (double)fste.value * sge.percentage;
		results.add (sge);
	}

	double totalSum = 0.0;
	for (SpamGridElement sge : results)
		totalSum += sge.value;


    boolean acceptable = Math.abs (fste.value-totalSum) < 0.5;
	if (!acceptable) {
		System.out.println ("totalSum downscaled grids = "   + totalSum + " faostat country value = " + fste.value + " ==> total downscaling error= " + Math.abs (fste.value-totalSum));
		System.out.println ("unexpected downscaling error > 0.5 - stopping here !");
		System.exit(0);
	}

	return results;
  }


  // ------------------------------------------------------------------------------------------------------------
  // provide a simple discrete color scale for the plots
  // ------------------------------------------------------------------------------------------------------------
  public static Color getColor (double value) {

	if (value == 0)   	return new Color(255,255,229);
	if (value <= 50)    return new Color(255,247,188);
	if (value <= 150)   return new Color(254,227,145);
	if (value <= 200)   return new Color(254,196,79);
	if (value <= 250)   return new Color(254,153,41);
	if (value <= 300)   return new Color(236,112,20);
	if (value <= 350)   return new Color(204,76,2);
	if (value <= 400)   return new Color(153,52,4);
	return new Color(102,37,6);

  }

  // ------------------------------------------------------------------------------------------------------------
  // generate and store a simple png map plot (grid based)
  // ------------------------------------------------------------------------------------------------------------
  public static void generatePlot(ArrayList<SpamGridElement> sges, String title, String fileID) throws Exception {

	final int SCALE = 4;

	int width = 200, height = 180;

	BufferedImage bi = new BufferedImage(SCALE * width, SCALE * height, BufferedImage.TYPE_INT_ARGB);

	Graphics2D grph = (Graphics2D) bi.getGraphics();
	grph.scale(SCALE, SCALE);

	int offsetX = 2520;
	int offsetY = 880;
	grph.setPaint(Color.white);
	grph.fillRect (0, 0, width,height);


	Font font = new Font("Arial", Font.PLAIN, 8);
	grph.setFont(font);
	FontMetrics fontMetrics = grph.getFontMetrics();
	int stringWidth = fontMetrics.stringWidth(title);
	int stringHeight = fontMetrics.getAscent();
	grph.setPaint(Color.black);
	grph.drawString(title, (width - stringWidth) / 2, 15);

	for (SpamGridElement sge : sges) {
		grph.setPaint(getColor(sge.value));
		grph.fillRect (sge.row-offsetX, sge.col-offsetY, 1,1);
	}
	font = new Font("Arial", Font.PLAIN, 4);
	grph.setFont(font);
	for (int i=0; i<=9; i++) {
		grph.setPaint(getColor(i*50));
		grph.fillRect (20, 20+5+8*i, 5, 5);
		grph.setPaint(Color.black);
		if (i==0) grph.drawString("0 ha", 27, 20+4+5+8*i);
		else if (i==9) grph.drawString("> 400 ha", 27, 20+4+5+8*i);
		else grph.drawString("<=" + i*50 + " ha", 27, 20+4+5+8*i);
	}
	grph.dispose();
	ImageIO.write(bi, "png", new File(fileID));
	System.out.println (fileID  + " generated.");
  }



  // ------------------------------------------------------------------------------------------------------------
  // extract and store the required subset of cell data to allow subsequent performance gains
  // ------------------------------------------------------------------------------------------------------------
  public static void extractCellData(String inFileID, String outFileID, int minID, int maxID) throws Exception {

    	int rowCounter = 0;
    	BufferedReader in  = new BufferedReader (new InputStreamReader (new FileInputStream (inFileID)));
     	PrintWriter out = new PrintWriter (new BufferedWriter (new OutputStreamWriter (new FileOutputStream (outFileID))), true);

  		// -----------------------------------------------------------------------------------
    	// process line by line
    	// -----------------------------------------------------------------------------------
    	String str = in.readLine();
    	out.println (str);

        str = in.readLine();
    	while (str != null) {
            rowCounter++;
            if (rowCounter%5000==0)
				System.out.println ("processing row : " + rowCounter);

		  	String[] array = str.split("\\,", -1);
		  	String allocKey = array[CELL5M_ALLOC_KEY_COLUMN_ID].trim();
		  	if (allocKey.startsWith("\"")) allocKey = allocKey.substring (1);
		  	if (allocKey.endsWith("\"")) allocKey = allocKey.substring (0, allocKey.length()-1);

		  	int idAsInt = Integer.parseInt(allocKey);
		  	if (idAsInt >= minID && idAsInt <= maxID) {
    			out.println (str);
			}
    	  	str = in.readLine();
		}
		in.close();
		System.out.println ("results stored to file " + outFileID);
		out.close();
  }


  public static ArrayList<SpamGridElement> addGridInformationToSpamGridCells(String inFileID, String outFileID, ArrayList<SpamGridElement> sges) throws Exception {

    	int hitCounter = 0;
    	int rowCounter = 0;

    	ArrayList<SpamGridElement> results = new ArrayList<SpamGridElement>();

    	BufferedReader in  = new BufferedReader (new InputStreamReader (new FileInputStream (inFileID)));
  		// -----------------------------------------------------------------------------------
    	// process line by line
    	// -----------------------------------------------------------------------------------
    	in.readLine();
    	String str = in.readLine();

    	while (str != null && hitCounter < sges.size()) {
            rowCounter++;
            if (rowCounter%5000==0)
				System.out.println ("processing row : " + rowCounter);

		  	String[] array = str.split("\\,", -1);

		  	int hitIndex = getSpamGridElementIndexForAllocKey(array[CELL5M_ALLOC_KEY_COLUMN_ID], sges);
		  	if (hitIndex != -1) {
				SpamGridElement sge = sges.get(hitIndex);
				sge.rowCol 	= array[CELL5M_COLROW_COLUMN_ID];
				sge.x 		= Double.parseDouble(array[CELL5M_X_COLUMN_ID]);
				sge.y 		= Double.parseDouble(array[CELL5M_Y_COLUMN_ID]);
				results.add(sge);
				System.out.println ("added info : " + sge);
				hitCounter++;
			}
    	  	str = in.readLine();
		}

	    // -----------------------------------------------------------------------------------
	    // sort result
	    // -----------------------------------------------------------------------------------
		Collections.sort(results, new Comparator<SpamGridElement>() {
			@Override
			public int compare(SpamGridElement s1, SpamGridElement s2) {
			  if (s1.admin1.compareTo(s2.admin1) == 0) return s1.admin2.compareTo(s2.admin2);
		      return s1.admin1.compareTo(s2.admin1);
		    }
	    });

     	PrintWriter out = new PrintWriter (new BufferedWriter (new OutputStreamWriter (new FileOutputStream (outFileID))), true);
     	out.println ("country,allocKey,iso3,admin1,admin2,year,value,rowCol,x,y");

		for (SpamGridElement sge : results)
		  	out.println (sge.country
		  	    + "," + sge.allocKey
		  		+ "," + sge.iso3
		  		+ "," + sge.admin1
		  		+ "," + sge.admin2
		  		+ "," + sge.year
		  		+ "," + sge.value
		  		+ "," + sge.rowCol
		  		+ "," + sge.x
		  		+ "," + sge.y);

		System.out.println ("results stored to file " + outFileID);
		out.close();

		return results;
  }


 // ------------------------------------------------------------------------------------------------------------
 // helper - check if a given allocKey is used in a grid cell set
 // ------------------------------------------------------------------------------------------------------------
 public static int getSpamGridElementIndexForAllocKey(String allocKey, ArrayList<SpamGridElement> sges) {
	//System.err.println (allocKey);
	allocKey = allocKey.substring (1, allocKey.length()-1);
	for (int i=0; i<sges.size(); i++) {
	  //System.err.println (i + " : "  + allocKey + " - " + sges.get(i).allocKey);
	  //System.exit (0);

	  String keyToCheck = sges.get(i).allocKey;
	  if (keyToCheck.length()==7) keyToCheck = "0" + keyToCheck;
	  if (allocKey.equals(keyToCheck)) return i;
    }
	return -1;
  }

  // ------------------------------------------------------------------------------------------------------------
  // generate the final prepared grid input dataset by adding geo references from
  // ------------------------------------------------------------------------------------------------------------
  public static ArrayList<SpamGridElement> extractSpamGridCellsFromAmendedFile(String inFileID, String outFileID) throws Exception {

    	BufferedReader in  = new BufferedReader (new InputStreamReader (new FileInputStream (inFileID)));

    	ArrayList<SpamGridElement> results = new ArrayList<SpamGridElement>();

    	// -----------------------------------------------------------------------------------
    	// process line by line
    	// -----------------------------------------------------------------------------------
    	in.readLine();
    	String str = in.readLine();

    	while (str != null) {
		  	String[] array = str.split("\\,", -1);

    	  	SpamGridElement sge = new SpamGridElement();
    	  	sge.country		= array[0];
  			sge.allocKey	= array[1];
  			sge.iso3		= array[2];
  			sge.admin1	= array[3];
  			sge.admin2	= array[4];
  			sge.year		= Integer.parseInt(array[5]);
  			sge.value		= Double.parseDouble(array[6]);
		    sge.rowCol = array[7];
		    sge.x	= Double.parseDouble(array[8]);
			sge.y 	= Double.parseDouble(array[9]);

		  	if (sge.rowCol.startsWith("\"")) 	sge.rowCol= sge.rowCol.substring (1);
		  	if (sge.rowCol.endsWith("\"")) 		sge.rowCol= sge.rowCol.substring (0, sge.rowCol.length()-1);

			StringTokenizer st = new StringTokenizer (sge.rowCol);
			sge.row = Integer.parseInt(st.nextToken());
			st.nextToken();
  			sge.col = Integer.parseInt(st.nextToken());

			results.add(sge);
    	  	str = in.readLine();
		}

		in.close();

	    // -----------------------------------------------------------------------------------
	    // sort result
	    // -----------------------------------------------------------------------------------
		Collections.sort(results, new Comparator<SpamGridElement>() {
			@Override
			public int compare(SpamGridElement s1, SpamGridElement s2) {
			  if (s1.admin1.compareTo(s2.admin1) == 0) return s1.admin2.compareTo(s2.admin2);
		      return s1.admin1.compareTo(s2.admin1);
		    }
	    });

	    prepareDownscalingFactors (results);

     	PrintWriter out = new PrintWriter (new BufferedWriter (new OutputStreamWriter (new FileOutputStream (outFileID))), true);
     	out.println ("country,allocKey,iso3,admin1,admin2,year,value,rowCol,row,col,x,y,percentage");

		for (SpamGridElement sge : results)
		  	out.println (sge.country
		  	    + "," + sge.allocKey
		  		+ "," + sge.iso3
		  		+ "," + sge.admin1
		  		+ "," + sge.admin2
		  		+ "," + sge.year
		  		+ "," + sge.value
		  		+ "," + sge.rowCol
		  		+ "," + sge.row
		  		+ "," + sge.col
		  		+ "," + sge.x
		  		+ "," + sge.y
		  		+ "," + sge.percentage);

		System.out.println ("results stored to file " + outFileID);
		out.close();

		return results;
  }




  // ------------------------------------------------------------------------------------------------------------
  // extract the required SPAM grid data
  // ------------------------------------------------------------------------------------------------------------
  public static ArrayList<SpamGridElement> extractSpamGridCells(String inFileID, String outFileID, String iso3Code, int year) throws Exception {

    	BufferedReader in  = new BufferedReader (new InputStreamReader (new FileInputStream (inFileID)));

    	ArrayList<SpamGridElement> results = new ArrayList<SpamGridElement>();

    	// -----------------------------------------------------------------------------------
    	// process line by line
    	// -----------------------------------------------------------------------------------
    	String str = in.readLine();

    	int counter = 0;
    	while (str != null) {
		  counter++;
		  String[] array = str.split("\\,", -1);
		  if (array[SPAM_ISO3_COLUMN_ID].equals(iso3Code)) {
			// System.out.println ("HIT AT row " + counter + ": " + str);
    	    SpamGridElement sge = new SpamGridElement(year);
  			sge.allocKey	= array[SPAM_ALLOC_KEY_COLUMN_ID];
  			sge.iso3		= array[SPAM_ISO3_COLUMN_ID];
  			sge.country		= array[SPAM_COUNTRY_COLUMN_ID];
  			sge.admin1		= array[SPAM_ADM1_COLUMN_ID];
  			sge.admin2		= array[SPAM_ADM2_COLUMN_ID];
  			sge.value		= Double.parseDouble(array[SPAM_VALUE_COLUMN_ID]);
			results.add(sge);
		  	}
    	  	str = in.readLine();
		}

	    // -----------------------------------------------------------------------------------
	    // sort result
	    // -----------------------------------------------------------------------------------
		Collections.sort(results, new Comparator<SpamGridElement>() {
			@Override
			public int compare(SpamGridElement s1, SpamGridElement s2) {
			  if (s1.admin1.compareTo(s2.admin1) == 0) return s1.admin2.compareTo(s2.admin2);
		      return s1.admin1.compareTo(s2.admin1);
		    }
	    });

	    // -----------------------------------------------------------------------------------
	    // print to console
	    // -----------------------------------------------------------------------------------
		for (SpamGridElement sge : results)
			System.out.println (sge);

		in.close();

     	PrintWriter out = new PrintWriter (new BufferedWriter (new OutputStreamWriter (new FileOutputStream (outFileID))), true);
     	out.println ("country,allocKey,iso3,admin1,admin2,year,value");

		for (SpamGridElement sge : results)
		  	out.println (sge.country
		  	    + "," + sge.allocKey
		  		+ "," + sge.iso3
		  		+ "," + sge.admin1
		  		+ "," + sge.admin2
		  		+ "," + sge.year
		  		+ "," + sge.value);

		System.out.println ("results stored to file " + outFileID);
		out.close();

		return results;
  }


  // ------------------------------------------------------------------------------------------------------------
  // extract the required FAO time series data
  // ------------------------------------------------------------------------------------------------------------
  public static ArrayList<FaoStatTsElement> extractFaoTs(String inFileID, String outFileID, String[] areaCodes) throws Exception {

    BufferedReader in  = new BufferedReader (new InputStreamReader (new FileInputStream (inFileID)));

    ArrayList<FaoStatTsElement> results = new ArrayList<FaoStatTsElement>();

    // -----------------------------------------------------------------------------------
    // process line by line
    // -----------------------------------------------------------------------------------
    String str = in.readLine();
    while (str != null) {
	  String[] array = str.split("\\,", -1);
	  if (isDesiredAreaCode (array[FAO_AREA_CODE_COLUMN_ID], areaCodes)) {
        FaoStatTsElement ftse = new FaoStatTsElement();
        ftse.areaCode = array[FAO_AREA_CODE_COLUMN_ID];
		ftse.area     = array[FAO_AREA_COLUMN_ID];
		ftse.year	  = Integer.parseInt(array[FAO_YEAR_COLUMN_ID]);
		ftse.value    = Integer.parseInt(array[FAO_VALUE_COLUMN_ID]);
		results.add(ftse);
	  }
      str = in.readLine();
	}
    // -----------------------------------------------------------------------------------
    // sort result by year
    // -----------------------------------------------------------------------------------
	Collections.sort(results, new Comparator<FaoStatTsElement>() {
		@Override
		public int compare(FaoStatTsElement f1, FaoStatTsElement f2) {
	      if (f1.year < f2.year) return -1;
	      if (f1.year > f2.year) return 1;
	      return 0;
	    }
    });
    // -----------------------------------------------------------------------------------
    // print to console
    // -----------------------------------------------------------------------------------
	// for (FaoStatTsElement ftse : results)
	//	System.out.println (ftse);

	in.close();
	return results;
  }

  // ------------------------------------------------------------------------------------------------------------
  // helper to handle more than one area code in extraction
  // ------------------------------------------------------------------------------------------------------------
  public static boolean isDesiredAreaCode (String areaCode, String[] areaCodes) {
	for (String str : areaCodes)
	  if (areaCode.equals(str)) return true;
	return false;
  }







  // ------------------------------------------------------------------------------------------------------------
  // entry point
  // ------------------------------------------------------------------------------------------------------------
  public static void main (String[] args) throws Exception  {

    // ------------------------------------------------------------------------------------------------------------
    // invalid command line input
    // ------------------------------------------------------------------------------------------------------------
	if (args.length != 1) {
		System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println ("usage: java FileGenerator [prepare1 | prepare2 | produce]");
		System.out.println ("java FileGenerator prepare1 --> extract cell data to speed up subsequent processing (will take some minutes; only required once)");
		System.out.println ("java FileGenerator prepare2 --> prepare further files to speed up subsequent processing (will take some minutes; only required once)");
		System.out.println ("java FileGenerator produce  --> will produce downscaled data for 2014 plus image files for 1961 to 2014 for result visualization");
		System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
	}

    // ------------------------------------------------------------------------------------------------------------
    // data preparation step 1
    // ------------------------------------------------------------------------------------------------------------
	else if (args[0].equals ("prepare1")) {
		if (! new File("../data/cell5m_allockey_xy.csv").exists()) {
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println ("The required input file (../data/cell5m_allockey_xy.csv) is not available");
			System.out.println ("please check the ../data directory !");
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.exit(0);
		}

		extractCellData("../data/cell5m_allockey_xy.csv", "cache/out_cell5_reduced.csv", 9000000, 12000000);
	}
    // ------------------------------------------------------------------------------------------------------------
    // data preparation step 2
    // ------------------------------------------------------------------------------------------------------------
	else if (args[0].equals ("prepare2")) {

		if (! new File("../data/spam2005v2r0_harvested-area_wheat_total.csv").exists()) {
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println ("The required input file (../data/spam2005v2r0_harvested-area_wheat_total.csv) is not available");
			System.out.println ("please check the ../data directory !");
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.exit(0);
		}
		if (! new File("cache/out_cell5_reduced.csv").exists()) {
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println ("The required output file from step prepare1 (cache/out_cell5_reduced.csv) is not available");
			System.out.println ("run java FileGenerator prepare1 first to generate this file !");
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.exit(0);
		}

		ArrayList<SpamGridElement> sges = extractSpamGridCells("../data/spam2005v2r0_harvested-area_wheat_total.csv", "cache/out_spam_grid_eth_basic.csv", "ETH", 2005);
		addGridInformationToSpamGridCells("cache/out_cell5_reduced.csv", "cache/out_spam_grid_eth_amended.csv", sges);
		extractSpamGridCellsFromAmendedFile("cache/out_spam_grid_eth_amended.csv", "cache/out_spam_grid_eth_amended_final.csv");
	}
    // ------------------------------------------------------------------------------------------------------------
    // produce downscaled data and image files
    // ------------------------------------------------------------------------------------------------------------
	else if (args[0].equals ("produce")) {
		if (! new File("../data/FAOSTAT_data_8-1-2017.csv").exists()) {
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println ("The required input file (../data/FAOSTAT_data_8-1-2017.csv) is not available");
			System.out.println ("please check the ../data directory !");
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.exit(0);
		}
		if (! new File("cache/out_spam_grid_eth_amended_final.csv").exists()) {
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println ("The output file from step prepare2 (cache/out_spam_grid_eth_amended_final.csv) is not available");
			System.out.println ("run java FileGenerator prepare2 first to generate this file !");
			System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
			System.exit(0);
		}

		ArrayList<SpamGridElement> referenceSges = extractSpamGridCellsFromAmendedFile("cache/out_spam_grid_eth_amended.csv", "cache/out_spam_grid_eth_amended_final.csv");
		ArrayList<FaoStatTsElement> fstes =  extractFaoTs("../data/FAOSTAT_data_8-1-2017.csv", "outFileID", new String[]{"62", "238"}); // Ethiopia

		for (int i=0; i<fstes.size(); i++) {
			ArrayList<SpamGridElement> downscaledSges = getDownscaledSges (fstes.get(i), referenceSges);
			generatePlot(referenceSges, "Ethiopia - wheat harvest area " + fstes.get(i).year, "../results/html/maps/ethiopia_wheat_harvest_area_" + fstes.get(i).year + ".png");
			if (fstes.get(i).year == 2014)
				storeDownscaledSgesToFile (downscaledSges, "../results/ethiopia_wheat_harvest_area_2014_downscaled.csv");
		}
	}
    // ------------------------------------------------------------------------------------------------------------
    // invalid command line input
    // ------------------------------------------------------------------------------------------------------------
	else {
		System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println ("usage: java FileGenerator [prepare1 | prepare2 | produce]");
		System.out.println ("java FileGenerator prepare1 --> extract cell data to speed up subsequent processing (will take some minutes; only required once)");
		System.out.println ("java FileGenerator prepare2 --> prepare further files to speed up subsequent processing (will take some minutes; only required once)");
		System.out.println ("java FileGenerator produce  --> will produce downscaled data for 2014 plus image files for 1961 to 2014 for result visualization");
		System.out.println ("------------------------------------------------------------------------------------------------------------------------------------------");
	}
  }

}
