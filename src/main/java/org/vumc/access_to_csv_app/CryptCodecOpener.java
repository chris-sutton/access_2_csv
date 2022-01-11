/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vumc.access_to_csv_app;
import java.io.File;
import java.io.IOException;
//imports from Jackcess Encrypt
import com.healthmarketscience.jackcess.crypt.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;

import net.ucanaccess.jdbc.JackcessOpenerInterface;

public class CryptCodecOpener implements JackcessOpenerInterface {
  public Database open(File fl,String pwd) throws IOException {
   DatabaseBuilder dbd =new DatabaseBuilder(fl);
   dbd.setAutoSync(false);
   dbd.setCodecProvider(new CryptCodecProvider(pwd));
   dbd.setReadOnly(false);
   return dbd.open();
   
  }
  // Notice that the parameter setting AutoSync=false is recommended with UCanAccess for performance reasons.
  // UCanAccess flushes the updates to disk at transaction end.
  // For more details about autosync parameter (and related tradeoff), see the Jackcess documentation.
}