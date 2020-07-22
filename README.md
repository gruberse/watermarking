# Digital Watermarking of Medical Sensor Data for Data Leakage Detection - a Proof of Concept Prototype
## Installation Guide
The prerequisite to execute the prototype is a set up database. Either the following database setup is used or the connections in the DatabaseService class must be adapted. The PostgreSQL database needs to be installed locally and be using the port 5432. Also, there must be a database called “watermarking”. The login data must be the user “postgres” with the password “admin”. In addition, the tables must be created according to the SQL script below. For the sake of simplicity, we do not use foreign keys, because we assume that the consistency of the data is checked beforehand. It should be noted that we do not store the sequence of (time, value) points as measurements but we store the sequence of measurements including their metadata in the measurements field of the fragment table.
### SQL Script
CREATE TABLE fragment (
<br>device_id text,
<br>measurements jsonb,
<br>type text,
<br>date date,
<br>unit text,
<br>secret_key bigint,
<br>CONSTRAINT fragment_pkey PRIMARY KEY (device_id, type, date, unit)
<br>)
<br>CREATE TABLE request (
<br>device_id text,
<br>type text,
<br>unit text,
<br>date date,
<br>timestamps timestamp without time zone[],
<br>number_of_watermark integer,
<br>data_user integer,
<br>CONSTRAINT request_pkey PRIMARY KEY (data_user, date, unit, type, device_id)
<br>)
<br>CREATE TABLE usability_constraint (
<br>type text,
<br>unit text,
<br>maximum_error numeric,
<br>minimum_value numeric,
<br>maximum_value numeric,
<br>number_of_ranges integer,
<br>CONSTRAINT usability_constraint_pkey PRIMARY KEY (type, unit)
<br>)
## User Guide
After the database is set up, the prototype can be executed as java program or as jar file. As java program, the ProgramUI class is used to execute simulators and the “files” folder in the project is used for the data. The prototype can be packaged into a jar file by running the maven install command. The resulting jar file is put into the “C:/temp” directory. In this case, the folder used for the data is the “C:/temp/files” folder. Also make sure that the “C:/temp/files” folder contains the test data file. The jar file can be executed in the command line or in batch files by java -jar C:/temp/prototype-1.jar. This basic prototype execution shows the possible commands which are also provided together with the configurable parameters below.
### Command Line Commands
-reset -table -fragment
<br>-reset -table -request
<br>-reset -table -usability_constraint
<br>-reset -files
<br>-reset -log
<br>
<br>-set -usability_constraint [maximumError] [numberOfRanges]
<br>
<br>-generate [deviceId] [from] [to] [seed]
<br>
<br>-store -random [datasetName]
<br>-store -one [datasetName]
<br>
<br>-request -patient [dataUserId] [deviceId] [from] [to]
<br>-request -patients [dataUserId] [numberOfDevices] [from] [to]
<br>
<br>-attack -deletion [datasetName] [frequency]
<br>-attack -random [datasetName] [maximumError] [seed]
<br>-attack -rounding [datasetName] [decimalDigit]
<br>-attack -subset [datasetName] [startIndex] [endIndex]
<br>-attack -collusion [datasetName1] ... [datasetNameN]
<br>
<br>-detect [datasetName] [fragmentSimilarityThreshold] [watermarkSimilarityThreshold] [numberOfColluders]
### Configurable Parameters
[dataUserId]: data user identifier as integer
<br>[datasetName]: dataset name as string
<br>[decimalDigit]: decimal digit as integer
<br>[deviceId]: device identifier as string
<br>[endIndex]: end index as integer
<br>[fragmentSimilarityThreshold]: fragment similarity threshold as double
<br>[frequency]: frequency as integer
<br>[from]: start date as string with the format [yyyy-MM-dd]
<br>[maximumError]:	maximum error as double
<br>[numberOfColluders]: number of colluders as integer
<br>[numberOfDevices]: number of devices as integer
<br>[numberOfRanges]:	number of ranges as integer
<br>[seed]: seed as integer
<br>[startIndex]:	start index as integer
<br>[to]:	end date as string with the format [yyyy-MM-dd]
<br>[watermarkSimilarityThreshold]: watermark similarity threshold as double
