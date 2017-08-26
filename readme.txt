**************************************************
* Introduction
**************************************************

The provided software has been developed by Dr. Markus Wrobel as an exercise for the IIASA Developer position 2017/09.

The resulting Java application can be run on a variety of platforms and combines data from the follwing input files:

- National harvest statistics over time (FAOSTAT): complete time series (1961-2014) from FAOSTAT's Harvested Area of Wheat at the countries level (FAOSTAT_data_8-1-2017.csv);
- IFPRI SPAM spatially explicit database of Harvested Areas of Wheat for 2005 (SPAM) (spam2005v2r0_harvested-area_wheat_total.csv)
- geoferences for the used gridded data from Global Spatially-Disaggregated Crop Production Statistics Data for 2005 Version 3.1 (cell5m_allockey_xy.csv)

The application allows to

(a) generate and store an extract from cell5m_allockey_xy.csv to speed up subsequent performance
(b) generate a georeferenced gridded dataset by combining the SPAM data with the extracted georeferences from  step (a)
(c) downscaling country based time series data from FAOSTAT's Harvested Area of Wheat to the SPAM grid
(d) producing a csv result file with the downscaled output for Ethiopia for 2014 (see [apphome]/results/ethiopia_wheat_harvest_area_2014_downscaled.csv)
(e) producing a set of simple map plots for the downscaled FAO data for Ethiopia covering each year from 1961 to 2014 (see [apphome]/results/html/maps/)

Downscaling will also perform a simple plausibility check by summing up the downscaled data for each year and comparing it to the original country value provided in the FAO dataset.

The results can then be viewed using a simple interactive web-enabled visualization (open [apphome]/results/html/demo-vis.html in your browser and drag the slider).

**************************************************
* --- Important information ----
**************************************************

Please note that the downscaling is being produced under the follwing assumption:
- It assumes that the distribution of the total amount of harvested area for wheat for 2005 from the SPAM dataset over the respective grid cells remains constant over time, ie the same distribution is assumed 
to be true for the years 1961 to 2014.
- Thus, due to the limitations of the data available, the resulting downscaling *** CAN AND DOES NOT *** take into account any location specific changes in local productivity that might have occured over time.

**************************************************
* Requirements 
**************************************************

To run the Java application in order to reproduce the results, a Java Runtime Environment (version 1.8) is required. If not already available, it can be downloaded here: https://java.com/de/download/

All other required resources (except the original input data files) are included.


**************************************************
* How to use 
**************************************************

View the generated and included results:
downscaled data file:      [apphome]/results/ethiopia_wheat_harvest_area_2014_downscaled.csv
interactive visualization: [apphome]/results/html/demo-vis.html

** Note: ** It is recommended to copy the provided results into a different directory as re-running the application will overwrite the resultsc in [apphome]/results !


**************************************************
* Application structure 
**************************************************
[apphome]/data/	            --> input data files (not included)
[apphome]/src/  	    --> The application (see below how to use)
[apphome]/src/cache/        --> intermediate file results produced from the input data (included). See below how to reproduce
[apphome]/results/	    --> resulting output data
[apphome]/results/html/     --> data viewer 
[apphome]/results/html/maps --> resulting map plots


**************************************************
* Running the application
**************************************************
Open the console and change to [apphome]/src/
Make sure that Java is installed correctly - run java -version to see if this is actually the case.

To enhance flexibility the application can be run in 3 different steps

------------------------------------------------
java FileGenerator prepare1 
------------------------------------------------

This command will perform task (a), ie extract a subset from cell5m_allockey_xy.csv to speed up subsequent processing. This step will take some minutes and is only required once.

-------------
Requirements: 
-------------
[apphome]/data/cell5m_allockey_xy.csv (not included)

-------------
Results:
-------------
[apphome]/src/cache/out_cell5_reduced.csv (included)

------------------------------------------------
java FileGenerator prepare2
------------------------------------------------
This command will perform task (b), ie generate a georeferenced gridded dataset by combining the SPAM data with the extracted georeferences produced in step (a)

-------------
Requirements: 
-------------
[apphome]/data/spam2005v2r0_harvested-area_wheat_total.csv (not included)
[apphome]/src/cache/out_cell5_reduced.csv (included)

-------------
Results:
-------------
[apphome]/src/cache/out_cell5_reduced.csv (included)
[apphome]/src/cache/out_spam_grid_eth_amended.csv (included)
[apphome]/src/cache/out_spam_grid_eth_amended_final.csv (included)
[apphome]/src/cache/out_spam_grid_eth_basic.csv (included)

------------------------------------------------
java FileGenerator produce
------------------------------------------------
This command will perform tasks (c) to (e) by downscaling country based time series data from FAOSTAT's Harvested Area of Wheat to the SPAM grid and producing a data file for 2014 as well as set of map plots 
covering the years from 1961 to 2014. Downscaling will also perform a simple plausibility check (see introduction)


-------------
Requirements: 
-------------
[apphome]/data//data/FAOSTAT_data_8-1-2017.csv  (not included)
[apphome]/src/cache/out_spam_grid_eth_amended_final.csv  (included)

-------------
Results:
-------------
downscaled data file:  [apphome]/results/ethiopia_wheat_harvest_area_2014_downscaled.csv
map plots:             [apphome]/results/html/maps


**************************************************
* Additional information
**************************************************

Although designed with flexibility in mind, due to the given time limit minor modifications in the source code would be required to adapt it eg to produce output for a different country. 
However, it can be easily extended to a version allowing to process the given datasets for any country included without further reprogramming.

In case of any questions please contact:

Markus Wrobel
dr.m.h.wrobel@gmail.com


------------------------------------------------

