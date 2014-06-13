package com.ibabai.android.proto;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME="ibabai.db";
	private static final int SCHEMA_VERSION=1;
	static final String DATE="date";
	static final String AGENT="agent";
	static final String AMOUNT="amount";
	static final String S_ID="store_id";
	static final String C_ID="city_id";
	static final String LAT = "latitude";
	static final String LON = "longitude";
	static final String P_ID="promoact_id";
	static final String CL_ID="client_id";
	static final String CL_NAME="client_name";
	static final String CODE="barcode";
	static final String DEL="delivery";
	static final String VIEW="view";
	static final String PURCH="purchase";
	static final String REW1="rew1";
	static final String REW2="rew2";
	static final String MULT="multiple";
	static final String STOP="stopped";
	static final String RAD = "radius";
	static final String V_ID="vendor_id";
	static final String V_NAME="vendor_name";
	static final String TYPE="type";
	static final String TABLE="logbook";
	static final String TABLE_C="cities";
	static final String TABLE_S="stores";
	static final String TABLE_P="promoacts";
	static final String TABLE_V="vendors";
	private static DatabaseHelper singleton=null;
	private Context ctxt=null;
	
	synchronized static DatabaseHelper getInstance(Context ctxt) {
		if (singleton == null) {
			singleton = new DatabaseHelper(ctxt.getApplicationContext());
		}
		return (singleton);
	}
	
	private DatabaseHelper(Context ctxt) {
		super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
		this.ctxt = ctxt;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE logbook (_id INTEGER PRIMARY KEY, date TEXT, agent TEXT, amount TEXT, type TEXT);");
		db.execSQL("CREATE TABLE stores (_id INTEGER PRIMARY KEY, store_id INTEGER, city_id INTEGER, latitude REAL, longitude REAL);");
		db.execSQL("CREATE TABLE promoacts (_id INTEGER PRIMARY KEY, promoact_id INTEGER, client_id INTEGER, client_name STRING, barcode STRING, delivery INTEGER DEFAULT 0, view INTEGER DEFAULT 0, purchase INTEGER DEFAULT 0, rew1 INTEGER DEFAULT 0,  rew2 INTEGER DEFAULT 0, multiple INTEGER, stopped INTEGER DEFAULT 0);");
		db.execSQL("CREATE TABLE vendors (_id INTEGER PRIMARY KEY, vendor_id INTEGER, vendor_name STRING);");
		db.execSQL("CREATE TABLE cities (_id INTEGER PRIMARY KEY, city_id INTEGER, latitude REAL, longitude REAL, radius INTEGER);");
				
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new RuntimeException(ctxt.getString(R.string.on_upgrade_error));
	}
	public void AddCity(City c) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues city_cv = new ContentValues();
		city_cv.put(DatabaseHelper.C_ID, c.getCityId());
		city_cv.put(DatabaseHelper.LAT, c.getCityLat());
		city_cv.put(DatabaseHelper.LON, c.getCityLon());
		city_cv.put(DatabaseHelper.RAD, c.getCityRadius());
		db.insert(DatabaseHelper.TABLE_C, DatabaseHelper.C_ID, city_cv);
		db.close();
	}
	public void AddVendor(Vendor v) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues ven_cv = new ContentValues();
		ven_cv.put(DatabaseHelper.V_ID, v.getVendorId());
		ven_cv.put(DatabaseHelper.V_NAME, v.getVendorName());						 
		db.insert(DatabaseHelper.TABLE_V, DatabaseHelper.V_ID, ven_cv);
		db.close();
	}

}
