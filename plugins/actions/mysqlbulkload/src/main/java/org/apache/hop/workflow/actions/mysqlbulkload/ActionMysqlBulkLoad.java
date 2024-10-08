/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.workflow.actions.mysqlbulkload;

import java.io.File;
import java.util.List;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.hop.core.Const;
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.Result;
import org.apache.hop.core.ResultFile;
import org.apache.hop.core.annotations.Action;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopFileException;
import org.apache.hop.core.exception.HopXmlException;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.vfs.HopVfs;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.resource.ResourceEntry;
import org.apache.hop.resource.ResourceEntry.ResourceType;
import org.apache.hop.resource.ResourceReference;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.ActionBase;
import org.apache.hop.workflow.action.IAction;
import org.apache.hop.workflow.action.validator.AbstractFileValidator;
import org.apache.hop.workflow.action.validator.ActionValidatorUtils;
import org.apache.hop.workflow.action.validator.AndValidator;
import org.apache.hop.workflow.action.validator.ValidatorContext;
import org.w3c.dom.Node;

/** This defines a MySQL action. */
@Action(
    id = "MYSQL_BULK_LOAD",
    name = "i18n::ActionMysqlBulkLoad.Name",
    description = "i18n::ActionMysqlBulkLoad.Description",
    image = "MysqlBulkLoad.svg",
    categoryDescription = "i18n:org.apache.hop.workflow:ActionCategory.Category.BulkLoading",
    keywords = "i18n::ActionMysqlBulkLoad.keyword",
    documentationUrl = "/workflow/actions/mysqlbulkload.html")
public class ActionMysqlBulkLoad extends ActionBase implements Cloneable, IAction {
  private static final Class<?> PKG = ActionMysqlBulkLoad.class;
  public static final String CONST_SPACES = "      ";
  public static final String CONST_TABLENAME = "tablename";
  public static final String CONST_FILENAME = "filename";

  private String schemaname;
  private String tableName;
  private String filename;
  private String separator;
  private String enclosed;
  private String escaped;
  private String linestarted;
  private String lineterminated;
  private String ignorelines;
  private boolean replacedata;
  private String listattribut;
  private boolean localinfile;
  public int prorityvalue;
  private boolean addfiletoresult;

  private DatabaseMeta connection;

  public ActionMysqlBulkLoad(String n) {
    super(n, "");
    tableName = null;
    schemaname = null;
    filename = null;
    separator = null;
    enclosed = null;
    escaped = null;
    lineterminated = null;
    linestarted = null;
    replacedata = true;
    ignorelines = "0";
    listattribut = null;
    localinfile = true;
    connection = null;
    addfiletoresult = false;
  }

  public ActionMysqlBulkLoad() {
    this("");
  }

  @Override
  public Object clone() {
    ActionMysqlBulkLoad je = (ActionMysqlBulkLoad) super.clone();
    return je;
  }

  @Override
  public String getXml() {
    StringBuilder retval = new StringBuilder(200);

    retval.append(super.getXml());
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("schemaname", schemaname));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue(CONST_TABLENAME, tableName));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue(CONST_FILENAME, filename));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("separator", separator));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("enclosed", enclosed));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("escaped", escaped));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("linestarted", linestarted));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("lineterminated", lineterminated));

    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("replacedata", replacedata));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("ignorelines", ignorelines));
    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("listattribut", listattribut));

    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("localinfile", localinfile));

    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("prorityvalue", prorityvalue));

    retval.append(CONST_SPACES).append(XmlHandler.addTagValue("addfiletoresult", addfiletoresult));

    retval
        .append(CONST_SPACES)
        .append(
            XmlHandler.addTagValue("connection", connection == null ? null : connection.getName()));

    return retval.toString();
  }

  @Override
  public void loadXml(Node entrynode, IHopMetadataProvider metadataProvider, IVariables variables)
      throws HopXmlException {
    try {
      super.loadXml(entrynode);
      schemaname = XmlHandler.getTagValue(entrynode, "schemaname");
      tableName = XmlHandler.getTagValue(entrynode, CONST_TABLENAME);
      filename = XmlHandler.getTagValue(entrynode, CONST_FILENAME);
      separator = XmlHandler.getTagValue(entrynode, "separator");
      enclosed = XmlHandler.getTagValue(entrynode, "enclosed");
      escaped = XmlHandler.getTagValue(entrynode, "escaped");

      linestarted = XmlHandler.getTagValue(entrynode, "linestarted");
      lineterminated = XmlHandler.getTagValue(entrynode, "lineterminated");
      replacedata = "Y".equalsIgnoreCase(XmlHandler.getTagValue(entrynode, "replacedata"));
      ignorelines = XmlHandler.getTagValue(entrynode, "ignorelines");
      listattribut = XmlHandler.getTagValue(entrynode, "listattribut");
      localinfile = "Y".equalsIgnoreCase(XmlHandler.getTagValue(entrynode, "localinfile"));
      prorityvalue = Const.toInt(XmlHandler.getTagValue(entrynode, "prorityvalue"), -1);
      String dbname = XmlHandler.getTagValue(entrynode, "connection");
      addfiletoresult = "Y".equalsIgnoreCase(XmlHandler.getTagValue(entrynode, "addfiletoresult"));
      connection = DatabaseMeta.loadDatabase(metadataProvider, dbname);
    } catch (HopException e) {
      throw new HopXmlException("Unable to load action of type 'Mysql bulk load' from XML node", e);
    }
  }

  public void setTablename(String tableName) {
    this.tableName = tableName;
  }

  public void setSchemaname(String schemaname) {
    this.schemaname = schemaname;
  }

  public String getSchemaname() {
    return schemaname;
  }

  public String getTablename() {
    return tableName;
  }

  public void setDatabase(DatabaseMeta database) {
    this.connection = database;
  }

  public DatabaseMeta getDatabase() {
    return connection;
  }

  @Override
  public boolean isEvaluation() {
    return true;
  }

  @Override
  public boolean isUnconditional() {
    return true;
  }

  @Override
  public Result execute(Result previousResult, int nr) {
    String replaceIgnore;
    String ignoreNbrLignes = "";
    String listOfColumn = "";
    String localExec = "";
    String priorityText = "";
    String lineTerminatedby = "";
    String fieldTerminatedby = "";

    Result result = previousResult;
    result.setResult(false);

    String vfsFilename = resolve(filename);

    // Let's check the filename ...
    if (!Utils.isEmpty(vfsFilename)) {
      try {
        // User has specified a file, We can continue ...
        //

        // This is running over VFS but we need a normal file.
        // As such, we're going to verify that it's a local file...
        // We're also going to convert VFS FileObject to File
        //
        FileObject fileObject = HopVfs.getFileObject(vfsFilename);
        if (!(fileObject instanceof LocalFile)) {
          // MySQL LOAD DATA can only use local files, so that's what we limit ourselves to.
          //
          throw new HopException(
              "Only local files are supported at this time, file ["
                  + vfsFilename
                  + "] is not a local file.");
        }

        // Convert it to a regular platform specific file name
        //
        String realFilename = HopVfs.getFilename(fileObject);

        // Here we go... back to the regular scheduled program...
        //
        File file = new File(realFilename);
        if ((file.exists() && file.canRead()) || isLocalInfile() == false) {
          // User has specified an existing file, We can continue ...
          if (log.isDetailed()) {
            logDetailed("File [" + realFilename + "] exists.");
          }

          if (connection != null) {
            // User has specified a connection, We can continue ...
            try (Database db = new Database(this, this, connection)) {
              db.connect();
              // Get schemaname
              String realSchemaname = resolve(schemaname);
              // Get tablename
              String realTablename = resolve(tableName);

              if (db.checkTableExists(realSchemaname, realTablename)) {
                // The table existe, We can continue ...
                if (log.isDetailed()) {
                  logDetailed("Table [" + realTablename + "] exists.");
                }

                // Add schemaname (Most the time Schemaname.Tablename)
                if (schemaname != null) {
                  realTablename = realSchemaname + "." + realTablename;
                }

                // Set the REPLACE or IGNORE
                if (isReplacedata()) {
                  replaceIgnore = "REPLACE";
                } else {
                  replaceIgnore = "IGNORE";
                }

                // Set the IGNORE LINES
                if (Const.toInt(getRealIgnorelines(), 0) > 0) {
                  ignoreNbrLignes = "IGNORE " + getRealIgnorelines() + " LINES";
                }

                // Set list of Column
                if (getRealListattribut() != null) {
                  listOfColumn = "(" + MysqlString(getRealListattribut()) + ")";
                }

                // Local File execution
                if (isLocalInfile()) {
                  localExec = "LOCAL";
                }

                // Prority
                if (prorityvalue == 1) {
                  // LOW
                  priorityText = "LOW_PRIORITY";
                } else if (prorityvalue == 2) {
                  // CONCURRENT
                  priorityText = "CONCURRENT";
                }

                // Fields ....
                if (getRealSeparator() != null
                    || getRealEnclosed() != null
                    || getRealEscaped() != null) {
                  fieldTerminatedby = "FIELDS ";

                  if (getRealSeparator() != null) {
                    fieldTerminatedby =
                        fieldTerminatedby
                            + "TERMINATED BY '"
                            + Const.replace(getRealSeparator(), "'", "''")
                            + "'";
                  }
                  if (getRealEnclosed() != null) {
                    fieldTerminatedby =
                        fieldTerminatedby
                            + " ENCLOSED BY '"
                            + Const.replace(getRealEnclosed(), "'", "''")
                            + "'";
                  }
                  if (getRealEscaped() != null) {

                    fieldTerminatedby =
                        fieldTerminatedby
                            + " ESCAPED BY '"
                            + Const.replace(getRealEscaped(), "'", "''")
                            + "'";
                  }
                }

                // LINES ...
                if (getRealLinestarted() != null || getRealLineterminated() != null) {
                  lineTerminatedby = "LINES ";

                  // Line starting By
                  if (getRealLinestarted() != null) {
                    lineTerminatedby =
                        lineTerminatedby
                            + "STARTING BY '"
                            + Const.replace(getRealLinestarted(), "'", "''")
                            + "'";
                  }

                  // Line terminating By
                  if (getRealLineterminated() != null) {
                    lineTerminatedby =
                        lineTerminatedby
                            + " TERMINATED BY '"
                            + Const.replace(getRealLineterminated(), "'", "''")
                            + "'";
                  }
                }

                String sqlBulkLoad =
                    "LOAD DATA "
                        + priorityText
                        + " "
                        + localExec
                        + " INFILE '"
                        + realFilename.replace('\\', '/')
                        + "' "
                        + replaceIgnore
                        + " INTO TABLE "
                        + realTablename
                        + " "
                        + fieldTerminatedby
                        + " "
                        + lineTerminatedby
                        + " "
                        + ignoreNbrLignes
                        + " "
                        + listOfColumn
                        + ";";

                try {
                  // Run the SQL
                  db.execStatement(sqlBulkLoad);

                  if (isAddFileToResult()) {
                    // Add zip filename to output files
                    ResultFile resultFile =
                        new ResultFile(
                            ResultFile.FILE_TYPE_GENERAL,
                            HopVfs.getFileObject(realFilename),
                            parentWorkflow.getWorkflowName(),
                            toString());
                    result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
                  }

                  result.setResult(true);
                } catch (HopDatabaseException je) {
                  result.setNrErrors(1);
                  logError("An error occurred executing this action : " + je.getMessage());
                } catch (HopFileException e) {
                  logError("An error occurred executing this action : " + e.getMessage());
                  result.setNrErrors(1);
                }
              } else {
                // Of course, the table should have been created already before the bulk load
                // operation
                result.setNrErrors(1);
                if (log.isDetailed()) {
                  logDetailed("Table [" + realTablename + "] doesn't exist!");
                }
              }
            } catch (HopDatabaseException dbe) {
              result.setNrErrors(1);
              logError("An error occurred executing this entry: " + dbe.getMessage());
            }
          } else {
            // No database connection is defined
            result.setNrErrors(1);
            logError(BaseMessages.getString(PKG, "ActionMysqlBulkLoad.Nodatabase.Label"));
          }
        } else {
          // the file doesn't exist
          result.setNrErrors(1);
          logError("File [" + realFilename + "] doesn't exist!");
        }
      } catch (Exception e) {
        // An unexpected error occurred
        result.setNrErrors(1);
        logError(BaseMessages.getString(PKG, "ActionMysqlBulkLoad.UnexpectedError.Label"), e);
      }
    } else {
      // No file was specified
      result.setNrErrors(1);
      logError(BaseMessages.getString(PKG, "ActionMysqlBulkLoad.Nofilename.Label"));
    }
    return result;
  }

  public boolean isReplacedata() {
    return replacedata;
  }

  public void setReplacedata(boolean replacedata) {
    this.replacedata = replacedata;
  }

  public void setLocalInfile(boolean localinfile) {
    this.localinfile = localinfile;
  }

  public boolean isLocalInfile() {
    return localinfile;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public String getFilename() {
    return filename;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public void setLineterminated(String lineterminated) {
    this.lineterminated = lineterminated;
  }

  public void setLinestarted(String linestarted) {
    this.linestarted = linestarted;
  }

  public String getEnclosed() {
    return enclosed;
  }

  public String getRealEnclosed() {
    return resolve(getEnclosed());
  }

  public void setEnclosed(String enclosed) {
    this.enclosed = enclosed;
  }

  public String getEscaped() {
    return escaped;
  }

  public String getRealEscaped() {
    return resolve(getEscaped());
  }

  public void setEscaped(String escaped) {
    this.escaped = escaped;
  }

  public String getSeparator() {
    return separator;
  }

  public String getLineterminated() {
    return lineterminated;
  }

  public String getLinestarted() {
    return linestarted;
  }

  public String getRealLinestarted() {
    return resolve(getLinestarted());
  }

  public String getRealLineterminated() {
    return resolve(getLineterminated());
  }

  public String getRealSeparator() {
    return resolve(getSeparator());
  }

  public void setIgnorelines(String ignorelines) {
    this.ignorelines = ignorelines;
  }

  public String getIgnorelines() {
    return ignorelines;
  }

  public String getRealIgnorelines() {
    return resolve(getIgnorelines());
  }

  public void setListattribut(String listattribut) {
    this.listattribut = listattribut;
  }

  public String getListattribut() {
    return listattribut;
  }

  public String getRealListattribut() {
    return resolve(getListattribut());
  }

  public void setAddFileToResult(boolean addfiletoresultin) {
    this.addfiletoresult = addfiletoresultin;
  }

  public boolean isAddFileToResult() {
    return addfiletoresult;
  }

  private String MysqlString(String listcolumns) {
    /*
     * Handle forbiden char like '
     */
    String returnString = "";
    String[] split = listcolumns.split(",");

    for (int i = 0; i < split.length; i++) {
      if (returnString.equals("")) {
        returnString = "`" + Const.trim(split[i]) + "`";
      } else {
        returnString = returnString + ", `" + Const.trim(split[i]) + "`";
      }
    }

    return returnString;
  }

  @Override
  public List<ResourceReference> getResourceDependencies(
      IVariables variables, WorkflowMeta workflowMeta) {
    List<ResourceReference> references = super.getResourceDependencies(variables, workflowMeta);
    ResourceReference reference = null;
    if (connection != null) {
      reference = new ResourceReference(this);
      references.add(reference);
      reference.getEntries().add(new ResourceEntry(connection.getHostname(), ResourceType.SERVER));
      reference
          .getEntries()
          .add(new ResourceEntry(connection.getDatabaseName(), ResourceType.DATABASENAME));
    }
    if (filename != null) {
      String realFilename = getRealFilename();
      if (reference == null) {
        reference = new ResourceReference(this);
        references.add(reference);
      }
      reference.getEntries().add(new ResourceEntry(realFilename, ResourceType.FILE));
    }
    return references;
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      WorkflowMeta workflowMeta,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace(ctx, getVariables());
    AndValidator.putValidators(
        ctx, ActionValidatorUtils.notBlankValidator(), ActionValidatorUtils.fileExistsValidator());
    ActionValidatorUtils.andValidator().validate(this, CONST_FILENAME, remarks, ctx);

    ActionValidatorUtils.andValidator()
        .validate(
            this,
            CONST_TABLENAME,
            remarks,
            AndValidator.putValidators(ActionValidatorUtils.notBlankValidator()));
  }
}
