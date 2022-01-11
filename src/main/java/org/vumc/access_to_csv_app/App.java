/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vumc.access_to_csv_app;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;

/**
 *
 * @author suttonc
 */
public class App {
    static Connection connection;
    public static void main(String[] args) throws ParseException, IOException, SQLException {
        // Processing args
        Options options = new Options();
        
        options.addOption("f",true,"The Access Database path to be opened.");
        options.addOption("t",true,"Name of a single table.");
        options.addOption("p",true,"Password for database, if applicable");
        options.addOption("s",true,"Save directory for csv files.");
        options.addOption("q",true,"SQL query file path to use on Access database. Query must be in a file.");
        
        
        String dbName = null;
        String tblName = null;
        String pwd = null;
        String sql = null;
        String saveDir = null;
        String SQLquery = null;
        ResultSet result = null;
        CSVWriter writer = null;
// Trying out one big try catch statement instead of a dozen individuals.
// First we parse the command line arguments.
// Then we prepare hte database connection string depending on if -p argument is given.
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            dbName = cmd.getOptionValue("f");
            tblName = cmd.getOptionValue("t");
            pwd = cmd.getOptionValue("p");
            saveDir = cmd.getOptionValue("s");
            SQLquery = cmd.getOptionValue("q");
            sql = "";
            Statement statement = null;
            String databaseURL = "jdbc:ucanaccess://" + dbName;
            
            if (pwd != null) {
                databaseURL += ";jackcessOpener=org.vumc.access_to_csv_app.CryptCodecOpener";
            }
                if (pwd == null) {
                    connection = DriverManager.getConnection(databaseURL);

                } else {
                    connection = DriverManager.getConnection(databaseURL,"java",pwd);
                }
                System.out.println("Connected to " + dbName);
                
// If -t <TABLE_NAME> option not specified, assumes the user wants to extract all TABLE tables from database.
                if (SQLquery == null) {
// If -q is not specified, run the normal program and export all or table name provided.
                
                    if (tblName == null) {
                        ResultSet rsMD = connection.getMetaData().getTables(null,null,"%",new String [] {"TABLE"});

                        while(rsMD.next()) {
                            statement = connection.createStatement();
                            tblName = "";
                            sql = "";
                            tblName = rsMD.getString(3);
                            sql = String.format("SELECT * FROM [%s]",tblName);
                             System.out.println(sql);
                            result = statement.executeQuery(sql);
                            if (saveDir == null) {
                                saveDir = "./";
                            }
                            writer = new CSVWriter(new FileWriter(saveDir + tblName + ".csv"));
                            ResultSetHelperService resultSetHelperService = new ResultSetHelperService();
                            resultSetHelperService.setDateFormat("yyyy-MM-dd");
                            resultSetHelperService.setDateTimeFormat("yyyy-MM-dd");
                            writer.setResultService(resultSetHelperService);
                            writer.writeAll(result,true);
                            writer.close();
                            System.out.println(tblName + " table saved to file.");
                        } 
                    } else {
    // If -t <TABLE_NAME> was provided, only pull that one table. Works for some View/queries but has trouble with some.
                        statement = connection.createStatement();
                        sql = String.format("SELECT * FROM %s",tblName);
                        result = statement.executeQuery(sql);
                        if (saveDir == null) {
                                saveDir = "./";
                            }
                        writer = new CSVWriter(new FileWriter(saveDir + tblName + ".csv"));
                        ResultSetHelperService resultSetHelperService = new ResultSetHelperService();
                        resultSetHelperService.setDateFormat("yyyy-MM-dd");
                        resultSetHelperService.setDateTimeFormat("yyyy-MM-dd");
                        writer.setResultService(resultSetHelperService);
                        writer.writeAll(result,true);
                        writer.close();
                        System.out.println(tblName + " table saved to file.");
                    }
                } else {
// If -q is not null, an argument containing either an SQL query as string, or file containing an SQL query string was provided.
                // Checking if the argument is a file. Otherwise, 
                File sqlFile = new File(SQLquery);
                boolean exists = sqlFile.exists();
                if (exists) {
                    sql = new String(Files.readAllBytes(Paths.get(SQLquery)));
                } else {
                    // Do nothing and let program finish.
                }
                System.out.println("This is the query:");
                System.out.println(sql);
//                Run the query.
                    statement = connection.createStatement();
                    result = statement.executeQuery(sql);

                    if (saveDir == null) {
                                saveDir = "./";
                            }
                    writer = new CSVWriter(new FileWriter(saveDir + tblName + ".csv"));
                    ResultSetHelperService resultSetHelperService = new ResultSetHelperService();
                    resultSetHelperService.setDateFormat("yyyy-MM-dd");
                    resultSetHelperService.setDateTimeFormat("yyyy-MM-dd");
                    writer.setResultService(resultSetHelperService);
                    writer.writeAll(result,true);
                    writer.close();
                    System.out.println(tblName + " table saved to file.");
                }
// My feeble attempt at error handling, still very new to Java and will revisit this if needed.
        } catch (IOException eio) {
            System.err.println("IOException: " + eio.getMessage());
        } catch (SQLException ex) {
            System.err.println("SQLException occurred with db: " + dbName + " table: " + tblName + " statement: " + sql);
            System.err.println(ex.getMessage());
        }
        
        connection.close();
    }
}