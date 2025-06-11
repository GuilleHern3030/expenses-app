package enel.dev.budgets.data.sql;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * BasicSQL tiene el objetivo de utilizar Bases de Datos SQL para android de manera sencilla.
 * Una vez llamada la class, se crea la Base de Datos.
 * Siempre que vaya a agregar una fila a una tabla, el array debe contener la misma cantidad de elementos que columnas de la tabla.
 * Siempre que termine de trabajar con esta class, debe llamar a la funcion cerrar() para cerrar la Base de Datos.
 */
@SuppressWarnings("unused")
public class BasicSQL {

    private SQLOpenHelper SQLoh;
    private SQLiteDatabase db;
    private final Context host;
    private final String databaseName;
    private static boolean fixNames;

    public final static String TABLES_LIST = "_tables_";
    public final static String[] TL_COLUMNS = new String[]{"tablename", "columns", "columnsname"};
    public final static int TL_IDCOLUMN_TABLENAME = 0, TL_IDCOLUMN_COLUMNS = 1, TL_IDCOLUMN_COLUMNNAME = 2;

    private final static boolean INITIALIZE_ROW_IN_ZERO = true;
    private final static String PRIMARY_KEY_NAME = "rowid";
    private final static String SEPARATOR = "|";
    public final static int ERROR = -1;

    /**
     * Crea o abre una base de datos
     * Debe recordar que para que la Base de Datos sea cerrada, tiene que llamarse a la función cerrar()
     * @param context Activity
     * @param nombreBaseDeDatos Nombre de la Base de Datos que desea crear o abrir
     */
    public BasicSQL(@Nullable final Context context, @NonNull final String nombreBaseDeDatos) {
        host = context;
        databaseName = forceOnlyLetters(nombreBaseDeDatos);
        SQLoh = new SQLOpenHelper(host, databaseName, TABLES_LIST, PRIMARY_KEY_NAME, TL_COLUMNS);
        db = SQLoh.getWritableDatabase();
        fixNames = true;
    }

    /**
     * Crea o abre una base de datos, especificando si se corregirá el nombre de las tablas automáticamente (true por defecto)
     * Debe recordar que para que la Base de Datos sea cerrada, tiene que llamarse a la función cerrar()
     * @param context Activity
     * @param nombreBaseDeDatos Nombre de la Base de Datos que desea crear o abrir
     * @param corregirNombreTablas true si desea corregir automáticamente los nombres de las tablas (por defecto)
     */
    public BasicSQL(@Nullable final Context context, @NonNull final String nombreBaseDeDatos, boolean corregirNombreTablas) {
        host = context;
        databaseName = forceOnlyLetters(nombreBaseDeDatos);
        SQLoh = new SQLOpenHelper(host, databaseName, TABLES_LIST, PRIMARY_KEY_NAME, TL_COLUMNS);
        db = SQLoh.getWritableDatabase();
        fixNames = corregirNombreTablas;
    }

    /**
     * Cierra la base de datos para finalizar todos los procesos
     */
    public void cerrar() {
        close_database();
    }

    /**
     * Elimina la base de datos abierta actualmente
     */
    public void eliminar() {
        delete_database();
    }


    /**
     * Elimina una base de datos específica
     * @param context Activity
     * @param nombreBaseDeDatos Nombre de la Base de Datos que desea eliminar
     */
    public static void eliminar(@Nullable Context context, @NonNull String nombreBaseDeDatos) {
        BasicSQL db = new BasicSQL(context, nombreBaseDeDatos);
        db.eliminar();
    }

    /**
     * Elimina todas las Bases de Datos existentes
     * @param context Activity
     */
    public static void eliminarTodasLasBasesDeDatos(@NonNull Context context) {
        delete_databases(context);
    }

    /**
     * Obtiene un array de todas las Bases de Datos existentes actualmente
     * @param context Activity
     * @return Array de {@link String} con los nombres de las Bases de Datos existentes actualmente, o null si no lo consigue
     */
    public static String[] listarBasesDeDatos(@Nullable Context context) {
        return list_databases(context);
    }

    /**
     * Obtiene la cantidad de tablas existentes en la Base de Datos
     * @return Número de tablas existentes en la Base de Datos actualmente
     */
    public int cantidadTablas() {
        return tables_in_database();
    }

    /**
     * Obtiene un array de todas las Tablas de la Base de Datos abierta
     * @return Array de {@link String} con los nombres de las Tablas de la Base de Datos, o null si no existe ninguna
     */
    public String[] listarTablas() {
        String[] tablas = list_tables_in_database();
        if(tablas != null && tablas.length > 0) {
            for (int i = 0; i < tablas.length; i++) tablas[i] = restoreSpecialCharacters(tablas[i]);
        } else return null;
        return tablas;
    }

    /**
     * Verifica si una Tabla existe en la Base de Datos
     * @param nombreTabla Nombre de la Tabla que se quiere verificar si existe
     * @return true si la Tabla existe, false si no existe
     */
    public boolean tablaExiste(@NonNull final String nombreTabla) {
        return table_exists(forceOnlyLetters(nombreTabla));
    }

    /**
     * Crea una Tabla en la Base de Datos
     * @param nombreTabla Nombre de la Tabla que se desea crear en la Base de Datos
     * @param nombreColumnas Array de {@link String} que equivale al nombre de las columnas de la Tabla a crear
     * @return true si la Tabla se creó satisfactoriamente, false en otro caso
     */
    public boolean tablaCrear(@NonNull final String nombreTabla, @NonNull final String[] nombreColumnas) {
        return create_table(forceOnlyLetters(nombreTabla), forceOnlyValidColumns(nombreColumnas));
    }

    /**
     * Cambia el nombre de una Tabla de la Base de Datos
     * @param nombreTabla Nombre de la Tabla que se desea cambiar
     * @param nuevoNombreTabla Nombre nuevo que se quiere asignar a la Tabla
     * @return true si el cambio de nombre fue exitoso, false en otro caso
     */
    public boolean tablaCambiarNombre(@NonNull final String nombreTabla, @NonNull final String nuevoNombreTabla) {
        return change_table_name(forceOnlyLetters(nombreTabla), forceOnlyLetters(nuevoNombreTabla));
    }

    /**
     * Elimina una Tabla de la Base de Datos
     * @param nombreTabla Nombre de la Tabla que se desea eliminar
     * @return true si la eliminación fue exitosa, false en otro caso
     */
    public boolean tablaEliminar(@NonNull final String nombreTabla) {
        return delete_table(forceOnlyLetters(nombreTabla));
    }

    /**
     * Elimina todas las tablas de la Base de Datos
     * @return true si la eliminación total fue exitosa, false en otro caso
     */
    public boolean tablaEliminarTodas() {
        return delete_all_tables();
    }

    /**
     * Obtiene la cantidad de filas de una Tabla existente en la Base de Datos
     * @param nombreTabla Nombre de la Tabla de la cual se quiere obtener la cantidad de filas
     * @return Número de filas que la Tabla contiene
     */
    public int tablaFilas(@NonNull final String nombreTabla) {
        return rows_in_table(forceOnlyLetters(nombreTabla));
    }

    /**
     * Obtiene la cantidad de columnas de una Tabla existente en la Base de Datos
     * @param nombreTabla Nombre de la Tabla de la cual se quiere obtener la cantidad de columnas
     * @return Número de columnas que la Tabla contiene
     */
    public int tablaColumnas(@NonNull final String nombreTabla) {
        return _columns_in_table(forceOnlyLetters(nombreTabla));
    }

    /**
     * Obtiene una lista con los nombres de todas las columnas de una Tabla existente en la Base de Datos
     * @param nombreTabla Nombre de la Tabla de la cual se quiere obtener el nombre de sus columnas
     * @return Array de {@link String} con los nombres de las columnas en orden, o null si la Tabla no existe
     */
    public String[] tablaNombreColumnas(@NonNull final String nombreTabla) {
        String[] columnas = _columns_name_in_table(forceOnlyLetters(nombreTabla));
        if(columnas != null && columnas.length > 0) {
            for (int i = 0; i < columnas.length; i++) columnas[i] = restoreSpecialCharacters(columnas[i]);
        } else return null;
        return columnas;
    }

    /**
     * Agrega una fila en una Tabla existente en la Base de Datos
     * @param nombreTabla Nombre de la Tabla a la cual se le quiere agregar una fila
     * @param infoDeLaFila Elementos que se quieren agregar a la fila, que debe coincidir con el número de columnas de la Tabla
     * @return Número de la fila creada o {@link #ERROR} en caso de no lograr crear la fila
     */
    public int tablaIngresarFila(@NonNull final String nombreTabla, final String[] infoDeLaFila) {
        if(nombreTabla.equals(TABLES_LIST)) return ERROR;
        else return add_row(forceOnlyLetters(nombreTabla), infoDeLaFila);
    }

    /**
     * Obtiene una fila de una Tabla existente en la Base de Datos
     * @param nombreTabla Nombre de la Tabla de la cual se quiere obtener una fila
     * @param fila Número de fila que se quiere obtener (inicia en 0 siempre que {@link #INITIALIZE_ROW_IN_ZERO} sea true, por defecto lo es)
     * @return Array de {@link String} con el contenido de cada fila, dividido en sus columnas en orden. null si no encuentra la fila.
     */
    public String[] tablaObtenerFila(@NonNull final String nombreTabla, int fila) {
        return get_row(forceOnlyLetters(nombreTabla), fila);
    }

    /**
     * Busca una fila en específico según los parámetros establecidos
     * @param nombreTabla Nombre de la Tabla en la cual se buscará una fila
     * @param nombreColumna Nombre de la columna donde se realizará la búsqueda
     * @param infoDeLaColumnaBuscado Contenido de la celda que se usará para encontrar la fila
     * @param ignorarMayusculas Establece si las mayúsculas deben ignorarse o no
     * @return Número de la primera fila encontrada con los parámetros establecidos, o {@link #ERROR} si no se encuentra
     */
    public int tablaBuscarFila(@NonNull final String nombreTabla, @NonNull final String nombreColumna, @NonNull final String infoDeLaColumnaBuscado, boolean ignorarMayusculas) {
        return search_row(forceOnlyLetters(nombreTabla), forceOnlyLetters(nombreColumna), infoDeLaColumnaBuscado, ignorarMayusculas);
    }

    /**
     * Busca las filas que cumplan los parámetros establecidos
     * @param nombreTabla Nombre de la Tabla en la cual se buscará una fila
     * @param nombreColumna Nombre de la columna donde se realizará la búsqueda
     * @param infoDeLaColumnaBuscado Contenido de la celda que se usará para encontrar la fila
     * @param ignorarMayusculas Establece si las mayúsculas deben ignorarse o no
     * @return Números de las filas encontrada con los parámetros establecidos
     */
    public int[] tablaBuscarFilas(@NonNull final String nombreTabla, @NonNull final String nombreColumna, @NonNull final String infoDeLaColumnaBuscado, boolean ignorarMayusculas) {
        return search_rows(forceOnlyLetters(nombreTabla), forceOnlyLetters(nombreColumna), infoDeLaColumnaBuscado, ignorarMayusculas);
    }

    /**
     * Edita una fila de una Tabla existente en la Base de Datos
     * @param nombreTabla Nombre de la Tabla donde se quiere realizar una edición
     * @param fila Número de fila que se quiere editar
     * @param infoDeLaFila Nuevo contenido de la fila, cuyos elementos deben coincidir con el número de columnas de la Tabla
     * @return true si fue exitoso, false en otro caso
     */
    public boolean tablaEditarFila(@NonNull final String nombreTabla, final int fila, final String[] infoDeLaFila) {
        if(nombreTabla.equals(TABLES_LIST)) return false;
        else return edit_row(forceOnlyLetters(nombreTabla), fila, infoDeLaFila);
    }

    /**
     * Elimina una fila de una Tabla de la Base de Datos
     * @param nombreTabla Nombre de la Tabla a la cual se le quiere eliminar una fila
     * @param fila Numero de fila que se quiere eliminar
     * @param ordenarTabla Si se desea reordenar la tabla (true) o si se quiere que la última fila de la tabla se mueva a la posición que se acaba de eliminar
     * @return true si fue exitoso, false en otro caso
     */
    public boolean tablaEliminarFila(@NonNull final String nombreTabla, int fila, boolean ordenarTabla) {
        if(nombreTabla.equals(TABLES_LIST)) return false;
        else return delete_row(forceOnlyLetters(nombreTabla), fila, ordenarTabla);
    }

    /**
     * Ordena una Tabla eligiendo una columna específica
     * @param nombreTabla Nombre de la Tabla que se quiere ordenar
     * @param columnaAOrdenar Nombre de la columna que se quiere ordenar
     * @param invertido true si se quiere ordenar de forma invertida
     * @return true si fue exitoso, false en otro caso
     */
    public boolean tablaOrdenar(@NonNull final String nombreTabla, @NonNull final String columnaAOrdenar, boolean invertido) {
        return ordenate_table(nombreTabla, columnaAOrdenar, invertido);
    }

    /**
     * Agrega una columna nueva a una Tabla existente en la Base de Datos actual
     * @param nombreTabla Nombre de la Tabla donde se quiere agregar una columna
     * @param nombreColumnaNueva Nombre de la nueva columna
     * @return true si fue exitoso, false en otro caso
     */
    public boolean tablaAgregarColumna(@NonNull final String nombreTabla, @NonNull String nombreColumnaNueva) {
        return add_columns_to_table(nombreTabla, new String[]{nombreColumnaNueva});
    }

    /**
     * Agrega varias columnas nuevas a una Tabla existente en la Base de Datos actual
     * @param nombreTabla Nombre de la Tabla donde se quiere agregar una columna
     * @param nombreColumnasNuevas Nombres de las nuevas columnas
     * @return true si fue exitoso, false en otro caso
     */
    public boolean tablaAgregarColumnas(@NonNull final String nombreTabla, @NonNull String[] nombreColumnasNuevas) {
        return add_columns_to_table(nombreTabla, nombreColumnasNuevas);
    }

    /**
     * Elimina una columna de una Tabla existente en la Base de Datos actual
     * @param nombreTabla Nombre de la Tabla donde se quiere eliminar una columna
     * @param nombreColumnaABorrar Nombre de la columna que se quiere borrar
     * @return true si fue exitoso, false en otro caso
     */
    public boolean tablaEliminarColumna(@NonNull final String nombreTabla, @NonNull String nombreColumnaABorrar) {
        return delete_column_from_table(nombreTabla, nombreColumnaABorrar);
    }

    //<editor-fold defaultstate="collapsed" desc=" Crear una copia de la base de datos en un directorio ">
    public static void BackupDataBase(@Nullable final Context context, @NonNull String databaseName, String path) {// directorio = Environment.getExternalStorageDirectory(), "folder_name"
        try {
            databaseName = forceOnlyLetters(databaseName);
            SQLOpenHelper SQLoh = new SQLOpenHelper(context, databaseName, TABLES_LIST, PRIMARY_KEY_NAME, TL_COLUMNS);
            SQLiteDatabase db = SQLoh.getWritableDatabase();
            String dbPath = db.getPath();
            @SuppressLint("SimpleDateFormat") String timeStamp = new java.text.SimpleDateFormat("ddMMyyyy_HHmmss").format(new java.util.Date());
            final String inFileName = db.getPath();
            java.io.File dbFile = new java.io.File(inFileName);
            java.io.FileInputStream fis = new java.io.FileInputStream((dbFile));
            java.io.File d = new java.io.File(path);
            boolean exists = d.exists();
            if (!exists) exists = d.mkdir();
            if (exists) {
                String outFileName = path + "/" + databaseName + "_" + timeStamp + ".db";
                java.io.OutputStream output = new java.io.FileOutputStream(outFileName);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) output.write(buffer, 0, length);
                output.flush();
                output.close();
                fis.close();
            }
        } catch (Exception ignored) { }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Validar nombres ">
    private static String forceOnlyLetters(final String text) {
        if(!fixNames) return text;
        if(text.equals(TABLES_LIST)) return TABLES_LIST;
        StringBuilder validName = new StringBuilder();
        if(text.length() > 0) for(int i = 0; i < text.length(); i++) {
            try {
                char character = text.charAt(i);
                if(!isValidChar(character)) validName.append("_").append(((int)character)).append("_");
                else validName.append(character);
            } catch (Exception ignored) { validName.append("x"); }
        } else return "x";
        return validName.toString();
    }

    private static String[] unrepeatableColumns(String[] columns) {
        if(!fixNames) return columns;
        if (columns.length > 1) {
            for (int column = 0; column < columns.length; column++) {
                if (column + 1 != columns.length) {
                    for (int i = column + 1; i < columns.length; i++) {
                        if (columns[i].equals(columns[column])) {
                            columns[i] += "_Rp_";
                            return unrepeatableColumns(columns);
                        }
                    }
                }
            }
        }
        return columns;
    }

    private static String[] forceOnlyValidColumns(String[] columns) {
        if(!fixNames) return columns;
        for(int i = 0; i < columns.length; i++) columns[i] = forceOnlyLetters(columns[i]);
        return unrepeatableColumns(columns);
    }

    private static String restoreSpecialCharacters(String text) {
        if(!fixNames) return text;
        if(text.equals(TABLES_LIST)) return TABLES_LIST;
        StringBuilder tRestored = new StringBuilder();
        if (text.length() > 0) {
            for (int i = 0; i < text.length(); i++) {
                String l = text.substring(i, i + 1);
                if (l.equals("_")) {
                    int _i = i + 1;
                    while (!text.substring(_i, _i + 1).contains("_")) _i++;
                    try {
                        String code = text.substring((i + 1), _i);
                        if (!code.equals("Rp")) {// rp es para indicar que hay una columna repetida
                            String decode = Character.toString((char) Integer.parseInt(code));
                            tRestored.append(decode);
                        }
                    } catch (Exception e) {
                        tRestored.append("?");
                    }
                    i = _i;
                } else tRestored.append(l);
            }
            return tRestored.toString();
        } else return text;
    }

    private static boolean isValidChar(char character) {// Devuelve TRUE si el char pertenece al alfabeto latino o a un número
        if(!fixNames) return true;
        return (character >= 48 && character <= 57 || character >= 65 && character <= 90 || character >= 97 && character <= 122);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" Table methods ">
    private boolean create_table(String tableName, String[] columnsName) {
        if(tableName != null && columnsName != null && tableName.length() > 0 && columnsName.length > 0) try {
            StringBuilder columns = new StringBuilder(PRIMARY_KEY_NAME + " int primary key");
            for (String tColumn : columnsName) columns.append(", ").append(tColumn).append(" text");
            db.execSQL("create table " + tableName + "(" + columns.toString() + ")");
            //<editor-fold defaultstate="collapsed" desc=" Add to _tables_ ">
            String[] arguments = new String[TL_COLUMNS.length];
            Arrays.fill(arguments, "");
            arguments[TL_IDCOLUMN_TABLENAME] = tableName;
            arguments[TL_IDCOLUMN_COLUMNS] = Integer.toString(columnsName.length);
            arguments[TL_IDCOLUMN_COLUMNNAME] = "";
            for(int i = 0; i < columnsName.length; i++) {
                arguments[TL_IDCOLUMN_COLUMNNAME] += columnsName[i];
                if(i+1 != columnsName.length) arguments[TL_IDCOLUMN_COLUMNNAME] += SEPARATOR;
            }
            add_row(TABLES_LIST, arguments);
            //</editor-fold>
            return true;
        } catch(Exception ignored) { }
        return false;
    }

    private boolean delete_table(String tableName) {
        if(tableName != null && tableName.length() > 0 && !tableName.equals(TABLES_LIST)) try {
            db.execSQL("drop table " + tableName);
            //<editor-fold defaultstate="collapsed" desc=" Delete from _tables_ ">
            int table = search_row(TABLES_LIST, TL_COLUMNS, TL_COLUMNS[TL_IDCOLUMN_TABLENAME], tableName, false);
            delete_row(TABLES_LIST, table, false);
            //</editor-fold>
        } catch(Exception ignored) { }
        return !table_exists(tableName);
    }

    private boolean delete_all_tables() {
        boolean result = true;
        String[] tables = list_tables_in_database();
        if(tables != null && tables.length > 0) {
            for (String table : tables) {
                if(!delete_table(table)) result = false;
            }
        }
        return result;
    }

    private boolean table_exists(String tableName) {
        if(tableName != null && tableName.length() > 0) try {
            db.execSQL("create table " + tableName + "(testa int primary key, testb text, testc int)");
            db.execSQL("drop table " + tableName);
            return false;
        } catch (Exception ignored) {
            return true;
        } else return false;
    }

    private int rows_in_table(final String tableName) {
        if(tableName != null && tableName.length() > 0 && table_exists(tableName)) {
            return (int) android.database.DatabaseUtils.queryNumEntries(db, tableName);
        }
        return 0;
    }

    private int last_row(final String tableName) {
        if(!INITIALIZE_ROW_IN_ZERO) return rows_in_table(tableName);
        else return rows_in_table(tableName) - 1;
    }

    private int first_row() {
        if(!INITIALIZE_ROW_IN_ZERO) return 1;
        else return 0;
    }

    private int tables_in_database() {
        try {
            return rows_in_table(TABLES_LIST);
        } catch(Exception e) { return _tables_in_database(); }
    }

    private int _tables_in_database() {
        try { 
            String[] tables = _list_tables_in_database();
            if(tables != null) return tables.length;
        } catch(Exception ignored) { }
        return 0;
    }

    private String[] list_tables_in_database() {
        int tables = tables_in_database();
        if(tables > 0) try {
            String[] table = new String[tables];
            for(int i = 0; i < tables; i++) {
                String[] row = get_row(TABLES_LIST, (i + first_row()));
                table[i] = row[TL_IDCOLUMN_TABLENAME];
            }
            return table;
        } catch(Exception ignored) { return new String[]{"FAILED"}/*_list_tables_in_database()*/; }
        return null;
    }

    private String[] _list_tables_in_database() {
        String[] tables;
        ArrayList<String> _tables = new ArrayList<>();
        try {
            @SuppressLint("Recycle")
            Cursor c = db.rawQuery("SELECT name FROM " + databaseName + " WHERE type='table'", null);
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) { _tables.add(c.getString(0)); c.moveToNext(); }
                if (_tables.size() > 0) {
                    int testTable = ERROR;
                    for(int i = 0; i < _tables.size(); i++) { if(_tables.get(i).equals(TABLES_LIST)) { testTable = i; break; } }
                    _tables.remove(testTable);
                    tables = new String[_tables.size()];
                    for (int i = 0; i < tables.length; i++) tables[i] = _tables.get(i);
                    return tables;
                }
            }
        } catch (Exception ignored) { }
        return null;
    }

    private int _columns_in_table(String tableName) {// column row_id (primaryKeyName) is not counted
        if(tableName != null && tableName.length() > 0 /*&& table_exists(tableName)*/) try {
            @SuppressLint("Recycle")
            Cursor c = db.query(tableName, null, null, null, null, null, null);
            return c.getColumnNames().length - 1;
        } catch (Exception ignored) { }
        return 0;
    }

    private int columns_in_table(String tableName) {// column row_id (primaryKeyName) is not counted
        if(tableName != null && tableName.length() > 0) {
            int tableId = search_row(TABLES_LIST, TL_COLUMNS, TL_COLUMNS[TL_IDCOLUMN_TABLENAME], tableName, false);
            if (tableId >= 0) {
                String[] table = get_row(TABLES_LIST, TL_COLUMNS, tableId);
                try { return Integer.parseInt(table[TL_IDCOLUMN_COLUMNS]); }
                catch (Exception ignored) { return _columns_in_table(tableName); }
            }
        }
        return 0;
    }

    private String[] _columns_name_in_table(String tableName) {// column row_id (primaryKeyName) is not counted
        if(tableName != null && tableName.length() > 0 /*&& table_exists(tableName))*/) try {
            @SuppressLint("Recycle")
            Cursor c = db.query(tableName, null, null, null, null, null, null);
            String[] _columnNames = c.getColumnNames();
            if (_columnNames != null && _columnNames.length > 1 && _columnNames[1].length() > 0) {
                String[] columnsName = new String[_columnNames.length - 1];
                System.arraycopy(_columnNames, 1, columnsName, 0, _columnNames.length - 1);
                return columnsName;
            }
        } catch (Exception ignored) { }
        return null;
    }

    private String[] columns_name_in_table(String tableName) {// column row_id (primaryKeyName) is not counted
        if(tableName != null && tableName.length() > 0) try {
            int tableId = search_row(TABLES_LIST, TL_COLUMNS, TL_COLUMNS[TL_IDCOLUMN_TABLENAME], tableName, false);
            if (tableId >= 0) {
                String[] table = get_row(TABLES_LIST, TL_COLUMNS, tableId);
                return table[TL_IDCOLUMN_COLUMNNAME].split(SEPARATOR);
            }
            return null;
        } catch(Exception ignored) { return _columns_name_in_table(tableName); }
        return null;
    }

    private int add_row(final String tableName, final String[] arguments) {
        if(arguments != null && arguments.length > 0 && tableName != null && tableName.length() > 0) try {
            int newRow = last_row(tableName) + 1;
            String[] columnsName = _columns_name_in_table(tableName);
            if(columnsName != null && columnsName.length == arguments.length) {
                ContentValues registro = new ContentValues();
                registro.put(PRIMARY_KEY_NAME, newRow);
                for (int i = 0; i < arguments.length; i++) registro.put(columnsName[i], arguments[i]);
                db.insert(tableName, null, registro);
                return newRow;
            }
        } catch(Exception ignored) { }// table not exists?
        return ERROR;
    }

    private String[] get_row(final String tableName, int row) {
        if(tableName != null && tableName.length() > 0) return get_row(tableName, _columns_name_in_table(tableName), row);
        else return null;
    }

    private String[] get_row(final String tableName, final String[] columnsName, int row) {
        String[] result;
        String rowToFind = PRIMARY_KEY_NAME + " = " + row;
        if (tableName != null && tableName.length() > 0 && columnsName != null && columnsName.length > 0 && row >= first_row() && row <= last_row(tableName)) try {
            StringBuilder columns = new StringBuilder();
            for (int i = 0; i < columnsName.length; i++) {
                columns.append(columnsName[i]);
                if (i + 1 != columnsName.length) columns.append(", ");
            }
            String rawQueryFormat = "select " + columns.toString() + " from " + tableName + " where " + rowToFind;
            Cursor f = db.rawQuery(rawQueryFormat, null);
            if (f.moveToFirst()) {
                result = new String[columnsName.length];
                for (int i = 0; i < columnsName.length; i++) result[i] = f.getString(i);
            } else { result = new String[]{""}; }
            f.close();
            return result;
        } catch(Exception ignored) { }// table not exists?
        return null;
    }

    private int search_row(final String tableName, final String columnName, final String data, boolean ignoreUpperCase) {
        if(tableName != null && tableName.length() > 0 && columnName != null && columnName.length() > 0 && data != null && data.length() > 0) {
            String[] columns = _columns_name_in_table(tableName);
            if (columns != null && columns.length > 0)
                for (int i = 0; i < columns.length; i++)
                    if (columns[i].equals(columnName))
                        return search_row(tableName, columns, i, data, ignoreUpperCase);
        }
        return ERROR;// error
    }

    private int[] search_rows(final String tableName, final String columnName, final String data, boolean ignoreUpperCase) {
        if(tableName != null && tableName.length() > 0 && columnName != null && columnName.length() > 0 && data != null && data.length() > 0) {
            String[] columns = _columns_name_in_table(tableName);
            if (columns != null && columns.length > 0)
                for (int i = 0; i < columns.length; i++)
                    if (columns[i].equals(columnName))
                        return search_rows(tableName, columns, i, data, ignoreUpperCase);
        }
        return new int[]{};// error
    }

    private int search_row(final String tableName, final String[] columnsName, final String columnName, final String data, boolean ignoreUpperCase) {
        if(tableName != null && tableName.length() > 0 && columnName != null && columnName.length() > 0 && data != null && data.length() > 0
                && columnsName != null && columnsName.length > 0) for(int i = 0; i < columnsName.length; i++) {
            if(columnsName[i].equals(columnName)) return search_row(tableName, columnsName, i, data, ignoreUpperCase);
        } return search_row(tableName, columnName, data, ignoreUpperCase);
    }

    private int search_row(final String tableName, final String[] columnsName, final int idColumn, String data, boolean ignoreUpperCase) {
        int result = ERROR;
        if(tableName != null && tableName.length() > 0 && columnsName != null && columnsName.length > 0 && data != null && data.length() > 0)
        if(ignoreUpperCase) data = data.toLowerCase();
        if(/*table_exists(tableName) && */idColumn >= 0) try {
            int row = first_row();
            while(row <= last_row(tableName) && result == ERROR) {
                String[] rowData = get_row(tableName, columnsName, row);
                if(rowData != null && rowData.length > 0) {
                    if (ignoreUpperCase) rowData[idColumn] = rowData[idColumn].toLowerCase();
                    if (rowData[idColumn].equals(data)) result = row;
                }
                row++;
            }
        } catch(Exception ignored) { result = ERROR; }
        return result;
    }

    private int[] search_rows(final String tableName, final String[] columnsName, final int idColumn, String data, boolean ignoreUpperCase) {
        ArrayList<Integer> results = new ArrayList<>();
        if(tableName != null && tableName.length() > 0 && columnsName != null && columnsName.length > 0 && data != null && data.length() > 0)
            if(ignoreUpperCase) data = data.toLowerCase();
        if(/*table_exists(tableName) && */idColumn >= 0) try {
            int row = first_row();
            while(row <= last_row(tableName)) {
                String[] rowData = get_row(tableName, columnsName, row);
                if(rowData != null && rowData.length > 0) {
                    if (ignoreUpperCase) rowData[idColumn] = rowData[idColumn].toLowerCase();
                    if (rowData[idColumn].equals(data)) results.add(row);
                }
                row++;
            }
        } catch(Exception ignored) { return new int[]{}; }

        final int[] resultsArray = new int[results.size()];
        for (int i = 0; i < resultsArray.length; i++)
            resultsArray[i] = results.get(i);
        return resultsArray;
    }

    private boolean edit_row(final String tableName, final int row, final String[] arguments) {
        if(tableName != null && tableName.length() > 0) return edit_row(tableName, _columns_name_in_table(tableName), row, arguments);
        else return false;
    }
    private boolean edit_row(final String tableName, final String[] columnsName, final int row, final String[] arguments) {
        if (tableName != null && arguments != null && columnsName != null && tableName.length() > 0 && columnsName.length > 0) try {
            if (row >= first_row() && row <= last_row(tableName) && columnsName.length == arguments.length) {
                ContentValues registro = new ContentValues();
                registro.put(PRIMARY_KEY_NAME, row);
                for (int i = 0; i < columnsName.length; i++) registro.put(columnsName[i], arguments[i]);
                int success = db.update(tableName, registro, PRIMARY_KEY_NAME + " = " + row, null);
                if (success == 1) return true;
            }
        } catch(Exception ignored) { }
        return false;
    }

    private boolean delete_row(final String tableName, int row, boolean ordenate) {
        if(tableName != null && tableName.length() > 0) {
            int lastRow = last_row(tableName);
            int rows = rows_in_table(tableName);
            String rowToFind = PRIMARY_KEY_NAME + " = " + lastRow;
            if (rows > 0 && row >= first_row() && row <= lastRow) try {
                String[] columnsName = _columns_name_in_table(tableName);
                if(row == lastRow) {
                    int success = db.delete(tableName, rowToFind, null);
                    return (success == 1);
                }
                else if(!ordenate) edit_row(tableName, columnsName, row, get_row(tableName, columnsName, lastRow));
                else for(int r = row; r <= lastRow - 1; r++) edit_row(tableName, columnsName, r, get_row(tableName, columnsName, r+1));
                int success = db.delete(tableName, rowToFind, null);
                return (success == 1);
            } catch(Exception ignored) { }
        }
        return false;
    }

    private int oChar(char character) {
        if(character >= 48 && character <= 57) return character+17;// numbers (65 - 74)
        else if(character >= 65 && character <= 90) return character+10;// alphabetic letters shift (75 - 100)
        else if(character >= 97 && character <= 122) return character+4;// alphabetic letters (101 - 126)
        else if(character < 128) return character-128;
        else return character;
    }

    private boolean ordenate_table(final String tableName, final String columnToOrdenate, boolean inverted) {
        if(tableName != null && tableName.length() > 0 && columnToOrdenate != null && columnToOrdenate.length() > 0) try {
            String[] columnsName = _columns_name_in_table(tableName);
            if(columnsName != null && columnsName.length > 0) {
                int idColumn = 0; while(idColumn < columnsName.length && !columnsName[idColumn].equals(columnToOrdenate)) idColumn++;
                if(idColumn < columnsName.length && columnsName[idColumn].equals(columnToOrdenate)) {
                    String[] row0, row1;
                    int tope = rows_in_table(tableName);
                    for(int j = first_row(); j < tope - 1; j++) {
                        for(int i = first_row(); i < tope - 1 - j; i++) {
                            int letra = 0;
                            row0 = get_row(tableName, columnsName, i);
                            row1 = get_row(tableName, columnsName, i+1);
                            try {
                                while (oChar(row0[idColumn].charAt(letra)) == oChar(row1[idColumn].charAt(letra))
                                        && letra != row0[idColumn].length() && letra != row1[idColumn].length()) {
                                    letra++;
                                }
                            } catch(Exception ignored) { letra = 0; }
                            if(inverted && oChar(row0[idColumn].charAt(letra)) < oChar(row1[idColumn].charAt(letra))
                            || !inverted && oChar(row0[idColumn].charAt(letra)) > oChar(row1[idColumn].charAt(letra))) {
                                edit_row(tableName, columnsName, i, row1);
                                edit_row(tableName, columnsName, i+1, row0);
                            }
                        }
                    } return true;
                }
            }
        } catch(Exception ignored) { } return false;
    }

    private boolean change_table_name(final String tableName, final String newTableName) {
        if(tableName != null && tableName.length() > 0 && newTableName != null && newTableName.length() > 0) try {
            if (table_exists(tableName) && !table_exists(newTableName)) {
                String[] columnsName = _columns_name_in_table(tableName);
                if(columnsName != null && columnsName.length > 0) {
                    create_table(newTableName, columnsName);
                    int rows = rows_in_table(tableName);
                    for (int row = 0; row < rows; row++) add_row(newTableName, get_row(tableName, columnsName, row));
                    delete_table(tableName);
                }
            }
            return table_exists(newTableName);
        } catch(Exception ignored) { }
        return false;
    }

    private boolean add_columns_to_table(final String tableName, final String[] newColumnsName) {
        if(tableName != null && tableName.length() > 0 && newColumnsName != null && newColumnsName.length > 0) try {
            String tmpTableName = "_TMP_";
            if (table_exists(tableName)) {
                String[] columnsName = _columns_name_in_table(tableName);
                if(columnsName != null && columnsName.length > 0) {
                    String[] finalColumnsName = new String[columnsName.length + newColumnsName.length];
                    System.arraycopy(columnsName, 0, finalColumnsName, 0, columnsName.length);
                    System.arraycopy(newColumnsName, 0, finalColumnsName, columnsName.length, finalColumnsName.length - columnsName.length);
                    change_table_name(tableName, tmpTableName);
                    delete_table(tableName);
                    create_table(tableName, finalColumnsName);
                    int rows = rows_in_table(tmpTableName);
                    for (int row = 0; row < rows; row++) {
                        String[] arguments = get_row(tmpTableName, columnsName, row);
                        String[] newArguments = new String[finalColumnsName.length];
                        System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
                        for(int i = arguments.length; i < newColumnsName.length; i++) newArguments[i] = "";
                        add_row(tableName, newArguments);
                    } return true;
                }
            }
        } catch(Exception ignored) {
            if(newColumnsName.length == 1) return add_column_to_table(tableName, newColumnsName[0]);
        }
        return false;
    }

    private boolean add_column_to_table(final String tableName, final String newColumnName) {
        if(tableName != null && tableName.length() > 0 && newColumnName != null && newColumnName.length() > 0) try {
            String tmpTableName = "_TMP_";
            if (table_exists(tableName)) {
                String[] columnsName = _columns_name_in_table(tableName);
                if(columnsName != null && columnsName.length > 0) {
                    String[] newColumnsName = new String[columnsName.length + 1];
                    System.arraycopy(columnsName, 0, newColumnsName, 0, columnsName.length);
                    newColumnsName[newColumnsName.length - 1] = newColumnName;
                    change_table_name(tableName, tmpTableName);
                    delete_table(tableName);
                    create_table(tableName, newColumnsName);
                    int rows = rows_in_table(tmpTableName);
                    for (int row = 0; row < rows; row++) {
                        String[] arguments = get_row(tmpTableName, columnsName, row);
                        String[] newArguments = new String[newColumnsName.length];
                        System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
                        newArguments[newArguments.length - 1] = "";
                        add_row(tableName, newArguments);
                    } return true;
                }
            }
        } catch(Exception ignored) { }
        return false;
    }

    private boolean delete_column_from_table(final String tableName, final String columnNameToDelete) {
        if(tableName != null && tableName.length() > 0 && columnNameToDelete != null && columnNameToDelete.length() > 0) try {
            String tmpTableName = "_TMP_";
            if (table_exists(tableName)) {
                String[] columnsName = _columns_name_in_table(tableName);
                if(columnsName != null && columnsName.length > 0) {
                    int idColumn = -1; for(int i = 0; i < columnsName.length; i++) { if(columnNameToDelete.equals(columnsName[i])) idColumn = i; }
                    if(idColumn > -1) {
                        String[] newColumnsName = new String[columnsName.length - 1];
                        for (int i = 0, j = 0; i < columnsName.length; i++) {
                            if (idColumn != i) {
                                newColumnsName[j] = columnsName[i];
                                j++;
                            }
                        }
                        change_table_name(tableName, tmpTableName);
                        delete_table(tableName);
                        create_table(tableName, newColumnsName);
                        int rows = rows_in_table(tmpTableName);
                        for (int row = 0; row < rows; row++) {
                            String[] arguments = get_row(tmpTableName, columnsName, row);
                            String[] newArguments = new String[newColumnsName.length];
                            for (int i = 0, j = 0; i < columnsName.length; i++) {
                                if (idColumn != i) {
                                    newArguments[j] = arguments[i];
                                    j++;
                                }
                            }
                            add_row(tableName, newArguments);
                        } return true;
                    }
                }
            }
        } catch(Exception ignored) { }
        return false;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" DataBase methods ">
    private void delete_database() {
        //String path = db.getPath();
        close_database();
        if (!db.isOpen()) try {
            destroy_database(host, databaseName);
        } catch(Exception ignored) { }
        db = null;
    }

    private void close_database() {
        try { db.close(); } catch (Exception ignored) { }
        try { SQLoh.close(); } catch (Exception ignored) { }
        SQLoh = null;
    }

    private static void destroy_database(Context host, String databaseName) {
        host.deleteDatabase(databaseName);
        BasicSQL sql = new BasicSQL(host, TABLES_LIST);
        try {
            int row = sql.search_row(TABLES_LIST, TL_COLUMNS, TL_COLUMNS[TL_IDCOLUMN_TABLENAME], databaseName, false);
            sql.delete_row(TABLES_LIST, row, false);
        } catch(Exception ignored) { }
        sql.close_database();
    }

    private static int databases_created(Context host) {
        int dbs;
        BasicSQL sql = new BasicSQL(host, TABLES_LIST);
        try {
            dbs = sql.rows_in_table(TABLES_LIST);
        } catch(Exception ignored) { dbs = 0; }
        sql.close_database();
        return dbs;
    }

    private static String[] list_databases(Context host) {
        String[] databases = null;
        BasicSQL sql = new BasicSQL(host, TABLES_LIST);
        try {
            int dbs = databases_created(host);
            String[] db = new String[dbs];
            if (dbs > 0) for (int i = sql.first_row(); i <= sql.last_row(TABLES_LIST); i++) {
                String[] tableData = sql.get_row(TABLES_LIST, TL_COLUMNS, i);
                db[i] = tableData[TL_IDCOLUMN_TABLENAME];
            }
            return db;
        } catch(Exception ignored) { }
        return null;
    }

    private static void delete_databases(Context host) {
        String[] databases = list_databases(host);
        if(databases != null && databases.length > 0) for (String database : databases) {
            try {
                host.deleteDatabase(database);
            } catch (Exception ignored) {
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc=" SQLite ">
    private static class SQLOpenHelper extends SQLiteOpenHelper {

        private final String tablesTable;
        private final String primaryKeyName;
        private final String[] tablesColumns;
        private final String dbName;
        private final Context context;

        public SQLOpenHelper(@Nullable Context context, @NonNull String dbName, @NonNull String tablesTable, @NonNull String primaryKeyName, @NonNull String[] tablesColumns) {
            super(context, dbName, null, 1);
            this.dbName = dbName;
            this.tablesTable = tablesTable;
            this.primaryKeyName = primaryKeyName;
            this.tablesColumns = tablesColumns;
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase BaseDeDatos) {
            StringBuilder columns = new StringBuilder();
            for (int i = 0; i < tablesColumns.length; i++) {
                columns.append(tablesColumns[i]);
                if (i + 1 != tablesColumns.length) columns.append(", ");
            }
            try {
                BaseDeDatos.execSQL("create table " + tablesTable + "(" + primaryKeyName + " int primary key, " + columns + ")");
            } catch (Exception ignored) {
            }
            if(!dbName.equals(tablesTable)) createDataBasesTable();
        }

        @Override
        public void onUpgrade(SQLiteDatabase BaseDeDatos, int i, int i1) {
        }

        private void createDataBasesTable() {
            BasicSQL sql = new BasicSQL(context,tablesTable);
            String[] row = BasicSQL.TL_COLUMNS;
            Arrays.fill(row, "");
            row[TL_IDCOLUMN_TABLENAME] = dbName;
            sql.add_row(tablesTable, row);
            sql.close_database();
        }
    }//</editor-fold>

    public static String[] invertirLista(final String[] lista) {
        if (lista.length > 1) {
            try {
                String[] invertedList = new String[lista.length];
                for (int i = 0; i < lista.length; i++)
                    invertedList[i] = lista[(lista.length - i - 1)];
                return invertedList;
            } catch (Exception e) {
                return lista;
            }
        } else return lista;
    }

    public static String[] sortLista(final String[] lista) {
        if (lista.length > 1) {
            try {
                String[] sortedList = new String[lista.length];
                ArrayList<String> _lista = new ArrayList<>();
                java.util.Collections.addAll(_lista, lista);
                java.util.Collections.sort(_lista);
                for (int i = 0; i < lista.length; i++) sortedList[i] = _lista.get(i);
                return sortedList;
            } catch (Exception e) {
                return lista;
            }
        } else return lista;
    }
}